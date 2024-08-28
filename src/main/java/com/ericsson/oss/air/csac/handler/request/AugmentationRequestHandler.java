/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.handler.request;

import static com.ericsson.oss.air.util.RestEndpointUtil.getRows;
import static com.ericsson.oss.air.util.RestEndpointUtil.getStart;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.ericsson.oss.air.api.model.AugmentationDto;
import com.ericsson.oss.air.api.model.AugmentationFieldDto;
import com.ericsson.oss.air.api.model.AugmentationListDto;
import com.ericsson.oss.air.api.model.AugmentationRuleDto;
import com.ericsson.oss.air.csac.model.AugmentationDefinition;
import com.ericsson.oss.air.csac.model.AugmentationRule;
import com.ericsson.oss.air.csac.model.AugmentationRuleField;
import com.ericsson.oss.air.csac.repository.AugmentationDefinitionDAO;
import com.ericsson.oss.air.csac.repository.EffectiveAugmentationDAO;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Handler class for augmentation REST requests.
 */
@Component
public class AugmentationRequestHandler {

    @Autowired
    private AugmentationDefinitionDAO augmentationDefinitionDAO;

    @Autowired
    private EffectiveAugmentationDAO effectiveAugmentationDAO;

    /**
     * Returns a list of augmentation definitions requested by a REST client.  The returned list includes
     * <ul>
     *     <li>total number of augmentation definitions</li>
     *     <li>number of augmentation definitions requested per page</li>
     *     <li>number of augmentation definitions found</li>
     *     <li>start page for the number of augmentation definitions</li>
     *     <li>list of augmentation definitions</li>
     * </ul>
     *
     * All augmentation definitions in the data dictionary are included in the response.  Additional runtime-specific data for effective
     * augmentations, e.g. the list of associated profiles, is included, if available.
     *
     * @param start
     *         start page for the query
     * @param rows
     *         number of rows requested per page
     * @return a list of augmentation definitions requested by a REST client
     */
    public AugmentationListDto getAugmentationList(final Integer start, final Integer rows) {

        final AugmentationListDto augmentationListDto = new AugmentationListDto().augmentations(new ArrayList<>()).start(start).rows(rows);

        // the augmentation definition comes from the data dictionary.  Additional runtime-specific data, e.g. URL and associated profiles, will be
        // retrieved from the runtime data store during definition-DTO mapping.

        final List<AugmentationDefinition> augmentationList = this.augmentationDefinitionDAO.findAll(getStart(start, rows), getRows(rows));

        augmentationListDto.setCount(augmentationList.size());
        augmentationListDto.setTotal(this.augmentationDefinitionDAO.totalAugmentationDefinitions());

        for (final AugmentationDefinition augmentation : augmentationList) {
            augmentationListDto.addAugmentationsItem(mapDefinitionToDto(augmentation));
        }

        return augmentationListDto;
    }

    /*
     * (non-javadoc)
     *
     * Maps AugmentationDefinition model objects to AugmentationDto objects. Fills in runtime-specific data
     * from the effective augmentation data store.
     *
     */
    protected AugmentationDto mapDefinitionToDto(final AugmentationDefinition definition) {

        final AugmentationDto dto = new AugmentationDto();
        dto.setArdqId(definition.getName());

        dto.setArdqRules(mapDefinitionsRulesToDto(definition));

        if (Strings.isNotBlank(definition.getUrl())) {
            dto.setArdqUrl(definition.getUrl());
        }

        if (Strings.isNotBlank(definition.getType())) {
            dto.setArdqType(definition.getType());
        }

        final List<String> profiles = new ArrayList<>();

        final Optional<AugmentationDefinition> effectiveDefinition = this.effectiveAugmentationDAO.findById(definition.getName());

        if (effectiveDefinition.isPresent()) {
            dto.setArdqUrl(effectiveDefinition.get().getUrl());
            profiles.addAll(this.effectiveAugmentationDAO.findAllProfileNames(definition.getName()));
        }

        dto.setProfiles(profiles);

        return dto;
    }

    /*
     * (non-javadoc)
     *
     * Maps augmentation rules in AugmentationDefinition model beans to their equivalent DTOs.
     */
    private static List<AugmentationRuleDto> mapDefinitionsRulesToDto(final AugmentationDefinition definition) {

        final List<AugmentationRuleDto> dtoRuleList = new ArrayList<>();

        for (final AugmentationRule augmentationRule : definition.getAugmentationRules()) {

            for (final String inputSchema : augmentationRule.getAllInputSchemas()) {
                final AugmentationRuleDto dtoRule = new AugmentationRuleDto();
                dtoRule.setInputSchema(inputSchema);
                dtoRule.setFields(mapDefinitionRuleFieldsToDto(augmentationRule));
                dtoRuleList.add(dtoRule);
            }

        }

        return dtoRuleList;
    }

    /*
     * (non-javadoc)
     *
     * Maps augmentation rule fields in AugmentationDefinition model beans to their equivalent DTOs.
     */
    private static List<AugmentationFieldDto> mapDefinitionRuleFieldsToDto(final AugmentationRule augmentationRule) {

        final List<AugmentationFieldDto> augmentationFieldDtoList = new ArrayList<>();

        for (final AugmentationRuleField fieldRule : augmentationRule.getFields()) {

            for (final String outputField : fieldRule.getAllOutputFields()) {
                final AugmentationFieldDto fieldDto = new AugmentationFieldDto();
                fieldDto.setOutput(outputField);
                fieldDto.setInput(fieldRule.getInputFields());
                augmentationFieldDtoList.add(fieldDto);
            }
        }

        return augmentationFieldDtoList;
    }
}
