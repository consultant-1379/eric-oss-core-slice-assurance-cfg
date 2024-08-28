/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.repository.impl.jdbc;

import static com.ericsson.oss.air.util.RestEndpointUtil.getSafeSublistIndex;
import static java.util.Collections.singletonList;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.ericsson.oss.air.csac.configuration.JdbcConfig;
import com.ericsson.oss.air.csac.model.InputMetric;
import com.ericsson.oss.air.csac.model.KPIDefinition;
import com.ericsson.oss.air.csac.model.PMDefinition;
import com.ericsson.oss.air.csac.repository.KPIDefinitionDAO;
import com.ericsson.oss.air.csac.repository.impl.jdbc.mapper.InputMetricWithKpiDefsMapper;
import com.ericsson.oss.air.csac.repository.impl.jdbc.mapper.KpiDefinitionMapper;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.DataAccessException;
import org.springframework.data.util.Pair;
import org.springframework.jdbc.core.ArgumentPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

/**
 * The type Kpi definition dao jdbc.
 */
@Slf4j
@Repository
@Primary
@AllArgsConstructor
@Profile({ "prod", "test" })
public class KPIDefinitionDAOJdbcImpl implements KPIDefinitionDAO {

    //Input metrics column
    public static final String COLUMN_KPI_NAME = "kpi_name";
    public static final String COLUMN_IM_ID = "id";
    public static final String COLUMN_IM_ALIAS = "alias";
    public static final String COLUMN_IM_INP_TYPE = "inp_type";

    // KPI Table column
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_DESCRIPTION = "description";
    public static final String COLUMN_DISPLAY_NAME = "display_name";
    public static final String COLUMN_EXPRESSION = "expression";
    public static final String COLUMN_AGGREGATION_TYPE = "aggregation_type";
    public static final String COLUMN_IS_VISIBLE = "is_visible";

    // Table name
    public static final String KPI_DEF_TABLE_NAME = "%1$s.kpi_def";
    public static final String KPI_INPUT_METRIC_TABLE_NAME = "%1$s.kpi_input_metric";

    // SQL Statement
    public static final String COUNT_STATEMENT = "SELECT COUNT(*) FROM " + KPI_DEF_TABLE_NAME;
    public static final String SELECT_ALL_NAMES_STATEMENT = "SELECT name FROM " + KPI_DEF_TABLE_NAME;
    public static final String INSERT_KPI_DEF_NAMED_STATEMENT = "INSERT INTO " + KPI_DEF_TABLE_NAME
            + "(name, description, display_name, expression, aggregation_type, is_visible)"
            + " VALUES(:name, :description, :displayName, :expression, :aggregationType, :isVisible)"
            + " ON CONFLICT (name) DO UPDATE"
            + " SET description = EXCLUDED.description, "
            + " display_name = EXCLUDED.display_name,"
            + " expression = EXCLUDED.expression,"
            + " aggregation_type = EXCLUDED.aggregation_type,"
            + " is_visible = EXCLUDED.is_visible";

    public static final String INSERT_INPUT_METRIC_NAMED_STATEMENT = "INSERT INTO " + KPI_INPUT_METRIC_TABLE_NAME
            + "(kpi_name, id, alias, inp_type)"
            + " VALUES(:kpi_name, :id, :alias, :inp_type::%1$s.inp_metric_type)"
            + " ON CONFLICT (kpi_name, id) DO NOTHING";

    public static final String SELECT_KPI_BY_OFFSET_AND_LIMIT_STATEMENT = "SELECT * FROM %1$s.kpi_def OFFSET %2$d LIMIT %3$d";

    public static final String SELECT_ALL_KPI_STATEMENT = "SELECT * FROM " + KPI_DEF_TABLE_NAME;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Autowired
    private JdbcConfig jdbcConfig;

    private static List<MapSqlParameterSource> getInputMetricsSqlPsList(
            final KPIDefinition kpiDef) {
        final List<InputMetric> inputMetrics = kpiDef.getInputMetrics();

        if (ObjectUtils.isEmpty(inputMetrics)) {
            return new ArrayList<>();
        }

        return inputMetrics
                .stream()
                .map(inputMetric -> new MapSqlParameterSource()
                        .addValue(COLUMN_KPI_NAME, kpiDef.getName())
                        .addValue(COLUMN_IM_ID, inputMetric.getId())
                        .addValue(COLUMN_IM_ALIAS, inputMetric.getAlias())
                        .addValue(COLUMN_IM_INP_TYPE, inputMetric.getType().toString()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void saveKPIDefinition(
            @NonNull final KPIDefinition kpiDef) {
        this.insertKPIDefinitions(singletonList(kpiDef));
    }

    @Override
    @Transactional
    public void insertKPIDefinitions(
            @NonNull final List<KPIDefinition> kpiDefs) {

        // Insert KPI
        final SqlParameterSource[] kpiParaBatch = SqlParameterSourceUtils.createBatch(kpiDefs.toArray());
        this.namedParameterJdbcTemplate.batchUpdate(String.format(INSERT_KPI_DEF_NAMED_STATEMENT, this.jdbcConfig.getDictionarySchemaName()),
                kpiParaBatch);

        // Insert IM
        final SqlParameterSource[] imParaBatch = kpiDefs.stream()
                .flatMap(kpi -> getInputMetricsSqlPsList(kpi).stream())
                .toArray(SqlParameterSource[]::new);
        if (ObjectUtils.isEmpty(imParaBatch)) {
            return;
        }
        this.namedParameterJdbcTemplate.batchUpdate(String.format(INSERT_INPUT_METRIC_NAMED_STATEMENT, this.jdbcConfig.getDictionarySchemaName()),
                imParaBatch);

    }

    @Override
    public KPIDefinition findByKPIDefName(final String kpiDefName) {

        final Set<KPIDefinition> kpiFetchResult = this.findByKPIDefNames(Set.of(kpiDefName));

        if (kpiFetchResult.isEmpty()) {
            return null;
        }

        return kpiFetchResult.iterator().next();
    }

    /**
     * A helper method to retrieve input metrics and add them into KPI Definition
     *
     * @param kpis a list of kpi definition
     */
    private void retrieveAndAddInputMetric(final List<KPIDefinition> kpis) {

        if (ObjectUtils.isEmpty(kpis)) {
            return;
        }

        final Set<String> kpiNames = kpis.stream().map(KPIDefinition::getName).collect(Collectors.toSet());

        final String findByNameIMStatement = String.format("SELECT * FROM " + KPI_INPUT_METRIC_TABLE_NAME + " WHERE kpi_name in (%2$s)",
                this.jdbcConfig.getDictionarySchemaName(),
                kpiNames.stream().map(v -> "?").collect(Collectors.joining(", ")));
        this.jdbcTemplate.query(findByNameIMStatement,
                new ArgumentPreparedStatementSetter(kpiNames.toArray()),
                new InputMetricWithKpiDefsMapper(kpis));
    }

    @Override
    public List<KPIDefinition> findAllKPIDefs(final Integer start, final Integer rows) {
        final Integer total = this.totalKPIDefinitions();
        final Pair<Integer, Integer> safeIndex = getSafeSublistIndex(total, start, rows);

        final String findByNameStatement = String.format(SELECT_KPI_BY_OFFSET_AND_LIMIT_STATEMENT, this.jdbcConfig.getDictionarySchemaName(),
                safeIndex.getFirst(), rows);

        final List<KPIDefinition> kpis = this.jdbcTemplate.query(findByNameStatement, new KpiDefinitionMapper());
        this.retrieveAndAddInputMetric(kpis);

        return kpis;
    }

    @Override
    public List<KPIDefinition> findAll() {
        final List<KPIDefinition> kpis = this.jdbcTemplate.query(String.format(SELECT_ALL_KPI_STATEMENT, this.jdbcConfig.getDictionarySchemaName()),
                new KpiDefinitionMapper());

        this.retrieveAndAddInputMetric(kpis);

        return kpis;
    }

    /**
     * A helper method to return a set of {@link KPIDefinition} by given names
     *
     * @param kpiNames a set of kpiNames
     * @return a set of {@link KPIDefinition}
     */
    Set<KPIDefinition> findByKPIDefNames(final Set<String> kpiNames) {
        if (ObjectUtils.isEmpty(kpiNames)) {
            return new HashSet<>();
        }

        final String findByNameStatement = String.format("SELECT * FROM " + KPI_DEF_TABLE_NAME + " WHERE name in (%2$s)",
                this.jdbcConfig.getDictionarySchemaName(),
                kpiNames.stream().map(v -> "?").collect(Collectors.joining(", ")));
        final List<KPIDefinition> kpis = this.jdbcTemplate.query(findByNameStatement,
                new ArgumentPreparedStatementSetter(kpiNames.toArray()),
                new KpiDefinitionMapper());

        this.retrieveAndAddInputMetric(kpis);
        return new HashSet<>(kpis);
    }

    @Override
    public Set<KPIDefinition> getAffectedKPIDefs(final Set<PMDefinition> pmDefs) {
        if (ObjectUtils.isEmpty(pmDefs)) {
            return new HashSet<>();
        }

        final List<String> metricsIds = pmDefs.stream().map(PMDefinition::getName).collect(Collectors.toList());

        final String findByNameIMStatement = String.format("SELECT kpi_name FROM " + KPI_INPUT_METRIC_TABLE_NAME + " WHERE id in (%2$s)",
                this.jdbcConfig.getDictionarySchemaName(),
                metricsIds.stream().map(v -> "?").collect(Collectors.joining(", ")));

        final List<String> kpiDefNames = this.jdbcTemplate.queryForList(findByNameIMStatement, String.class, metricsIds.toArray());
        return this.findByKPIDefNames(new HashSet<>(kpiDefNames));
    }

    @Override
    public Set<String> getAllKpiDefNames() {
        final List<String> strings = this.jdbcTemplate.queryForList(
                String.format(SELECT_ALL_NAMES_STATEMENT, this.jdbcConfig.getDictionarySchemaName()), String.class);
        if (ObjectUtils.isEmpty(strings)) {
            return new HashSet<>();
        }
        return new HashSet<>(strings);
    }

    @Override
    public Integer totalKPIDefinitions() throws DataAccessException {
        return this.jdbcTemplate.queryForObject(String.format(COUNT_STATEMENT, this.jdbcConfig.getDictionarySchemaName()), Integer.class);
    }

}
