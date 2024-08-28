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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;

import com.ericsson.oss.air.csac.model.runtime.ProvisioningState;
import org.springframework.jdbc.core.RowMapper;

/**
 * Maps a row from the runtime provisioning state table to a {@link ProvisioningState} instance.
 */
public class ProvisioningStateRowMapper implements RowMapper<ProvisioningState> {

    @Override
    public ProvisioningState mapRow(final ResultSet rs, final int rowNum) throws SQLException {

        final int id = rs.getInt(COLUMN_PK);
        final Instant startTime = rs.getTimestamp(COLUMN_PROV_START_TIME).toInstant();

        final Optional<Timestamp> endTimeStamp = Optional.ofNullable(rs.getTimestamp(COLUMN_PROV_END_TIME));
        final Instant endTime = endTimeStamp.isPresent()
                ? endTimeStamp.get().toInstant()
                : null;

        final ProvisioningState.State state = ProvisioningState.State.fromString(rs.getString(COLUMN_PROV_STATE));

        return new ProvisioningState(id, startTime, endTime, state);
    }
}
