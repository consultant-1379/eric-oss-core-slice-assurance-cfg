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
 * RuntimeException to indicate any exception during CSAC consistency check.
 */

public class CsacConsistencyCheckException extends RuntimeException {

    /**
     * Returns a new {@code CsacConsistencyCheckException} with the specified cause.
     *
     * @param cause cause of this exception
     */
    public CsacConsistencyCheckException(final Throwable cause) {
        super(cause);
    }

}