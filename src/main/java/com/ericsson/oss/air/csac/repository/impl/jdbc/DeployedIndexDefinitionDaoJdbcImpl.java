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

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.ericsson.oss.air.csac.configuration.JdbcConfig;
import com.ericsson.oss.air.csac.handler.validation.ValidationHandler;
import com.ericsson.oss.air.csac.model.runtime.index.DeployedIndexDefinitionDto;
import com.ericsson.oss.air.csac.repository.DeployedIndexDefinitionDao;
import com.ericsson.oss.air.csac.repository.impl.jdbc.mapper.DeployedIndexDefinitionMapper;
import com.ericsson.oss.air.exception.CsacDAOException;
import com.ericsson.oss.air.util.codec.Codec;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.stereotype.Repository;

/**
 * This class provides the JDBC implementation of the {@link DeployedIndexDefinitionDao}. All operations will be executed using live JDBC data base
 * connections.
 */
@Repository
@Primary
@AllArgsConstructor
@Profile({ "prod", "test" })
public class DeployedIndexDefinitionDaoJdbcImpl implements DeployedIndexDefinitionDao {

    /**
     * Deployed index name column.
     */
    public static final String COLUMN_IDX_NAME = "idx_name";

    /**
     * Deployed index definition column.
     */
    public static final String COLUMN_IDX_DEF = "idx_def";

    /**
     * Deployed index definition table.
     */
    public static final String TABLE_RT_IDX_DEF = "%1$s.rt_idx_def";

    // SQL statements
    public static final String INSERT_RT_IDX_DEF_SQL = "INSERT INTO " + TABLE_RT_IDX_DEF
            + " (" + COLUMN_IDX_NAME + "," + COLUMN_IDX_DEF + ")"
            + " VALUES(?, to_json(?::json))"
            + " ON CONFLICT (" + COLUMN_IDX_NAME + ") DO UPDATE"
            + " SET " + COLUMN_IDX_DEF + " = EXCLUDED." + COLUMN_IDX_DEF;

    public static final String SELECT_RT_IDX_DEF_SQL = "SELECT " + COLUMN_IDX_DEF + " FROM " + TABLE_RT_IDX_DEF;

    public static final String WHERE = " WHERE ";

    public static final String SELECT_RT_IDX_DEF_BY_ID_SQL = SELECT_RT_IDX_DEF_SQL + WHERE + COLUMN_IDX_NAME + " = '%2$s'";

    public static final String SELECT_RT_IDX_DEF_IN_IDS_SQL = SELECT_RT_IDX_DEF_SQL + WHERE + COLUMN_IDX_NAME + " IN (%2$s)";

    public static final String COUNT_RT_IDX_DEF_SQL = "SELECT COUNT(*) FROM " + TABLE_RT_IDX_DEF;

    public static final String COUNT_RT_IDX_DEF_BY_ID_SQL = COUNT_RT_IDX_DEF_SQL + WHERE + COLUMN_IDX_NAME + " = '%2$s'";

    public static final String DELETE_RT_IDX_DEF_SQL = "DELETE FROM " + TABLE_RT_IDX_DEF;

    public static final String DELETE_RT_IDX_DEF_BY_ID_SQL = DELETE_RT_IDX_DEF_SQL + WHERE + COLUMN_IDX_NAME + " = ?";

    public static final String DELETE_RT_IDX_DEF_IN_IDS_SQL = DELETE_RT_IDX_DEF_SQL + WHERE
            + COLUMN_IDX_NAME + " IN (%2$s)";

    private JdbcTemplate jdbcTemplate;

    private Codec codec;

    private ValidationHandler validationHandler;

    private JdbcConfig jdbcConfig;

    @Override
    public <S extends DeployedIndexDefinitionDto> S save(final S entity) {

        this.validationHandler.checkEntity(entity);

        try {

            final String idxName = entity.indexDefinitionName();
            final String idxDef = this.codec.writeValueAsString(entity);

            final PreparedStatementSetter statementSetter = this.getSaveStatementSetter(idxName, idxDef);
            this.jdbcTemplate.update(String.format(INSERT_RT_IDX_DEF_SQL, this.jdbcConfig.getRuntimeDatastoreSchemaName()), statementSetter);

            return entity;

        } catch (final DataAccessException | JsonProcessingException e) {
            throw new CsacDAOException(e);
        }
    }

    /*
     * (non-javadoc)
     *
     * Returns a PreparedStatementSetter for the save() operation.
     */
    protected PreparedStatementSetter getSaveStatementSetter(final String idxName, final String idxDef) {
        return new PreparedStatementSetter() {

            @Override
            public void setValues(final PreparedStatement ps) throws SQLException {
                ps.setString(1, idxName);
                ps.setString(2, idxDef);
            }
        };
    }

    @Override
    public <S extends DeployedIndexDefinitionDto> Iterable<S> saveAll(final Iterable<S> entities) {

        for (final S entity : entities) {
            this.validationHandler.checkEntity(entity);
        }

        try {

            // collect the entity defs to two arrays
            final List<String> idxNameList = new ArrayList<>();
            final List<String> idxDefList = new ArrayList<>();

            for (final S entity : entities) {
                idxNameList.add(entity.indexDefinitionName());
                idxDefList.add(this.codec.writeValueAsString(entity));
            }

            final BatchPreparedStatementSetter statementSetter = this.getSaveAllStatementSetter(idxNameList, idxDefList);
            this.jdbcTemplate.batchUpdate(String.format(INSERT_RT_IDX_DEF_SQL, this.jdbcConfig.getRuntimeDatastoreSchemaName()), statementSetter);

            return entities;

        } catch (final DataAccessException | JsonProcessingException e) {
            throw new CsacDAOException(e);
        }
    }

    /*
     * (non-javadoc)
     *
     * Return a BatchPreparedStatementSetter for the saveAll operation.
     */
    protected BatchPreparedStatementSetter getSaveAllStatementSetter(final List<String> idxNameList, final List<String> idxDefList) {
        return new BatchPreparedStatementSetter() {

            @Override
            public void setValues(final PreparedStatement ps, final int i) throws SQLException {
                ps.setString(1, idxNameList.get(i));
                ps.setString(2, idxDefList.get(i));
            }

            @Override
            public int getBatchSize() {
                return idxNameList.size();
            }
        };
    }

    @Override
    public Optional<DeployedIndexDefinitionDto> findById(final String id) {

        try {

            final String sql = String.format(SELECT_RT_IDX_DEF_BY_ID_SQL, this.jdbcConfig.getRuntimeDatastoreSchemaName(), id);
            final List<DeployedIndexDefinitionDto> obj = this.jdbcTemplate.query(sql, new DeployedIndexDefinitionMapper());

            if (obj.isEmpty()) {
                return Optional.empty();
            }

            return Optional.of(obj.get(0));

        } catch (final DataAccessException e) {
            throw new CsacDAOException(e);
        }
    }

    @Override
    public boolean existsById(final String id) {

        try {
            final String sql = String.format(COUNT_RT_IDX_DEF_BY_ID_SQL, this.jdbcConfig.getRuntimeDatastoreSchemaName(), id);
            final Integer count = this.jdbcTemplate.queryForObject(sql, Integer.class);

            Objects.requireNonNull(count);

            return count > 0;

        } catch (final DataAccessException e) {
            throw new CsacDAOException(e);
        }
    }

    @Override
    public Iterable<DeployedIndexDefinitionDto> findAll() {

        try {
            return this.jdbcTemplate.query(String.format(SELECT_RT_IDX_DEF_SQL, this.jdbcConfig.getRuntimeDatastoreSchemaName()),
                    new DeployedIndexDefinitionMapper());
        } catch (final DataAccessException e) {
            throw new CsacDAOException(e);
        }
    }

    @Override
    public Iterable<DeployedIndexDefinitionDto> findAllById(final Iterable<String> ids) {

        try {

            final List<String> idList = new ArrayList<>();

            ids.forEach(idList::add);

            final String inParams = String.join(",", Collections.nCopies(idList.size(), "?"));

            final String sql = String.format(SELECT_RT_IDX_DEF_IN_IDS_SQL, this.jdbcConfig.getRuntimeDatastoreSchemaName(), inParams);

            return this.jdbcTemplate.query(sql, new DeployedIndexDefinitionMapper(), idList.toArray());

        } catch (final DataAccessException e) {
            throw new CsacDAOException(e);
        }
    }

    @Override
    public long count() {

        try {

            final Long count = this.jdbcTemplate.queryForObject(String.format(COUNT_RT_IDX_DEF_SQL, this.jdbcConfig.getRuntimeDatastoreSchemaName()),
                    Long.class);

            Objects.requireNonNull(count);

            return count.longValue();

        } catch (final DataAccessException e) {
            throw new CsacDAOException(e);
        }
    }

    @Override
    public void deleteById(final String id) {

        try {

            this.jdbcTemplate.update(String.format(DELETE_RT_IDX_DEF_BY_ID_SQL, this.jdbcConfig.getRuntimeDatastoreSchemaName()), id);

        } catch (final DataAccessException e) {
            throw new CsacDAOException(e);
        }
    }

    @Override
    public void delete(final DeployedIndexDefinitionDto entity) {

        if (Objects.nonNull(entity)) {
            this.deleteById(entity.indexDefinitionName());
        }
    }

    @Override
    public void deleteAllById(final Iterable<? extends String> ids) {

        try {

            final List<String> idList = new ArrayList<>();

            ids.forEach(idList::add);

            final String inParams = String.join(",", Collections.nCopies(idList.size(), "?"));

            final String sql = String.format(DELETE_RT_IDX_DEF_IN_IDS_SQL, this.jdbcConfig.getRuntimeDatastoreSchemaName(), inParams);

            this.jdbcTemplate.update(sql, idList.toArray());

        } catch (final DataAccessException e) {
            throw new CsacDAOException(e);
        }
    }

    @Override
    public void deleteAll(final Iterable<? extends DeployedIndexDefinitionDto> entities) {

        final List<String> idList = StreamSupport.stream(entities.spliterator(), false).map(DeployedIndexDefinitionDto::indexDefinitionName)
                .toList();

        this.deleteAllById(idList);

    }

    @Override
    public void deleteAll() {

        try {

            this.jdbcTemplate.update(String.format(DELETE_RT_IDX_DEF_SQL, this.jdbcConfig.getRuntimeDatastoreSchemaName()));

        } catch (final DataAccessException e) {
            throw new CsacDAOException(e);
        }
    }

    @Override
    public <S extends DeployedIndexDefinitionDto> Stream<S> stream() {
        return (Stream<S>) StreamSupport.stream(this.findAll().spliterator(), false);
    }

}
