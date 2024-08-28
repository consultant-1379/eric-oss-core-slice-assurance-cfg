/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.handler;

import static com.ericsson.oss.air.exception.http.HttpProblemException.DEFAULT_TYPE;

import com.ericsson.oss.air.api.model.Problem;
import com.ericsson.oss.air.exception.CsacConflictStateException;
import com.ericsson.oss.air.exception.CsacInternalErrorException;
import com.ericsson.oss.air.exception.CsacNotFoundException;
import com.ericsson.oss.air.exception.http.HttpProblemException;
import com.ericsson.oss.air.util.logging.audit.AuditLogFactory;
import com.ericsson.oss.air.util.logging.audit.AuditLogger;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Exception handler to support global error handling for rest response and provide mapping of several exceptions to the same method, to be handled
 * together.
 */
@ControllerAdvice
public class RestResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {

    private static final AuditLogger AUDIT_LOGGER = AuditLogFactory.getLogger(RestResponseEntityExceptionHandler.class);

    /**
     * handle ConstraintViolationException and MethodArgumentTypeMismatchException exceptions
     *
     * @param ex      Runtime exception
     * @param request web request
     * @return ResponseEntity with response details
     */
    @ExceptionHandler({ ConstraintViolationException.class, MethodArgumentTypeMismatchException.class })
    protected ResponseEntity<Problem> handleBadRequest(final RuntimeException ex, final WebRequest request) {

        final Problem problem = new Problem()
                .title(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .status(HttpStatus.BAD_REQUEST.value())
                .detail(ex.getLocalizedMessage())
                .instance(((ServletWebRequest) request).getRequest().getRequestURI())
                .type(DEFAULT_TYPE);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_JSON)
                .body(problem);
    }

    @ExceptionHandler({ UnsupportedOperationException.class, CsacConflictStateException.class })
    protected ResponseEntity<Problem> handleUnsupportedOperation(final RuntimeException ex, final WebRequest request) {

        final Problem problem = new Problem()
                .title(HttpStatus.CONFLICT.getReasonPhrase())
                .status(HttpStatus.CONFLICT.value())
                .detail(ex.getLocalizedMessage())
                .instance(((ServletWebRequest) request).getRequest().getRequestURI())
                .type(DEFAULT_TYPE);

        return ResponseEntity.status(HttpStatus.CONFLICT)
                .contentType(MediaType.APPLICATION_JSON)
                .body(problem);
    }

    /**
     * Handles responses for {@link CsacNotFoundException}'s
     *
     * @param ex      the exception that is thrown
     * @param request web request
     * @return the response entity with the {@link Problem} object in the body
     */
    @ExceptionHandler(CsacNotFoundException.class)
    protected ResponseEntity<Problem> handleNotFound(final RuntimeException ex, final WebRequest request) {

        final Problem problem = new Problem()
                .title(HttpStatus.NOT_FOUND.getReasonPhrase())
                .status(HttpStatus.NOT_FOUND.value())
                .detail(ex.getLocalizedMessage())
                .instance(((ServletWebRequest) request).getRequest().getRequestURI())
                .type(DEFAULT_TYPE);

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .contentType(MediaType.APPLICATION_JSON)
                .body(problem);
    }

    /**
     * Handles responses for {@link HttpProblemException}'s.
     *
     * @param httpProblemException the httpProblemException that is thrown
     * @return the response entity with the problem object in the body
     */
    @ExceptionHandler(HttpProblemException.class)
    protected ResponseEntity<Problem> handleHttpProblemException(final HttpProblemException httpProblemException, final WebRequest request) {

        final Problem problem = httpProblemException.getProblem().instance(((ServletWebRequest) request).getRequest().getRequestURI());

        return ResponseEntity.status(httpProblemException.getHttpStatus().value())
                .contentType(MediaType.APPLICATION_JSON)
                .body(problem);
    }

    @ExceptionHandler({ CsacInternalErrorException.class, Exception.class })
    protected ResponseEntity<Problem> handleAll(final Throwable ex, final WebRequest request) {

        AUDIT_LOGGER.error(ex);

        final Problem problem = new Problem()
                .title(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .detail(ex.getLocalizedMessage())
                .instance(((ServletWebRequest) request).getRequest().getRequestURI())
                .type(DEFAULT_TYPE);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .contentType(MediaType.APPLICATION_JSON)
                .body(problem);
    }

}