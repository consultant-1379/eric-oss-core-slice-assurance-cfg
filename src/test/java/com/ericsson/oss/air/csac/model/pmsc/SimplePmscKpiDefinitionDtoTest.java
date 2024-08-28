/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.model.pmsc;

import static com.ericsson.oss.air.csac.model.TestResourcesUtils.*;
import static org.junit.jupiter.api.Assertions.*;

import jakarta.validation.Validation;

import com.ericsson.oss.air.util.codec.Codec;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;
import org.springframework.util.ObjectUtils;

class SimplePmscKpiDefinitionDtoTest {

    private static final Codec codec = new Codec(Validation.buildDefaultValidatorFactory().getValidator());

    @Test
    void testSimplePmscKpiDefinitionDto_withCommonRequiredFields() throws JsonProcessingException {
        final SimplePmscKpiDefinitionDto simpleKpiDefDto = codec.withValidation().readValue(NEW_SIMPLE_KPI_DEF_STR, SimplePmscKpiDefinitionDto.class);
        assertFalse(ObjectUtils.isEmpty(simpleKpiDefDto.getName()));
        assertFalse(ObjectUtils.isEmpty(simpleKpiDefDto.getExpression()));
        assertFalse(ObjectUtils.isEmpty(simpleKpiDefDto.getObjectType()));
        assertFalse(ObjectUtils.isEmpty(simpleKpiDefDto.getAggregationType()));
        assertTrue(ObjectUtils.isEmpty(simpleKpiDefDto.getExportable()));
    }

    @Test
    void testSimplePmscKpiDefinitionDto_missingRequiredField_throwNPE() {
        assertThrows(NullPointerException.class, () -> SimplePmscKpiDefinitionDto.builder().build());
    }
}
