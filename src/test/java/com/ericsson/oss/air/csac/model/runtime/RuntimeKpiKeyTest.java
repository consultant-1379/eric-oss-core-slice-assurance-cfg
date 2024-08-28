/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.model.runtime;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import com.ericsson.oss.air.csac.model.pmsc.AggregationPeriod;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class RuntimeKpiKeyTest {

    private RuntimeKpiKey testRtKpiKey;

    @BeforeEach
    public void setUp() {
        this.testRtKpiKey = RuntimeKpiKey.builder().withKpDefinitionName("testKpi").withAggregationFields(List.of("field1"))
                .withAggregationPeriod(AggregationPeriod.FIFTEEN.getValue()).build();
    }

    @Test
    void getKpiDefinitionName() {
        assertEquals("testKpi", this.testRtKpiKey.getKpDefinitionName());
    }

    @Test
    void getAggregationFields() {
        assertEquals("field1", this.testRtKpiKey.getAggregationFields().get(0));
    }

    @Test
    void getAggregationPeriod() {
        assertEquals(AggregationPeriod.FIFTEEN.getValue(), this.testRtKpiKey.getAggregationPeriod());
    }
}
