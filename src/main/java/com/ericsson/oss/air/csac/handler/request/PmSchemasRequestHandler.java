/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.handler.request;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.ericsson.oss.air.api.model.RtPmSchemaInfoDto;
import com.ericsson.oss.air.api.model.RtPmSchemaInfoListDto;
import com.ericsson.oss.air.csac.configuration.schema.InputSchemaProvider;
import com.ericsson.oss.air.csac.model.AugmentationDefinition;
import com.ericsson.oss.air.csac.model.PMDefinition;
import com.ericsson.oss.air.csac.model.pmsc.KpiDefinitionDTO;
import com.ericsson.oss.air.csac.model.pmsc.KpiTypeEnum;
import com.ericsson.oss.air.csac.repository.DeployedKpiDefDAO;
import com.ericsson.oss.air.csac.repository.EffectiveAugmentationDAO;
import com.ericsson.oss.air.csac.repository.PMDefinitionDAO;
import com.ericsson.oss.air.csac.service.augmentation.AugmentationProvisioningService;
import com.ericsson.oss.air.util.ListUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Request handler for REST requests related to PM Schemas.
 */
@RequiredArgsConstructor
@Component
@Slf4j
public class PmSchemasRequestHandler {

    private final InputSchemaProvider inputSchemaProvider;

    private final DeployedKpiDefDAO deployedKpiDefDAO;

    private final PMDefinitionDAO pmDefinitionDAO;

    private final EffectiveAugmentationDAO effectiveAugmentationDAO;

    private final AugmentationProvisioningService augmentationService;

    /**
     * Returns a list of PM schemas currently configured for use in runtime KPI calculations.
     * <p>
     * The basic flow is as follows:
     * <ol>
     * <li>Get all simple deployed KPI definitions</li>
     * <li>Get all dictionary PM definitions</li>
     * <li>Get all effective augmentation definitions</li>
     * <li>Get all augmented schema references</li>
     * </ol>
     * <p>
     * Each simple deployed KPI definition will be iterated over to compile a list of PM schemas. The resulting list
     * will contain one object per schema reference.
     *
     * @return a list of PM schemas currently configured for use in runtime KPI calculations.
     */
    public RtPmSchemaInfoListDto getPmSchemas() {

        log.info("Getting the list of runtime PM schemas");

        final List<KpiDefinitionDTO> simpleDeployedKpiList = this.deployedKpiDefDAO.getAllDeployedKpis().stream()
                .filter(deployedKpi -> KpiTypeEnum.SIMPLE == deployedKpi.getKpiType())
                .collect(Collectors.toList());

        // Return empty result if there are no simple deployed KPIs
        if (simpleDeployedKpiList.isEmpty()) {
            return new RtPmSchemaInfoListDto().pmschemas(new ArrayList<>());
        }

        // Index PM definitions by schema reference. Each map entry has a list of the PM names associated with the key,
        // a schema reference.
        final Map<String, List<String>> pmDefinitionIndex = this.createPmDefinitionIndex();
        log.debug("PM Definition Index: {}", pmDefinitionIndex);

        final List<AugmentationDefinition> augmentationDefinitionList = this.effectiveAugmentationDAO.findAll();

        // If there are effective augmentations, then need mapping between unaugmented/input schema references and augmented/output schema references.
        // Create index where key is augmented schema reference and value is unaugmented schema reference
        final Map<String, String> augmentedSchemaIndex = new HashMap<>();
        if (!augmentationDefinitionList.isEmpty()) {
            augmentedSchemaIndex.putAll(this.createAugMappingIndex(augmentationDefinitionList));
        }
        log.debug("Augmentation Mappings Index: {}", augmentedSchemaIndex);

        // Iterate over each simple deployed KPI to create a unique Runtime PM Schema object per schema reference
        final List<RtPmSchemaInfoDto> resultList = this.generateResultList(simpleDeployedKpiList, pmDefinitionIndex, augmentedSchemaIndex);

        return new RtPmSchemaInfoListDto().pmschemas(resultList);
    }

    /*
     * Returns the PM definitions indexed by their source values. A PM definition source is a schema reference.
     */
    Map<String, List<String>> createPmDefinitionIndex() {

        final Map<String, List<String>> pmDefIndex = this.pmDefinitionDAO.findAllPMDefinitions(0, Integer.MAX_VALUE)
                .stream().collect(Collectors.groupingBy(PMDefinition::getSource, Collectors.collectingAndThen(Collectors.toList(),
                        list -> list.stream().map(PMDefinition::getName).collect(Collectors.toList()))));

        return Map.copyOf(pmDefIndex);
    }

    /*
     * Returns a map/index where the augmented schema references are the keys and the unaugmented schema references are
     * the values.
     */
    Map<String, String> createAugMappingIndex(final List<AugmentationDefinition> augmentationDefinitionList) {

        final Map<String, String> resultingIndex = new HashMap<>();

        augmentationDefinitionList.stream()
                .map(augmentationDefinition -> this.augmentationService.getSchemaMappings(augmentationDefinition.getName()))
                .forEach(ardqMap -> {

                    for (final Map.Entry<String, String> entry : ardqMap.entrySet()) {
                        resultingIndex.put(entry.getValue(), entry.getKey());
                    }
                });

        return Map.copyOf(resultingIndex);

    }

    /*
     * Creates the PM runtime schema object.
     */
    RtPmSchemaInfoDto createRtPmSchemaInfoDto(final KpiDefinitionDTO simpleDeployedKpi, final String schemaReference,
                                              final Map<String, List<String>> pmDefinitionIndex,
                                              final Map<String, String> augmentedSchemaIndex) {

        final String schemaTopic = this.inputSchemaProvider.getSchema(schemaReference).kafkaTopic();
        final List<String> contexts = simpleDeployedKpi.getUnqualifiedAggregationElements();
        final boolean isAugmented = !pmDefinitionIndex.containsKey(schemaReference);
        final String unaugmentedSchemaReference = isAugmented ? augmentedSchemaIndex.get(schemaReference) : schemaReference;
        final List<String> pmDefNames = pmDefinitionIndex.get(unaugmentedSchemaReference);

        return new RtPmSchemaInfoDto()
                .schemaRef(schemaReference)
                .schemaTopic(schemaTopic)
                .augmented(isAugmented)
                .pmdefs(pmDefNames)
                .contexts(contexts);
    }

    /*
     * Generates the final resulting list of PM runtime schemas to send to the client. There will be one object in this list per schema reference.
     */
    private List<RtPmSchemaInfoDto> generateResultList(final List<KpiDefinitionDTO> simpleDeployedKpiList,
                                                       final Map<String, List<String>> pmDefinitionIndex,
                                                       final Map<String, String> augmentedSchemaIndex) {

        final Map<String, RtPmSchemaInfoDto> resultMap = new HashMap<>();

        for (final KpiDefinitionDTO simpleDeployedKpi : simpleDeployedKpiList) {

            final String schemaReference = simpleDeployedKpi.getInpDataIdentifier();

            if (resultMap.containsKey(schemaReference)) {

                // Need to add this deployed KPI's contexts to existing RT PM Schema object
                final RtPmSchemaInfoDto existingSchemaInfoDto = resultMap.get(schemaReference);
                final List<String> allContexts = ListUtils.getMergedList(existingSchemaInfoDto.getContexts(),
                        simpleDeployedKpi.getUnqualifiedAggregationElements());
                existingSchemaInfoDto.setContexts(allContexts);

            } else {
                final RtPmSchemaInfoDto schemaInfoDto = this.createRtPmSchemaInfoDto(simpleDeployedKpi, schemaReference, pmDefinitionIndex,
                        augmentedSchemaIndex);
                resultMap.put(schemaReference, schemaInfoDto);
            }

        }

        return resultMap.values().stream().toList();
    }

}
