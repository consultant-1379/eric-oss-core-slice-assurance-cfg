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
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ericsson.oss.air.csac.model.ProfileDefinition;
import com.ericsson.oss.air.csac.model.pmsc.KpiDefinitionDTO;
import com.ericsson.oss.air.csac.model.pmsc.KpiDefinitionDTOWithRelationship;
import com.ericsson.oss.air.csac.model.pmsc.KpiTypeEnum;
import com.ericsson.oss.air.csac.service.KPICalculator;
import com.ericsson.oss.air.util.codec.Codec;
import com.fasterxml.jackson.core.JsonProcessingException;

import lombok.extern.slf4j.Slf4j;

/**
 * The Class to handler PMSC Related request
 */
@Component
@Slf4j
public class PMSCHandler {

    private final KPICalculator kpiCalculator;

    private final Provisioner provisioner;

    private final Codec codec;

    @Autowired
    public PMSCHandler(final KPICalculator kpiCalculator, final Provisioner provisioner, final Codec codec) {
        this.kpiCalculator = kpiCalculator;
        this.provisioner = provisioner;
        this.codec = codec;
    }

    /**
     * Trigger the submission of pending profiles
     *
     * @param pendingProfiles the pending profiles
     */
    public final void submit(final List<ProfileDefinition> pendingProfiles) {

        // no work to do
        if (pendingProfiles.isEmpty()) {
            log.info("No KPI changes found.  Skipping update.");
            return;
        }

        final List<KpiDefinitionDTOWithRelationship> kpiDefinitionDTOs = this.kpiCalculator.calculateAffectedKPIs(pendingProfiles);

        try {
            final String kpisInJson = this.codec.writeValueAsStringPretty(kpiDefinitionDTOs);
            log.info("Calculated KPIs: {}", kpisInJson);
        } catch (final JsonProcessingException e) {
            log.error("Unable to serialize to JSON \n", e);
        }

        log.info("Number of new and updated simple KPIs: {}",
                 kpiDefinitionDTOs.stream().filter(kpi -> kpi.getKpiType().equals(KpiTypeEnum.SIMPLE)).count());
        final String updatedSimpleKpiNames = kpiDefinitionDTOs.stream()
                .filter(kpi -> kpi.getKpiType().equals(KpiTypeEnum.SIMPLE))
                .map(KpiDefinitionDTO::getName)
                .collect(Collectors.joining(","));
        log.debug("Submitting affected simple KPIs: {} \n", updatedSimpleKpiNames);

        log.info("Number of new and updated complex KPIs: {}",
                 kpiDefinitionDTOs.stream().filter(kpi -> kpi.getKpiType().equals(KpiTypeEnum.COMPLEX)).count());
        final String updatedComplexKpiNames = kpiDefinitionDTOs.stream()
                .filter(kpi -> kpi.getKpiType().equals(KpiTypeEnum.COMPLEX))
                .map(KpiDefinitionDTO::getName)
                .collect(Collectors.joining(","));
        log.debug("Submitting affected complex KPIs: {} \n", updatedComplexKpiNames);

        this.provisioner.provision(kpiDefinitionDTOs, pendingProfiles);
    }
}
