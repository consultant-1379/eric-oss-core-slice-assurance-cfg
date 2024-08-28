/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.handler.validation.pmsc;

import com.ericsson.oss.air.csac.configuration.kpi.pmsc.PmscProperties;
import com.ericsson.oss.air.csac.handler.validation.AppConfigValidator;
import com.ericsson.oss.air.exception.CsacValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Validation API for PMSC-specific application configuration.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PmscConfigurationValidator implements AppConfigValidator {

    private final PmscProperties pmscProperties;

    @Override
    public void validateAppConfig() {
        checkDataReliabilityOffset();
    }

    /*
     * (non-javadoc)
     *
     * Checks the data reliability offset.  The value must be less than or equal to the current aggregation period.
     */
    protected void checkDataReliabilityOffset() {

        final int configuredValue = this.pmscProperties.getDataReliabilityOffset();
        final int aggregationPeriod = this.pmscProperties.getDefaultAggregationPeriod();

        if (configuredValue > aggregationPeriod) {
            throw new CsacValidationException(
                    "Invalid PMSC data reliability offset. Value must be less than or equal to the aggregation period (" + aggregationPeriod + ")");
        }
    }
}
