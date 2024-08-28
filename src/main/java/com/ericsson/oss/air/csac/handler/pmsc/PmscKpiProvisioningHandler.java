/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.handler.pmsc;

import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.ericsson.oss.air.csac.handler.kpi.KpiProvisioningHandler;
import com.ericsson.oss.air.csac.handler.pmsc.legacy.PMSCHandler;
import com.ericsson.oss.air.csac.model.ProfileDefinition;

import lombok.RequiredArgsConstructor;

/**
 * Provisioning handler for PMSCH.  This class passes KPI provisioning notifications to the legacy {@link PMSCHandler}.  KPIs are calculated and
 * provisioned using the legacy PMSCH provisioning services.
 */
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(value = "provisioning.pmsc.enabled",
        havingValue = "true")
public class PmscKpiProvisioningHandler extends KpiProvisioningHandler {

    private final PMSCHandler pmscHandler;

    @Override
    protected void doApply(final List<ProfileDefinition> profileDefinitions) {
        this.pmscHandler.submit(profileDefinitions);
    }
}
