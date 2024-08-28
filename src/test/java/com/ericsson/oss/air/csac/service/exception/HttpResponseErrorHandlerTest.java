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

import static com.ericsson.oss.air.util.logging.TestLoggingUtils.FACILITY_KEY;
import static com.ericsson.oss.air.util.logging.TestLoggingUtils.FACILITY_VALUE;
import static com.ericsson.oss.air.util.logging.TestLoggingUtils.SUBJECT_KEY;
import static com.ericsson.oss.air.util.logging.TestLoggingUtils.SUBJECT_VALUE;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.ericsson.oss.air.exception.http.BadRequestException;
import com.ericsson.oss.air.exception.http.ConflictException;
import com.ericsson.oss.air.exception.http.InternalServerErrorException;
import com.ericsson.oss.air.exception.http.NotFoundException;
import com.ericsson.oss.air.exception.http.ServiceUnavailableException;
import com.ericsson.oss.air.exception.http.TooManyRequestsException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.OngoingStubbing;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpResponse;

@ExtendWith(MockitoExtension.class)
class HttpResponseErrorHandlerTest {

    private final static String ENDPOINT = "GET http://localhost:8080/catalog/v1/data-type?dataSpace=5G&dataCategory=PM_COUNTERS&schemaName=AMF_Mobility_NetworkSlice_1";

    @Mock
    private ClientHttpResponse response;

    private HttpResponseErrorHandler errorHandler;

    private Logger log;

    private ListAppender<ILoggingEvent> listAppender;

    @BeforeEach
    public void setUp() {

        this.log = (Logger) LoggerFactory.getLogger(HttpResponseErrorHandler.class);
        this.log.setLevel(Level.INFO);

        this.listAppender = new ListAppender<>();
        this.listAppender.start();

        this.log.addAppender(this.listAppender);

        this.errorHandler = new HttpResponseErrorHandler(ENDPOINT);
    }

    @AfterEach
    public void tearDown() throws Exception {
        this.listAppender.stop();
    }

    @Test
    void hasError() throws IOException {
        when(response.getStatusCode()).thenReturn(HttpStatus.TOO_MANY_REQUESTS);
        assertTrue(errorHandler.hasError(response));

        when(response.getStatusCode()).thenReturn(HttpStatus.SERVICE_UNAVAILABLE);
        assertTrue(errorHandler.hasError(response));
    }

    @Test
    void handleError_notFound_success() throws IOException {
        when(response.getStatusCode()).thenReturn(HttpStatus.NOT_FOUND);
        assertThrows(NotFoundException.class,
                () -> errorHandler.handleError(response));
    }

    @Test
    void handleError_badRequest_success() throws IOException {
        when(response.getStatusCode()).thenReturn(HttpStatus.BAD_REQUEST);
        assertThrows(BadRequestException.class,
                () -> errorHandler.handleError(response));
    }

    @Test
    void handleError_conflict_success() throws IOException {
        when(response.getStatusCode()).thenReturn(HttpStatus.CONFLICT);
        assertThrows(ConflictException.class,
                () -> errorHandler.handleError(response));
    }

    @Test
    void handleError_tooManyRequest_success() throws IOException {
        when(response.getStatusCode()).thenReturn(HttpStatus.TOO_MANY_REQUESTS);
        assertThrows(TooManyRequestsException.class,
                () -> errorHandler.handleError(response));
    }

    @Test
    void handleError_internalServerError_success() throws IOException {
        when(response.getStatusCode()).thenReturn(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThrows(InternalServerErrorException.class,
                () -> errorHandler.handleError(response));
    }

    @Test
    void handleError_serviceUnavailable_success() throws IOException {
        when(response.getStatusCode()).thenReturn(HttpStatus.SERVICE_UNAVAILABLE);
        assertThrows(ServiceUnavailableException.class,
                () -> errorHandler.handleError(response));
    }

    @Test
    void handleError_noContent_success() throws IOException {
        when(response.getStatusCode()).thenReturn(HttpStatus.NO_CONTENT);
        assertDoesNotThrow(() -> errorHandler.handleError(response));
    }

    @Test
    void handleError_securityConnectivityIssue_AuditLog() throws IOException {

        // ESOA-11082
        // Responses with status codes 400-405, 408, 409, 413-415, 429 and 500-505 must emit an audit log
        final List<HttpStatus> statusCodes = Stream.of(400, 401, 402, 403, 404, 405, 408, 409, 413, 414, 415, 429, 500, 501, 502, 503, 504, 505)
                .map(HttpStatus::resolve).toList();

        OngoingStubbing<HttpStatusCode> stubbing = when(this.response.getStatusCode());

        for (final HttpStatus httpStatus : statusCodes) {
            stubbing = stubbing.thenReturn(httpStatus);
        }

        statusCodes.forEach(httpStatus -> {
            try {
                this.errorHandler.handleError(this.response);
            } catch (final Exception e) {
                // no-op
            }
        });

        final List<ILoggingEvent> loggingEventList = this.listAppender.list;

        assertFalse(loggingEventList.isEmpty());
        assertEquals(statusCodes.size(), loggingEventList.size());

        for (int i = 0; i < loggingEventList.size(); i++) {

            final ILoggingEvent loggingEvent = loggingEventList.get(i);

            final String expectedMessage = ENDPOINT + ": " + statusCodes.get(i).value() + " " + statusCodes.get(i).getReasonPhrase();

            assertTrue(loggingEvent.getFormattedMessage().endsWith(expectedMessage));
            assertEquals(Level.ERROR, loggingEvent.getLevel());
            assertFalse(loggingEvent.getMDCPropertyMap().isEmpty());
            assertEquals(2, loggingEvent.getMDCPropertyMap().size());
            assertEquals(FACILITY_VALUE, loggingEvent.getMDCPropertyMap().get(FACILITY_KEY));
            assertEquals(SUBJECT_VALUE, loggingEvent.getMDCPropertyMap().get(SUBJECT_KEY));
        }
    }

    @Test
    void handleError_nonSecureConnectivityIssue_NoAuditLog() throws IOException {

        when(this.response.getStatusCode()).thenReturn(HttpStatus.I_AM_A_TEAPOT);
        this.errorHandler.handleError(this.response);

        final List<ILoggingEvent> loggingEventList = this.listAppender.list;

        assertFalse(loggingEventList.isEmpty());
        assertEquals(1, loggingEventList.size());

        final ILoggingEvent nonAuditEvent = this.listAppender.list.get(0);

        assertEquals(Level.ERROR, nonAuditEvent.getLevel());
        assertTrue(nonAuditEvent.getMDCPropertyMap().isEmpty());

    }
}