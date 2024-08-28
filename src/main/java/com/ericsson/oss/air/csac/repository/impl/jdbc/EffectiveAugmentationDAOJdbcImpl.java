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

import java.util.List;
import java.util.Optional;

import com.ericsson.oss.air.csac.configuration.JdbcConfig;
import com.ericsson.oss.air.csac.model.AugmentationDefinition;
import com.ericsson.oss.air.csac.repository.EffectiveAugmentationDAO;
import com.ericsson.oss.air.csac.repository.impl.EffectiveAugmentationDAOBase;
import com.ericsson.oss.air.csac.repository.impl.jdbc.mapper.AugmentationDefinitionMapper;
import com.ericsson.oss.air.exception.CsacDAOException;
import com.ericsson.oss.air.util.logging.FaultHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * JDBC implementation of the {@link EffectiveAugmentationDAO}.
 */
@Slf4j
@Repository
@Primary
@AllArgsConstructor
@Profile({ "prod", "test" })
public class EffectiveAugmentationDAOJdbcImpl extends EffectiveAugmentationDAOBase {

    private static final String COLUMN_NAME = "name";

    private static final String COLUMN_DEF = "def";

    private static final String COLUMN_PROF_NAME = "prof_name";

    private static final String COLUMN_AUG_NAME = "aug_name";

    private static final String TABLE_RT_AUG_DEF = "%1$s.rt_aug";

    private static final String TABLE_RT_PROF_AUG = "%1$s.rt_prof_aug";

    private static final String INSERT_EFFECTIVE_AUG_DEF_NAMED_STATEMENT = "INSERT INTO " + TABLE_RT_AUG_DEF
            + "(name, def)"
            + " VALUES(:name, to_json(:def::json))"
            + " ON CONFLICT (name) DO UPDATE"
            + " SET def = EXCLUDED.def";

    private static final String INSERT_AFFECTED_PROFILE_NAMED_STATEMENT = "INSERT INTO " + TABLE_RT_PROF_AUG
            + "(prof_name, aug_name)"
            + " VALUES(:prof_name, :aug_name)"
            + " ON CONFLICT (prof_name) DO UPDATE"
            + " SET aug_name = EXCLUDED.aug_name";

    private static final String SELECT_EFFECTIVE_AUG_DEF_STATEMENT = "SELECT " + COLUMN_DEF + " FROM " + TABLE_RT_AUG_DEF
            + " WHERE " + COLUMN_NAME + " = :name";

    public static final String SELECT_ALL_EFFECTIVE_AUG_DEF_STATEMENT = "SELECT " + COLUMN_DEF + " FROM " + TABLE_RT_AUG_DEF;

    protected static final String DELETE_EFFECTIVE_AUG_DEF_STATEMENT = "DELETE FROM " + TABLE_RT_AUG_DEF
            + " WHERE " + COLUMN_NAME + " = ?";

    private static final String SELECT_EFFECTED_PROFILES = "SELECT " + COLUMN_PROF_NAME + " FROM " + TABLE_RT_PROF_AUG
            + " WHERE " + COLUMN_AUG_NAME + " = :name";

    protected static final String COUNT_STATEMENT = "SELECT COUNT(*) FROM " + TABLE_RT_AUG_DEF;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private FaultHandler faultHandler;

    @Autowired
    private JdbcConfig jdbcConfig;

    @Override
    @Transactional
    public void doSave(final AugmentationDefinition augmentationDefinition, final List<String> affectedProfiles) {

        final String augmentationName = augmentationDefinition.getName();

        try {
            final String augmentationDefString = this.mapper.writeValueAsString(augmentationDefinition);

            final SqlParameterSource namedParameters = new MapSqlParameterSource()
                    .addValue(COLUMN_NAME, augmentationDefinition.getName())
                    .addValue(COLUMN_DEF, augmentationDefString);

            this.namedParameterJdbcTemplate.update(
                    String.format(INSERT_EFFECTIVE_AUG_DEF_NAMED_STATEMENT, this.jdbcConfig.getRuntimeDatastoreSchemaName()), namedParameters);

        } catch (final JsonProcessingException jpe) {
            this.faultHandler.error(jpe);
            throw new CsacDAOException(jpe);
        }

        for (final String affectedProfile : affectedProfiles) {

            final SqlParameterSource namedParameters = new MapSqlParameterSource()
                    .addValue(COLUMN_PROF_NAME, affectedProfile)
                    .addValue(COLUMN_AUG_NAME, augmentationName);

            this.namedParameterJdbcTemplate.update(
                    String.format(INSERT_AFFECTED_PROFILE_NAMED_STATEMENT, this.jdbcConfig.getRuntimeDatastoreSchemaName()), namedParameters);

        }
    }

    @Override
    public List<AugmentationDefinition> findAll() {
        return this.jdbcTemplate.query(String.format(SELECT_ALL_EFFECTIVE_AUG_DEF_STATEMENT, this.jdbcConfig.getRuntimeDatastoreSchemaName()),
                new AugmentationDefinitionMapper());
    }

    @Override
    public Optional<AugmentationDefinition> findById(final String ardqId) {

        try {
            final SqlParameterSource namedParameter = new MapSqlParameterSource()
                    .addValue("name", ardqId);

            final List<AugmentationDefinition> rs = this.namedParameterJdbcTemplate.query(
                    String.format(SELECT_EFFECTIVE_AUG_DEF_STATEMENT, this.jdbcConfig.getRuntimeDatastoreSchemaName()),
                    namedParameter,
                    new AugmentationDefinitionMapper());

            if (rs.isEmpty()) {
                return Optional.empty();
            }

            return Optional.of(rs.get(0));
        } catch (final DataAccessException e) {
            throw new CsacDAOException(e);
        }
    }

    @Override
    public void delete(final String ardqId) {
        this.jdbcTemplate.update(String.format(DELETE_EFFECTIVE_AUG_DEF_STATEMENT, this.jdbcConfig.getRuntimeDatastoreSchemaName()), ardqId);
    }

    @Override
    public List<String> findAllProfileNames(final String ardqId) {

        final SqlParameterSource namedParameter = new MapSqlParameterSource().addValue("name", ardqId);

        log.info("Find profile names SQL {} for {}", String.format(SELECT_EFFECTED_PROFILES, this.jdbcConfig.getRuntimeDatastoreSchemaName()),
                ardqId);

        return this.namedParameterJdbcTemplate.queryForList(
                String.format(SELECT_EFFECTED_PROFILES, this.jdbcConfig.getRuntimeDatastoreSchemaName()),
                namedParameter,
                String.class);
    }

    @Override
    public int totalEffectiveAugmentations() {
        return this.jdbcTemplate.queryForObject(String.format(COUNT_STATEMENT, this.jdbcConfig.getRuntimeDatastoreSchemaName()), Integer.class);
    }
}
