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

import com.ericsson.oss.air.csac.model.pmsc.KpiDefinitionDTO;
import com.ericsson.oss.air.csac.model.pmsc.KpiTypeEnum;
import org.apache.logging.log4j.util.Strings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RuntimeKpiDefinitionTest {

    private RuntimeKpiDefinition testRtKpiDef;

    @BeforeEach
    public void setUp() throws Exception {
        this.testRtKpiDef = KpiDefinitionDTO.builder()
                .withKpiType(KpiTypeEnum.SIMPLE)
                .withAlias("csac_complex_snssai")
                .withAggregationPeriod(15)
                .build();
    }

    @Test
    void getFactTableName() {

        assertEquals("kpi_csac_complex_snssai_15", testRtKpiDef.getFactTableName());
    }

    @Test
    void getKpiType() {

        assertEquals(KpiTypeEnum.SIMPLE, testRtKpiDef.getKpiType());
    }

    @Test
    void getFactTableName_default() {

        final RuntimeKpiDefinition actual = new RuntimeKpiDefinition() {
            @Override
            public KpiTypeEnum getKpiType() {
                return KpiTypeEnum.SIMPLE;
            }
        };

        assertTrue(Strings.isEmpty(actual.getFactTableName()));
    }
}