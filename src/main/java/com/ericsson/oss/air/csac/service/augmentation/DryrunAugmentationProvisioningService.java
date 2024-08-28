/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.service.augmentation;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.ericsson.oss.air.csac.model.AugmentationDefinition;
import com.ericsson.oss.air.csac.model.AugmentationRule;
import com.ericsson.oss.air.csac.model.augmentation.AugmentationRequestDto;
import com.ericsson.oss.air.csac.model.pmschema.SchemaReference;
import com.ericsson.oss.air.csac.repository.EffectiveAugmentationDAO;
import com.ericsson.oss.air.util.codec.Codec;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/**
 * {@inheritDoc} This implementation logs the operations being performed but does not submit augmentation resources to the augmentation service.
 */
@Slf4j
@RequiredArgsConstructor
@Service
@Profile({ "dry-run" })
public class DryrunAugmentationProvisioningService implements AugmentationProvisioningService {

    private final Codec codec;

    private final EffectiveAugmentationDAO augmentationDAO;

    @Override
    public void checkArdqType(final AugmentationDefinition augDefinition) {

        final String queryType = Strings.isBlank(augDefinition.getType()) ? "default" : augDefinition.getType();
        log.info("Checking dimensioning query type: {}", queryType);
    }

    @SneakyThrows
    @Override
    public void create(final List<AugmentationRequestDto> augmentationRequestDtoList) {
        log.info("Submitting new augmentation definitions: {}", this.codec.writeValueAsString(augmentationRequestDtoList));
    }

    @SneakyThrows
    @Override
    public void update(final List<AugmentationRequestDto> augmentationRequestDtoList) {
        log.info("Updating augmentation definitions: {}", this.codec.writeValueAsString(augmentationRequestDtoList));
    }

    @SneakyThrows
    @Override
    public void delete(final List<AugmentationRequestDto> augmentationRequestDtoList) {
        log.info("Deleting augmentation definitions: {}", this.codec.writeValueAsString(augmentationRequestDtoList));
    }

    @Override
    public Map<String, String> getSchemaMappings(final String ardqId) {

        final Map<String, String> resultMap = new HashMap<>();

        final Optional<AugmentationDefinition> augmentationDefinition = this.augmentationDAO.findById(ardqId);

        if (augmentationDefinition.isPresent()) {
            final List<String> inputSchemaReferences = augmentationDefinition.get().getAugmentationRules().stream().map(AugmentationRule::getAllInputSchemas).flatMap(Collection::stream).collect(
                    Collectors.toList());

            inputSchemaReferences.forEach(inputSchema -> {
                final SchemaReference reference = SchemaReference.of(inputSchema);

                final String outputSchema = SchemaReference.builder()
                        .dataSpace(reference.getDataSpace())
                        .dataCategory(reference.getDataCategory())
                        .schemaId(ardqId + "_" + reference.getSchemaId())
                        .build()
                        .toString();

                resultMap.put(inputSchema, outputSchema);
            });
        }

        return resultMap;
    }

    @SneakyThrows
    @Override
    public void deleteAll() {

        log.info("Deleting effective augmentation definitions: {}", this.codec.writeValueAsString(this.augmentationDAO.findAll()));
    }
}
