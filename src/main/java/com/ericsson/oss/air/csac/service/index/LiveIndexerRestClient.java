/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/
package com.ericsson.oss.air.csac.service.index;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.ericsson.oss.air.csac.model.runtime.index.DeployedIndexDefinitionDto;
import com.ericsson.oss.air.csac.repository.DeployedIndexDefinitionDao;
import com.ericsson.oss.air.csac.service.exception.HttpResponseErrorHandler;
import com.ericsson.oss.air.exception.http.InternalServerErrorException;
import com.ericsson.oss.air.exception.http.ServiceUnavailableException;
import com.ericsson.oss.air.exception.http.TooManyRequestsException;
import com.ericsson.oss.air.util.codec.Codec;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
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
import org.springframework.util.ObjectUtils;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Live implementation of the {@link IndexerRestClient}. This client submits requests to a live AIS service.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Primary
@Profile({ "prod", "test" })
public class LiveIndexerRestClient implements IndexerRestClient {

    static final String AIS_INDEXER_URI = "/v1/indexer-info/indexer";

    private final Codec codec = new Codec();

    private final RestTemplate restTemplate;

    private final DeployedIndexDefinitionDao definitionDao;

    @Value("${provisioning.index.url}")
    private String indexerUrl;

    @Value("${provisioning.index.legacy:true}")
    private boolean isLegacyIndexClient;

    // Use resilience4j configuration as workaround for the ticket ESOA-4203
    @Override
    @Retryable(maxAttemptsExpression = "#{${resilience4j.retry.instances.index.max-attempts}}",
               backoff = @Backoff(delayExpression = "#{${resilience4j.retry.instances.index.wait-duration}}",
                                  multiplierExpression = "#{${resilience4j.retry.instances.index.exponential-backoff-multiplier}}",
                                  maxDelayExpression = "#{${resilience4j.retry.instances.index.maxDelay}}"),
               retryFor = { InternalServerErrorException.class, TooManyRequestsException.class, ServiceUnavailableException.class,
                       ResourceAccessException.class })
    public ResponseEntity<Void> create(final DeployedIndexDefinitionDto indexDto) {
        return this.sendRequest("Creating", indexDto);
    }

    // Use resilience4j configuration as workaround for the ticket ESOA-4203
    @Override
    @Retryable(maxAttemptsExpression = "#{${resilience4j.retry.instances.index.max-attempts}}",
               backoff = @Backoff(delayExpression = "#{${resilience4j.retry.instances.index.wait-duration}}",
                                  multiplierExpression = "#{${resilience4j.retry.instances.index.exponential-backoff-multiplier}}",
                                  maxDelayExpression = "#{${resilience4j.retry.instances.index.maxDelay}}"),
               retryFor = { InternalServerErrorException.class, TooManyRequestsException.class, ServiceUnavailableException.class,
                       ResourceAccessException.class })
    public ResponseEntity<Void> update(final DeployedIndexDefinitionDto indexDto) {
        return this.sendRequest("Updating", indexDto);
    }

    @Override
    public void deleteById(final List<String> ids) {
        if (ObjectUtils.isEmpty(ids)) {
            log.info("Nothing to be deleted in AIS");
        }
        ids.forEach(id -> {
            final URI url = UriComponentsBuilder.fromUriString(this.indexerUrl).path(AIS_INDEXER_URI).queryParam("name", id).build(id);

            final String endpoint = "DELETE " + url;

            log.info("AIS request: {}", endpoint);
            this.restTemplate.setErrorHandler(new HttpResponseErrorHandler(endpoint));
            this.restTemplate.delete(url);
        });
        log.info("Successfully deleted list of index definitions in AIS: {}", ids);
    }

    @Override
    public void delete(final List<DeployedIndexDefinitionDto> definitionDtos) {
        final List<String> indexIds = definitionDtos.stream().map(DeployedIndexDefinitionDto::indexDefinitionName).collect(Collectors.toList());
        log.info("Deleting the list of deployed index definitions: {}", indexIds);
        this.deleteById(indexIds);
    }

    @Override
    @SneakyThrows
    @Retryable(maxAttemptsExpression = "#{${resilience4j.retry.instances.index.max-attempts}}",
               backoff = @Backoff(delayExpression = "#{${resilience4j.retry.instances.index.wait-duration}}",
                                  multiplierExpression = "#{${resilience4j.retry.instances.index.exponential-backoff-multiplier}}",
                                  maxDelayExpression = "#{${resilience4j.retry.instances.index.maxDelay}}"),
               retryFor = { InternalServerErrorException.class, TooManyRequestsException.class, ServiceUnavailableException.class,
                       ResourceAccessException.class })
    public void deleteAll() {
        final List<String> ids = new ArrayList<>();
        this.definitionDao.findAll().forEach(deployedIndexDefinitionDto -> ids.add(deployedIndexDefinitionDto.indexDefinitionName()));
        log.info("Retrieved all deployed indexes: {}", ids);
        this.deleteById(ids);
    }

    @SneakyThrows
    private ResponseEntity<Void> sendRequest(final String requestType, final DeployedIndexDefinitionDto indexDto) {
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        final HttpEntity<DeployedIndexDefinitionDto> entity = new HttpEntity<>(indexDto, headers);

        final HttpMethod httpMethod = isLegacyIndexClient ? HttpMethod.POST : HttpMethod.PUT;

        log.info("{} index definition {}: {}", requestType, indexDto.indexDefinitionName(), this.codec.writeValueAsString(indexDto));

        final String endpoint = httpMethod + " " + this.indexerUrl + AIS_INDEXER_URI;

        log.info("AIS request: {}", endpoint);
        this.restTemplate.setErrorHandler(new HttpResponseErrorHandler(endpoint));

        final ResponseEntity<Void> response = this.restTemplate.exchange(this.indexerUrl + AIS_INDEXER_URI, httpMethod, entity, Void.class);
        log.info("AIS response : {} - {}", response.getStatusCode().value(), ((HttpStatus) response.getStatusCode()).getReasonPhrase());

        return response;
    }

}
