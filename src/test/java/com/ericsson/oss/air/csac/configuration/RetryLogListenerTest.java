/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.configuration;

import static com.ericsson.oss.air.util.logging.TestLoggingUtils.FACILITY_KEY;
import static com.ericsson.oss.air.util.logging.TestLoggingUtils.FACILITY_VALUE;
import static com.ericsson.oss.air.util.logging.TestLoggingUtils.SUBJECT_KEY;
import static com.ericsson.oss.air.util.logging.TestLoggingUtils.SUBJECT_VALUE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.util.List;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.ericsson.oss.air.exception.http.InternalServerErrorException;
import com.ericsson.oss.air.exception.http.NotFoundException;
import com.ericsson.oss.air.exception.http.ServiceUnavailableException;
import com.ericsson.oss.air.exception.http.TooManyRequestsException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
import org.springframework.retry.RetryContext;
import org.springframework.retry.annotation.Retryable;
import org.springframework.retry.interceptor.MethodInvocationRetryCallback;
import org.springframework.web.client.ResourceAccessException;

@ExtendWith(MockitoExtension.class)
class RetryLogListenerTest {

    private static final String EXCEPTION_MESSAGE = "GET /foobar: 503 Service Unavailable";

    private static final RuntimeException EXCEPTION = new RuntimeException(EXCEPTION_MESSAGE);

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private RetryContext context;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private MethodInvocationRetryCallback callback;

    private Logger log;

    private ListAppender<ILoggingEvent> listAppender;

    private RetryLogListener listener;

    @BeforeEach
    public void setUp() {
        this.log = (Logger) LoggerFactory.getLogger(RetryLogListener.class);
        this.log.setLevel(Level.INFO);

        this.listAppender = new ListAppender<>();
        this.listAppender.start();

        this.log.addAppender(this.listAppender);

        this.listener = new RetryLogListener();

    }

    @AfterEach
    public void tearDown() throws Exception {
        this.listAppender.stop();
    }

    @Test
    void doClose_AttemptsAreExhausted() {
        when(this.context.getAttribute("context.exhausted")).thenReturn(true);
        when(this.context.getRetryCount()).thenReturn(2);

        this.listener.doClose(this.context, this.callback, EXCEPTION);

        final List<ILoggingEvent> loggingEventList = this.listAppender.list;

        assertFalse(loggingEventList.isEmpty());

        final ILoggingEvent loggingEvent = loggingEventList.get(0);

        assertEquals(Level.ERROR, loggingEvent.getLevel());
        assertEquals("Retries exhausted after 2 attempts. Cause: ", loggingEvent.getFormattedMessage());
        assertEquals(EXCEPTION_MESSAGE, loggingEvent.getThrowableProxy().getMessage());
        assertFalse(loggingEvent.getMDCPropertyMap().isEmpty());
        assertEquals(2, loggingEvent.getMDCPropertyMap().size());
        assertEquals(FACILITY_VALUE, loggingEvent.getMDCPropertyMap().get(FACILITY_KEY));
        assertEquals(SUBJECT_VALUE, loggingEvent.getMDCPropertyMap().get(SUBJECT_KEY));
    }

    @Test
    void doClose_IsNotExhausted_LastRetryAttemptPassed() {
        when(this.context.getAttribute("context.exhausted")).thenReturn(null);

        this.listener.doClose(this.context, this.callback, EXCEPTION);

        final List<ILoggingEvent> loggingEventList = this.listAppender.list;

        assertTrue(loggingEventList.isEmpty());
    }

    @Test
    void doClose_NonRecoverableException() {
        when(this.context.getAttribute("context.exhausted")).thenReturn(true);
        when(this.context.getRetryCount()).thenReturn(1);

        this.listener.doClose(this.context, this.callback, NotFoundException.builder().build());

        final List<ILoggingEvent> loggingEventList = this.listAppender.list;

        assertTrue(loggingEventList.isEmpty());
    }

    @Test
    void doOnSuccess_MethodThatWasRetried() {
        when(this.context.getRetryCount()).thenReturn(1);
        when(this.context.getLastThrowable().getMessage()).thenReturn(EXCEPTION_MESSAGE);

        this.listener.doOnSuccess(this.context, this.callback, null);

        final List<ILoggingEvent> loggingEventList = this.listAppender.list;

        assertFalse(loggingEventList.isEmpty());

        final ILoggingEvent loggingEvent = loggingEventList.get(0);

        assertEquals(Level.INFO, loggingEvent.getLevel());
        assertEquals("Attempt 2 successful for previous exception: " + EXCEPTION_MESSAGE, loggingEvent.getFormattedMessage());
        assertFalse(loggingEvent.getMDCPropertyMap().isEmpty());
        assertEquals(2, loggingEvent.getMDCPropertyMap().size());
        assertEquals(FACILITY_VALUE, loggingEvent.getMDCPropertyMap().get(FACILITY_KEY));
        assertEquals(SUBJECT_VALUE, loggingEvent.getMDCPropertyMap().get(SUBJECT_KEY));
    }

    @Test
    void doOnSuccess_MethodThatWasNotRetried() {
        when(this.context.getRetryCount()).thenReturn(0);

        this.listener.doOnSuccess(this.context, this.callback, null);

        assertTrue(this.listAppender.list.isEmpty());
    }

    @Test
    void doOnError_RetryableError_WithSpecifiedRetryFor() throws NoSuchMethodException {
        final ServiceUnavailableException exception = ServiceUnavailableException.builder()
                .description(EXCEPTION_MESSAGE)
                .build();

        final Method exampleRetryableMethod = RetryLogListenerTest.class.getDeclaredMethod("retryMethodWithRetryFor");

        when(this.callback.getInvocation().getMethod()).thenReturn(exampleRetryableMethod);
        when(this.context.getRetryCount()).thenReturn(1);

        this.listener.doOnError(this.context, this.callback, exception);

        final List<ILoggingEvent> loggingEventList = this.listAppender.list;

        assertFalse(loggingEventList.isEmpty());

        final ILoggingEvent loggingEvent = loggingEventList.get(0);

        assertEquals(Level.ERROR, loggingEvent.getLevel());
        assertEquals("Attempt 1 failed: " + EXCEPTION_MESSAGE + ". Retrying.", loggingEvent.getFormattedMessage());
        assertFalse(loggingEvent.getMDCPropertyMap().isEmpty());
        assertEquals(2, loggingEvent.getMDCPropertyMap().size());
        assertEquals(FACILITY_VALUE, loggingEvent.getMDCPropertyMap().get(FACILITY_KEY));
        assertEquals(SUBJECT_VALUE, loggingEvent.getMDCPropertyMap().get(SUBJECT_KEY));

    }

    @Test
    void doOnError_RetryableError_WithoutSpecifiedRetryFor() throws NoSuchMethodException {
        final ServiceUnavailableException exception = ServiceUnavailableException.builder()
                .description(EXCEPTION_MESSAGE)
                .build();

        final Method exampleRetryableMethod = RetryLogListenerTest.class.getDeclaredMethod("retryMethod");

        when(this.callback.getInvocation().getMethod()).thenReturn(exampleRetryableMethod);
        when(this.context.getRetryCount()).thenReturn(1);

        this.listener.doOnError(this.context, this.callback, exception);

        final List<ILoggingEvent> loggingEventList = this.listAppender.list;

        assertFalse(loggingEventList.isEmpty());

        final ILoggingEvent loggingEvent = loggingEventList.get(0);

        assertEquals(Level.ERROR, loggingEvent.getLevel());
        assertEquals("Attempt 1 failed: " + EXCEPTION_MESSAGE + ". Retrying.", loggingEvent.getFormattedMessage());
        assertFalse(loggingEvent.getMDCPropertyMap().isEmpty());
        assertEquals(2, loggingEvent.getMDCPropertyMap().size());
        assertEquals(FACILITY_VALUE, loggingEvent.getMDCPropertyMap().get(FACILITY_KEY));
        assertEquals(SUBJECT_VALUE, loggingEvent.getMDCPropertyMap().get(SUBJECT_KEY));

    }

    @Test
    void doOnError_NonRetryableError() throws NoSuchMethodException {
        final NotFoundException exception = NotFoundException.builder().build();

        final Method exampleRetryableMethod = RetryLogListenerTest.class.getDeclaredMethod("retryMethodWithRetryFor");

        when(this.callback.getInvocation().getMethod()).thenReturn(exampleRetryableMethod);

        this.listener.doOnError(this.context, this.callback, exception);

        final List<ILoggingEvent> loggingEventList = this.listAppender.list;

        assertTrue(loggingEventList.isEmpty());
    }

    @Retryable
    private void retryMethod() {
    }

    @Retryable(retryFor = { InternalServerErrorException.class, TooManyRequestsException.class, ServiceUnavailableException.class,
            ResourceAccessException.class })
    private void retryMethodWithRetryFor() {
    }
}