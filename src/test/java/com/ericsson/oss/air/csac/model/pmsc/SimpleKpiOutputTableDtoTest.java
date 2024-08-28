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

import static com.ericsson.oss.air.csac.model.TestResourcesUtils.NEW_SIMPLE_KPI_DEF_STR;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import jakarta.validation.Validation;

import com.ericsson.oss.air.util.codec.Codec;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;
import org.springframework.util.ObjectUtils;

public class SimpleKpiOutputTableDtoTest {

    private static final Codec CODEC = new Codec(Validation.buildDefaultValidatorFactory().getValidator());

    private static final SimplePmscKpiDefinitionDto SIMPLE_PMSC_KPI_DEFINITION_DTO;

    static {
        try {
            SIMPLE_PMSC_KPI_DEFINITION_DTO =
                    CODEC.readValue(NEW_SIMPLE_KPI_DEF_STR, SimplePmscKpiDefinitionDto.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void simpleKpiOutputTableDtoTest_withRequiredFields() {
        final SimpleKpiOutputTableDto simpleKpiOutputTable
                = SimpleKpiOutputTableDto.customSimpleKpiOutputTableDtoBuilder()
                .aggregationElements(List.of("agg1", "agg2"))
                .aggregationPeriod(AggregationPeriod.SIXTY)
                .alias("alias")
                .kpiDefinitions(List.of(SIMPLE_PMSC_KPI_DEFINITION_DTO))
                .build();

        assertFalse(ObjectUtils.isEmpty(simpleKpiOutputTable.getAggregationElements()));
        assertFalse(ObjectUtils.isEmpty(simpleKpiOutputTable.getAlias()));
        assertFalse(ObjectUtils.isEmpty(simpleKpiOutputTable.getKpiDefinitions()));
        assertFalse(ObjectUtils.isEmpty(simpleKpiOutputTable.getAggregationPeriod()));
        assertTrue(ObjectUtils.isEmpty(simpleKpiOutputTable.getInputDataIdentifier()));
    }

    @Test
    void simpleKpiOutputTableDtoTest_withAllFields() {
        final SimpleKpiOutputTableDto simpleKpiOutputTable
                = SimpleKpiOutputTableDto.customSimpleKpiOutputTableDtoBuilder()
                .aggregationElements(List.of("agg1", "agg2"))
                .alias("alias")
                .kpiDefinitions(List.of(SIMPLE_PMSC_KPI_DEFINITION_DTO))
                .aggregationPeriod(AggregationPeriod.SIXTY)
                .inputDataIdentifier("id1")
                .build();

        assertFalse(ObjectUtils.isEmpty(simpleKpiOutputTable.getAggregationElements()));
        assertFalse(ObjectUtils.isEmpty(simpleKpiOutputTable.getAlias()));
        assertFalse(ObjectUtils.isEmpty(simpleKpiOutputTable.getKpiDefinitions()));
        assertFalse(ObjectUtils.isEmpty(simpleKpiOutputTable.getAggregationPeriod()));
        assertFalse(ObjectUtils.isEmpty(simpleKpiOutputTable.getInputDataIdentifier()));
    }

    @Test
    void simpleKpiOutputTableDtoTest_missingRequiredFields_shouldThrowNPE() {
        assertThrows(NullPointerException.class, () ->
                SimpleKpiOutputTableDto.customSimpleKpiOutputTableDtoBuilder().build());
    }

}
