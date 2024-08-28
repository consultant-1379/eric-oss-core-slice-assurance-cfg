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

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;

import com.ericsson.oss.air.csac.handler.util.VoidOperator;
import com.ericsson.oss.air.csac.service.augmentation.AugmentationProvisioningService;
import com.ericsson.oss.air.util.concurrent.AtomicDouble;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * Reset operator that resets the effective augmentation configuration in the AAS.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AugmentationResetOperator implements VoidOperator {

    private final AugmentationProvisioningService augmentationProvisioningService;

    @Autowired
    @Qualifier("configurationResetAugErrorCounter")
    private AtomicLong errorCountMetric;

    @Autowired
    @Qualifier("configurationResetAugTime")
    private AtomicDouble elapsedTime;

    @Override
    public void apply() {

        final Instant start = Instant.now();
        try {
            this.augmentationProvisioningService.deleteAll();
            this.errorCountMetric.set(0L);
        } catch (final Exception ex) {
            this.errorCountMetric.incrementAndGet();
            throw ex;
        } finally {
            final double d = Duration.between(start, Instant.now()).toMillis() / 1000.0D;
            this.elapsedTime.set(d);
        }

    }
}
