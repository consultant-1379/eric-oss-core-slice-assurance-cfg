/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.exception;

/**
 * RuntimeException to indicate exception when loading resources from disk.
 */
public class ResourceFileLoaderException extends RuntimeException {

    /**
     * ResourceFileLoaderException constructor with message and cause
     *
     * @param message exception detail message
     * @param cause exception
     *
     */
    public ResourceFileLoaderException(final String message, final Throwable cause) {
        super(message, cause);
    }

}

