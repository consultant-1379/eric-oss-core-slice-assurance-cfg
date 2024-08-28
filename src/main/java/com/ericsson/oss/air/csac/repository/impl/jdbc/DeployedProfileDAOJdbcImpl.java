/*******************************************************************************
 * COPYRIGHT Ericsson 2024
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

import java.sql.Array;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import com.ericsson.oss.air.csac.configuration.JdbcConfig;
import com.ericsson.oss.air.csac.model.KPIDefinition;
import com.ericsson.oss.air.csac.model.KPIReference;
import com.ericsson.oss.air.csac.model.ProfileDefinition;
import com.ericsson.oss.air.csac.repository.DeployedProfileDAO;
import com.ericsson.oss.air.csac.repository.impl.jdbc.mapper.RichProfileDefinitionMapper;
import com.ericsson.oss.air.exception.CsacDAOException;
import com.ericsson.oss.air.util.logging.FaultHandler;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.ArgumentPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils;
import org.springframework.stereotype.Repository;
import org.springframework.util.ObjectUtils;

@Repository
@Slf4j
@Primary
@AllArgsConstructor
@Profile({ "prod", "test" })
public class DeployedProfileDAOJdbcImpl implements DeployedProfileDAO {

    public static final String COLUMN_NAME = "name";

    public static final String COLUMN_DESCRIPTION = "description";

    public static final String COLUMN_PROF_NAME = "prof_name";

    public static final String COLUMN_AGG_FIELDS = "agg_fields";

    public static final String COLUMN_KPI_DEF_NAME = "kpi_def_name";

    public static final String COLUMN_KPI_REFS = "kpi_refs";

    public static final String RUNTIME_PROFILE_TABLE_NAME = "%1$s.rt_profile";

    public static final String RUNTIME_PROFILE_AGGREGATION_TABLE_NAME = "%1$s.rt_prof_agg";

    public static final String RUNTIME_PROFILE_KPI_TABLE_NAME = "%1$s.rt_prof_kpi";

    public static final String INSERT_TO_RT_PROF_NAMED_STATEMENT = "INSERT INTO " + RUNTIME_PROFILE_TABLE_NAME
            + " (name, description)"
            + " VALUES (:name,:description)"
            + " ON CONFLICT (name) DO UPDATE"
            + " SET description = EXCLUDED.description";

    public static final String INSERT_TO_RT_PROF_AGG_NAMED_STATEMENT = "INSERT INTO " + RUNTIME_PROFILE_AGGREGATION_TABLE_NAME
            + " (prof_name, agg_fields)"
            + " VALUES (:prof_name,:agg_fields)"
            + " ON CONFLICT (prof_name) DO UPDATE"
            + " SET agg_fields = EXCLUDED.agg_fields";

    public static final String INSERT_TO_RT_PROF_KPI_NAMED_STATEMENT = "INSERT INTO " + RUNTIME_PROFILE_KPI_TABLE_NAME
            + " (prof_name, kpi_def_name)"
            + " VALUES (:prof_name,:kpi_def_name)"
            + " ON CONFLICT (prof_name, kpi_def_name) DO NOTHING";

    public static final String COUNT_STATEMENT = "SELECT COUNT(*) FROM " + RUNTIME_PROFILE_TABLE_NAME;

    // Rich profile CTE queries
    public static final String RICH_PROFILE_TABLE_NAME = "rich_profile";

    private static final String RICH_PROFILE_CTE = "WITH " + RICH_PROFILE_TABLE_NAME + " AS (SELECT name, description, kpi_refs, agg_fields\n"
            + "                      FROM %1$s.rt_profile profile\n"
            + "                          LEFT JOIN(SELECT prof_name, array_agg(kpi_def_name) AS kpi_refs FROM %1$s.rt_prof_kpi GROUP BY prof_name ) kpi ON profile.name = kpi.prof_name\n"
            + "                          LEFT JOIN %1$s.rt_prof_agg agg ON profile.name = agg.prof_name)\n";

    private static final String SELECT_PROFILE_WITH_OFFSET = RICH_PROFILE_CTE + " SELECT * FROM " + RICH_PROFILE_TABLE_NAME + " OFFSET ? LIMIT ?";

    private static final String SELECT_PROFILE_BY_NAME = RICH_PROFILE_CTE + " SELECT * FROM " + RICH_PROFILE_TABLE_NAME + " WHERE name in (%2$s)";

    private static final String SELECT_PROFILE_BY_KPI_NAME = RICH_PROFILE_CTE + " SELECT * FROM " + RICH_PROFILE_TABLE_NAME
            + " INNER JOIN %1$s.rt_prof_kpi kpi ON rich_profile.name = kpi.prof_name WHERE kpi.kpi_def_name IN (%2$s);";

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Autowired
    private FaultHandler faultHandler;

    @Autowired
    private JdbcConfig jdbcConfig;

    static List<MapSqlParameterSource> getProfileKpiRefsParameterSourceList(
            @NonNull final ProfileDefinition profileDefinition) {
        final List<KPIReference> kpiRefs = profileDefinition.getKpis();

        if (ObjectUtils.isEmpty(kpiRefs)) {
            return new ArrayList<>();
        }

        return kpiRefs
                .stream()
                .map(kpiReference -> new MapSqlParameterSource()
                        .addValue(COLUMN_PROF_NAME, profileDefinition.getName())
                        .addValue(COLUMN_KPI_DEF_NAME, kpiReference.getRef()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void saveProfileDefinition(
            @NonNull final ProfileDefinition profileDefinition) {
        this.insertProfileDefinitions(singletonList(profileDefinition));
    }

    @Override
    public ProfileDefinition findByProfileDefName(final String profileDefName) {
        final Set<ProfileDefinition> profileFetchResult = this.findByProfileDefNames(Set.of(profileDefName));

        if (profileFetchResult.isEmpty()) {
            return null;
        }

        return profileFetchResult.iterator().next();
    }

    @Override
    public List<ProfileDefinition> findAllProfileDefinitions(final Integer start, final Integer rows) {
        final Integer safeStartPoint = getSafeSublistIndex(this.totalProfileDefinitions(), start, rows).getFirst();
        final String pagedSelectStat = String.format(SELECT_PROFILE_WITH_OFFSET, this.jdbcConfig.getRuntimeDatastoreSchemaName());

        return this.jdbcTemplate.query(pagedSelectStat, new RichProfileDefinitionMapper(), safeStartPoint, rows);
    }

    @Override
    public int totalProfileDefinitions() {
        return this.jdbcTemplate.queryForObject(String.format(COUNT_STATEMENT, this.jdbcConfig.getRuntimeDatastoreSchemaName()), Integer.class);
    }

    @Override
    public Set<ProfileDefinition> getProfileDefinitions() {
        return new HashSet<>(this.findAllProfileDefinitions(0, Integer.MAX_VALUE));
    }

    @Override
    @Transactional
    public void insertProfileDefinitions(final List<ProfileDefinition> profileDefinitions) {

        //Insert into rt_profile table
        final SqlParameterSource[] profileDefBatch = SqlParameterSourceUtils.createBatch(profileDefinitions.toArray());
        this.namedParameterJdbcTemplate.batchUpdate(String.format(INSERT_TO_RT_PROF_NAMED_STATEMENT, this.jdbcConfig.getRuntimeDatastoreSchemaName()),
                profileDefBatch);

        //Insert into rt_prof_agg table
        final SqlParameterSource[] profileAggregationBatch = this.getProfileAggregationSqlParameterSourceList(profileDefinitions)
                .toArray(SqlParameterSource[]::new);
        this.namedParameterJdbcTemplate.batchUpdate(
                String.format(INSERT_TO_RT_PROF_AGG_NAMED_STATEMENT, this.jdbcConfig.getRuntimeDatastoreSchemaName()), profileAggregationBatch);

        //Insert into rt_prof_kpi table
        final SqlParameterSource[] profileKpiRefBatch = profileDefinitions.stream()
                .flatMap(profileDefinition -> getProfileKpiRefsParameterSourceList(profileDefinition).stream())
                .toArray(SqlParameterSource[]::new);

        this.namedParameterJdbcTemplate.batchUpdate(
                String.format(INSERT_TO_RT_PROF_KPI_NAMED_STATEMENT, this.jdbcConfig.getRuntimeDatastoreSchemaName()), profileKpiRefBatch);
    }

    @Override
    public boolean isMatched(final ProfileDefinition profileDefinition) {
        final ProfileDefinition matchByProfileDefName = this.findByProfileDefName(profileDefinition.getName());
        return !ObjectUtils.isEmpty(matchByProfileDefName) && matchByProfileDefName.equals(profileDefinition);
    }

    @Override
    public Set<ProfileDefinition> getAffectedProfiles(final List<KPIDefinition> kpiDefinitions) {
        if (ObjectUtils.isEmpty(kpiDefinitions)) {
            return new HashSet<>();
        }

        final String findProfileStat = String.format(SELECT_PROFILE_BY_KPI_NAME,
                this.jdbcConfig.getRuntimeDatastoreSchemaName(),
                kpiDefinitions.stream().map(v -> "?").collect(Collectors.joining(", ")));

        final List<ProfileDefinition> profileDefinitions = this.jdbcTemplate.query(findProfileStat,
                new ArgumentPreparedStatementSetter(kpiDefinitions.stream().map(KPIDefinition::getName).toArray()),
                new RichProfileDefinitionMapper());

        return new HashSet<>(profileDefinitions);

    }

    Set<ProfileDefinition> findByProfileDefNames(final Set<String> profileDefNames) {
        if (ObjectUtils.isEmpty(profileDefNames)) {
            return new HashSet<>();
        }

        final String findByProfileNameStatement = String.format(SELECT_PROFILE_BY_NAME,
                this.jdbcConfig.getRuntimeDatastoreSchemaName(),
                profileDefNames.stream().map(v -> "?").collect(Collectors.joining(", ")));

        final List<ProfileDefinition> profileDefinitions = this.jdbcTemplate.query(findByProfileNameStatement,
                new ArgumentPreparedStatementSetter(profileDefNames.toArray()),
                new RichProfileDefinitionMapper());

        return new HashSet<>(profileDefinitions);
    }

    List<MapSqlParameterSource> getProfileAggregationSqlParameterSourceList(
            @NonNull final List<ProfileDefinition> profileDefinitions) {
        final DataSource datasource = this.jdbcTemplate.getDataSource();

        if (Objects.isNull(datasource)) {
            throw new CsacDAOException("Cannot create datasource.");
        }

        try (final Connection connection = datasource.getConnection()) {
            final List<MapSqlParameterSource> profileAggregationSqlParameterSourceList = new ArrayList<>();

            for (final ProfileDefinition profileDefinition : profileDefinitions) {
                final Array aggregationFields = connection.createArrayOf("VARCHAR", profileDefinition.getContext().toArray());
                final MapSqlParameterSource profileAggregationSqlParameterSource = new MapSqlParameterSource()
                        .addValue(COLUMN_PROF_NAME, profileDefinition.getName())
                        .addValue(COLUMN_AGG_FIELDS, aggregationFields);
                profileAggregationSqlParameterSourceList.add(profileAggregationSqlParameterSource);
            }

            return profileAggregationSqlParameterSourceList;
        } catch (final SQLException sqle) {
            this.faultHandler.fatal(sqle);
            throw new CsacDAOException(sqle);
        }

    }

}
