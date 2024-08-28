/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.handler.request;

import com.ericsson.oss.air.api.model.RtKpiInstanceDto;
import com.ericsson.oss.air.api.model.RtKpiInstanceListDto;
import com.ericsson.oss.air.csac.model.pmsc.KpiDefinitionDTO;
import com.ericsson.oss.air.csac.model.runtime.RuntimeKpiInstance;
import com.ericsson.oss.air.csac.repository.DeployedKpiDefDAO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
class RuntimeKpiRequestHandlerTest {

    @Autowired
    private RuntimeKpiRequestHandler runtimeKpiRequestHandler;

    @MockBean
    private DeployedKpiDefDAO deployedKpiDefDAO;

    private final KpiDefinitionDTO kpi1 = KpiDefinitionDTO.builder()
            .withName("csac_aaaa_1111")
            .withAggregationElements(List.of("fact_table_1.snssai"))
            .withAggregationPeriod(15)
            .build();

    private final KpiDefinitionDTO kpi2 = KpiDefinitionDTO.builder()
            .withName("csac_bbbb_2222")
            .withAggregationElements(List.of("fact_table_1.snssai"))
            .withAggregationPeriod(15)
            .withExecutionGroup("csac_test")
            .build();

    private final RuntimeKpiInstance rtKpi1 = RuntimeKpiInstance.builder()
            .withInstanceId("csac_aaaa_1111")
            .withContextFieldList(List.of("snssai"))
            .withKpDefinitionName("kpi1")
            .withRuntimeDefinition(kpi1)
            .build();

    private final RuntimeKpiInstance rtKpi2 = RuntimeKpiInstance.builder()
            .withInstanceId("csac_bbbb_2222")
            .withContextFieldList(List.of("snssai"))
            .withKpDefinitionName("kpi2")
            .withRuntimeDefinition(kpi2)
            .build();

    private final List<RuntimeKpiInstance> runtimeKpiInstanceList = List.of(rtKpi1, rtKpi2);

    @Test
    void getRuntimeKpiDefinitions() {

        when(this.deployedKpiDefDAO.findAllRuntimeKpis(0, 2)).thenReturn(this.runtimeKpiInstanceList);
        when(this.deployedKpiDefDAO.totalDeployedKpiDefinitions()).thenReturn(2);

        final RtKpiInstanceListDto actual = this.runtimeKpiRequestHandler.getRuntimeKpiDefinitions(0, 2);

        assertEquals(2, actual.getTotal());
        assertEquals(2, actual.getCount());
        assertEquals(0, actual.getStart());
        assertEquals(2, actual.getRows());
        assertEquals(2, actual.getKpiDefs().size());

        assertEquals(RtKpiInstanceDto.KpiTypeEnum.SIMPLE, actual.getKpiDefs().get(0).getKpiType());
        assertEquals(RtKpiInstanceDto.KpiTypeEnum.COMPLEX, actual.getKpiDefs().get(1).getKpiType());
    }
}