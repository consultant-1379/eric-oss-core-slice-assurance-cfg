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

import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Component;

/**
 * The FaultHandler API ensures that CSAC errors are logged consistently and level of detail appropriate for the current log level.  For example, when
 * the log level is TRACE or DEBUG, errors are logged with the message and the associated stack trace. When the log level is INFO or above, only the
 * error message is logged.
 */
@Component
@Slf4j
public class FaultHandler {

    private static final String DEFAULT_FATAL_MSG_PREFIX = "Fatal error. Service restart may be required. ";

    /**
     * Logs a fatal error.  'Fatal'-level errors are similar to 'Error'-level errors except that Fatal errors will be prefixed with a message
     * indicating that the error is fatal and a service restart may be required after the error is corrected.
     *
     * @param cause the cause of the fatal logging event
     */
    public void fatal(final Throwable cause) {
        error(DEFAULT_FATAL_MSG_PREFIX, cause);
    }

    /**
     * Logs an error.
     *
     * @param cause the cause of the logging event
     */
    public void error(final Throwable cause) {
        error(Strings.EMPTY, cause);
    }

    /**
     * Logs an error with a message starting with {@code messagePrefix}.
     *
     * @param messagePrefix the message that will be prepended to the outputted message
     * @param cause         the cause of the logging event
     */
    public void error(final String messagePrefix, final Throwable cause) {

        final String message = messagePrefix + cause.getMessage();
        logError(message, cause);
    }

    /**
     * Logs a warning.
     *
     * @param cause the cause of the logging event
     */
    public void warn(final Throwable cause) {
        warn(Strings.EMPTY, cause);
    }

    /**
     * Logs a warning with a message starting with {@code messagePrefix}.
     *
     * @param messagePrefix the message that will be prepended to the outputted message
     * @param cause         the cause of the logging event
     */
    public void warn(final String messagePrefix, final Throwable cause) {

        final String message = messagePrefix + cause.getMessage();
        if (doLogDetails()) {
            log.warn(message, cause);
        } else {
            log.warn(message);
        }
    }

    // simple method for common logging of fatal and error-level faults
    private void logError(final String message, final Throwable throwable) {

        if (doLogDetails()) {
            log.error(message, throwable);
        } else {
            log.error(message);
        }
    }

    private boolean doLogDetails() {
        return log.isTraceEnabled() || log.isDebugEnabled();
    }
}
