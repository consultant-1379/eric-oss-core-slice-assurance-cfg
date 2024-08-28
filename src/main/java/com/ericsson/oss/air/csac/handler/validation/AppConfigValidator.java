/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.handler.validation;

import com.ericsson.oss.air.exception.CsacValidationException;

/**
 * API for application configuration validation.
 */
public interface AppConfigValidator {

    /**
     * Checks the application configuration.  Implementations may check parts of the configuration that are relevant to a specific
     * component or element of the application.
     *
     * @throws CsacValidationException if the targeted application properties are invalid.
     */
    void validateAppConfig();
}
