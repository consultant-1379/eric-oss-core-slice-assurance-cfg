/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.model;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.ericsson.oss.air.csac.model.pmsc.AggregationPeriod;

/**
 * Simple configuration class that returns a list of configured aggregation period values.  Currently, only PMSC aggregation period values are
 * supplied.
 */
public class AggregationPeriodSupplier implements Supplier<List<Integer>> {

    private static final AggregationPeriodSupplier INSTANCE = new AggregationPeriodSupplier();

    /**
     * Returns a singleton instance of this supplier.
     *
     * @return singleton instance of this supplier
     */
    public static AggregationPeriodSupplier getInstance() {
        return INSTANCE;
    }

    @Override
    public List<Integer> get() {
        return Arrays.stream(AggregationPeriod.values()).map(AggregationPeriod::getValue).collect(Collectors.toList());
    }
}
