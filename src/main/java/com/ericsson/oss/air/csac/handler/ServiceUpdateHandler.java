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

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ericsson.oss.air.csac.configuration.metrics.CustomMetricsRegistry;
import com.ericsson.oss.air.csac.handler.augmentation.AugmentationHandler;
import com.ericsson.oss.air.csac.handler.index.IndexProvisioningHandler;
import com.ericsson.oss.air.csac.handler.kpi.KpiProvisioningHandler;
import com.ericsson.oss.air.csac.model.ProfileDefinition;
import com.ericsson.oss.air.util.concurrent.AtomicDouble;
import com.ericsson.oss.air.util.logging.FaultHandler;

import io.micrometer.core.instrument.Counter;
import lombok.RequiredArgsConstructor;

/**
 * This class handlers service update.
 */
@Component
@RequiredArgsConstructor
public class ServiceUpdateHandler {

    /**
     * Enumeration of the possible service update operations for a given provisioning handler.
     */
    public enum ServiceUpdateType {
        CREATE,
        UPDATE,
        DELETE,
        NO_OP
    }

    private static final Counter kpiRuntimeErrorCounter = CustomMetricsRegistry.registerKpiRuntimeErrorCount();

    private static final Counter augRuntimeErrorCounter = CustomMetricsRegistry.registerRuntimeAugmentationErrorCount();

    private static final Counter indexRuntimeErrorCounter = CustomMetricsRegistry.registerRuntimeIndexErrorCount();

    private final AugmentationHandler augmentationHandler;

    private final KpiProvisioningHandler kpiHandler;

    private final IndexProvisioningHandler indexHandler;

    private final FaultHandler faultHandler;

    @Autowired
    private AtomicDouble provisioningAasTime;

    @Autowired
    private AtomicDouble provisioningPmscTime;

    @Autowired
    private AtomicDouble provisioningKpiTime;

    @Autowired
    private AtomicDouble provisioningIndexTime;

    @Autowired
    private AtomicDouble provisioningTotalTime;

    /**
     * Notify ServiceUpdateHandler with a list of pending profiles
     *
     * @param pendingProfiles a list of Profile
     */
    public void notify(final List<ProfileDefinition> pendingProfiles, final Instant csacStart) {

        this.resetAllTimers();

        try {
            this.provisioningAas(pendingProfiles, csacStart);
            this.provisioningKpi(pendingProfiles, csacStart);
            this.provisioningIndex(pendingProfiles, csacStart);
        } catch (final Exception e) {
            this.faultHandler.error("Unable to complete provisioning: ", e);
            throw e;
        } finally {
            this.provisioningTotalTime.set(getTimeElapsed(csacStart));
        }
    }

    private void provisioningAas(final List<ProfileDefinition> pendingProfiles, final Instant csacStart) {

        try {
            this.augmentationHandler.submit(pendingProfiles);
        } catch (final Exception e) {
            augRuntimeErrorCounter.increment();
            throw e;
        } finally {
            final double d = getTimeElapsed(csacStart);
            this.provisioningAasTime.set(d);
        }
    }

    private void provisioningKpi(final List<ProfileDefinition> pendingProfiles, final Instant csacStart) {

        try {
            this.kpiHandler.apply(pendingProfiles);
        } catch (final RuntimeException e) {
            kpiRuntimeErrorCounter.increment();
            throw e;
        } finally {
            final double d = getTimeElapsed(csacStart);
            this.provisioningPmscTime.set(d);
            this.provisioningKpiTime.set(d);
        }
    }

    private void provisioningIndex(final List<ProfileDefinition> pendingProfiles, final Instant csacStart) {

        try {
            this.indexHandler.apply(pendingProfiles);
        } catch (final RuntimeException e) {
            indexRuntimeErrorCounter.increment();
            throw e;
        } finally {
            final double d = getTimeElapsed(csacStart);
            this.provisioningIndexTime.set(d);
        }
    }

    private Double getTimeElapsed(final Instant csacStart) {
        return Duration.between(csacStart, Instant.now()).toMillis() / 1000.0D;
    }

    private void resetAllTimers() {
        this.provisioningAasTime.set(0.0D);
        this.provisioningPmscTime.set(0.0D);
        this.provisioningKpiTime.set(0.0D);
        this.provisioningIndexTime.set(0.0D);
        this.provisioningTotalTime.set(0.0D);
    }
}
