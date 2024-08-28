/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.handler.request;

import static com.ericsson.oss.air.csac.model.TestResourcesUtils.DEPLOYED_SIMPLE_KPI_NAME;
import static com.ericsson.oss.air.csac.model.TestResourcesUtils.DEPLOYED_SIMPLE_KPI_OBJ;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;

import com.ericsson.oss.air.api.model.RtContextMetadataDto;
import com.ericsson.oss.air.api.model.RtKpiMetadataDto;
import com.ericsson.oss.air.csac.model.KPIDefinition;
import com.ericsson.oss.air.csac.model.pmsc.KPIDefinitionDTOMapping;
import com.ericsson.oss.air.csac.model.pmsc.KpiDefinitionDTO;
import com.ericsson.oss.air.csac.model.runtime.RuntimeKpiInstance;
import com.ericsson.oss.air.csac.model.runtime.metadata.KpiContextId;
import com.ericsson.oss.air.csac.repository.DeployedKpiDefDAO;
import com.ericsson.oss.air.csac.repository.KPIDefinitionDAO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
public class RuntimeMetadataRequestHandlerTest {

    @MockBean
    private DeployedKpiDefDAO deployedKpiDefDAO;

    @MockBean
    private KPIDefinitionDAO kpiDefinitionDAO;

    @Autowired
    private RuntimeMetadataRequestHandler runtimeMetadataRequestHandler;

    private final KpiDefinitionDTO kpi1 = KpiDefinitionDTO.builder()
            .withName("csac_aaaa_1111")
            .withAggregationElements(List.of("fact_table_1.snssai"))
            .withAggregationPeriod(15)
            .withIsVisible(true)
            .build();

    private final KpiDefinitionDTO kpi2 = KpiDefinitionDTO.builder()
            .withName("csac_bbbb_2222")
            .withAggregationElements(List.of("fact_table_1.snssai"))
            .withAggregationPeriod(15)
            .withIsVisible(true)
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

    @Test
    void test_getContextKpiMetadata() {

        final RuntimeKpiInstance runtimeKpiInstance = new RuntimeKpiInstance();
        runtimeKpiInstance.setKpDefinitionName("KpiDefName");
        runtimeKpiInstance.setInstanceId("instanceId");

        final KPIDefinition kpiDefinition = new KPIDefinition();
        kpiDefinition.setName("name");
        kpiDefinition.setDisplayName("displayName");
        kpiDefinition.setDescription("description");

        when(this.deployedKpiDefDAO.findAllByContextId(any(KpiContextId.class), eq(true)))
                .thenReturn(List.of(runtimeKpiInstance));
        when(this.kpiDefinitionDAO.findByKPIDefName(anyString())).thenReturn(kpiDefinition);

        final List<RtKpiMetadataDto> metaDataDtoList = this.runtimeMetadataRequestHandler.getContextKpiMetadata(KpiContextId.of(Set.of("mock")));
        assertEquals(1, metaDataDtoList.size());
        assertEquals(KPIDefinitionDTOMapping.OBJECT_TYPE, metaDataDtoList.get(0).getType());

    }

    @Test
    void getContextMetadata_sameContext() {
        when(this.deployedKpiDefDAO.findAllRuntimeKpis(true)).thenReturn(List.of(rtKpi1, rtKpi2));

        final List<RtContextMetadataDto> contextMetadata = this.runtimeMetadataRequestHandler.getContextMetadata();

        assertEquals(1, contextMetadata.size());
        assertEquals("snssai", contextMetadata.get(0).getId());
    }

    @Test
    void getContextMetadata_differentContexts() {
        final RuntimeKpiInstance rtKpi = RuntimeKpiInstance.builder().withInstanceId("InstanceId").withRuntimeDefinition(DEPLOYED_SIMPLE_KPI_OBJ)
                .withKpDefinitionName(DEPLOYED_SIMPLE_KPI_NAME).withContextFieldList(List.of("field1", "field2")).build();

        when(this.deployedKpiDefDAO.findAllRuntimeKpis(true)).thenReturn(List.of(rtKpi, rtKpi1));
        final List<RtContextMetadataDto> contextMetadata = this.runtimeMetadataRequestHandler.getContextMetadata();

        assertEquals(2, contextMetadata.size());

    }
}

