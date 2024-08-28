/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.handler.augmentation;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.ericsson.oss.air.csac.configuration.augmentation.AugmentationConfiguration;
import com.ericsson.oss.air.csac.handler.event.ConsistencyCheckEvent;
import com.ericsson.oss.air.csac.handler.event.ConsistencyCheckHandler;
import com.ericsson.oss.air.csac.model.AugmentationDefinition;
import com.ericsson.oss.air.csac.model.AugmentationRule;
import com.ericsson.oss.air.csac.model.AugmentationRuleField;
import com.ericsson.oss.air.csac.model.ProfileDefinition;
import com.ericsson.oss.air.csac.model.augmentation.AugmentationFieldRequestDto;
import com.ericsson.oss.air.csac.model.augmentation.AugmentationRequestDto;
import com.ericsson.oss.air.csac.model.augmentation.AugmentationRuleRequestDto;
import com.ericsson.oss.air.csac.repository.AugmentationDefinitionDAO;
import com.ericsson.oss.air.csac.repository.EffectiveAugmentationDAO;
import com.ericsson.oss.air.csac.service.augmentation.AugmentationProvisioningService;
import com.ericsson.oss.air.exception.CsacConsistencyCheckException;
import com.ericsson.oss.air.util.DiffEngine;
import com.ericsson.oss.air.util.codec.Codec;
import com.ericsson.oss.air.util.logging.FaultHandler;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

/**
 * {@inheritDoc} This implementation is invoked when AAS provisioning is enabled.
 */
@Component
@Slf4j
@ConditionalOnProperty(value = "provisioning.aas.enabled",
                       havingValue = "true")
@Primary
@RequiredArgsConstructor
public class LiveAugmentationHandler implements AugmentationHandler {

    private final AugmentationDefinitionDAO augmentationDefinitionDAO;

    private final EffectiveAugmentationDAO effectiveAugmentationDAO;

    private final AugmentationConfiguration augmentationConfiguration;

    private final FaultHandler faultHandler;

    private final AugmentationDiffCalculator calculator;

    private final AugmentationProvisioningService augmentationProvisioningService;

    private final Codec codec;

    private final ConsistencyCheckHandler consistencyCheckHandler;

    /**
     * {@inheritDoc}
     * <br>
     * This method does the following:
     * <ol>
     *     <li>Calculates effective augmentations.</li>
     *     <li>Invokes {@link AugmentationDiffCalculator} to calculate new, updated and deleted augmentations</li>
     *     <li>Submits calculated augmentations to AAS using {@link AugmentationProvisioningService}</li>
     *     <li>Persists new and updated augmentations, removes deleted augmentations
     *     from runtime datastore using {@link com.ericsson.oss.air.csac.repository.EffectiveAugmentationDAO}</li>
     * </ol>
     *
     * @param pendingProfiles list of pending profiles in the current resource submission.
     */
    @SneakyThrows
    @Override
    public void submit(final List<ProfileDefinition> pendingProfiles) {

        if (pendingProfiles.isEmpty()) {
            log.info("No augmentation changes found.  Skipping update.");
            return;
        }

        final List<AugmentationDefinition> candidateList = this.getEffectiveAugmentations(pendingProfiles);
        log.info("Effective Augmentations: {}", this.codec.writeValueAsString(candidateList));

        final List<AugmentationDefinition> sourceList = this.effectiveAugmentationDAO.findAll();
        final DiffEngine<AugmentationDefinition> diffCalculator = this.calculator.builder()
                .identityFunction(AugmentationDefinition::getName)
                .source(sourceList)
                .build();

        createAugmentations(diffCalculator.getAdded(candidateList), pendingProfiles);
        updateAugmentations(diffCalculator.getUpdated(candidateList), pendingProfiles);
        deleteAugmentations(diffCalculator.getDeleted(candidateList));

        log.info("AAS provisioning completed successfully.");

    }

    /*
     * (non-javadoc)
     *
     * Returns a list of effective augmentation definitions from the provided
     * list of all pending profiles in the current resource submission.
     *
     * @param pendingProfiles list of pending profiles in the current resource submission.
     *
     * @return list of effective augmentations.
     */
    protected List<AugmentationDefinition> getEffectiveAugmentations(final List<ProfileDefinition> profileList) {

        final List<AugmentationDefinition> effectiveAugmentationDefList = new ArrayList<>();
        final List<AugmentationDefinition> dictionaryAugmentationDefList = this.augmentationDefinitionDAO.findAll();

        if (!ObjectUtils.isEmpty(dictionaryAugmentationDefList)) {

            for (final AugmentationDefinition augmentationDefinition : dictionaryAugmentationDefList) {

                final String augmentationName = augmentationDefinition.getName();

                for (final ProfileDefinition profileDefinition : profileList) {

                    if (augmentationName.equals(profileDefinition.getAugmentation())) {

                        //If augmentation definition has url reference, resolve and set it in augmentation definition.
                        final String url = this.augmentationConfiguration.getResolvedUrl(augmentationDefinition.getUrl());
                        augmentationDefinition.setUrl(url);

                        effectiveAugmentationDefList.add(augmentationDefinition);
                        break;
                    }
                }
            }
        }
        return effectiveAugmentationDefList;
    }

    @SneakyThrows
    protected void createAugmentations(final List<AugmentationDefinition> augmentationDefinitionList,
                                       final List<ProfileDefinition> profileList) {

        List<AugmentationRequestDto> augmentationRequestDtoList = new ArrayList<>();

        if (!augmentationDefinitionList.isEmpty()) {

            augmentationRequestDtoList = augmentationDefinitionList.stream()
                    .map(this::mapDefinitionToDto)
                    .collect(Collectors.toList());

            augmentationProvisioningService.create(augmentationRequestDtoList);

            for (final AugmentationDefinition augmentationDefinition : augmentationDefinitionList) {
                final List<String> affectedProfiles = profileList.stream()
                        .filter(profile -> augmentationDefinition.getName().equals(profile.getAugmentation()))
                        .map(ProfileDefinition::getName)
                        .collect(Collectors.toList());

                consistencyCheckOnDBOperation(augmentationDefinition, affectedProfiles);

                log.debug("Augmentation '{}' is associated with profile(s): {}",
                        augmentationDefinition.getName(),
                        codec.writeValueAsString(affectedProfiles));
            }
        }

        log.info("Number of augmentations created: {}", augmentationRequestDtoList.size());
        log.debug("Created augmentations json: {}", this.codec.writeValueAsStringPretty(augmentationRequestDtoList));
    }

    @SneakyThrows
    protected void updateAugmentations(final List<AugmentationDefinition> augmentationDefinitionList,
                                       final List<ProfileDefinition> profileList) {

        List<AugmentationRequestDto> augmentationRequestDtoList = new ArrayList<>();

        if (!augmentationDefinitionList.isEmpty()) {
            augmentationRequestDtoList = augmentationDefinitionList.stream()
                    .map(this::mapDefinitionToDto)
                    .collect(Collectors.toList());

            augmentationProvisioningService.update(augmentationRequestDtoList);

            for (final AugmentationDefinition augmentationDefinition : augmentationDefinitionList) {
                final List<String> affectedProfiles = profileList.stream()
                        .filter(profile -> augmentationDefinition.getName().equals(profile.getAugmentation()))
                        .map(ProfileDefinition::getName)
                        .collect(Collectors.toList());

                consistencyCheckOnDBOperation(augmentationDefinition, affectedProfiles);

                log.debug("Augmentation '{}' is associated with profile(s): {}",
                        augmentationDefinition.getName(),
                        codec.writeValueAsString(affectedProfiles));
            }
        }

        log.info("Number of augmentations updated: {}", augmentationRequestDtoList.size());
        log.debug("Updated augmentations json: {}", this.codec.writeValueAsStringPretty(augmentationRequestDtoList));
    }

    @SneakyThrows
    protected void deleteAugmentations(final List<AugmentationDefinition> augmentationDefinitionList) {

        List<AugmentationRequestDto> augmentationRequestDtoList = new ArrayList<>();

        if (!augmentationDefinitionList.isEmpty()) {

            augmentationRequestDtoList = augmentationDefinitionList.stream()
                    .map(this::mapDefinitionToDto)
                    .collect(Collectors.toList());

            this.augmentationProvisioningService.delete(augmentationRequestDtoList);

            augmentationDefinitionList.stream()
                    .forEach(augmentationDefinition -> {
                                try {
                                    this.effectiveAugmentationDAO.delete(augmentationDefinition.getName());
                                } catch (final Exception e) {
                                    this.consistencyCheckHandler.notifyCheckFailure(
                                            new ConsistencyCheckEvent.Payload(ConsistencyCheckEvent.Payload.Type.SUSPECT, 1));
                                    throw new CsacConsistencyCheckException(e);
                                }
                            }
                    );
        }

        log.info("Number of augmentations deleted: {}", augmentationRequestDtoList.size());
        log.debug("Deleted augmentations json: {}", this.codec.writeValueAsStringPretty(augmentationRequestDtoList));
    }

    /*
     * Maps CSAC augmentation definition to AAS augmentation DTO.
     */
    protected AugmentationRequestDto mapDefinitionToDto(final AugmentationDefinition augmentationDefinition) {

        final List<AugmentationRuleRequestDto> augmentationRuleRequestDtoList = mapDefinitionRulesToDtoRules(augmentationDefinition);

        final AugmentationRequestDto augmentationRequestDto = AugmentationRequestDto.builder()
                .ardqId(augmentationDefinition.getName())
                .ardqUrl(augmentationDefinition.getUrl())
                .rules(augmentationRuleRequestDtoList)
                .build();

        //Set ardqType if exists.
        if (!isEmpty(augmentationDefinition.getType())) {
            augmentationRequestDto.setArdqType(augmentationDefinition.getType());
        }

        return augmentationRequestDto;
    }

    private List<AugmentationRuleRequestDto> mapDefinitionRulesToDtoRules(final AugmentationDefinition augmentationDefinition) {

        final List<AugmentationRuleRequestDto> augmentationRuleRequestDtoList = new ArrayList<>();
        for (final AugmentationRule augmentationRule : augmentationDefinition.getAugmentationRules()) {

            final List<AugmentationFieldRequestDto> augmentationFieldRequestDtoList = mapDefinitionRuleFieldsToDtoRuleFields(
                    augmentationRule);

            for (final String inputSchema : augmentationRule.getAllInputSchemas()) {

                final AugmentationRuleRequestDto augmentationRuleRequestDto =
                        AugmentationRuleRequestDto.builder()
                                .inputSchema(inputSchema)
                                .fields(augmentationFieldRequestDtoList)
                                .build();

                augmentationRuleRequestDtoList.add(augmentationRuleRequestDto);
            }
        }
        return augmentationRuleRequestDtoList;
    }

    private List<AugmentationFieldRequestDto> mapDefinitionRuleFieldsToDtoRuleFields(final AugmentationRule augmentationRule) {
        final List<AugmentationFieldRequestDto> augmentationFieldRequestDtoList = new ArrayList<>();
        for (final AugmentationRuleField fieldRule : augmentationRule.getFields()) {

            for (final String outputField : fieldRule.getAllOutputFields()) {

                final AugmentationFieldRequestDto augmentationFieldRequestDto =
                        AugmentationFieldRequestDto.builder()
                                .input(fieldRule.getInputFields())
                                .output(outputField)
                                .build();

                augmentationFieldRequestDtoList.add(augmentationFieldRequestDto);
            }
        }
        return augmentationFieldRequestDtoList;
    }

    private boolean isEmpty(String ardqType) {
        return ObjectUtils.isEmpty(ardqType) || ardqType.trim().isEmpty();
    }

    private void consistencyCheckOnDBOperation(final AugmentationDefinition augmentationDefinition, final List<String> affectedProfiles) {
        try {
            this.effectiveAugmentationDAO.save(augmentationDefinition, affectedProfiles);
        } catch (final Exception e) {
            this.consistencyCheckHandler.notifyCheckFailure(new ConsistencyCheckEvent.Payload(ConsistencyCheckEvent.Payload.Type.SUSPECT, 1));
            throw new CsacConsistencyCheckException(e);
        }
    }

}
