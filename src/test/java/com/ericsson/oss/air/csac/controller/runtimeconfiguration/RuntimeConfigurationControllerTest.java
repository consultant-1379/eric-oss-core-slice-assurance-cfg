/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.controller.runtimeconfiguration;

import static com.ericsson.oss.air.csac.model.TestResourcesUtils.PMSCHEMAS_EX;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import com.ericsson.oss.air.api.model.KpiRefsDto;
import com.ericsson.oss.air.api.model.Problem;
import com.ericsson.oss.air.api.model.ProfileDefinitionDto;
import com.ericsson.oss.air.api.model.ProfileDefinitionListDto;
import com.ericsson.oss.air.api.model.RtIndexDefListDto;
import com.ericsson.oss.air.api.model.RtKpiInstanceListDto;
import com.ericsson.oss.air.api.model.RtPmSchemaInfoListDto;
import com.ericsson.oss.air.csac.handler.request.PmSchemasRequestHandler;
import com.ericsson.oss.air.csac.handler.request.ProfileDefRequestHandler;
import com.ericsson.oss.air.csac.handler.request.RuntimeIndexDefRequestHandler;
import com.ericsson.oss.air.csac.handler.request.RuntimeKpiRequestHandler;
import com.ericsson.oss.air.csac.handler.status.ProvisioningTracker;
import com.ericsson.oss.air.csac.model.TestResourcesUtils;
import com.ericsson.oss.air.csac.model.runtime.ProvisioningState;
import com.ericsson.oss.air.util.codec.Codec;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class RuntimeConfigurationControllerTest {

    private static final List<ProfileDefinitionDto> PROFILE_DEFINITIONS_DTOS = Arrays.asList(
            new ProfileDefinitionDto().name("name1").description("desc1").context(List.of("agg1", "agg2"))
                    .kpis(List.of(new KpiRefsDto().ref("ref1"), new KpiRefsDto().ref("ref2"))),
            new ProfileDefinitionDto().name("name2").description("desc2").context(List.of("agg3", "agg4"))
                    .kpis(List.of(new KpiRefsDto().ref("ref3"), new KpiRefsDto().ref("ref4")))
    );

    private static final String RT_KPI_INSTANCE_LIST_STRING = "{\"total\":2,\"count\":2,\"start\":0,\"rows\":10,\"kpi_defs\":[{\"kpi_name\":\"GTP_InDataOctN3UPF_SNSSAI\",\"kpi_type\":\"simple\",\"kpi_context\":[\"snssai\"],\"deployment_details\":{\"rt_name\":\"csac_0dc9fe31_e876_45d2_a44d_7c3f8b02eed9\",\"rt_table\":\"kpi_csac_simple_snssai_15\",\"rt_definition\":{\"name\":\"csac_0dc9fe31_e876_45d2_a44d_7c3f8b02eed9\",\"alias\":\"csac_simple_snssai\",\"expression\":\"SUM(up_payload_dnn_slice_1.pmCounters.ul_ipv4_received_bytes+up_payload_dnn_slice_1.pmCounters.ul_ipv6_received_bytes+up_payload_dnn_slice_1.pmCounters.ul_unstr_received_bytes)\",\"object_type\":\"FLOAT\",\"aggregation_type\":\"SUM\",\"aggregation_period\":15,\"aggregation_elements\":[\"up_payload_dnn_slice_1.snssai\"],\"is_visible\":false,\"inp_data_category\":\"pm_data\",\"inp_data_identifier\":\"5G|PM_COUNTERS|up_payload_dnn_slice_1\"}}},{\"kpi_name\":\"UlUeThroughput_SNw\",\"kpi_type\":\"complex\",\"kpi_context\":[\"ta\",\"gnodeB\"],\"deployment_details\":{\"rt_name\":\"csac_12d65538_0e25_491e_b169_3b2b216ba1ba\",\"rt_table\":\"kpi_csac_complex_ta_gnodeb_15\",\"rt_definition\":{\"name\":\"csac_12d65538_0e25_491e_b169_3b2b216ba1ba\",\"alias\":\"csac_complex_ta_gnodeb\",\"expression\":\"SUM(kpi_csac_simple_ta_gnodeb_15.csac_c1719792_f981_4504_9cc1_43133d698d84 / kpi_csac_simple_ta_gnodeb_15.csac_4972ad87_5e41_46cd_8d88_01e61a9c4103) * 64.0 FROM kpi_db://kpi_csac_simple_ta_gnodeb_15\",\"object_type\":\"FLOAT\",\"aggregation_type\":\"SUM\",\"aggregation_period\":15,\"aggregation_elements\":[\"kpi_csac_simple_ta_gnodeb_15.ta\",\"kpi_csac_simple_ta_gnodeb_15.gnodeB\"],\"is_visible\":true,\"execution_group\":\"csac_execution_group\"}}}]}";

    private static RtKpiInstanceListDto rtKpiInstanceListDto;

    private static RtPmSchemaInfoListDto pmSchemaListDto;

    private final ProfileDefinitionListDto profileDefinitionListDto = new ProfileDefinitionListDto();

    @MockBean
    private ProfileDefRequestHandler profileDefRequestHandler;

    @MockBean
    private RuntimeKpiRequestHandler runtimeKpiRequestHandler;

    @MockBean
    private RuntimeIndexDefRequestHandler runtimeIndexDefRequestHandler;

    @Autowired
    private RuntimeConfigurationController configurationController;

    @Autowired
    private TestRestTemplate restTemplate;

    @MockBean
    private ProvisioningTracker provisioningTracker;

    @MockBean
    private PmSchemasRequestHandler pmSchemasRequestHandler;

    private static final Codec CODEC = new Codec();

    @BeforeAll
    public static void setUpClass() throws Exception {

        rtKpiInstanceListDto = CODEC.readValue(RT_KPI_INSTANCE_LIST_STRING, RtKpiInstanceListDto.class);

        pmSchemaListDto = CODEC.readValue(PMSCHEMAS_EX, RtPmSchemaInfoListDto.class);

    }

    @BeforeEach
    void setup() {
        profileDefinitionListDto.total(2).count(2).start(0).rows(2).profileDefs(PROFILE_DEFINITIONS_DTOS);
    }

    @Test
    void getProfiles_validRequest_SuccessResponse() {
        when(profileDefRequestHandler.getProfileDefinitions(anyInt(), anyInt())).thenReturn(profileDefinitionListDto);
        ResponseEntity<ProfileDefinitionListDto> response = restTemplate.getForEntity("/v1/runtime/profiles", ProfileDefinitionListDto.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(profileDefinitionListDto, response.getBody());
    }

    @Test
    void getProfiles_validRequest_SuccessResponseWithTrailingSlash() {
        when(profileDefRequestHandler.getProfileDefinitions(anyInt(), anyInt())).thenReturn(profileDefinitionListDto);
        ResponseEntity<ProfileDefinitionListDto> response = restTemplate.getForEntity("/v1/runtime/profiles/", ProfileDefinitionListDto.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(profileDefinitionListDto, response.getBody());
    }

    @Test
    void getProfiles_badQuery_badRequestResponse() {
        ResponseEntity<ProfileDefinitionListDto> response = restTemplate.getForEntity("/v1/runtime/profiles?start=0,rows=1",
                ProfileDefinitionListDto.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void getProfiles_negativeStart_BadRequest() {
        ResponseEntity<ProfileDefinitionListDto> response = restTemplate.getForEntity("/v1/runtime/profiles?start=-1&rows=5",
                ProfileDefinitionListDto.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void getProfiles_NegativeRows_GoodRequest() {

        final ProfileDefinitionListDto expected = new ProfileDefinitionListDto().start(0).rows(-1).count(2).total(2)
                .profileDefs(PROFILE_DEFINITIONS_DTOS);

        when(profileDefRequestHandler.getProfileDefinitions(anyInt(), anyInt())).thenReturn(expected);

        ResponseEntity<ProfileDefinitionListDto> response = restTemplate.getForEntity("/v1/runtime/profiles?start=0&rows=-1",
                ProfileDefinitionListDto.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        final ProfileDefinitionListDto actual = response.getBody();
        assertNotNull(actual);
        assertEquals(-1, actual.getRows());
        assertEquals(2, actual.getCount());
        assertEquals(actual.getCount(), actual.getProfileDefs().size());
    }

    @Test
    void getProfiles_InvalidStart_BadRequest() {
        ResponseEntity<ProfileDefinitionListDto> response = restTemplate.getForEntity("/v1/dictionary/kpis?start=test",
                ProfileDefinitionListDto.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void getProfiles_InvalidRows_BadRequest() {
        ResponseEntity<ProfileDefinitionListDto> response = restTemplate.getForEntity("/v1/dictionary/kpis?rows=test",
                ProfileDefinitionListDto.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void getRuntimeKpis_validRequest_SuccessResponse() {

        when(this.runtimeKpiRequestHandler.getRuntimeKpiDefinitions(anyInt(), anyInt())).thenReturn(rtKpiInstanceListDto);

        ResponseEntity<RtKpiInstanceListDto> response = restTemplate.getForEntity("/v1/runtime/kpis", RtKpiInstanceListDto.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(rtKpiInstanceListDto, response.getBody());
    }

    @Test
    void getRuntimeKpis_validRequest_SuccessResponseWithTrailingSlash() {

        when(this.runtimeKpiRequestHandler.getRuntimeKpiDefinitions(anyInt(), anyInt())).thenReturn(rtKpiInstanceListDto);

        ResponseEntity<RtKpiInstanceListDto> response = restTemplate.getForEntity("/v1/runtime/kpis/", RtKpiInstanceListDto.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(rtKpiInstanceListDto, response.getBody());
    }

    @Test
    void getRuntimeKpis_badQuery_badRequestResponse() {
        ResponseEntity<RtKpiInstanceListDto> response = restTemplate.getForEntity("/v1/runtime/kpis?start=-1,rows=1",
                RtKpiInstanceListDto.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void getRtIndexes_validRequest_successResponse() {
        when(this.runtimeIndexDefRequestHandler.getRtIndexDefinitions()).thenReturn(TestResourcesUtils.RT_INDEX_DEF_LIST_DTO);
        ResponseEntity<RtIndexDefListDto> response = restTemplate.getForEntity("/v1/runtime/indexes", RtIndexDefListDto.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(TestResourcesUtils.RT_INDEX_DEF_LIST_DTO, response.getBody());
        verify(this.runtimeIndexDefRequestHandler, times(1)).getRtIndexDefinitions();
    }

    @Test
    void getRtIndexes_validRequest_successResponseWithTrailingSlash() {
        when(this.runtimeIndexDefRequestHandler.getRtIndexDefinitions()).thenReturn(TestResourcesUtils.RT_INDEX_DEF_LIST_DTO);
        ResponseEntity<RtIndexDefListDto> response = restTemplate.getForEntity("/v1/runtime/indexes/", RtIndexDefListDto.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(TestResourcesUtils.RT_INDEX_DEF_LIST_DTO, response.getBody());
        verify(this.runtimeIndexDefRequestHandler, times(1)).getRtIndexDefinitions();
    }

    @Test
    void getRtPmSchemas_SuccessResponse() {

        when(this.provisioningTracker.currentProvisioningState()).thenReturn(ProvisioningState.completed());
        when(this.pmSchemasRequestHandler.getPmSchemas()).thenReturn(pmSchemaListDto);

        final ResponseEntity<RtPmSchemaInfoListDto> response = this.restTemplate.getForEntity("/v1/runtime/pmschemas", RtPmSchemaInfoListDto.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(pmSchemaListDto, response.getBody());
    }

    @Test
    void getRtPmSchemas_SuccessResponseWithTrailingSlash() {

        when(this.provisioningTracker.currentProvisioningState()).thenReturn(ProvisioningState.completed());
        when(this.pmSchemasRequestHandler.getPmSchemas()).thenReturn(pmSchemaListDto);

        final ResponseEntity<RtPmSchemaInfoListDto> response = this.restTemplate.getForEntity("/v1/runtime/pmschemas/", RtPmSchemaInfoListDto.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(pmSchemaListDto, response.getBody());
    }

    @Test
    void getRtPmSchemas_ProvisioningStateStarted_AcceptedResponse() {

        when(this.provisioningTracker.currentProvisioningState()).thenReturn(ProvisioningState.started());

        final ResponseEntity<RtPmSchemaInfoListDto> response = this.restTemplate.getForEntity("/v1/runtime/pmschemas", RtPmSchemaInfoListDto.class);

        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
    }

    @Test
    void getRtPmSchemas_ProvisioningStateCompleted_OkResponse() {

        when(this.provisioningTracker.currentProvisioningState()).thenReturn(ProvisioningState.completed());

        final ResponseEntity<RtPmSchemaInfoListDto> response = this.restTemplate.getForEntity("/v1/runtime/pmschemas", RtPmSchemaInfoListDto.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void getRtPmSchemas_ProvisioningStateReset_OkResponse() {

        when(this.provisioningTracker.currentProvisioningState()).thenReturn(ProvisioningState.ofState(ProvisioningState.State.RESET));

        final ResponseEntity<RtPmSchemaInfoListDto> response = this.restTemplate.getForEntity("/v1/runtime/pmschemas", RtPmSchemaInfoListDto.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void getRtPmSchemas_ProvisioningStateInitial_OkResponse() {

        when(this.provisioningTracker.currentProvisioningState()).thenReturn(ProvisioningState.ofState(ProvisioningState.State.INITIAL));

        final ResponseEntity<RtPmSchemaInfoListDto> response = this.restTemplate.getForEntity("/v1/runtime/pmschemas", RtPmSchemaInfoListDto.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void getRtPmSchemas_ProvisioningStateError_ConflictResponse() {

        when(this.provisioningTracker.currentProvisioningState()).thenReturn(ProvisioningState.error());

        final String expectedErrorMessage = "Unable to successfully compute the runtime configuration. " +
                "Conflict with current provisioning state: ERROR";

        final ResponseEntity<Problem> response = this.restTemplate.getForEntity("/v1/runtime/pmschemas", Problem.class);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals(expectedErrorMessage, response.getBody().getDetail());
    }

    @Test
    void getRtPmSchemas_ProvisioningStateInterrupt_ConflictResponse() {

        when(this.provisioningTracker.currentProvisioningState()).thenReturn(ProvisioningState.ofState(ProvisioningState.State.INTERRUPT));

        final String expectedErrorMessage = "Unable to successfully compute the runtime configuration. " +
                "Conflict with current provisioning state: INTERRUPT";

        final ResponseEntity<Problem> response = this.restTemplate.getForEntity("/v1/runtime/pmschemas", Problem.class);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals(expectedErrorMessage, response.getBody().getDetail());
    }

}
