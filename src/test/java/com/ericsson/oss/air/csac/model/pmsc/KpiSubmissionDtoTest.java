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
import static com.ericsson.oss.air.csac.model.TestResourcesUtils.NEW_SIMPLE_KPI_DEF_STR;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import com.ericsson.oss.air.util.codec.Codec;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Validation;
import org.junit.jupiter.api.Test;

public class KpiSubmissionDtoTest {

    public static final List<String> AGGREGATION_ELEMENTS = List.of("agg1", "agg2");

    private static final Codec CODEC = new Codec(Validation.buildDefaultValidatorFactory().getValidator());

    public static final String ALIAS = "alias";

    public static final String INPUT_DATA_IDENTIFIER = "1";

    public static final SimplePmscKpiDefinitionDto SIMPLE_PMSC_KPI_DEFINITION_DTO;

    static {
        try {
            SIMPLE_PMSC_KPI_DEFINITION_DTO = CODEC.readValue(NEW_SIMPLE_KPI_DEF_STR, SimplePmscKpiDefinitionDto.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private static final ComplexPmscKpiDefinitionDto COMPLEX_PMSC_KPI_DEFINITION_DTO;

    static {
        try {
            COMPLEX_PMSC_KPI_DEFINITION_DTO = CODEC.readValue(NEW_COMPLEX_KPI_DEF_ALL_STR, ComplexPmscKpiDefinitionDto.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void kpiSubmissionDtoTest_defaultValues() throws JsonProcessingException {
        final String expectedSubmissionJson = "{}";

        final KpiSubmissionDto actualSubmission = KpiSubmissionDto.builder().build();
        final String actualSubmissionJson = CODEC.writeValueAsString(actualSubmission);

        assertEquals(expectedSubmissionJson, actualSubmissionJson);
    }

    @Test
    public void kpiSubmissionDtoTest_simpleKpiAllFields() throws JsonProcessingException {
        final String expectedSubmissionJson =
                "{\"scheduled_simple\":{\"kpi_output_tables\":[{" + "\"aggregation_period\":60," + "\"alias\":\"alias\","
                        + "\"aggregation_elements\":[\"agg1\",\"agg2\"]," + "\"data_reliability_offset\":0,"
                        + "\"kpi_definitions\":[{\"name\":\"sum_integer_1440_simple\",\"expression\":\"SUM(new_fact_table_0.pmCounters.integerColumn0)\",\"object_type\":\"INTEGER\",\"aggregation_type\":\"SUM\"}],"
                        + "\"inp_data_identifier\":\"1\"}]}}";

        final SimpleKpiOutputTableDto simpleOutputTable = SimpleKpiOutputTableDto.customSimpleKpiOutputTableDtoBuilder()
                .aggregationPeriod(AggregationPeriod.SIXTY).aggregationElements(AGGREGATION_ELEMENTS).alias(ALIAS)
                .inputDataIdentifier(INPUT_DATA_IDENTIFIER).kpiDefinitions(List.of(SIMPLE_PMSC_KPI_DEFINITION_DTO)).build();
        final KpiOutputTableListDto simpleKpiOutputTableList = KpiOutputTableListDto.builder().kpiOutputTables(List.of(simpleOutputTable)).build();

        final KpiSubmissionDto actualSubmission = KpiSubmissionDto.builder().scheduledSimple(simpleKpiOutputTableList).build();

        final String actualSubmissionJson = new ObjectMapper().writeValueAsString(actualSubmission);

        assertEquals(expectedSubmissionJson, actualSubmissionJson);
    }

    @Test
    public void kpiSubmissionDtoTest_ComplexKpiAllFields() throws JsonProcessingException {
        final String expectedSubmissionJson =
                "{" + "\"scheduled_complex\":{\"kpi_output_tables\":[{" + "\"aggregation_period\":" + AggregationPeriod.SIXTY.getValue() + ","
                        + "\"alias\":\"" + ALIAS + "\"," + "\"aggregation_elements\":[\"agg1\",\"agg2\"]," + "\"data_reliability_offset\":0,"
                        + "\"kpi_definitions\":[{\"name\":\"sum_integer_60_complex\",\"expression\":\"SUM(kpi_simple_60.integer_simple) FROM kpi_db://kpi_simple_60\",\"object_type\":\"INTEGER\",\"aggregation_type\":\"SUM\",\"exportable\":true,\"execution_group\":\"COMPLEX1\"}]}]}"
                        + "}";

        final KpiOutputTableDto complexKpiOutputTable = KpiOutputTableDto.customKpiOutputTableDtoBuilder().aggregationPeriod(AggregationPeriod.SIXTY)
                .aggregationElements(AGGREGATION_ELEMENTS).alias(ALIAS).kpiDefinitions(List.of(COMPLEX_PMSC_KPI_DEFINITION_DTO)).build();

        final KpiOutputTableListDto complexKpiOutputTableList = KpiOutputTableListDto.builder().kpiOutputTables(List.of(complexKpiOutputTable))
                .build();

        final KpiSubmissionDto actualSubmission = KpiSubmissionDto.builder().scheduledComplex(complexKpiOutputTableList).build();

        final String actualSubmissionJson = new ObjectMapper().writeValueAsString(actualSubmission);

        assertEquals(expectedSubmissionJson, actualSubmissionJson);
    }
}
