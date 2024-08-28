/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.service.kpi.pmsc.legacy;

import com.ericsson.oss.air.csac.model.pmsc.KpiCalculationDTO;
import com.ericsson.oss.air.csac.model.pmsc.KpiDefinitionSubmission;
import com.ericsson.oss.air.util.codec.Codec;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

/**
 * Dry-run implementation of the PM Stats Calculator REST client service. This is effectively a loopback class that just logs the output that would
 * have been submitted to the PM Stats Calculator if the service was 'live'.
 */
@Service
@Profile({ "dry-run" })
@Slf4j
public class DryrunPmscClient implements PmscProvisioningService {

    @Autowired
    private Codec codec;

    /*
     * For unit testing only.
     */
    protected DryrunPmscClient setCodec(final Codec codec) {
        this.codec = codec;
        return this;
    }

    @SneakyThrows
    @Override
    public ResponseEntity<Void> updatePMSCKpisDefinitions(final KpiDefinitionSubmission kpiDefinitionSubmission) {
        log.info("Updating KPI definitions: {} ", this.codec.writeValueAsString(kpiDefinitionSubmission));
        return ResponseEntity.ok().build();
    }

    @SneakyThrows
    @Override
    public ResponseEntity<Void> createPMSCKpisCalculation(final KpiCalculationDTO kpiCalculationDTO) {

        log.info("Creating KPI calculation: {} ", this.codec.writeValueAsString(kpiCalculationDTO));
        return ResponseEntity.ok().build();
    }
}
