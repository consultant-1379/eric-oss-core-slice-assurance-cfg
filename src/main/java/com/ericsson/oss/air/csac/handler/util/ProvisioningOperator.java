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

import com.ericsson.oss.air.util.operator.SequentialOperator;
import com.ericsson.oss.air.util.operator.StatefulSequentialOperator;

/**
 * Base class for atomic provisioning operators.
 *
 * @param <T>
 *         input data type
 * @param <R>
 *         rollback data type
 */
public abstract class ProvisioningOperator<T, R> extends SequentialOperator<T> {

    private StatefulSequentialOperator<R> rollbackOperator = StatefulSequentialOperator.noop();

    /**
     * Returns a composite {@link StatefulSequentialOperator} representing the rollback operations for the chain of operators. The rollback operators
     * will be executed in the reverse order, i.e. child -> parent, of the original operations.
     *
     * @return a composite {@code StatefulSequentialOperator} representing the rollback operations for the chain of operators
     */
    public StatefulSequentialOperator getRollback() {

        ProvisioningOperator<?, ?> current = this;
        while (current.child != null) {
            current = (ProvisioningOperator<?, ?>) current.child;
        }

        final StatefulSequentialOperator composedRollback = current.getRollbackOperator();

        while (current.parent != null) {
            current = (ProvisioningOperator<?, ?>) current.parent;
            composedRollback.then(current.getRollbackOperator());
        }

        return composedRollback;
    }

    protected StatefulSequentialOperator getRollbackOperator() {
        return this.rollbackOperator;
    }

    /**
     * Sets the rollback operator for this {@code ProvisioningOperator}.
     *
     * @param rollback
     *         rollback operator for this {@code ProvisioningOperator}
     * @return this operator
     */
    public ProvisioningOperator<T, R> setRollbackOperator(final StatefulSequentialOperator<R> rollback) {
        this.rollbackOperator = rollback;

        return this;
    }

    /**
     * Adds a {@code ProvisioningOperator} to the sequence.
     *
     * @param nextOperator
     *         operator to add to the sequence.
     * @return this operator.
     */
    public ProvisioningOperator then(final ProvisioningOperator nextOperator) {
        super.then(nextOperator);
        return this;
    }

    /**
     * The generic {@link SequentialOperator#then(SequentialOperator)} method is not supported.
     *
     * @throws UnsupportedOperationException
     */
    @Override
    public SequentialOperator then(final SequentialOperator nextOperator) {
        throw new UnsupportedOperationException("Generic sequential operator not supported. Use ProvisioningOperator::then instead");
    }

    /*
     * (non-javadoc)
     *
     * This method should be invoked only in the doApply method of this operator.
     */
    protected void setRollbackData(final R rollbackData) {
        this.rollbackOperator.value(rollbackData);
    }
}
