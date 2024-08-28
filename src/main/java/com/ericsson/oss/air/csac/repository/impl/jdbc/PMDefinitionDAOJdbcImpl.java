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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.ericsson.oss.air.csac.configuration.JdbcConfig;
import com.ericsson.oss.air.csac.model.PMDefinition;
import com.ericsson.oss.air.csac.repository.PMDefinitionDAO;
import com.ericsson.oss.air.csac.repository.impl.jdbc.mapper.PMDefinitionMapper;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.data.util.Pair;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

@Slf4j
@Repository
@Primary
@AllArgsConstructor
@Profile({ "prod", "test" })
public class PMDefinitionDAOJdbcImpl implements PMDefinitionDAO {

    // pm_def table name
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_SOURCE = "source";
    public static final String COLUMN_DESCRIPTION = "description";

    // pm_schema table column
    public static final String COLUMN_SCHEMA = "schema";
    public static final String COLUMN_PM_NAME = "pm_name";

    public static final String COUNT_STATEMENT = "SELECT COUNT(*) FROM %1$s.pm_def";
    public static final String SELECT_ALL_NAMES_STATEMENT = "SELECT name FROM %1$s.pm_def";
    public static final String SELECT_PM_DEF_BY_NAME_STATEMENT = "SELECT * FROM %1$s.pm_def WHERE name = ?";
    public static final String SELECT_BY_OFFSET_AND_LIMIT_STATEMENT = "SELECT * FROM %1$s.pm_def OFFSET %2$d LIMIT %3$d";
    public static final String SELECT_SCHEMA_STATEMENT = "SELECT schema FROM %1$s.pm_schema WHERE pm_name = ? ";

    private static final String INSERT_PM_DEF_NAMED_STATEMENT = "INSERT INTO %1$s.pm_def"
            + "(name, source, description)"
            + " VALUES(:name, :source, :description)"
            + " ON CONFLICT (name) DO UPDATE"
            + " SET source = EXCLUDED.source, "
            + " description = EXCLUDED.description ";

    private static final String INSERT_PM_SCHEMA_NAMED_STATEMENT = "INSERT INTO %1$s.pm_schema"
            + "(schema, pm_name)"
            + " VALUES(:schema, :pm_name)"
            + " ON CONFLICT (pm_name) DO NOTHING";

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Autowired
    private JdbcConfig jdbcConfig;

    /**
     * A helper method to generate a list MapSqlParameterSource
     */
    private static List<MapSqlParameterSource> getPMSqlPsList(
            final Map<String, List<PMDefinition>> schemaPmDefsMap) {

        if (ObjectUtils.isEmpty(schemaPmDefsMap)) {
            return new ArrayList<>();
        }

        final List<MapSqlParameterSource> pmDefSqlList = new ArrayList<>();
        schemaPmDefsMap.forEach((schemaName, pmDefs) -> pmDefs.forEach(pmDef -> {
            final MapSqlParameterSource pSource = pmDefToSqlParameterSource(pmDef);
            pSource.addValue(COLUMN_SCHEMA, schemaName);
            pmDefSqlList.add(pSource);
        }));
        return pmDefSqlList;
    }

    /**
     * A helper method to generate a MapSqlParameterSource
     */
    private static MapSqlParameterSource pmDefToSqlParameterSource(final PMDefinition pmDef) {
        return new MapSqlParameterSource()
                .addValue(COLUMN_PM_NAME, pmDef.getName())
                .addValue(COLUMN_NAME, pmDef.getName())
                .addValue(COLUMN_SOURCE, pmDef.getSource())
                .addValue(COLUMN_DESCRIPTION, pmDef.getDescription());
    }

    @Override
    @Transactional
    public void savePMDefinition(
            @NonNull final PMDefinition pmDefinition,
            @NonNull final String schemaName) {
        final Map<String, List<PMDefinition>> schemaPmDefMap = new HashMap<>();
        schemaPmDefMap.put(schemaName, singletonList(pmDefinition));
        this.insertPMDefinitions(schemaPmDefMap);

    }

    @Override
    public void updatePMDefinition(
            @NonNull final PMDefinition pmDefinition) {
        final MapSqlParameterSource pSource = pmDefToSqlParameterSource(pmDefinition);

        this.namedParameterJdbcTemplate.update(String.format(INSERT_PM_DEF_NAMED_STATEMENT, this.jdbcConfig.getDictionarySchemaName()), pSource);
    }

    @Override
    @Transactional
    public void insertPMDefinitions(final Map<String, List<PMDefinition>> validatedPMDefinitions) {
        final SqlParameterSource[] sqlParaSources = getPMSqlPsList(validatedPMDefinitions).toArray(new SqlParameterSource[] {});
        if (ObjectUtils.isEmpty(sqlParaSources)) {
            return;
        }
        this.namedParameterJdbcTemplate.batchUpdate(String.format(INSERT_PM_DEF_NAMED_STATEMENT, this.jdbcConfig.getDictionarySchemaName()),
                sqlParaSources);
        this.namedParameterJdbcTemplate.batchUpdate(String.format(INSERT_PM_SCHEMA_NAMED_STATEMENT, this.jdbcConfig.getDictionarySchemaName()),
                sqlParaSources);
    }

    @Override
    public String findSchemaByPMDefName(final String pmDefName) {
        return DataAccessUtils.singleResult(this.jdbcTemplate.queryForList(
                String.format(SELECT_SCHEMA_STATEMENT, this.jdbcConfig.getDictionarySchemaName()), String.class, pmDefName));
    }

    @Override
    public PMDefinition findByPMDefName(final String pmDefName) {
        final List<PMDefinition> pmDefsByName = this.jdbcTemplate.query(
                String.format(SELECT_PM_DEF_BY_NAME_STATEMENT, this.jdbcConfig.getDictionarySchemaName()), new PMDefinitionMapper(), pmDefName);
        if (ObjectUtils.isEmpty(pmDefsByName)) {
            return null;
        }
        return pmDefsByName.get(0);
    }

    @Override
    public List<PMDefinition> findAllPMDefinitions(final Integer start, final Integer rows) {
        final Integer total = this.totalPMDefinitions();
        final Pair<Integer, Integer> safeIndex = getSafeSublistIndex(total, start, rows);
        final String sql = String.format(SELECT_BY_OFFSET_AND_LIMIT_STATEMENT, this.jdbcConfig.getDictionarySchemaName(), safeIndex.getFirst(), rows);
        return this.jdbcTemplate.query(sql, new PMDefinitionMapper());
    }

    @Override
    public Set<String> getAllPmDefNames() {
        final List<String> strings = this.jdbcTemplate.queryForList(
                String.format(SELECT_ALL_NAMES_STATEMENT, this.jdbcConfig.getDictionarySchemaName()), String.class);
        if (ObjectUtils.isEmpty(strings)) {
            return new HashSet<>();
        }
        return new HashSet<>(strings);
    }

    @Override
    public Integer totalPMDefinitions() {
        return this.jdbcTemplate.queryForObject(String.format(COUNT_STATEMENT, this.jdbcConfig.getDictionarySchemaName()), Integer.class);
    }

}
