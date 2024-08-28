/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.model;

import static com.ericsson.oss.air.csac.model.ResourceSubmission.AUGMENTATIONS;
import static com.ericsson.oss.air.csac.model.ResourceSubmission.KPI_DEFS;
import static com.ericsson.oss.air.csac.model.ResourceSubmission.PM_DEFS;
import static com.ericsson.oss.air.csac.model.ResourceSubmission.PM_SCHEMAS;
import static com.ericsson.oss.air.csac.model.ResourceSubmission.PROFILE_DEFS;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import com.ericsson.oss.air.exception.CsacValidationException;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.util.CollectionUtils;

/**
 * Bean representing a loaded resource submission.  The resource submission contains all the resources, e.g. KPI definitions and profile definitions
 * that will be validated and provisioned by CSAC.  Resource submissions are cumulative, meaning that as resource files are processed, CSAC will
 * update a previously loaded resource submission with the new resources.
 */
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({ PM_DEFS, KPI_DEFS, PROFILE_DEFS, PM_SCHEMAS, AUGMENTATIONS })
@Builder(toBuilder = true)
public class ResourceSubmission {

    public static final String PM_DEFS = "pm_defs";

    public static final String KPI_DEFS = "kpi_defs";

    public static final String PROFILE_DEFS = "profile_defs";

    public static final String PM_SCHEMAS = "pm_schemas";

    public static final String AUGMENTATIONS = "augmentations";

    /*
     * Optional array of PM definitions.
     */
    @Valid
    @JsonProperty(value = PM_DEFS)
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @Builder.Default
    private List<PMDefinition> pmDefs = Collections.emptyList();

    /*
     * Optional array of PM schema definitions.
     */
    @Valid
    @JsonProperty(value = PM_SCHEMAS)
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @Builder.Default
    private List<PMSchemaDefinition> pmSchemaDefs = Collections.emptyList();

    /*
     * Optional array of KPI definitions.
     */
    @Valid
    @JsonProperty(value = KPI_DEFS)
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @Builder.Default
    private List<KPIDefinition> kpiDefs = Collections.emptyList();

    /*
     * Optional list of augmentation definitions.
     */
    @Valid
    @JsonProperty(value = AUGMENTATIONS)
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @Builder.Default
    private List<AugmentationDefinition> augmentationDefinitions = Collections.emptyList();

    /*
     * Optional array of Profile definitions.
     */
    @Valid
    @JsonProperty(value = PROFILE_DEFS)
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @Builder.Default
    private List<ProfileDefinition> profileDefs = Collections.emptyList();

    /*
     * Checks if the optional field pmDefs exists or is empty
     *
     * @return false if null/empty, true otherwise
     */
    public boolean hasPmDefs() {
        return !CollectionUtils.isEmpty(pmDefs);
    }

    /**
     * Checks if the optional field kpiDefs exists or is empty
     *
     * @return false if null/empty, true otherwise
     */
    public boolean hasKpiDefs() {
        return !CollectionUtils.isEmpty(kpiDefs);
    }

    /**
     * Returns {@code true} if this resource submission has at least one augmentation definition.
     *
     * @return {@code true} if this resource submission has at least one augmentation definition
     */
    public boolean hasAugmentationDefs() {
        return !this.augmentationDefinitions.isEmpty();
    }

    /**
     * Returns {@code true} if this resource submission has at least one PM schema definition.
     *
     * @return {@code true} if this resource submission has at least one PM schema definition
     */
    public boolean hasPmSchemaDefs() {
        return !this.pmSchemaDefs.isEmpty();
    }

    /**
     * Checks if the optional field profileDefs exists or is empty
     *
     * @return false if null/empty, true otherwise
     */
    public boolean hasProfileDefs() {
        return !CollectionUtils.isEmpty(profileDefs);
    }

    /**
     * Merges the provided {@link ResourceSubmission} onto this object. For properties with the same name, the property from the new
     * {@link ResourceSubmission} overwrites the existing property
     *
     * @param newResourceSubmission The new {@link ResourceSubmission} to be merged with the existing one
     */
    public void mergeResourceSubmission(final ResourceSubmission newResourceSubmission) {
        if (Objects.isNull(newResourceSubmission)) {
            throw new CsacValidationException("New Resource Submission does not exist");
        }

        final List<PMSchemaDefinition> mergedPmSchemaDefs = getMergedPmSchemaDefs(newResourceSubmission);
        if (!CollectionUtils.isEmpty(mergedPmSchemaDefs)) {
            this.setPmSchemaDefs(mergedPmSchemaDefs);
        }

        final List<PMDefinition> mergedPmDefs = getMergedPmDefs(newResourceSubmission);
        if (!CollectionUtils.isEmpty(mergedPmDefs)) {
            this.setPmDefs(mergedPmDefs);
        }

        final List<KPIDefinition> mergedKpiDefs = getMergedKpiDefs(newResourceSubmission);
        if (!CollectionUtils.isEmpty(mergedKpiDefs)) {
            this.setKpiDefs(mergedKpiDefs);
        }

        final List<AugmentationDefinition> mergedAugDefs = getMergedAugmentationDefs(newResourceSubmission);
        if (!mergedAugDefs.isEmpty()) {
            this.setAugmentationDefinitions(mergedAugDefs);
        }

        final List<ProfileDefinition> mergedProfileDefs = getMergedProfileDefs(newResourceSubmission);
        if (!CollectionUtils.isEmpty(mergedProfileDefs)) {
            this.setProfileDefs(mergedProfileDefs);
        }
    }

    /**
     * Gets a list of the merged {@link PMDefinition} objects. For PM Definitions with the same name, the property from the new
     * {@link ResourceSubmission} overwrites the existing property
     *
     * @param newResourceSubmission The new {@link ResourceSubmission} to be merged with the existing one
     * @return the merged list of {@link PMDefinition} objects, empty list if both {@link PMDefinition} lists are null/empty
     */
    private List<PMDefinition> getMergedPmDefs(ResourceSubmission newResourceSubmission) {
        if (hasPmDefs()) {
            if (newResourceSubmission.hasPmDefs()) {
                return getMergedResourceDefinitions(pmDefs, newResourceSubmission.getPmDefs());
            } else {
                return pmDefs;
            }
        } else {
            if (newResourceSubmission.hasPmDefs()) {
                return newResourceSubmission.getPmDefs();
            } else {
                return List.of();
            }
        }
    }

    /*
     * Returns a list of PM schema definitions that includes both existing definitions in this resource submission and those provided by the
     * specified resource submission.
     */
    private List<PMSchemaDefinition> getMergedPmSchemaDefs(final ResourceSubmission newResourceSubmission) {

        final List<PMSchemaDefinition> merged = new ArrayList<>();

        if (this.hasPmSchemaDefs()) {
            if (newResourceSubmission.hasPmSchemaDefs()) {
                merged.addAll(getMergedResourceDefinitions(this.pmSchemaDefs, newResourceSubmission.getPmSchemaDefs()));
            }
        } else {
            merged.addAll(newResourceSubmission.getPmSchemaDefs());
        }

        return merged;
    }

    /**
     * Gets a list of the merged {@link KPIDefinition} objects. For KPI Definitions with the same name, the property from the new
     * {@link ResourceSubmission} overwrites the existing property
     *
     * @param newResourceSubmission The new {@link ResourceSubmission} to be merged with the existing one
     * @return the merged list of {@link KPIDefinition} objects, empty list if both {@link KPIDefinition} lists are null/empty
     */
    private List<KPIDefinition> getMergedKpiDefs(ResourceSubmission newResourceSubmission) {
        if (hasKpiDefs()) {
            if (newResourceSubmission.hasKpiDefs()) {
                return getMergedResourceDefinitions(kpiDefs, newResourceSubmission.getKpiDefs());
            } else {
                return kpiDefs;
            }
        } else {
            if (newResourceSubmission.hasKpiDefs()) {
                return newResourceSubmission.getKpiDefs();
            } else {
                return List.of();
            }
        }
    }

    /*
     * Gets a list of the merged {@link ProfileDefinition} objects. For Profile Definitions with the same name, the property from the new
     * {@link ResourceSubmission} overwrites the existing property
     *
     * @param newResourceSubmission
     *         The new {@link ResourceSubmission} to be merged with the existing one
     * @return the merged list of {@link ProfileDefinition} objects, empty list if both {@link ProfileDefinition} lists are null/empty
     */
    private List<ProfileDefinition> getMergedProfileDefs(final ResourceSubmission newResourceSubmission) {
        if (hasProfileDefs()) {
            if (newResourceSubmission.hasProfileDefs()) {
                return getMergedResourceDefinitions(profileDefs, newResourceSubmission.getProfileDefs());
            } else {
                return profileDefs;
            }
        } else {
            if (newResourceSubmission.hasProfileDefs()) {
                return newResourceSubmission.getProfileDefs();
            } else {
                return List.of();
            }
        }
    }

    /*
     *Returns a list of augmentation definitions that includes both existing definitions in this resource submission and those provided by the
     * specified resource submission.
     */
    private List<AugmentationDefinition> getMergedAugmentationDefs(final ResourceSubmission newResourceSubmission) {

        final List<AugmentationDefinition> merged = new ArrayList<>();

        if (this.hasAugmentationDefs()) {
            if (newResourceSubmission.hasAugmentationDefs()) {
                merged.addAll(getMergedResourceDefinitions(this.augmentationDefinitions, newResourceSubmission.getAugmentationDefinitions()));
            }
        } else {
            merged.addAll(newResourceSubmission.getAugmentationDefinitions());
        }

        return merged;
    }

    /**
     * Get the merged list of {@link ResourceDefinition}. Definitions in the new List with the same name overwrite the previous definitions.
     *
     * @param prevDefs A list of {@link ResourceDefinition} objects read previously
     * @param newDefs  A list of {@link ResourceDefinition} objects from a more recent resource file
     * @return a master list of {@link ResourceDefinition} objects that is a union of the two inputs
     */
    private <T extends ResourceDefinition> List<T> getMergedResourceDefinitions(final List<T> prevDefs, final List<T> newDefs) {
        final Map<String, T> newDefsMap = newDefs.stream().collect(Collectors.toMap(ResourceDefinition::getName, val -> val));
        Map<String, T> prevDefsMap = prevDefs.stream().collect(Collectors.toMap(ResourceDefinition::getName, val -> val));

        prevDefsMap.putAll(newDefsMap);
        return new ArrayList<>(prevDefsMap.values());
    }

    /**
     * Adds the PM counters specified in this instance's PM schemas to the list of this instance's PM definitions.
     * PM definitions explicitly defined in the CSAC configuration will overwrite any PM counters included in PM Schema
     * dictionary objects.
     */
    public void addPmCountersToPmDefList() {

        final Map<String, PMDefinition> pmDefsFromPmSchemas = new HashMap<>();

        this.pmSchemaDefs.stream().filter(PMSchemaDefinition::hasPmCounters).forEach(pmSchemaDef -> {

            for (final PMSchemaDefinition.PMCounter pmCounter : pmSchemaDef.getPmCounters()) {

                final PMDefinition pmDefinition = pmCounter.toPmDefinition(pmSchemaDef.getUri());
                pmDefsFromPmSchemas.put(pmDefinition.getName(), pmDefinition);
            }
        });

        this.pmDefs.forEach(pmDefinition -> pmDefsFromPmSchemas.put(pmDefinition.getName(), pmDefinition));

        this.pmDefs = new ArrayList<>(pmDefsFromPmSchemas.values());

    }
}
