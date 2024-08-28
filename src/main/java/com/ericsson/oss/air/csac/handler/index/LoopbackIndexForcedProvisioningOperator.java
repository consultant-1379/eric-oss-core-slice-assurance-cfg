/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.handler.index;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Loopback forced index provisioning operator.  This will be called if index provisioning is disabled.
 */
@Component
@ConditionalOnProperty(value = "provisioning.index.enabled",
        havingValue = "false")
@RequiredArgsConstructor
@Slf4j
public class LoopbackIndexForcedProvisioningOperator extends IndexForcedProvisioningOperator {

    private final LoopbackIndexProvisioningHandler indexProvisioningHandler;

    @Override
    protected void doApply(final Void unused) {
        this.indexProvisioningHandler.apply(null);
    }
}
