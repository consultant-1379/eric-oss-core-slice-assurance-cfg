/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.handler.request;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.ericsson.oss.air.api.model.RtProvisioningStateDto;
import com.ericsson.oss.air.csac.model.runtime.ProvisioningState;
import com.ericsson.oss.air.csac.repository.ProvisioningStateDao;
import com.ericsson.oss.air.util.Conditional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Request handler for REST requests related to CSAC provisioning state.
 */
@RequiredArgsConstructor
@Component
public class ProvisioningStateRequestHandler {

    private final ProvisioningStateDao provisioningStateDao;

    /**
     * Returns the most recent provisioning state.
     *
     * @return the most recent provisioning state
     */
    public RtProvisioningStateDto getLatestProvisioningState() {

        final ProvisioningState latestState = this.provisioningStateDao.findLatest();

        return mapProvisioningState(latestState);
    }

    /**
     * Returns the provisioning history of CSAC as a list of all provisioning states.
     *
     * @return the provisioning history of CSAC as a list of all provisioning states
     */
    public List<RtProvisioningStateDto> getProvisioningStates() {

        return StreamSupport.stream(this.provisioningStateDao.findAll().spliterator(), false)
                .map(ProvisioningStateRequestHandler::mapProvisioningState)
                .collect(Collectors.toList());
    }

    private static RtProvisioningStateDto mapProvisioningState(final ProvisioningState state) {

        Objects.requireNonNull(state);

        // end time may be null
        final Conditional<Long, Instant> provisioningEndTime = Conditional.<Long, Instant> builder()
                .condition(Objects::nonNull)
                .value(Instant::toEpochMilli)
                .build();

        return new RtProvisioningStateDto()
                .id(state.getId())
                .provisioningState(state.getProvisioningState().name())
                .provisioningStartTime(state.getProvisioningStartTime().toEpochMilli())
                .provisioningEndTime(provisioningEndTime.apply(state.getProvisioningEndTime()));
    }
}
