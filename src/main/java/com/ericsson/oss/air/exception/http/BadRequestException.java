/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.exception.http;

import lombok.Builder;
import org.springframework.http.HttpStatus;

/**
 * An Exception Extension of the HttpProblemException class for 400 Bad Request.
 */
public class BadRequestException extends HttpProblemException {

    /**
     * Constructs a {@code BadRequestException}.
     *
     * @param description the detailed message that describes the problem. It will be used as the exception's message,
     *                    e.g. the message returned by invoking this exception's {@link #getMessage()} method.
     * @param instance    the instance that caused the problem
     * @param type        the type of the problem
     */
    @Builder
    public BadRequestException(final String description, final String instance, final String type) {
        super(HttpStatus.BAD_REQUEST, description, instance, type);
    }
}
