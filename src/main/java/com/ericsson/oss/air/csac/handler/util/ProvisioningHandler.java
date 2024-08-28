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
 * The {@code ProvisioningHandler} class provides a composable set of handler operations when multiple handlers must operate on the same set of input
 * data statelessly.  For example, KPI provisioning and augmentation provisioning must operate on the same set of profile definitions.  This class
 * also supports composable rollback operations, so that a single or composed ProvisioningHandler instance can provide a composed rollback operator
 * which will execute all required rollback operations in the correct sequence.
 *
 * <pre>
 *     class KpiProvisioningHandler extends ProvisioningHandler&lt;List&lt;ProfileDefinition&gt;&gt; {}
 *
 *     class AugmentationProvisioningHandler extends ProvisioningHandler&lt;List&lt;ProfileDefinition&gt;&gt; {}
 *
 *     final KpiProvisioningHandler kpiHandler = new KpiProvisioningHandler();
 *     final AugmentationProvisioningHandler augHandler = new AugmentationProvisioningHandler();
 *
 *     final ProvisioningHandler&lt;List&lt;ProfileDefinition&gt;&gt; provisioning = kpiHandler.then(augHandler);
 *
 *     try {
 *         provisioning.apply(profileList);
 *     } catch (final Exception ex) {
 *         provisioning.getRollback().apply();
 *     }
 *
 * </pre>
 *
 * This {@code ProvisioningHandler} is a wrapper for a composite {@link ProvisioningOperator}, which itself may be composed of multiple provisioning
 * operations.
 *
 * @param <T>
 *         input data type for this handler.
 */
public abstract class ProvisioningHandler<T> extends SequentialOperator<T> {

    private boolean isHandlerCompleted;

    /*
     * (non-javadoc)
     *
     * Returns the rollback operator associated with composed provisioning operator(s) defined in this handler.
     */
    protected abstract StatefulSequentialOperator getRollbackOperator();

    /**
     * Returns the composed rollback operator for all handlers in this composite handler.
     *
     * @return the composed rollback operator for all handlers in this composite handler
     */
    public StatefulSequentialOperator getRollback() {

        if (this.isHandlerCompleted) {
            return StatefulSequentialOperator.noop();
        }

        final StatefulSequentialOperator composedRollback = this.getRollbackOperator();

        ProvisioningHandler current = this;
        while (current.parent != null) {
            current = (ProvisioningHandler) current.parent;
            composedRollback.then(current.getRollbackOperator());
        }

        this.isHandlerCompleted = true;

        return composedRollback;
    }

    @Override
    public void apply(final T t) {
        this.isHandlerCompleted = false;

        super.apply(t);

        this.isHandlerCompleted = true;
    }

    /*
     * (non-javadoc)
     *
     * Sets the isHandlerCompleted flag.  Intended for test purposes only.
     */
    protected void setIsHandlerComplete(final boolean isComplete) {
        this.isHandlerCompleted = isComplete;
    }

    /**
     * Returns a flag indicating whether this handler's operation completed. This flag does not indicate success or failure.
     *
     * @return a flag indicating whether this handler's operation completed
     */
    public boolean getIsHandlerComplete() {
        return this.isHandlerCompleted;
    }

    /**
     * Handler typesafe implementation of the {@link SequentialOperator#then} method.
     *
     * @param nextHandler
     *         next provisioning handler in the sequence
     * @return next provisioning handler in the sequence
     */
    public ProvisioningHandler<T> then(final ProvisioningHandler<T> nextHandler) {
        return (ProvisioningHandler<T>) super.then(nextHandler);
    }

    /**
     * Disables the superclass method to ensure type safety in the composed ProvisioningHandler.
     */
    @Override
    public SequentialOperator<T> then(final SequentialOperator<T> nextOperator) {
        throw new UnsupportedOperationException();
    }
}
