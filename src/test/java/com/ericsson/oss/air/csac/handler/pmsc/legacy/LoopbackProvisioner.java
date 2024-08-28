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

import java.util.List;

import com.ericsson.oss.air.csac.model.ProfileDefinition;
import com.ericsson.oss.air.csac.model.pmsc.KpiDefinitionDTOWithRelationship;

import lombok.extern.slf4j.Slf4j;

/**
 * This provisioner will bypass the PMSC.
 */
@Slf4j
public class LoopbackProvisioner implements Provisioner {

    @Override
    public void provision(final List<KpiDefinitionDTOWithRelationship> kpiDefinitionDTOs, final List<ProfileDefinition> pendingProfiles) {
        log.info("Provisioning the PMSC has been disabled.");
    }
}
