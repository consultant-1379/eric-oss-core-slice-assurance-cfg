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

import com.ericsson.oss.air.csac.model.pmsc.KpiCalculationDTO;
import com.ericsson.oss.air.csac.model.pmsc.KpiDefinitionSubmission;
import com.ericsson.oss.air.csac.service.exception.PmscHttpResponseErrorHandler;
import com.ericsson.oss.air.exception.http.InternalServerErrorException;
import com.ericsson.oss.air.exception.http.ServiceUnavailableException;
import com.ericsson.oss.air.exception.http.TooManyRequestsException;
import com.ericsson.oss.air.util.codec.Codec;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

/**
 * PM Status Calculator (PMSC) rest client to update KPI Definitions and create KPI Calculations.
 * <p> This lazy rest client bean will be initialized when being used<p/>
 */
@Service
@Slf4j
@ConditionalOnProperty(prefix = "provisioning.pmsc.restClient",
                       name = "url")
@Profile({ "prod", "test" })
@RequiredArgsConstructor
public class PMSCRestClient implements PmscProvisioningService {

    static final String PMSC_KPIS_DEFINITIONS_API = "/son-om/kpi/v1/kpis/definitions";

    static final String PMSC_KPIS_CALCULATION_API = "/son-om/kpi/v1/kpis/calculation";

    final Codec codec = new Codec();

    private final RestTemplate restTemplate;

    @Value("${provisioning.pmsc.restClient.url}")
    private String pmscUrl;

    /**
     * Update PMSC Kpis Definitions.
     *
     * @param kpiDefinitionSubmission Kpi definition submission to be submitted to PMSC
     * @return The HTTP response of type {@link KpiDefinitionSubmission} from PMSC Kpis Definitions
     */
    @Override
    @SneakyThrows
    // Use resilience4j configuration as workaround for the ticket ESOA-4203
    @Retryable(maxAttemptsExpression = "#{${resilience4j.retry.instances.pmsc.max-attempts}}",
               backoff = @Backoff(delayExpression = "#{${resilience4j.retry.instances.pmsc.wait-duration}}",
                                  multiplierExpression = "#{${resilience4j.retry.instances.pmsc.exponential-backoff-multiplier}}",
                                  maxDelayExpression = "#{${resilience4j.retry.instances.pmsc.maxDelay}}"),
               retryFor = { InternalServerErrorException.class, TooManyRequestsException.class, ServiceUnavailableException.class,
                       ResourceAccessException.class })
    public ResponseEntity<Void> updatePMSCKpisDefinitions(final KpiDefinitionSubmission kpiDefinitionSubmission) {

        final HttpHeaders headers = new HttpHeaders();

        headers.setContentType(MediaType.APPLICATION_JSON);
        final HttpEntity<KpiDefinitionSubmission> entity = new HttpEntity<>(kpiDefinitionSubmission, headers);

        log.info("Provisioning KPI submission: {}", this.codec.writeValueAsString(kpiDefinitionSubmission));

        final String url = this.pmscUrl + PMSC_KPIS_DEFINITIONS_API;
        final String endpoint = "PUT " + url;

        log.info("PMSC request: {}", endpoint);

        this.restTemplate.setErrorHandler(new PmscHttpResponseErrorHandler(endpoint));

        final ResponseEntity<Void> response = this.restTemplate.exchange(url, HttpMethod.PUT, entity, Void.class);

        log.info("PMSC response: {} - {}", response.getStatusCode().value(), ((HttpStatus) response.getStatusCode()).getReasonPhrase());

        log.info("KPI submission provisioning successful");

        return response;
    }

    /**
     * Create PMSC Kpi Calculation.
     *
     * @param kpiCalculationDTO PMSC KpiCalculationDTO object
     * @return The HTTP response of type {@link KpiCalculationDTO} from PMSC Kpis Calculation
     */
    @Override
    @SneakyThrows
    // Use resilience4j configuration as workaround for the ticket ESOA-4203
    @Retryable(maxAttemptsExpression = "#{${resilience4j.retry.instances.pmsc.max-attempts}}",
               backoff = @Backoff(delayExpression = "#{${resilience4j.retry.instances.pmsc.wait-duration}}",
                                  multiplierExpression = "#{${resilience4j.retry.instances.pmsc.exponential-backoff-multiplier}}",
                                  maxDelayExpression = "#{${resilience4j.retry.instances.pmsc.maxDelay}}"),
               retryFor = { InternalServerErrorException.class, TooManyRequestsException.class, ServiceUnavailableException.class,
                       ResourceAccessException.class })
    public ResponseEntity<Void> createPMSCKpisCalculation(final KpiCalculationDTO kpiCalculationDTO) {

        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        final HttpEntity<KpiCalculationDTO> entity = new HttpEntity<>(kpiCalculationDTO, headers);

        log.info("Submitting KPI calculation request: {}", this.codec.writeValueAsString(kpiCalculationDTO));

        final String url = this.pmscUrl + PMSC_KPIS_CALCULATION_API;
        final String endpoint = "POST " + url;

        this.restTemplate.setErrorHandler(new PmscHttpResponseErrorHandler(endpoint));
        final ResponseEntity<Void> response = this.restTemplate.exchange(url, HttpMethod.POST, entity,
                Void.class);
        log.info("KPI calculation request successful");

        return response;

    }

}
