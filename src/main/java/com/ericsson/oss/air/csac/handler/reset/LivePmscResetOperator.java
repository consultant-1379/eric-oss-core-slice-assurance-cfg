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

import com.ericsson.oss.air.csac.repository.cache.ResolvedKpiCache;
import com.ericsson.oss.air.csac.service.kpi.pmsc.PmscRestClient;
import com.ericsson.oss.air.util.concurrent.AtomicDouble;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Implementation of {@link PmscConfigurationResetOperator} which invokes {@link PmscRestClient}
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "provisioning.pmsc.restClient",
                       name = "legacy",
                       havingValue = "false")
@RequiredArgsConstructor
public class LivePmscResetOperator implements PmscConfigurationResetOperator {

    private final PmscRestClient pmscRestClient;

    private final ResolvedKpiCache resolvedKpiCache;

    @Autowired
    @Qualifier("configurationResetKpiErrorCounter")
    private AtomicLong errorCounter;

    @Autowired
    @Qualifier("configurationResetKpiTime")
    private AtomicDouble elapsedTime;

    @Override
    public void apply() {
        final Instant start = Instant.now();
        try {
            this.pmscRestClient.deleteAll();
            this.resolvedKpiCache.deleteAll();
            this.errorCounter.set(0L);
        } catch (final Exception ex) {
            this.errorCounter.incrementAndGet();
            throw ex;
        } finally {
            final double total = Duration.between(start, Instant.now()).toMillis() / 1000.0D;
            this.elapsedTime.set(total);
        }

    }
}
