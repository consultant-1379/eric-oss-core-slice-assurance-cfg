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

import static com.ericsson.oss.air.csac.model.InputMetric.Type.PM_DATA;
import static com.ericsson.oss.air.csac.model.TestResourcesUtils.DEFAULT_AGGREGATION_PERIOD;
import static com.ericsson.oss.air.csac.model.TestResourcesUtils.DEPLOYED_COMPLEX_KPI_OBJ;
import static com.ericsson.oss.air.csac.model.TestResourcesUtils.DEPLOYED_SIMPLE_KPI_OBJ;
import static com.ericsson.oss.air.csac.model.TestResourcesUtils.VALID_COMPLEX_KPI_DEF_OBJ;
import static com.ericsson.oss.air.csac.model.TestResourcesUtils.VALID_PM_DEF_NAME;
import static com.ericsson.oss.air.csac.model.TestResourcesUtils.VALID_PM_DEF_SOURCE;
import static com.ericsson.oss.air.csac.model.TestResourcesUtils.VALID_PROFILE_DEF_OBJ;
import static com.ericsson.oss.air.csac.model.TestResourcesUtils.VALID_SCHEMA_NAME;
import static com.ericsson.oss.air.csac.model.pmsc.KPIDefinitionDTOMapping.DOT_CHAR;
import static com.ericsson.oss.air.csac.model.pmsc.KPIDefinitionDTOMapping.EQUAL_CHAR;
import static com.ericsson.oss.air.csac.model.pmsc.KPIDefinitionDTOMapping.FROM_KPI_DB;
import static com.ericsson.oss.air.csac.model.pmsc.KPIDefinitionDTOMapping.KPI_DB;
import static com.ericsson.oss.air.csac.model.pmsc.KPIDefinitionDTOMapping.buildComplexExpression;
import static com.ericsson.oss.air.csac.model.pmsc.KPIDefinitionDTOMapping.createComplexKPIDTO;
import static com.ericsson.oss.air.csac.model.pmsc.KPIDefinitionDTOMapping.createSimpleKPIDTO;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.ericsson.oss.air.csac.model.InputMetric;
import com.ericsson.oss.air.csac.model.InputMetricOverride;
import com.ericsson.oss.air.csac.model.KPIDefinition;
import com.ericsson.oss.air.csac.model.KPIReference;
import com.ericsson.oss.air.csac.model.PMDefinition;
import com.ericsson.oss.air.csac.model.ProfileDefinition;
import com.ericsson.oss.air.csac.model.TestResourcesUtils;
import com.ericsson.oss.air.exception.CsacValidationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.util.ObjectUtils;

class KPIDefinitionDTOMappingTest {

    private static final InputMetric inputMetric = new InputMetric("PM_ID0", "p0", PM_DATA);

    private static final InputMetric inputMetric1 = new InputMetric("PM_ID1", "p1", PM_DATA);

    private static final InputMetric inputMetric2 = new InputMetric("PM_ID2", "p2", PM_DATA);

    private static final List<InputMetric> inputMetricList = List.of(inputMetric, inputMetric1, inputMetric2);

    /**
     * Variables used for testing runtime complex KPIs
     */
    private static final String PM_DEF_NAME_B = "pmCounters.PMb";

    private static final String PM_DEF_NAME_C = "pmCounters.PMc";

    private static final String SCHEMA_NAME_B = "schemaNameB";

    private static final String SCHEMA_NAME_C = "schemaNameC";

    private static final String KPI_DEF_NAME_B = "B";

    private static final String KPI_DEF_NAME_C = "C";

    private static final String KPI_DEF_NAME_A = "A";

    private static final String AGG_TYPE_SIMPLE_KPI = "AVG";

    private static final String AGG_TYPE_COMPLEX_KPI = "SUM";

    private static final String AGG_FIELD_1 = "agg1";

    private static final String AGG_FIELD_2 = "agg2";

    private static final PMDefinition PM_DEFINITION_B = PMDefinition.builder()
            .name(PM_DEF_NAME_B)
            .source("5G|PM_COUNTERS|" + SCHEMA_NAME_B)
            .build();

    private static final PMDefinition PM_DEFINITION_C = PMDefinition.builder()
            .name(PM_DEF_NAME_C)
            .source("5G|PM_COUNTERS|" + SCHEMA_NAME_B)
            .build();

    private static final KPIDefinition SIMPLE_KPI_DEFINITION_B = KPIDefinition.builder()
            .name(KPI_DEF_NAME_B)
            .expression(AGG_TYPE_SIMPLE_KPI + "(" + PM_DEF_NAME_B + ")")
            .aggregationType(AGG_TYPE_SIMPLE_KPI)
            .isVisible(true)
            .inputMetrics(List.of(new InputMetric(PM_DEF_NAME_B, "", InputMetric.Type.PM_DATA)))
            .build();

    private static final KPIDefinition SIMPLE_KPI_DEFINITION_C = KPIDefinition.builder()
            .name(KPI_DEF_NAME_C)
            .expression(AGG_TYPE_SIMPLE_KPI + "(" + PM_DEF_NAME_C + ")")
            .aggregationType(AGG_TYPE_SIMPLE_KPI)
            .isVisible(true)
            .inputMetrics(List.of(new InputMetric(PM_DEF_NAME_C, "", InputMetric.Type.PM_DATA)))
            .build();

    private static final KPIDefinition COMPLEX_KPI_DEFINITION_A = KPIDefinition.builder()
            .name(KPI_DEF_NAME_A)
            .expression(AGG_TYPE_COMPLEX_KPI + "(" + KPI_DEF_NAME_B + " + " + KPI_DEF_NAME_C + ")")
            .aggregationType(AGG_TYPE_COMPLEX_KPI)
            .isVisible(true)
            .inputMetrics(List.of(new InputMetric(KPI_DEF_NAME_B, "", InputMetric.Type.KPI),
                    new InputMetric(KPI_DEF_NAME_C, "", InputMetric.Type.KPI)))
            .build();

    private static final List<KPIReference> KPI_REFERENCE_LIST = Stream.of(SIMPLE_KPI_DEFINITION_B, SIMPLE_KPI_DEFINITION_C, COMPLEX_KPI_DEFINITION_A)
            .map(kpiDefinition -> KPIReference.builder().ref(kpiDefinition.getName()).build())
            .collect(Collectors.toList());

    private static final ProfileDefinition PROFILE_DEFINITION = ProfileDefinition.builder()
            .name("test profile")
            .context(List.of(AGG_FIELD_1, AGG_FIELD_2))
            .kpis(KPI_REFERENCE_LIST)
            .build();

    private static final KpiDefinitionDTO SIMPLE_KPI_DEFINITION_DTO_B = createSimpleKPIDTO(SIMPLE_KPI_DEFINITION_B,
            VALID_PROFILE_DEF_OBJ.getContext(), SCHEMA_NAME_B, PM_DEFINITION_B.getSource(), 60);

    private static final KpiDefinitionDTO SIMPLE_KPI_DEFINITION_DTO_C = createSimpleKPIDTO(SIMPLE_KPI_DEFINITION_C,
            VALID_PROFILE_DEF_OBJ.getContext(), SCHEMA_NAME_C, PM_DEFINITION_C.getSource(), 60);

    private static final String EXPECTED_RUNTIME_COMPLEX_KPI_EXPRESSION = "SUM(kpi_csac_simple_agg1_agg2_60." + SIMPLE_KPI_DEFINITION_DTO_B.getName()
            + " + kpi_csac_simple_agg1_agg2_60." + SIMPLE_KPI_DEFINITION_DTO_C.getName() + ") FROM kpi_db://kpi_csac_simple_agg1_agg2_60";

    @Test
    void buildKpiDefName_general_valid() {
        final String sampleKpiName = KPIDefinitionDTOMapping.buildUniqueKpiDefName();
        assertFalse(sampleKpiName.contains("-"));
        Assertions.assertTrue(sampleKpiName.contains("csac"));
    }

    @Test
    void buildKpiDefName_mockedUUID_valid() {
        final UUID emptyUUID = UUID.fromString("00000000-0000-0000-0000-000000000000");
        try (final MockedStatic<UUID> utilities = Mockito.mockStatic(UUID.class)) {
            utilities.when(UUID::randomUUID).thenReturn(emptyUUID);
            final String sampleKpiName = KPIDefinitionDTOMapping.buildUniqueKpiDefName();
            assertEquals("csac_00000000_0000_0000_0000_000000000000", sampleKpiName);
        }
    }

    @Test
    void buildSimpleExpression_SingleMax_valid() {

        final String generatedExp = KPIDefinitionDTOMapping.substituteExpressionParameters("MAX(p0)", "schemaName", inputMetricList);
        assertEquals("MAX(schemaName.PM_ID0)", generatedExp);
    }

    @Test
    void buildSimpleExpression_MultipleMethod_valid() {

        final String generatedExp = KPIDefinitionDTOMapping.substituteExpressionParameters("MAX(p0) + AVG(p0)", "schemaName", inputMetricList);
        assertEquals("MAX(schemaName.PM_ID0) + AVG(schemaName.PM_ID0)", generatedExp);
    }

    @Test
    void buildSimpleExpression_MultipleMethodDiffParameter_valid() {

        String generatedExp = KPIDefinitionDTOMapping.substituteExpressionParameters("MAX(p0) + AVG(p1)", "schemaName", inputMetricList);
        assertEquals("MAX(schemaName.PM_ID0) + AVG(schemaName.PM_ID1)", generatedExp);

        generatedExp = KPIDefinitionDTOMapping.substituteExpressionParameters("MAX(p0) + COUNT(p2)", "schemaName", inputMetricList);
        assertEquals("MAX(schemaName.PM_ID0) + COUNT(schemaName.PM_ID2)", generatedExp);
    }

    @Test
    void buildSimpleExpression_emptyInputMetrics_valid() {
        final List<InputMetric> inputMetricList = new ArrayList<>();
        final String generatedExp = KPIDefinitionDTOMapping.substituteExpressionParameters("MAX(p0)", "schemaName", inputMetricList);
        assertEquals("MAX(p0)", generatedExp);
    }

    @Test
    void buildSimpleExpression_noAlias_valid() {
        final InputMetric inputMetric = new InputMetric(VALID_PM_DEF_NAME, null, PM_DATA);
        final List<InputMetric> inputMetricList = singletonList(inputMetric);

        final String generatedExp = KPIDefinitionDTOMapping.substituteExpressionParameters("MAX(p0)", "schemaName", inputMetricList);
        assertEquals("MAX(p0)", generatedExp);
    }

    @Test
    void buildComplexExpression_general_valid() {

        final String expected = MessageFormat.format("SUM({0}." + SIMPLE_KPI_DEFINITION_DTO_B.getName()
                        + " + {1}." + SIMPLE_KPI_DEFINITION_DTO_C.getName() + ") FROM kpi_db://{0}", SIMPLE_KPI_DEFINITION_DTO_B.getFactTableName(),
                SIMPLE_KPI_DEFINITION_DTO_C.getFactTableName());

        final Map<String, KpiDefinitionDTO> inputSimpleKpiDefinitionDtoMap = new HashMap<>();
        inputSimpleKpiDefinitionDtoMap.put(KPI_DEF_NAME_B, SIMPLE_KPI_DEFINITION_DTO_B);
        inputSimpleKpiDefinitionDtoMap.put(KPI_DEF_NAME_C, SIMPLE_KPI_DEFINITION_DTO_C);

        final Map<String, Set<String>> inputMetricTableMap = KPIDefinitionDTOMapping.getInputMetricTableMap(inputSimpleKpiDefinitionDtoMap);

        final String generatedExpression = KPIDefinitionDTOMapping.buildComplexExpression(COMPLEX_KPI_DEFINITION_A,
                SIMPLE_KPI_DEFINITION_DTO_B.getFactTableName(), inputSimpleKpiDefinitionDtoMap, inputMetricTableMap);
        assertEquals(expected, generatedExpression);
    }

    @Test
    void buildComplexExpression_ReplaceAliases_valid() {

        final String expected = MessageFormat.format("SUM({0}." + SIMPLE_KPI_DEFINITION_DTO_B.getName()
                + " + {0}." + SIMPLE_KPI_DEFINITION_DTO_C.getName() + ") FROM kpi_db://{0}", SIMPLE_KPI_DEFINITION_DTO_B.getFactTableName());

        final KPIDefinition complexKpiDefinition_A = KPIDefinition.builder()
                .name(KPI_DEF_NAME_A)
                .expression("SUM(aliasB + aliasC)")
                .aggregationType(AGG_TYPE_COMPLEX_KPI)
                .isVisible(true)
                .inputMetrics(List.of(new InputMetric(KPI_DEF_NAME_B, "aliasB", InputMetric.Type.KPI),
                        new InputMetric(KPI_DEF_NAME_C, "aliasC", InputMetric.Type.KPI)))
                .build();

        final Map<String, KpiDefinitionDTO> inputSimpleKpiDefinitionDtoMap = new HashMap<>();
        inputSimpleKpiDefinitionDtoMap.put(KPI_DEF_NAME_B, SIMPLE_KPI_DEFINITION_DTO_B);
        inputSimpleKpiDefinitionDtoMap.put(KPI_DEF_NAME_C, SIMPLE_KPI_DEFINITION_DTO_C);

        final Map<String, Set<String>> inputMetricTableMap = KPIDefinitionDTOMapping.getInputMetricTableMap(inputSimpleKpiDefinitionDtoMap);

        final String generatedExpression = KPIDefinitionDTOMapping.buildComplexExpression(complexKpiDefinition_A,
                SIMPLE_KPI_DEFINITION_DTO_B.getFactTableName(), inputSimpleKpiDefinitionDtoMap, inputMetricTableMap);
        assertEquals(expected, generatedExpression);
    }

    @Test
    void buildAggregationElements_general_valid() {
        final List<String> fields = List.of("field0", "field1");
        final List<String> generatedFields = KPIDefinitionDTOMapping.buildAggregationElements(fields, "prefix");

        final List<String> expectedFields = List.of("prefix.field0", "prefix.field1");
        assertEquals(expectedFields, generatedFields);
    }

    @Test
    void createSimpleKPIDTO_general_valid() {
        final ProfileDefinition profile = TestResourcesUtils.VALID_PROFILE_DEF_OBJ;
        final KPIDefinition kpi = TestResourcesUtils.VALID_SIMPLE_KPI_DEF_OBJ;
        final KpiDefinitionDTO kpiDTO = createSimpleKPIDTO(
                kpi, profile.getContext(), VALID_SCHEMA_NAME, VALID_PM_DEF_SOURCE, TestResourcesUtils.DEFAULT_AGGREGATION_PERIOD);

        Assertions.assertNotNull(kpiDTO.getName());
        Assertions.assertNotNull(kpiDTO.getExpression());
        Assertions.assertNotNull(kpiDTO.getObjectType());
        Assertions.assertNotNull(kpiDTO.getAggregationElements());

        assertEquals(DEPLOYED_SIMPLE_KPI_OBJ.getExpression(), kpiDTO.getExpression());
        assertEquals(DEPLOYED_SIMPLE_KPI_OBJ.getAlias(), kpiDTO.getAlias());
        assertEquals(DEPLOYED_SIMPLE_KPI_OBJ.getIsVisible(), kpiDTO.getIsVisible());
        assertEquals(DEPLOYED_SIMPLE_KPI_OBJ.getInpDataCategory(), kpiDTO.getInpDataCategory());
        assertEquals(DEPLOYED_SIMPLE_KPI_OBJ.getInpDataIdentifier(), kpiDTO.getInpDataIdentifier());
        assertEquals(DEPLOYED_SIMPLE_KPI_OBJ.getAggregationPeriod(), kpiDTO.getAggregationPeriod());

        Assertions.assertNull(kpiDTO.getExecutionGroup());

    }

    @Test
    void createComplexKPIDTO_general_valid() throws Exception {

        final Map<String, KpiDefinitionDTO> inputSimpleKpiDefinitionDtoMap = new HashMap<>();
        inputSimpleKpiDefinitionDtoMap.put(KPI_DEF_NAME_B, SIMPLE_KPI_DEFINITION_DTO_B);

        final KPIDefinition complexKpi = VALID_COMPLEX_KPI_DEF_OBJ.toBuilder()
                .inputMetrics(List.of(new InputMetric(KPI_DEF_NAME_B, "input_alias", InputMetric.Type.KPI))).build();

        final KpiDefinitionDTO kpiDTO = createComplexKPIDTO(complexKpi, VALID_PROFILE_DEF_OBJ,
                DEFAULT_AGGREGATION_PERIOD, inputSimpleKpiDefinitionDtoMap);

        Assertions.assertNotNull(kpiDTO.getName());
        Assertions.assertNotNull(kpiDTO.getExpression());
        Assertions.assertNotNull(kpiDTO.getObjectType());
        Assertions.assertNotNull(kpiDTO.getAggregationElements());

        final String expectedExpression =
                "MAX(" + SIMPLE_KPI_DEFINITION_DTO_B.getFactTableName() + "." + SIMPLE_KPI_DEFINITION_DTO_B.getName() + ")" + FROM_KPI_DB
                        + SIMPLE_KPI_DEFINITION_DTO_B.getFactTableName();

        final List<String> expectedContext = KPIDefinitionDTOMapping.getFactTableAggregationFieldNames(
                DEPLOYED_COMPLEX_KPI_OBJ.getAggregationElements());
        final List<String> actualContext = KPIDefinitionDTOMapping.getFactTableAggregationFieldNames(kpiDTO.getAggregationElements());

        assertEquals(expectedExpression, kpiDTO.getExpression());
        assertEquals(DEPLOYED_COMPLEX_KPI_OBJ.getAggregationType(), kpiDTO.getAggregationType());
        assertEquals(DEPLOYED_COMPLEX_KPI_OBJ.getAggregationPeriod(), kpiDTO.getAggregationPeriod());
        assertEquals(DEPLOYED_COMPLEX_KPI_OBJ.getObjectType(), kpiDTO.getObjectType());
        assertEquals(expectedContext, actualContext);
        assertEquals(DEPLOYED_COMPLEX_KPI_OBJ.getExecutionGroup(), kpiDTO.getExecutionGroup());
        assertEquals(DEPLOYED_COMPLEX_KPI_OBJ.getIsVisible(), kpiDTO.getIsVisible());

        Assertions.assertNull(kpiDTO.getInpDataCategory());
        Assertions.assertNull(kpiDTO.getInpDataIdentifier());

    }

    @Test
    void getCommonFields() {
        final List<String> list1 = List.of("a", "b", "c");
        final List<String> list2 = List.of("b");
        assertEquals(List.of("b"), KPIDefinitionDTOMapping.getCommonFields(list1, list2));
    }

    @Test
    void getCommonFields_oneCommon() {
        final List<String> list1 = List.of("a", "b", "c");
        final List<String> list2 = List.of("b", "d");
        assertEquals(List.of("b"), KPIDefinitionDTOMapping.getCommonFields(list1, list2));
    }

    @Test
    void getCommonFields_twoCommon() {
        final List<String> list1 = List.of("a", "b", "c");
        final List<String> list2 = List.of("b", "c");
        assertEquals(List.of("b", "c"), KPIDefinitionDTOMapping.getCommonFields(list1, list2));
    }

    @Test
    void getCommonFields_noCommon() {
        final List<String> list1 = List.of("a", "b", "c");
        final List<String> list2 = List.of("d", "e");
        Assertions.assertTrue(KPIDefinitionDTOMapping.getCommonFields(list1, list2).isEmpty());
    }

    @Test
    void buildComplexExpression_oneCommonFieldInInputMetricOverride() {

        final Map<String, KpiDefinitionDTO> inputSimpleKpiDefinitionDtoMap = new HashMap<>();
        final KpiDefinitionDTO overrideSimpleKpi = createSimpleKPIDTO(SIMPLE_KPI_DEFINITION_B,
                List.of("field1"), SCHEMA_NAME_B, PM_DEFINITION_B.getSource(), 60);

        inputSimpleKpiDefinitionDtoMap.put(KPI_DEF_NAME_B, overrideSimpleKpi);
        inputSimpleKpiDefinitionDtoMap.put(KPI_DEF_NAME_C, SIMPLE_KPI_DEFINITION_DTO_C);

        final Map<String, Set<String>> inputMetricTableMap = KPIDefinitionDTOMapping.getInputMetricTableMap(inputSimpleKpiDefinitionDtoMap);
        final ProfileDefinition profileA = ProfileDefinition.builder().name("test profile").context(List.of(AGG_FIELD_1, AGG_FIELD_2)).kpis(List.of(
                KPIReference.builder().ref(KPI_DEF_NAME_A)
                        .inputMetricOverrides(List.of(InputMetricOverride.builder().id(KPI_DEF_NAME_B).context(List.of(AGG_FIELD_1)).build()))
                        .build())).build();
        final String simpleFactTableName = SIMPLE_KPI_DEFINITION_DTO_C.getFactTableName();
        final String overrideSimpleKpiTableName = overrideSimpleKpi.getFactTableName();
        final String expectedStatement =
                AGG_TYPE_COMPLEX_KPI + "(" + overrideSimpleKpiTableName + DOT_CHAR + overrideSimpleKpi.getName() + " + " + simpleFactTableName
                        + DOT_CHAR + SIMPLE_KPI_DEFINITION_DTO_C.getName() + ")" + FROM_KPI_DB + simpleFactTableName + " INNER JOIN " + KPI_DB
                        + overrideSimpleKpiTableName + " ON " + simpleFactTableName + DOT_CHAR + "field1" + EQUAL_CHAR + overrideSimpleKpiTableName
                        + DOT_CHAR + "field1";

        final String complexExpression = buildComplexExpression(COMPLEX_KPI_DEFINITION_A, simpleFactTableName,
                inputSimpleKpiDefinitionDtoMap, inputMetricTableMap);

        assertEquals(expectedStatement, complexExpression);
    }

    @Test
    void buildComplexExpression_twoCommonFieldsInInputMetricOverride() {
        final Map<String, KpiDefinitionDTO> inputSimpleKpiDefinitionDtoMap = new HashMap<>();
        final KpiDefinitionDTO overrideSimpleKpi = createSimpleKPIDTO(SIMPLE_KPI_DEFINITION_B,
                List.of("field1", "field2", "node"), SCHEMA_NAME_B, PM_DEFINITION_B.getSource(), 60);

        inputSimpleKpiDefinitionDtoMap.put(KPI_DEF_NAME_B, overrideSimpleKpi);
        inputSimpleKpiDefinitionDtoMap.put(KPI_DEF_NAME_C, SIMPLE_KPI_DEFINITION_DTO_C);

        final Map<String, Set<String>> inputMetricTableMap = KPIDefinitionDTOMapping.getInputMetricTableMap(inputSimpleKpiDefinitionDtoMap);

        final ProfileDefinition profileA = ProfileDefinition.builder().name("test profile").context(List.of(AGG_FIELD_1, AGG_FIELD_2, "extra"))
                .kpis(List.of(
                        KPIReference.builder().ref(KPI_DEF_NAME_A)
                                .inputMetricOverrides(
                                        List.of(InputMetricOverride.builder().id(KPI_DEF_NAME_B).context(List.of(AGG_FIELD_1, AGG_FIELD_2, "node"))
                                                .build()))
                                .build())).build();
        final String simpleFactTableName = SIMPLE_KPI_DEFINITION_DTO_C.getFactTableName();
        final String overrideSimpleKpiTableName = overrideSimpleKpi.getFactTableName();
        final String expectedStatement =
                AGG_TYPE_COMPLEX_KPI + "(" + overrideSimpleKpiTableName + DOT_CHAR + overrideSimpleKpi.getName() + " + " + simpleFactTableName
                        + DOT_CHAR + SIMPLE_KPI_DEFINITION_DTO_C.getName() + ")" + FROM_KPI_DB + simpleFactTableName + " INNER JOIN " + KPI_DB
                        + overrideSimpleKpiTableName + " ON " + simpleFactTableName + DOT_CHAR + "field1" + EQUAL_CHAR + overrideSimpleKpiTableName
                        + DOT_CHAR + "field1" + " AND " + simpleFactTableName + DOT_CHAR + "field2" + EQUAL_CHAR + overrideSimpleKpiTableName
                        + DOT_CHAR + "field2";

        final String complexExpression = buildComplexExpression(COMPLEX_KPI_DEFINITION_A, simpleFactTableName,
                inputSimpleKpiDefinitionDtoMap, inputMetricTableMap);

        assertEquals(expectedStatement, complexExpression);
    }

    @Test
    void buildComplexExpression_pmsComeFromSameTable_noInnerJoin() {
        final Map<String, KpiDefinitionDTO> inputSimpleKpiDefinitionDtoMap = new HashMap<>();
        final KpiDefinitionDTO overrideSimpleKpi = createSimpleKPIDTO(SIMPLE_KPI_DEFINITION_B,
                List.of("field1", "field2"), SCHEMA_NAME_B, PM_DEFINITION_B.getSource(), 60);

        inputSimpleKpiDefinitionDtoMap.put(KPI_DEF_NAME_B, overrideSimpleKpi);
        inputSimpleKpiDefinitionDtoMap.put(KPI_DEF_NAME_C, SIMPLE_KPI_DEFINITION_DTO_C);

        final Map<String, Set<String>> inputMetricTableMap = KPIDefinitionDTOMapping.getInputMetricTableMap(inputSimpleKpiDefinitionDtoMap);

        final ProfileDefinition profileA = ProfileDefinition.builder().name("test profile").context(List.of("field1"))
                .kpis(List.of(
                        KPIReference.builder().ref(KPI_DEF_NAME_A)
                                .inputMetricOverrides(
                                        List.of(InputMetricOverride.builder().id(KPI_DEF_NAME_B).context(List.of("field1", "field2")).build(),
                                                InputMetricOverride.builder().id(KPI_DEF_NAME_C).context(List.of("field1", "field2")).build()))
                                .build())).build();
        final String simpleFactTableName = SIMPLE_KPI_DEFINITION_DTO_C.getFactTableName();
        final String overrideSimpleKpiTableName = overrideSimpleKpi.getFactTableName();
        final String expectedStatement =
                AGG_TYPE_COMPLEX_KPI + "(" + overrideSimpleKpiTableName + DOT_CHAR + overrideSimpleKpi.getName() + " + " + simpleFactTableName
                        + DOT_CHAR + SIMPLE_KPI_DEFINITION_DTO_C.getName() + ")" + FROM_KPI_DB + simpleFactTableName;

        final String complexExpression = buildComplexExpression(COMPLEX_KPI_DEFINITION_A, simpleFactTableName,
                inputSimpleKpiDefinitionDtoMap, inputMetricTableMap);

        assertEquals(expectedStatement, complexExpression);
    }

    @Test
    void buildComplexExpression_overrideSameAsProfileContext_noInnerJoin() throws JsonProcessingException {
        final Map<String, KpiDefinitionDTO> inputSimpleKpiDefinitionDtoMap = new HashMap<>();
        final KpiDefinitionDTO overrideSimpleKpi = createSimpleKPIDTO(SIMPLE_KPI_DEFINITION_B,
                List.of("field1", "field2"), SCHEMA_NAME_B, PM_DEFINITION_B.getSource(), 60);

        inputSimpleKpiDefinitionDtoMap.put(KPI_DEF_NAME_B, overrideSimpleKpi);
        inputSimpleKpiDefinitionDtoMap.put(KPI_DEF_NAME_C, SIMPLE_KPI_DEFINITION_DTO_C);

        final Map<String, Set<String>> inputMetricTableMap = KPIDefinitionDTOMapping.getInputMetricTableMap(inputSimpleKpiDefinitionDtoMap);

        final ProfileDefinition profileA = ProfileDefinition.builder().name("test profile").context(List.of("field1", "field2"))
                .kpis(List.of(
                        KPIReference.builder().ref(KPI_DEF_NAME_A)
                                .inputMetricOverrides(
                                        List.of(InputMetricOverride.builder().id(KPI_DEF_NAME_B).context(List.of("field1", "field2")).build()))
                                .build())).build();
        final String simpleFactTableName = SIMPLE_KPI_DEFINITION_DTO_C.getFactTableName();
        final String overrideSimpleKpiTableName = overrideSimpleKpi.getFactTableName();
        final String expectedStatement =
                AGG_TYPE_COMPLEX_KPI + "(" + overrideSimpleKpiTableName + DOT_CHAR + overrideSimpleKpi.getName() + " + " + simpleFactTableName
                        + DOT_CHAR + SIMPLE_KPI_DEFINITION_DTO_C.getName() + ")" + FROM_KPI_DB + simpleFactTableName;

        final String complexExpression = buildComplexExpression(COMPLEX_KPI_DEFINITION_A, simpleFactTableName,
                inputSimpleKpiDefinitionDtoMap, inputMetricTableMap);

        assertEquals(expectedStatement, complexExpression);
    }

    @Test
    void buildComplexExpression_simpleKpiGot() {
        final Map<String, KpiDefinitionDTO> inputSimpleKpiDefinitionDtoMap = new HashMap<>();
        final KpiDefinitionDTO overrideSimpleKpi = createSimpleKPIDTO(SIMPLE_KPI_DEFINITION_B,
                List.of("field1", "field2"), SCHEMA_NAME_B, PM_DEFINITION_B.getSource(), 60);

        inputSimpleKpiDefinitionDtoMap.put(KPI_DEF_NAME_B, overrideSimpleKpi);
        inputSimpleKpiDefinitionDtoMap.put(KPI_DEF_NAME_C, SIMPLE_KPI_DEFINITION_DTO_C);

        final Map<String, Set<String>> inputMetricTableMap = KPIDefinitionDTOMapping.getInputMetricTableMap(inputSimpleKpiDefinitionDtoMap);

        final ProfileDefinition profileA = ProfileDefinition.builder().name("test profile").context(List.of(AGG_FIELD_1, AGG_FIELD_2))
                .kpis(List.of(
                        KPIReference.builder().ref(KPI_DEF_NAME_A)
                                .inputMetricOverrides(
                                        List.of(InputMetricOverride.builder().id(KPI_DEF_NAME_B).context(List.of(AGG_FIELD_1, AGG_FIELD_2)).build()))
                                .build())).build();
        final String simpleFactTableName = SIMPLE_KPI_DEFINITION_DTO_C.getFactTableName();
        final String overrideSimpleKpiTableName = overrideSimpleKpi.getFactTableName();
        final String expectedStatement =
                AGG_TYPE_COMPLEX_KPI + "(" + overrideSimpleKpiTableName + DOT_CHAR + overrideSimpleKpi.getName() + " + " + simpleFactTableName
                        + DOT_CHAR + SIMPLE_KPI_DEFINITION_DTO_C.getName() + ")" + FROM_KPI_DB + simpleFactTableName;

        final String complexExpression = buildComplexExpression(COMPLEX_KPI_DEFINITION_A, simpleFactTableName,
                inputSimpleKpiDefinitionDtoMap, inputMetricTableMap);

        assertEquals(expectedStatement, complexExpression);
    }

    @Test
    void buildComplexExpression_unassociatedJoinFields() throws Exception {

        final Map<String, KpiDefinitionDTO> inputSimpleKpiDefinitionDtoMap = new HashMap<>();
        final KpiDefinitionDTO overrideSimpleKpi = createSimpleKPIDTO(SIMPLE_KPI_DEFINITION_B,
                List.of("notAField"), SCHEMA_NAME_B, PM_DEFINITION_B.getSource(), 60);

        inputSimpleKpiDefinitionDtoMap.put(KPI_DEF_NAME_B, overrideSimpleKpi);
        inputSimpleKpiDefinitionDtoMap.put(KPI_DEF_NAME_C, SIMPLE_KPI_DEFINITION_DTO_C);

        final Map<String, Set<String>> inputMetricTableMap = KPIDefinitionDTOMapping.getInputMetricTableMap(inputSimpleKpiDefinitionDtoMap);

        final ProfileDefinition profileA = ProfileDefinition.builder().name("test profile").context(List.of(AGG_FIELD_1, AGG_FIELD_2)).kpis(List.of(
                KPIReference.builder().ref(KPI_DEF_NAME_A)
                        .inputMetricOverrides(List.of(InputMetricOverride.builder().id(KPI_DEF_NAME_B).context(List.of("notAField")).build()))
                        .build())).build();

        final String simpleFactTableName = SIMPLE_KPI_DEFINITION_DTO_C.getFactTableName();
        final String overrideSimpleKpiTableName = overrideSimpleKpi.getFactTableName();

        assertThrows(CsacValidationException.class, () -> buildComplexExpression(COMPLEX_KPI_DEFINITION_A, simpleFactTableName,
                inputSimpleKpiDefinitionDtoMap, inputMetricTableMap));

    }

    @Test
    void createComplexKPIDTO_mismatchedContextOverride() throws Exception {

        final Map<String, KpiDefinitionDTO> inputSimpleKpiDefinitionDtoMap = new HashMap<>();

        inputSimpleKpiDefinitionDtoMap.put(KPI_DEF_NAME_B, SIMPLE_KPI_DEFINITION_DTO_B);

        final KpiDefinitionDTO overrideSimpleKpi = createSimpleKPIDTO(SIMPLE_KPI_DEFINITION_C,
                List.of("notAField"), SCHEMA_NAME_C, PM_DEFINITION_C.getSource(), 60);

        inputSimpleKpiDefinitionDtoMap.put(KPI_DEF_NAME_B, overrideSimpleKpi);
        final KPIDefinition complexKpi = VALID_COMPLEX_KPI_DEF_OBJ.toBuilder()
                .inputMetrics(List.of(new InputMetric(KPI_DEF_NAME_B, "input_alias", InputMetric.Type.KPI))).build();

        assertThrows(CsacValidationException.class, () -> createComplexKPIDTO(complexKpi, VALID_PROFILE_DEF_OBJ,
                DEFAULT_AGGREGATION_PERIOD, inputSimpleKpiDefinitionDtoMap));

    }

    @Test
    void getJoinClause_noJoin() throws Exception {

        final Map<String, Set<String>> inputMetricTableMap = new TreeMap<>();

        inputMetricTableMap.put("table1", Set.of("field1", "field2"));

        final KPIDefinition dto = KPIDefinition.builder().name("Dummy KPI").build();

        final StringBuilder actual = KPIDefinitionDTOMapping.getJoinClause(dto, "table1", inputMetricTableMap);

        assertTrue(actual.isEmpty());

    }

    @Test
    void getJoinClause_singleJoin() throws Exception {

        final Map<String, Set<String>> inputMetricTableMap = new TreeMap<>();

        inputMetricTableMap.put("table1", Set.of("field1", "field2"));
        inputMetricTableMap.put("table2", Set.of("field1", "field2", "field3"));

        final KPIDefinition dto = KPIDefinition.builder().name("Dummy KPI").build();

        final StringBuilder actual = KPIDefinitionDTOMapping.getJoinClause(dto, "table1", inputMetricTableMap);

        assertFalse(actual.isEmpty());

        final List<String> actualClauses = new ArrayList<>();

        for (final String s : actual.toString().trim().split("INNER JOIN")) {
            if (!ObjectUtils.isEmpty(s)) {
                actualClauses.add(s);
            }
        }

        assertEquals(1, actualClauses.size());

        final String table2clause = actualClauses.stream().filter(c -> c.contains("kpi_db://table2 ON")).findFirst()
                .orElseThrow(IllegalStateException::new);

        assertTrue(table2clause.contains(" AND "));
        assertTrue(table2clause.contains("table1.field2 = table2.field2"));
        assertTrue(table2clause.contains("table1.field1 = table2.field1"));
    }

    @Test
    void getJoinClause_multipleJoins() throws Exception {

        final Map<String, Set<String>> inputMetricTableMap = new TreeMap<>();

        inputMetricTableMap.put("table1", Set.of("field1", "field2"));
        inputMetricTableMap.put("table2", Set.of("field1", "field2", "field3"));
        inputMetricTableMap.put("table3", Set.of("field1", "field2", "field4"));

        final KPIDefinition dto = KPIDefinition.builder().name("Dummy KPI").build();

        final StringBuilder actual = KPIDefinitionDTOMapping.getJoinClause(dto, "table1", inputMetricTableMap);

        // what we expect is:
        // - two INNER JOIN clauses
        // - each clause will have a to-table.
        // - each to-table will be unique
        // - each ON section contains fromTable.col = toTable.col for common columns

        final List<String> actualClauses = new ArrayList<>();

        for (final String s : actual.toString().trim().split("INNER JOIN")) {
            if (!ObjectUtils.isEmpty(s)) {
                actualClauses.add(s);
            }
        }
        actualClauses.removeIf(clause -> ObjectUtils.isEmpty(clause));

        assertEquals(2, actualClauses.size());

        final String table2clause = actualClauses.stream().filter(c -> c.contains("kpi_db://table2 ON")).findFirst()
                .orElseThrow(IllegalStateException::new);

        assertTrue(table2clause.contains(" AND "));
        assertTrue(table2clause.contains("table1.field2 = table2.field2"));
        assertTrue(table2clause.contains("table1.field1 = table2.field1"));

        final String table3clause = actualClauses.stream().filter(c -> c.contains("kpi_db://table3 ON")).findFirst()
                .orElseThrow(IllegalStateException::new);

        assertTrue(table3clause.contains(" AND "));
        assertTrue(table3clause.contains("table1.field2 = table3.field2"));
        assertTrue(table3clause.contains("table1.field1 = table3.field1"));
    }

    @Test
    void getJoinClause_error_noPossibleJoin() throws Exception {

        final Map<String, Set<String>> inputMetricTableMap = new TreeMap<>();

        inputMetricTableMap.put("table1", Set.of("field1", "field2"));
        inputMetricTableMap.put("table2", Set.of("field3", "field4", "field5"));
        inputMetricTableMap.put("table3", Set.of("field3", "field4", "field5"));

        final KPIDefinition dto = KPIDefinition.builder().name("Dummy KPI").build();

        assertThrows(CsacValidationException.class, () -> KPIDefinitionDTOMapping.getJoinClause(dto, "table1", inputMetricTableMap));

    }

    @Test
    void getJoinClause_error_oneNotPossibleJoin() throws Exception {

        final Map<String, Set<String>> inputMetricTableMap = new TreeMap<>();

        inputMetricTableMap.put("table1", Set.of("field1", "field2"));
        inputMetricTableMap.put("table2", Set.of("field1", "field2", "field5"));
        inputMetricTableMap.put("table3", Set.of("field3", "field4", "field5"));

        final KPIDefinition dto = KPIDefinition.builder().name("Dummy KPI").build();

        assertThrows(CsacValidationException.class, () -> KPIDefinitionDTOMapping.getJoinClause(dto, "table1", inputMetricTableMap));

    }
}
