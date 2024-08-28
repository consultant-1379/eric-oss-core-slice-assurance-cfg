/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.handler.validation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import com.ericsson.oss.air.csac.model.AugmentationDefinition;
import com.ericsson.oss.air.csac.model.InputMetric;
import com.ericsson.oss.air.csac.model.InputMetricOverride;
import com.ericsson.oss.air.csac.model.KPIDefinition;
import com.ericsson.oss.air.csac.model.KPIReference;
import com.ericsson.oss.air.csac.model.ProfileDefinition;
import com.ericsson.oss.air.csac.model.ResourceSubmission;
import com.ericsson.oss.air.csac.model.pmsc.KPIDefinitionDTOMapping;
import com.ericsson.oss.air.csac.repository.KPIDefinitionDAO;
import com.ericsson.oss.air.exception.CsacValidationException;
import com.ericsson.oss.air.util.logging.FaultHandler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class ProfileContextValidator implements CsacValidator<ResourceSubmission> {


    private final KPIDefinitionDAO kpiDefinitionDAO;


    private final FaultHandler faultHandler;

    /**
     * Validates the context of Profile definitions after bean validation
     *
     * @param resourceToValidate the submitted resource to validate
     */
    @Override
    public void validate(final ResourceSubmission resourceToValidate) {
        this.validateProfileDefinitions(resourceToValidate);
    }

    /**
     * Validates that the profiles in the given resource submission only reference KPIs existing either in the current submission or in the data
     * dictionary
     *
     * @param resourceSubmission the Resource Submission containing the profiles to be validated
     * @throws CsacValidationException
     */
    public void validateProfileDefinitions(final ResourceSubmission resourceSubmission) {

        if (!resourceSubmission.hasProfileDefs()) {
            return;
        }

        final List<ProfileDefinition> profileDefinitions = resourceSubmission.getProfileDefs();
        profileDefinitions.forEach(profileDefinition -> checkProfileContext(profileDefinition, resourceSubmission));
    }

    /**
     * This method validates the following:
     * <ul>
     *     <li>The profile definition only references KPIs exists either in the current submission or in the data dictionary</li>
     *     <li>The profile definition only references Augmentation definitions exists in current submission</li>
     *     <li>Input metric ids defined in input metric override within a profile kpi references exists in referenced KPI definition</li>
     *     <li>Input metric contexts defined in input metric override within a profile kpi references contains at least one common field from
     *     profile context fields</li>
     *     <li>Input metric contexts defined in input metric override within a profile kpi references must be the same</li>
     * </ul>
     *
     * @param profileDefinition  the profile definition to be validated
     * @param resourceSubmission the resource submission that contains the KPIs to be validated against
     * @throws CsacValidationException
     */
    private void checkProfileContext(final ProfileDefinition profileDefinition, final ResourceSubmission resourceSubmission) {

        final Set<KPIDefinition> resourceKpiDefinitions = new HashSet<>(resourceSubmission.getKpiDefs());
        final Set<KPIDefinition> dbKpiDefinitions = new HashSet<>(this.kpiDefinitionDAO.findAll());

        //If profile has augmentation, check if associated augmentation definition exists in resource submission.
        final String profileAugmentation = profileDefinition.getAugmentation();
        if (StringUtils.hasText(profileAugmentation)) {
            validateProfileAugmentation(profileAugmentation, resourceSubmission);
        }

        final Map<String, KPIDefinition> kpiDefNameIdx = dbKpiDefinitions.stream()
                .collect(Collectors.toMap(KPIDefinition::getName, Function.identity()));
        kpiDefNameIdx.putAll(resourceKpiDefinitions.stream().collect(Collectors.toMap(KPIDefinition::getName, Function.identity())));

        profileDefinition.getKpis().forEach(kpiReference -> {

            if (!kpiDefNameIdx.containsKey(kpiReference.getRef())) {

                final String errorMessage = String.format("KPI %s does not exist in the existing resource submissions or data dictionary",
                                                          kpiReference.getRef());
                final CsacValidationException cve = new CsacValidationException(errorMessage);
                this.faultHandler.fatal(cve);
                throw cve;
            }

            if (!isBlank(kpiReference.getInputMetricOverrides())) {
                final List<String> profileContext = profileDefinition.getContext();
                checkInputMetricOverrideContext(kpiReference, kpiDefNameIdx.get(kpiReference.getRef()), profileContext);
            }
        });
    }

    private void validateProfileAugmentation(final String profileAugmentation, final ResourceSubmission resourceSubmission) {

        final String errorMsg = String.format("Augmentation %s does not exist in the existing resource submissions", profileAugmentation);
        final List<AugmentationDefinition> augmentationDefinitionList = resourceSubmission.getAugmentationDefinitions();

        if (!ObjectUtils.isEmpty(augmentationDefinitionList)) {

            for (final AugmentationDefinition augmentationDefinition : augmentationDefinitionList) {
                if (augmentationDefinition.getName().equals(profileAugmentation)) {
                    return;
                }
            }
        }

        final CsacValidationException cve = new CsacValidationException(errorMsg);
        this.faultHandler.fatal(cve);
        throw cve;
    }

    private boolean isBlank(final List<InputMetricOverride> overrideList) {
        return Objects.isNull(overrideList) || overrideList.isEmpty();
    }

    private void checkInputMetricOverrideContext(final KPIReference kpiReference, final KPIDefinition kpiDefinition,
                                                 final List<String> profileContext) {

        final Set<String> existingIds = kpiDefinition.getInputMetrics().stream().map(InputMetric::getId).collect(Collectors.toSet());

        kpiReference.getInputMetricOverrides().forEach(inputMetricOverride -> {

            final String overrideId = inputMetricOverride.getId();
            if (!existingIds.contains(overrideId)) {
                final String errorMessage =
                        "Invalid input metric override: input metric " + overrideId + " does not exist in KPI defintion " + kpiDefinition.getName();
                log.error(errorMessage);
                throw new CsacValidationException(errorMessage);
            }

            final List<String> overrideFieldsList = inputMetricOverride.getContext();
            if (!CollectionUtils.isEmpty(overrideFieldsList)) {
                final List<String> commonFields = KPIDefinitionDTOMapping.getCommonFields(profileContext, overrideFieldsList);
                // At least one common fields
                if (CollectionUtils.isEmpty(commonFields)) {
                    final String errorMsg = "Invalid input metrics override context fields " + inputMetricOverride.getContext()
                            + ", must contain at least one of the context fields from the profile";
                    log.error(errorMsg);
                    throw new CsacValidationException(errorMsg);
                }
            }
        });
    }
}
