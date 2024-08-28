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
 * Runtime DAO API for {@link AugmentationDefinition}.
 */
public interface EffectiveAugmentationDAO {

    /**
     * Saves the effective augmentation definitions to the runtime data store. If the augmentation definition already exists, this method will update
     * the existing entry. Effective augmentation includes one or more affected profiles.
     *
     * @param augmentationDefinition
     *         effective augmentation definition to persist.
     * @param affectedProfiles
     *         list of profiles associated with this effective augmentation
     */
    void save(AugmentationDefinition augmentationDefinition, List<String> affectedProfiles);

    /**
     * Returns an unordered list of all effective augmentation definitions in the runtime data store. To retrieve associated profiles, use the
     * {@link EffectiveAugmentationDAO#findAllProfileNames(String)} findAllProfileNames} method for each effective profile.
     *
     * @return unordered list of effective augmentation definitions in the runtime data store.
     */
    List<AugmentationDefinition> findAll();

    /**
     * Returns the specified effective augmentation definition, or an empty Optional if definition does not exist. To retrieve associated profiles,
     * use the {@link EffectiveAugmentationDAO#findAllProfileNames(String)} findAllProfileNames} method.
     *
     * @param ardqId
     *         case-sensitive identifier for the augmentation definition.
     * @return optional containing the target augmentation definition, or an empty Optional if it does not exist.
     */
    Optional<AugmentationDefinition> findById(String ardqId);

    /**
     * Deletes the specified augmentation definition from the runtime data store.
     *
     * @param ardqId
     *         case-sensitive identifier for the augmentation definition.
     */
    void delete(String ardqId);

    /**
     * Returns the list of profiles associated with this effective augmentation.
     *
     * @param ardqId
     *         unique identifier for the target effective augmentation.
     * @return list of profiles associated with this effective augmentation
     */
    List<String> findAllProfileNames(String ardqId);

    /**
     * Returns the total number of effective augmentation definitions in the runtime data store.
     *
     * @return the total number of effective augmentation definitions in the runtime data store
     */
    int totalEffectiveAugmentations();

}
