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
import java.util.Optional;

import com.ericsson.oss.air.csac.model.ProfileDefinition;

/**
 * Data dictionary DAO API for {@link com.ericsson.oss.air.csac.model.ProfileDefinition}.
 */
public interface ProfileDefinitionDAO {

    /**
     * Saves the profile definition to the data dictionary. If the profile definition already exists, this method will update the existing
     * entry in the data dictionary.
     *
     * @param profileDefinition profile definition to persist.
     */
    void save(ProfileDefinition profileDefinition);

    /**
     * Saves all the specified profile definitions to the data dictionary.  If any definitions already exist, they will be updated in the
     * dictionary.
     *
     * @param profileDefinitions list of augmentation definitions to save.
     */
    void saveAll(List<ProfileDefinition> profileDefinitions);

    /**
     * Returns the specified profile definition, or an empty Optional if definition does not exist.
     *
     * @param profileId case-sensitive identifier for the profile definition.
     * @return optional containing the target profile definition, or an empty Optional if it does not exist.
     */
    Optional<ProfileDefinition> findById(String profileId);

    /**
     * Returns an unordered list of all profile definitions in the data dictionary.
     *
     * @return unordered list of profile definitions in the data dictionary.
     */
    List<ProfileDefinition> findAll();

    /**
     * Returns the total number of profile definitions in the data dictionary.
     *
     * @return the total number of profile definitions in the data dictionary
     */
    int totalProfileDefinitions();

}
