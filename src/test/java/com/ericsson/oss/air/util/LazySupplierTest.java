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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class LazySupplierTest {

    private final AtomicInteger counter = new AtomicInteger(0);

    private final LazySupplier<String> testSupplier = new LazySupplier<String>() {
        @Override
        protected String initialize() {
            return "test" + counter.incrementAndGet();
        }
    };

    @AfterEach
    void tearDown() {
        this.testSupplier.reset();
    }

    @Test
    void testGet() {
        assertEquals("test1", this.testSupplier.get());
    }

    @Test
    void testReset() {

        assertEquals("test1", this.testSupplier.get());

        this.testSupplier.reset();

        assertEquals("test2", this.testSupplier.get());

    }

    @Test
    void testAutoClose() {

        try (final LazySupplier<String> supplier = this.testSupplier) {
            assertEquals("test1", supplier.get());
        }

        assertTrue(this.testSupplier.isClosed());
    }

    @Test
    void testClose() {

        assertEquals("test1", this.testSupplier.get());

        this.testSupplier.close();

        assertTrue(this.testSupplier.isClosed());

        assertEquals("test2", this.testSupplier.get());

    }

}