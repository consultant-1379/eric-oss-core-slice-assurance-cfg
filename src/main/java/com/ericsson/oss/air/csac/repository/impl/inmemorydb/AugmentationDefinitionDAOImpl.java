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

import static com.ericsson.oss.air.util.RestEndpointUtil.getSafeSublistIndex;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.ericsson.oss.air.csac.model.AugmentationDefinition;
import com.ericsson.oss.air.csac.repository.AugmentationDefinitionDAO;
import lombok.NoArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Repository;

/**
 * {@inheritDoc}
 * <p>
 * In-memory implementation of the {@link AugmentationDefinitionDAO}.
 */
@Repository
@NoArgsConstructor
@Profile({ "dry-run" })
public class AugmentationDefinitionDAOImpl implements AugmentationDefinitionDAO, Clearable {

    private final Map<String, AugmentationDefinition> inMemoryStore = new HashMap<>();

    @Override
    public void save(final AugmentationDefinition augmentationDefinition) {
        this.inMemoryStore.put(augmentationDefinition.getName(), augmentationDefinition);
    }

    @Override
    public void saveAll(final List<AugmentationDefinition> augmentationDefinitions) {
        this.inMemoryStore.putAll(augmentationDefinitions.stream()
                .collect(Collectors.toMap(AugmentationDefinition::getName, Function.identity())));
    }

    @Override
    public List<AugmentationDefinition> findAll() {
        return new ArrayList<>(this.inMemoryStore.values());
    }

    @Override
    public List<AugmentationDefinition> findAll(final Integer start, final Integer rows) {

        if (rows <= 0 || this.inMemoryStore.isEmpty()) {
            return Collections.emptyList();
        }

        final Pair<Integer, Integer> safeIndex = getSafeSublistIndex(this.inMemoryStore.size(), start, rows);

        return new ArrayList<>(this.inMemoryStore.values())
                .subList(safeIndex.getFirst(), safeIndex.getSecond());
    }

    @Override
    public Optional<AugmentationDefinition> findById(final String ardqId) {
        return Optional.ofNullable(this.inMemoryStore.get(ardqId));
    }

    @Override
    public void delete(final String ardqId) {
        this.inMemoryStore.remove(ardqId);
    }

    @Override
    public int totalAugmentationDefinitions() {
        return this.inMemoryStore.size();
    }

    @Override
    public void clear() {
        this.inMemoryStore.clear();
    }
}
