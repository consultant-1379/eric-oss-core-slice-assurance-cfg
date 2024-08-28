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

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.junit.jupiter.api.Test;

class ListUtilsTest {

    @Test
    void getMergedList() {

        final List<String> sourceList = List.of("a", "b", "c");
        final List<String> newElementList = List.of("a", "d");

        final List<String> expectedList = List.of("a", "b", "c", "d");
        final List<String> mergedList = ListUtils.getMergedList(sourceList, newElementList);

        assertTrue(CollectionUtils.isEqualCollection(expectedList, mergedList));
    }
}