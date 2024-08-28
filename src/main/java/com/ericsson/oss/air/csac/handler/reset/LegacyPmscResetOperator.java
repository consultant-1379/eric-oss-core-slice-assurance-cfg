/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.handler.reset;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Implementation of {@link PmscConfigurationResetOperator} for legacy pmsc rest client
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "provisioning.pmsc.restClient",
                       name = "legacy",
                       havingValue = "true")
public class LegacyPmscResetOperator implements PmscConfigurationResetOperator {

    @Override
    public void apply() {
        log.warn("Configuration reset is not supported for the legacy PMSC service");
    }
}
