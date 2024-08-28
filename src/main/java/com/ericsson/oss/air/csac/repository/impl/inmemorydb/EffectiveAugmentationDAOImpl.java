/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.repository.impl.inmemorydb;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

import com.ericsson.oss.air.csac.model.AugmentationDefinition;
import com.ericsson.oss.air.csac.repository.EffectiveAugmentationDAO;
import com.ericsson.oss.air.csac.repository.impl.EffectiveAugmentationDAOBase;
import lombok.NoArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

/**
 * In-memory implementation of the {@link EffectiveAugmentationDAO}.
 */
@Repository
@NoArgsConstructor
@Profile({ "dry-run" })
public class EffectiveAugmentationDAOImpl extends EffectiveAugmentationDAOBase implements Clearable {

    private final Map<String, List<String>> augmentationProfileMap = new HashMap<>();

    private final Map<String, AugmentationDefinition> effectiveAugmentationStore = new HashMap<>();

    @Override
    public void doSave(final AugmentationDefinition augmentationDefinition, final List<String> affectedProfiles) {

        final List<String> mergedProfiles = mergeAffectedProfiles(augmentationDefinition.getName(), affectedProfiles);
        this.augmentationProfileMap.put(augmentationDefinition.getName(), mergedProfiles);
        this.effectiveAugmentationStore.put(augmentationDefinition.getName(), augmentationDefinition);
    }

    @Override
    public List<AugmentationDefinition> findAll() {
        return new ArrayList<>(this.effectiveAugmentationStore.values());
    }

    @Override
    public Optional<AugmentationDefinition> findById(final String ardqId) {
        return Optional.ofNullable(this.effectiveAugmentationStore.get(ardqId));
    }

    @Override
    public void delete(final String ardqId) {

        this.augmentationProfileMap.remove(ardqId);
        this.effectiveAugmentationStore.remove(ardqId);
    }

    @Override
    public List<String> findAllProfileNames(final String ardqId) {
        return this.augmentationProfileMap.getOrDefault(ardqId, Collections.emptyList());
    }

    @Override
    public int totalEffectiveAugmentations() {
        return this.effectiveAugmentationStore.size();
    }

    /*
     * (non-javadoc)
     *
     * Returns a list of affected profile names containing the existing names as well as the provided names.
     */
    private List<String> mergeAffectedProfiles(final String ardqId, final List<String> affectedProfiles) {

        final Set<String> existingAffectedProfiles = new TreeSet<>();

        if (this.augmentationProfileMap.containsKey(ardqId)) {
            existingAffectedProfiles.addAll(this.augmentationProfileMap.get(ardqId));
        }

        existingAffectedProfiles.addAll(affectedProfiles);

        return new ArrayList<>(existingAffectedProfiles);
    }

    @Override
    public void clear() {
        this.augmentationProfileMap.clear();
        this.effectiveAugmentationStore.clear();
    }
}
