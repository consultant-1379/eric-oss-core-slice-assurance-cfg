/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.handler.augmentation;

import java.util.List;

import com.ericsson.oss.air.csac.model.ProfileDefinition;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * {@inheritDoc}
 * This implementation is invoked when AAS provisioning is disabled.
 */
@Component
@Slf4j
public class LoopbackAugmentationHandler implements AugmentationHandler {

    @Override
    public void submit(final List<ProfileDefinition> pendingProfiles) {
        log.info("Provisioning AAS is disabled.");
    }
}
