/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.stream.Stream;

import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;

import com.ericsson.oss.air.util.codec.Codec;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class PMDefinitionTest {

    private static final String VALID_SIMPLE_NAME = "pmdef_name";
    private static final String VALID_NAME = "pmCounters." + VALID_SIMPLE_NAME;
    private static final String VALID_NESTED_NAME = "outerRecord." + VALID_NAME;
    private static final String INVALID_NAME = " invalid pmdef_name";

    private static final String VALID_SOURCE = "pmdef_source";
    private static final String INVALID_SOURCE = " ";

    private static final String DESCRIPTION = "pmdef decription";

    private static final String VALID_PM_DEF_WITH_EMPTY_DESC =
        "{\"name\":\"" + VALID_NAME + "\",\"source\":\"" + VALID_SOURCE + "\",\"description\":\"\"}";
    private static final String VALID_PM_DEF_WITH_NO_DESC = "{\"name\":\"" + VALID_NAME + "\",\"source\":\"" + VALID_SOURCE + "\"}";

    private Codec codec = new Codec(Validation.buildDefaultValidatorFactory().getValidator());

    @ParameterizedTest
    @MethodSource("provideArguments")
    void testValidPMDef(final String name, final String source, final String description) throws JsonProcessingException {
        final PMDefinition expectedPmDef = new PMDefinition(name, source, description);
        final PMDefinition pmDef = this.codec.withValidation().readValue(getPMDefAsJsonString(name, source, description), PMDefinition.class);

        assertNotNull(pmDef);
        assertEquals(expectedPmDef, pmDef);
    }

    @Test
    public void testPMDefWithEmptyDescription() throws JsonProcessingException {
        final PMDefinition expectedPmDef = new PMDefinition(VALID_NAME, VALID_SOURCE, "");
        final PMDefinition pmDef = this.codec.withValidation().readValue(VALID_PM_DEF_WITH_EMPTY_DESC, PMDefinition.class);

        assertNotNull(pmDef);
        assertEquals(this.codec.writeValueAsStringPretty(expectedPmDef), codec.writeValueAsStringPretty(pmDef));
        assertEquals(expectedPmDef, pmDef);
    }

    @Test
    public void testPMDefWithNoDescription() throws JsonProcessingException {
        final PMDefinition expectedPmDef = new PMDefinition(VALID_NAME, VALID_SOURCE, null);
        final PMDefinition pmDef = this.codec.withValidation().readValue(VALID_PM_DEF_WITH_NO_DESC, PMDefinition.class);

        assertNotNull(pmDef);
        assertEquals(this.codec.writeValueAsStringPretty(expectedPmDef), codec.writeValueAsStringPretty(pmDef));
        assertEquals(expectedPmDef, pmDef);
    }

    @Test
    public void testPMDefWithInvalidNameValue() {
        assertThrows(ConstraintViolationException.class,
                () -> this.codec.withValidation().readValue(getPMDefAsJsonString(INVALID_NAME, VALID_SOURCE, DESCRIPTION),
                        PMDefinition.class));
    }

    @Test
    public void testPMDefWithBlankSource() {
        assertThrows(ConstraintViolationException.class,
                () -> this.codec.withValidation().readValue(getPMDefAsJsonString(VALID_NAME, INVALID_SOURCE, DESCRIPTION),
                        PMDefinition.class));
    }

    private static Stream<Arguments> provideArguments() {
        return Stream.of(
                Arguments.of(VALID_NAME, VALID_SOURCE, DESCRIPTION),
                Arguments.of(VALID_SIMPLE_NAME, VALID_SOURCE, DESCRIPTION),
                Arguments.of(VALID_NESTED_NAME, VALID_SOURCE, DESCRIPTION)
        );
    }

    private static String getPMDefAsJsonString(final String name, final String source, final String description){
        return "{\"name\":\"" + name + "\",\"source\":\"" + source + "\",\"description\":\"" + description + "\"}";
    }

}