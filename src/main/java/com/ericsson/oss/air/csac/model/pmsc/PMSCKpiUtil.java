/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.model.pmsc;

import java.util.List;
import java.util.stream.Collectors;

/**
 * An Util class to create KpiDefinitionSubmission and KpiCalculationDTO objects from list of KpiDefinitionDTO objects
 */
public class PMSCKpiUtil {

    /**
     * For PMSC KPI definitions and KPI calculation source values, they were defined as below
     */
    public static final String CSAC_KPI_DEF_SOURCE = "CSAC_KPI_DEF_SOURCE";
    protected static final String CSAC_KPI_CAL_SOURCE = "CSAC_KPI_CAL_SOURCE";
    protected static final String CSAC_PARAMETER_EXECUTION_ID = CSAC_KPI_CAL_SOURCE + "_" + 1;

    private PMSCKpiUtil() {
    }

    /**
     * Create a KpiCalculationDTO object from list of KpiDefinitionDTO objects
     *
     * @param kpis list of @{@link KpiDefinitionDTO} KpiDefinitionDTO objects
     * @return @{@link KpiCalculationDTO} object
     */
    public static KpiCalculationDTO createKpiCalculation(final List<KpiDefinitionDTO> kpis) {
        final List<String> kpiNames = kpis.stream().map(KpiDefinitionDTO::getName).collect(Collectors.toList());
        return createKpiCalculationByNames(kpiNames);
    }

    /**
     * Create a KpiCalculationDTO object from list of KpiDefinitionDTO names
     *
     * @param kpiNames list of KpiDefinition names
     * @return @{@link KpiCalculationDTO} object
     */
    public static KpiCalculationDTO createKpiCalculationByNames(final List<String> kpiNames) {
        final ParameterDTO parameter = new ParameterDTO(CSAC_PARAMETER_EXECUTION_ID, java.time.LocalDate.now().toString());
        return new KpiCalculationDTO(CSAC_KPI_CAL_SOURCE, kpiNames, parameter);
    }
}
