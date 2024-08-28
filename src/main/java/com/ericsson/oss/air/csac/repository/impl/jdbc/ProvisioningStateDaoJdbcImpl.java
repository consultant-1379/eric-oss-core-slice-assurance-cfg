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

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.ericsson.oss.air.csac.configuration.JdbcConfig;
import com.ericsson.oss.air.csac.handler.validation.ValidationHandler;
import com.ericsson.oss.air.csac.model.runtime.ProvisioningState;
import com.ericsson.oss.air.csac.repository.ProvisioningStateDao;
import com.ericsson.oss.air.csac.repository.impl.jdbc.mapper.ProvisioningStateRowMapper;
import com.ericsson.oss.air.exception.CsacDAOException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * JDBC implementation of the {@link com.ericsson.oss.air.csac.repository.ProvisioningStateDao} API.
 */
@Slf4j
@Repository
@Primary
@AllArgsConstructor
@Profile({ "prod", "test" })
public class ProvisioningStateDaoJdbcImpl implements ProvisioningStateDao {

    public static final String COLUMN_PK = "id";

    public static final String COLUMN_PROV_START_TIME = "provisioning_start_time";

    public static final String COLUMN_PROV_END_TIME = "provisioning_end_time";

    public static final String COLUMN_PROV_STATE = "provisioning_state";

    public static final String WHERE = " WHERE ";

    public static final String ORDER_BY_ID = " ORDER BY " + COLUMN_PK;

    public static final String TABLE_RT_PROV_STATE = "%1$s.rt_prov_state";

    public static final String SQL_SELECT_LATEST_ID = "SELECT MAX(" + COLUMN_PK + ") FROM " + TABLE_RT_PROV_STATE;

    public static final String SQL_SELECT_PROV_STATE = "SELECT * FROM " + TABLE_RT_PROV_STATE;
    public static final String SQL_SELECT_LATEST_PROV_STATE = SQL_SELECT_PROV_STATE
            + ORDER_BY_ID + " DESC LIMIT 1";

    public static final String SQL_SELECT_BY_ID = SQL_SELECT_PROV_STATE
            + WHERE + COLUMN_PK + " = %2$s";

    public static final String SQL_SELECT_ID_IN = SQL_SELECT_PROV_STATE
            + WHERE + COLUMN_PK + " IN (%2$s)";

    public static final String SQL_INSERT_NEW_PROV_STATE = "INSERT INTO " + TABLE_RT_PROV_STATE
            + "(" + COLUMN_PROV_START_TIME + "," + COLUMN_PROV_END_TIME + "," + COLUMN_PROV_STATE + ") "
            + "SELECT current_timestamp, current_timestamp, '%2$s'";

    public static final String SQL_UPDATE_PROV_STATE = "UPDATE " + TABLE_RT_PROV_STATE
            + " SET " + COLUMN_PROV_END_TIME + " = current_timestamp, " + COLUMN_PROV_STATE + " = '%2$s' "
            + "WHERE " + COLUMN_PK + " = (" + SQL_SELECT_LATEST_ID + ")";

    public static final String SQL_COUNT_PROV_STATES = "SELECT COUNT(*) FROM " + TABLE_RT_PROV_STATE;

    public static final String SQL_COUNT_PROV_STATES_BY_ID = SQL_COUNT_PROV_STATES + WHERE + COLUMN_PK + " = %2$s";

    private JdbcTemplate jdbcTemplate;

    private ValidationHandler validationHandler;

    private JdbcConfig jdbcConfig;

    @Override
    public <S extends ProvisioningState> S save(final S entity) {

        this.validationHandler.checkEntity(entity);

        try {
            final ProvisioningState latest = findLatest();

            latest.getProvisioningState().checkStateTransition(entity.getProvisioningState());

            final String sql = latest.getProvisioningState() == ProvisioningState.State.STARTED
                    ? String.format(SQL_UPDATE_PROV_STATE, this.jdbcConfig.getRuntimeDatastoreSchemaName(), entity.getProvisioningState().name())
                    : String.format(SQL_INSERT_NEW_PROV_STATE, this.jdbcConfig.getRuntimeDatastoreSchemaName(), entity.getProvisioningState().name());

            this.jdbcTemplate.update(sql);

            return (S) findLatest();

        } catch (final DataAccessException e) {
            throw new CsacDAOException(e);
        }
    }

    @Override
    public Optional<ProvisioningState> findById(final Integer id) {

        final String sql = String.format(SQL_SELECT_BY_ID, this.jdbcConfig.getRuntimeDatastoreSchemaName(), id);

        try {
            final List<ProvisioningState> obj = this.jdbcTemplate.query(sql, new ProvisioningStateRowMapper());

            return obj.isEmpty()
                    ? Optional.empty()
                    : Optional.of(obj.get(0));

        } catch (final DataAccessException e) {
            throw new CsacDAOException(e);
        }
    }

    @Override
    public boolean existsById(final Integer id) {

        final String sql = String.format(SQL_COUNT_PROV_STATES_BY_ID, this.jdbcConfig.getRuntimeDatastoreSchemaName(), id);

        try {

            final Integer count = this.jdbcTemplate.queryForObject(sql, Integer.class);

            Objects.requireNonNull(count);

            return count > 0;

        } catch (final DataAccessException e) {
            throw new CsacDAOException(e);
        }
    }

    @Override
    public Iterable<ProvisioningState> findAll() {

        final String sql = String.format(SQL_SELECT_PROV_STATE, this.jdbcConfig.getRuntimeDatastoreSchemaName());

        try {
            return this.jdbcTemplate.query(sql, new ProvisioningStateRowMapper());
        } catch (final DataAccessException e) {
            throw new CsacDAOException(e);
        }
    }

    @Override
    public Iterable<ProvisioningState> findAllById(final Iterable<Integer> ids) {

        final List<Integer> idList = new ArrayList<>();

        ids.forEach(idList::add);
        final String inParams = String.join(",", Collections.nCopies(idList.size(), "?"));

        final String sql = String.format(SQL_SELECT_ID_IN, this.jdbcConfig.getRuntimeDatastoreSchemaName(), inParams);

        try {
            return this.jdbcTemplate.query(sql, new ProvisioningStateRowMapper(), idList.toArray());
        } catch (final DataAccessException e) {
            throw new CsacDAOException(e);
        }
    }

    @Override
    public long count() {

        try {

            final Long count = this.jdbcTemplate.queryForObject(String.format(SQL_COUNT_PROV_STATES, this.jdbcConfig.getRuntimeDatastoreSchemaName()),
                    Long.class);

            Objects.requireNonNull(count);

            return count;

        } catch (final DataAccessException e) {
            throw new CsacDAOException(e);
        }
    }

    @Override
    public ProvisioningState findLatest() {

        final String sql = String.format(SQL_SELECT_LATEST_PROV_STATE, this.jdbcConfig.getRuntimeDatastoreSchemaName());

        try {
            final List<ProvisioningState> latestAsList = this.jdbcTemplate.query(sql, new ProvisioningStateRowMapper());

            return latestAsList.isEmpty()
                    ? getDummyInitialState()
                    : latestAsList.get(0);

        } catch (final DataAccessException e) {
            throw new CsacDAOException(e);
        }
    }

    private ProvisioningState getDummyInitialState() {
        return ProvisioningState.builder()
                .withProvisioningState(ProvisioningState.State.INITIAL)
                .withId(1)
                .withProvisioningStartTime(Instant.now())
                .withProvisioningEndTime(Instant.now())
                .build();
    }
}
