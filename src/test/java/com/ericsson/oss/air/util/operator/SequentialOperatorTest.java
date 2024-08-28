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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

class SequentialOperatorTest {

    private final List<String> testData = new ArrayList<>();

    private final SequentialOperator<List<String>> testOperator1 = new SequentialOperator<List<String>>() {
        @Override
        protected void doApply(List<String> stringList) {
            for (int i = 0; i < stringList.size(); i++) {
                stringList.set(i, stringList.get(i) + "_1");
            }
        }
    };

    private final SequentialOperator<List<String>> testOperator2 = new SequentialOperator<List<String>>() {
        @Override
        protected void doApply(List<String> stringList) {
            for (int i = 0; i < stringList.size(); i++) {
                stringList.set(i, stringList.get(i) + "_2");
            }
        }
    };

    private final SequentialOperator<List<String>> testOperator3 = new SequentialOperator<List<String>>() {
        @Override
        protected void doApply(List<String> stringList) {
            for (int i = 0; i < stringList.size(); i++) {
                stringList.set(i, stringList.get(i) + "_3");
            }
        }
    };

    @Test
    void singleOperator() {

        final List<String> data = new ArrayList<>(List.of("one", "two", "three"));

        final List<String> expected = List.of("one_1", "two_1", "three_1");

        this.testOperator1.apply(data);

        assertEquals(expected, data);
    }

    @Test
    void multipleOperators() {

        final List<String> data = new ArrayList<>(List.of("one", "two", "three"));

        final List<String> expected = List.of("one_1_2_3", "two_1_2_3", "three_1_2_3");

        this.testOperator1.then(this.testOperator2).then(this.testOperator3).apply(data);

        assertEquals(expected, data);
    }
}