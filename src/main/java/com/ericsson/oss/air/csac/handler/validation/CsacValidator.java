/*******************************************************************************
 * COPYRIGHT Ericsson 2022
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
 * Interface for all CSAC validators
 */
@FunctionalInterface
public interface CsacValidator<T> {

    /**
     * Validates the submitted resource of type T after bean validation.
     *
     * @param resourceToValidate
     *      the submitted resource to validate
     * @throws CsacValidationException
     *      throws this runtime exception if any validation violation occurs
     */
    public void validate(final T resourceToValidate);

}
