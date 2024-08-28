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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.ericsson.oss.air.csac.configuration.JdbcConfig;
import com.ericsson.oss.air.csac.model.ProfileDefinition;
import com.ericsson.oss.air.csac.repository.ProfileDefinitionDAO;
import com.ericsson.oss.air.csac.repository.impl.jdbc.mapper.ProfileDefinitionMapper;
import com.ericsson.oss.air.exception.CsacDAOException;
import com.ericsson.oss.air.util.logging.FaultHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

/**
 * JDBC implementation of the {@link ProfileDefinitionDAO}.
 */
@Slf4j
@Repository
@Primary
@AllArgsConstructor
@Profile({ "prod", "test" })
public class ProfileDefinitionDAOJdbcImpl implements ProfileDefinitionDAO {

    private static final String COLUMN_NAME = "name";

    public static final String COLUMN_DEF = "def";

    private static final String TABLE_PROF_DEF = "%1$s.prof_def";

    private static final String INSERT_PROFILE_DEF_STATEMENT = "INSERT INTO " + TABLE_PROF_DEF
            + "(name, def)"
            + " VALUES(:name, to_json(:def::json))"
            + " ON CONFLICT (name) DO UPDATE"
            + " SET def = EXCLUDED.def";

    private static final String SELECT_PROFILE_DEF_STATEMENT = "SELECT " + COLUMN_DEF + " FROM " + TABLE_PROF_DEF
            + " WHERE " + COLUMN_NAME + " = :name";

    protected static final String COUNT_STATEMENT = "SELECT COUNT(*) FROM " + TABLE_PROF_DEF;

    protected static final String SELECT_ALL_PROFILE_DEF_STATEMENT = "SELECT " + COLUMN_DEF + " FROM " + TABLE_PROF_DEF;

    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    private JdbcTemplate jdbcTemplate;

    private ObjectMapper mapper;

    private FaultHandler faultHandler;

    private JdbcConfig jdbcConfig;

    @Transactional
    @Override
    public void save(
            @NonNull final ProfileDefinition profileDefinition) {
        this.saveAll(List.of(profileDefinition));
    }

    @Transactional
    @Override
    public void saveAll(
            @NonNull final List<ProfileDefinition> profileDefinitionList) {

        try {
            final List<MapSqlParameterSource> sqlParameterSourceList = new ArrayList<>();

            for (final ProfileDefinition profileDefinition : profileDefinitionList) {

                final String profileDefString = this.mapper.writeValueAsString(profileDefinition);
                final MapSqlParameterSource augmentationSqlParameterSource = new MapSqlParameterSource()
                        .addValue(COLUMN_NAME, profileDefinition.getName())
                        .addValue(COLUMN_DEF, profileDefString);

                sqlParameterSourceList.add(augmentationSqlParameterSource);
            }

            this.namedParameterJdbcTemplate.batchUpdate(String.format(INSERT_PROFILE_DEF_STATEMENT, this.jdbcConfig.getDictionarySchemaName()),
                    sqlParameterSourceList.toArray(SqlParameterSource[]::new));

        } catch (final JsonProcessingException jpe) {
            this.faultHandler.error(jpe);
            throw new CsacDAOException(jpe);
        }
    }

    @Override
    public Optional<ProfileDefinition> findById(final String profileId) {

        try {
            final SqlParameterSource namedParameter = new MapSqlParameterSource()
                    .addValue("name", profileId);

            final List<ProfileDefinition> rs = this.namedParameterJdbcTemplate.query(
                    String.format(SELECT_PROFILE_DEF_STATEMENT, this.jdbcConfig.getDictionarySchemaName()),
                    namedParameter,
                    new ProfileDefinitionMapper());

            if (ObjectUtils.isEmpty(rs)) {
                return Optional.empty();
            }

            return Optional.of(rs.get(0));
        } catch (final DataAccessException e) {
            throw new CsacDAOException(e);
        }
    }

    @Override
    public List<ProfileDefinition> findAll() {
        return this.jdbcTemplate.query(String.format(SELECT_ALL_PROFILE_DEF_STATEMENT, this.jdbcConfig.getDictionarySchemaName()),
                new ProfileDefinitionMapper());
    }

    @Override
    public int totalProfileDefinitions() {
        return this.jdbcTemplate.queryForObject(String.format(COUNT_STATEMENT, this.jdbcConfig.getDictionarySchemaName()), Integer.class);
    }
}
