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

import static com.ericsson.oss.air.exception.http.HttpProblemExceptionTest.DESCRIPTION;
import static com.ericsson.oss.air.exception.http.HttpProblemExceptionTest.INSTANCE;
import static com.ericsson.oss.air.exception.http.HttpProblemExceptionTest.NON_DEFAULT_TYPE;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.ericsson.oss.air.api.model.Problem;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class ServiceUnavailableExceptionTest {

    @Test
    void builder() {

        final ServiceUnavailableException exception = ServiceUnavailableException.builder()
                .type(NON_DEFAULT_TYPE)
                .description(DESCRIPTION)
                .instance(INSTANCE)
                .build();

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, exception.getHttpStatus());
        assertEquals(DESCRIPTION, exception.getDescription());
        assertEquals(INSTANCE, exception.getInstance());
        assertEquals(NON_DEFAULT_TYPE, exception.getType());
        assertEquals(DESCRIPTION, exception.getMessage());

        final Problem expectedProblem = new Problem()
                .type(NON_DEFAULT_TYPE)
                .detail(DESCRIPTION)
                .instance(INSTANCE)
                .title(HttpStatus.SERVICE_UNAVAILABLE.getReasonPhrase())
                .status(HttpStatus.SERVICE_UNAVAILABLE.value());

        assertEquals(expectedProblem, exception.getProblem());

    }
}