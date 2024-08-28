/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.service.kpi.pmsc;

import static com.ericsson.oss.air.csac.model.TestResourcesUtils.DEPLOYED_SIMPLE_KPI_OBJ;
import static com.ericsson.oss.air.csac.service.kpi.pmsc.DryRunPmscRestClientTest.KPI_DEFINITIONS_SUBMISSION;
import static com.ericsson.oss.air.csac.service.kpi.pmsc.LivePmscRestClient.PMSC_KPIS_DEFINITIONS_API;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.client.ExpectedCount.manyTimes;
import static org.springframework.test.web.client.ExpectedCount.times;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withCreatedEntity;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import com.ericsson.oss.air.csac.model.pmsc.AggregationPeriod;
import com.ericsson.oss.air.csac.model.pmsc.KpiOutputTableListDto;
import com.ericsson.oss.air.csac.model.pmsc.KpiSubmissionDto;
import com.ericsson.oss.air.csac.model.pmsc.PmscKpiDefinitionDto;
import com.ericsson.oss.air.csac.model.pmsc.SimpleKpiOutputTableDto;
import com.ericsson.oss.air.csac.model.pmsc.SimplePmscKpiDefinitionDto;
import com.ericsson.oss.air.csac.service.exception.PmscHttpResponseErrorHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

@RestClientTest(LivePmscRestClient.class)
@ActiveProfiles("test")
public class LivePmscRestClientTest {

    @Value("${provisioning.pmsc.restClient.url}")
    private String pmscUrl;

    @Autowired
    private MockRestServiceServer server;

    @Autowired
    private LivePmscRestClient testClient;

    @Autowired
    private ObjectMapper objectMapper;

    @SpyBean
    private RestTemplate restTemplate;

    private String URL;

    private static final SimplePmscKpiDefinitionDto SIMPLE_KPI_DEFINITION_DTO = SimplePmscKpiDefinitionDto.builder()
            .name("csac_3d3b5e94_51c6_401d_b1fd_440361beb32c_simple_kpi")
            .aggregationType("SUM")
            .expression("SUM(AMF_Mobility_NetworkSlice_1.pmCounters.VS_NS_NbrRegisteredSub_5GS)")
            .objectType("FLOAT")
            .build();

    private static final SimpleKpiOutputTableDto SIMPLE_KPI_OUTPUT_TABLE_DTO = SimpleKpiOutputTableDto.customSimpleKpiOutputTableDtoBuilder()
            .aggregationPeriod(AggregationPeriod.FIFTEEN)
            .aggregationElements(List.of("AMF_Mobility_NetworkSlice_1.nodeFDN"))
            .alias("c42b7ae773359dfc2249fe0e95ca44a624a1cf")
            .dataReliabilityOffset(0)
            .inputDataIdentifier("5G|PM_COUNTERS|AMF_Mobility_NetworkSlice_1")
            .kpiDefinitions(List.of(SIMPLE_KPI_DEFINITION_DTO))
            .build();

    private static final KpiOutputTableListDto SIMPLE_KPI_OUTPUT_TABLE_LIST = KpiOutputTableListDto.builder()
            .kpiOutputTables(List.of(SIMPLE_KPI_OUTPUT_TABLE_DTO)).build();

    public static final KpiSubmissionDto KPI_SUBMISSION_DTO = KpiSubmissionDto.builder().scheduledSimple(SIMPLE_KPI_OUTPUT_TABLE_LIST).build();

    @BeforeEach
    void setUp() {
        URL = this.pmscUrl + PMSC_KPIS_DEFINITIONS_API;
    }

    @Test
    void create() {

        this.server.expect(manyTimes(), requestTo(URL)).andRespond(withCreatedEntity(null));

        final ResponseEntity<Void> response = testClient.create(KPI_DEFINITIONS_SUBMISSION);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());

        verify(this.restTemplate, Mockito.times(2)).setErrorHandler(any(PmscHttpResponseErrorHandler.class));

    }

    @Test
    void deleteById() {

        this.server.expect(times(1), requestTo(URL)).andRespond(withSuccess());

        this.testClient.deleteById(List.of("kpi1", "kpi2"));

        server.verify();

        verify(this.restTemplate, Mockito.times(1)).setErrorHandler(any(PmscHttpResponseErrorHandler.class));

    }

    @Test
    void delete() {
        this.server.expect(times(1), requestTo(URL)).andRespond(withSuccess());

        this.testClient.delete(List.of(DEPLOYED_SIMPLE_KPI_OBJ));

        this.server.verify();

        verify(this.restTemplate, Mockito.times(1)).setErrorHandler(any(PmscHttpResponseErrorHandler.class));

    }

    @Test
    void getAll() throws IOException {
        final String responseString = FileUtils.readFileToString(new File("src/test/resources/fixtures/pmsc/pmsc_get_kpi_response.json"),
                StandardCharsets.UTF_8);

        this.server.expect(requestTo(URL)).andExpect(method(HttpMethod.GET)).andRespond(withSuccess(responseString, MediaType.APPLICATION_JSON));

        final List<PmscKpiDefinitionDto> result = this.testClient.getAll();

        this.server.verify();
        assertNotNull(result);

        verify(this.restTemplate, Mockito.times(1)).setErrorHandler(any(PmscHttpResponseErrorHandler.class));

    }

    @Test
    void deleteAll() throws IOException {
        String responseString = FileUtils.readFileToString(new File("src/test/resources/fixtures/pmsc/pmsc_get_kpi_response.json"),
                StandardCharsets.UTF_8);

        this.server.expect(requestTo(URL)).andExpect(method(HttpMethod.GET)).andRespond(withSuccess(responseString, MediaType.APPLICATION_JSON));
        this.server.expect(requestTo(URL)).andExpect(method(HttpMethod.DELETE)).andRespond(withSuccess());

        this.testClient.deleteAll();

        this.server.verify();

        verify(this.restTemplate, Mockito.times(2)).setErrorHandler(any(PmscHttpResponseErrorHandler.class));

    }

    @Test
    void getAll_emptyResponse() {

        this.server.expect(requestTo(URL)).andExpect(method(HttpMethod.GET)).andRespond(withSuccess("", MediaType.APPLICATION_JSON));

        final List<PmscKpiDefinitionDto> result = this.testClient.getAll();

        this.server.verify();
        assertEquals(result, List.of());

        verify(this.restTemplate, Mockito.times(1)).setErrorHandler(any(PmscHttpResponseErrorHandler.class));

    }

    @Test
    void getKpiIdsFromLegacy() throws Exception {
        assertEquals(1, this.testClient.getKpiIdsFromLegacy(KPI_DEFINITIONS_SUBMISSION).size());
    }

    @Test
    void getKpiIdsFromSubmission() throws Exception {

        assertEquals(1, this.testClient.getKpiIdsFromSubmission(KPI_SUBMISSION_DTO).size());

    }

    @Test
    void doSkipCreate_existingModel_legacySubmission() throws Exception {

        final String responseString = FileUtils.readFileToString(
                new File("src/test/resources/fixtures/pmsc/pmsc_get_kpi_response_single_simple.json"),
                StandardCharsets.UTF_8);

        this.server.expect(requestTo(URL)).andExpect(method(HttpMethod.GET)).andRespond(withSuccess(responseString, MediaType.APPLICATION_JSON));

        assertTrue(this.testClient.doSkipCreate(KPI_DEFINITIONS_SUBMISSION));
    }

    @Test
    void doSkipCreate_existingModel_newSubmission() throws Exception {

        final String responseString = FileUtils.readFileToString(
                new File("src/test/resources/fixtures/pmsc/pmsc_get_kpi_response_single_simple.json"),
                StandardCharsets.UTF_8);

        this.server.expect(requestTo(URL)).andExpect(method(HttpMethod.GET)).andRespond(withSuccess(responseString, MediaType.APPLICATION_JSON));

        assertTrue(this.testClient.doSkipCreate(KPI_SUBMISSION_DTO));
    }

    @Test
    void doSkipCreate_false_legacySubmission() throws Exception {

        this.server.expect(manyTimes(), requestTo(URL)).andRespond(withCreatedEntity(null));

        assertFalse(this.testClient.doSkipCreate(KPI_DEFINITIONS_SUBMISSION));
    }

    @Test
    void doSkipCreate_false_newSubmission() throws Exception {

        this.server.expect(manyTimes(), requestTo(URL)).andRespond(withCreatedEntity(null));

        assertFalse(this.testClient.doSkipCreate(KPI_SUBMISSION_DTO));
    }

    @Test
    void create_existingModel_legacySubmission() throws IOException {

        final String responseString = FileUtils.readFileToString(
                new File("src/test/resources/fixtures/pmsc/pmsc_get_kpi_response_single_simple.json"),
                StandardCharsets.UTF_8);

        this.server.expect(requestTo(URL)).andExpect(method(HttpMethod.GET)).andRespond(withSuccess(responseString, MediaType.APPLICATION_JSON));

        final ResponseEntity<Void> response = this.testClient.create(KPI_DEFINITIONS_SUBMISSION);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());

        verify(this.restTemplate, Mockito.times(1)).setErrorHandler(any(PmscHttpResponseErrorHandler.class));

    }

    @Test
    void create_existingModel_newSubmission() throws IOException {

        final String responseString = FileUtils.readFileToString(
                new File("src/test/resources/fixtures/pmsc/pmsc_get_kpi_response_single_simple.json"),
                StandardCharsets.UTF_8);

        this.server.expect(requestTo(URL)).andExpect(method(HttpMethod.GET)).andRespond(withSuccess(responseString, MediaType.APPLICATION_JSON));

        final ResponseEntity<Void> response = this.testClient.create(KPI_SUBMISSION_DTO);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());

        verify(this.restTemplate, Mockito.times(1)).setErrorHandler(any(PmscHttpResponseErrorHandler.class));

    }
}