/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.repository;

import java.util.List;
import java.util.Set;

import com.ericsson.oss.air.csac.model.KPIDefinition;
import com.ericsson.oss.air.csac.model.PMDefinition;
import org.springframework.util.ObjectUtils;

public interface KPIDefinitionDAO {

    /**
     * Save KPIDefinition.
     *
     * @param kpiDefinition
     *         the kpi definition
     */
    void saveKPIDefinition(final KPIDefinition kpiDefinition);

    /**
     * Find KPIDefinition by KPIDefinition name
     *
     * @param kpiDefName
     *         KPIDefinition name
     * @return KPIDefinition object
     */
    KPIDefinition findByKPIDefName(final String kpiDefName);

    /**
     * Get all KPI definition names
     *
     * @return a set of kpi definition names
     */
    Set<String> getAllKpiDefNames();

    /**
     * Get total number of KPIDefinitions from the internal map kpiDefMap
     *
     * @return size of the internal map kpiDefMap
     */
    Integer totalKPIDefinitions();

    /**
     * Insert validated KPIDefinitions into Data Dictionary
     *
     * @param validatedKPIDefinitions
     *         list of validated KPIDefinitions with schema name
     */
    void insertKPIDefinitions(final List<KPIDefinition> validatedKPIDefinitions);

    /**
     * Check if a matching KPI definition exists
     *
     * @param kpiDef
     *         The kpi definition to be matched
     * @return boolean : Returns True if all the fields match, Returns false otherwise
     */
    default boolean isMatched(final KPIDefinition kpiDef) {
        final KPIDefinition byKPIDefName = this.findByKPIDefName(kpiDef.getName());

        return !ObjectUtils.isEmpty(byKPIDefName) && byKPIDefName.equals(kpiDef);
    }

    /**
     * A method returns a set of affected @{@link KPIDefinition} by given a set of  @{@link PMDefinition}
     *
     * @param pmDefs
     *         a set of pmDefs
     * @return a set of @{@link KPIDefinition} that affected by given  @{@link PMDefinition}
     */
    Set<KPIDefinition> getAffectedKPIDefs(final Set<PMDefinition> pmDefs);

    /**
     * Find all KPI definitions based on the requested start page and number of rows per page
     *
     * @param start
     *         the start page location
     * @param rows
     *         the number of rows per page
     * @return a list of KPI definitions as requested
     */
    List<KPIDefinition> findAllKPIDefs(final Integer start, final Integer rows);

    /**
     * Returns all KPI definitions.
     *
     * @return all KPI definitions
     */
    List<KPIDefinition> findAll();
}
