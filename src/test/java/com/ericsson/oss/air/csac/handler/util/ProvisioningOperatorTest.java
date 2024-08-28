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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.ericsson.oss.air.util.operator.SequentialOperator;
import com.ericsson.oss.air.util.operator.StatefulSequentialOperator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ProvisioningOperatorTest {

    private List<String> dataStore = new ArrayList<>();

    class TestRollbackOperator extends StatefulSequentialOperator<List<String>> {

        @Override
        protected void doApply() {

            if (Objects.isNull(inputData) || inputData.isEmpty()) {
                ProvisioningOperatorTest.this.dataStore.clear();
                return;
            }

            for (int i = 0; i < inputData.size(); i++) {
                ProvisioningOperatorTest.this.dataStore.set(i, inputData.get(i));
            }
        }
    }

    final ProvisioningOperator<List<String>, List<String>> testOperator1 = new ProvisioningOperator<List<String>, List<String>>() {

        @Override
        protected void doApply(final List<String> stringList) {

            setRollbackData(Collections.emptyList());

            for (int i = 0; i < stringList.size(); i++) {
                ProvisioningOperatorTest.this.dataStore.add(stringList.get(i) + "_1");
            }
        }
    };

    final ProvisioningOperator<List<String>, List<String>> testOperator2 = new ProvisioningOperator<List<String>, List<String>>() {

        @Override
        protected void doApply(final List<String> stringList) {

            final List<String> rollbackData = new ArrayList<>(stringList);

            Collections.copy(stringList, rollbackData);

            setRollbackData(rollbackData);

            for (int i = 0; i < stringList.size(); i++) {
                ProvisioningOperatorTest.this.dataStore.set(i, stringList.get(i) + "_2");
            }
        }
    };

    @BeforeEach
    void setUp() {

        this.dataStore = new ArrayList<>();

        this.testOperator1.setRollbackOperator(new TestRollbackOperator());
        this.testOperator2.setRollbackOperator(new TestRollbackOperator());
    }

    @Test
    void testSingleOperator() {

        final List<String> data = new ArrayList<>(List.of("one", "two", "three"));

        final List<String> expected = List.of("one_1", "two_1", "three_1");

        testOperator1.apply(data);

        assertEquals(expected, this.dataStore);

        testOperator1.getRollback().apply();

        assertTrue(this.dataStore.isEmpty());

    }

    @Test
    void testMultipleOperator_individualRollbacks() {

        final List<String> data = new ArrayList<>(List.of("one", "two", "three"));

        final List<String> expected = List.of("one_2", "two_2", "three_2");

        final ProvisioningOperator<List<String>, List<String>> operator = testOperator1.then(testOperator2);

        operator.apply(data);

        assertEquals(expected, this.dataStore);

        testOperator2.getRollbackOperator().apply();

        assertEquals(data, this.dataStore);

        testOperator1.getRollbackOperator().apply();

        assertTrue(this.dataStore.isEmpty());

    }

    @Test
    void testMultipleOperator_compositeRollback() {

        final List<String> data = new ArrayList<>(List.of("one", "two", "three"));

        final List<String> expected = List.of("one_2", "two_2", "three_2");

        final ProvisioningOperator<List<String>, List<String>> operator = testOperator1.then(testOperator2);

        operator.apply(data);

        assertEquals(expected, this.dataStore);

        operator.getRollback().apply();

        assertTrue(this.dataStore.isEmpty());

    }

    @Test
    void testInvalidOperator() {

        final SequentialOperator<List<String>> invalid = new SequentialOperator<List<String>>() {
            @Override
            protected void doApply(List<String> strings) {
                // do nothing
            }
        };

        assertThrows(UnsupportedOperationException.class, () -> testOperator1.then(invalid));
    }

}