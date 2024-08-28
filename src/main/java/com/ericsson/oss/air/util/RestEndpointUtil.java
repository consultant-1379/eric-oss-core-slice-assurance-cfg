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

import org.springframework.data.util.Pair;

public class RestEndpointUtil {

    private RestEndpointUtil() {
    }

    /**
     * Utility function to calculate and return the start index (included) and end index (excluded) of the entries in a data structure given that the
     * structure should be retrieved in a paged format.
     *
     * @param total
     *         the total size of the data structure to be paged
     * @param pageNumber
     *         the page number from which the entries must be retrieved
     * @param pageSize
     *         the size of each page requested
     * @return a pair containing the start index (inclusive) and end index (exclusive) of the requested entries
     */
    public static Pair<Integer, Integer> getSafeSublistIndex(final int total, final int pageNumber, final int pageSize) {
        final Integer startIndex = Math.min(pageNumber * pageSize, total);
        final Integer endIndex = Math.min((pageNumber + 1) * pageSize, total);

        return Pair.of(startIndex, endIndex);
    }

    public static Integer getRows(final Integer rows) {
        return (rows < 0) ? Integer.MAX_VALUE : rows;
    }

    public static Integer getStart(final Integer start, final Integer rows) {
        return (rows < 0) ? 0 : start;
    }
}
