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

import static org.springframework.http.HttpStatus.resolve;

import java.io.IOException;
import java.util.Objects;

import com.ericsson.oss.air.exception.http.BadRequestException;
import com.ericsson.oss.air.exception.http.ConflictException;
import com.ericsson.oss.air.exception.http.InternalServerErrorException;
import com.ericsson.oss.air.exception.http.NotFoundException;
import com.ericsson.oss.air.exception.http.ServiceUnavailableException;
import com.ericsson.oss.air.exception.http.TooManyRequestsException;
import com.ericsson.oss.air.util.logging.audit.AuditLogFactory;
import com.ericsson.oss.air.util.logging.audit.AuditLogger;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.ResponseErrorHandler;

/**
 * Implementation of ResponseErrorHandler which will be injected into RestTemplate through RestTemplateBuilder.
 * This implementation handles the HTTP errors returned by remote APIs
 */

@Slf4j
@RequiredArgsConstructor
@Getter
public class HttpResponseErrorHandler implements ResponseErrorHandler {

    private static final AuditLogger AUDIT_LOGGER = AuditLogFactory.getLogger(HttpResponseErrorHandler.class);

    private static final String FATAL_ERROR = "Fatal error. {}";

    private static final String UNMATCHED_RESPONSE = "Unmatched error response. {}";

    private final String endpoint;

    /**
     * Delegates to HttpStatusCode with the response status code.
     *
     * @param response client http response
     */
    @Override
    public boolean hasError(final ClientHttpResponse response) throws IOException {
        return (response.getStatusCode().is4xxClientError() || response.getStatusCode().is5xxServerError());
    }

    /*
     * Handles the error in the given response with the given resolved status code.
     * @param response client http response
     */
    @Override
    public void handleError(final ClientHttpResponse response) throws IOException {
        final HttpStatus status = resolve(response.getStatusCode().value());
        Objects.requireNonNull(status);

        final String message = this.endpoint + ": " + status.value() + " " + status.getReasonPhrase();

        switch (status) {
            case NOT_FOUND -> {
                AUDIT_LOGGER.error(FATAL_ERROR, message);
                throw NotFoundException.builder().description(message).build();
            }
            case BAD_REQUEST -> {
                AUDIT_LOGGER.error(FATAL_ERROR, message);
                throw BadRequestException.builder().description(message).build();
            }
            case CONFLICT -> {
                AUDIT_LOGGER.error(FATAL_ERROR, message);
                throw ConflictException.builder().description(message).build();
            }
            case TOO_MANY_REQUESTS -> {
                AUDIT_LOGGER.error(message);
                throw TooManyRequestsException.builder().description(message).build();
            }
            case INTERNAL_SERVER_ERROR -> {
                AUDIT_LOGGER.error(message);
                throw InternalServerErrorException.builder().description(message).build();
            }
            case SERVICE_UNAVAILABLE -> {
                AUDIT_LOGGER.error(message);
                throw ServiceUnavailableException.builder().description(message).build();
            }
            case UNAUTHORIZED, PAYMENT_REQUIRED, FORBIDDEN, METHOD_NOT_ALLOWED, REQUEST_TIMEOUT, PAYLOAD_TOO_LARGE, URI_TOO_LONG, UNSUPPORTED_MEDIA_TYPE, NOT_IMPLEMENTED, BAD_GATEWAY, GATEWAY_TIMEOUT, HTTP_VERSION_NOT_SUPPORTED -> {
                AUDIT_LOGGER.error(UNMATCHED_RESPONSE, message);
            }
            default -> log.error(UNMATCHED_RESPONSE, message);
        }
    }

}
