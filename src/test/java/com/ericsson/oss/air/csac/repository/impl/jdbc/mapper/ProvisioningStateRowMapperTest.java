/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.repository.impl.jdbc.mapper;

import static com.ericsson.oss.air.csac.repository.impl.jdbc.ProvisioningStateDaoJdbcImpl.COLUMN_PK;
import static com.ericsson.oss.air.csac.repository.impl.jdbc.ProvisioningStateDaoJdbcImpl.COLUMN_PROV_END_TIME;
import static com.ericsson.oss.air.csac.repository.impl.jdbc.ProvisioningStateDaoJdbcImpl.COLUMN_PROV_START_TIME;
import static com.ericsson.oss.air.csac.repository.impl.jdbc.ProvisioningStateDaoJdbcImpl.COLUMN_PROV_STATE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.Instant;

import com.ericsson.oss.air.csac.model.runtime.ProvisioningState;
import org.h2.tools.SimpleResultSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ProvisioningStateRowMapperTest {

    private SimpleResultSet testResultSet;

    private ProvisioningStateRowMapper testMapper;

    private final long startTime = 1705078000L;

    private final long endTime = 1705079000L;

    @BeforeEach
    void setUp() {
        this.testMapper = new ProvisioningStateRowMapper();

        this.testResultSet = new SimpleResultSet();
        this.testResultSet.addColumn(COLUMN_PK, Types.INTEGER, 0, 0);
        this.testResultSet.addColumn(COLUMN_PROV_START_TIME, Types.TIMESTAMP_WITH_TIMEZONE, 0, 0);
        this.testResultSet.addColumn(COLUMN_PROV_END_TIME, Types.TIMESTAMP_WITH_TIMEZONE, 0, 0);
        this.testResultSet.addColumn(COLUMN_PROV_STATE, Types.VARCHAR, 0, 0);
    }

    @Test
    void mapRow() throws Exception {

        final ProvisioningState expected = new ProvisioningState(1, Instant.ofEpochMilli(startTime), Instant.ofEpochMilli(endTime),
                ProvisioningState.State.INITIAL);

        this.testResultSet.addRow(1, Timestamp.from(Instant.ofEpochMilli(this.startTime)), Timestamp.from(Instant.ofEpochMilli(this.endTime)),
                ProvisioningState.State.INITIAL.name());
        this.testResultSet.next();

        final ProvisioningState actual = this.testMapper.mapRow(this.testResultSet, 0);

        assertNotNull(actual);
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getProvisioningStartTime(), actual.getProvisioningStartTime());
        assertEquals(expected.getProvisioningEndTime(), actual.getProvisioningEndTime());
        assertEquals(expected.getProvisioningState(), actual.getProvisioningState());
    }

    @Test
    void mapRow_startedNoEndTime() throws Exception {

        final ProvisioningState expected = new ProvisioningState(1, Instant.ofEpochMilli(startTime), null,
                ProvisioningState.State.STARTED);

        this.testResultSet.addRow(1, Timestamp.from(Instant.ofEpochMilli(this.startTime)), null,
                ProvisioningState.State.STARTED.name());
        this.testResultSet.next();

        final ProvisioningState actual = this.testMapper.mapRow(this.testResultSet, 0);

        assertNotNull(actual);
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getProvisioningStartTime(), actual.getProvisioningStartTime());
        assertEquals(expected.getProvisioningEndTime(), actual.getProvisioningEndTime());
        assertEquals(expected.getProvisioningState(), actual.getProvisioningState());
    }

    @Test
    void mapRow_jdbcException() throws Exception {

        final SimpleResultSet resultSet = new SimpleResultSet();
        resultSet.addColumn("RANDOM_COLUMN_NAME", 0, 0, 0);
        resultSet.next();

        assertThrows(SQLException.class, () -> this.testMapper.mapRow(resultSet, 0));
    }
}