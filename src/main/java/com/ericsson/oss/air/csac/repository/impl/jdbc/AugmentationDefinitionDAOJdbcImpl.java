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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.ericsson.oss.air.csac.configuration.JdbcConfig;
import com.ericsson.oss.air.csac.model.AugmentationDefinition;
import com.ericsson.oss.air.csac.repository.AugmentationDefinitionDAO;
import com.ericsson.oss.air.csac.repository.impl.jdbc.mapper.AugmentationDefinitionMapper;
import com.ericsson.oss.air.exception.CsacDAOException;
import com.ericsson.oss.air.util.logging.FaultHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.DataAccessException;
import org.springframework.data.util.Pair;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * {@inheritDoc} JDBC implementation of the {@link AugmentationDefinitionDAO}.
 */
@Slf4j
@Repository
@Primary
@AllArgsConstructor
@Profile({ "prod", "test" })
public class AugmentationDefinitionDAOJdbcImpl implements AugmentationDefinitionDAO {

    public static final String COLUMN_DEF = "def";

    private static final String COLUMN_NAME = "name";

    private static final String TABLE_DICT_AUG_DEF = "%1$s.aug_def";

    public static final String SELECT_BY_OFFSET_AND_LIMIT_STATEMENT = "SELECT * FROM " + TABLE_DICT_AUG_DEF + " OFFSET %2$d LIMIT %3$d";

    public static final String SELECT_ALL_AUG_DEF_STATEMENT = "SELECT " + COLUMN_DEF + " FROM " + TABLE_DICT_AUG_DEF;

    protected static final String DELETE_AUG_DEF_STATEMENT = "DELETE FROM " + TABLE_DICT_AUG_DEF
            + " WHERE " + COLUMN_NAME + " = ?";

    protected static final String AUG_COUNT_STATEMENT = "SELECT COUNT(*) FROM " + TABLE_DICT_AUG_DEF;

    private static final String INSERT_AUG_DEF_NAMED_STATEMENT = "INSERT INTO " + TABLE_DICT_AUG_DEF
            + "(name, def)"
            + " VALUES(:name, to_json(:def::json))"
            + " ON CONFLICT (name) DO UPDATE"
            + " SET def = EXCLUDED.def";

    private static final String SELECT_AUG_DEF_STATEMENT = "SELECT " + COLUMN_DEF + " FROM " + TABLE_DICT_AUG_DEF
            + " WHERE name = :name";

    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private FaultHandler faultHandler;

    private JdbcConfig jdbcConfig;

    @Override
    @Transactional
    public void save(
            @NonNull final AugmentationDefinition augmentationDefinition) {

        this.saveAll(singletonList(augmentationDefinition));
    }

    @Override
    @Transactional
    public void saveAll(
            @NonNull final List<AugmentationDefinition> augmentationDefinitionList) {

        try {
            final List<MapSqlParameterSource> augmentationSqlParameterSourceList = new ArrayList<>();

            for (final AugmentationDefinition augmentationDefinition : augmentationDefinitionList) {

                final String augmentationDefString = this.mapper.writeValueAsString(augmentationDefinition);
                final MapSqlParameterSource augmentationSqlParameterSource = new MapSqlParameterSource()
                        .addValue(COLUMN_NAME, augmentationDefinition.getName())
                        .addValue(COLUMN_DEF, augmentationDefString);

                augmentationSqlParameterSourceList.add(augmentationSqlParameterSource);
            }

            this.namedParameterJdbcTemplate.batchUpdate(String.format(INSERT_AUG_DEF_NAMED_STATEMENT, this.jdbcConfig.getDictionarySchemaName()),
                    augmentationSqlParameterSourceList.toArray(SqlParameterSource[]::new));

        } catch (final JsonProcessingException jpe) {
            this.faultHandler.error(jpe);
            throw new CsacDAOException(jpe);
        }
    }

    @Override
    public List<AugmentationDefinition> findAll() {
        return this.jdbcTemplate.query(String.format(SELECT_ALL_AUG_DEF_STATEMENT, this.jdbcConfig.getDictionarySchemaName()),
                new AugmentationDefinitionMapper());
    }

    @Override
    public List<AugmentationDefinition> findAll(final Integer start, final Integer rows) {
        final Integer total = this.totalAugmentationDefinitions();
        final Pair<Integer, Integer> safeIndex = getSafeSublistIndex(total, start, rows);
        final String sql = String.format(SELECT_BY_OFFSET_AND_LIMIT_STATEMENT,
                this.jdbcConfig.getDictionarySchemaName(),
                safeIndex.getFirst(),
                rows);

        return this.jdbcTemplate.query(sql, new AugmentationDefinitionMapper());
    }

    @Override
    public Optional<AugmentationDefinition> findById(final String ardqId) {

        try {
            final SqlParameterSource namedParameter = new MapSqlParameterSource()
                    .addValue("name", ardqId);

            final List<AugmentationDefinition> rs = this.namedParameterJdbcTemplate.query(
                    String.format(SELECT_AUG_DEF_STATEMENT, this.jdbcConfig.getDictionarySchemaName()),
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
        this.jdbcTemplate.update(String.format(DELETE_AUG_DEF_STATEMENT, this.jdbcConfig.getDictionarySchemaName()), ardqId);
    }

    @Override
    public int totalAugmentationDefinitions() {
        return this.jdbcTemplate.queryForObject(String.format(AUG_COUNT_STATEMENT, this.jdbcConfig.getDictionarySchemaName()), Integer.class);
    }
}
