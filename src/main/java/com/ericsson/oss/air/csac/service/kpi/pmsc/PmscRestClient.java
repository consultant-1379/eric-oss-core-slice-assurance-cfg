/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.service.kpi.pmsc;

import java.util.List;

import com.ericsson.oss.air.csac.model.pmsc.KpiDefinitionDTO;
import com.ericsson.oss.air.csac.model.pmsc.KpiDefinitionSubmission;
import com.ericsson.oss.air.csac.model.pmsc.PmscKpiDefinitionDto;
import org.springframework.http.ResponseEntity;

/**
 * This interface provides the API for submitting REST requests to the PM Stats Calculator (PMSC).
 */
public interface PmscRestClient {

    /**
     * Submits a request to PMSC to create a set of new KPI definitions in the provided {@link KpiDefinitionSubmission}.
     *
     * @param kpiDefinitionSubmission
     * @return
     */
    ResponseEntity<Void> create(final KpiDefinitionSubmission kpiDefinitionSubmission);

    /**
     * Delete a list of kpi definitions in PMSC
     *
     * @param kpiDefinitionDtoList a list of runtime kpi definitions
     */
    void delete(final List<KpiDefinitionDTO> kpiDefinitionDtoList);

    /**
     * Submits a request to PMSC to delete a list of runtime kpi definitions
     *
     * @param ids a list of kpi names
     */
    void deleteById(final List<String> ids);

    /**
     * Delete all persisted csac runtime kpis in PMSC
     */
    void deleteAll();

    /**
     * Submits a request to PMSC to get all persisted kpi definitions with csac name convention
     *
     * @return a list of {@link PmscKpiDefinitionDto}
     */
    List<PmscKpiDefinitionDto> getAll();
}
