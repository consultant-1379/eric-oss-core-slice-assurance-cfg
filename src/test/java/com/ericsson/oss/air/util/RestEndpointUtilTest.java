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

import static com.ericsson.oss.air.util.RestEndpointUtil.getRows;
import static com.ericsson.oss.air.util.RestEndpointUtil.getSafeSublistIndex;
import static com.ericsson.oss.air.util.RestEndpointUtil.getStart;
import static org.junit.jupiter.api.Assertions.assertEquals;


import org.junit.jupiter.api.Test;
import org.springframework.data.util.Pair;

public class RestEndpointUtilTest {

    @Test
    void testGetSafeSublistIndex() {
        int listSize = 5;

        Pair<Integer, Integer> startEndIndex = getSafeSublistIndex(listSize, 0, 1);
        assertEquals(0, startEndIndex.getFirst());
        assertEquals(1, startEndIndex.getSecond());

        startEndIndex = getSafeSublistIndex(listSize, 0, 2);
        assertEquals(0, startEndIndex.getFirst());
        assertEquals(2, startEndIndex.getSecond());

        startEndIndex = getSafeSublistIndex(listSize, 1, 2);
        assertEquals(2, startEndIndex.getFirst());
        assertEquals(4, startEndIndex.getSecond());

        startEndIndex = getSafeSublistIndex(listSize, 1, 5);
        assertEquals(5, startEndIndex.getFirst());
        assertEquals(5, startEndIndex.getSecond());

        startEndIndex = getSafeSublistIndex(listSize, 0, 0);
        assertEquals(0, startEndIndex.getFirst());
        assertEquals(0, startEndIndex.getSecond());
    }

    @Test
    void testGetRows() {

        assertEquals(0, getRows(0));

        assertEquals(Integer.MAX_VALUE, getRows(-1));
    }

    @Test
    void testGetStart() {

        assertEquals(0, getStart(0, 0));

        assertEquals(1, getStart(1, 0));

        assertEquals(0, getStart(1, -1));
    }
}
