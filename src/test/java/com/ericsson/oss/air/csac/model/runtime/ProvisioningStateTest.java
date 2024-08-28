/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.model.runtime;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;

import com.ericsson.oss.air.exception.CsacProvisioningStateTransitionException;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

class ProvisioningStateTest {

    private final long startTime = 1704990000;
    private final long endTime = 1704990100;

    private final long invalidEndTime = 1704980000;

    private static final Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void testBuilder_validBean() {

        final ProvisioningState actual = ProvisioningState.builder()
                .withId(1)
                .withProvisioningState(ProvisioningState.State.STARTED)
                .withProvisioningStartTime(Instant.ofEpochMilli(this.startTime))
                .build();

        assertTrue(VALIDATOR.validate(actual).isEmpty());
    }

    @Test
    void testBuilder_defaultBean() {

        final ProvisioningState actual = ProvisioningState.builder().build();

        assertTrue(VALIDATOR.validate(actual).isEmpty());

        assertEquals(ProvisioningState.State.STARTED, actual.getProvisioningState());
    }

    @Test
    void testBuilder_completedState() {

        final ProvisioningState actual = ProvisioningState.builder()
                .withProvisioningState(ProvisioningState.State.COMPLETED)
                .build();

        assertTrue(VALIDATOR.validate(actual).isEmpty());

        assertEquals(ProvisioningState.State.COMPLETED, actual.getProvisioningState());
    }

    @Test
    void testBean_ctor() {

        final ProvisioningState actual = new ProvisioningState(1, Instant.ofEpochMilli(startTime), Instant.ofEpochMilli(endTime),
                ProvisioningState.State.ERROR);

        assertEquals(1, actual.getId());
        assertEquals(startTime, actual.getProvisioningStartTime().toEpochMilli());
        assertEquals(endTime, actual.getProvisioningEndTime().toEpochMilli());
        assertEquals(ProvisioningState.State.ERROR, actual.getProvisioningState());
    }

    @Test
    void testOf() {

        final ProvisioningState actual = ProvisioningState.ofState(ProvisioningState.State.STARTED);

        assertEquals(ProvisioningState.State.STARTED, actual.getProvisioningState());
    }

    @Test
    void testStarted() {

        final ProvisioningState actual = ProvisioningState.started();

        assertEquals(ProvisioningState.State.STARTED, actual.getProvisioningState());
    }

    @Test
    void testCompleted() {

        final ProvisioningState actual = ProvisioningState.completed();

        assertEquals(ProvisioningState.State.COMPLETED, actual.getProvisioningState());
    }

    @Test
    void testError() {

        final ProvisioningState actual = ProvisioningState.error();

        assertEquals(ProvisioningState.State.ERROR, actual.getProvisioningState());
    }

    @Test
    void testState_fromString() {

        assertEquals(ProvisioningState.State.STARTED, ProvisioningState.State.fromString("STARTED"));
        assertEquals(ProvisioningState.State.STARTED, ProvisioningState.State.fromString("started"));
        assertEquals(ProvisioningState.State.STARTED, ProvisioningState.State.fromString("STartED"));

        assertThrows(NullPointerException.class, () -> ProvisioningState.State.fromString(null));
        assertThrows(IllegalArgumentException.class, () -> ProvisioningState.State.fromString("NotAState"));
    }

    @Test
    void testEquals() {

        final Instant ts1 = Instant.ofEpochMilli(1705415000);
        final Instant ts2 = Instant.ofEpochMilli(1705416000);

        final ProvisioningState expected = ProvisioningState.builder()
                .withId(1)
                .withProvisioningState(ProvisioningState.State.STARTED)
                .withProvisioningStartTime(ts1)
                .build();

        final ProvisioningState actual = ProvisioningState.builder()
                .withId(1)
                .withProvisioningState(ProvisioningState.State.COMPLETED)
                .withProvisioningStartTime(ts1)
                .withProvisioningEndTime(ts2)
                .build();

        assertEquals(actual, expected);
    }

    @Test
    void testNotEquals() {

        final Instant ts = Instant.now();

        final ProvisioningState expected = ProvisioningState.builder()
                .withId(1)
                .withProvisioningState(ProvisioningState.State.COMPLETED)
                .withProvisioningStartTime(ts)
                .withProvisioningEndTime(ts)
                .build();

        final ProvisioningState actual = ProvisioningState.builder()
                .withId(2)
                .withProvisioningState(ProvisioningState.State.COMPLETED)
                .withProvisioningStartTime(ts)
                .withProvisioningEndTime(ts)
                .build();

        // first, check that two provisioning states are not equal based on Id only
        assertNotEquals(actual, expected);

        // now ensure that null is Ok
        assertNotEquals(null, actual);

        // finally, objects of a different type
        assertNotEquals(actual, Integer.valueOf(1));
    }

    @Test
    void testCompareTo() {

        final Instant ts1 = Instant.ofEpochMilli(1705415000);
        final Instant ts2 = Instant.ofEpochMilli(1705416000);

        final ProvisioningState expected = ProvisioningState.builder()
                .withId(1)
                .withProvisioningState(ProvisioningState.State.STARTED)
                .withProvisioningStartTime(ts1)
                .build();

        final ProvisioningState actual1 = ProvisioningState.builder()
                .withId(1)
                .withProvisioningState(ProvisioningState.State.COMPLETED)
                .withProvisioningStartTime(ts1)
                .withProvisioningEndTime(ts2)
                .build();

        final ProvisioningState actual2 = ProvisioningState.builder()
                .withId(2)
                .withProvisioningState(ProvisioningState.State.COMPLETED)
                .withProvisioningStartTime(ts1)
                .withProvisioningEndTime(ts2)
                .build();

        assertEquals(0, actual1.compareTo(expected));
        assertEquals(1, actual2.compareTo(expected));
    }

    @Test
    void checkStateTransition_initial() {

        assertThrows(CsacProvisioningStateTransitionException.class, () -> ProvisioningState.State.INITIAL.checkStateTransition(
                ProvisioningState.State.INITIAL));
        assertThrows(CsacProvisioningStateTransitionException.class, () -> ProvisioningState.State.INITIAL.checkStateTransition(
                ProvisioningState.State.COMPLETED));
        assertThrows(CsacProvisioningStateTransitionException.class, () -> ProvisioningState.State.INITIAL.checkStateTransition(
                ProvisioningState.State.ERROR));

        assertDoesNotThrow(() -> ProvisioningState.State.INITIAL.checkStateTransition(
                ProvisioningState.State.STARTED));
        assertDoesNotThrow(() -> ProvisioningState.State.INITIAL.checkStateTransition(
                ProvisioningState.State.RESET));
    }

    @Test
    void checkStateTransition_started() {

        assertThrows(CsacProvisioningStateTransitionException.class, () -> ProvisioningState.State.STARTED.checkStateTransition(
                ProvisioningState.State.INITIAL));
        assertThrows(CsacProvisioningStateTransitionException.class, () -> ProvisioningState.State.STARTED.checkStateTransition(
                ProvisioningState.State.STARTED));

        assertDoesNotThrow(() -> ProvisioningState.State.STARTED.checkStateTransition(
                ProvisioningState.State.COMPLETED));
        assertDoesNotThrow(() -> ProvisioningState.State.STARTED.checkStateTransition(
                ProvisioningState.State.ERROR));
        assertDoesNotThrow(() -> ProvisioningState.State.STARTED.checkStateTransition(
                ProvisioningState.State.RESET));
    }

    @Test
    void checkStateTransition_completed() {

        assertThrows(CsacProvisioningStateTransitionException.class, () -> ProvisioningState.State.COMPLETED.checkStateTransition(
                ProvisioningState.State.INITIAL));
        assertThrows(CsacProvisioningStateTransitionException.class, () -> ProvisioningState.State.COMPLETED.checkStateTransition(
                ProvisioningState.State.COMPLETED));
        assertThrows(CsacProvisioningStateTransitionException.class, () -> ProvisioningState.State.COMPLETED.checkStateTransition(
                ProvisioningState.State.ERROR));

        assertDoesNotThrow(() -> ProvisioningState.State.COMPLETED.checkStateTransition(
                ProvisioningState.State.STARTED));
        assertDoesNotThrow(() -> ProvisioningState.State.COMPLETED.checkStateTransition(
                ProvisioningState.State.RESET));
    }

    @Test
    void checkStateTransition_error() {

        assertThrows(CsacProvisioningStateTransitionException.class, () -> ProvisioningState.State.ERROR.checkStateTransition(
                ProvisioningState.State.INITIAL));
        assertThrows(CsacProvisioningStateTransitionException.class, () -> ProvisioningState.State.ERROR.checkStateTransition(
                ProvisioningState.State.COMPLETED));
        assertThrows(CsacProvisioningStateTransitionException.class, () -> ProvisioningState.State.ERROR.checkStateTransition(
                ProvisioningState.State.ERROR));

        assertDoesNotThrow(() -> ProvisioningState.State.ERROR.checkStateTransition(
                ProvisioningState.State.STARTED));
        assertDoesNotThrow(() -> ProvisioningState.State.ERROR.checkStateTransition(
                ProvisioningState.State.RESET));
    }

    @Test
    void checkStateTransition_reset() {

        assertThrows(CsacProvisioningStateTransitionException.class, () -> ProvisioningState.State.RESET.checkStateTransition(
                ProvisioningState.State.INITIAL));
        assertThrows(CsacProvisioningStateTransitionException.class, () -> ProvisioningState.State.RESET.checkStateTransition(
                ProvisioningState.State.COMPLETED));
        assertThrows(CsacProvisioningStateTransitionException.class, () -> ProvisioningState.State.RESET.checkStateTransition(
                ProvisioningState.State.ERROR));
        assertThrows(CsacProvisioningStateTransitionException.class, () -> ProvisioningState.State.RESET.checkStateTransition(
                ProvisioningState.State.INITIAL));

        assertDoesNotThrow(() -> ProvisioningState.State.RESET.checkStateTransition(
                ProvisioningState.State.STARTED));

    }
}