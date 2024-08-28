/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.exception;

/**
 * RuntimeException to indicate any exception in CSAC DAOs.
 */

public class CsacDAOException extends RuntimeException {

    /**
     * Returns a {@code CsacDAOException} with the specified message.
     *
     * @param message
     *         detailed exception message
     */
    public CsacDAOException(final String message) {
        super(message);
    }

    /**
     * Returns a new {@code CsacDAOException} with the specified cause.
     *
     * @param cause
     *         cause of this exception
     */
    public CsacDAOException(final Throwable cause) {
        super(cause);
    }

    /**
     * Returns a new {@code CsacDAOException} with the specified message and cause.
     *
     * @param message
     *         message for this exception
     * @param cause
     *         cause of this exception
     */
    public CsacDAOException(final String message, final Throwable cause) {
        super(message, cause);
    }

}