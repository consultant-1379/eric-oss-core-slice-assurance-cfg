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

import com.ericsson.oss.air.api.model.Problem;
import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * A {@link RuntimeException} that represents an HTTP exception.
 */
@Getter
public class HttpProblemException extends RuntimeException {

    public static final String DEFAULT_TYPE = "about:blank";

    private final String type;

    private final HttpStatus httpStatus;

    private final String description;

    private final String instance;

    /**
     * Constructs a {@code HttpProblemException}.
     *
     * @param httpStatus  the http status of the problem
     * @param description the detailed message that describes the problem. It will be used as the exception's message,
     *                    e.g. the message returned by invoking this exception's {@link #getMessage()} method.
     * @param instance    the instance that caused the problem
     * @param type        the type of the problem
     */
    public HttpProblemException(final HttpStatus httpStatus, final String description, final String instance, final String type) {

        super(description);

        this.httpStatus = httpStatus;
        this.description = description;
        this.instance = instance;
        this.type = type != null ? type : DEFAULT_TYPE;
    }

    /**
     * Creates a Problem instance populated with the content of this {@code HttpProblemException}.
     */
    public Problem getProblem() {
        return new Problem()
                .type(this.type)
                .title(this.httpStatus.getReasonPhrase())
                .status(this.httpStatus.value())
                .detail(this.description)
                .instance(this.instance);
    }
}
