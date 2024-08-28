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

import java.net.URI;

import com.ericsson.oss.air.csac.model.pmschema.PMSchemaDTO;
import com.ericsson.oss.air.csac.service.exception.HttpResponseErrorHandler;
import com.ericsson.oss.air.exception.http.InternalServerErrorException;
import com.ericsson.oss.air.exception.http.ServiceUnavailableException;
import com.ericsson.oss.air.exception.http.TooManyRequestsException;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Lazy;
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
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Schema registry rest client to retrieve schema
 * <p> This lazy rest client bean will be initialized when being used<p/>
 */
@Service
@NoArgsConstructor
@Slf4j
@ConditionalOnProperty(prefix = "validation.external.restClient.schemaregistry",
                       name = "url")
@Lazy
public class SchemaRegRestClient {

    /* Schema Registry server path for getting schema by Subject and version */
    private static final String SCHEMA_REG_URL_BY_SUBJECT_VERSION_PATH = "subjects/{subject}/versions/{version}/schema";

    /* Parameter that used to retrieve the latest version of a schema */
    private static final String LATEST = "latest";

    @Autowired
    private RestTemplate restTemplate;

    @Value("${validation.external.restClient.schemaregistry.url}")
    private String schemaRegUrl;

    /**
     * SchemaRegRestClient constructor
     *
     * @param restTemplate {@link RestTemplate}
     * @param schemaRegUrl schema registry client url
     */
    SchemaRegRestClient(final RestTemplate restTemplate, final String schemaRegUrl) {
        this.restTemplate = restTemplate;
        this.schemaRegUrl = schemaRegUrl;
    }

    /**
     * Get schema by given uri.
     *
     * @param subject the subject that saves schema information
     * @return a {@link PMSchemaDTO}
     */
    // Use resilience4j configuration as workaround for the ticket ESOA-4203
    @Retryable(maxAttemptsExpression = "#{${resilience4j.retry.instances.schemaRegistry.max-attempts}}",
               backoff = @Backoff(delayExpression = "#{${resilience4j.retry.instances.schemaRegistry.wait-duration}}",
                                  multiplierExpression = "#{${resilience4j.retry.instances.schemaRegistry.exponential-backoff-multiplier}}",
                                  maxDelayExpression = "#{${resilience4j.retry.instances.schemaRegistry.maxDelay}}"),
               retryFor = { InternalServerErrorException.class, TooManyRequestsException.class, ServiceUnavailableException.class,
                       ResourceAccessException.class })
    public PMSchemaDTO getSchema(final String subject) throws RestClientException {
        final URI uri = getSchemaRegUriBySubjectVersion(subject);
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        final HttpEntity<URI> requestEntity = new HttpEntity<>(uri, headers);

        final String endpoint = "GET " + uri.toString();

        this.restTemplate.setErrorHandler(new HttpResponseErrorHandler(endpoint));

        log.info("Schema Registry request: {}", endpoint);
        final ResponseEntity<PMSchemaDTO> response = this.restTemplate.exchange(uri, HttpMethod.GET, requestEntity, PMSchemaDTO.class);

        log.info("Schema Registry response: {} - {}", response.getStatusCode().value(), ((HttpStatus) response.getStatusCode()).getReasonPhrase());

        return response.getBody();

    }

    /**
     * Returns a {@link URI} to use for retrieving a schema by its subject and version.
     * <p>
     * Note: a URI needs to be returned because the subject may contain special URL characters. The subject is a path parameter. If a String is
     * returned with encoded parameters, then RestTemplate calls will double encode it.
     *
     * @param subject the registered name of the schema in the Schema Registry
     * @return a {@link URI}
     */
    protected URI getSchemaRegUriBySubjectVersion(final String subject) {
        return UriComponentsBuilder.fromUriString(this.schemaRegUrl)
                .path(SCHEMA_REG_URL_BY_SUBJECT_VERSION_PATH)
                .build(subject, SchemaRegRestClient.LATEST);
    }
}
