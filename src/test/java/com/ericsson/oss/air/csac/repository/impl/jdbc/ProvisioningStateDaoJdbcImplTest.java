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

import static com.ericsson.oss.air.csac.repository.impl.jdbc.ProvisioningStateDaoJdbcImpl.SQL_COUNT_PROV_STATES;
import static com.ericsson.oss.air.csac.repository.impl.jdbc.ProvisioningStateDaoJdbcImpl.SQL_COUNT_PROV_STATES_BY_ID;
import static com.ericsson.oss.air.csac.repository.impl.jdbc.ProvisioningStateDaoJdbcImpl.SQL_SELECT_ID_IN;
import static com.ericsson.oss.air.csac.repository.impl.jdbc.ProvisioningStateDaoJdbcImpl.SQL_SELECT_LATEST_PROV_STATE;
import static com.ericsson.oss.air.csac.repository.impl.jdbc.ProvisioningStateDaoJdbcImpl.SQL_SELECT_PROV_STATE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

import com.ericsson.oss.air.csac.configuration.JdbcConfig;
import com.ericsson.oss.air.csac.handler.validation.ValidationHandler;
import com.ericsson.oss.air.csac.model.runtime.ProvisioningState;
import com.ericsson.oss.air.csac.repository.impl.jdbc.mapper.ProvisioningStateRowMapper;
import com.ericsson.oss.air.exception.CsacDAOException;
import jakarta.validation.Validation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

@ExtendWith(MockitoExtension.class)
class ProvisioningStateDaoJdbcImplTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private JdbcConfig jdbcConfig;

    @Spy
    private ValidationHandler validationHandler = new ValidationHandler();

    @InjectMocks
    private ProvisioningStateDaoJdbcImpl testDao;

    @Captor
    private ArgumentCaptor<String> sqlCaptor;

    @Captor
    private ArgumentCaptor<ProvisioningStateRowMapper> rowMapperCaptor;

    private final ProvisioningState initialState = ProvisioningState.builder()
            .withId(1)
            .withProvisioningStartTime(Instant.now())
            .withProvisioningEndTime(Instant.now())
            .withProvisioningState(ProvisioningState.State.INITIAL)
            .build();

    private final String queryLatestSql = String.format(SQL_SELECT_LATEST_PROV_STATE, "rtds");

    @BeforeEach
    void setUp() throws Exception {
        this.validationHandler.setValidator(Validation.buildDefaultValidatorFactory().getValidator());

        lenient().when(this.jdbcConfig.getRuntimeDatastoreSchemaName()).thenReturn("rtds");

        final String queryLatestSql = String.format(SQL_SELECT_LATEST_PROV_STATE, "rtds");
        lenient().when(this.jdbcTemplate.query(eq(queryLatestSql), any(ProvisioningStateRowMapper.class))).thenReturn(List.of(initialState));
    }

    @Test
    void save_new() {

        this.testDao.save(ProvisioningState.started());

        verify(this.jdbcTemplate).update(sqlCaptor.capture());

        assertEquals(
                "INSERT INTO rtds.rt_prov_state(provisioning_start_time,provisioning_end_time,provisioning_state) SELECT current_timestamp, current_timestamp, 'STARTED'",
                sqlCaptor.getValue());
    }

    @Test
    void save_udpate() {

        when(this.jdbcTemplate.query(eq(queryLatestSql), any(ProvisioningStateRowMapper.class))).thenReturn(List.of(ProvisioningState.started()));

        this.testDao.save(ProvisioningState.completed());

        verify(this.jdbcTemplate).update(sqlCaptor.capture());

        assertEquals(
                "UPDATE rtds.rt_prov_state SET provisioning_end_time = current_timestamp, provisioning_state = 'COMPLETED' WHERE id = (SELECT MAX(id) FROM rtds.rt_prov_state)",
                sqlCaptor.getValue());
    }

    @Test
    void save_dataAccessException() {

        final DataAccessException mockException = mock(DataAccessException.class);

        doNothing().when(this.validationHandler).checkEntity(any());

        when(this.jdbcTemplate.update(anyString())).thenThrow(mockException);

        assertThrows(CsacDAOException.class, () -> this.testDao.save(ProvisioningState.started()));
    }

    @Test
    void findById() {

        when(this.jdbcTemplate.query(anyString(), any(ProvisioningStateRowMapper.class))).thenReturn(List.of(initialState));

        final Optional<ProvisioningState> actual = this.testDao.findById(1);

        verify(this.jdbcTemplate).query(sqlCaptor.capture(), rowMapperCaptor.capture());

        assertEquals("SELECT * FROM rtds.rt_prov_state WHERE id = 1", sqlCaptor.getValue());

        assertTrue(actual.isPresent());

        assertEquals(initialState, actual.get());
    }

    @Test
    void findById_noMatch() {

        when(this.jdbcTemplate.query(anyString(), any(ProvisioningStateRowMapper.class))).thenReturn(List.of());

        final Optional<ProvisioningState> actual = this.testDao.findById(1);

        verify(this.jdbcTemplate).query(sqlCaptor.capture(), rowMapperCaptor.capture());

        assertEquals("SELECT * FROM rtds.rt_prov_state WHERE id = 1", sqlCaptor.getValue());

        assertTrue(actual.isEmpty());
    }

    @Test
    void findById_dataAccessException() {

        final DataAccessException mockException = mock(DataAccessException.class);

        when(this.jdbcTemplate.query(anyString(), any(ProvisioningStateRowMapper.class))).thenThrow(mockException);

        assertThrows(CsacDAOException.class, () -> this.testDao.findById(1));
    }

    @Test
    void existsById() {

        final String sql = String.format(SQL_COUNT_PROV_STATES_BY_ID, this.jdbcConfig.getRuntimeDatastoreSchemaName(), 1);
        when(this.jdbcTemplate.queryForObject(eq(sql), eq(Integer.class))).thenReturn(1);

        assertTrue(this.testDao.existsById(1));
    }

    @Test
    void existsById_noMatch() {

        final String sql = String.format(SQL_COUNT_PROV_STATES_BY_ID, this.jdbcConfig.getRuntimeDatastoreSchemaName(), 1);
        when(this.jdbcTemplate.queryForObject(eq(sql), eq(Integer.class))).thenReturn(0);

        assertFalse(this.testDao.existsById(1));
    }

    @Test
    void existsById_dataAccessException() {

        final DataAccessException mockException = mock(DataAccessException.class);

        final String sql = String.format(SQL_COUNT_PROV_STATES_BY_ID, this.jdbcConfig.getRuntimeDatastoreSchemaName(), 1);
        when(this.jdbcTemplate.queryForObject(eq(sql), eq(Integer.class))).thenThrow(mockException);

        assertThrows(CsacDAOException.class, () -> this.testDao.existsById(1));
    }

    @Test
    void findAll() {

        final String sql = String.format(SQL_SELECT_PROV_STATE, this.jdbcConfig.getRuntimeDatastoreSchemaName());

        final ProvisioningState other = ProvisioningState.builder()
                .withId(2)
                .withProvisioningState(ProvisioningState.State.STARTED)
                .withProvisioningStartTime(Instant.now())
                .build();

        when(this.jdbcTemplate.query(eq(sql), any(ProvisioningStateRowMapper.class))).thenReturn(List.of(initialState, other));

        assertEquals(2, StreamSupport.stream(this.testDao.findAll().spliterator(), false).count());

    }

    @Test
    void findAll_dataAccessException() {

        final DataAccessException mockException = mock(DataAccessException.class);

        final String sql = String.format(SQL_SELECT_PROV_STATE, this.jdbcConfig.getRuntimeDatastoreSchemaName());
        when(this.jdbcTemplate.query(eq(sql), any(ProvisioningStateRowMapper.class))).thenThrow(mockException);

        assertThrows(CsacDAOException.class, () -> this.testDao.findAll());
    }

    @Test
    void findAllById() {

        final String sql = String.format(SQL_SELECT_ID_IN, this.jdbcConfig.getRuntimeDatastoreSchemaName(), "?");

        when(this.jdbcTemplate.query(eq(sql), any(ProvisioningStateRowMapper.class), any())).thenReturn(List.of(initialState));

        assertEquals(1, StreamSupport.stream(this.testDao.findAllById(List.of(1)).spliterator(), false).count());
    }

    @Test
    void findAllById_dataAccessException() {

        final DataAccessException mockException = mock(DataAccessException.class);

        final String sql = String.format(SQL_SELECT_ID_IN, this.jdbcConfig.getRuntimeDatastoreSchemaName(), "?");

        when(this.jdbcTemplate.query(eq(sql), any(ProvisioningStateRowMapper.class), any())).thenThrow(mockException);

        assertThrows(CsacDAOException.class, () -> this.testDao.findAllById(List.of(1)));
    }

    @Test
    void count() {

        final String sql = String.format(SQL_COUNT_PROV_STATES, this.jdbcConfig.getRuntimeDatastoreSchemaName());

        when(this.jdbcTemplate.queryForObject(eq(sql), eq(Long.class))).thenReturn(1L);

        assertEquals(1, this.testDao.count());
    }

    @Test
    void count_dataAccessException() {

        final DataAccessException mockException = mock(DataAccessException.class);

        final String sql = String.format(SQL_COUNT_PROV_STATES, this.jdbcConfig.getRuntimeDatastoreSchemaName());

        when(this.jdbcTemplate.queryForObject(eq(sql), eq(Long.class))).thenThrow(mockException);

        assertThrows(CsacDAOException.class, () -> this.testDao.count());
    }

    @Test
    void findLatest() {

        // set up in the BeforeEach function
        assertEquals(this.initialState, this.testDao.findLatest());
    }

    @Test
    void findLatest_dataAccessException() {

        final DataAccessException mockException = mock(DataAccessException.class);

        final String queryLatestSql = String.format(SQL_SELECT_LATEST_PROV_STATE, "rtds");

        when(this.jdbcTemplate.query(eq(queryLatestSql), any(ProvisioningStateRowMapper.class))).thenThrow(mockException);

        assertThrows(CsacDAOException.class, () -> this.testDao.findLatest());
    }
}