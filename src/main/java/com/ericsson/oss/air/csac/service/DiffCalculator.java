/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.ericsson.oss.air.csac.configuration.augmentation.AugmentationConfiguration;
import com.ericsson.oss.air.csac.model.AugmentationDefinition;
import com.ericsson.oss.air.csac.model.KPIDefinition;
import com.ericsson.oss.air.csac.model.KPIReference;
import com.ericsson.oss.air.csac.model.PMDefinition;
import com.ericsson.oss.air.csac.model.PMSchemaDefinition;
import com.ericsson.oss.air.csac.model.ProfileDefinition;
import com.ericsson.oss.air.csac.model.ResourceSubmission;
import com.ericsson.oss.air.csac.repository.AugmentationDefinitionDAO;
import com.ericsson.oss.air.csac.repository.DeployedProfileDAO;
import com.ericsson.oss.air.csac.repository.EffectiveAugmentationDAO;
import com.ericsson.oss.air.csac.repository.KPIDefinitionDAO;
import com.ericsson.oss.air.csac.repository.PMDefinitionDAO;
import com.ericsson.oss.air.csac.repository.PMSchemaDefinitionDao;
import com.ericsson.oss.air.csac.repository.ProfileDefinitionDAO;
import com.ericsson.oss.air.csac.repository.impl.inmemorydb.KPIDefinitionDAOImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

@Component
@Slf4j
@RequiredArgsConstructor
public class DiffCalculator {

    private final DeployedProfileDAO deployedProfileDAO;

    private final ProfileDefinitionDAO profileDefinitionDAO;

    private final KPIDefinitionDAO kpiDefinitionDAO;

    private final PMDefinitionDAO pmDefinitionDAO;

    private final AugmentationDefinitionDAO augmentationDefinitionDAO;

    private final EffectiveAugmentationDAO effectiveAugmentationDAO;

    private final AugmentationConfiguration augmentationConfiguration;

    private final PMSchemaDefinitionDao pmSchemaDefinitionDao;

    /**
     * return true if any object in resourceSubmission is not matched with existing resources
     *
     * @param resourceSubmission a @{@link ResourceSubmission} object
     * @return true if any object in resourceSubmission is not matched with existing resources
     */
    public boolean isChanged(final ResourceSubmission resourceSubmission) {

        //check if kpi defs changed
        final boolean isKpiDefsChanged = Objects.requireNonNullElse(resourceSubmission.getKpiDefs(), new ArrayList<KPIDefinition>()).stream()
                .anyMatch(kpiDefinition -> !this.kpiDefinitionDAO.isMatched(kpiDefinition));

        if (isKpiDefsChanged) {
            return true;
        }

        //check if pm defs changed
        final boolean isPmDefsChanged = Objects.requireNonNullElse(resourceSubmission.getPmDefs(), new ArrayList<PMDefinition>()).stream()
                .anyMatch(def -> !this.pmDefinitionDAO.isMatched(def));

        if (isPmDefsChanged) {
            return true;
        }

        //check if PM Schema definitions changed
        final boolean isPmSchemaDefsChanged = Objects.requireNonNullElse(resourceSubmission.getPmSchemaDefs(), new ArrayList<PMSchemaDefinition>())
                .stream()
                .anyMatch(def -> !this.pmSchemaDefinitionDao.isMatched(def));

        if (isPmSchemaDefsChanged) {
            return true;
        }

        //check if augmentation defs changed
        final Set<AugmentationDefinition> changedAugDefs = this.getChangedAugmentationDefs(
                resourceSubmission.getAugmentationDefinitions());
        final boolean isAugmentationDefsChanged = this.isNotEmpty(changedAugDefs);

        if (isAugmentationDefsChanged) {
            return true;
        }

        //check if profile defs changed
        final Set<ProfileDefinition> changedProfileDefs = this.getChangedProfDefs(resourceSubmission.getProfileDefs());

        return this.isNotEmpty(changedProfileDefs);

    }

    /*
     * (non-javadoc)
     *
     * Return a Set of changed PM definitions
     */
    private Set<PMDefinition> getChangedPMDef(final List<PMDefinition> pmDefs) {
        final Set<PMDefinition> changedPMDefs = new HashSet<>();

        if (ObjectUtils.isEmpty(pmDefs)) {
            return changedPMDefs;
        }

        pmDefs.forEach(pmDef -> {
            if (!this.pmDefinitionDAO.isMatched(pmDef)) {
                changedPMDefs.add(pmDef);
            }
        });

        return changedPMDefs;
    }

    /*
     * (non-javadoc)
     *
     * Return a Set of changed KPI definitions
     */
    private Set<KPIDefinition> getChangedKPIDef(final List<KPIDefinition> kpiDefs) {
        final Set<KPIDefinition> changedKPIDefs = new HashSet<>();

        if (ObjectUtils.isEmpty(kpiDefs)) {
            return changedKPIDefs;
        }

        kpiDefs.forEach(pmDef -> {
            if (!this.kpiDefinitionDAO.isMatched(pmDef)) {
                changedKPIDefs.add(pmDef);
            }
        });

        return changedKPIDefs;
    }

    /*
     * (non-javadoc)
     *
     * Return a set of changed augmentation definitions.
     */
    private Set<AugmentationDefinition> getChangedAugmentationDefs(final List<AugmentationDefinition> rsAugDefList) {
        final Set<AugmentationDefinition> changedAugmentationDefs = new HashSet<>();

        if (ObjectUtils.isEmpty(rsAugDefList)) {
            return changedAugmentationDefs;
        }

        final List<AugmentationDefinition> dictionaryAugmentationDefList = this.augmentationDefinitionDAO.findAll();
        final List<AugmentationDefinition> deployedAugmentationDefList = this.effectiveAugmentationDAO.findAll();

        final Map<String, AugmentationDefinition> dictionaryAugmentationDefMap = dictionaryAugmentationDefList.
                stream().collect(Collectors.toMap(AugmentationDefinition::getName, Function.identity()));

        final Map<String, AugmentationDefinition> deployedAugmentationDefMap = deployedAugmentationDefList.
                stream().collect(Collectors.toMap(AugmentationDefinition::getName, Function.identity()));

        for (final AugmentationDefinition rsAugDef : rsAugDefList) {

            final String AugId = rsAugDef.getName();
            final AugmentationDefinition dictionaryAugDef = dictionaryAugmentationDefMap.get(AugId);
            final AugmentationDefinition deployedAugDef = deployedAugmentationDefMap.get(AugId);

            // If augmentation definition does not exist in dictionary or if it does not equal to the definition in dictionary, then add to changed augmentations list and check next definition in input list.
            if (Objects.isNull(dictionaryAugDef) || !Objects.equals(rsAugDef, dictionaryAugDef)) {
                changedAugmentationDefs.add(rsAugDef);
                continue;
            }

            /*
             * Deployed/effective augmentations have resolved URLs. The URL must be resolved in a cloned input aug
             * definition (clonedRsAugDef) before checking for equality.
             *
             * If the input aug definitions are directly modified so that their URLs are resolved, then the second
             * execution of this method with the same input object may produce a different result. If the input aug
             * definitions originally had unresolved URLs, then their corresponding dictionary aug definitions will
             * have unresolved URLs. Thus, when this method is executed the second time, the input aug definitions
             * with their newly resolved URLs will not equal their dictionary aug definitions.
             *
             * In other words, cloning the input aug def (rsAugDef) and changing the URL for the clone rather than the
             * original object makes this method idempotent. This fixed ESOA-7308.
             */
            final AugmentationDefinition clonedRsAugDef = rsAugDef.withUrl(this.augmentationConfiguration.getResolvedUrl(rsAugDef.getUrl()));

            // This check is needed for restart scenario when previous AAS provisioning fails.
            // If augmentation definition does not exist in runtime store or if it does not equal to the definition in runtime store, then add to changed augmentations list and check next definition in input list.
            if (Objects.isNull(deployedAugDef) || !Objects.equals(clonedRsAugDef, deployedAugDef)) {
                changedAugmentationDefs.add(clonedRsAugDef);
            }
        }

        return changedAugmentationDefs;
    }

    /*
     * (non-javadoc)
     *
     * Return a set of changed profile definitions.
     */
    private Set<ProfileDefinition> getChangedProfDefs(final List<ProfileDefinition> rsProfileDefList) {

        final Set<ProfileDefinition> changedProfileDefList = new HashSet<>();

        if (ObjectUtils.isEmpty(rsProfileDefList)) {
            return changedProfileDefList;
        }

        final Set<ProfileDefinition> deployedProfileDefSet = this.deployedProfileDAO.getProfileDefinitions();
        final Map<String, ProfileDefinition> deployedProfileDefMap = deployedProfileDefSet.
                stream().collect(Collectors.toMap(ProfileDefinition::getName, Function.identity()));

        final List<ProfileDefinition> dictionaryProfileDefList = this.profileDefinitionDAO.findAll();
        final Map<String, ProfileDefinition> dictionaryProfileDefMap = dictionaryProfileDefList.
                stream().collect(Collectors.toMap(ProfileDefinition::getName, Function.identity()));

        for (final ProfileDefinition rsProfileDef : rsProfileDefList) {

            final String rsProfileDefName = rsProfileDef.getName();
            final ProfileDefinition deployedProfileDef = deployedProfileDefMap.get(rsProfileDefName);

            if (Objects.isNull(deployedProfileDef) || !Objects.equals(rsProfileDef, dictionaryProfileDefMap.get(rsProfileDefName))) {

                changedProfileDefList.add(rsProfileDef);
            }
        }

        return changedProfileDefList;
    }

    /**
     * Return a list of ProfileDefintion with given ResourceSubmission
     *
     * @param rs A @{@link ResourceSubmission}
     * @return a list of affected @{@link ProfileDefinition}
     */
    public Set<ProfileDefinition> getAffectedProfiles(final ResourceSubmission rs) {
        final Set<PMDefinition> changedPMDef = this.getChangedPMDef(rs.getPmDefs());
        final Set<AugmentationDefinition> changedAugmentationDefs = this.getChangedAugmentationDefs(
                rs.getAugmentationDefinitions());

        // PM Def ->> Affected KPI Def
        final Set<KPIDefinition> affectedKPIDefs = this.kpiDefinitionDAO.getAffectedKPIDefs(changedPMDef);
        final Set<KPIDefinition> currentAffectedKPIDefs = KPIDefinitionDAOImpl.getAffectedKPIDefs(changedPMDef, rs.getKpiDefs());

        affectedKPIDefs.addAll(currentAffectedKPIDefs);
        affectedKPIDefs.addAll(this.getChangedKPIDef(rs.getKpiDefs()));

        // KPI Defs ->> Affected Profile Def
        final Set<ProfileDefinition> affectedProfiles = this.deployedProfileDAO.getAffectedProfiles(new ArrayList<>(affectedKPIDefs));
        affectedProfiles.addAll(this.getChangedProfDefs(rs.getProfileDefs()));

        // Aug Defs ->> Affected Profile Defs
        final Set<ProfileDefinition> profilesAffectedByAugmentations = this.getProfileDefsAffectedByAllAugmentations(changedAugmentationDefs, rs);
        affectedProfiles.addAll(profilesAffectedByAugmentations);

        affectedProfiles.forEach(prof -> {
            final String kpiNames = prof.getKpis().stream().map(KPIReference::getRef).collect(Collectors.joining(", "));
            log.info("Affected profile: '{}'. KPIs: {} \n", prof.getName(), kpiNames);
        });

        return affectedProfiles;
    }

    /*
     * (non-javadoc)
     *
     * Gets profiles associated with a given augmentation.
     */
    private Set<ProfileDefinition> getProfileDefsAffectedByAllAugmentations(final Set<AugmentationDefinition> augmentationDefinitionSet,
                                                                            final ResourceSubmission rs) {

        return augmentationDefinitionSet.stream()
                .map(augDef -> this.getProfilesAffectedByAnAugmentation(augDef.getName(), rs.getProfileDefs()))
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());

    }

    /*
     * (non-javadoc)
     *
     * Returns set of profile names associated to an augmentation.
     */
    private Set<ProfileDefinition> getProfilesAffectedByAnAugmentation(final String augName, final List<ProfileDefinition> profileDefList) {
        final Set<ProfileDefinition> affectedProfileList = new HashSet<>();

        for (final ProfileDefinition profile : profileDefList) {

            if (augName.equals(profile.getAugmentation())) {
                affectedProfileList.add(profile);
            }
        }

        return affectedProfileList;
    }

    /*
     * (non-javadoc)
     *
     * Returns true if list is not empty else return false.
     */
    private <T> boolean isNotEmpty(final Set<T> changedDefinitions) {
        return !changedDefinitions.isEmpty();
    }

}
