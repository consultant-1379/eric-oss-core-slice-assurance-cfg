/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.handler.reset;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.ericsson.oss.air.csac.handler.status.ProvisioningTracker;
import com.ericsson.oss.air.util.concurrent.AtomicDouble;
import com.ericsson.oss.air.util.logging.FaultHandler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Configuration reset handler invokes reset operators for targeted reset operations.
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class ResetConfigurationHandler {

    private final AugmentationResetOperator augmentationResetOperator;

    private final KpiConfigurationResetOperator kpiConfigurationResetOperator;

    private final DbResetOperator dbResetOperator;

    private final IndexerConfigurationResetOperator indexerConfigResetOperator;

    private final FaultHandler faultHandler;

    private final ProvisioningTracker provisioningTracker;

    @Autowired
    private AtomicDouble configurationResetTotalTime;

    @Autowired
    private AtomicLong configurationResetTotalErrors;

    @Autowired
    private AtomicLong configurationResetAugErrorCounter;

    @Autowired
    private AtomicLong configurationResetKpiErrorCounter;

    @Autowired
    private AtomicLong configurationResetDbErrorCounter;

    @Autowired
    private AtomicLong configurationResetIndexErrorCounter;

    @Value("${provisioning.pmsc.enabled}")
    private boolean isPmscEnabled;

    /**
     * Applies the reset operations for resetting CSAC.
     */
    public void apply() {

        final Instant start = Instant.now();

        final KpiConfigurationResetOperator kpiResetOperator = getKpiResetOperator(this.isPmscEnabled);

        try {
            kpiResetOperator
                    .andThen(this.augmentationResetOperator)
                    .andThen(this.indexerConfigResetOperator)
                    .andThen(this.dbResetOperator)
                    .apply();

            // on successful reset, add the reset state
            this.provisioningTracker.resetProvisioning();
        } catch (final Exception ex) {
            this.faultHandler.error("Unable to complete reset operation: ", ex);
            throw ex;
        } finally {
            final double d = Duration.between(start, Instant.now()).toMillis() / 1000.0D;
            this.configurationResetTotalTime.set(d);
            this.configurationResetTotalErrors.set(getTotalErrors());
        }
    }

    private long getTotalErrors() {
        return this.configurationResetDbErrorCounter.get() + this.configurationResetAugErrorCounter.get()
                + this.configurationResetKpiErrorCounter.get() + this.configurationResetIndexErrorCounter.get();
    }

    /*
     * (non-javadoc)
     *
     * Returns a stubbed KpiConfigurationResetOperator if PMSC is disabled.
     */
    protected KpiConfigurationResetOperator getKpiResetOperator(final boolean pmscEnabled) {
        return pmscEnabled
                ? this.kpiConfigurationResetOperator
                : (() -> log.info("KPI configuration reset is disabled"));
    }
}
