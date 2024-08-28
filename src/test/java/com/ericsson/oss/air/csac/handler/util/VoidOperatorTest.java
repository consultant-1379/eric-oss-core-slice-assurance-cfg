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

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class VoidOperatorTest {

    private final List<Integer> operatorMemo = new ArrayList<>();

    private final VoidOperator op1 = new VoidOperator() {

        @Override
        public void apply() {
            operatorMemo.add(1);
        }
    };

    private final VoidOperator op2 = new VoidOperator() {

        @Override
        public void apply() {
            operatorMemo.add(2);
        }
    };

    private final VoidOperator op3 = new VoidOperator() {

        @Override
        public void apply() {
            operatorMemo.add(3);
        }
    };

    @BeforeEach
    void setUp() {
        this.operatorMemo.clear();
    }

    @Test
    void apply() {

        this.op1.apply();

        assertEquals(1, this.operatorMemo.size());
    }

    @Test
    void andThen() {

        this.op1.andThen(op2).andThen(op3).apply();

        assertEquals(3, this.operatorMemo.size());
        assertEquals(1, this.operatorMemo.get(0));
        assertEquals(2, this.operatorMemo.get(1));
        assertEquals(3, this.operatorMemo.get(2));
    }
}