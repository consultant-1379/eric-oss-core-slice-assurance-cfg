/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.service.kpi.pmsc.legacy;

import static com.ericsson.oss.air.csac.model.TestResourcesUtils.DEPLOYED_SIMPLE_KPI_OBJ;
import static com.ericsson.oss.air.csac.service.kpi.pmsc.legacy.PMSCRestClient.PMSC_KPIS_CALCULATION_API;
import static com.ericsson.oss.air.csac.service.kpi.pmsc.legacy.PMSCRestClient.PMSC_KPIS_DEFINITIONS_API;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import com.ericsson.oss.air.csac.model.pmsc.KpiCalculationDTO;
import com.ericsson.oss.air.csac.model.pmsc.KpiDefinitionSubmission;
import com.ericsson.oss.air.csac.model.pmsc.LegacyKpiSubmissionDto;
import com.ericsson.oss.air.csac.model.pmsc.ParameterDTO;
import com.ericsson.oss.air.csac.service.exception.PmscHttpResponseErrorHandler;
import com.ericsson.oss.air.exception.http.ConflictException;
import com.ericsson.oss.air.exception.http.ServiceUnavailableException;
import com.ericsson.oss.air.exception.http.TooManyRequestsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
class PMSCRestClientTest {

    private static final String PMSC_URL = "http://localhost:8080";

    static final ParameterDTO PARAMETER_DTO = new ParameterDTO("id", "filter");

    static final KpiCalculationDTO KPI_CALCULATION_DTO_OBJECT = new KpiCalculationDTO("source", List.of("kpi names"), PARAMETER_DTO);

    static final KpiDefinitionSubmission KPI_DEFINITIONS_SUBMISSION = new LegacyKpiSubmissionDto("source",
            List.of(DEPLOYED_SIMPLE_KPI_OBJ));

    @Value("${provisioning.pmsc.restClient.url}")
    private String pmscUrl;

    @Mock
    private RestTemplate restTemplate;

    private PMSCRestClient pmscRestClient;

    @BeforeEach
    void setUp() {
        this.pmscRestClient = new PMSCRestClient(restTemplate);
    }

    @Test
    void updatePMSCKpisDefinitions_validURL_successResponse() {
        final String validURL = this.pmscUrl + PMSC_KPIS_DEFINITIONS_API;
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        final HttpEntity<KpiDefinitionSubmission> entity = new HttpEntity<>(KPI_DEFINITIONS_SUBMISSION, headers);
        final ResponseEntity<Void> response = ResponseEntity.ok(null);

        when(this.restTemplate.exchange(anyString(), eq(HttpMethod.PUT), any(), eq(Void.class))).thenReturn(response);

        this.pmscRestClient.updatePMSCKpisDefinitions(KPI_DEFINITIONS_SUBMISSION);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(this.restTemplate, times(1)).exchange(validURL, HttpMethod.PUT, entity, Void.class);
        verify(this.restTemplate, times(1)).setErrorHandler(any(PmscHttpResponseErrorHandler.class));
    }

    @Test
    void createPMSCKpisCalculation_validURL_successResponse() {
        final String validURL = this.pmscUrl + PMSC_KPIS_CALCULATION_API;
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        final HttpEntity<KpiCalculationDTO> entity = new HttpEntity<>(KPI_CALCULATION_DTO_OBJECT, headers);
        final ResponseEntity<Void> response = ResponseEntity.ok(null);

        when(this.restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), eq(Void.class))).thenReturn(response);

        this.pmscRestClient.createPMSCKpisCalculation(KPI_CALCULATION_DTO_OBJECT);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(this.restTemplate, times(1)).exchange(validURL, HttpMethod.POST, entity, Void.class);
        verify(this.restTemplate, times(1)).setErrorHandler(any(PmscHttpResponseErrorHandler.class));
    }

    @Test
    public void updatePMSCKpisDefinitions_validURL_conflictResponse() {
        final String validURL = this.pmscUrl + PMSC_KPIS_DEFINITIONS_API;
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        final HttpEntity<KpiDefinitionSubmission> entity = new HttpEntity<>(KPI_DEFINITIONS_SUBMISSION, headers);

        when(this.restTemplate.exchange(anyString(), eq(HttpMethod.PUT), any(), eq(Void.class))).thenThrow(ConflictException.class);

        assertThrows(ConflictException.class, () -> this.pmscRestClient.updatePMSCKpisDefinitions(KPI_DEFINITIONS_SUBMISSION));

        verify(this.restTemplate, times(1)).exchange(validURL, HttpMethod.PUT, entity, Void.class);
        verify(this.restTemplate, times(1)).setErrorHandler(any(PmscHttpResponseErrorHandler.class));
    }

    @Test
    public void createPMSCKpisCalculation_validURL_tooManyResponse() {
        final String validURL = this.pmscUrl + PMSC_KPIS_CALCULATION_API;

        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        final HttpEntity<KpiCalculationDTO> entity = new HttpEntity<>(KPI_CALCULATION_DTO_OBJECT, headers);

        when(this.restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), eq(Void.class))).thenThrow(TooManyRequestsException.class);

        assertThrows(TooManyRequestsException.class, () -> this.pmscRestClient.createPMSCKpisCalculation(KPI_CALCULATION_DTO_OBJECT));

        verify(this.restTemplate, times(1)).exchange(validURL, HttpMethod.POST, entity, Void.class);
        verify(this.restTemplate, times(1)).setErrorHandler(any(PmscHttpResponseErrorHandler.class));
    }

    @Test
    public void createPMSCKpisCalculation_validURL_serviceUnavailable() {
        final String validURL = this.pmscUrl + PMSC_KPIS_CALCULATION_API;
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        final HttpEntity<KpiCalculationDTO> entity = new HttpEntity<>(KPI_CALCULATION_DTO_OBJECT, headers);

        when(this.restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), eq(Void.class))).thenThrow(ServiceUnavailableException.class);

        assertThrows(ServiceUnavailableException.class, () -> this.pmscRestClient.createPMSCKpisCalculation(KPI_CALCULATION_DTO_OBJECT));
        verify(this.restTemplate, times(1)).exchange(validURL, HttpMethod.POST, entity, Void.class);
        verify(this.restTemplate, times(1)).setErrorHandler(any(PmscHttpResponseErrorHandler.class));
    }

    @Test
    public void checkUrl() {

        assertDoesNotThrow(() -> this.pmscRestClient.checkUrl(PMSC_URL + PMSC_KPIS_DEFINITIONS_API));
        assertDoesNotThrow(() -> this.pmscRestClient.checkUrl(PMSC_URL + PMSC_KPIS_CALCULATION_API));
    }

}
