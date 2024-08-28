/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.handler.victoriametrics;

import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.ericsson.oss.air.csac.handler.event.ConsistencyCheckEvent;
import com.ericsson.oss.air.csac.handler.event.ConsistencyCheckHandler;
import com.ericsson.oss.air.csac.handler.kpi.KpiProvisioningHandler;
import com.ericsson.oss.air.csac.model.ProfileDefinition;
import com.ericsson.oss.air.csac.model.pmsc.KpiDefinitionDTOWithRelationship;
import com.ericsson.oss.air.csac.repository.DeployedProfileDAO;
import com.ericsson.oss.air.csac.repository.cache.ResolvedKpiCache;
import com.ericsson.oss.air.csac.service.KPICalculator;
import com.ericsson.oss.air.exception.CsacConsistencyCheckException;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

/**
 * Provisioning handler for Victoria Metrics.  Currently, this class is stubbed so that it will perform the runtime KPI calculation but will not
 * trigger a provisioning service to actively create KPI definitions in Victoria Metrics.
 */
@Component
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(name = "provisioning.vm.enabled", havingValue = "true")
public class VmKpiProvisioningHandler extends KpiProvisioningHandler {

    private final KPICalculator kpiCalculator;

    private final DeployedProfileDAO deployedProfileDAO;

    private final ResolvedKpiCache resolvedKpiCache;

    private final ConsistencyCheckHandler consistencyCheckHandler;

    @SneakyThrows
    @Override
    protected void doApply(final List<ProfileDefinition> profileDefinitions) {

        final List<KpiDefinitionDTOWithRelationship> result = this.kpiCalculator.calculateAffectedKPIs(profileDefinitions);

        log.info("Updated {} KPI definitions", result.size());

        this.resolvedKpiCache.flush();

        try {
            this.deployedProfileDAO.insertProfileDefinitions(profileDefinitions);
        } catch (final Exception e) {
            this.consistencyCheckHandler.notifyCheckFailure(new ConsistencyCheckEvent.Payload(ConsistencyCheckEvent.Payload.Type.SUSPECT, 1));
            throw new CsacConsistencyCheckException(e);
        }
    }
}
