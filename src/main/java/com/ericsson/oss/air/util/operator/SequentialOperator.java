/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.util.operator;

import java.util.Objects;

/**
 * The {@code SequentialOperator} provides a mechanism for chaining stateless functionality that uses a common input to perform different and
 * otherwise unrelated operations fluently.  For example:
 *
 * <pre>
 *     operator1.then(operator2).then(operator3).apply(input);
 * </pre>
 *
 * is a fluent way of writing
 *
 * <pre>
 *     operator1.apply(input);
 *     operator2.apply(input);
 *     operator3.apply(input);
 * </pre>
 *
 * If an exception occurs in any of the operators in the sequence, the sequence is broken.
 *
 * There is no return value for the sequential operator.
 *
 * @param <T>
 *         the input type.
 */
public abstract class SequentialOperator<T> {

    protected SequentialOperator parent;
    protected SequentialOperator child;

    /**
     * Operator instance-specific application.  An operator is expected to override this method to provide the business logic for that operator
     * instance.
     *
     * @param t
     *         input for this operator instance
     */
    protected abstract void doApply(T t);

    /**
     * The apply method which will execute each operator in the sequence with the provided input.
     *
     * @param t
     *         input for each operator in the sequence.
     */
    public void apply(final T t) {

        SequentialOperator handler = this;

        // navigate to the top of the list
        while (Objects.nonNull(handler.parent)) {
            handler = handler.parent;
        }

        handler.doApply(t);

        while (Objects.nonNull(handler.child)) {
            handler = handler.child;
            handler.doApply(t);
        }

    }

    /**
     * Adds an operator to the sequence.
     *
     * @param nextOperator
     *         operator to add to the sequence.
     * @return the next operator in the sequence.
     */
    public SequentialOperator<T> then(final SequentialOperator<T> nextOperator) {

        this.child = nextOperator;
        nextOperator.parent = this;

        return nextOperator;
    }
}
