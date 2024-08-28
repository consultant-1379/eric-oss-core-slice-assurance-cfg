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

import static org.junit.Assert.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;

@SpringBootTest(classes = ConsistencyCheckHandler.class)
@RecordApplicationEvents
@EnableAsync
class ConsistencyCheckHandlerTest {

    @Autowired
    private ApplicationEvents applicationEvents;

    @Autowired
    private ConsistencyCheckHandler handler;

    @Test
    void notifyCheckFailure() {
        final ConsistencyCheckEvent.Payload payload = new ConsistencyCheckEvent.Payload(ConsistencyCheckEvent.Payload.Type.FAILURE, 2);

        this.handler.notifyCheckFailure(payload);
        assertEquals(1, applicationEvents.stream().filter(ev -> ev instanceof ConsistencyCheckEvent).count());

        final ConsistencyCheckEvent.Payload receivedPayload = applicationEvents.stream()
                .filter(ev -> ev instanceof ConsistencyCheckEvent)
                .map(ev -> ((ConsistencyCheckEvent) ev).getPayload()).findFirst().get();

        assertEquals(2, receivedPayload.getCount());
        assertEquals(ConsistencyCheckEvent.Payload.Type.FAILURE, receivedPayload.getType());
    }

}