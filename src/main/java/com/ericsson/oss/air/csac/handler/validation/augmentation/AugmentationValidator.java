/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.handler.validation.augmentation;

import com.ericsson.oss.air.csac.handler.validation.AppConfigValidator;
import com.ericsson.oss.air.csac.handler.validation.CsacValidator;
import com.ericsson.oss.air.csac.model.AugmentationDefinition;

/**
 * The AugmentationValidator is responsible for context validation of augmentation definitions and augmentation application config.
 */
public interface AugmentationValidator extends CsacValidator<AugmentationDefinition>, AppConfigValidator {

}
