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
import com.ericsson.oss.air.csac.service.index.IndexerRestClient;
import com.ericsson.oss.air.util.concurrent.AtomicDouble;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * Reset operator that resets the deployed indexer definitions in the AIS.
 */
@Component
@Slf4j
public class IndexerConfigurationResetOperator implements VoidOperator {

    private final IndexerRestClient indexerRestClient;

    @Autowired
    @Qualifier("configurationResetIndexErrorCounter")
    private AtomicLong errorCounter;

    @Autowired
    @Qualifier("configurationResetIndexTime")
    private AtomicDouble elapsedTime;

    public IndexerConfigurationResetOperator(IndexerRestClient indexerRestClient) {
        this.indexerRestClient = indexerRestClient;
    }

    @Override
    public void apply() {
        final Instant start = Instant.now();
        try {
            this.indexerRestClient.deleteAll();
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
