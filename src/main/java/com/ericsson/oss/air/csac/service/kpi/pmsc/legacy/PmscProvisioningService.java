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
import com.ericsson.oss.air.csac.service.ProvisioningService;
import lombok.SneakyThrows;
import org.springframework.http.ResponseEntity;

/**
 * Base type for PM Stats Calculator provisioning services
 */
public interface PmscProvisioningService extends ProvisioningService {

    @SneakyThrows
    ResponseEntity<Void> updatePMSCKpisDefinitions(KpiDefinitionSubmission kpiDefinitionSubmission);

    @SneakyThrows
    ResponseEntity<Void> createPMSCKpisCalculation(KpiCalculationDTO kpiCalculationDTO);
}
