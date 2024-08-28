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

import com.ericsson.oss.air.api.model.KpiRefsDto;
import com.ericsson.oss.air.api.model.ProfileDefinitionListDto;
import com.ericsson.oss.air.csac.model.InputMetricOverride;
import com.ericsson.oss.air.csac.model.KPIReference;
import com.ericsson.oss.air.csac.model.ProfileDefinition;
import com.ericsson.oss.air.csac.model.TestResourcesUtils;
import com.ericsson.oss.air.csac.repository.DeployedProfileDAO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class ProfileDefRequestHandlerTest {

    private static final KPIReference KPI_REFERENCE_WITH_AGG_PERIOD_OVERRIDE = KPIReference.builder()
            .aggregationPeriod(60)
            .build();

    private static final InputMetricOverride INPUT_METRIC_OVERRIDE_AGG_PERIOD = InputMetricOverride.builder()
            .id("input1")
            .aggregationPeriod(15)
            .build();

    private static final KPIReference KPI_REFERENCE_WITH_INPUT_METRIC_OVERRIDE = KPIReference.builder()
            .inputMetricOverrides(List.of(INPUT_METRIC_OVERRIDE_AGG_PERIOD))
            .build();

    private static final ProfileDefinition PROFILE_WITH_KPI_REF_OVERRIDE = ProfileDefinition.builder()
            .name("prof_with_ref_override")
            .context(List.of("context1"))
            .kpis(List.of(KPI_REFERENCE_WITH_AGG_PERIOD_OVERRIDE))
            .build();

    private static final ProfileDefinition PROFILE_WITH_KPI_INPUT_METRIC_OVERRIDE = ProfileDefinition.builder()
            .name("profile_with_kpi_input_metric_override")
            .context(List.of("context1"))
            .kpis(List.of(KPI_REFERENCE_WITH_INPUT_METRIC_OVERRIDE))
            .build();

    private static final List<KPIReference> VALID_KPI_REFERENCE_LIST = List.of(KPIReference.builder().ref("kpi_ref_1").build(),
            KPIReference.builder().ref("kpi_ref_2").build());

    private static final ProfileDefinition VALID_PROFILE_DEF_OBJ_2 = ProfileDefinition.builder()
            .name("profiledef_name_2")
            .description("profiledef decription 2")
            .context(List.of("field3", "field4"))
            .kpis(VALID_KPI_REFERENCE_LIST)
            .build();

    private static final List<ProfileDefinition> LIST_PROFILE_DEF_OBJ = List.of(TestResourcesUtils.VALID_PROFILE_DEF_OBJ, VALID_PROFILE_DEF_OBJ_2);

    @Autowired
    private ProfileDefRequestHandler profileDefRequestHandler;

    @MockBean
    private DeployedProfileDAO deployedProfileDAO;

    @Test
    void getProfileDefinitions_withKpiReferenceOverride() {

        final List<ProfileDefinition> allProfiles = List.of(PROFILE_WITH_KPI_REF_OVERRIDE);
        when(deployedProfileDAO.findAllProfileDefinitions(0, 10)).thenReturn(allProfiles);
        when(deployedProfileDAO.totalProfileDefinitions()).thenReturn(allProfiles.size());

        final ProfileDefinitionListDto actual = this.profileDefRequestHandler.getProfileDefinitions(0, 10);

        verify(deployedProfileDAO, times(1)).findAllProfileDefinitions(anyInt(), anyInt());

        assertEquals(1, actual.getCount());
        assertEquals(0, actual.getStart());
        assertEquals(10, actual.getRows());
        assertEquals(1, actual.getTotal());

        final KpiRefsDto dto = actual.getProfileDefs().get(0).getKpis().get(0);
        assertEquals(60, dto.getAggregationPeriod());

    }

    @Test
    void getProfileDefinitions_withKpiInputMetricOverride() {

        final List<ProfileDefinition> allProfiles = List.of(PROFILE_WITH_KPI_INPUT_METRIC_OVERRIDE);
        when(deployedProfileDAO.findAllProfileDefinitions(0, 10)).thenReturn(allProfiles);
        when(deployedProfileDAO.totalProfileDefinitions()).thenReturn(allProfiles.size());

        final ProfileDefinitionListDto actual = this.profileDefRequestHandler.getProfileDefinitions(0, 10);

        verify(deployedProfileDAO, times(1)).findAllProfileDefinitions(anyInt(), anyInt());

        assertEquals(1, actual.getCount());
        assertEquals(0, actual.getStart());
        assertEquals(10, actual.getRows());
        assertEquals(1, actual.getTotal());

        final KpiRefsDto dto = actual.getProfileDefs().get(0).getKpis().get(0);
        assertEquals(1, dto.getInputMetrics().size());
        assertEquals(15, dto.getInputMetrics().get(0).getAggregationPeriod());

    }

    @Test
    void getProfileDefinitions() {
        when(deployedProfileDAO.findAllProfileDefinitions(0, 2)).thenReturn(LIST_PROFILE_DEF_OBJ);
        when(deployedProfileDAO.totalProfileDefinitions()).thenReturn(LIST_PROFILE_DEF_OBJ.size());
        final ProfileDefinitionListDto profileDefinitionListDto = this.profileDefRequestHandler.getProfileDefinitions(0, 2);
        verify(deployedProfileDAO, times(1)).findAllProfileDefinitions(anyInt(), anyInt());

        assertEquals(2, profileDefinitionListDto.getTotal());
        assertEquals(2, profileDefinitionListDto.getCount());
        assertEquals(0, profileDefinitionListDto.getStart());
        assertEquals(2, profileDefinitionListDto.getRows());
        assertEquals("profiledef_name", profileDefinitionListDto.getProfileDefs().get(0).getName());
        assertEquals("profiledef_name_2", profileDefinitionListDto.getProfileDefs().get(1).getName());
        assertEquals("profiledef decription", profileDefinitionListDto.getProfileDefs().get(0).getDescription());
        assertEquals("profiledef decription 2", profileDefinitionListDto.getProfileDefs().get(1).getDescription());
        assertEquals(List.of("field1", "field2"), profileDefinitionListDto.getProfileDefs().get(0).getContext());
        assertEquals(List.of("field3", "field4"), profileDefinitionListDto.getProfileDefs().get(1).getContext());
        assertEquals(List.of(new KpiRefsDto().ref("kpi_simple_name"), new KpiRefsDto().ref("kpi_complex_name")),
                profileDefinitionListDto.getProfileDefs().get(0).getKpis());
        assertEquals(List.of(new KpiRefsDto().ref("kpi_ref_1"), new KpiRefsDto().ref("kpi_ref_2")),
                profileDefinitionListDto.getProfileDefs().get(1).getKpis());

    }

    @Test
    void getKpis_rowsGreaterThanTotal() {
        when(deployedProfileDAO.findAllProfileDefinitions(0, 10)).thenReturn(LIST_PROFILE_DEF_OBJ);
        when(deployedProfileDAO.totalProfileDefinitions()).thenReturn(LIST_PROFILE_DEF_OBJ.size());

        final ProfileDefinitionListDto profileDefinitionListDto = this.profileDefRequestHandler.getProfileDefinitions(0, 10);
        verify(deployedProfileDAO, times(1)).findAllProfileDefinitions(anyInt(), anyInt());

        assertEquals(0, profileDefinitionListDto.getStart());
        assertEquals(10, profileDefinitionListDto.getRows());
        assertEquals(2, profileDefinitionListDto.getCount());
        assertEquals(2, profileDefinitionListDto.getTotal());
    }

    @Test
    void getProfileDefs_NegativeRows_EmptyList() {
        when(deployedProfileDAO.findAllProfileDefinitions(0, -1)).thenReturn(new ArrayList<>());
        when(deployedProfileDAO.totalProfileDefinitions()).thenReturn(0);
        final ProfileDefinitionListDto profileDefinitionListDto = this.profileDefRequestHandler.getProfileDefinitions(0, -1);
        verify(deployedProfileDAO, times(1)).findAllProfileDefinitions(anyInt(), anyInt());
        assertEquals(0, profileDefinitionListDto.getTotal());
        assertEquals(0, profileDefinitionListDto.getCount());
        assertEquals(0, profileDefinitionListDto.getStart());
        assertEquals(-1, profileDefinitionListDto.getRows());
        assertEquals(0, profileDefinitionListDto.getProfileDefs().size());
    }
}
