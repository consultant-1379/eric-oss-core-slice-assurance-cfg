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
import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.ericsson.oss.air.exception.http.ConflictException;
import com.ericsson.oss.air.exception.http.TooManyRequestsException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;

@ExtendWith(MockitoExtension.class)
class PmscHttpResponseErrorHandlerTest {

    private static final String PMSC_ERROR_BODY_STRING = "{\"timeStamp\":\"2023-07-25T14:59:21.734\",\"status\":409,\"error\":\"Conflict\","
            + "\"message\":\"KPI name must be unique but 'rolling_bulk_sum_test_integer_2' is already defined in the database\"}";

    private static final String BAD_PMSC_ERRORS_BODY = "{\"placeholder\"}";

    private final PmscHttpResponseErrorHandler errorHandler = new PmscHttpResponseErrorHandler("GET PMSC_URL");

    @Mock
    private ClientHttpResponse response;

    private Logger log;

    private ListAppender<ILoggingEvent> listAppender;

    @BeforeEach
    public void setUp() {

        this.log = (Logger) LoggerFactory.getLogger(HttpResponseErrorHandler.class);
        this.log.setLevel(Level.INFO);

        this.listAppender = new ListAppender<>();
        this.listAppender.start();

        this.log.addAppender(this.listAppender);
    }

    @AfterEach
    public void tearDown() throws Exception {
        this.listAppender.stop();
    }

    @Test
    void handleError_withBody_success() throws IOException {
        when(response.getStatusCode()).thenReturn(HttpStatus.CONFLICT);
        when(response.getBody()).thenReturn(new ByteArrayInputStream(PMSC_ERROR_BODY_STRING.getBytes()));

        assertThrows(ConflictException.class,
                () -> errorHandler.handleError(response));

        final List<ILoggingEvent> loggingEventList = this.listAppender.list;

        assertFalse(loggingEventList.isEmpty());
        assertEquals(1, loggingEventList.size());

        final ILoggingEvent loggingEvent = loggingEventList.get(0);

        assertEquals(Level.ERROR, loggingEvent.getLevel());
        assertFalse(loggingEvent.getMDCPropertyMap().isEmpty());
        assertEquals(2, loggingEvent.getMDCPropertyMap().size());
        assertEquals(FACILITY_VALUE, loggingEvent.getMDCPropertyMap().get(FACILITY_KEY));
        assertEquals(SUBJECT_VALUE, loggingEvent.getMDCPropertyMap().get(SUBJECT_KEY));
    }

    @Test
    void handleError_withBadFormatBody_exception() throws IOException {
        when(response.getStatusCode()).thenReturn(HttpStatus.TOO_MANY_REQUESTS);
        when(response.getBody()).thenReturn(new ByteArrayInputStream(BAD_PMSC_ERRORS_BODY.getBytes()));

        assertThrows(TooManyRequestsException.class,
                () -> errorHandler.handleError(response));

        final List<ILoggingEvent> loggingEventList = this.listAppender.list;

        assertFalse(loggingEventList.isEmpty());
        assertEquals(1, loggingEventList.size());

        final ILoggingEvent loggingEvent = loggingEventList.get(0);

        assertEquals(Level.ERROR, loggingEvent.getLevel());
        assertFalse(loggingEvent.getMDCPropertyMap().isEmpty());
        assertEquals(2, loggingEvent.getMDCPropertyMap().size());
        assertEquals(FACILITY_VALUE, loggingEvent.getMDCPropertyMap().get(FACILITY_KEY));
        assertEquals(SUBJECT_VALUE, loggingEvent.getMDCPropertyMap().get(SUBJECT_KEY));
    }

    @Test
    void handleError_withEmptyBody_noException() throws IOException {
        when(response.getStatusCode()).thenReturn(HttpStatus.I_AM_A_TEAPOT);
        when(response.getBody()).thenReturn(new ByteArrayInputStream("".getBytes()));

        errorHandler.handleError(response);

        final List<ILoggingEvent> loggingEventList = this.listAppender.list;

        assertFalse(loggingEventList.isEmpty());
        assertEquals(1, loggingEventList.size());

        final ILoggingEvent loggingEvent = loggingEventList.get(0);

        assertEquals(Level.ERROR, loggingEvent.getLevel());
        assertTrue(loggingEvent.getMDCPropertyMap().isEmpty());
    }

}
