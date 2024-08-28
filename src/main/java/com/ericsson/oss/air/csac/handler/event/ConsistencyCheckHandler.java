/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.handler.event;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * This class provides the entry point for the business logic required to perform a resource consistency check notification.
 */
@Component
@RequiredArgsConstructor
public class ConsistencyCheckHandler {

    private final ApplicationEventPublisher eventPublisher;

    /**
     * This function is invoked by provisioning handlers when writes to the CSAC DB fail after successful provisioning
     * raises the custom ConsistencyCheckEvent with a payload indicating a consistency check failure
     *
     * @param payload {@link com.ericsson.oss.air.csac.handler.event.ConsistencyCheckEvent.Payload}
     *                for the consistency check event {@link ConsistencyCheckEvent}
     */
    public void notifyCheckFailure(final ConsistencyCheckEvent.Payload payload) {
        this.eventPublisher.publishEvent(new ConsistencyCheckEvent(this, payload));
    }

}
