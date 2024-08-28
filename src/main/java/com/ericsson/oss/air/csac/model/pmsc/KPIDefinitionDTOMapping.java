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

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import com.ericsson.oss.air.csac.handler.pmsc.util.KpiId;
import com.ericsson.oss.air.csac.model.InputMetric;
import com.ericsson.oss.air.csac.model.KPIDefinition;
import com.ericsson.oss.air.csac.model.ProfileDefinition;
import com.ericsson.oss.air.exception.CsacValidationException;
import com.ericsson.oss.air.util.MapUtils;
import org.springframework.util.ObjectUtils;

/**
 * The type Kpi definition dto mapping.
 */
public class KPIDefinitionDTOMapping {

    public static final String OBJECT_TYPE = "FLOAT";

    public static final String CSAC_EXECUTION_GROUP = "csac_execution_group";

    public static final String EQUAL_CHAR = " = ";

    public static final String DOT_CHAR = ".";

    public static final String KPI_DB = "kpi_db://";

    public static final String FROM_KPI_DB = " FROM " + KPI_DB;

    public static final String AND = " AND ";

    public static final String INNER_JOIN = " INNER JOIN ";

    public static final String ON = " ON ";

    private KPIDefinitionDTOMapping() {
    }

    /**
     * Build KPI DTO unit name and the result will be used as ID
     *
     * @return the string
     */
    static String buildUniqueKpiDefName() {
        return new KpiId().generateKpiName();
    }

    /**
     * Substitute parameters in the supplied expression by the following steps:
     * <ol>
     * <li> Replace alias in expression with inputMetric Id
     * <li> Add the qualifier as prefix
     * </ol>
     *
     * @param expression   the original csac expression
     * @param qualifier    the qualifier
     * @param inputMetrics the input metrics
     * @return the PMSC expression
     */
    static String substituteExpressionParameters(final String expression, final String qualifier, final List<InputMetric> inputMetrics) {

        String updatedExpression = expression;

        final Map<String, String> aliasMap = new HashMap<>();

        if (ObjectUtils.isEmpty(inputMetrics)) {
            return updatedExpression;
        }

        inputMetrics.forEach(inputMetric -> {
            if (ObjectUtils.isEmpty(inputMetric.getAlias())) {
                return;
            }
            aliasMap.put(inputMetric.getAlias(), inputMetric.getId());
        });

        // Replace alias with the input metric id, which is pm def name
        for (final Map.Entry<String, String> aliasEntry : aliasMap.entrySet()) {
            updatedExpression = updatedExpression.replace(aliasEntry.getKey(), aliasEntry.getValue());
        }

        // Add prefix to input metric id
        for (final InputMetric inputMetric : inputMetrics) {
            updatedExpression = updatedExpression.replace(inputMetric.getId(),
                    qualifier + DOT_CHAR + inputMetric.getId());
        }

        return updatedExpression;
    }

    /**
     * Build the expression for a complex KPI that uses only simple KPIs as input metrics.
     *
     * @param kpiDefinition                  the KPI definition
     * @param simpleKpiFactTableName         the fact table name for the simple KPIs
     * @param inputSimpleKpiDefinitionDtoMap the simple KPI Definition DTOs mapped by their input metric IDs
     * @param inputMetricTableMap            the current {@link ProfileDefinition}
     * @return the expression for a complex KPI that uses only simple KPIs as input metrics.
     */
    static String buildComplexExpression(final KPIDefinition kpiDefinition,
                                         final String simpleKpiFactTableName,
                                         final Map<String, KpiDefinitionDTO> inputSimpleKpiDefinitionDtoMap,
                                         Map<String, Set<String>> inputMetricTableMap) {

        // Need to substitute the alias in expression with corresponding fact table name and generated runtime simple kpi definition names
        String updatedExpression = kpiDefinition.getExpression();

        final Map<String, String> aliasMap = new HashMap<>();
        final List<InputMetric> inputMetrics = kpiDefinition.getInputMetrics();
        inputMetrics.forEach(inputMetric -> {
            if (ObjectUtils.isEmpty(inputMetric.getAlias())) {
                return;
            }
            aliasMap.put(inputMetric.getAlias(), inputMetric.getId());
        });

        // Replace alias with the input metric id
        for (final Map.Entry<String, String> aliasEntry : aliasMap.entrySet()) {
            updatedExpression = updatedExpression.replace(aliasEntry.getKey(), aliasEntry.getValue());
        }

        /*
          The simple KPI input metric IDs are not the column names for the PMSC. The generated runtime simple KPI definition names are the column names.
          Therefore, the input metric IDs need to be substituted for the generated runtime simple KPI definition names.
         */
        for (final Map.Entry<String, KpiDefinitionDTO> inputMetricMapEntry : inputSimpleKpiDefinitionDtoMap.entrySet()) {
            final String tableName = inputMetricMapEntry.getValue().getFactTableName();
            final String columnName = tableName + DOT_CHAR + inputMetricMapEntry.getValue().getName();
            updatedExpression = updatedExpression.replace(inputMetricMapEntry.getKey(), columnName);
        }

        final StringBuilder joinClause = getJoinClause(kpiDefinition, simpleKpiFactTableName, inputMetricTableMap);

        return updatedExpression + FROM_KPI_DB + simpleKpiFactTableName + joinClause;
    }

    private static StringBuilder getTableJoin(final Map.Entry<String, Set<String>> fromTable,
                                              final Map.Entry<String, Set<String>> toTable) {

        // create the join clause for these two tables
        final StringBuilder joinBuilder = new StringBuilder();

        for (final String column : fromTable.getValue()) {

            final StringBuilder columnJoinBuilder = new StringBuilder();

            if (toTable.getValue().contains(column)) {
                columnJoinBuilder.append(fromTable.getKey()).append(DOT_CHAR).append(column).append(EQUAL_CHAR);
                columnJoinBuilder.append(toTable.getKey()).append(DOT_CHAR).append(column);

                if (!joinBuilder.isEmpty()) {
                    joinBuilder.append(AND);
                } else {
                    joinBuilder.append(INNER_JOIN).append(KPI_DB).append(toTable.getKey()).append(ON);
                }

                joinBuilder.append(columnJoinBuilder);
            }
        }

        return joinBuilder;

    }

    /*
     * (non-javadoc)
     *
     * Returns a StringBuilder containing all the elements of the join clause needed for a given complex KPI.
     *
     * The join uses a map of input metric table names and context fields to determine which, if any, tables need
     * to be joined using an INNER JOIN clause.
     */
    static StringBuilder getJoinClause(final KPIDefinition kpiDefinition, final String simpleKpiFactTableName,
                                       final Map<String, Set<String>> inputMetricTableMap) {

        final StringBuilder joinStr = new StringBuilder();

        // add INNER JOIN clause for unique pairs of FROM table and JOIN table
        if (inputMetricTableMap.size() > 1) {

            final Map.Entry<String, Set<String>> fromTable = MapUtils.getMapEntry(simpleKpiFactTableName, inputMetricTableMap);
            inputMetricTableMap.remove(simpleKpiFactTableName);

            for (final Map.Entry<String, Set<String>> toTable : inputMetricTableMap.entrySet()) {

                final StringBuilder tableJoin = getTableJoin(fromTable, toTable);

                if (ObjectUtils.isEmpty(tableJoin)) {
                    final String joinContext = fromTable.getValue().toString();
                    throw new CsacValidationException(
                            "Complex KPI \"" + kpiDefinition.getName() + "\" cannot be instantiated with context " + joinContext
                                    + ". Input metric contexts cannot be associated.");
                }

                joinStr.append(tableJoin);
            }

            if (ObjectUtils.isEmpty(joinStr)) {
                throw new CsacValidationException(
                        "Complex KPI \"" + kpiDefinition.getName() + "\" cannot be instantiated. Input metric contexts cannot be associated.");
            }
        }

        return joinStr;
    }

    /**
     * Return the common fields between two lists
     *
     * @param profileContext               the profile level aggregation fields
     * @param inputMetricsContextOverrides the input metrics level context override fields
     * @return a list of common fields
     */
    public static List<String> getCommonFields(final List<String> profileContext, final List<String> inputMetricsContextOverrides) {
        return profileContext.stream().filter(inputMetricsContextOverrides::contains).collect(Collectors.toList());
    }

    /**
     * Build aggregation elements list by adding a qualifier to aggregation fields
     *
     * @param aggregationFields the aggregation fields
     * @param qualifier         the qualifier
     * @return the list of aggregation fields
     */
    public static List<String> buildAggregationElements(final List<String> aggregationFields, final String qualifier) {
        return aggregationFields.stream().map(element -> qualifier + DOT_CHAR + element).collect(Collectors.toList());
    }

    /**
     * A mapping function that converts a simple KPI definition into a runtime kpi definition
     *
     * @param kpiDefinition     the simple kpi definition
     * @param aggregationFields the list of aggregation fields
     * @param schemaName        the schema name that will be used as table name
     * @param source            the schema reference
     * @param aggregationPeriod the aggregation period
     * @return the runtime kpi definition dto
     */
    public static KpiDefinitionDTO createSimpleKPIDTO(final KPIDefinition kpiDefinition, final List<String> aggregationFields,
                                                      final String schemaName, final String source, final Integer aggregationPeriod) {
        final String kpiName = buildUniqueKpiDefName();

        final String expression = substituteExpressionParameters(kpiDefinition.getExpression(), schemaName, kpiDefinition.getInputMetrics());
        final List<String> aggregationElements = buildAggregationElements(aggregationFields, schemaName);

        return KpiDefinitionDTO.builder()
                .withKpiType(KpiTypeEnum.SIMPLE)
                .withName(kpiName)
                .withExpression(expression)
                .withAggregationType(kpiDefinition.getAggregationType())
                .withAggregationPeriod(aggregationPeriod)
                .withObjectType(OBJECT_TYPE)
                .withAggregationElements(aggregationElements)
                .withIsVisible(kpiDefinition.getIsVisible())
                .withInpDataCategory(InputMetric.Type.PM_DATA.toString())
                .withInpDataIdentifier(source)
                .build();
    }

    /**
     * Maps a single complex CSAC KPI that uses only simple KPIs as input metrics to its corresponding PMSC KPI definition.
     * <p>
     * Following fields are hardcoded as default value. Execution group: csac_execution_group ObjectType: float
     *
     * @param complexDefinition              the kpi definition
     * @param profileDefinition              the profile definition
     * @param inputSimpleKpiDefinitionDtoMap the simple KPI Definition DTOs mapped by their input metric IDs
     * @return the kpi definition dto
     */
    public static KpiDefinitionDTO createComplexKPIDTO(
            final KPIDefinition complexDefinition,
            final ProfileDefinition profileDefinition,
            final Integer aggregationPeriod,
            final Map<String, KpiDefinitionDTO> inputSimpleKpiDefinitionDtoMap
    ) {

        // get a map of input fact table and context fields for potential join creation
        final Map<String, Set<String>> inputMetricTableMap = getInputMetricTableMap(inputSimpleKpiDefinitionDtoMap);

        // context fields in the profile must be contained in at least one of the Simple KPI tables
        // just find the first that includes all profile context fields
        final Set<String> profileContext = new HashSet<>(profileDefinition.getContext());
        final Optional<Map.Entry<String, Set<String>>> matchingFactTable = inputMetricTableMap.entrySet().stream()
                .filter(e -> e.getValue().containsAll(profileContext)
                ).findFirst();

        if (matchingFactTable.isEmpty()) {
            throw new CsacValidationException(
                    "Complex KPI \"" + complexDefinition.getName() + "\" cannot be instantiated. No matching input metric context.");
        }

        final String simpleKpiFactTableName = matchingFactTable.get().getKey();

        final String kpiName = buildUniqueKpiDefName();
        final List<String> aggregationElements = buildAggregationElements(profileDefinition.getContext(), simpleKpiFactTableName);
        final String expression = buildComplexExpression(complexDefinition, simpleKpiFactTableName, inputSimpleKpiDefinitionDtoMap,
                inputMetricTableMap);

        return KpiDefinitionDTO.builder()
                .withKpiType(KpiTypeEnum.COMPLEX)
                .withName(kpiName)
                .withExpression(expression)
                .withAggregationType(complexDefinition.getAggregationType())
                .withAggregationPeriod(aggregationPeriod)
                .withObjectType(OBJECT_TYPE)
                .withAggregationElements(aggregationElements)
                .withExecutionGroup(CSAC_EXECUTION_GROUP)
                .withIsVisible(complexDefinition.getIsVisible())
                .build();
    }

    /**
     * Returns a map of input metric table names and context fields used when validating complex KPIs and for generating join statements in complex
     * KPI expressions.
     *
     * @param inputSimpleKpiDefinitionDtoMap map of input KPI definitions to input KPI name.
     * @return a map of input metric table names and context fields
     */
    public static Map<String, Set<String>> getInputMetricTableMap(final Map<String, KpiDefinitionDTO> inputSimpleKpiDefinitionDtoMap) {

        final Map<String, Set<String>> inputMetricTableMap = new LinkedHashMap<>();

        for (final KpiDefinitionDTO inputSimpleKpi : inputSimpleKpiDefinitionDtoMap.values()) {
            final List<String> factTableContext = getFactTableAggregationFieldNames(inputSimpleKpi.getAggregationElements());

            inputMetricTableMap.putIfAbsent(inputSimpleKpi.getFactTableName(), new TreeSet(factTableContext));
        }

        return inputMetricTableMap;

    }

    /**
     * Returns a list of aggregation field names stripped of the aggregation table names in the source aggregation fields.
     *
     * @param aggregationField list of qualified aggregation fields
     * @return a list of aggregation field names stripped of the aggregation table names
     */
    public static List<String> getFactTableAggregationFieldNames(final List<String> aggregationField) {
        return aggregationField.stream().map(a -> a.substring(a.lastIndexOf('.') + 1)
        ).collect(Collectors.toList());
    }
}
