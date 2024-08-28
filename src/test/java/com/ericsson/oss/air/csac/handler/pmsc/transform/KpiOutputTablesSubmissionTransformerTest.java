/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.handler.pmsc.transform;

import static com.ericsson.oss.air.csac.handler.pmsc.transform.KpiOutputTablesSubmissionTransformer.createComplexKpiDefinitionDtoFn;
import static com.ericsson.oss.air.csac.handler.pmsc.transform.KpiOutputTablesSubmissionTransformer.createComplexKpiOutputTableDtoFn;
import static com.ericsson.oss.air.csac.handler.pmsc.transform.KpiOutputTablesSubmissionTransformer.createSimpleKpiDefinitionDtoFn;
import static com.ericsson.oss.air.csac.handler.pmsc.transform.KpiOutputTablesSubmissionTransformer.createSimpleKpiOutputTableDtoFn;
import static com.ericsson.oss.air.csac.model.TestResourcesUtils.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ericsson.oss.air.csac.model.InputMetric;
import com.ericsson.oss.air.csac.model.pmsc.ComplexKpiOutputTableDto;
import com.ericsson.oss.air.csac.model.pmsc.ComplexPmscKpiDefinitionDto;
import com.ericsson.oss.air.csac.model.pmsc.KPIDefinitionDTOMapping;
import com.ericsson.oss.air.csac.model.pmsc.KpiDefinitionDTO;
import com.ericsson.oss.air.csac.model.pmsc.KpiOutputTableDto;
import com.ericsson.oss.air.csac.model.pmsc.KpiSubmissionDto;
import com.ericsson.oss.air.csac.model.pmsc.PmscKpiDefinitionDto;
import com.ericsson.oss.air.csac.model.pmsc.SimpleKpiOutputTableDto;
import com.ericsson.oss.air.csac.model.pmsc.SimplePmscKpiDefinitionDto;
import org.junit.jupiter.api.Test;

class KpiOutputTablesSubmissionTransformerTest {

    private static final List<KpiDefinitionDTO> SIMPLE_KPI_LIST = List.of(DEPLOYED_SIMPLE_KPI_OBJ);

    private static final List<KpiDefinitionDTO> SIMPLE_HETEROGENEOUS_KPI_LIST = List.of(DEPLOYED_SIMPLE_KPI_OBJ, DEPLOYED_SIMPLE_KPI_OBJ_2);

    private static final List<KpiDefinitionDTO> COMPLEX_KPI_LIST = List.of(DEPLOYED_COMPLEX_KPI_OBJ);

    private final KpiOutputTablesSubmissionTransformer kpiTransformer = new KpiOutputTablesSubmissionTransformer();

    @Test
    void testCreateSimpleKpiDefinitionDtoFn_success() {
        final SimplePmscKpiDefinitionDto kpi = (SimplePmscKpiDefinitionDto) createSimpleKpiDefinitionDtoFn.apply(
                DEPLOYED_SIMPLE_KPI_OBJ.getInpDataIdentifier(), DEPLOYED_SIMPLE_KPI_OBJ);

        assertNotNull(kpi);
        assertEquals(DEPLOYED_SIMPLE_KPI_OBJ.getName(), kpi.getName());
        assertEquals(DEPLOYED_SIMPLE_KPI_OBJ.getExpression(), kpi.getExpression());
        assertEquals(DEPLOYED_SIMPLE_KPI_OBJ.getObjectType(), kpi.getObjectType());
        assertEquals(DEPLOYED_SIMPLE_KPI_OBJ.getAggregationType(), kpi.getAggregationType());
        assertEquals(DEPLOYED_SIMPLE_KPI_OBJ.getIsVisible(), kpi.getExportable());
        assertNull(kpi.getInputDataIdentifier());
        assertNull(kpi.getAggregationElements());
    }

    @Test
    void testCreateSimpleKpiDefinitionDtoFn_withInputDataIdentifier_success() {

        final SimplePmscKpiDefinitionDto kpi = (SimplePmscKpiDefinitionDto) createSimpleKpiDefinitionDtoFn.apply(
                DEPLOYED_SIMPLE_KPI_OBJ.getInpDataIdentifier() + "_1", DEPLOYED_SIMPLE_KPI_OBJ);

        assertNotNull(kpi);
        assertEquals(DEPLOYED_SIMPLE_KPI_OBJ.getName(), kpi.getName());
        assertEquals(DEPLOYED_SIMPLE_KPI_OBJ.getExpression(), kpi.getExpression());
        assertEquals(DEPLOYED_SIMPLE_KPI_OBJ.getObjectType(), kpi.getObjectType());
        assertEquals(DEPLOYED_SIMPLE_KPI_OBJ.getAggregationType(), kpi.getAggregationType());
        assertEquals(DEPLOYED_SIMPLE_KPI_OBJ.getIsVisible(), kpi.getExportable());
        assertEquals(DEPLOYED_SIMPLE_KPI_OBJ.getInpDataIdentifier(), kpi.getInputDataIdentifier());
        assertEquals(DEPLOYED_SIMPLE_KPI_OBJ.getAggregationElements(), kpi.getAggregationElements());
    }

    @Test
    void testCreateComplexKpiDefinitionDtoFn_success() {
        final ComplexPmscKpiDefinitionDto kpi = (ComplexPmscKpiDefinitionDto) createComplexKpiDefinitionDtoFn.apply(DEPLOYED_COMPLEX_KPI_OBJ);
        assertNotNull(kpi);
        assertEquals(DEPLOYED_COMPLEX_KPI_OBJ.getName(), kpi.getName());
        assertEquals(DEPLOYED_COMPLEX_KPI_OBJ.getExpression(), kpi.getExpression());
        assertEquals(DEPLOYED_COMPLEX_KPI_OBJ.getObjectType(), kpi.getObjectType());
        assertEquals(DEPLOYED_COMPLEX_KPI_OBJ.getAggregationType(), kpi.getAggregationType());
        assertEquals(DEPLOYED_COMPLEX_KPI_OBJ.getIsVisible(), kpi.getExportable());
        assertEquals(DEPLOYED_COMPLEX_KPI_OBJ.getExecutionGroup(), kpi.getExecutionGroup());
    }

    @Test
    void testCreateSimpleKpiOutputTableDto_emptyResponse() {
        assertNull(createSimpleKpiOutputTableDtoFn.apply(new ArrayList<>()));
    }

    @Test
    void testCreateSimpleKpiOutputTableDto_success() {

        final SimpleKpiOutputTableDto tableDTO = (SimpleKpiOutputTableDto) createSimpleKpiOutputTableDtoFn.apply(SIMPLE_KPI_LIST);
        assertNotNull(tableDTO);
        assertEquals(DEPLOYED_SIMPLE_KPI_OBJ.getAggregationPeriod(), tableDTO.getAggregationPeriod().getValue());
        assertEquals(DEPLOYED_SIMPLE_KPI_OBJ.getAlias(), tableDTO.getAlias());
        assertEquals(DEPLOYED_SIMPLE_KPI_OBJ.getInpDataIdentifier(), tableDTO.getInputDataIdentifier());
        assertEquals(DEPLOYED_SIMPLE_KPI_OBJ.getAggregationElements(), tableDTO.getAggregationElements());

        assertEquals(1, tableDTO.getKpiDefinitions().size());

        assertNull(((SimplePmscKpiDefinitionDto) tableDTO.getKpiDefinitions().get(0)).getInputDataIdentifier());
    }

    @Test
    void testCreateSimpleKpiOutputTableDto_heterogeneousList_success() {

        final SimpleKpiOutputTableDto tableDTO = (SimpleKpiOutputTableDto) createSimpleKpiOutputTableDtoFn.apply(SIMPLE_HETEROGENEOUS_KPI_LIST);

        assertNotNull(tableDTO);
        assertEquals(DEPLOYED_SIMPLE_KPI_OBJ.getAggregationPeriod(), tableDTO.getAggregationPeriod().getValue());
        assertEquals(DEPLOYED_SIMPLE_KPI_OBJ.getAlias(), tableDTO.getAlias());
        assertEquals(DEPLOYED_SIMPLE_KPI_OBJ.getInpDataIdentifier(), tableDTO.getInputDataIdentifier());
        assertEquals(DEPLOYED_SIMPLE_KPI_OBJ.getAggregationElements(), tableDTO.getAggregationElements());

        assertEquals(2, tableDTO.getKpiDefinitions().size());

        // first KPI definition should have no input data identifier
        assertNull(((SimplePmscKpiDefinitionDto) tableDTO.getKpiDefinitions().get(0)).getInputDataIdentifier());

        // second KPI definition should have one that is different than the table data identifier but equal to the original data identifier before transformation
        final String heterogeneousDataIndentifier = ((SimplePmscKpiDefinitionDto) tableDTO.getKpiDefinitions().get(1)).getInputDataIdentifier();

        assertEquals(DEPLOYED_SIMPLE_KPI_OBJ_2.getInpDataIdentifier(), heterogeneousDataIndentifier);
        assertNotEquals(tableDTO.getInputDataIdentifier(), heterogeneousDataIndentifier);
    }

    @Test
    void testCreateComplexKpiOutputTableDto_emptyResponse() {
        assertNull(createComplexKpiOutputTableDtoFn.apply(new ArrayList<>()));
    }

    @Test
    void testCreateComplexKpiOutputTableDto_success() {
        final ComplexKpiOutputTableDto tableDTO = (ComplexKpiOutputTableDto) createComplexKpiOutputTableDtoFn.apply(COMPLEX_KPI_LIST);
        assertNotNull(tableDTO);
        assertEquals(DEPLOYED_COMPLEX_KPI_OBJ.getAggregationPeriod(), tableDTO.getAggregationPeriod().getValue());
        assertEquals(DEPLOYED_COMPLEX_KPI_OBJ.getAlias(), tableDTO.getAlias());
        assertEquals(DEPLOYED_COMPLEX_KPI_OBJ.getAggregationElements(), tableDTO.getAggregationElements());
        assertEquals(1, tableDTO.getKpiDefinitions().size());
    }

    @Test
    void testCreateKpiDefinitionSubmission_simpleKpi_success() {
        final KpiSubmissionDto submission = (KpiSubmissionDto) this.kpiTransformer.apply(SIMPLE_KPI_LIST);
        assertNotNull(submission);
        assertNull(submission.getOnDemand());
        assertNull(submission.getScheduledComplex());
        assertEquals(1, submission.getScheduledSimple().getKpiOutputTables().size());

        final SimpleKpiOutputTableDto kpiOutputTableDto = (SimpleKpiOutputTableDto) submission.getScheduledSimple().getKpiOutputTables().get(0);
        assertEquals(DEPLOYED_SIMPLE_KPI_OBJ.getAlias(), kpiOutputTableDto.getAlias());
        assertEquals(DEPLOYED_SIMPLE_KPI_OBJ.getAggregationElements(), kpiOutputTableDto.getAggregationElements());
        assertEquals(DEPLOYED_SIMPLE_KPI_OBJ.getAggregationPeriod(), kpiOutputTableDto.getAggregationPeriod().getValue());
        assertEquals(DEPLOYED_SIMPLE_KPI_OBJ.getInpDataIdentifier(), kpiOutputTableDto.getInputDataIdentifier());
        assertEquals(1, kpiOutputTableDto.getKpiDefinitions().size());
    }

    @Test
    void testCreateKpiDefinitionSubmission_complexKpi_success() {
        final KpiSubmissionDto submission = (KpiSubmissionDto) this.kpiTransformer.apply(COMPLEX_KPI_LIST);
        assertNotNull(submission);
        assertNull(submission.getOnDemand());
        assertNull(submission.getScheduledSimple());
        assertEquals(1, submission.getScheduledComplex().getKpiOutputTables().size());

        final KpiOutputTableDto kpiOutputTableDto = submission.getScheduledComplex().getKpiOutputTables().get(0);
        assertEquals(DEPLOYED_COMPLEX_KPI_OBJ.getAlias(), kpiOutputTableDto.getAlias());
        assertEquals(DEPLOYED_COMPLEX_KPI_OBJ.getAggregationElements(), kpiOutputTableDto.getAggregationElements());
        assertEquals(DEPLOYED_COMPLEX_KPI_OBJ.getAggregationPeriod(), kpiOutputTableDto.getAggregationPeriod().getValue());
        assertEquals(1, kpiOutputTableDto.getKpiDefinitions().size());
    }

    @Test
    void testCreateKpiDefinitionSubmission_success() {
        final KpiSubmissionDto submission = (KpiSubmissionDto) this.kpiTransformer.apply(List.of(DEPLOYED_SIMPLE_KPI_OBJ, DEPLOYED_COMPLEX_KPI_OBJ));

        assertNotNull(submission);
        assertNull(submission.getOnDemand());
        assertEquals(1, submission.getScheduledSimple().getKpiOutputTables().size());
        assertEquals(1, submission.getScheduledComplex().getKpiOutputTables().size());

    }

    @Test
    void testCreateKpiDefinitionSubmission_groupBy_success() {

        final KpiDefinitionDTO simpleKpiGroup1_1 = KPIDefinitionDTOMapping.createSimpleKPIDTO(VALID_SIMPLE_KPI_DEF_OBJ,
                VALID_PROFILE_DEF_OBJ.getContext(),
                DEFAULT_SCHEMA_NAME, VALID_PM_DEF_SOURCE, DEFAULT_AGGREGATION_PERIOD);

        final String newPmDefSource = VALID_PM_DEF_SOURCE + "2";

        final KpiDefinitionDTO simpleKpiGroup2_1 = KPIDefinitionDTOMapping.createSimpleKPIDTO(VALID_SIMPLE_KPI_DEF_OBJ,
                VALID_PROFILE_DEF_OBJ.getContext(),
                DEFAULT_SCHEMA_NAME, newPmDefSource, DEFAULT_AGGREGATION_PERIOD);

        final KpiDefinitionDTO simpleKpiGroup2_2 = KPIDefinitionDTOMapping.createSimpleKPIDTO(VALID_SIMPLE_KPI_DEF_OBJ,
                VALID_PROFILE_DEF_OBJ.getContext(),
                DEFAULT_SCHEMA_NAME, newPmDefSource, DEFAULT_AGGREGATION_PERIOD);

        final List<InputMetric> ip = VALID_COMPLEX_KPI_DEF_OBJ.getInputMetrics();
        final Map<String, KpiDefinitionDTO> group1InputMetricMap = new HashMap<>();
        group1InputMetricMap.put("kpi_simple_name", simpleKpiGroup1_1);
        final KpiDefinitionDTO complexKpiGroup1_1 = KPIDefinitionDTOMapping.createComplexKPIDTO(VALID_COMPLEX_KPI_DEF_OBJ, VALID_PROFILE_DEF_OBJ,
                DEFAULT_AGGREGATION_PERIOD, group1InputMetricMap);

        final Map<String, KpiDefinitionDTO> group2InputMetricMap = new HashMap<>();
        group2InputMetricMap.put("kpi_simple_name", simpleKpiGroup2_1);
        final KpiDefinitionDTO complexKpiGroup1_2 = KPIDefinitionDTOMapping.createComplexKPIDTO(VALID_COMPLEX_KPI_DEF_OBJ, VALID_PROFILE_DEF_OBJ,
                DEFAULT_AGGREGATION_PERIOD, group2InputMetricMap);

        final KpiSubmissionDto submission = (KpiSubmissionDto) this.kpiTransformer.apply(
                List.of(simpleKpiGroup1_1, simpleKpiGroup2_1, simpleKpiGroup2_2, complexKpiGroup1_1, complexKpiGroup1_2));

        assertNotNull(submission);
        assertNull(submission.getOnDemand());
        assertNotNull(submission.getScheduledSimple());
        assertNotNull(submission.getScheduledComplex());
        assertEquals(2, submission.getScheduledComplex().getKpiOutputTables().size());
        assertEquals(1, submission.getScheduledComplex().getKpiOutputTables().get(0).getKpiDefinitions().size());

        final List<KpiOutputTableDto> simpleKpiTables = submission.getScheduledSimple().getKpiOutputTables();

        assertEquals(2, simpleKpiTables.size());

        final List<PmscKpiDefinitionDto> kpiDefinitions1 = simpleKpiTables.stream()
                .filter(kpiOutputTableDto -> ((SimpleKpiOutputTableDto) kpiOutputTableDto).getInputDataIdentifier().equals(VALID_PM_DEF_SOURCE))
                .findFirst().get().getKpiDefinitions();

        assertEquals(1, kpiDefinitions1.size());

        final List<PmscKpiDefinitionDto> kpiDefinitions2 = simpleKpiTables.stream()
                .filter(kpiOutputTableDto -> ((SimpleKpiOutputTableDto) kpiOutputTableDto).getInputDataIdentifier().equals(newPmDefSource))
                .findFirst().get().getKpiDefinitions();

        assertEquals(2, kpiDefinitions2.size());

    }

}
