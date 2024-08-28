/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

class MapUtilsTest {

    @Test
    void getMapEntry() {

        final Map<String, String> source = new HashMap<>();
        source.put("one", "One Value");
        source.put("two", "Two Value");
        source.put("three", "Three Value");

        final Map.Entry<String, String> actual = MapUtils.getMapEntry("one", source);

        assertNotNull(actual);
        assertEquals("one", actual.getKey());
        assertEquals("One Value", actual.getValue());
    }

    @Test
    void getMapEntry_nullEntry() {

        final Map<String, String> source = new HashMap<>();
        source.put("one", "One Value");
        source.put("two", "Two Value");
        source.put("three", "Three Value");

        assertNull(MapUtils.getMapEntry("four", source));
    }

    @Test
    void getMapEntry_nullKey() {
        assertThrows(NullPointerException.class, () -> MapUtils.getMapEntry(null, new HashMap()));
    }

    @Test
    void getMapEntry_nullSource() {
        assertThrows(NullPointerException.class, () -> MapUtils.getMapEntry("one", null));
    }
}