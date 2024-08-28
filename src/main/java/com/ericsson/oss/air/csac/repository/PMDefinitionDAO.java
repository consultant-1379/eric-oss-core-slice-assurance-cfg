/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.repository;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.ericsson.oss.air.csac.model.PMDefinition;
import org.springframework.util.ObjectUtils;

public interface PMDefinitionDAO {

    /**
     * Save PMDefinition with schemaName
     *
     * @param pmDefinition PMDefinition object to be saved
     * @param schemaName   schema name fetched
     */
    void savePMDefinition(final PMDefinition pmDefinition, final String schemaName);

    /**
     * Update PMDefinition
     *
     * @param pmDefinition PMDefinition object
     */
    void updatePMDefinition(final PMDefinition pmDefinition);

    /**
     * Insert validated PMDefinitions into Data Dictionary
     *
     * @param validatedPMDefinitions map of validated PMDefinitions with schema name
     */
    void insertPMDefinitions(final Map<String, List<PMDefinition>> validatedPMDefinitions);

    /**
     * Find schema name from a pm name
     *
     * @param pmDefName PMDefinition name
     * @return schema name
     */
    String findSchemaByPMDefName(final String pmDefName);

    /**
     * Find PMDefinition Object by a PMDefiniton name
     *
     * @param pmdefName PMDefinition name
     * @return PMDefinition object
     */
    PMDefinition findByPMDefName(final String pmdefName);

    /**
     * Find all PMDefinitions by start page and number of rows
     *
     * @param start current page
     * @param rows  number of rows requested
     * @return List of PMDefinitions
     */
    List<PMDefinition> findAllPMDefinitions(final Integer start, final Integer rows);

    /**
     * Get all PM definition names
     *
     * @return a set of pm definition names
     */
    Set<String> getAllPmDefNames();

    /**
     * Get total number of PMDefinitions
     *
     * @return size of the internal map pmDefMapByPMName
     */
    Integer totalPMDefinitions();

    /**
     * Check if a matching PM definition exists
     *
     * @param pmDef The pm definition to be matched
     * @return boolean : Returns True if all the fields match, Returns false otherwise
     */
    default boolean isMatched(PMDefinition pmDef) {
        final PMDefinition byPMDefName = this.findByPMDefName(pmDef.getName());
        return !ObjectUtils.isEmpty(byPMDefName) && byPMDefName.equals(pmDef);
    }

}
