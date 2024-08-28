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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

class StatefulSequentialOperatorTest {

    final StatefulSequentialOperator<List<String>> testOperator1 = new StatefulSequentialOperator<List<String>>() {

        @Override
        protected void doApply() {

            for (int i = 0; i < this.inputData.size(); i++) {
                this.inputData.set(i, this.inputData.get(i) + "_1");
            }
        }
    };

    final StatefulSequentialOperator<List<String>> testOperator2 = new StatefulSequentialOperator<List<String>>() {

        @Override
        protected void doApply() {

            for (int i = 0; i < this.inputData.size(); i++) {
                this.inputData.set(i, this.inputData.get(i) + "_2");
            }
        }
    };

    @Test
    void testOneOperator() {

        final List<String> data = new ArrayList<>(List.of("one", "two", "three"));

        final List<String> expected = List.of("one_1", "two_1", "three_1");

        this.testOperator1.value(data).apply();

        assertEquals(expected, data);
    }

    @Test
    void testNullInputData() {

        final List<String> data = null;

        assertDoesNotThrow(() -> this.testOperator1.value(data).apply());

        assertNull(data);
    }

    @Test
    void testMultipleOperator() {

        final List<String> data = new ArrayList<>(List.of("one", "two", "three"));

        final List<String> expected = List.of("one_1_2", "two_1_2", "three_1_2");

        testOperator1.value(data);
        testOperator2.value(data);

        testOperator1.then(testOperator2).apply();

        assertEquals(expected, data);
    }

    @Test
    void testMultipleOperator_confusingSyntax() {

        final List<String> data = new ArrayList<>(List.of("one", "two", "three"));

        final List<String> expected = List.of("one_1_2", "two_1_2", "three_1_2");

        testOperator1.value(data).then(testOperator2.value(data)).apply();

        assertEquals(expected, data);
    }

    @Test
    void testNoopOperator() {

        final String data = "foo";

        final String expected = "foo";

        StatefulSequentialOperator noop = StatefulSequentialOperator.noop();

        noop.value(data).apply();

        assertEquals(expected, data);
    }
}