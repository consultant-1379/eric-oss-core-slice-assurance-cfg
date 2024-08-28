/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.model.pmsc;

import static com.ericsson.oss.air.csac.model.TestResourcesUtils.NEW_COMPLEX_KPI_DEF_ALL_STR;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import com.ericsson.oss.air.util.codec.Codec;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.validation.Validation;
import org.junit.jupiter.api.Test;
import org.springframework.util.ObjectUtils;

public class KpiOutputTableDtoTest {

    private static final Codec CODEC = new Codec(Validation.buildDefaultValidatorFactory().getValidator());

    private static final ComplexPmscKpiDefinitionDto COMPLEX_PMSC_KPI_DEFINITION_DTO;

    static {
        try {
            COMPLEX_PMSC_KPI_DEFINITION_DTO = CODEC.readValue(NEW_COMPLEX_KPI_DEF_ALL_STR, ComplexPmscKpiDefinitionDto.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void kpiOutputTableDtoTest_withAllFields() {
        final KpiOutputTableDto kpiOutputTable = KpiOutputTableDto.customKpiOutputTableDtoBuilder()
                .aggregationPeriod(AggregationPeriod.FIFTEEN)
                .aggregationElements(List.of("agg1", "agg2"))
                .alias("alias")
                .dataReliabilityOffset(0)
                .kpiDefinitions(List.of(COMPLEX_PMSC_KPI_DEFINITION_DTO))
                .build();

        assertFalse(ObjectUtils.isEmpty(kpiOutputTable.getAggregationElements()));
        assertFalse(ObjectUtils.isEmpty(kpiOutputTable.getAlias()));
        assertFalse(ObjectUtils.isEmpty(kpiOutputTable.getDataReliabilityOffset()));
        assertFalse(ObjectUtils.isEmpty(kpiOutputTable.getKpiDefinitions()));
        assertFalse(ObjectUtils.isEmpty(kpiOutputTable.getAggregationPeriod()));
    }

    @Test
    void kpiOutputTableDtoTest_missingAlias_shouldThrowNPE() {
        assertThrows(NullPointerException.class, () -> KpiOutputTableDto.customKpiOutputTableDtoBuilder()
                .aggregationElements(List.of("agg1", "agg2"))
                .dataReliabilityOffset(0)
                .kpiDefinitions(List.of(COMPLEX_PMSC_KPI_DEFINITION_DTO))
                .aggregationPeriod(AggregationPeriod.FIFTEEN)
                .build());
    }

    @Test
    void kpiOutputTableDtoTest_missingAggregationElements_shouldThrowNPE() {
        assertThrows(NullPointerException.class,
                () -> KpiOutputTableDto.customKpiOutputTableDtoBuilder()
                        .alias("alias")
                        .dataReliabilityOffset(0)
                        .kpiDefinitions(List.of(COMPLEX_PMSC_KPI_DEFINITION_DTO))
                        .aggregationPeriod(AggregationPeriod.FIFTEEN).build());
    }

    @Test
    void kpiOutputTableDtoTest_missingKpiDefinitions_shouldThrowNPE() {
        assertThrows(NullPointerException.class,
                () -> KpiOutputTableDto.customKpiOutputTableDtoBuilder()
                        .alias("alias")
                        .dataReliabilityOffset(0)
                        .aggregationElements(List.of("agg1", "agg2"))
                        .aggregationPeriod(AggregationPeriod.FIFTEEN)
                        .build());
    }

    @Test
    void kpiOutputTableDtoTest_missingAggregationPeriod_shouldThrowNPE() {
        assertThrows(NullPointerException.class,
                () -> KpiOutputTableDto.customKpiOutputTableDtoBuilder()
                        .alias("alias")
                        .dataReliabilityOffset(0)
                        .aggregationElements(List.of("agg1", "agg2"))
                        .kpiDefinitions(List.of(COMPLEX_PMSC_KPI_DEFINITION_DTO))
                        .build());
    }
}
