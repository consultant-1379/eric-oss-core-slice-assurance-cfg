/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.model.pmschema;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class SchemaReferenceTest {

    @Test
    void of() {

        final SchemaReference expected = SchemaReference.builder()
                .dataSpace("5G")
                .dataCategory("PM_COUNTERS")
                .schemaId("Schema_1")
                .build();

        final SchemaReference actual = SchemaReference.of("5G|PM_COUNTERS|Schema_1");

        assertEquals(expected, actual);
    }

    @Test
    void of_invalidString() {

        assertThrows(IllegalArgumentException.class, () -> SchemaReference.of("5G|PM_COUNTERS|"));
        assertThrows(IllegalArgumentException.class, () -> SchemaReference.of("5G||Schema_1"));
        assertThrows(IllegalArgumentException.class, () -> SchemaReference.of("|PM_COUNTERS|Schema_1"));
    }

    @Test
    void testToString() {

        final String expected = "5G|PM_COUNTERS|Schema_1";

        final String actual = SchemaReference.builder()
                .dataSpace("5G")
                .dataCategory("PM_COUNTERS")
                .schemaId("Schema_1")
                .build()
                .toString();

        assertEquals(expected, actual);
    }
}