/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class ConditionalTest {

    @Test
    void apply_defaultElseValue() {

        final Conditional<String, String> conditional = Conditional.<String, String> builder()
                .condition(s -> s.equals("Initial"))
                .value(s -> "Expected")
                .build();

        assertEquals("Expected", conditional.apply("Initial"));

        assertNull(conditional.apply("NoMatch"));
    }

    @Test
    void apply_nonDefaultElseValue() {

        final Conditional<String, String> conditional = Conditional.<String, String> builder()
                .condition(s -> s.equals("Initial"))
                .value(s -> "Expected")
                .elseValue(s -> "Alternate")
                .build();

        assertEquals("Expected", conditional.apply("Initial"));

        assertEquals("Alternate", conditional.apply("NoMatch"));
    }
}