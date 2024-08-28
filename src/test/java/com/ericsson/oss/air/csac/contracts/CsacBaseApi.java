/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.contracts;

import static com.ericsson.oss.air.csac.model.TestResourcesUtils.AUG_DEF_LIST_EX_STR;
import static com.ericsson.oss.air.csac.model.TestResourcesUtils.KPI_DEFINITION_LIST_DTO_EX;
import static com.ericsson.oss.air.csac.model.TestResourcesUtils.PMSCHEMAS_EX;
import static com.ericsson.oss.air.csac.model.TestResourcesUtils.PM_DEFINITION_LIST_DTO_EX;
import static com.ericsson.oss.air.csac.model.TestResourcesUtils.PROFILE_DEFINITION_LIST_DTO_EX;
import static com.ericsson.oss.air.csac.model.TestResourcesUtils.PROVISIONING_STATE_COMPLETED_STATE;
import static com.ericsson.oss.air.csac.model.TestResourcesUtils.PROVISIONING_STATE_INITIAL_STATE;
import static com.ericsson.oss.air.csac.model.TestResourcesUtils.RT_INDEX_DEF_LIST_DTO;
import static com.ericsson.oss.air.csac.model.TestResourcesUtils.RT_KPI_DEF_LIST_EX_STR;
import static com.ericsson.oss.air.csac.model.TestResourcesUtils.RUNTIME_CONTEXT_METADATA_EX;
import static com.ericsson.oss.air.csac.model.TestResourcesUtils.RUNTIME_CONTEXT_METADATA_EX_2;
import static com.ericsson.oss.air.csac.model.TestResourcesUtils.RUNTIME_KPI_METADATA_EX;
import static com.ericsson.oss.air.csac.model.TestResourcesUtils.RUNTIME_KPI_METADATA_EX_2;
import static com.ericsson.oss.air.csac.model.TestResourcesUtils.RUNTIME_KPI_METADATA_EX_3;
import static com.ericsson.oss.air.csac.model.TestResourcesUtils.RUNTIME_KPI_METADATA_EX_4;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.ericsson.oss.air.api.model.AugmentationListDto;
import com.ericsson.oss.air.api.model.KpiDefinitionListDto;
import com.ericsson.oss.air.api.model.PmDefinitionListDto;
import com.ericsson.oss.air.api.model.ProfileDefinitionListDto;
import com.ericsson.oss.air.api.model.RtContextMetadataDto;
import com.ericsson.oss.air.api.model.RtKpiInstanceListDto;
import com.ericsson.oss.air.api.model.RtKpiMetadataDto;
import com.ericsson.oss.air.api.model.RtPmSchemaInfoListDto;
import com.ericsson.oss.air.api.model.RtProvisioningStateDto;
import com.ericsson.oss.air.csac.handler.request.AugmentationRequestHandler;
import com.ericsson.oss.air.csac.handler.request.KPIDefRequestHandler;
import com.ericsson.oss.air.csac.handler.request.PMDefRequestHandler;
import com.ericsson.oss.air.csac.handler.request.PmSchemasRequestHandler;
import com.ericsson.oss.air.csac.handler.request.ProfileDefRequestHandler;
import com.ericsson.oss.air.csac.handler.request.ProvisioningStateRequestHandler;
import com.ericsson.oss.air.csac.handler.request.RuntimeIndexDefRequestHandler;
import com.ericsson.oss.air.csac.handler.request.RuntimeKpiRequestHandler;
import com.ericsson.oss.air.csac.handler.request.RuntimeMetadataRequestHandler;
import com.ericsson.oss.air.csac.handler.status.ProvisioningTracker;
import com.ericsson.oss.air.csac.model.runtime.ProvisioningState;
import com.ericsson.oss.air.csac.model.runtime.metadata.KpiContextId;
import com.ericsson.oss.air.exception.CsacConflictStateException;
import com.ericsson.oss.air.exception.CsacNotFoundException;
import com.ericsson.oss.air.util.codec.Codec;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.context.WebApplicationContext;

@ActiveProfiles("test")
@SpringBootTest
public class CsacBaseApi {

    private AutoCloseable closeable;

    @MockBean
    private KPIDefRequestHandler kpiDefRequestHandler;

    @MockBean
    private PMDefRequestHandler pmDefRequestHandler;

    @MockBean
    private AugmentationRequestHandler augmentationRequestHandler;

    @MockBean
    private ProfileDefRequestHandler profileDefRequestHandler;

    @MockBean
    private RuntimeKpiRequestHandler runtimeKpiRequestHandler;

    @MockBean
    private RuntimeIndexDefRequestHandler runtimeIndexDefRequestHandler;

    @MockBean
    private ProvisioningStateRequestHandler provisioningStateRequestHandler;

    @MockBean
    private PmSchemasRequestHandler pmSchemasRequestHandler;

    @MockBean
    private RuntimeMetadataRequestHandler runtimeMetadataRequestHandler;

    @MockBean
    private ProvisioningTracker provisioningTracker;

    @Autowired
    protected WebApplicationContext webApplicationContext;

    private static RtKpiInstanceListDto rtKpiInstanceListDtoEx;

    private static AugmentationListDto augmentationListDtoEx;

    private static RtProvisioningStateDto initialState;

    private static RtProvisioningStateDto completedState;

    private static RtPmSchemaInfoListDto pmSchemaListDto;

    private static RtContextMetadataDto rtContextMetadataDto;

    private static RtContextMetadataDto rtContextMetadataDtoOnlyName;

    private static RtKpiMetadataDto rtKpiMetadataDto;

    private static RtKpiMetadataDto rtKpiMetadataDto2;

    private static RtKpiMetadataDto rtKpiMetadataDto3;

    private static RtKpiMetadataDto rtKpiMetadataDto4;

    private static final Codec CODEC = new Codec();

    @BeforeAll
    public static void setUpClass() throws Exception {

        rtKpiInstanceListDtoEx = CODEC.readValue(RT_KPI_DEF_LIST_EX_STR, RtKpiInstanceListDto.class);

        augmentationListDtoEx = CODEC.readValue(AUG_DEF_LIST_EX_STR, AugmentationListDto.class);

        initialState = CODEC.readValue(PROVISIONING_STATE_INITIAL_STATE, RtProvisioningStateDto.class);

        completedState = CODEC.readValue(PROVISIONING_STATE_COMPLETED_STATE, RtProvisioningStateDto.class);

        pmSchemaListDto = CODEC.readValue(PMSCHEMAS_EX, RtPmSchemaInfoListDto.class);

        rtContextMetadataDto = CODEC.readValue(RUNTIME_CONTEXT_METADATA_EX, RtContextMetadataDto.class);

        rtContextMetadataDtoOnlyName = CODEC.readValue(RUNTIME_CONTEXT_METADATA_EX_2, RtContextMetadataDto.class);

        rtKpiMetadataDto = CODEC.readValue(RUNTIME_KPI_METADATA_EX, RtKpiMetadataDto.class);
        rtKpiMetadataDto2 = CODEC.readValue(RUNTIME_KPI_METADATA_EX_2, RtKpiMetadataDto.class);
        rtKpiMetadataDto3 = CODEC.readValue(RUNTIME_KPI_METADATA_EX_3, RtKpiMetadataDto.class);
        rtKpiMetadataDto4 = CODEC.readValue(RUNTIME_KPI_METADATA_EX_4, RtKpiMetadataDto.class);
    }

    @BeforeEach
    public void setup() {
        closeable = MockitoAnnotations.openMocks(this);
        RestAssuredMockMvc.webAppContextSetup(this.webApplicationContext);

        when(augmentationRequestHandler.getAugmentationList(0, 10)).thenReturn(augmentationListDtoEx);
        when(augmentationRequestHandler.getAugmentationList(0, 0)).thenReturn(
                new AugmentationListDto().total(0).count(0).start(0).rows(0).augmentations(new ArrayList<>())
        );

        when(kpiDefRequestHandler.getKPIDefinitions(0, 10)).thenReturn(KPI_DEFINITION_LIST_DTO_EX);
        when(kpiDefRequestHandler.getKPIDefinitions(0, 0)).thenReturn(
                new KpiDefinitionListDto().total(0).count(0).start(0).rows(0).kpiDefs(new ArrayList<>())
        );

        when(pmDefRequestHandler.getPMDefinitions(0, 10)).thenReturn(PM_DEFINITION_LIST_DTO_EX);
        when(pmDefRequestHandler.getPMDefinitions(0, 0)).thenReturn(
                new PmDefinitionListDto().total(0).count(0).start(0).rows(0).pmDefs(new ArrayList<>())
        );

        when(profileDefRequestHandler.getProfileDefinitions(0, 10)).thenReturn(PROFILE_DEFINITION_LIST_DTO_EX);
        when(profileDefRequestHandler.getProfileDefinitions(0, 0)).thenReturn(
                new ProfileDefinitionListDto().total(0).count(0).start(0).rows(0).profileDefs(new ArrayList<>())
        );

        when(this.runtimeKpiRequestHandler.getRuntimeKpiDefinitions(0, 10)).thenReturn(rtKpiInstanceListDtoEx);
        when(this.runtimeKpiRequestHandler.getRuntimeKpiDefinitions(0, 0))
                .thenReturn(new RtKpiInstanceListDto().total(0).count(0).start(0).rows(0).kpiDefs(Collections.emptyList()));

        when(this.runtimeIndexDefRequestHandler.getRtIndexDefinitions()).thenReturn(RT_INDEX_DEF_LIST_DTO);

        when(this.provisioningStateRequestHandler.getLatestProvisioningState()).thenReturn(completedState);

        when(this.provisioningStateRequestHandler.getProvisioningStates()).thenReturn(List.of(initialState, completedState));

        when(this.pmSchemasRequestHandler.getPmSchemas()).thenReturn(pmSchemaListDto);

        when(this.provisioningTracker.currentProvisioningState()).thenReturn(ProvisioningState.completed());

        when(this.runtimeMetadataRequestHandler.getContextMetadata()).thenReturn(List.of(rtContextMetadataDto, rtContextMetadataDtoOnlyName));

        when(this.runtimeMetadataRequestHandler.getContextKpiMetadata(KpiContextId.of("plmnid_qos_snssai"))).thenReturn(
                List.of(rtKpiMetadataDto, rtKpiMetadataDto2, rtKpiMetadataDto3, rtKpiMetadataDto4));

        when(this.runtimeMetadataRequestHandler.getContextKpiMetadata(KpiContextId.of("not_found"))).thenThrow(
                new CsacNotFoundException("The requested resource was not found on this server."));

        when(this.runtimeMetadataRequestHandler.getContextKpiMetadata(KpiContextId.of("conflict_state"))).thenThrow(
                new CsacConflictStateException(
                        "Conflict with provisioning state: INITIAL. Runtime metadata computation failed. Retry may be successful later"));

    }

    @AfterEach
    public void tearDown() throws Exception {
        closeable.close();
    }
}
