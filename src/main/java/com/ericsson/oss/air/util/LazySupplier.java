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

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

/**
 * An abstract thread-safe implementation of a Supplier interface. In this implementation, the get() method is an identify function, given that it
 * will always initialize and return the same value until explicitly closed or reset.
 *
 * This supplier is also auto-closeable, meaning it can be used safely in a try-with-resources block if the supplier is associated with any resources
 * that required cleanup when this supplier is closed due to a runtime exception.
 *
 * @param <T>
 *         Java type of supplier
 */
public abstract class LazySupplier<T> implements Supplier<T>, AutoCloseable {

    private boolean isClosed = false;

    /**
     * The atomic instance being supplied.
     */
    protected AtomicReference<T> instance;

    /**
     * Returns the instance, initialize atomically if not initialized.
     */
    @Override
    public synchronized T get() {
        if (this.instance == null) {
            this.instance = new AtomicReference<>(initialize());
        }

        return this.instance.get();
    }

    /**
     * Closes this supplier by releasing associated resources and clearing the internal instance reference.
     */
    @Override
    public void close() {
        reset();

        this.isClosed = true;
    }

    /**
     * Returns true if this supplier is closed.  The supplier state can be reset using the {@link #reset()} method.
     *
     * @return true if this supplier is closed
     */
    public boolean isClosed() {
        return this.isClosed;
    }

    /**
     * Releases any resources associated with this supplier and clears the internal instance reference. To ensure that resources are released
     * correctly, implementors must override the {@link #release()} method and add any code that would relinquish underlying resources. If there are
     * no underlying resources requiring releasing, it is not necessary to implement the {@code release()} method. If an exception must be thrown, it
     * must be a runtime, i.e., unchecked, exception.
     */
    public synchronized void reset() {

        this.isClosed = false;

        if (this.instance != null) {
            release();
        }

        this.instance = null;
    }

    /**
     * Override this thread-safe method to allow the instance to release any resources it may have opened.
     */
    protected void release() {
        // no default operation
    }

    /**
     * Initializes the instance.
     */
    protected abstract T initialize();
}