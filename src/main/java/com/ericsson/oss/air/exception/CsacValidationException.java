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
 * Runtime exception to indicate a validation exception when loading or validating CSAC resources.
 */
public class CsacValidationException extends RuntimeException {

    private static final long serialVersionUID = 1168281363082259682L;

    /**
     * Creates a {@code CsacValidationException} with a message.
     *
     * @param message
     *         detailed exception message
     */
    public CsacValidationException(final String message) {
        super(message);
    }

    /**
     * Creates a {@code CsacValidationException} with the specified cause.
     *
     * @param cause
     *         cause of this exception.
     */
    public CsacValidationException(final Throwable cause) {
        super(cause);
    }
}

