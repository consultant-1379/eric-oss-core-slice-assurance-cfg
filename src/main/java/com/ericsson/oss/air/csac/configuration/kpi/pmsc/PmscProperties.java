/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.configuration.kpi.pmsc;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * This class provides PMSC-specific configuration properties from the application configuration.
 */
@Configuration
@ConfigurationProperties(prefix = "provisioning")
@AllArgsConstructor
@RequiredArgsConstructor
public class PmscProperties {

    private static final int DEFAULT_AGGREGATION_PERIOD = 15;

    @Setter
    private Map<String, Object> pmsc;

    /**
     * Returns the configured data reliability offset as an int.
     *
     * @return the configured data reliability offset as an int
     * @throws NullPointerException if the expected configuration is missing
     * @throws ClassCastException   if the value exists but is not an integer
     */
    public int getDataReliabilityOffset() {
        return getIntValueFrom(((Map<String, Object>) pmsc.get("data")).get("reliabilityOffset"));
    }

    /**
     * Returns the currently configured default aggregation period.
     *
     * @return the currently configured default aggregation period
     */
    public int getDefaultAggregationPeriod() {

        if (this.pmsc.containsKey("aggregationPeriod")) {
            return getIntValueFrom(((Map<String, Object>) this.pmsc.get("aggregationPeriod")).get("default"));
        }

        return DEFAULT_AGGREGATION_PERIOD;
    }

    private int getIntValueFrom(final Object value) {

        return (value instanceof String)
                ? Integer.parseInt((String) value)
                : (Integer) value;

    }
}
