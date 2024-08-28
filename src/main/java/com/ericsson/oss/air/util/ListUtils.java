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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Utilities for using List classes.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ListUtils {

    /**
     * Returns an unmodifiable list containing distinct elements of the provided source list and new element list.
     *
     * @param sourceList       the source list
     * @param newElementList   the list containing the new elements to merge into the source list
     * @return an unmodifiable list containing distinct elements of the provided source list and new element list
     * @param <T>              the type of list elements
     */
    public static <T> List<T> getMergedList(final List<T> sourceList, final List<T> newElementList) {

        final Set<T> mergedSet = new HashSet<>(sourceList);
        mergedSet.addAll(newElementList);
        return List.copyOf(mergedSet.stream().toList());
    }
}
