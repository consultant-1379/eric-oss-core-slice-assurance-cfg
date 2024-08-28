/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.model.pmsc;

import static org.junit.jupiter.api.Assertions.*;

import com.ericsson.oss.air.exception.CsacValidationException;
import org.junit.jupiter.api.Test;

public class AggregationPeriodTest {

    @Test
    void getValueTest() {
        assertEquals(15, AggregationPeriod.FIFTEEN.getValue());
        assertEquals(60, AggregationPeriod.SIXTY.getValue());
        assertEquals(1440, AggregationPeriod.ONE_THOUSAND_FOUR_HUNDRED_FORTY.getValue());
    }

    @Test
    void containsTest_positiveTest() {
        final int valueToTest = 60;
        assertTrue(AggregationPeriod.contains(valueToTest));
    }

    @Test
    void containsTest_negativeTest() {
        final int valueToTest = 10;
        assertFalse(AggregationPeriod.contains(valueToTest));
    }

    @Test
    void valueOfTest() {
        assertEquals(AggregationPeriod.FIFTEEN, AggregationPeriod.valueOf(15));
        assertEquals(AggregationPeriod.SIXTY, AggregationPeriod.valueOf(60));
        assertEquals(AggregationPeriod.ONE_THOUSAND_FOUR_HUNDRED_FORTY, AggregationPeriod.valueOf(1440));
    }

    @Test
    void valueOf_exceptionTest() {
        assertThrows(CsacValidationException.class, () -> AggregationPeriod.valueOf(999));
    }
}
