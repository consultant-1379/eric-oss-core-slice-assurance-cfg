/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.repository.impl.inmemorydb;

import static com.ericsson.oss.air.csac.model.TestResourcesUtils.DEPLOYED_COMPLEX_KPI_OBJ;
import static com.ericsson.oss.air.csac.model.TestResourcesUtils.DEPLOYED_SIMPLE_KPI_NAME_2;
import static com.ericsson.oss.air.csac.model.TestResourcesUtils.DEPLOYED_SIMPLE_KPI_OBJ;
import static com.ericsson.oss.air.csac.model.TestResourcesUtils.DEPLOYED_SIMPLE_KPI_OBJ_2;
import static com.ericsson.oss.air.csac.model.TestResourcesUtils.VALID_COMPLEX_KPI_DEF_NAME;
import static com.ericsson.oss.air.csac.model.TestResourcesUtils.VALID_PROFILE_DEF_AGGREGATION_FIELD_LIST;
import static com.ericsson.oss.air.csac.model.TestResourcesUtils.VALID_PROFILE_DEF_OBJ;
import static com.ericsson.oss.air.csac.model.TestResourcesUtils.VALID_SIMPLE_KPI_DEF_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.ericsson.oss.air.csac.model.TestResourcesUtils;
import com.ericsson.oss.air.csac.model.pmsc.KpiDefinitionDTO;
import com.ericsson.oss.air.csac.model.runtime.RuntimeKpiInstance;
import com.ericsson.oss.air.csac.model.runtime.metadata.KpiContextId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DeployedKpiDefDAOImpTest {

    private DeployedKpiDefDAOImp deployedKpiDefDAO;

    @BeforeEach
    void setUp() {
        this.deployedKpiDefDAO = new DeployedKpiDefDAOImp();
    }

    @AfterEach
    void tearDown() {
        this.deployedKpiDefDAO = null;
    }

    @Test
    void createDeployedKpiTest() {
        this.deployedKpiDefDAO.createDeployedKpi(DEPLOYED_SIMPLE_KPI_OBJ, "kpidName1", VALID_PROFILE_DEF_OBJ);
        assertEquals(1, this.deployedKpiDefDAO.getAllDeployedKpis().size());

        this.deployedKpiDefDAO.createDeployedKpi(DEPLOYED_COMPLEX_KPI_OBJ, "kpidName2", VALID_PROFILE_DEF_OBJ);
        assertEquals(2, this.deployedKpiDefDAO.getAllDeployedKpis().size());
    }

    @Test
    void createDeployedKpiTestWithAggregationFields() {
        this.deployedKpiDefDAO.createDeployedKpi(DEPLOYED_SIMPLE_KPI_OBJ, "kpidName1", List.of("field"));
        assertEquals(1, this.deployedKpiDefDAO.getAllDeployedKpis().size());

        this.deployedKpiDefDAO.createDeployedKpi(DEPLOYED_COMPLEX_KPI_OBJ, "kpidName2", List.of("field"));
        assertEquals(2, this.deployedKpiDefDAO.getAllDeployedKpis().size());
    }

    @Test
    void updateDeployedKpiTest() {
        KpiDefinitionDTO kpi = DEPLOYED_COMPLEX_KPI_OBJ.toBuilder()
                .withIsVisible(true)
                .build();
        this.deployedKpiDefDAO.createDeployedKpi(kpi, "kpidName1", VALID_PROFILE_DEF_OBJ);

        assertEquals(1, this.deployedKpiDefDAO.getAllDeployedKpis().size());
        assertEquals(true, this.deployedKpiDefDAO.getDeployedKpi(kpi.getName()).getIsVisible());

        kpi = DEPLOYED_COMPLEX_KPI_OBJ.toBuilder()
                .withIsVisible(false)
                .build();
        this.deployedKpiDefDAO.updateDeployedKpi(kpi);

        assertEquals(1, this.deployedKpiDefDAO.getAllDeployedKpis().size());
        assertEquals(false, this.deployedKpiDefDAO.getDeployedKpi(kpi.getName()).getIsVisible());
    }

    @Test
    void deleteDeployedKpiTest() {
        this.deployedKpiDefDAO.createDeployedKpi(DEPLOYED_COMPLEX_KPI_OBJ, "kpidName1", VALID_PROFILE_DEF_OBJ);
        assertEquals(1, this.deployedKpiDefDAO.getAllDeployedKpis().size());

        this.deployedKpiDefDAO.deleteDeployedKpi(DEPLOYED_COMPLEX_KPI_OBJ.getName());
        assertEquals(0, this.deployedKpiDefDAO.getAllDeployedKpis().size());
    }

    @Test
    void getDeployedKpiTest() {
        this.deployedKpiDefDAO.createDeployedKpi(DEPLOYED_COMPLEX_KPI_OBJ, "kpidName1", VALID_PROFILE_DEF_OBJ);
        assertEquals(1, this.deployedKpiDefDAO.getAllDeployedKpis().size());

        final KpiDefinitionDTO kpi = this.deployedKpiDefDAO.getDeployedKpi(DEPLOYED_COMPLEX_KPI_OBJ.getName());
        assertEquals(DEPLOYED_COMPLEX_KPI_OBJ, kpi);
    }

    @Test
    void getDeployedKpiByProfileTest() {
        List<KpiDefinitionDTO> deployedKpiByProfile = this.deployedKpiDefDAO.getDeployedKpiByProfile(VALID_PROFILE_DEF_OBJ);
        assertEquals(0, deployedKpiByProfile.size());

        this.deployedKpiDefDAO.createDeployedKpi(DEPLOYED_SIMPLE_KPI_OBJ, VALID_SIMPLE_KPI_DEF_NAME, VALID_PROFILE_DEF_OBJ);
        this.deployedKpiDefDAO.createDeployedKpi(DEPLOYED_COMPLEX_KPI_OBJ, VALID_COMPLEX_KPI_DEF_NAME, VALID_PROFILE_DEF_OBJ);

        deployedKpiByProfile = this.deployedKpiDefDAO.getDeployedKpiByProfile(VALID_PROFILE_DEF_OBJ);
        assertEquals(2, deployedKpiByProfile.size());
    }

    @Test
    void getDeployedKpiByDefinitionNameTest() {
        this.deployedKpiDefDAO.createDeployedKpi(DEPLOYED_SIMPLE_KPI_OBJ, VALID_SIMPLE_KPI_DEF_NAME, VALID_PROFILE_DEF_OBJ);
        this.deployedKpiDefDAO.createDeployedKpi(DEPLOYED_COMPLEX_KPI_OBJ, VALID_COMPLEX_KPI_DEF_NAME, VALID_PROFILE_DEF_OBJ);

        final List<KpiDefinitionDTO> kpis = this.deployedKpiDefDAO.getDeployedKpiByDefinitionName(VALID_SIMPLE_KPI_DEF_NAME);
        assertEquals(1, kpis.size());
        assertEquals(TestResourcesUtils.DEPLOYED_SIMPLE_KPI_NAME, kpis.get(0).getName());
    }

    @Test
    void getDeployedKpiByAggregationTest() {
        this.deployedKpiDefDAO.createDeployedKpi(DEPLOYED_SIMPLE_KPI_OBJ, VALID_SIMPLE_KPI_DEF_NAME, VALID_PROFILE_DEF_OBJ);
        this.deployedKpiDefDAO.createDeployedKpi(DEPLOYED_COMPLEX_KPI_OBJ, VALID_COMPLEX_KPI_DEF_NAME, VALID_PROFILE_DEF_OBJ);

        final KpiDefinitionDTO noMatchedKpi = this.deployedKpiDefDAO.getDeployedKpiByAggregation(VALID_SIMPLE_KPI_DEF_NAME, new ArrayList<>());
        Assertions.assertNull(noMatchedKpi);

        final KpiDefinitionDTO kpi = this.deployedKpiDefDAO.getDeployedKpiByAggregation(VALID_SIMPLE_KPI_DEF_NAME,
                VALID_PROFILE_DEF_AGGREGATION_FIELD_LIST);
        Assertions.assertNotNull(kpi);
        assertEquals(DEPLOYED_SIMPLE_KPI_OBJ, kpi);
    }

    @Test
    void getAllDeployedKpisTest() {

        this.deployedKpiDefDAO.createDeployedKpi(DEPLOYED_SIMPLE_KPI_OBJ, VALID_SIMPLE_KPI_DEF_NAME, VALID_PROFILE_DEF_OBJ);
        this.deployedKpiDefDAO.createDeployedKpi(DEPLOYED_COMPLEX_KPI_OBJ, VALID_COMPLEX_KPI_DEF_NAME, VALID_PROFILE_DEF_OBJ);

        final List<KpiDefinitionDTO> kpis = this.deployedKpiDefDAO.getAllDeployedKpis();
        assertEquals(2, kpis.size());

    }

    @Test
    void findAllRuntimeKpis() throws Exception {

        this.deployedKpiDefDAO.createDeployedKpi(DEPLOYED_SIMPLE_KPI_OBJ, VALID_SIMPLE_KPI_DEF_NAME, VALID_PROFILE_DEF_OBJ);
        this.deployedKpiDefDAO.createDeployedKpi(DEPLOYED_COMPLEX_KPI_OBJ, VALID_COMPLEX_KPI_DEF_NAME, VALID_PROFILE_DEF_OBJ);

        final List<RuntimeKpiInstance> kpis = this.deployedKpiDefDAO.findAllRuntimeKpis();

        assertEquals(2, kpis.size());

    }

    @Test
    void findAllByContextId() throws Exception {

        this.deployedKpiDefDAO.createDeployedKpi(DEPLOYED_SIMPLE_KPI_OBJ, VALID_SIMPLE_KPI_DEF_NAME, VALID_PROFILE_DEF_OBJ);
        this.deployedKpiDefDAO.createDeployedKpi(DEPLOYED_COMPLEX_KPI_OBJ, VALID_COMPLEX_KPI_DEF_NAME, VALID_PROFILE_DEF_OBJ);

        assertEquals(0, this.deployedKpiDefDAO.findAllByContextId(KpiContextId.of(Set.of("field1")), false).size());
        assertEquals(2, this.deployedKpiDefDAO.findAllByContextId(KpiContextId.of(Set.of("field1", "field2")), false).size());
        assertEquals(2, this.deployedKpiDefDAO.findAllByContextId(KpiContextId.of(Set.of("field1", "field2")), true).size());

    }

    @Test
    void findAllRuntimeKpis_start0_rows1() throws Exception {

        this.deployedKpiDefDAO.createDeployedKpi(DEPLOYED_SIMPLE_KPI_OBJ, VALID_SIMPLE_KPI_DEF_NAME, VALID_PROFILE_DEF_OBJ);
        this.deployedKpiDefDAO.createDeployedKpi(DEPLOYED_COMPLEX_KPI_OBJ, VALID_COMPLEX_KPI_DEF_NAME, VALID_PROFILE_DEF_OBJ);

        final List<RuntimeKpiInstance> kpis = this.deployedKpiDefDAO.findAllRuntimeKpis(0, 1);

        assertEquals(1, kpis.size());
    }

    @Test
    void findAllRuntimeKpis_start1_rows1() throws Exception {

        this.deployedKpiDefDAO.createDeployedKpi(DEPLOYED_SIMPLE_KPI_OBJ, VALID_SIMPLE_KPI_DEF_NAME, VALID_PROFILE_DEF_OBJ);
        this.deployedKpiDefDAO.createDeployedKpi(DEPLOYED_COMPLEX_KPI_OBJ, VALID_COMPLEX_KPI_DEF_NAME, VALID_PROFILE_DEF_OBJ);

        final List<RuntimeKpiInstance> kpis = this.deployedKpiDefDAO.findAllRuntimeKpis(1, 1);

        assertEquals(1, kpis.size());
    }

    @Test
    void findAllRuntimeKpis_start2_rows1() throws Exception {

        this.deployedKpiDefDAO.createDeployedKpi(DEPLOYED_SIMPLE_KPI_OBJ, VALID_SIMPLE_KPI_DEF_NAME, VALID_PROFILE_DEF_OBJ);
        this.deployedKpiDefDAO.createDeployedKpi(DEPLOYED_COMPLEX_KPI_OBJ, VALID_COMPLEX_KPI_DEF_NAME, VALID_PROFILE_DEF_OBJ);

        final List<RuntimeKpiInstance> kpis = this.deployedKpiDefDAO.findAllRuntimeKpis(2, 1);

        assertEquals(0, kpis.size());
    }

    @Test
    void findAllRuntimeKpis_start0_row0() throws Exception {

        this.deployedKpiDefDAO.createDeployedKpi(DEPLOYED_SIMPLE_KPI_OBJ, VALID_SIMPLE_KPI_DEF_NAME, VALID_PROFILE_DEF_OBJ);
        this.deployedKpiDefDAO.createDeployedKpi(DEPLOYED_COMPLEX_KPI_OBJ, VALID_COMPLEX_KPI_DEF_NAME, VALID_PROFILE_DEF_OBJ);

        final List<RuntimeKpiInstance> kpis = this.deployedKpiDefDAO.findAllRuntimeKpis(0, 0);

        assertEquals(0, kpis.size());
    }

    @Test
    void findAllRuntimeKpis_start0_rowsAll() throws Exception {

        this.deployedKpiDefDAO.createDeployedKpi(DEPLOYED_SIMPLE_KPI_OBJ, VALID_SIMPLE_KPI_DEF_NAME, VALID_PROFILE_DEF_OBJ);
        this.deployedKpiDefDAO.createDeployedKpi(DEPLOYED_COMPLEX_KPI_OBJ, VALID_COMPLEX_KPI_DEF_NAME, VALID_PROFILE_DEF_OBJ);

        final List<RuntimeKpiInstance> kpis = this.deployedKpiDefDAO.findAllRuntimeKpis(0, Integer.MAX_VALUE);

        assertEquals(2, kpis.size());
    }

    @Test
    void findAllRuntimeKpis_noData() throws Exception {

        final List<RuntimeKpiInstance> kpis = this.deployedKpiDefDAO.findAllRuntimeKpis(0, Integer.MAX_VALUE);

        assertEquals(0, kpis.size());
    }

    @Test
    void findAllRuntimeKpis_isVisible() {
        this.deployedKpiDefDAO.createDeployedKpi(DEPLOYED_SIMPLE_KPI_OBJ, VALID_SIMPLE_KPI_DEF_NAME, VALID_PROFILE_DEF_OBJ);
        this.deployedKpiDefDAO.createDeployedKpi(DEPLOYED_COMPLEX_KPI_OBJ, VALID_COMPLEX_KPI_DEF_NAME, VALID_PROFILE_DEF_OBJ);
        this.deployedKpiDefDAO.createDeployedKpi(DEPLOYED_SIMPLE_KPI_OBJ_2, DEPLOYED_SIMPLE_KPI_NAME_2, VALID_PROFILE_DEF_OBJ);

        final List<RuntimeKpiInstance> visibleKpis = this.deployedKpiDefDAO.findAllRuntimeKpis(true);
        final List<RuntimeKpiInstance> allKpis = this.deployedKpiDefDAO.findAllRuntimeKpis(false);

        assertEquals(2, visibleKpis.size());
        assertEquals(3, allKpis.size());
    }

    @Test
    void clear() {

        this.deployedKpiDefDAO.createDeployedKpi(DEPLOYED_SIMPLE_KPI_OBJ, VALID_SIMPLE_KPI_DEF_NAME, VALID_PROFILE_DEF_OBJ);
        this.deployedKpiDefDAO.createDeployedKpi(DEPLOYED_COMPLEX_KPI_OBJ, VALID_COMPLEX_KPI_DEF_NAME, VALID_PROFILE_DEF_OBJ);

        assertFalse(this.deployedKpiDefDAO.findAllRuntimeKpis(0, Integer.MAX_VALUE).isEmpty());

        this.deployedKpiDefDAO.clear();

        assertTrue(this.deployedKpiDefDAO.findAllRuntimeKpis(0, Integer.MAX_VALUE).isEmpty());

    }
}
