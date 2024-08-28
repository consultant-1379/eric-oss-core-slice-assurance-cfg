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

import static com.ericsson.oss.air.util.logging.TestLoggingUtils.FACILITY_KEY;
import static com.ericsson.oss.air.util.logging.TestLoggingUtils.FACILITY_VALUE;
import static com.ericsson.oss.air.util.logging.TestLoggingUtils.SUBJECT_KEY;
import static com.ericsson.oss.air.util.logging.TestLoggingUtils.SUBJECT_VALUE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.ericsson.oss.air.api.model.Problem;
import com.ericsson.oss.air.exception.CsacNotFoundException;
import com.ericsson.oss.air.exception.http.NotFoundException;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.messaging.handler.annotation.support.MethodArgumentTypeMismatchException;
import org.springframework.web.context.request.ServletWebRequest;

@ExtendWith(MockitoExtension.class)
class RestResponseEntityExceptionHandlerTest {

    private static final String TEST_DETAIL_MSG = "test exception";

    private static final String TEST_TYPE = "about:blank";

    private static final String TEST_INSTANCE = "/dummy/path";

    final ServletWebRequest mockServletRequest = mock(ServletWebRequest.class, RETURNS_DEEP_STUBS);

    private Logger log;

    private ListAppender<ILoggingEvent> listAppender;

    @BeforeEach
    void setUp() {

        when(mockServletRequest.getRequest().getRequestURI()).thenReturn(TEST_INSTANCE);

        this.log = (Logger) LoggerFactory.getLogger(RestResponseEntityExceptionHandler.class);
        this.log.setLevel(Level.INFO);

        this.listAppender = new ListAppender<>();
        this.listAppender.start();

        this.log.addAppender(this.listAppender);
    }

    @AfterEach
    void tearDown() {
        this.listAppender.stop();
    }

    private final RestResponseEntityExceptionHandler handler = new RestResponseEntityExceptionHandler();

    @Test
    void handleBadRequest_constraintViolationException() {

        final ResponseEntity<Problem> response = this.handler.handleBadRequest(
                new ConstraintViolationException(TEST_DETAIL_MSG, Collections.emptySet()), mockServletRequest);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        final Problem expected = new Problem()
                .detail(TEST_DETAIL_MSG)
                .instance(TEST_INSTANCE)
                .title(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .status(HttpStatus.BAD_REQUEST.value())
                .type(TEST_TYPE);

        assertEquals(expected, response.getBody());

    }

    @Test
    void handleBadRequest_methodArgumentTypeMismatchException() {

        final MethodArgumentTypeMismatchException ex = mock(MethodArgumentTypeMismatchException.class);

        when(ex.getLocalizedMessage()).thenReturn(TEST_DETAIL_MSG);

        final ResponseEntity<Problem> response = this.handler.handleBadRequest(
                ex, mockServletRequest);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        final Problem expected = new Problem()
                .detail(TEST_DETAIL_MSG)
                .instance(TEST_INSTANCE)
                .title(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .status(HttpStatus.BAD_REQUEST.value())
                .type(TEST_TYPE);

        assertEquals(expected, response.getBody());

    }

    @Test
    void handleUnsupportedOperation() {

        final ResponseEntity<Problem> response = this.handler.handleUnsupportedOperation(
                new UnsupportedOperationException(TEST_DETAIL_MSG), mockServletRequest);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());

        final Problem expected = new Problem()
                .detail(TEST_DETAIL_MSG)
                .instance(TEST_INSTANCE)
                .title(HttpStatus.CONFLICT.getReasonPhrase())
                .status(HttpStatus.CONFLICT.value())
                .type(TEST_TYPE);

        assertEquals(expected, response.getBody());
    }

    @Test
    void handleAll_NotDBConnectionException() {

        final ResponseEntity<Problem> response = this.handler.handleAll(
                new RuntimeException(TEST_DETAIL_MSG), mockServletRequest);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());

        final Problem expected = new Problem()
                .detail(TEST_DETAIL_MSG)
                .instance(TEST_INSTANCE)
                .title(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .type(TEST_TYPE);

        assertEquals(expected, response.getBody());
        assertTrue(this.listAppender.list.isEmpty());
    }

    @Test
    void handleAll_DBConnectionException() {

        final ResponseEntity<Problem> response = this.handler.handleAll(
                new CannotGetJdbcConnectionException(TEST_DETAIL_MSG), mockServletRequest);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());

        final Problem expected = new Problem()
                .detail(TEST_DETAIL_MSG)
                .instance(TEST_INSTANCE)
                .title(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .type(TEST_TYPE);

        assertEquals(expected, response.getBody());

        final List<ILoggingEvent> loggingEventList = this.listAppender.list;

        assertFalse(loggingEventList.isEmpty());
        assertEquals(1, loggingEventList.size());

        final ILoggingEvent auditEvent = this.listAppender.list.get(0);

        assertEquals(Level.ERROR, auditEvent.getLevel());
        assertEquals("Cannot connect to database: ", auditEvent.getFormattedMessage());
        assertNotNull(auditEvent.getThrowableProxy());

        final Map<String, String> mdcPropMap = auditEvent.getMDCPropertyMap();

        assertFalse(mdcPropMap.isEmpty());
        assertEquals(2, mdcPropMap.size());
        assertEquals(FACILITY_VALUE, mdcPropMap.get(FACILITY_KEY));
        assertEquals(SUBJECT_VALUE, mdcPropMap.get(SUBJECT_KEY));
    }

    @Test
    void handleHttpProblemException() {

        final ResponseEntity<Problem> response = this.handler.handleHttpProblemException(
                NotFoundException.builder().description(TEST_DETAIL_MSG).build(), mockServletRequest);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

        final Problem expected = new Problem()
                .detail(TEST_DETAIL_MSG)
                .instance(TEST_INSTANCE)
                .title(HttpStatus.NOT_FOUND.getReasonPhrase())
                .status(HttpStatus.NOT_FOUND.value())
                .type(TEST_TYPE);

        assertEquals(expected, response.getBody());
    }

    @Test
    void handleNotFound() {
        final ResponseEntity<Problem> response = this.handler.handleNotFound(
                new CsacNotFoundException(TEST_DETAIL_MSG), mockServletRequest);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

        final Problem expected = new Problem()
                .detail(TEST_DETAIL_MSG)
                .instance(TEST_INSTANCE)
                .title(HttpStatus.NOT_FOUND.getReasonPhrase())
                .status(HttpStatus.NOT_FOUND.value())
                .type(TEST_TYPE);

        assertEquals(expected, response.getBody());
    }
}