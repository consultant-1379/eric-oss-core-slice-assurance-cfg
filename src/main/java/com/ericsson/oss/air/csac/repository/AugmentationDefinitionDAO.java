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

import com.ericsson.oss.air.csac.model.AugmentationDefinition;

/**
 * Data dictionary DAO API for {@link com.ericsson.oss.air.csac.model.AugmentationDefinition}.
 */
public interface AugmentationDefinitionDAO {

    /**
     * Saves the augmentation definition to the data dictionary. If the augmentation definition already exists, this method will update the existing
     * entry in the data dictionary.
     *
     * @param augmentationDefinition augmentation definition to persist.
     */
    void save(AugmentationDefinition augmentationDefinition);

    /**
     * Returns an unordered list of all augmentation definitions in the data dictionary.
     *
     * @return unordered of augmentation definitions in the data dictionary.
     */
    List<AugmentationDefinition> findAll();

    /**
     * Returns a paged list of all augmentation definitions in the data dictionary, ordered by augmentation Id.
     *
     * @param start start page for this query
     * @param rows  number of rows per page.  A value of -1 will return all rows starting at page 0.
     * @return a paged list of all augmentation definitions in the data dictionary, ordered by augmentation Id
     */
    List<AugmentationDefinition> findAll(Integer start, Integer rows);

    /**
     * Returns the specified augmentation definition, or an empty Optional if definition does not exist.
     *
     * @param ardqId case-sensitive identifier for the augmentation definition.
     * @return optional containing the target augmentation definition, or an empty Optional if it does not exist.
     */
    Optional<AugmentationDefinition> findById(String ardqId);

    /**
     * Deletes the specified augmentation definition from the data dictionary.
     *
     * @param ardqId case-sensitive identifier for the augmentation definition.
     */
    void delete(String ardqId);

    /**
     * Saves all the specified augmentation definitions to the data dictionary.  If any definitions already exist, they will be updated in the
     * dictionary.
     *
     * @param augmentationDefinitions list of augmentation definitions to save.
     */
    void saveAll(List<AugmentationDefinition> augmentationDefinitions);

    /**
     * Returns the total number of augmentation definitions in the data dictionary.
     *
     * @return the total number of augmentation definitions in the data dictionary.
     */
    int totalAugmentationDefinitions();
}
