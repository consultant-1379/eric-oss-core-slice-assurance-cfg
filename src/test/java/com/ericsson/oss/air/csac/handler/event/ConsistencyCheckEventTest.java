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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

class ConsistencyCheckEventTest {

    final ConsistencyCheckEvent.Payload payload = new ConsistencyCheckEvent.Payload(ConsistencyCheckEvent.Payload.Type.FAILURE, 2);

    final Object source = new Object();

    private final ConsistencyCheckEvent consistencyCheckEvent = new ConsistencyCheckEvent(source, payload);

    @Test
    void testGetTimestamp() {
        assertNotNull(consistencyCheckEvent.getTimestamp());
    }

    @Test
    void testGetSource() {
        assertNotNull(consistencyCheckEvent.getPayload());
    }

    @Test
    void testGetPayload() {
        assertEquals(4, Arrays.stream(ConsistencyCheckEvent.Payload.Type.values()).count());
        assertEquals(2, consistencyCheckEvent.getPayload().getCount());
        assertEquals(ConsistencyCheckEvent.Payload.Type.FAILURE, consistencyCheckEvent.getPayload().getType());
    }

}