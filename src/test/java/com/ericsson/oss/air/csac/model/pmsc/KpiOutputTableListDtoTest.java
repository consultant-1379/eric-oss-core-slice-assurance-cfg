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

import static com.ericsson.oss.air.csac.model.TestResourcesUtils.NEW_COMPLEX_KPI_DEF_ALL_STR;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import jakarta.validation.Validation;

import com.ericsson.oss.air.util.codec.Codec;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;

public class KpiOutputTableListDtoTest {

    private static final Codec CODEC = new Codec(Validation.buildDefaultValidatorFactory().getValidator());
    private static final ComplexPmscKpiDefinitionDto COMPLEX_PMSC_KPI_DEFINITION_DTO;

    static {
        try {
            COMPLEX_PMSC_KPI_DEFINITION_DTO =
                    CODEC.readValue(NEW_COMPLEX_KPI_DEF_ALL_STR, ComplexPmscKpiDefinitionDto.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void kpiOutTableListDtoTest_setKpiOutputTableList() {

        final KpiOutputTableDto complexKpiOutputTable
                = KpiOutputTableDto.customKpiOutputTableDtoBuilder()
                .aggregationPeriod(AggregationPeriod.SIXTY)
                .aggregationElements(List.of("agg1", "agg2"))
                .alias("alias")
                .kpiDefinitions(List.of(COMPLEX_PMSC_KPI_DEFINITION_DTO))
                .build();

        final KpiOutputTableListDto outputTableList = KpiOutputTableListDto.builder()
                .kpiOutputTables(List.of(complexKpiOutputTable))
                .build();

        assertEquals(1, outputTableList.getKpiOutputTables().size());
        assertEquals(complexKpiOutputTable, outputTableList.getKpiOutputTables().get(0));
    }

    @Test
    void kpiOutTableListDtoTest_nullValue_shouldThrowNPE() {

        assertThrows(NullPointerException.class, () -> KpiOutputTableListDto.builder()
                .build());
    }
}
