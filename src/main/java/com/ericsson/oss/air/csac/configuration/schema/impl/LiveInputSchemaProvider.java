/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.configuration.schema.impl;

import static com.ericsson.oss.air.csac.model.datacatalog.MessageSchemaDTO.SPEC_REF_PATTERN;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;

import com.ericsson.oss.air.csac.configuration.schema.InputSchemaProvider;
import com.ericsson.oss.air.csac.model.AugmentationDefinition;
import com.ericsson.oss.air.csac.model.PMDefinition;
import com.ericsson.oss.air.csac.model.ProfileDefinition;
import com.ericsson.oss.air.csac.model.datacatalog.MessageSchemaDTO;
import com.ericsson.oss.air.csac.model.pmschema.PMSchemaDTO;
import com.ericsson.oss.air.csac.model.pmschema.PmSchema;
import com.ericsson.oss.air.csac.repository.AugmentationDefinitionDAO;
import com.ericsson.oss.air.csac.service.DataCatalogService;
import com.ericsson.oss.air.csac.service.SchemaRegistryService;
import com.ericsson.oss.air.csac.service.augmentation.AugmentationProvisioningService;
import com.ericsson.oss.air.exception.CsacValidationException;
import com.ericsson.oss.air.util.logging.FaultHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Provider for input schemas. An InputSchemaProvider instance caches schemas in memory for later access by CSAC business logic.
 */
@Component
@RequiredArgsConstructor
@Profile({ "prod", "test" })
public class LiveInputSchemaProvider implements InputSchemaProvider {

    private final Map<String, PmSchema> cachedInputSchemas = new HashMap<>();

    private final DataCatalogService dataCatalogService;

    private final SchemaRegistryService schemaRegistryService;

    private final FaultHandler faultHandler;

    private final AugmentationDefinitionDAO augmentationDefinitionDAO;

    private final AugmentationProvisioningService augmentationService;

    /**
     * Prefetches the input schemas identified in the set of schema references. This will ensure the local availability of all schemas referenced in
     * the set of schema references.
     * A 2-stage lookup, e.g. querying the Data Catalog and the Schema Registry, is required for PM validation
     *
     * @param schemaReferenceSet set of input schema references in the format '5G|PM_COUNTERS|Subject'.
     */
    @Override
    public void prefetchInputSchemas(final Set<String> schemaReferenceSet) {

        // filter to ensure any schemas already in the internal cache are not fetched again
        final Set<String> filteredSet = filteredSet(schemaReferenceSet, cachedInputSchemas.keySet());

        final Map<String, MessageSchemaDTO> cachedMessageSchemas = this.dataCatalogService.getMessageSchemas(filteredSet);

        for (final var entry : cachedMessageSchemas.entrySet()) {
            final MessageSchemaDTO messageSchema = entry.getValue();
            if (Objects.isNull(messageSchema)) {
                final String errorMessage = "No message schema retrieved from Data Catalog for this schema reference: " + entry.getKey();
                final CsacValidationException cve = new CsacValidationException(errorMessage);
                this.faultHandler.error(cve);
                throw cve;
            }
            final PMSchemaDTO inputSchemaDto = this.schemaRegistryService.getSchemaLatest(fetchSubject(messageSchema.getSpecificationReference()));
            final PmSchema inputSchema = new PmSchema(entry.getValue().getMessageDataTopic().getName(), inputSchemaDto);
            cachedInputSchemas.put(entry.getKey(), inputSchema);
        }

    }

    /**
     * Returns a SchemaReference representing the source of the specified PMDefinition. If the profile is augmented, the returned reference will
     * represent the augmented schema. Otherwise, the parser schema reference will be returned. Augmented schema references are obtained directly from
     * the AAS via the AugmentationProvisioningService.
     *
     * @param profile      profile definition containing the context for this PMDefinition.
     * @param pmDefinition PM definition specifying the PM schema source.
     * @return schema reference representing the parser or augmented schema containing the PM.
     */
    @Override
    public String getSchemaReference(final ProfileDefinition profile, final PMDefinition pmDefinition) {

        final String parserSchemaReference = pmDefinition.getSource();

        if (Objects.isNull(profile.getAugmentation())) {
            // Profile is not augmented, return parser schema reference
            return parserSchemaReference;
        }
        final Optional<AugmentationDefinition> augmentationDefinition = this.augmentationDefinitionDAO.findById(profile.getAugmentation());
        if (augmentationDefinition.isEmpty()) {
            // return parser schema reference
            return parserSchemaReference;
        }
        // If profile is augmented, return augmented schema reference from schema mappings returned from AAS (via AugmentationProvisioningService) else return parser schema.
        final Map<String, String> schemaMappings = this.augmentationService.getSchemaMappings(profile.getAugmentation());

        final Optional<String> augmentedSchemaReference = Optional.ofNullable(schemaMappings.get(pmDefinition.getSource()));

        return augmentedSchemaReference.orElse(parserSchemaReference);
    }

    /**
     * Returns the schema represented by the specified schema reference. This method will attempt to resolve the Schema object from its internal cache.  If not present, it will read the
     * latest version of the schema from the Data Catalog and Schema Registry and cache it before returning it.
     *
     * @param schemaReference reference for the schema.
     * @return schema represented by the specified schema reference.
     */
    @Override
    public PmSchema getSchema(final String schemaReference) {
        if (Objects.nonNull(cachedInputSchemas.get(schemaReference))) {
            return cachedInputSchemas.get(schemaReference);
        } else {
            final MessageSchemaDTO messageSchemaDTO = this.dataCatalogService.getMessageSchema(schemaReference);
            if (Objects.isNull(messageSchemaDTO)) {
                final String errorMessage = "No message schema retrieved from Data Catalog for this schema reference: " + schemaReference;
                final CsacValidationException cve = new CsacValidationException(errorMessage);
                this.faultHandler.error(cve);
                throw cve;
            }

            final String specificationReference = messageSchemaDTO.getSpecificationReference();
            final PMSchemaDTO inputSchemaDto = this.schemaRegistryService.getSchemaLatest(fetchSubject(specificationReference));
            return new PmSchema(messageSchemaDTO.getMessageDataTopic().getName(), inputSchemaDto);
        }
    }

    protected Set<String> filteredSet(final Set<String> newSet, final Set<String> existingValues) {
        newSet.removeIf(existingValues::contains);
        return newSet;
    }

    protected String fetchSubject(final String specificationReference) {
        final Matcher matcher = SPEC_REF_PATTERN.matcher(specificationReference);
        if (!matcher.matches()) {
            final String errorMessage = "Message schema contains invalid specification reference format. Expected format "
                    + "'<subject>/<version>'. Actual value '" + specificationReference + "'.";
            final CsacValidationException cve = new CsacValidationException(errorMessage);
            this.faultHandler.error(cve);
            throw cve;
        }
        return matcher.group(1);
    }
}
