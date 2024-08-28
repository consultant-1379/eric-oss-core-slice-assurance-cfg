/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.repository;

import java.util.List;

import com.ericsson.oss.air.csac.model.ProfileDefinition;
import com.ericsson.oss.air.csac.model.pmsc.KpiDefinitionDTO;
import com.ericsson.oss.air.csac.model.runtime.RuntimeKpiInstance;
import com.ericsson.oss.air.csac.model.runtime.metadata.KpiContextId;

/**
 * The interface Deployed kpi def dao. DeployedKpiDefDAO stores deployed pmsc KPI.
 */
public interface DeployedKpiDefDAO {

    /**
     * Create deployed kpi.
     *
     * @param kpiDefinitionDTO  the kpi definition dto
     * @param kpiDefName        the kpi def name
     * @param profileDefinition the profile definition
     */
    void createDeployedKpi(final KpiDefinitionDTO kpiDefinitionDTO, final String kpiDefName, final ProfileDefinition profileDefinition);

    /**
     * Create deployed kpi.
     *
     * @param kpiDefinitionDTO  the kpi definition dto
     * @param kpiDefName        the kpi_def name
     * @param aggregationFields the list of aggregation fields
     */
    void createDeployedKpi(final KpiDefinitionDTO kpiDefinitionDTO, final String kpiDefName, final List<String> aggregationFields);

    /**
     * Update deployed kpi.
     *
     * @param kpiDefinitionDTO the kpi definition dto
     */
    void updateDeployedKpi(final KpiDefinitionDTO kpiDefinitionDTO);

    /**
     * Delete deployed kpi.
     *
     * @param pmscKpiName the pmsc kpi name
     */
    void deleteDeployedKpi(final String pmscKpiName);

    /**
     * Gets deployed kpi.
     *
     * @param pmscKpiName the pmsc kpi name
     * @return the deployed kpi
     */
    KpiDefinitionDTO getDeployedKpi(final String pmscKpiName);

    /**
     * Gets deployed kpi by profile.
     *
     * @param profileDefinition the profile definition
     * @return the deployed kpi by profile
     */
    List<KpiDefinitionDTO> getDeployedKpiByProfile(final ProfileDefinition profileDefinition);

    /**
     * Gets deployed kpi by definition.
     *
     * @param kpiDefinitionName the kpi definition name
     * @return the deployed kpi by definition
     */
    List<KpiDefinitionDTO> getDeployedKpiByDefinitionName(final String kpiDefinitionName);

    /**
     * Gets deployed kpi by aggregation.
     *
     * @param kpiDefinitionName the kpi definition name
     * @param aggregationFields the aggregation fields
     * @return the deployed kpi by aggregation
     */
    KpiDefinitionDTO getDeployedKpiByAggregation(final String kpiDefinitionName, final List<String> aggregationFields);

    /**
     * Gets all deployed kpis.
     *
     * @return the all deployed kpis
     */
    List<KpiDefinitionDTO> getAllDeployedKpis();

    /**
     * Get the number of deployed KPI definitions
     *
     * @return the size of the deployed kpis map
     */
    int totalDeployedKpiDefinitions();

    /**
     * Returns an unbounded list of all runtime KPI instances as stored in the runtime data store;
     *
     * @return an unbounded list of all runtime KPI instances as stored in the runtime data store.
     */
    List<RuntimeKpiInstance> findAllRuntimeKpis();

    /**
     * Returns a paged list of runtime KPIs as stored in the runtime data store.
     *
     * @param start start page
     * @param rows  number of rows per page
     * @return a paged list of runtime KPIs as stored in the runtime data store.
     */
    List<RuntimeKpiInstance> findAllRuntimeKpis(Integer start, Integer rows);

    /**
     * Returns a list of runtime KPI instances.
     * If the 'visibleOnly' parameter is set to 'true', the results will be filtered based on the 'is_visible' property in the KPI definition
     *
     * @param visibleOnly isVisible or not
     * @return a list of runtime KPI instances
     */
    List<RuntimeKpiInstance> findAllRuntimeKpis(boolean visibleOnly);

    /**
     * This function returns a list of runtime KPI instances associated with the provided KPI context ID. If the 'visibleOnly' parameter is set to
     * 'true', the results will be filtered based on the 'is_visible' property in the KPI definition.
     *
     * @param contextId   the context id
     * @param visibleOnly isVisible or not
     * @return a list of runtime KPI instances.
     */
    List<RuntimeKpiInstance> findAllByContextId(KpiContextId contextId, boolean visibleOnly);

}
