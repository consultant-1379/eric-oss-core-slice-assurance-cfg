/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.handler.kpi;

import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;

import com.ericsson.oss.air.csac.model.ProfileDefinition;

import lombok.extern.slf4j.Slf4j;

/**
 * Loopback provisioning handler.  This handler is invoked if no analytics target is enabled.  It does nothing but emit a log message indicating
 * that KPI provisiong is disabled.
 */
@Component
@Slf4j
@ConditionalOnExpression("${provisioning.pmsc.enabled:false} == false and ${provisioning.vm.enabled:false} == false")
public class LoopbackKpiProvisioningHandler extends KpiProvisioningHandler {

    @Override
    protected void doApply(final List<ProfileDefinition> profileDefinitions) {
        log.info("KPI provisioning disabled");
    }
}
