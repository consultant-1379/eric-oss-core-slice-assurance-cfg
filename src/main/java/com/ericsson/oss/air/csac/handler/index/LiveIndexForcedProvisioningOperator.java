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

import java.util.List;
import java.util.Map;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import com.ericsson.oss.air.csac.model.runtime.RuntimeKpiInstance;
import com.ericsson.oss.air.csac.model.runtime.index.DeployedIndexDefinitionDto;
import com.ericsson.oss.air.csac.service.index.IndexerProvisioningService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Live implementation of the index forced provisioning operator.  This operator reads the runtime index configuration from the CSAC runtime data
 * store and provisions the index service, regardless of whether or nor the service had been previously provisioned.
 */
@RequiredArgsConstructor
@Slf4j
@Component
@ConditionalOnProperty(value = "provisioning.index.enabled",
        havingValue = "true")
public class LiveIndexForcedProvisioningOperator extends IndexForcedProvisioningOperator {

    private final LiveIndexProvisioningHandler indexProvisioningHandler;

    private final IndexerProvisioningService indexerService;

    @Override
    protected void doApply(final Void unused) {

        log.info("Forcing indexer provisioning");

        final Map<String, List<RuntimeKpiInstance>> writerData = this.indexProvisioningHandler.getWriterData();

        if (writerData.isEmpty()) {
            log.info("No indexable KPIs.  Skipping indexer provisioning");
            return;
        }

        final DeployedIndexDefinitionDto defaultIndex = this.indexProvisioningHandler.getDefaultIndexDefinition(writerData);

        this.indexerService.create(defaultIndex);
    }
}
