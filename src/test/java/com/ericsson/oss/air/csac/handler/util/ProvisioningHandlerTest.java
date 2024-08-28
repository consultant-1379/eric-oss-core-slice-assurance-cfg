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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.ericsson.oss.air.util.operator.SequentialOperator;
import com.ericsson.oss.air.util.operator.StatefulSequentialOperator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ProvisioningHandlerTest {

    private List<String> dataStore = new ArrayList<>();

    class TestRollbackOperator extends StatefulSequentialOperator<List<String>> {

        @Override
        protected void doApply() {

            if (Objects.isNull(inputData) || inputData.isEmpty()) {
                ProvisioningHandlerTest.this.dataStore.clear();
                return;
            }

            for (int i = 0; i < inputData.size(); i++) {
                ProvisioningHandlerTest.this.dataStore.set(i, inputData.get(i));
            }
        }
    }

    private final ProvisioningOperator<List<String>, List<String>> testOperator1 = new ProvisioningOperator<List<String>, List<String>>() {

        @Override
        protected void doApply(List<String> strings) {

            setRollbackData(Collections.emptyList());

            for (int i = 0; i < strings.size(); i++) {
                ProvisioningHandlerTest.this.dataStore.add(strings.get(i) + "_1");
            }
        }
    };

    private final ProvisioningOperator<List<String>, List<String>> testOperator2 = new ProvisioningOperator<List<String>, List<String>>() {

        @Override
        protected void doApply(List<String> strings) {

            final List<String> rollbackData = new ArrayList<>(Collections.nCopies(ProvisioningHandlerTest.this.dataStore.size(), null));

            Collections.copy(rollbackData, ProvisioningHandlerTest.this.dataStore);

            setRollbackData(rollbackData);

            for (int i = 0; i < strings.size(); i++) {
                ProvisioningHandlerTest.this.dataStore.set(i, ProvisioningHandlerTest.this.dataStore.get(i) + "_" + strings.get(i) + "_2");
            }
        }
    };

    private final ProvisioningHandler<List<String>> handler1 = new ProvisioningHandler<List<String>>() {

        private final ProvisioningOperator<List<String>, List<String>> provisioningOperator = testOperator1;

        @Override
        protected StatefulSequentialOperator<?> getRollbackOperator() {
            return this.provisioningOperator.getRollbackOperator();
        }

        @Override
        protected void doApply(final List<String> stringList) {

            this.provisioningOperator.apply(stringList);
        }
    };

    private final ProvisioningHandler<List<String>> handler2 = new ProvisioningHandler<List<String>>() {

        private final ProvisioningOperator<List<String>, List<String>> provisioningOperator = testOperator2;

        @Override
        protected StatefulSequentialOperator<?> getRollbackOperator() {
            return this.provisioningOperator.getRollbackOperator();
        }

        @Override
        protected void doApply(List<String> stringList) {
            this.provisioningOperator.apply(stringList);
        }
    };

    @BeforeEach
    void setUp() {

        this.dataStore = new ArrayList<>();

        this.testOperator1.setRollbackOperator(new TestRollbackOperator());
        this.testOperator2.setRollbackOperator(new TestRollbackOperator());
    }

    @Test
    void testSingleHandler() {

        final List<String> data = List.of("one", "two", "three");

        this.handler1.apply(data);

        assertEquals(List.of("one_1", "two_1", "three_1"), this.dataStore);

        assertTrue(this.handler1.getIsHandlerComplete());

        // handler is complete.  Rollback should do nothing
        this.handler1.getRollback().apply();

        assertEquals(List.of("one_1", "two_1", "three_1"), this.dataStore);

        // force a rollback to ensure the correct rollback operator is constructed
        this.handler1.getRollbackOperator().apply();

        assertTrue(this.dataStore.isEmpty());
    }

    @Test
    void testCompositeHandler() {

        final List<String> data = List.of("one", "two", "three");

        final ProvisioningHandler<List<String>> handler = this.handler1.then(this.handler2);

        handler.apply(data);

        assertEquals(List.of("one_1_one_2", "two_1_two_2", "three_1_three_2"), this.dataStore);

        assertTrue(handler.getIsHandlerComplete());

        handler.getRollback().apply();
        assertEquals(List.of("one_1_one_2", "two_1_two_2", "three_1_three_2"), this.dataStore);

        // force the rollbacks one by one
        this.testOperator2.getRollback().apply();
        assertEquals(List.of("one_1", "two_1", "three_1"), this.dataStore);

        this.testOperator1.getRollback().apply();
        assertTrue(this.dataStore.isEmpty());

        // re-apply the handler
        handler.apply(data);
        assertEquals(List.of("one_1_one_2", "two_1_two_2", "three_1_three_2"), this.dataStore);

        // now force a full rollback
        handler.setIsHandlerComplete(false);
        handler.getRollback().apply();
        assertTrue(this.dataStore.isEmpty());
    }

    @Test
    void testInvalidHandler() {
        final SequentialOperator<List<String>> invalid = new SequentialOperator<List<String>>() {
            @Override
            protected void doApply(List<String> strings) {
                // do nothing
            }
        };

        assertThrows(UnsupportedOperationException.class, () -> this.handler1.then(invalid));
    }

}