/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.handler.validation.augmentation;

import com.ericsson.oss.air.csac.model.AugmentationDefinition;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * This class bypasses live validation of Augmentation configuration and resources.
 */
@Component
@Slf4j
@ConditionalOnProperty(value = "validation.external.enabled",
                       havingValue = "false")
public class LoopbackAugmentationValidator implements AugmentationValidator {

    @PostConstruct
    public void init() {
        log.warn("External augmentation validation is disabled");
    }

    @Override
    public void validate(final AugmentationDefinition resourceToValidate) {
        // no-op
    }

    @Override
    public void validateAppConfig() {
        // no-op
    }
}
