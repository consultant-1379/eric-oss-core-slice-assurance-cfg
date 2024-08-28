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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.ericsson.oss.air.csac.model.KPIDefinition;
import com.ericsson.oss.air.csac.model.KPIReference;
import com.ericsson.oss.air.csac.model.ProfileDefinition;
import com.ericsson.oss.air.csac.repository.DeployedProfileDAO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Repository;
import org.springframework.util.ObjectUtils;

@Repository
@Getter
@NoArgsConstructor
@Profile({ "dry-run" })
public class DeployedProfileDAOImpl implements DeployedProfileDAO, Clearable {

    private final Map<String, ProfileDefinition> profileDefMap = new HashMap<>();

    protected static List<ProfileDefinition> getAffectedProfilesImpl(final KPIDefinition kpiDef,
                                                                     final ArrayList<ProfileDefinition> profileDefinitions) {
        final List<ProfileDefinition> affectedProDef = new ArrayList<>();
        profileDefinitions.forEach(proDef -> {
            for (final KPIReference kpi : proDef.getKpis()) {
                if (kpi.getRef().equals(kpiDef.getName())) {
                    affectedProDef.add(proDef);
                    break;
                }
            }

        });
        return affectedProDef;
    }

    @Override
    public void saveProfileDefinition(final ProfileDefinition profileDefinition) {
        this.profileDefMap.put(profileDefinition.getName(), profileDefinition);
    }

    @Override
    public ProfileDefinition findByProfileDefName(final String profileDefName) {
        return this.profileDefMap.get(profileDefName);
    }

    @Override
    public List<ProfileDefinition> findAllProfileDefinitions(final Integer start, final Integer rows) {

        // return empty list if rows equals to zero or negative
        // return empty list if the DAO object is empty
        if (rows <= 0 || this.profileDefMap.size() == 0) {
            return new ArrayList<>();
        }

        final List<ProfileDefinition> allProfileDefs = new ArrayList<>(this.profileDefMap.values());

        final Pair<Integer, Integer> safeIndex = getSafeSublistIndex(allProfileDefs.size(), start, rows);
        return allProfileDefs.subList(safeIndex.getFirst(), safeIndex.getSecond());
    }

    @Override
    public Set<ProfileDefinition> getProfileDefinitions() {
        return new HashSet<>(this.profileDefMap.values());
    }

    @Override
    public void insertProfileDefinitions(final List<ProfileDefinition> profileDefinitions) {
        profileDefinitions.forEach(this::saveProfileDefinition);
    }

    @Override
    public boolean isMatched(final ProfileDefinition profDef) {
        final ProfileDefinition byProfileDefName = this.findByProfileDefName(profDef.getName());
        return !ObjectUtils.isEmpty(byProfileDefName) && byProfileDefName.equals(profDef);
    }

    @Override
    public Set<ProfileDefinition> getAffectedProfiles(final List<KPIDefinition> kpiDefinitions) {
        final Set<ProfileDefinition> affectedProfiles = new HashSet<>();

        kpiDefinitions.forEach(kpiDef -> {
            final List<ProfileDefinition> affectedProDefList = DeployedProfileDAOImpl.getAffectedProfilesImpl(kpiDef,
                    new ArrayList<>(this.profileDefMap.values()));
            affectedProfiles.addAll(affectedProDefList);
        });
        return affectedProfiles;
    }

    @Override
    public int totalProfileDefinitions() {
        return this.profileDefMap.size();
    }

    protected void clearInternalMaps() {
        this.profileDefMap.clear();
    }

    @Override
    public void clear() {
        this.profileDefMap.clear();
    }
}
