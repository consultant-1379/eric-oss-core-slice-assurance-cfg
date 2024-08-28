/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.service.augmentation;

import java.util.List;

import com.ericsson.oss.air.csac.configuration.augmentation.AugmentationProperties;
import com.ericsson.oss.air.csac.model.augmentation.ArdqRegistrationResponseDto;
import com.ericsson.oss.air.csac.model.augmentation.AugmentationRequestDto;
import com.ericsson.oss.air.csac.service.exception.HttpResponseErrorHandler;
import com.ericsson.oss.air.exception.http.InternalServerErrorException;
import com.ericsson.oss.air.exception.http.ServiceUnavailableException;
import com.ericsson.oss.air.exception.http.TooManyRequestsException;
import com.ericsson.oss.air.util.codec.Codec;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
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
 * Rest client for Assurance Augmentation Service(AAS).
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AugmentationRestClient {

    private static final String ARDQ_REGISTRATION_URI = "/v1/augmentation/registration/ardq";

    private final RestTemplate restTemplate;

    private final AugmentationProperties augmentationProperties;

    private final Codec codec;

    /**
     * Creates provided augmentation in AAS.
     *
     * @param augmentationRequestDto {@link AugmentationRequestDto}
     */
    // Use resilience4j configuration as workaround for the ticket ESOA-4203
    @Retryable(maxAttemptsExpression = "#{${resilience4j.retry.instances.aas.max-attempts}}",
               backoff = @Backoff(delayExpression = "#{${resilience4j.retry.instances.aas.wait-duration}}",
                                  multiplierExpression = "#{${resilience4j.retry.instances.aas.exponential-backoff-multiplier}}",
                                  maxDelayExpression = "#{${resilience4j.retry.instances.aas.maxDelay}}"),
               retryFor = { InternalServerErrorException.class, TooManyRequestsException.class, ServiceUnavailableException.class,
                       ResourceAccessException.class })
    @SneakyThrows
    public void create(final AugmentationRequestDto augmentationRequestDto) {
        log.debug("Submitting new augmentation definition: {}", this.codec.writeValueAsString(augmentationRequestDto));

        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        final HttpEntity<AugmentationRequestDto> requestEntity = new HttpEntity<>(augmentationRequestDto, headers);

        final String url = this.augmentationProperties.getAasUrl() + ARDQ_REGISTRATION_URI;
        final String endpoint = "POST " + url + " for " + augmentationRequestDto.getArdqId();

        this.restTemplate.setErrorHandler(new HttpResponseErrorHandler(endpoint));

        log.info("Augmentation request: {}", endpoint);

        final ResponseEntity<Void> response = this.restTemplate.exchange(url,
                HttpMethod.POST,
                requestEntity,
                Void.class);

        log.info("Augmentation response: {} - {}", response.getStatusCode().value(), ((HttpStatus) response.getStatusCode()).getReasonPhrase());
    }

    /**
     * Updates provided augmentation in AAS.
     *
     * @param augmentationRequestDto {@link AugmentationRequestDto}
     */
    // Use resilience4j configuration as workaround for the ticket ESOA-4203
    @Retryable(maxAttemptsExpression = "#{${resilience4j.retry.instances.aas.max-attempts}}",
               backoff = @Backoff(delayExpression = "#{${resilience4j.retry.instances.aas.wait-duration}}",
                                  multiplierExpression = "#{${resilience4j.retry.instances.aas.exponential-backoff-multiplier}}",
                                  maxDelayExpression = "#{${resilience4j.retry.instances.aas.maxDelay}}"),
               retryFor = { InternalServerErrorException.class, TooManyRequestsException.class, ServiceUnavailableException.class,
                       ResourceAccessException.class })
    @SneakyThrows
    public void update(final AugmentationRequestDto augmentationRequestDto) {
        log.debug("Updating augmentation definition: {}", this.codec.writeValueAsString(augmentationRequestDto));

        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        final HttpEntity<AugmentationRequestDto> requestEntity = new HttpEntity<>(augmentationRequestDto, headers);

        final String url = this.augmentationProperties.getAasUrl() + ARDQ_REGISTRATION_URI;
        final String endpoint = "PUT " + url + " for " + augmentationRequestDto.getArdqId();

        this.restTemplate.setErrorHandler(new HttpResponseErrorHandler(endpoint));

        log.info("Augmentation request: {}", endpoint);

        final ResponseEntity<Void> response = this.restTemplate.exchange(url,
                HttpMethod.PUT,
                requestEntity,
                Void.class);

        log.info("Augmentation response: {} - {}", response.getStatusCode().value(), ((HttpStatus) response.getStatusCode()).getReasonPhrase());
    }

    /**
     * Deletes an augmentation in AAS identified by given id.
     *
     * @param ardqId Augmentation id.
     */
    // Use resilience4j configuration as workaround for the ticket ESOA-4203
    @Retryable(maxAttemptsExpression = "#{${resilience4j.retry.instances.aas.max-attempts}}",
               backoff = @Backoff(delayExpression = "#{${resilience4j.retry.instances.aas.wait-duration}}",
                                  multiplierExpression = "#{${resilience4j.retry.instances.aas.exponential-backoff-multiplier}}",
                                  maxDelayExpression = "#{${resilience4j.retry.instances.aas.maxDelay}}"),
               retryFor = { InternalServerErrorException.class, TooManyRequestsException.class, ServiceUnavailableException.class,
                       ResourceAccessException.class })
    public void delete(final String ardqId) {
        log.debug("Deleting augmentation: {}", ardqId);

        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        final HttpEntity<?> httpEntity = new HttpEntity<>(headers);

        final String url = this.augmentationProperties.getAasUrl() + ARDQ_REGISTRATION_URI + '/' + ardqId;
        final String endpoint = "DELETE " + url;

        this.restTemplate.setErrorHandler(new HttpResponseErrorHandler(endpoint));

        log.info("Augmentation request: {}", endpoint);

        final ResponseEntity<Void> response = this.restTemplate.exchange(
                url,
                HttpMethod.DELETE,
                httpEntity,
                Void.class);

        log.info("Augmentation response: {} - {}", response.getStatusCode().value(), ((HttpStatus) response.getStatusCode()).getReasonPhrase());
    }

    /**
     * Retrieve ardq registration details from AAS.
     *
     * @param ardqId ardq_id of the registration
     * @return {@link ArdqRegistrationResponseDto}
     */
    // Use resilience4j configuration as workaround for the ticket ESOA-4203
    @Retryable(maxAttemptsExpression = "#{${resilience4j.retry.instances.aas.max-attempts}}",
               backoff = @Backoff(delayExpression = "#{${resilience4j.retry.instances.aas.wait-duration}}",
                                  multiplierExpression = "#{${resilience4j.retry.instances.aas.exponential-backoff-multiplier}}",
                                  maxDelayExpression = "#{${resilience4j.retry.instances.aas.maxDelay}}"),
               retryFor = { InternalServerErrorException.class, TooManyRequestsException.class, ServiceUnavailableException.class,
                       ResourceAccessException.class })
    public ArdqRegistrationResponseDto getArdqRegistrationById(final String ardqId) {
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        final HttpEntity<String> entity = new HttpEntity<>(headers);

        final String requestUrl = this.augmentationProperties.getAasUrl() + ARDQ_REGISTRATION_URI + "/" + ardqId;
        final String endpoint = "GET " + requestUrl;

        this.restTemplate.setErrorHandler(new HttpResponseErrorHandler(endpoint));

        log.info("Augmentation request: {}", endpoint);

        final ResponseEntity<ArdqRegistrationResponseDto> response = this.restTemplate.exchange(requestUrl, HttpMethod.GET, entity,
                ArdqRegistrationResponseDto.class);

        log.info("Augmentation response: {} - {}", response.getStatusCode().value(), ((HttpStatus) response.getStatusCode()).getReasonPhrase());

        return response.getBody();
    }

    /**
     * Returns a list of all ARDQ Ids configured in the AAS.
     *
     * @return a list of all ARDQ Ids configured in the AAS
     */
    @Retryable(maxAttemptsExpression = "#{${resilience4j.retry.instances.aas.max-attempts}}",
               backoff = @Backoff(delayExpression = "#{${resilience4j.retry.instances.aas.wait-duration}}",
                                  multiplierExpression = "#{${resilience4j.retry.instances.aas.exponential-backoff-multiplier}}",
                                  maxDelayExpression = "#{${resilience4j.retry.instances.aas.maxDelay}}"),
               retryFor = { InternalServerErrorException.class, TooManyRequestsException.class, ServiceUnavailableException.class,
                       ResourceAccessException.class })
    public List<String> getAllArdqIds() {

        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        final HttpEntity<String> entity = new HttpEntity<>(headers);

        final String requestUrl = this.augmentationProperties.getAasUrl() + "/v1/augmentation/registration/ardq-ids";
        final String endpoint = "GET " + requestUrl;

        this.restTemplate.setErrorHandler(new HttpResponseErrorHandler(endpoint));

        log.info("Augmentation request: {}", endpoint);

        final ResponseEntity<List<String>> response = this.restTemplate.exchange(requestUrl, HttpMethod.GET, entity,
                new ParameterizedTypeReference<List<String>>() {
                });

        log.info("Augmentation response: {} - {}", response.getStatusCode().value(), ((HttpStatus) response.getStatusCode()).getReasonPhrase());

        return response.getBody();
    }
}

