/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.exception;

/**
 * Runtime exception to indicate an invalid provisioning state transition.
 */
public class CsacProvisioningStateTransitionException extends RuntimeException {

    /**
     * Returns a default {@code CsacProvisioningStateTransitionException}
     */
    public CsacProvisioningStateTransitionException() {
        super();
    }

    /**
     * Returns a {@code CsacProvisioningStateTransitionException} with the specified message.
     *
     * @param message exception message
     */
    public CsacProvisioningStateTransitionException(final String message) {
        super(message);
    }

    /**
     * Returns a {@code CsacProvisioningStateTransitionException} with the specified cause.
     *
     * @param cause cause of this exception
     */
    public CsacProvisioningStateTransitionException(final Throwable cause) {
        super(cause);
    }

    /**
     * Returns a {@code CsacProvisioningStateTransitionException} with the specified message and cause.
     *
     * @param message exception message
     * @param cause   cause of this exception
     */
    public CsacProvisioningStateTransitionException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
