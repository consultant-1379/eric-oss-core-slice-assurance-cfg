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

import lombok.EqualsAndHashCode;

/**
 * The {@code StatefulSequentialOperator} provides a mechanism for chaining functionality that relies on stateful data.  For example:
 *
 * <pre>
 *     StatefulSequentialOperator&lt;String&gt; op1 = new StatefulSequentialOperator&lt;&gt;().value("one");
 *     StatefulSequentialOperator&lt;String&gt; op2 = new StatefulSequentialOperator&lt;&gt;().value("two");
 *     StatefulSequentialOperator&lt;String&gt; op3 = new StatefulSequentialOperator&lt;&gt;().value("three");
 *
 *     op1.then(op2).then(op3).apply();
 * </pre>
 *
 * If an exception occurs in any of the operators in the sequence, the sequence is broken.
 *
 * There is no return value for this operator.
 *
 * @param <T>
 *         the input type.
 */
@EqualsAndHashCode
public abstract class StatefulSequentialOperator<T> {

    protected StatefulSequentialOperator parent;
    protected StatefulSequentialOperator child;

    protected T inputData;

    /**
     * No-operation instance of this operator.
     */
    public static StatefulSequentialOperator noop() {

        return new StatefulSequentialOperator() {

            @Override
            protected void doApply() {
                // do nothing
            }
        };
    }

    /**
     * Operator instance-specific application.  An operator implementation is expected to override this method to provide the business logic for that
     * operator implementation.
     */
    protected abstract void doApply();

    /**
     * The apply method which will execute each rollback in the sequence with the provided input.
     */
    public void apply() {

        if (Objects.isNull(this.inputData)) {
            return;
        }

        StatefulSequentialOperator handler = this;

        // navigate to the top of the list
        while (Objects.nonNull(handler.parent)) {
            handler = handler.parent;
        }

        handler.doApply();

        while (Objects.nonNull(handler.child)) {
            handler = handler.child;
            handler.doApply();
        }

    }

    /**
     * Sets the data to perform rollback actions on.
     *
     * @param inputData
     *         data to perform rollback actions on
     * @return this operator instance
     */
    public StatefulSequentialOperator<T> value(final T inputData) {
        this.inputData = inputData;

        return this;
    }

    /**
     * Adds an operator to the sequence.
     *
     * @param nextOperator
     *         operator to add to the sequence.
     * @return the next operator in the sequence.
     */
    public StatefulSequentialOperator then(final StatefulSequentialOperator nextOperator) {

        this.child = nextOperator;
        nextOperator.parent = this;

        return nextOperator;
    }
}
