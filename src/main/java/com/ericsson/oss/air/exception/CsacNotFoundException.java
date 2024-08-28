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
 * CsacNotFoundException to indicate requested resource is not found on this server
 */
public class CsacNotFoundException extends RuntimeException {

    /**
     * Instantiates a new Csac Resource Not Found exception.
     *
     * @param message the message
     */
    public CsacNotFoundException(final String message) {
        super(message);
    }
}
