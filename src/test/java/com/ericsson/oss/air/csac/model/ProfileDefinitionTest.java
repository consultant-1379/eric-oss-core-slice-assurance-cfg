/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;

import com.ericsson.oss.air.util.codec.Codec;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;

class ProfileDefinitionTest {

    private static final String VALID_NAME = "profiledef_name";
    private static final String EMPTY_NAME = "";

    private static final String DESCRIPTION = "profiledef decription";

    private static final String VALID_AGGREGATION_FIELD = "field";
    private static final List<String> VALID_AGGREGATION_FIELDS = List.of(VALID_AGGREGATION_FIELD);

    private static final String VALID_REF = "kpi_reference";

    private static final String AUGMENTATION = "aug1";

    // define KPI References
    private static final KPIReference VALID_KPI_REF = KPIReference.builder().ref(VALID_REF).build();
    private static final List<KPIReference> VALID_LIST_KPI_REFS = List.of(VALID_KPI_REF);
    private static final List<KPIReference> EMPTY_KPI_REFS = List.of();

    private static final String VALID_PROFILE_DEF_LEGACY =
            "{\"name\":\"" + VALID_NAME + "\", " + "\"description\":\"" + DESCRIPTION + "\", " + "\"aggregation_fields\":[\"" + VALID_AGGREGATION_FIELD
                    + "\"], " + "\"kpis\":[" + "{\"ref\":\"" + VALID_REF + "\"}]}";

    private static final String VALID_PROFILE_DEF =
            "{\"name\":\"" + VALID_NAME + "\", " + "\"description\":\"" + DESCRIPTION + "\", " + "\"context\":[\"" + VALID_AGGREGATION_FIELD
                    + "\"], " + "\"kpis\":[" + "{\"ref\":\"" + VALID_REF + "\"}]}";

    private static final String PROFILE_DEF_WITH_EMPTY_NAME =
            "{\"name\":\"" + EMPTY_NAME + "\", " + "\"description\":\"" + DESCRIPTION + "\", " + "\"aggregation_fields\":[\"" + VALID_AGGREGATION_FIELD
                    + "\"], " + "\"kpis\":[" + "{\"ref\":\"" + VALID_REF + "\"}]}";

    private static final String PROFILE_DEF_WITH_EMPTY_DESCRIPTION =
            "{\"name\":\"" + VALID_NAME + "\", " + "\"description\":\"\", " + "\"aggregation_fields\":[\"" + VALID_AGGREGATION_FIELD + "\"], "
                    + "\"kpis\":[" + "{\"ref\":\"" + VALID_REF + "\"}]}";

    private static final String PROFILE_DEF_WITH_NO_DESCRIPTION =
            "{\"name\":\"" + VALID_NAME + "\", " + "\"aggregation_fields\":[\"" + VALID_AGGREGATION_FIELD + "\"], " + "\"kpis\":[" + "{\"ref\":\""
                    + VALID_REF + "\"}]}";

    private static final String PROFILE_DEF_WITH_EMPTY_AGGREGATION_FIELD =
            "{\"name\":\"" + VALID_NAME + "\", " + "\"description\":\"" + DESCRIPTION + "\", " + "\"aggregation_fields\":[], " + "\"kpis\":["
                    + "{\"ref\":\"" + VALID_REF + "\"}]}";

    private static final String PROFILE_DEF_WITH_EMPTY_KPIS =
            "{\"name\":\"" + VALID_NAME + "\", " + "\"description\":\"" + DESCRIPTION + "\", " + "\"aggregation_fields\":[\"" + VALID_AGGREGATION_FIELD
                    + "\"], " + "\"kpis\":[]}";

    private static final String VALID_PROFILE_DEF_WITH_AUGMENTATION =
            "{\"name\":\"" + VALID_NAME + "\", " + "\"description\":\"" + DESCRIPTION + "\", " + "\"augmentation\": \"" + AUGMENTATION + "\"," + "\"context\":[\"" + VALID_AGGREGATION_FIELD
                    + "\"], " + "\"kpis\":[" + "{\"ref\":\"" + VALID_REF + "\"}]}";

    private final Codec codec = new Codec(Validation.buildDefaultValidatorFactory().getValidator());

    @Test
    public void testValidProfileDef_Legacy() throws JsonProcessingException {

        final ProfileDefinition expectedProfileDef = ProfileDefinition.builder()
                .name(VALID_NAME)
                .description(DESCRIPTION)
                .context(VALID_AGGREGATION_FIELDS)
                .kpis(VALID_LIST_KPI_REFS)
                .build();

        final ProfileDefinition profileDef = this.codec.withValidation().readValue(VALID_PROFILE_DEF_LEGACY, ProfileDefinition.class);

        assertNotNull(profileDef);
        assertEquals(expectedProfileDef, profileDef);
    }

    @Test
    public void testValidProfileDef() throws Exception {

        final ProfileDefinition expected = ProfileDefinition.builder()
                .name(VALID_NAME)
                .description(DESCRIPTION)
                .context(VALID_AGGREGATION_FIELDS)
                .kpis(VALID_LIST_KPI_REFS)
                .build();

        final ProfileDefinition actual = this.codec.withValidation().readValue(VALID_PROFILE_DEF, ProfileDefinition.class);

        assertEquals(1, actual.getContext().size());
        assertEquals(expected, actual);
    }

    @Test
    public void testProfileDefWithEmptyDescription() throws JsonProcessingException {
        final ProfileDefinition expectedProfileDef = ProfileDefinition.builder()
                .name(VALID_NAME)
                .description("")
                .context(VALID_AGGREGATION_FIELDS)
                .kpis(VALID_LIST_KPI_REFS)
                .build();
        final ProfileDefinition profileDef = this.codec.withValidation().readValue(PROFILE_DEF_WITH_EMPTY_DESCRIPTION, ProfileDefinition.class);

        assertNotNull(profileDef);
        assertEquals(this.codec.writeValueAsStringPretty(expectedProfileDef), this.codec.writeValueAsStringPretty(profileDef));
        assertEquals(expectedProfileDef, profileDef);
    }

    @Test
    public void testProfileDefWithNoDescription() throws JsonProcessingException {
        final ProfileDefinition expectedProfileDef = ProfileDefinition.builder()
                .name(VALID_NAME)
                .description(null)
                .context(VALID_AGGREGATION_FIELDS)
                .kpis(VALID_LIST_KPI_REFS)
                .build();
        final ProfileDefinition profileDef = this.codec.withValidation().readValue(PROFILE_DEF_WITH_NO_DESCRIPTION, ProfileDefinition.class);

        assertNotNull(profileDef);
        assertEquals(this.codec.writeValueAsStringPretty(expectedProfileDef), this.codec.writeValueAsStringPretty(profileDef));
        assertEquals(expectedProfileDef, profileDef);
    }

    @Test
    public void testProfileDefWithEmptyKpis() throws JsonProcessingException {
        final ProfileDefinition expectedProfileDef =
                ProfileDefinition.builder()
                        .name(VALID_NAME)
                        .description(DESCRIPTION)
                        .context(VALID_AGGREGATION_FIELDS)
                        .kpis(EMPTY_KPI_REFS)
                        .build();
        final ProfileDefinition profileDef = this.codec.withValidation().readValue(PROFILE_DEF_WITH_EMPTY_KPIS, ProfileDefinition.class);

        assertNotNull(profileDef);
        assertEquals(this.codec.writeValueAsStringPretty(expectedProfileDef), this.codec.writeValueAsStringPretty(profileDef));
        assertEquals(expectedProfileDef, profileDef);
    }

    @Test
    public void testProfileDefWithEmptyName() {
        assertThrows(ConstraintViolationException.class,
                () -> this.codec.withValidation().readValue(PROFILE_DEF_WITH_EMPTY_NAME, ProfileDefinition.class));
    }

    @Test
    public void testProfileDefWithEmptyContext() {
        assertThrows(ConstraintViolationException.class,
                () -> this.codec.withValidation().readValue(PROFILE_DEF_WITH_EMPTY_AGGREGATION_FIELD, ProfileDefinition.class));
    }

    @Test
    public void testValidProfileWithAugmentation() throws Exception {

        final ProfileDefinition expected = ProfileDefinition.builder()
                .name(VALID_NAME)
                .description(DESCRIPTION)
                .augmentation(AUGMENTATION)
                .context(VALID_AGGREGATION_FIELDS)
                .kpis(VALID_LIST_KPI_REFS)
                .build();

        final ProfileDefinition actual = this.codec.withValidation().readValue(VALID_PROFILE_DEF_WITH_AUGMENTATION, ProfileDefinition.class);

        assertEquals(AUGMENTATION, actual.getAugmentation());
        assertEquals(expected, actual);

    }
}