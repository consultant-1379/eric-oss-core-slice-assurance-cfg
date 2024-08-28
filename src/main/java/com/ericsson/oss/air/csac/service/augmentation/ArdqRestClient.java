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

import com.ericsson.oss.air.csac.service.exception.HttpResponseErrorHandler;
import com.ericsson.oss.air.exception.CsacValidationException;
import com.ericsson.oss.air.exception.http.InternalServerErrorException;
import com.ericsson.oss.air.exception.http.ServiceUnavailableException;
import com.ericsson.oss.air.exception.http.TooManyRequestsException;
import com.ericsson.oss.air.util.logging.FaultHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

/**
 * Rest client for ARDQ services.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ArdqRestClient {

    public static final String ARDQ_URI = "/v1/augmentation-info/augmentation/query/types";

    private final RestTemplate restTemplate;

    private final FaultHandler faultHandler;

    /**
     * Queries ARDQ service to get it's supported dimensioning query types.
     *
     * @param ardqUrl ARDQ service url
     * @return List of supported dimensioning query types.
     */
    // Use resilience4j configuration as workaround for the ticket ESOA-4203
    @Retryable(maxAttemptsExpression = "#{${resilience4j.retry.instances.ardq.max-attempts}}",
               backoff = @Backoff(delayExpression = "#{${resilience4j.retry.instances.ardq.wait-duration}}",
                                  multiplierExpression = "#{${resilience4j.retry.instances.ardq.exponential-backoff-multiplier}}",
                                  maxDelayExpression = "#{${resilience4j.retry.instances.ardq.maxDelay}}"),
               retryFor = { InternalServerErrorException.class, TooManyRequestsException.class, ServiceUnavailableException.class,
                       ResourceAccessException.class })
    public List<String> getArdqQueryTypes(final String ardqUrl) {
        log.debug("Retrieving dimensioning query types from {}", ardqUrl);

        final String endpoint = "GET " + ardqUrl + ARDQ_URI;

        this.restTemplate.setErrorHandler(new HttpResponseErrorHandler(endpoint));

        log.info("ARDQ request: {}", endpoint);

        final ResponseEntity<String> response = this.restTemplate.getForEntity(ardqUrl + ARDQ_URI, String.class);

        log.info("ARDQ response: {} - {}", response.getStatusCode().value(), ((HttpStatus) response.getStatusCode()).getReasonPhrase());

        try {

            final ObjectMapper mapper = new ObjectMapper();
            final JsonNode jNode = mapper.readTree(response.getBody());
            return jNode.findValuesAsText("queryType");

        } catch (final JsonProcessingException jspe) {

            this.faultHandler.fatal(jspe);
            throw new CsacValidationException(jspe);
        }
    }
}
