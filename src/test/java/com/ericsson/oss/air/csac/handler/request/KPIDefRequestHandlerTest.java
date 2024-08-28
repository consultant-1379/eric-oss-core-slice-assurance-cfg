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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import com.ericsson.oss.air.api.model.KpiDefinitionListDto;
import com.ericsson.oss.air.csac.model.InputMetric;
import com.ericsson.oss.air.csac.model.KPIDefinition;
import com.ericsson.oss.air.csac.model.KPIReference;
import com.ericsson.oss.air.csac.model.ProfileDefinition;
import com.ericsson.oss.air.csac.repository.KPIDefinitionDAO;
import com.ericsson.oss.air.csac.repository.ProfileDefinitionDAO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class KPIDefRequestHandlerTest {

    @Autowired
    private KPIDefRequestHandler kpiDefRequestHandler;

    @MockBean
    private KPIDefinitionDAO kpiDefinitionDAO;

    @MockBean
    private ProfileDefinitionDAO profileDefinitionDAO;

    private static final List<InputMetric> INPUT_METRIC_PM = List.of(new InputMetric("id1", "p0", InputMetric.Type.PM_DATA));

    private static final List<InputMetric> INPUT_METRIC_KPI = List.of(new InputMetric("id2", "p2", InputMetric.Type.KPI));

    private static final List<KPIDefinition> KPI_DEFINITIONS = List.of(
            new KPIDefinition("name1", "description1", "displayName1", "expression1", "agg1", null, true, INPUT_METRIC_PM),
            new KPIDefinition("name2", "description2", "displayName2", "exp2", "agg2", null, false, INPUT_METRIC_KPI));

    private static final List<KPIReference> VALID_KPI_REFERENCE_LIST = List.of(
            KPIReference.builder().ref("name1").build(),
            KPIReference.builder().ref("name2").build()
    );

    private static final ProfileDefinition VALID_PROFILE_DEF_OBJ = ProfileDefinition.builder()
            .name("profiledef_name")
            .description("profiledef decription")
            .context(List.of("field1", "field2"))
            .kpis(VALID_KPI_REFERENCE_LIST)
            .build();

    @Test
    void getKPIDefinitions() {
        when(this.kpiDefinitionDAO.findAllKPIDefs(0, 2)).thenReturn(KPI_DEFINITIONS);
        when(this.kpiDefinitionDAO.totalKPIDefinitions()).thenReturn(KPI_DEFINITIONS.size());
        when(this.profileDefinitionDAO.findAll()).thenReturn(List.of(VALID_PROFILE_DEF_OBJ));

        final KpiDefinitionListDto kpiDefinitionListDto = this.kpiDefRequestHandler.getKPIDefinitions(0, 2);
        verify(this.kpiDefinitionDAO, times(1)).findAllKPIDefs(anyInt(), anyInt());
        verify(this.profileDefinitionDAO, times(1)).findAll();

        assertEquals(2, kpiDefinitionListDto.getTotal());
        assertEquals(2, kpiDefinitionListDto.getCount());
        assertEquals(0, kpiDefinitionListDto.getStart());
        assertEquals(2, kpiDefinitionListDto.getRows());
        assertEquals("name1", kpiDefinitionListDto.getKpiDefs().get(0).getName());
        assertEquals("name2", kpiDefinitionListDto.getKpiDefs().get(1).getName());
        assertEquals("description1", kpiDefinitionListDto.getKpiDefs().get(0).getDescription());
        assertEquals("description2", kpiDefinitionListDto.getKpiDefs().get(1).getDescription());
        assertEquals("displayName1", kpiDefinitionListDto.getKpiDefs().get(0).getDisplayName());
        assertEquals("displayName2", kpiDefinitionListDto.getKpiDefs().get(1).getDisplayName());
        assertEquals("expression1", kpiDefinitionListDto.getKpiDefs().get(0).getExpression());
        assertEquals("exp2", kpiDefinitionListDto.getKpiDefs().get(1).getExpression());
        assertEquals("agg1", kpiDefinitionListDto.getKpiDefs().get(0).getAggregationType());
        assertEquals("agg2", kpiDefinitionListDto.getKpiDefs().get(1).getAggregationType());
        assertEquals(true, kpiDefinitionListDto.getKpiDefs().get(0).getIsVisible());
        assertEquals(false, kpiDefinitionListDto.getKpiDefs().get(1).getIsVisible());
        assertEquals(1, kpiDefinitionListDto.getKpiDefs().get(0).getInputMetrics().size());
        assertEquals(1, kpiDefinitionListDto.getKpiDefs().get(0).getProfiles().size());
        assertEquals("profiledef_name", kpiDefinitionListDto.getKpiDefs().get(0).getProfiles().get(0));
        assertEquals(1, kpiDefinitionListDto.getKpiDefs().get(1).getProfiles().size());
        assertEquals("profiledef_name", kpiDefinitionListDto.getKpiDefs().get(1).getProfiles().get(0));
    }

    @Test
    void getKpis_rowsGreaterThanTotal() {
        when(kpiDefinitionDAO.findAllKPIDefs(0, 10)).thenReturn(KPI_DEFINITIONS);
        when(kpiDefinitionDAO.totalKPIDefinitions()).thenReturn(KPI_DEFINITIONS.size());

        final KpiDefinitionListDto kpiDefinitionListDto = this.kpiDefRequestHandler.getKPIDefinitions(0, 10);
        verify(kpiDefinitionDAO, times(1)).findAllKPIDefs(anyInt(), anyInt());

        assertEquals(0, kpiDefinitionListDto.getStart());
        assertEquals(10, kpiDefinitionListDto.getRows());
        assertEquals(2, kpiDefinitionListDto.getCount());
        assertEquals(2, kpiDefinitionListDto.getTotal());
    }

    @Test
    void getKpis_NegativeRows_EmptyList() {
        when(kpiDefinitionDAO.findAllKPIDefs(0, -1)).thenReturn(new ArrayList<>());
        when(kpiDefinitionDAO.totalKPIDefinitions()).thenReturn(0);
        final KpiDefinitionListDto kpiDefinitionListDto = this.kpiDefRequestHandler.getKPIDefinitions(0, -1);
        verify(kpiDefinitionDAO, times(1)).findAllKPIDefs(anyInt(), anyInt());
        assertEquals(0, kpiDefinitionListDto.getTotal());
        assertEquals(0, kpiDefinitionListDto.getCount());
        assertEquals(0, kpiDefinitionListDto.getStart());
        assertEquals(-1, kpiDefinitionListDto.getRows());
        assertEquals(0, kpiDefinitionListDto.getKpiDefs().size());
    }

}
