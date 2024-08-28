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

import static com.ericsson.oss.air.csac.model.TestResourcesUtils.VALID_PM_DEF_NEW_OBJ;
import static com.ericsson.oss.air.csac.model.TestResourcesUtils.VALID_PM_DEF_OBJ;
import static com.ericsson.oss.air.csac.model.TestResourcesUtils.VALID_SIMPLE_KPI_DEF_NAME;
import static com.ericsson.oss.air.csac.model.TestResourcesUtils.VALID_SIMPLE_KPI_DEF_NAME_NEW;
import static com.ericsson.oss.air.csac.model.TestResourcesUtils.VALID_SIMPLE_KPI_DEF_OBJ;
import static com.ericsson.oss.air.csac.model.TestResourcesUtils.VALID_SIMPLE_KPI_DEF_OBJ_NEW;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.ericsson.oss.air.api.model.PmDefinitionListDto;
import com.ericsson.oss.air.csac.model.PMDefinition;
import com.ericsson.oss.air.csac.repository.KPIDefinitionDAO;
import com.ericsson.oss.air.csac.repository.PMDefinitionDAO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class PMDefRequestHandlerTest {

    private static final PMDefinition PMDEF_ONE = new PMDefinition("nameOne", "sourceOne", "descOne");

    private static final PMDefinition PMDEF_TWO = new PMDefinition("nameTwo", "sourceTwo", "descTwo");

    private static final List<PMDefinition> PM_DEFINITIONS = List.of(PMDEF_ONE, PMDEF_TWO);

    @MockBean
    private PMDefinitionDAO pmDefDAO;

    @MockBean
    private KPIDefinitionDAO kpiDefinitionDAO;

    @Autowired
    private PMDefRequestHandler pmDefReqHandler;

    @Test
    void getPMDefinitions() {
        when(pmDefDAO.findAllPMDefinitions(0, 2)).thenReturn(PM_DEFINITIONS);
        when(pmDefDAO.totalPMDefinitions()).thenReturn(PM_DEFINITIONS.size());

        final PmDefinitionListDto pmDefinitionDtoList = this.pmDefReqHandler.getPMDefinitions(0, 2);
        verify(pmDefDAO, times(1)).findAllPMDefinitions(anyInt(), anyInt());

        assertEquals(0, pmDefinitionDtoList.getStart());
        assertEquals(2, pmDefinitionDtoList.getRows());
        assertEquals(2, pmDefinitionDtoList.getCount());
        assertEquals(2, pmDefinitionDtoList.getTotal());

        assertEquals("nameOne", pmDefinitionDtoList.getPmDefs().get(0).getName());
        assertEquals("sourceOne", pmDefinitionDtoList.getPmDefs().get(0).getSource());
        assertEquals("descOne", pmDefinitionDtoList.getPmDefs().get(0).getDescription());

        assertEquals("nameTwo", pmDefinitionDtoList.getPmDefs().get(1).getName());
        assertEquals("sourceTwo", pmDefinitionDtoList.getPmDefs().get(1).getSource());
        assertEquals("descTwo", pmDefinitionDtoList.getPmDefs().get(1).getDescription());
    }

    @Test
    void getPMDefinitions_rowsGreaterThanTotal() {
        when(pmDefDAO.findAllPMDefinitions(0, 10)).thenReturn(PM_DEFINITIONS);
        when(pmDefDAO.totalPMDefinitions()).thenReturn(PM_DEFINITIONS.size());

        final PmDefinitionListDto pmDefinitionDtoList = this.pmDefReqHandler.getPMDefinitions(0, 10);
        verify(pmDefDAO, times(1)).findAllPMDefinitions(anyInt(), anyInt());

        assertEquals(0, pmDefinitionDtoList.getStart());
        assertEquals(10, pmDefinitionDtoList.getRows());
        assertEquals(2, pmDefinitionDtoList.getCount());
        assertEquals(2, pmDefinitionDtoList.getTotal());
    }

    @Test
    void getPmdefs_NegativeRows_EmptyList() {
        when(pmDefDAO.findAllPMDefinitions(0, -1)).thenReturn(new ArrayList<>());
        when(pmDefDAO.totalPMDefinitions()).thenReturn(0);
        final PmDefinitionListDto pmDefinitionListDto = this.pmDefReqHandler.getPMDefinitions(0, -1);
        verify(pmDefDAO, times(1)).findAllPMDefinitions(anyInt(), anyInt());
        assertEquals(0, pmDefinitionListDto.getTotal());
        assertEquals(0, pmDefinitionListDto.getCount());
        assertEquals(0, pmDefinitionListDto.getStart());
        assertEquals(-1, pmDefinitionListDto.getRows());
        assertEquals(0, pmDefinitionListDto.getPmDefs().size());
    }

    @Test
    void getKpiNames_success_onePmDef_oneKpi() {
        when(kpiDefinitionDAO.getAffectedKPIDefs(Set.of(VALID_PM_DEF_OBJ))).thenReturn(Set.of(VALID_SIMPLE_KPI_DEF_OBJ));

        final List<String> kpiNames = this.pmDefReqHandler.getKpiNames(VALID_PM_DEF_OBJ);

        assertEquals(1, kpiNames.size());
        assertThat(kpiNames).hasSameElementsAs(Set.of(VALID_SIMPLE_KPI_DEF_NAME));
    }

    @Test
    void getKpiNames_success_onePmDef_twoKpi() {
        when(kpiDefinitionDAO.getAffectedKPIDefs(Set.of(VALID_PM_DEF_NEW_OBJ))).thenReturn(
                Set.of(VALID_SIMPLE_KPI_DEF_OBJ, VALID_SIMPLE_KPI_DEF_OBJ_NEW));

        final List<String> kpiNames = this.pmDefReqHandler.getKpiNames(VALID_PM_DEF_NEW_OBJ);

        assertEquals(2, kpiNames.size());
        assertThat(kpiNames).hasSameElementsAs(Set.of(VALID_SIMPLE_KPI_DEF_NAME, VALID_SIMPLE_KPI_DEF_NAME_NEW));
    }

}