/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.util.logging.audit;

import static com.ericsson.oss.air.util.logging.TestLoggingUtils.FACILITY_KEY;
import static com.ericsson.oss.air.util.logging.TestLoggingUtils.FACILITY_VALUE;
import static com.ericsson.oss.air.util.logging.TestLoggingUtils.SUBJECT_KEY;
import static com.ericsson.oss.air.util.logging.TestLoggingUtils.SUBJECT_VALUE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.SQLException;
import java.sql.SQLTransientConnectionException;
import java.util.Map;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.core.read.ListAppender;
import com.ericsson.oss.air.CoreApplication;
import com.ericsson.oss.air.exception.CsacDAOException;
import com.ericsson.oss.air.exception.CsacValidationException;
import org.apache.logging.log4j.util.Strings;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.transaction.TransactionSystemException;

class AuditLoggerTest {

    private static final Class<?> TEST_CLASS = CoreApplication.class;

    private static final String BEFORE_AUDIT_EVENT = "before audit event";

    private static final String AFTER_AUDIT_EVENT = "after audit event";

    private static final String UNFORMATTED_MSG = "something audit worthy";

    private static final String FORMAT_STR = "foo{}";

    private static final String TEST_ARGUMENT = "bar";

    private static final String FORMATTED_MSG = "foobar";

    private static final String EXCEPTION_MSG = "oops";

    private static final String DATABASE_MSG = "Cannot connect to database: ";

    private static final Exception TEST_EXCEPTION = new CsacValidationException(EXCEPTION_MSG);

    private Logger log;

    private ListAppender<ILoggingEvent> listAppender;

    private AuditLogger auditLogger;

    @BeforeEach
    void setUp() {
        this.log = (Logger) LoggerFactory.getLogger(TEST_CLASS);
        this.log.setLevel(Level.TRACE);

        this.listAppender = new ListAppender<>();
        this.listAppender.start();

        this.log.addAppender(this.listAppender);

        this.auditLogger = AuditLogFactory.getLogger(TEST_CLASS);
    }

    @AfterEach
    void tearDown() {
        this.listAppender.stop();
    }

    @Test
    void trace_NoArguments() {
        this.log.info(BEFORE_AUDIT_EVENT);
        this.auditLogger.trace(UNFORMATTED_MSG);
        this.log.info(AFTER_AUDIT_EVENT);

        final ILoggingEvent beforeEvent = this.listAppender.list.get(0);
        final ILoggingEvent auditEvent = this.listAppender.list.get(1);
        final ILoggingEvent afterEvent = this.listAppender.list.get(2);

        assertSurroundingEvents(beforeEvent, afterEvent);
        assertAuditEvent(auditEvent, Level.TRACE, UNFORMATTED_MSG);
    }

    @Test
    void trace_WithArguments() {
        this.log.info(BEFORE_AUDIT_EVENT);
        this.auditLogger.trace(FORMAT_STR, TEST_ARGUMENT);
        this.log.info(AFTER_AUDIT_EVENT);

        final ILoggingEvent beforeEvent = this.listAppender.list.get(0);
        final ILoggingEvent auditEvent = this.listAppender.list.get(1);
        final ILoggingEvent afterEvent = this.listAppender.list.get(2);

        assertSurroundingEvents(beforeEvent, afterEvent);
        assertAuditEvent(auditEvent, Level.TRACE, FORMATTED_MSG);
    }

    @Test
    void trace_Exception() {
        this.log.info(BEFORE_AUDIT_EVENT);
        this.auditLogger.trace(UNFORMATTED_MSG, TEST_EXCEPTION);
        this.log.info(AFTER_AUDIT_EVENT);

        final ILoggingEvent beforeEvent = this.listAppender.list.get(0);
        final ILoggingEvent auditEvent = this.listAppender.list.get(1);
        final ILoggingEvent afterEvent = this.listAppender.list.get(2);

        assertSurroundingEvents(beforeEvent, afterEvent);
        assertAuditEvent(auditEvent, Level.TRACE, UNFORMATTED_MSG, TEST_EXCEPTION);
    }

    @Test
    void debug_NoArguments() {
        this.log.info(BEFORE_AUDIT_EVENT);
        this.auditLogger.debug(UNFORMATTED_MSG);
        this.log.info(AFTER_AUDIT_EVENT);

        final ILoggingEvent beforeEvent = this.listAppender.list.get(0);
        final ILoggingEvent auditEvent = this.listAppender.list.get(1);
        final ILoggingEvent afterEvent = this.listAppender.list.get(2);

        assertSurroundingEvents(beforeEvent, afterEvent);
        assertAuditEvent(auditEvent, Level.DEBUG, UNFORMATTED_MSG);
    }

    @Test
    void debug_WithArguments() {
        this.log.info(BEFORE_AUDIT_EVENT);
        this.auditLogger.debug(FORMAT_STR, TEST_ARGUMENT);
        this.log.info(AFTER_AUDIT_EVENT);

        final ILoggingEvent beforeEvent = this.listAppender.list.get(0);
        final ILoggingEvent auditEvent = this.listAppender.list.get(1);
        final ILoggingEvent afterEvent = this.listAppender.list.get(2);

        assertSurroundingEvents(beforeEvent, afterEvent);
        assertAuditEvent(auditEvent, Level.DEBUG, FORMATTED_MSG);
    }

    @Test
    void debug_Exception() {
        this.log.info(BEFORE_AUDIT_EVENT);
        this.auditLogger.debug(UNFORMATTED_MSG, TEST_EXCEPTION);
        this.log.info(AFTER_AUDIT_EVENT);

        final ILoggingEvent beforeEvent = this.listAppender.list.get(0);
        final ILoggingEvent auditEvent = this.listAppender.list.get(1);
        final ILoggingEvent afterEvent = this.listAppender.list.get(2);

        assertSurroundingEvents(beforeEvent, afterEvent);
        assertAuditEvent(auditEvent, Level.DEBUG, UNFORMATTED_MSG, TEST_EXCEPTION);
    }

    @Test
    void info_NoArguments() {
        this.log.info(BEFORE_AUDIT_EVENT);
        this.auditLogger.info(UNFORMATTED_MSG);
        this.log.info(AFTER_AUDIT_EVENT);

        final ILoggingEvent beforeEvent = this.listAppender.list.get(0);
        final ILoggingEvent auditEvent = this.listAppender.list.get(1);
        final ILoggingEvent afterEvent = this.listAppender.list.get(2);

        assertSurroundingEvents(beforeEvent, afterEvent);
        assertAuditEvent(auditEvent, Level.INFO, UNFORMATTED_MSG);
    }

    @Test
    void info_WithArguments() {
        this.log.info(BEFORE_AUDIT_EVENT);
        this.auditLogger.info(FORMAT_STR, TEST_ARGUMENT);
        this.log.info(AFTER_AUDIT_EVENT);

        final ILoggingEvent beforeEvent = this.listAppender.list.get(0);
        final ILoggingEvent auditEvent = this.listAppender.list.get(1);
        final ILoggingEvent afterEvent = this.listAppender.list.get(2);

        assertSurroundingEvents(beforeEvent, afterEvent);
        assertAuditEvent(auditEvent, Level.INFO, FORMATTED_MSG);
    }

    @Test
    void info_Exception() {
        this.log.info(BEFORE_AUDIT_EVENT);
        this.auditLogger.info(UNFORMATTED_MSG, TEST_EXCEPTION);
        this.log.info(AFTER_AUDIT_EVENT);

        final ILoggingEvent beforeEvent = this.listAppender.list.get(0);
        final ILoggingEvent auditEvent = this.listAppender.list.get(1);
        final ILoggingEvent afterEvent = this.listAppender.list.get(2);

        assertSurroundingEvents(beforeEvent, afterEvent);
        assertAuditEvent(auditEvent, Level.INFO, UNFORMATTED_MSG, TEST_EXCEPTION);
    }

    @Test
    void warn_NoArguments() {
        this.log.info(BEFORE_AUDIT_EVENT);
        this.auditLogger.warn(UNFORMATTED_MSG);
        this.log.info(AFTER_AUDIT_EVENT);

        final ILoggingEvent beforeEvent = this.listAppender.list.get(0);
        final ILoggingEvent auditEvent = this.listAppender.list.get(1);
        final ILoggingEvent afterEvent = this.listAppender.list.get(2);

        assertSurroundingEvents(beforeEvent, afterEvent);
        assertAuditEvent(auditEvent, Level.WARN, UNFORMATTED_MSG);
    }

    @Test
    void warn_WithArguments() {
        this.log.info(BEFORE_AUDIT_EVENT);
        this.auditLogger.warn(FORMAT_STR, TEST_ARGUMENT);
        this.log.info(AFTER_AUDIT_EVENT);

        final ILoggingEvent beforeEvent = this.listAppender.list.get(0);
        final ILoggingEvent auditEvent = this.listAppender.list.get(1);
        final ILoggingEvent afterEvent = this.listAppender.list.get(2);

        assertSurroundingEvents(beforeEvent, afterEvent);
        assertAuditEvent(auditEvent, Level.WARN, FORMATTED_MSG);
    }

    @Test
    void warn_Exception() {
        this.log.info(BEFORE_AUDIT_EVENT);
        this.auditLogger.warn(UNFORMATTED_MSG, TEST_EXCEPTION);
        this.log.info(AFTER_AUDIT_EVENT);

        final ILoggingEvent beforeEvent = this.listAppender.list.get(0);
        final ILoggingEvent auditEvent = this.listAppender.list.get(1);
        final ILoggingEvent afterEvent = this.listAppender.list.get(2);

        assertSurroundingEvents(beforeEvent, afterEvent);
        assertAuditEvent(auditEvent, Level.WARN, UNFORMATTED_MSG, TEST_EXCEPTION);
    }

    @Test
    void error_NoArguments() {
        this.log.info(BEFORE_AUDIT_EVENT);
        this.auditLogger.error(UNFORMATTED_MSG);
        this.log.info(AFTER_AUDIT_EVENT);

        final ILoggingEvent beforeEvent = this.listAppender.list.get(0);
        final ILoggingEvent auditEvent = this.listAppender.list.get(1);
        final ILoggingEvent afterEvent = this.listAppender.list.get(2);

        assertSurroundingEvents(beforeEvent, afterEvent);
        assertAuditEvent(auditEvent, Level.ERROR, UNFORMATTED_MSG);
    }

    @Test
    void error_WithArguments() {
        this.log.info(BEFORE_AUDIT_EVENT);
        this.auditLogger.error(FORMAT_STR, TEST_ARGUMENT);
        this.log.info(AFTER_AUDIT_EVENT);

        final ILoggingEvent beforeEvent = this.listAppender.list.get(0);
        final ILoggingEvent auditEvent = this.listAppender.list.get(1);
        final ILoggingEvent afterEvent = this.listAppender.list.get(2);

        assertSurroundingEvents(beforeEvent, afterEvent);
        assertAuditEvent(auditEvent, Level.ERROR, FORMATTED_MSG);
    }

    @Test
    void error_Exception() {
        this.log.info(BEFORE_AUDIT_EVENT);
        this.auditLogger.error(UNFORMATTED_MSG, TEST_EXCEPTION);
        this.log.info(AFTER_AUDIT_EVENT);

        final ILoggingEvent beforeEvent = this.listAppender.list.get(0);
        final ILoggingEvent auditEvent = this.listAppender.list.get(1);
        final ILoggingEvent afterEvent = this.listAppender.list.get(2);

        assertSurroundingEvents(beforeEvent, afterEvent);
        assertAuditEvent(auditEvent, Level.ERROR, UNFORMATTED_MSG, TEST_EXCEPTION);
    }

    @Test
    void error_Throwable_NullException() {

        this.auditLogger.error(null);

        assertTrue(this.listAppender.list.isEmpty());
    }

    @Test
    void error_Throwable_CsacDAOException_WithAuditableCause() {

        final DataAccessException nestedException = new DataAccessResourceFailureException(EXCEPTION_MSG);
        final CsacDAOException exception = new CsacDAOException(nestedException);

        final String expectedExceptionMessage = "org.springframework.dao.DataAccessResourceFailureException: " + EXCEPTION_MSG;

        this.auditLogger.error(exception);

        assertFalse(this.listAppender.list.isEmpty());
        assertAuditEvent(this.listAppender.list.get(0), Level.ERROR, DATABASE_MSG, exception, expectedExceptionMessage);

    }

    @Test
    void error_Throwable_CsacDAOException_WithNonAuditableCause() {

        final Exception nestedException = new CsacValidationException(UNFORMATTED_MSG);
        final CsacDAOException exception = new CsacDAOException(nestedException);

        this.auditLogger.error(exception);

        assertTrue(this.listAppender.list.isEmpty());
    }

    @Test
    void error_Throwable_CsacDAOException_NullCause() {

        final CsacDAOException exception = new CsacDAOException(UNFORMATTED_MSG);

        this.auditLogger.error(exception);

        assertTrue(this.listAppender.list.isEmpty());
    }

    @Test
    void error_Throwable_TransactionSystemException_WithAuditableCause() {

        final DataAccessException nestedException = new DataAccessResourceFailureException(Strings.EMPTY);
        final TransactionSystemException exception = new TransactionSystemException(EXCEPTION_MSG);
        exception.initApplicationException(nestedException);

        this.auditLogger.error(exception);

        assertFalse(this.listAppender.list.isEmpty());
        assertAuditEvent(this.listAppender.list.get(0), Level.ERROR, DATABASE_MSG, exception);
    }

    @Test
    void error_Throwable_TransactionSystemException_WithNonAuditableCause() {

        final Exception nestedException = new CsacValidationException(Strings.EMPTY);
        final TransactionSystemException exception = new TransactionSystemException(EXCEPTION_MSG, nestedException);
        exception.initApplicationException(nestedException);

        this.auditLogger.error(exception);

        assertTrue(this.listAppender.list.isEmpty());
    }

    @Test
    void error_Throwable_DataAccessResourceFailureException() {

        final DataAccessException exception = new CannotGetJdbcConnectionException(EXCEPTION_MSG);

        this.auditLogger.error(exception);

        assertFalse(this.listAppender.list.isEmpty());
        assertAuditEvent(this.listAppender.list.get(0), Level.ERROR, DATABASE_MSG, exception);
    }

    @Test
    void error_Throwable_SQLTransientConnectionException() {

        final SQLException exception = new SQLTransientConnectionException(EXCEPTION_MSG);

        this.auditLogger.error(exception);

        assertFalse(this.listAppender.list.isEmpty());
        assertAuditEvent(this.listAppender.list.get(0), Level.ERROR, DATABASE_MSG, exception);
    }

    @Test
    void error_Throwable_NonAuditableException() {

        final Exception exception = new CsacValidationException(EXCEPTION_MSG);

        this.auditLogger.error(exception);

        assertTrue(this.listAppender.list.isEmpty());
    }

    private void assertAuditEvent(final ILoggingEvent auditEvent, final Level level, final String message) {

        assertNotNull(auditEvent);
        assertEquals(level, auditEvent.getLevel());
        assertEquals(message, auditEvent.getFormattedMessage());

        final Map<String, String> mdcPropMap = auditEvent.getMDCPropertyMap();

        assertFalse(mdcPropMap.isEmpty());
        assertEquals(2, mdcPropMap.size());
        assertEquals(FACILITY_VALUE, mdcPropMap.get(FACILITY_KEY));
        assertEquals(SUBJECT_VALUE, mdcPropMap.get(SUBJECT_KEY));

    }

    private void assertAuditEvent(final ILoggingEvent auditEvent, final Level level, final String message, final Exception exception) {
        this.assertAuditEvent(auditEvent, level, message, exception, EXCEPTION_MSG);
    }

    private void assertAuditEvent(final ILoggingEvent auditEvent, final Level level, final String message, final Exception exception,
                                  final String exceptionMessage) {

        assertNotNull(auditEvent);
        assertEquals(level, auditEvent.getLevel());
        assertEquals(message, auditEvent.getFormattedMessage());

        final IThrowableProxy iThrowableProxy = auditEvent.getThrowableProxy();

        assertNotNull(iThrowableProxy);
        assertEquals(exceptionMessage, iThrowableProxy.getMessage());
        assertEquals(exception.getClass().getName(), iThrowableProxy.getClassName());

        final Map<String, String> mdcPropMap = auditEvent.getMDCPropertyMap();

        assertFalse(mdcPropMap.isEmpty());
        assertEquals(2, mdcPropMap.size());
        assertEquals(FACILITY_VALUE, mdcPropMap.get(FACILITY_KEY));
        assertEquals(SUBJECT_VALUE, mdcPropMap.get(SUBJECT_KEY));
    }

    private void assertSurroundingEvents(final ILoggingEvent beforeEvent, final ILoggingEvent afterEvent) {

        assertNotNull(beforeEvent);
        assertEquals(Level.INFO, beforeEvent.getLevel());
        assertEquals(BEFORE_AUDIT_EVENT, beforeEvent.getMessage());
        assertTrue(beforeEvent.getMDCPropertyMap().isEmpty());

        assertNotNull(afterEvent);
        assertEquals(Level.INFO, afterEvent.getLevel());
        assertEquals(AFTER_AUDIT_EVENT, afterEvent.getMessage());
        assertTrue(afterEvent.getMDCPropertyMap().isEmpty());
    }
}