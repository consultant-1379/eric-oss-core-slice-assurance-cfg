/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.service.exception;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import com.ericsson.oss.air.csac.model.pmsc.PmscErrorResponse;
import com.ericsson.oss.air.util.codec.Codec;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.validation.Validation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.ObjectUtils;

@Slf4j
public class PmscHttpResponseErrorHandler extends HttpResponseErrorHandler {

    private final Codec codec = new Codec(Validation.buildDefaultValidatorFactory().getValidator());

    /**
     * Constructs a {@code PmscHttpResponseErrorHandler} for the provided end point.
     *
     * @param endpoint the endpoint of the REST request
     */
    public PmscHttpResponseErrorHandler(final String endpoint) {
        super(endpoint);
    }

    @Override
    public void handleError(final ClientHttpResponse response) throws IOException {
        final String body = new String(response.getBody().readAllBytes(), StandardCharsets.UTF_8);

        if (!ObjectUtils.isEmpty(body)) {
            log.error("PMSC request failed: {} - {}", response.getStatusCode().value(), ((HttpStatus) response.getStatusCode()).getReasonPhrase());

            try {
                final PmscErrorResponse pmscErrorResponse = this.codec.readValue(body, PmscErrorResponse.class);
                log.error("PMSC response failure error: {}", pmscErrorResponse.getError());
                log.error("PMSC response failure status code: {}", pmscErrorResponse.getStatus());
                log.error("PMSC response failure message: {}", pmscErrorResponse.getMessage());
            } catch (final JsonProcessingException e) {
                log.debug("Error parsing PMSC error response", e);

                // since we cannot parse the error as JSON, we have to log it as it arrived
                log.error("Failure message body: {}", body);
            }

        }

        super.handleError(response);
    }

}
