/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.util.logging;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.core.read.ListAppender;
import com.ericsson.oss.air.exception.CsacValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.StringUtils;
import org.slf4j.LoggerFactory;

class FaultHandlerTest {

    private static final String TEST_PREPENDED_MESSAGE = "Test: ";

    private static final String EXCEPTION_MESSAGE = "Something happened.";

    private final FaultHandler faultHandler = new FaultHandler();

    private Logger log;

    private ListAppender<ILoggingEvent> listAppender;

    @BeforeEach
    void init() {
        log = (Logger) LoggerFactory.getLogger(FaultHandler.class);
        log.setLevel(Level.INFO);

        listAppender = new ListAppender<>();
        listAppender.start();

        log.addAppender(listAppender);
    }

    @Test
    void fatal_LogIsOutput() {
        this.faultHandler.fatal(new CsacValidationException(EXCEPTION_MESSAGE));
        final ILoggingEvent element = listAppender.list.get(0);

        assertNull(element.getThrowableProxy());
        assertNotNull(element);
        assertEquals(Level.ERROR, element.getLevel());
        assertTrue(StringUtils.isNotBlank(element.getMessage()));
    }

    @Test
    void error_WithPrependedMessage_LogIsOutput() {
        this.faultHandler.error(TEST_PREPENDED_MESSAGE, new CsacValidationException(EXCEPTION_MESSAGE));
        final ILoggingEvent element = listAppender.list.get(0);

        assertNull(element.getThrowableProxy());
        assertNotNull(element);
        assertEquals(Level.ERROR, element.getLevel());
        assertTrue(StringUtils.isNotBlank(element.getMessage()));
    }

    @Test
    void error_WithNoPrependedMessage_LogIsOutput() {
        this.faultHandler.error(new CsacValidationException(EXCEPTION_MESSAGE));
        final ILoggingEvent element = listAppender.list.get(0);

        assertNull(element.getThrowableProxy());
        assertNotNull(element);
        assertEquals(Level.ERROR, element.getLevel());
        assertTrue(StringUtils.isNotBlank(element.getMessage()));
    }

    @Test
    void warning_WithPrependedMessage_LogIsOutput() {
        this.faultHandler.warn(TEST_PREPENDED_MESSAGE, new CsacValidationException(EXCEPTION_MESSAGE));
        final ILoggingEvent element = listAppender.list.get(0);

        assertNull(element.getThrowableProxy());
        assertNotNull(element);
        assertEquals(Level.WARN, element.getLevel());
        assertTrue(StringUtils.isNotBlank(element.getMessage()));
    }

    @Test
    void warning_WithNoPrependedMessage_LogIsOutput() {
        this.faultHandler.warn(new CsacValidationException(EXCEPTION_MESSAGE));
        final ILoggingEvent element = listAppender.list.get(0);

        assertNull(element.getThrowableProxy());
        assertNotNull(element);
        assertEquals(Level.WARN, element.getLevel());
        assertTrue(StringUtils.isNotBlank(element.getMessage()));
    }

    @Test
    void fatal_TraceEnabled_LogIsOutput() {
        log.setLevel(Level.TRACE);

        this.faultHandler.fatal(new CsacValidationException(EXCEPTION_MESSAGE));
        final ILoggingEvent element = listAppender.list.get(0);

        IThrowableProxy throwableProxy = element.getThrowableProxy();

        assertNotNull(throwableProxy);
        assertNotNull(element);
        assertEquals(Level.ERROR, element.getLevel());
        assertTrue(StringUtils.isNotBlank(element.getMessage()));
        assertEquals(CsacValidationException.class.getName(), throwableProxy.getClassName());

    }

    @Test
    void error_TraceEnabled_LogIsOutput() {
        log.setLevel(Level.TRACE);

        this.faultHandler.error(new CsacValidationException(EXCEPTION_MESSAGE));
        final ILoggingEvent element = listAppender.list.get(0);

        IThrowableProxy throwableProxy = element.getThrowableProxy();

        assertNotNull(throwableProxy);
        assertNotNull(element);
        assertEquals(Level.ERROR, element.getLevel());
        assertTrue(StringUtils.isNotBlank(element.getMessage()));
        assertEquals(CsacValidationException.class.getName(), throwableProxy.getClassName());

    }

    @Test
    void warn_TraceEnabled_LogIsOutput() {
        log.setLevel(Level.TRACE);

        this.faultHandler.warn(new CsacValidationException(EXCEPTION_MESSAGE));
        final ILoggingEvent element = listAppender.list.get(0);

        IThrowableProxy throwableProxy = element.getThrowableProxy();

        assertNotNull(throwableProxy);
        assertNotNull(element);
        assertEquals(Level.WARN, element.getLevel());
        assertTrue(StringUtils.isNotBlank(element.getMessage()));
        assertEquals(CsacValidationException.class.getName(), throwableProxy.getClassName());

    }

    @Test
    void fatal_DebugEnabled_LogIsOutput() {
        log.setLevel(Level.DEBUG);

        this.faultHandler.fatal(new CsacValidationException(EXCEPTION_MESSAGE));
        final ILoggingEvent element = listAppender.list.get(0);

        IThrowableProxy throwableProxy = element.getThrowableProxy();

        assertNotNull(throwableProxy);
        assertNotNull(element);
        assertEquals(Level.ERROR, element.getLevel());
        assertTrue(StringUtils.isNotBlank(element.getMessage()));
        assertEquals(CsacValidationException.class.getName(), throwableProxy.getClassName());

    }

    @Test
    void error_DebugEnabled_LogIsOutput() {
        log.setLevel(Level.DEBUG);

        this.faultHandler.error(new CsacValidationException(EXCEPTION_MESSAGE));
        final ILoggingEvent element = listAppender.list.get(0);

        IThrowableProxy throwableProxy = element.getThrowableProxy();

        assertNotNull(throwableProxy);
        assertNotNull(element);
        assertEquals(Level.ERROR, element.getLevel());
        assertTrue(StringUtils.isNotBlank(element.getMessage()));
        assertEquals(CsacValidationException.class.getName(), throwableProxy.getClassName());

    }

    @Test
    void warn_DebugEnabled_LogIsOutput() {
        log.setLevel(Level.DEBUG);

        this.faultHandler.warn(new CsacValidationException(EXCEPTION_MESSAGE));
        final ILoggingEvent element = listAppender.list.get(0);

        IThrowableProxy throwableProxy = element.getThrowableProxy();

        assertNotNull(throwableProxy);
        assertNotNull(element);
        assertEquals(Level.WARN, element.getLevel());
        assertTrue(StringUtils.isNotBlank(element.getMessage()));
        assertEquals(CsacValidationException.class.getName(), throwableProxy.getClassName());

    }
}