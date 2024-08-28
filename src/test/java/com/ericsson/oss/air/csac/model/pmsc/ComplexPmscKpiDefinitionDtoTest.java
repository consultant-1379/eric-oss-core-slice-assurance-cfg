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

class ComplexPmscKpiDefinitionDtoTest {

    private static final Codec codec = new Codec(Validation.buildDefaultValidatorFactory().getValidator());

    @Test
    void testComplexPmscKpiDefinitionDto_withCommonRequiredFields() throws JsonProcessingException {
        final ComplexPmscKpiDefinitionDto complexPmscKpiDefinitionDto = codec.withValidation().readValue(NEW_COMPLEX_KPI_DEF_STR, ComplexPmscKpiDefinitionDto.class);
        assertFalse(ObjectUtils.isEmpty(complexPmscKpiDefinitionDto.getName()));
        assertFalse(ObjectUtils.isEmpty(complexPmscKpiDefinitionDto.getExpression()));
        assertFalse(ObjectUtils.isEmpty(complexPmscKpiDefinitionDto.getObjectType()));
        assertFalse(ObjectUtils.isEmpty(complexPmscKpiDefinitionDto.getAggregationType()));
        assertTrue(ObjectUtils.isEmpty(complexPmscKpiDefinitionDto.getExportable()));
        assertTrue(ObjectUtils.isEmpty(complexPmscKpiDefinitionDto.getExecutionGroup()));
    }

    @Test
    void testComplexPmscKpiDefinitionDto_withFullFields() throws JsonProcessingException {
        final ComplexPmscKpiDefinitionDto complexPmscKpiDefinitionDto = codec.withValidation().readValue(NEW_COMPLEX_KPI_DEF_ALL_STR, ComplexPmscKpiDefinitionDto.class);
        assertFalse(ObjectUtils.isEmpty(complexPmscKpiDefinitionDto.getName()));
        assertFalse(ObjectUtils.isEmpty(complexPmscKpiDefinitionDto.getExpression()));
        assertFalse(ObjectUtils.isEmpty(complexPmscKpiDefinitionDto.getObjectType()));
        assertFalse(ObjectUtils.isEmpty(complexPmscKpiDefinitionDto.getAggregationType()));
        assertFalse(ObjectUtils.isEmpty(complexPmscKpiDefinitionDto.getExportable()));
        assertFalse(ObjectUtils.isEmpty(complexPmscKpiDefinitionDto.getExecutionGroup()));
    }

    @SuppressWarnings("java:S5778")
    @Test
    void testSimplePmscKpiDefinitionDto_missingRequiredField_throwNPE() {
        assertThrows(NullPointerException.class, () -> ComplexPmscKpiDefinitionDto.builder().name("complex").expression("expression").objectType("INT").aggregationType("SUM").build());
    }
}
