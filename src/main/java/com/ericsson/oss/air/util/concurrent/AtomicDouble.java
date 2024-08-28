/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.util.concurrent;

import static java.lang.Double.doubleToRawLongBits;
import static java.lang.Double.longBitsToDouble;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLongFieldUpdater;

/**
 * A {@code double} value that can be updated atomically.  The properties of atomic variables are described in the {@link java.util.concurrent.atomic}
 * package specification.  An {@code AtomicDouble} can be used to atomically set double values but cannot be used as a replacement for
 * {@code java.lang.Double}.  However, the {@code AtomicDouble} extends {@code java.lang.Number} which allows uniform access by tools and utilities
 * that deal with numerically-based classes.
 *
 * The {@code AtomicDouble} is ideally suited as a thread-safe holder for double values.
 */
public final class AtomicDouble extends Number implements Serializable {

    private static final long serialVersionUID = 0L;

    private transient volatile long rep;

    private static final AtomicLongFieldUpdater<AtomicDouble> UPDATER =
            AtomicLongFieldUpdater.newUpdater(AtomicDouble.class, "rep");

    /**
     * Creates an {@code AtomicDouble} with the initial value of {@code 0.0d}
     */
    public AtomicDouble() {
        this.rep = 0L;
    }

    /**
     * Creates an {@code AtomicDouble} with the specified initial value.
     *
     * @param value
     *         initial value for this {@code AtomicDouble}
     */
    public AtomicDouble(final double value) {
        this.set(value);
    }

    /**
     * Returns this value as a {@code double}.
     *
     * @return this value as a {@code double}
     */
    public double get() {
        return longBitsToDouble(this.rep);
    }

    /**
     * Atomically sets to the given value and returns the old value.
     *
     * @param newValue
     *         new value
     * @return old value
     */
    public double getAndSet(final double newValue) {
        final long nextValue = doubleToRawLongBits(newValue);
        return longBitsToDouble(UPDATER.getAndSet(this, nextValue));
    }

    /**
     * Sets to the specified value.
     *
     * @param value
     *         value to set
     */
    public void set(final double value) {
        this.rep = doubleToRawLongBits(value);
    }

    /**
     * Atomically sets the value to the given updated value if the current value == the expected value.
     *
     * @param expectedValue
     *         expected value
     * @param value
     *         value to set
     * @return {@code true} if successful. {@code false} indicates that the actual value was not equal to the expected value.
     */
    public boolean compareAndSet(final double expectedValue, final double value) {
        return UPDATER.compareAndSet(this, doubleToRawLongBits(expectedValue), doubleToRawLongBits(value));
    }

    /**
     * Return an {@code AtomicDouble} instance representing the specified double value.
     *
     * @param value
     *         double value to set
     * @return an {@code AtomicDouble} instance representing the specified double value
     */
    public static AtomicDouble valueOf(final double value) {
        return new AtomicDouble(value);
    }

    /**
     * Returns an {@code AtomicDouble} object holding the double value represented by the argument string s.  The semantics for string parsing are
     * described in the specification for {@link Double#valueOf(String)}.
     *
     * @param strValue
     *         the string to be parsed.
     * @return an {@code AtomicDouble} object holding the double value represented by the argument string s
     * @throws {@link
     *         NumberFormatException} - if the string does not contain a parsable number.
     */
    public static AtomicDouble valueOf(final String strValue) throws NumberFormatException {
        return new AtomicDouble(Double.parseDouble(strValue));
    }

    @Override
    public int intValue() {
        return (int) this.get();
    }

    @Override
    public long longValue() {
        return (long) this.get();
    }

    @Override
    public float floatValue() {
        return (float) this.get();
    }

    @Override
    public double doubleValue() {
        return this.get();
    }

    /**
     * Returns the String representation of the current value.
     *
     * @return the String representation of the current value
     */
    @Override
    public String toString() {
        return Double.toString(this.get());
    }

    @Override
    public boolean equals(final Object other) {

        if (!Objects.isNull(other) && other.getClass() == this.getClass()) {
            return this.rep == ((AtomicDouble) other).rep;
        }

        return false;
    }

    @Override
    public int hashCode() {
        return Long.valueOf(this.rep).hashCode();
    }

    /**
     * Serializes the state to an output stream as a {@code double}.
     *
     * @param outputStream
     *         output stream for serialization.
     * @serialData The current value is emitted (a {@code double}).
     */
    @Serial
    private void writeObject(final ObjectOutputStream outputStream) throws java.io.IOException {

        outputStream.defaultWriteObject();
        outputStream.writeDouble(get());
    }

    /**
     * Deserializes the instance from an input stream.
     *
     * @param inputStream
     *         input stream for deserialization.
     */
    @Serial
    private void readObject(final ObjectInputStream inputStream)
            throws java.io.IOException, ClassNotFoundException {
        inputStream.defaultReadObject();

        set(inputStream.readDouble());
    }
}
