/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.handler.util;

import java.util.Objects;

/**
 * Represents an operator that takes no arguments and returns nothing.
 */
@FunctionalInterface
public interface VoidOperator {

    /**
     * Applies the operator.
     */
    void apply();

    /**
     * Returns a composed operator that first applies this operator, then applies the subsequent operator.
     *
     * @param nextOp
     *         the next operator to apply.
     * @return a composed operator that first applies this operator, then applies the subsequent operator
     */
    default VoidOperator andThen(final VoidOperator nextOp) {
        Objects.requireNonNull(nextOp);
        apply();
        return nextOp;
    }
}
