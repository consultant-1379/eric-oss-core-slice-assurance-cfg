/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class CsacProvisioningStateTransitionExceptionTest {

    @Test
    void testDefaultCtor() {

        final CsacProvisioningStateTransitionException actual = new CsacProvisioningStateTransitionException();

        assertNull(actual.getMessage());
        assertNull(actual.getCause());
    }

    @Test
    void testMessageCtor() {

        final CsacProvisioningStateTransitionException actual = new CsacProvisioningStateTransitionException("Message");

        assertEquals("Message", actual.getMessage());
        assertNull(actual.getCause());
    }

    @Test
    void testCauseCtor() {

        final CsacProvisioningStateTransitionException actual = new CsacProvisioningStateTransitionException(new IllegalArgumentException());

        assertEquals("java.lang.IllegalArgumentException", actual.getMessage());
        assertNotNull(actual.getCause());
        assertEquals(IllegalArgumentException.class, actual.getCause().getClass());
    }

    @Test
    void testMessageCauseCtor() {

        final CsacProvisioningStateTransitionException actual = new CsacProvisioningStateTransitionException("Message",
                new IllegalArgumentException());

        assertEquals("Message", actual.getMessage());
        assertNotNull(actual.getCause());
        assertEquals(IllegalArgumentException.class, actual.getCause().getClass());
    }

}