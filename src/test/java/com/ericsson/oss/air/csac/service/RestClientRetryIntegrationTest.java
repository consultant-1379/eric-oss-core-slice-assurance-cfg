/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.service;

import static com.ericsson.oss.air.csac.model.TestResourcesUtils.DEPLOYED_INDEX_DEFINITION_DTO_A;
import static com.ericsson.oss.air.csac.model.TestResourcesUtils.DEPLOYED_SIMPLE_KPI_OBJ;
import static com.ericsson.oss.air.csac.service.kpi.pmsc.LivePmscRestClientTest.KPI_SUBMISSION_DTO;
import static com.ericsson.oss.air.util.logging.TestLoggingUtils.FACILITY_KEY;
import static com.ericsson.oss.air.util.logging.TestLoggingUtils.FACILITY_VALUE;
import static com.ericsson.oss.air.util.logging.TestLoggingUtils.SUBJECT_KEY;
import static com.ericsson.oss.air.util.logging.TestLoggingUtils.SUBJECT_VALUE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.ericsson.oss.air.csac.configuration.RetryLogListener;
import com.ericsson.oss.air.csac.model.augmentation.ArdqRegistrationResponseDto;
import com.ericsson.oss.air.csac.model.augmentation.AugmentationFieldRequestDto;
import com.ericsson.oss.air.csac.model.augmentation.AugmentationRequestDto;
import com.ericsson.oss.air.csac.model.augmentation.AugmentationRuleRequestDto;
import com.ericsson.oss.air.csac.model.pmsc.response.PmscKpiResponseDto;
import com.ericsson.oss.air.csac.model.pmschema.PMSchemaDTO;
import com.ericsson.oss.air.csac.model.runtime.index.DeployedIndexDefinitionDto;
import com.ericsson.oss.air.csac.repository.DeployedIndexDefinitionDao;
import com.ericsson.oss.air.csac.service.augmentation.ArdqRestClient;
import com.ericsson.oss.air.csac.service.augmentation.AugmentationRestClient;
import com.ericsson.oss.air.csac.service.index.LiveIndexerRestClient;
import com.ericsson.oss.air.csac.service.kpi.pmsc.LivePmscRestClient;
import com.ericsson.oss.air.exception.http.BadRequestException;
import com.ericsson.oss.air.exception.http.ConflictException;
import com.ericsson.oss.air.exception.http.InternalServerErrorException;
import com.ericsson.oss.air.exception.http.NotFoundException;
import com.ericsson.oss.air.exception.http.ServiceUnavailableException;
import com.ericsson.oss.air.exception.http.TooManyRequestsException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

@SpringBootTest
@ActiveProfiles("test")
public class RestClientRetryIntegrationTest {

    private static final String ARDQ_ID = "cardq";

    private static final String ARDQ_URL = "http://cardq:8080";

    private static final String EXCEPTION_MESSAGE = "exception message";

    private static final ServiceUnavailableException SERVICE_UNAVAILABLE_EXCEPTION = ServiceUnavailableException.builder()
            .description(EXCEPTION_MESSAGE).build();

    private static final InternalServerErrorException INTERNAL_SERVER_ERROR_EXCEPTION = InternalServerErrorException.builder()
            .description(EXCEPTION_MESSAGE).build();

    private static final TooManyRequestsException TOO_MANY_REQUESTS_EXCEPTION = TooManyRequestsException.builder()
            .description(EXCEPTION_MESSAGE).build();

    private static final ResourceAccessException RESOURCE_ACCESS_EXCEPTION = new ResourceAccessException(EXCEPTION_MESSAGE);

    private final AugmentationFieldRequestDto augmentationFieldRequestDto = AugmentationFieldRequestDto.builder()
            .output("nsi")
            .input(List.of("snssai", "moFdn"))
            .build();

    private final AugmentationRuleRequestDto augmentationRuleRequestDto = AugmentationRuleRequestDto.builder()
            .inputSchema("5G|PM_COUNTERS|AMF_Mobility_NetworkSlice_1")
            .fields(List.of(augmentationFieldRequestDto))
            .build();

    private final AugmentationRequestDto augmentationRequestDto = AugmentationRequestDto.builder()
            .ardqId(ARDQ_ID)
            .ardqUrl(ARDQ_URL)
            .rules(List.of(augmentationRuleRequestDto))
            .build();

    @Autowired
    private AugmentationRestClient augmentationRestClient;

    @Autowired
    private DataCatalogService dataCatalogService;

    @Autowired
    private SchemaRegRestClient schemaRegRestClient;

    @Autowired
    private ArdqRestClient ardqRestClient;

    @Autowired
    private LivePmscRestClient livePmscRestClient;

    @Autowired
    private LiveIndexerRestClient liveIndexerRestClient;

    @MockBean
    private RestTemplate restTemplate;

    @MockBean
    private DeployedIndexDefinitionDao IndexDefinitionDao;

    private Logger log;

    private ListAppender<ILoggingEvent> listAppender;

    @BeforeEach
    public void setUp() {
        this.log = (Logger) LoggerFactory.getLogger(RetryLogListener.class);
        this.log.setLevel(Level.INFO);

        this.listAppender = new ListAppender<>();
        this.listAppender.start();

        this.log.addAppender(this.listAppender);
    }

    @AfterEach
    public void tearDown() throws Exception {
        this.listAppender.stop();
    }

    @Test
    void augmentation_create_validRetry_serviceUnavailableException() {
        given(this.restTemplate.exchange(anyString(), any(), any(), eq(Void.class)))
                .willThrow(SERVICE_UNAVAILABLE_EXCEPTION);

        assertThrows(ServiceUnavailableException.class, () -> this.augmentationRestClient.create(augmentationRequestDto));
        verify(this.restTemplate, times(3)).exchange(anyString(), any(), any(), eq(Void.class));

        assertLogsWhenRetryAttemptsAreExhausted();
    }

    @Test
    void augmentation_create_noRetry_notFoundException() {
        given(this.restTemplate.exchange(anyString(), any(), any(), eq(Void.class)))
                .willThrow(NotFoundException.class);

        assertThrows(NotFoundException.class, () -> this.augmentationRestClient.create(augmentationRequestDto));
        verify(this.restTemplate, times(1)).exchange(anyString(), any(), any(), eq(Void.class));

        assertNonretryableErrorLogEvents();
    }

    @Test
    void augmentation_update_validRetry_serviceUnavailableException() {
        given(this.restTemplate.exchange(anyString(), any(), any(), eq(Void.class)))
                .willThrow(SERVICE_UNAVAILABLE_EXCEPTION);

        assertThrows(ServiceUnavailableException.class, () -> this.augmentationRestClient.update(augmentationRequestDto));
        verify(this.restTemplate, times(3)).exchange(anyString(), any(), any(), eq(Void.class));

        assertLogsWhenRetryAttemptsAreExhausted();
    }

    @Test
    void augmentation_update_noRetry_notFoundException() {
        given(this.restTemplate.exchange(anyString(), any(), any(), eq(Void.class)))
                .willThrow(NotFoundException.class);

        assertThrows(NotFoundException.class, () -> this.augmentationRestClient.update(augmentationRequestDto));
        verify(this.restTemplate, times(1)).exchange(anyString(), any(), any(), eq(Void.class));

        assertNonretryableErrorLogEvents();
    }

    @Test
    void augmentation_getArdqRegistrationById_validRetry_serviceUnavailableException() {
        given(this.restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(ArdqRegistrationResponseDto.class)))
                .willThrow(SERVICE_UNAVAILABLE_EXCEPTION);

        assertThrows(ServiceUnavailableException.class, () -> this.augmentationRestClient.getArdqRegistrationById("someId"));
        verify(this.restTemplate, times(3)).exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(ArdqRegistrationResponseDto.class));

        assertLogsWhenRetryAttemptsAreExhausted();
    }

    @Test
    void augmentation_getArdqRegistrationById_noRetry_notFoundException() {
        final ParameterizedTypeReference<List<String>> param = new ParameterizedTypeReference<List<String>>() {
        };

        given(this.restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(ArdqRegistrationResponseDto.class)))
                .willThrow(NotFoundException.class);

        assertThrows(NotFoundException.class, () -> this.augmentationRestClient.getArdqRegistrationById("someId"));
        verify(this.restTemplate, times(1)).exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(ArdqRegistrationResponseDto.class));

        assertNonretryableErrorLogEvents();
    }

    @Test
    void augmentation_getAllArdqIds_validRetryTest_serviceUnavailableException() {
        final ParameterizedTypeReference<List<String>> param = new ParameterizedTypeReference<List<String>>() {
        };

        given(this.restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(param)))
                .willThrow(SERVICE_UNAVAILABLE_EXCEPTION);

        assertThrows(ServiceUnavailableException.class, () -> this.augmentationRestClient.getAllArdqIds());
        verify(this.restTemplate, times(3)).exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(param));

        assertLogsWhenRetryAttemptsAreExhausted();
    }

    @Test
    void augmentation_getAllArdqIds_noRetryTest_badRequestException() {
        final ParameterizedTypeReference<List<String>> param = new ParameterizedTypeReference<List<String>>() {
        };

        given(this.restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(param)))
                .willThrow(BadRequestException.class);

        assertThrows(BadRequestException.class, () -> this.augmentationRestClient.getAllArdqIds());
        verify(this.restTemplate, times(1)).exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(param));

        assertNonretryableErrorLogEvents();
    }

    @Test
    void augmentation_delete_validRetryTest_serviceUnavailableException() {
        given(this.restTemplate.exchange(anyString(), eq(HttpMethod.DELETE), any(), eq(Void.class)))
                .willThrow(SERVICE_UNAVAILABLE_EXCEPTION);

        assertThrows(ServiceUnavailableException.class, () -> this.augmentationRestClient.delete("ardqId"));
        verify(this.restTemplate, times(3)).exchange(anyString(), eq(HttpMethod.DELETE), any(), eq(Void.class));

        assertLogsWhenRetryAttemptsAreExhausted();
    }

    @Test
    void augmentation_delete_noRetryTest_notFoundException() {
        given(this.restTemplate.exchange(anyString(), eq(HttpMethod.DELETE), any(), eq(Void.class)))
                .willThrow(NotFoundException.class);

        assertThrows(NotFoundException.class, () -> this.augmentationRestClient.delete("ardqId"));
        verify(this.restTemplate, times(1)).exchange(anyString(), eq(HttpMethod.DELETE), any(), eq(Void.class));

        assertNonretryableErrorLogEvents();
    }

    @Test
    void dataCatalogRestClientValidRetryTest_HttpServerErrorException() {
        given(this.restTemplate.exchange(anyString(), any(), any(), (ParameterizedTypeReference<Object>) any()))
                .willThrow(INTERNAL_SERVER_ERROR_EXCEPTION);

        assertThrows(InternalServerErrorException.class, () -> this.dataCatalogService.getMessageSchema("dataSpace|dataCategory|schema"));
        verify(this.restTemplate, times(3)).exchange(anyString(), any(), any(), (ParameterizedTypeReference<Object>) any());

        assertLogsWhenRetryAttemptsAreExhausted();
    }

    @Test
    void dataCatalogInvalidRetry_conflictException() {
        given(this.restTemplate.exchange(anyString(), any(), any(), (ParameterizedTypeReference<Object>) any()))
                .willThrow(ConflictException.class);

        assertThrows(ConflictException.class, () -> this.dataCatalogService.getMessageSchema("dataSpace|dataCategory|schema"));
        verify(this.restTemplate, times(1)).exchange(anyString(), any(), any(), (ParameterizedTypeReference<Object>) any());

        assertNonretryableErrorLogEvents();
    }

    @Test
    void schemaRegistryRestClientValidRetry() {
        given(this.restTemplate.exchange(eq(URI.create("http://127.0.0.1:8081/subjects/5G%7CPM_COUNTERS%7Cschema/versions/latest/schema")), any(),
                any(), eq(PMSchemaDTO.class)))
                .willThrow(TOO_MANY_REQUESTS_EXCEPTION);

        assertThrows(TooManyRequestsException.class, () -> this.schemaRegRestClient.getSchema("5G|PM_COUNTERS|schema"));
        verify(this.restTemplate, times(3)).exchange(
                eq(URI.create("http://127.0.0.1:8081/subjects/5G%7CPM_COUNTERS%7Cschema/versions/latest/schema")), any(), any(),
                eq(PMSchemaDTO.class));

        assertLogsWhenRetryAttemptsAreExhausted();
    }

    @Test
    void schemaRegistryRestClientNotRecoverableRetry() {
        given(this.restTemplate.exchange(eq(URI.create("http://127.0.0.1:8081/subjects/5G%7CPM_COUNTERS%7Cschema/versions/latest/schema")), any(),
                any(), eq(PMSchemaDTO.class)))
                .willThrow(NotFoundException.class);

        assertThrows(NotFoundException.class, () -> this.schemaRegRestClient.getSchema("5G|PM_COUNTERS|schema"));
        verify(this.restTemplate, times(1)).exchange(
                eq(URI.create("http://127.0.0.1:8081/subjects/5G%7CPM_COUNTERS%7Cschema/versions/latest/schema")), any(), any(),
                eq(PMSchemaDTO.class));

        assertNonretryableErrorLogEvents();
    }

    @Test
    void ardqRestClientValidRetry() {
        given(this.restTemplate.getForEntity(anyString(), eq(String.class)))
                .willThrow(SERVICE_UNAVAILABLE_EXCEPTION);

        assertThrows(ServiceUnavailableException.class, () -> this.ardqRestClient.getArdqQueryTypes("http://localhost:8080"));
        verify(this.restTemplate, times(3)).getForEntity(anyString(), eq(String.class));

        assertLogsWhenRetryAttemptsAreExhausted();
    }

    @Test
    void ardqRestClientNotRecoverableRetry() {
        given(this.restTemplate.getForEntity(anyString(), eq(String.class)))
                .willThrow(NotFoundException.class);

        assertThrows(NotFoundException.class, () -> this.ardqRestClient.getArdqQueryTypes("http://localhost:8080"));
        verify(this.restTemplate, times(1)).getForEntity(anyString(), eq(String.class));

        assertNonretryableErrorLogEvents();
    }

    @Test
    void indexer_create_validRetryTest_serviceUnavailableException() {
        given(this.restTemplate.exchange(anyString(), any(), any(), eq(Void.class)))
                .willThrow(SERVICE_UNAVAILABLE_EXCEPTION);

        assertThrows(ServiceUnavailableException.class, () -> this.liveIndexerRestClient.create(new DeployedIndexDefinitionDto()));
        verify(this.restTemplate, times(3)).exchange(anyString(), any(), any(), eq(Void.class));

        assertLogsWhenRetryAttemptsAreExhausted();
    }

    @Test
    void indexer_create_noRetryTest_badRequestException() {
        given(this.restTemplate.exchange(anyString(), any(), any(), eq(Void.class)))
                .willThrow(BadRequestException.class);

        assertThrows(BadRequestException.class, () -> this.liveIndexerRestClient.create(new DeployedIndexDefinitionDto()));
        verify(this.restTemplate, times(1)).exchange(anyString(), any(), any(), eq(Void.class));

        assertNonretryableErrorLogEvents();
    }

    @Test
    void indexer_update_validRetryTest_serviceUnavailableException() {
        given(this.restTemplate.exchange(anyString(), any(), any(), eq(Void.class)))
                .willThrow(SERVICE_UNAVAILABLE_EXCEPTION);

        assertThrows(ServiceUnavailableException.class, () -> this.liveIndexerRestClient.update(new DeployedIndexDefinitionDto()));
        verify(this.restTemplate, times(3)).exchange(anyString(), any(), any(), eq(Void.class));

        assertLogsWhenRetryAttemptsAreExhausted();
    }

    @Test
    void indexer_update_noRetryTest_badRequestException() {
        given(this.restTemplate.exchange(anyString(), any(), any(), eq(Void.class)))
                .willThrow(BadRequestException.class);

        assertThrows(BadRequestException.class, () -> this.liveIndexerRestClient.update(new DeployedIndexDefinitionDto()));
        verify(this.restTemplate, times(1)).exchange(anyString(), any(), any(), eq(Void.class));

        assertNonretryableErrorLogEvents();
    }

    @Test
    void indexer_deleteAll_validRetrytest_httpServerErrorException() {

        final URI uri = URI.create("http://127.0.0.1:8080/v1/indexer-info/indexer?name=nameOfIndexerA");
        when(this.IndexDefinitionDao.findAll()).thenReturn(List.of(DEPLOYED_INDEX_DEFINITION_DTO_A));
        doThrow(INTERNAL_SERVER_ERROR_EXCEPTION).when(this.restTemplate).delete(uri);

        assertThrows(InternalServerErrorException.class, () -> this.liveIndexerRestClient.deleteAll());
        verify(this.restTemplate, times(3)).delete(uri);

        assertLogsWhenRetryAttemptsAreExhausted();
    }

    @Test
    void indexer_deleteAll_noRetryTest_notFoundException() {
        final URI uri = URI.create("http://127.0.0.1:8080/v1/indexer-info/indexer?name=nameOfIndexerA");
        when(this.IndexDefinitionDao.findAll()).thenReturn(List.of(DEPLOYED_INDEX_DEFINITION_DTO_A));
        doThrow(NotFoundException.class).when(this.restTemplate).delete(uri);

        assertThrows(NotFoundException.class, () -> this.liveIndexerRestClient.delete(List.of(DEPLOYED_INDEX_DEFINITION_DTO_A)));
        verify(this.restTemplate, times(1)).delete(uri);

        assertNonretryableErrorLogEvents();
    }

    @Test
    void pmsc_create_validRetryTest_ServiceUnavailableException() throws Exception {

        final String responseString = FileUtils.readFileToString(new File("src/test/resources/fixtures/pmsc/pmsc_get_kpi_empty_response.json"),
                StandardCharsets.UTF_8);
        final PmscKpiResponseDto responseDto = new ObjectMapper().readValue(responseString, PmscKpiResponseDto.class);
        final ResponseEntity<PmscKpiResponseDto> response = new ResponseEntity<>(responseDto, HttpStatus.OK);

        given(this.restTemplate.getForEntity(anyString(), eq(PmscKpiResponseDto.class))).willReturn(response);
        given(this.restTemplate.exchange(anyString(), any(), any(), eq(Void.class)))
                .willThrow(SERVICE_UNAVAILABLE_EXCEPTION);

        assertThrows(ServiceUnavailableException.class, () -> this.livePmscRestClient.create(KPI_SUBMISSION_DTO));
        verify(this.restTemplate, times(5)).exchange(anyString(), any(), any(), eq(Void.class));

        assertLogsWhenRetryAttemptsAreExhausted(5);
    }

    @Test
    void pmsc_create_doSkipCreate_ServiceUnavailableException() throws Exception {

        given(this.restTemplate.getForEntity(anyString(), eq(PmscKpiResponseDto.class))).willThrow(SERVICE_UNAVAILABLE_EXCEPTION);

        assertThrows(ServiceUnavailableException.class, () -> this.livePmscRestClient.create(KPI_SUBMISSION_DTO));

        assertLogsWhenRetryAttemptsAreExhausted(5);
    }

    @Test
    void pmsc_create_noRetryTest_NotFoundException() throws Exception {

        final String responseString = FileUtils.readFileToString(new File("src/test/resources/fixtures/pmsc/pmsc_get_kpi_empty_response.json"),
                StandardCharsets.UTF_8);
        final PmscKpiResponseDto responseDto = new ObjectMapper().readValue(responseString, PmscKpiResponseDto.class);
        final ResponseEntity<PmscKpiResponseDto> response = new ResponseEntity<>(responseDto, HttpStatus.OK);

        given(this.restTemplate.getForEntity(anyString(), eq(PmscKpiResponseDto.class))).willReturn(response);
        given(this.restTemplate.exchange(anyString(), any(), any(), eq(Void.class)))
                .willThrow(NotFoundException.class);

        assertThrows(NotFoundException.class, () -> this.livePmscRestClient.create(KPI_SUBMISSION_DTO));
        verify(this.restTemplate, times(1)).exchange(anyString(), any(), any(), eq(Void.class));

        assertNonretryableErrorLogEvents();
    }

    @Test
    void pmsc_create_validRetryTest_resourceAccessException() throws Exception {

        final String responseString = FileUtils.readFileToString(new File("src/test/resources/fixtures/pmsc/pmsc_get_kpi_empty_response.json"),
                StandardCharsets.UTF_8);
        final PmscKpiResponseDto responseDto = new ObjectMapper().readValue(responseString, PmscKpiResponseDto.class);
        final ResponseEntity<PmscKpiResponseDto> response = new ResponseEntity<>(responseDto, HttpStatus.OK);

        given(this.restTemplate.getForEntity(anyString(), eq(PmscKpiResponseDto.class))).willReturn(response);

        given(this.restTemplate.exchange(anyString(), any(), any(), eq(Void.class)))
                .willThrow(new ResourceAccessException(EXCEPTION_MESSAGE));

        assertThrows(ResourceAccessException.class, () -> this.livePmscRestClient.create(KPI_SUBMISSION_DTO));
        verify(this.restTemplate, times(5)).exchange(anyString(), any(), any(), eq(Void.class));

        assertLogsWhenRetryAttemptsAreExhausted(5);
    }

    @Test
    void pmscRestClient_deleteAllValidRetry() throws IOException {
        final String responseString = FileUtils.readFileToString(new File("src/test/resources/fixtures/pmsc/pmsc_get_kpi_response.json"),
                StandardCharsets.UTF_8);
        final PmscKpiResponseDto responseDto = new ObjectMapper().readValue(responseString, PmscKpiResponseDto.class);
        final ResponseEntity<PmscKpiResponseDto> response = new ResponseEntity<>(responseDto, HttpStatus.OK);

        given(this.restTemplate.getForEntity(anyString(), eq(PmscKpiResponseDto.class))).willReturn(response);
        given(this.restTemplate.exchange(anyString(), any(), any(), eq(Void.class))).willThrow(SERVICE_UNAVAILABLE_EXCEPTION);

        assertThrows(ServiceUnavailableException.class, () -> this.livePmscRestClient.deleteAll());
        verify(this.restTemplate, times(5)).exchange(anyString(), any(), any(), eq(Void.class));

        assertLogsWhenRetryAttemptsAreExhausted(5);
    }

    @Test
    void pmscRestClient_deleteAllInvalidRetry() {
        given(this.restTemplate.getForEntity(anyString(), eq(PmscKpiResponseDto.class))).willThrow(BadRequestException.class);

        assertThrows(BadRequestException.class, () -> this.livePmscRestClient.deleteAll());
        verify(this.restTemplate, times(1)).getForEntity(anyString(), eq(PmscKpiResponseDto.class));

        assertNonretryableErrorLogEvents();
    }

    @Test
    void pmscRestClient_deleteValidRetry() {
        given(this.restTemplate.exchange(anyString(), any(), any(), eq(Void.class))).willThrow(SERVICE_UNAVAILABLE_EXCEPTION);
        assertThrows(ServiceUnavailableException.class, () -> this.livePmscRestClient.delete(List.of(DEPLOYED_SIMPLE_KPI_OBJ)));
        verify(this.restTemplate, times(5)).exchange(anyString(), any(), any(), eq(Void.class));

        assertLogsWhenRetryAttemptsAreExhausted(5);

    }

    @Test
    void pmscRestClient_delete_noRetryTest_notFoundException() {
        given(this.restTemplate.exchange(anyString(), any(), any(), eq(Void.class))).willThrow(NotFoundException.class);
        assertThrows(NotFoundException.class, () -> this.livePmscRestClient.delete(List.of(DEPLOYED_SIMPLE_KPI_OBJ)));
        verify(this.restTemplate, times(1)).exchange(anyString(), any(), any(), eq(Void.class));

        assertNonretryableErrorLogEvents();
    }

    @Test
    void validRetry_serviceUnavailableException_attemptSucceeds() {
        given(this.restTemplate.exchange(anyString(), any(), any(), eq(Void.class)))
                .willThrow(SERVICE_UNAVAILABLE_EXCEPTION)
                .willThrow(SERVICE_UNAVAILABLE_EXCEPTION)
                .willReturn(ResponseEntity.ok().build());

        this.augmentationRestClient.create(augmentationRequestDto);
        verify(this.restTemplate, times(3)).exchange(anyString(), any(), any(), eq(Void.class));

        assertLogsWhenLastRetryAttemptPasses();
    }

    @Test
    void validRetry_serviceUnavailableException_failsOnRetryAttempt_UnrecoverableError() {
        given(this.restTemplate.exchange(anyString(), any(), any(), eq(Void.class)))
                .willThrow(SERVICE_UNAVAILABLE_EXCEPTION)
                .willThrow(NotFoundException.class);

        assertThrows(NotFoundException.class, () -> this.augmentationRestClient.create(augmentationRequestDto));
        verify(this.restTemplate, times(2)).exchange(anyString(), any(), any(), eq(Void.class));

        assertLogsWhenRetryAttemptsFailDueToUnrecoverableIssue();
    }

    private void assertLogsWhenRetryAttemptsAreExhausted() {
        this.assertLogsWhenRetryAttemptsAreExhausted(3);
    }

    private void assertLogsWhenRetryAttemptsAreExhausted(final int numOfRetries) {

        final List<ILoggingEvent> loggingEventList = this.listAppender.list;
        assertFalse(loggingEventList.isEmpty());

        assertEquals(numOfRetries + 1, loggingEventList.size());

        for (int idx = 0; idx < numOfRetries; idx++) {

            final ILoggingEvent loggingEvent = loggingEventList.get(idx);

            assertEquals(Level.ERROR, loggingEvent.getLevel());
            assertEquals("Attempt " + (idx + 1) + " failed: " + EXCEPTION_MESSAGE + ". Retrying.", loggingEvent.getFormattedMessage());
            assertFalse(loggingEvent.getMDCPropertyMap().isEmpty());
            assertEquals(2, loggingEvent.getMDCPropertyMap().size());
            assertEquals(FACILITY_VALUE, loggingEvent.getMDCPropertyMap().get(FACILITY_KEY));
            assertEquals(SUBJECT_VALUE, loggingEvent.getMDCPropertyMap().get(SUBJECT_KEY));
        }

        final ILoggingEvent loggingEvent = loggingEventList.get(numOfRetries);

        assertEquals(Level.ERROR, loggingEvent.getLevel());
        assertEquals("Retries exhausted after " + numOfRetries + " attempts. Cause: ", loggingEvent.getFormattedMessage());
        assertFalse(loggingEvent.getMDCPropertyMap().isEmpty());
        assertEquals(2, loggingEvent.getMDCPropertyMap().size());
        assertEquals(FACILITY_VALUE, loggingEvent.getMDCPropertyMap().get(FACILITY_KEY));
        assertEquals(SUBJECT_VALUE, loggingEvent.getMDCPropertyMap().get(SUBJECT_KEY));

    }

    private void assertLogsWhenLastRetryAttemptPasses() {
        this.assertLogsWhenLastRetryAttemptPasses(3);
    }

    private void assertLogsWhenLastRetryAttemptPasses(final int numOfRetries) {
        final List<ILoggingEvent> loggingEventList = this.listAppender.list;
        assertFalse(loggingEventList.isEmpty());

        assertEquals(numOfRetries, loggingEventList.size());

        for (int idx = 0; idx < numOfRetries - 1; idx++) {

            final ILoggingEvent loggingEvent = loggingEventList.get(idx);

            assertEquals(Level.ERROR, loggingEvent.getLevel());
            assertEquals("Attempt " + (idx + 1) + " failed: " + EXCEPTION_MESSAGE + ". Retrying.", loggingEvent.getFormattedMessage());
            assertFalse(loggingEvent.getMDCPropertyMap().isEmpty());
            assertEquals(2, loggingEvent.getMDCPropertyMap().size());
            assertEquals(FACILITY_VALUE, loggingEvent.getMDCPropertyMap().get(FACILITY_KEY));
            assertEquals(SUBJECT_VALUE, loggingEvent.getMDCPropertyMap().get(SUBJECT_KEY));
        }

        final ILoggingEvent loggingEvent = loggingEventList.get(numOfRetries - 1);

        assertEquals(Level.INFO, loggingEvent.getLevel());
        assertEquals("Attempt " + numOfRetries + " successful for previous exception: " + EXCEPTION_MESSAGE, loggingEvent.getFormattedMessage());
        assertFalse(loggingEvent.getMDCPropertyMap().isEmpty());
        assertEquals(2, loggingEvent.getMDCPropertyMap().size());
        assertEquals(FACILITY_VALUE, loggingEvent.getMDCPropertyMap().get(FACILITY_KEY));
        assertEquals(SUBJECT_VALUE, loggingEvent.getMDCPropertyMap().get(SUBJECT_KEY));

    }

    private void assertLogsWhenRetryAttemptsFailDueToUnrecoverableIssue() {
        this.assertLogsWhenRetryAttemptsFailDueToUnrecoverableIssue(2);
    }

    private void assertLogsWhenRetryAttemptsFailDueToUnrecoverableIssue(final int numOfRetries) {

        final List<ILoggingEvent> loggingEventList = this.listAppender.list;
        assertFalse(loggingEventList.isEmpty());

        assertEquals(numOfRetries, loggingEventList.size());

        for (int idx = 0; idx < numOfRetries - 1; idx++) {

            final ILoggingEvent loggingEvent = loggingEventList.get(idx);

            assertEquals(Level.ERROR, loggingEvent.getLevel());
            assertEquals("Attempt " + (idx + 1) + " failed: " + EXCEPTION_MESSAGE + ". Retrying.", loggingEvent.getFormattedMessage());
            assertFalse(loggingEvent.getMDCPropertyMap().isEmpty());
            assertEquals(2, loggingEvent.getMDCPropertyMap().size());
            assertEquals(FACILITY_VALUE, loggingEvent.getMDCPropertyMap().get(FACILITY_KEY));
            assertEquals(SUBJECT_VALUE, loggingEvent.getMDCPropertyMap().get(SUBJECT_KEY));
        }

        final ILoggingEvent loggingEvent = loggingEventList.get(numOfRetries - 1);

        assertEquals(Level.ERROR, loggingEvent.getLevel());
        assertEquals("Retries exhausted after " + numOfRetries + " attempts. Cause: ", loggingEvent.getFormattedMessage());
        assertFalse(loggingEvent.getMDCPropertyMap().isEmpty());
        assertEquals(2, loggingEvent.getMDCPropertyMap().size());
        assertEquals(FACILITY_VALUE, loggingEvent.getMDCPropertyMap().get(FACILITY_KEY));
        assertEquals(SUBJECT_VALUE, loggingEvent.getMDCPropertyMap().get(SUBJECT_KEY));

    }

    private void assertNonretryableErrorLogEvents() {

        final List<ILoggingEvent> loggingEventList = this.listAppender.list;
        assertTrue(loggingEventList.isEmpty());

    }

}
