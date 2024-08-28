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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.ericsson.oss.air.csac.model.ProfileDefinition;
import com.ericsson.oss.air.csac.repository.ProfileDefinitionDAO;
import lombok.NoArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

/**
 * In-memory implementation of the {@link ProfileDefinitionDAO}.
 */
@Repository
@NoArgsConstructor
@Profile({ "dry-run" })
public class ProfileDefinitionDAOImpl implements ProfileDefinitionDAO, Clearable {

    private final Map<String, ProfileDefinition> profileDefMap = new HashMap<>();

    @Override
    public void save(ProfileDefinition profileDefinition) {
        this.profileDefMap.put(profileDefinition.getName(), profileDefinition);
    }

    @Override
    public void saveAll(List<ProfileDefinition> profileDefinitions) {
        this.profileDefMap.putAll(profileDefinitions.stream()
                .collect(Collectors.toMap(ProfileDefinition::getName, Function.identity())));
    }

    @Override
    public Optional<ProfileDefinition> findById(String profileId) {
        return Optional.ofNullable(this.profileDefMap.get(profileId));
    }

    @Override
    public List<ProfileDefinition> findAll() {
        return new ArrayList<>(this.profileDefMap.values());
    }

    @Override
    public int totalProfileDefinitions() {
        return this.profileDefMap.size();
    }

    @Override
    public void clear() {
        this.profileDefMap.clear();
    }
}
