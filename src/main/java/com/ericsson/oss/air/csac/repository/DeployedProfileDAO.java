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
import java.util.Set;

import com.ericsson.oss.air.csac.model.KPIDefinition;
import com.ericsson.oss.air.csac.model.ProfileDefinition;

/**
 * Runtime DAO API for {@link com.ericsson.oss.air.csac.model.ProfileDefinition}.
 */
public interface DeployedProfileDAO {

    /**
     * Save the deployed profile definition
     *
     * @param profileDefinition profileDefinition object to be saved
     */
    void saveProfileDefinition(final ProfileDefinition profileDefinition);

    /**
     * Find ProfileDefinition Object by its unique name
     *
     * @param profileDefName Unique profile definition name
     * @return ProfileDefinition object
     */
    ProfileDefinition findByProfileDefName(final String profileDefName);

    /**
     * Find all ProfileDefinitions by start page and number of rows
     *
     * @param start current page
     * @param rows  number of rows requested
     * @return List of ProfileDefinitions
     */
    List<ProfileDefinition> findAllProfileDefinitions(final Integer start, final Integer rows);

    /**
     * Get total number of ProfileDefinitions
     *
     * @return size of the internal map profileDefMap
     */
    int totalProfileDefinitions();

    /**
     * Get All Profile Definitions
     *
     * @return Set of unique profile definitions based on their profile names
     */
    Set<ProfileDefinition> getProfileDefinitions();

    /**
     * Insert KPIDefinitions into Data Dictionary
     *
     * @param profileDefinitions list of profileDefinitions
     */
    void insertProfileDefinitions(final List<ProfileDefinition> profileDefinitions);

    /**
     * Check if a matching profile definition exists
     *
     * @param profileDefinition The profile definition to be matched
     * @return boolean : Returns True if all the fields match, Returns false otherwise
     */
    boolean isMatched(final ProfileDefinition profileDefinition);

    /**
     * Check if the given profile definition exists
     *
     * @param kpiDefinitions The list of KPIs that in turn affect the deployed profiles
     * @return Set of affected profile definitions
     */
    Set<ProfileDefinition> getAffectedProfiles(final List<KPIDefinition> kpiDefinitions);

}
