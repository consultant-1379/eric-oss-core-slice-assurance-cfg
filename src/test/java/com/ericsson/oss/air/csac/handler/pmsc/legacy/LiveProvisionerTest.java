/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.handler.pmsc.legacy;

import static com.ericsson.oss.air.csac.model.TestResourcesUtils.DEPLOYED_COMPLEX_KPI_OBJ;
import static com.ericsson.oss.air.csac.model.TestResourcesUtils.DEPLOYED_SIMPLE_KPI_OBJ;
import static com.ericsson.oss.air.csac.model.TestResourcesUtils.VALID_PROFILE_DEF_OBJ;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.List;

import com.ericsson.oss.air.csac.handler.event.ConsistencyCheckEvent;
import com.ericsson.oss.air.csac.handler.event.ConsistencyCheckHandler;
import com.ericsson.oss.air.csac.handler.pmsc.transform.KpiSubmissionTransformer;
import com.ericsson.oss.air.csac.model.pmsc.KpiDefinitionDTOWithRelationship;
import com.ericsson.oss.air.csac.repository.DeployedProfileDAO;
import com.ericsson.oss.air.csac.repository.cache.ResolvedKpiCache;
import com.ericsson.oss.air.csac.service.kpi.pmsc.PmscRestClient;
import com.ericsson.oss.air.csac.service.kpi.pmsc.legacy.PMSCRestClient;
import com.ericsson.oss.air.exception.CsacConsistencyCheckException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class LiveProvisionerTest {

    private LiveProvisioner liveProvisioner;

    @Mock
    private DeployedProfileDAO deployedProfileDAO;

    @Mock
    private KpiSubmissionTransformer kpiSubmissionTransformer;

    @Mock
    private PMSCRestClient legacyRestClient;

    @Mock
    private PmscRestClient pmscRestClient;

    @Mock
    private ResolvedKpiCache resolvedKpiCache;

    @Mock
    private ConsistencyCheckHandler consistencyCheckHandler;

    @BeforeEach
    void setUp() {
        this.liveProvisioner = new LiveProvisioner(this.deployedProfileDAO, this.kpiSubmissionTransformer, this.legacyRestClient,
                this.pmscRestClient, this.resolvedKpiCache, this.consistencyCheckHandler);
        ReflectionTestUtils.setField(this.liveProvisioner, "isLegacyPmscClient", true);
    }

    @Test
    void provision_KpiMissedInfoWithValidProfile_Void() {
        final List<KpiDefinitionDTOWithRelationship> kpi = List.of(
                new KpiDefinitionDTOWithRelationship(DEPLOYED_SIMPLE_KPI_OBJ, "name", VALID_PROFILE_DEF_OBJ));

        this.liveProvisioner.provision(kpi, List.of(VALID_PROFILE_DEF_OBJ));

        verify(legacyRestClient, times(1)).updatePMSCKpisDefinitions(any());
        verify(pmscRestClient, times(0)).create(any());
        verify(resolvedKpiCache, times(1)).flush();
        verify(deployedProfileDAO, times(1)).insertProfileDefinitions(any());
    }

    @Test
    void provision_SimpleKpiWithValidProfile_Void() {
        final List<KpiDefinitionDTOWithRelationship> kpi = List.of(
                new KpiDefinitionDTOWithRelationship(DEPLOYED_SIMPLE_KPI_OBJ, "name", VALID_PROFILE_DEF_OBJ));

        this.liveProvisioner.provision(kpi, List.of(VALID_PROFILE_DEF_OBJ));

        verify(legacyRestClient, times(1)).updatePMSCKpisDefinitions(any());
        verify(pmscRestClient, times(0)).create(any());
        verify(resolvedKpiCache, times(1)).flush();
        verify(deployedProfileDAO, times(1)).insertProfileDefinitions(any());
    }

    @Test
    void provision_ComplexKpiWithValidProfile_Void() {
        final List<KpiDefinitionDTOWithRelationship> kpi = List.of(
                new KpiDefinitionDTOWithRelationship(DEPLOYED_COMPLEX_KPI_OBJ, "name", VALID_PROFILE_DEF_OBJ));

        this.liveProvisioner.provision(kpi, List.of(VALID_PROFILE_DEF_OBJ));

        verify(legacyRestClient, times(1)).updatePMSCKpisDefinitions(any());
        verify(pmscRestClient, times(0)).create(any());
        verify(resolvedKpiCache, times(1)).flush();
        verify(deployedProfileDAO, times(1)).insertProfileDefinitions(any());
    }

    @Test
    void provision_ComplexKpiWithValidProfile_newPmscClient() {

        ReflectionTestUtils.setField(this.liveProvisioner, "isLegacyPmscClient", false);

        final List<KpiDefinitionDTOWithRelationship> kpi = List.of(
                new KpiDefinitionDTOWithRelationship(DEPLOYED_COMPLEX_KPI_OBJ, "name", VALID_PROFILE_DEF_OBJ));

        this.liveProvisioner.provision(kpi, List.of(VALID_PROFILE_DEF_OBJ));

        verify(legacyRestClient, times(0)).updatePMSCKpisDefinitions(any());
        verify(pmscRestClient, times(1)).create(any());
        verify(resolvedKpiCache, times(1)).flush();
        verify(deployedProfileDAO, times(1)).insertProfileDefinitions(any());
    }

    @Test
    void provision_KpiMissedInfoWithValidProfile_exception() {
        final List<KpiDefinitionDTOWithRelationship> kpi = List.of(
                new KpiDefinitionDTOWithRelationship(DEPLOYED_SIMPLE_KPI_OBJ, "name", VALID_PROFILE_DEF_OBJ));

        doThrow(new RuntimeException("test")).when(this.deployedProfileDAO).insertProfileDefinitions(any());

        assertThrows(CsacConsistencyCheckException.class,
                () -> this.liveProvisioner.provision(kpi, List.of(VALID_PROFILE_DEF_OBJ)));

        final ArgumentCaptor<ConsistencyCheckEvent.Payload> payloadArgumentCaptor = ArgumentCaptor.forClass(ConsistencyCheckEvent.Payload.class);
        verify(this.consistencyCheckHandler, times(1)).notifyCheckFailure(payloadArgumentCaptor.capture());
        assertEquals(ConsistencyCheckEvent.Payload.Type.SUSPECT, payloadArgumentCaptor.getValue().getType());
        assertEquals(1, payloadArgumentCaptor.getValue().getCount());
    }
}
