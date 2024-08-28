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

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ericsson.oss.air.csac.model.datacatalog.DataTypeResponseDto;
import com.ericsson.oss.air.csac.model.datacatalog.MessageSchemaDTO;
import com.ericsson.oss.air.csac.service.exception.HttpResponseErrorHandler;
import com.ericsson.oss.air.exception.CsacValidationException;
import com.ericsson.oss.air.exception.http.InternalServerErrorException;
import com.ericsson.oss.air.exception.http.ServiceUnavailableException;
import com.ericsson.oss.air.exception.http.TooManyRequestsException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
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
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Interacts with Data Catalog rest client
 */
@Service
@Slf4j
@ConditionalOnProperty(prefix = "validation.external.restClient.datacatalog",
                       name = "url")
@Profile({ "prod", "test" })
public class DataCatalogRestClient {

    private static final Pattern SCHEMA_REF_PATTERN = Pattern.compile("(.+)\\|(.+)\\|(.+)");

    static final String MESSAGE_SCHEMA_URI = "/catalog/v1/data-type";

    @Autowired
    private RestTemplate restTemplate;

    @Value("${validation.external.restClient.datacatalog.url}")
    private String dataCatalogUrl;

    /**
     * Retrieves message schema from the data catalog for the given schema reference.
     *
     * @param schemaReferenceStr message schema reference
     * @return {@link MessageSchemaDTO}
     */
    // Use resilience4j configuration as workaround for the ticket ESOA-4203
    @Retryable(maxAttemptsExpression = "#{${resilience4j.retry.instances.dataCatalog.max-attempts}}",
               backoff = @Backoff(delayExpression = "#{${resilience4j.retry.instances.dataCatalog.wait-duration}}",
                                  multiplierExpression = "#{${resilience4j.retry.instances.dataCatalog.exponential-backoff-multiplier}}",
                                  maxDelayExpression = "#{${resilience4j.retry.instances.dataCatalog.maxDelay}}"),
               retryFor = { InternalServerErrorException.class, TooManyRequestsException.class, ServiceUnavailableException.class,
                       ResourceAccessException.class })
    public MessageSchemaDTO getMessageSchema(final String schemaReferenceStr) {

        final Matcher matcher = SCHEMA_REF_PATTERN.matcher(schemaReferenceStr);
        if (!matcher.matches()) {
            final String errorMessage = "Invalid schema reference format. Expected format: '<dataSpace>|<dataCategory>|<schemaName>'. "
                    + "Actual value: '" + schemaReferenceStr + "'.";
            log.error(errorMessage);
            throw new CsacValidationException(errorMessage);
        }

        final String dataSpace = matcher.group(1);
        final String dataCategory = matcher.group(2);
        final String schemaName = matcher.group(3);

        log.info(String.format("Retrieving metadata for schema reference: %s|%s|%s", dataSpace, dataCategory, schemaName));

        final String endpointUrl = UriComponentsBuilder
                .fromUriString(dataCatalogUrl + MESSAGE_SCHEMA_URI)
                .queryParam("dataSpace", dataSpace)
                .queryParam("dataCategory", dataCategory)
                .queryParam("schemaName", schemaName)
                .toUriString();

        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        final HttpEntity<String> entity = new HttpEntity<>(headers);

        final String endpoint = "GET " + endpointUrl;
        restTemplate.setErrorHandler(new HttpResponseErrorHandler(endpoint));

        log.info("Data Catalog request: {}", endpoint);

        final ResponseEntity<List<DataTypeResponseDto>> response = this.restTemplate.exchange(endpointUrl, HttpMethod.GET, entity,
                new ParameterizedTypeReference<>() {
                });

        log.info("Data Catalog response: {} - {}", response.getStatusCode().value(), ((HttpStatus) response.getStatusCode()).getReasonPhrase());

        final List<DataTypeResponseDto> dataTypeResponseDtoList = response.getBody();
        if (CollectionUtils.isEmpty(dataTypeResponseDtoList)) {
            return null;
        }
        return dataTypeResponseDtoList.get(0).getMessageSchema();
    }

}
