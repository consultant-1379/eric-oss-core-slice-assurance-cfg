/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.handler;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.ericsson.oss.air.csac.handler.index.IndexForcedProvisioningOperator;
import com.ericsson.oss.air.util.operator.SequentialOperator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Configuration class for forced provisioning operations.  This class creates the required composite operator bean for performing all requried
 * forced provisioning operations.
 */
@RequiredArgsConstructor
@Slf4j
@Configuration
public class ForcedProvisioningConfiguration {

    private final IndexForcedProvisioningOperator indexForcedProvisioningOperator;

    /**
     * Returns a composed operator that will perform all required force provisioning operations.
     *
     * @param forceIndex if true, indicates that index provisioning must be forced.
     * @return composed operator that will perform all required force provisioning operations
     */
    @Bean(name = "forcedProvisioningOperator")
    public SequentialOperator<Void> forcedProvisioningOperator(@Value("${provisioning.index.force}") final boolean forceIndex) {

        final SequentialOperator<Void> provisioningOperator = new SequentialOperator<Void>() {
            @Override
            protected void doApply(final Void unused) {
                // intentionally empty as this is a no-op
            }
        };

        if (forceIndex) {
            provisioningOperator.then(this.indexForcedProvisioningOperator);
        }

        return provisioningOperator;
    }
}
