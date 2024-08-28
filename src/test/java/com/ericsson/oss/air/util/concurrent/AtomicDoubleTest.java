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

import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.junit.jupiter.api.Test;
import org.springframework.util.ObjectUtils;

class AtomicDoubleTest {

    @Test
    void get() {

        final double expected = Math.PI;

        final AtomicDouble actual = new AtomicDouble(Math.PI);

        assertEquals(expected, actual.get());
    }

    @Test
    void set() {

        final double expected = Math.PI;

        final AtomicDouble actual = new AtomicDouble();

        assertEquals(0.0, actual.get());

        actual.set(Math.PI);

        assertEquals(expected, actual.get());
    }

    @Test
    void intValue() {

        final AtomicDouble actual = new AtomicDouble(Math.PI);

        assertEquals(3, actual.intValue());

    }

    @Test
    void longValue() {

        final AtomicDouble actual = new AtomicDouble(Math.PI);

        assertEquals(3L, actual.longValue());
    }

    @Test
    void floatValue() {

        final AtomicDouble actual = new AtomicDouble(Math.PI);

        assertEquals((float) Math.PI, actual.floatValue());
    }

    @Test
    void doubleValue() {

        final AtomicDouble actual = new AtomicDouble(Math.PI);

        assertEquals(Math.PI, actual.doubleValue());
    }

    @Test
    void getAndSet() {

        final AtomicDouble d = new AtomicDouble();

        final double expected = 0d;

        final double actual = d.getAndSet(Math.PI);

        assertEquals(expected, actual);

        assertEquals(Math.PI, d.get());
    }

    @Test
    void compareAndSet_success() {

        final AtomicDouble actual = new AtomicDouble(Math.PI);

        assertTrue(actual.compareAndSet(Math.PI, Math.E));

        assertEquals(Math.E, actual.get());
    }

    @Test
    void compareAndSet_failure() {

        final AtomicDouble actual = new AtomicDouble(0.0d);

        assertFalse(actual.compareAndSet(Math.PI, Math.E));

        assertEquals(0.0d, actual.get());
    }

    @Test
    void valueOf_double() {

        final double expected = Math.PI;

        final AtomicDouble actual = AtomicDouble.valueOf(Math.PI);

        assertEquals(expected, actual.get());
    }

    @Test
    void valueOf_string() {

        final String expectedStr = "3.14159";

        final double expected = 3.14159d;

        final AtomicDouble actual = AtomicDouble.valueOf(expectedStr);

        assertEquals(expected, actual.get());
    }

    @Test
    void valueOf_string_invalidNumber() throws Exception {

        assertThrows(NumberFormatException.class, () -> AtomicDouble.valueOf("definitely not a number"));
    }

    @Test
    void testToString() {

        final AtomicDouble actual = new AtomicDouble(3.14159d);

        assertEquals("3.14159", actual.toString());
    }

    @Test
    void testEquals() {

        final AtomicDouble pi1 = new AtomicDouble(Math.PI);
        final AtomicDouble pi2 = new AtomicDouble(Math.PI);
        final AtomicDouble e = new AtomicDouble(Math.E);

        assertNotEquals(System.identityHashCode(pi1), System.identityHashCode(pi2));
        final boolean expectedEquals = pi1.equals(pi2);
        assertTrue(expectedEquals);

        final boolean expectedNotEquals = pi1.equals(e);
        assertFalse(expectedNotEquals);
    }

    @Test
    void testHashCode() {

        final Object obj;

        final AtomicDouble pi1 = new AtomicDouble(Math.PI);
        final AtomicDouble pi2 = new AtomicDouble(Math.PI);
        final AtomicDouble e = new AtomicDouble(Math.E);

        assertEquals(pi1.hashCode(), pi2.hashCode());
        assertNotEquals(pi1.hashCode(), e.hashCode());
    }

    @Test
    void testSerializationDeserialization() throws IOException, ClassNotFoundException {

        final AtomicDouble pi = new AtomicDouble(Math.PI);

        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        final ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
        objectOutputStream.writeObject(pi);
        objectOutputStream.flush();
        objectOutputStream.close();

        final byte[] objectByteArray = byteArrayOutputStream.toByteArray();

        assertFalse(ObjectUtils.isEmpty(objectByteArray));

        final ObjectInputStream objectInputStream = new ObjectInputStream(new ByteArrayInputStream(objectByteArray));
        final AtomicDouble newPi = (AtomicDouble) objectInputStream.readObject();
        objectOutputStream.close();

        assertEquals(pi, newPi);

    }
}