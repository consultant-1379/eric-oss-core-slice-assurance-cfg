/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.handler.pmsc.legacy;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import com.ericsson.oss.air.csac.handler.event.ConsistencyCheckEvent;
import com.ericsson.oss.air.csac.handler.event.ConsistencyCheckHandler;
import com.ericsson.oss.air.csac.handler.pmsc.transform.KpiSubmissionTransformer;
import com.ericsson.oss.air.csac.model.ProfileDefinition;
import com.ericsson.oss.air.csac.model.pmsc.KpiDefinitionDTO;
import com.ericsson.oss.air.csac.model.pmsc.KpiDefinitionDTOWithRelationship;
import com.ericsson.oss.air.csac.model.pmsc.KpiDefinitionSubmission;
import com.ericsson.oss.air.csac.repository.DeployedProfileDAO;
import com.ericsson.oss.air.csac.repository.cache.ResolvedKpiCache;
import com.ericsson.oss.air.csac.service.kpi.pmsc.PmscRestClient;
import com.ericsson.oss.air.csac.service.kpi.pmsc.legacy.PmscProvisioningService;
import com.ericsson.oss.air.exception.CsacConsistencyCheckException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * This class will provision a running PMSC.
 */
@Component
@Slf4j
@RequiredArgsConstructor
@Primary
public class LiveProvisioner implements Provisioner {

    private final DeployedProfileDAO deployedProfileDAO;

    private final KpiSubmissionTransformer kpiSubmissionTransformer;

    private final PmscProvisioningService legacyRestClient;

    private final PmscRestClient restClient;

    private final ResolvedKpiCache resolvedKpiCache;

    private final ConsistencyCheckHandler consistencyCheckHandler;

    @Value("${provisioning.pmsc.restClient.legacy:false}")
    private boolean isLegacyPmscClient;

    @Override
    public void provision(final List<KpiDefinitionDTOWithRelationship> kpiDefinitionDTOs, final List<ProfileDefinition> pendingProfiles) {

        final List<KpiDefinitionDTO> kpis = new ArrayList<>(kpiDefinitionDTOs);

        final KpiDefinitionSubmission kpiDefinitionSubmission = kpiSubmissionTransformer.apply(kpis);

        if (this.isLegacyPmscClient) {
            this.legacyRestClient.updatePMSCKpisDefinitions(kpiDefinitionSubmission);
        } else {
            this.restClient.create(kpiDefinitionSubmission);
        }

        /**
         * Manually triggering the KPI calculations should only be done if there is at least one on-demand runtime KPI definition in the PMSC
         * submission.
         * The two other types of runtime KPI Definitions: simple and complex are scheduled KPIs. They will be calculated on a periodic basis.
         * If there are no on-demand KPIs and the calculations are manually triggered, then the PMSC will return an error.
         *
         * TODO only invoke this.restClient.createPMSCKpisCalculation if there are on-demand runtime KPI Definitions
         */

        this.resolvedKpiCache.flush();

        try {
            this.deployedProfileDAO.insertProfileDefinitions(pendingProfiles);
        } catch (final Exception e) {
            this.consistencyCheckHandler.notifyCheckFailure(new ConsistencyCheckEvent.Payload(ConsistencyCheckEvent.Payload.Type.SUSPECT, 1));
            throw new CsacConsistencyCheckException(e);
        }
    }
}
