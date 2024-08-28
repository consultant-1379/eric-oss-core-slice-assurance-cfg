/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.model.pmsc;

import com.ericsson.oss.air.exception.CsacValidationException;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;

/**
 * Enum to represent permissible values for the PMSC KPI aggregation period (in minutes)
 */
@AllArgsConstructor
public enum AggregationPeriod {

    FIFTEEN(15),
    SIXTY(60),
    ONE_THOUSAND_FOUR_HUNDRED_FORTY(1440);

    private final int value;

    public static boolean contains(final int value) {
        for (final AggregationPeriod validAggregationPeriod : AggregationPeriod.values()) {
            if (value == validAggregationPeriod.getValue()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Return AggregationPeriod enum object from given integer value
     *
     * @param value
     *         the integer value
     * @return the aggregation period enum
     */
    public static AggregationPeriod valueOf(final Integer value) {
        if (!AggregationPeriod.contains(value)) {
            throw new CsacValidationException(
                    String.format("%d is not a permitted value for the PMSC KPI aggregation period", value));
        }
        switch (value) {
            case 15:
                return FIFTEEN;
            case 60:
                return SIXTY;
            default:
                return ONE_THOUSAND_FOUR_HUNDRED_FORTY;
        }
    }

    /**
     * Gets the integer value of the {@Code ValidAggregationPeriodEnum}
     *
     * @return the integer value of the {@Code ValidAggregationPeriodEnum}
     */
    @JsonValue
    public Integer getValue() {
        return this.value;
    }
}
