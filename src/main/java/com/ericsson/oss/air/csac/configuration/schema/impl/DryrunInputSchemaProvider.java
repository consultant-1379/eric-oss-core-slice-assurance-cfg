/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.configuration.schema.impl;

import java.util.Set;

import com.ericsson.oss.air.csac.configuration.schema.InputSchemaProvider;
import com.ericsson.oss.air.csac.model.PMDefinition;
import com.ericsson.oss.air.csac.model.ProfileDefinition;
import com.ericsson.oss.air.csac.model.pmschema.PMSchemaDTO;
import com.ericsson.oss.air.csac.model.pmschema.PmSchema;
import com.ericsson.oss.air.csac.model.pmschema.SchemaReference;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Dry run implementation of {@link InputSchemaProvider}.  This implementation will provide input and augmented schema references suitable for local
 * testing purposes.
 */
@Component
@RequiredArgsConstructor
@Profile({ "dry-run" })
@Slf4j
public class DryrunInputSchemaProvider implements InputSchemaProvider {

    private static final String KAFKA_TOPIC = "dry-run-kafka-topic";

    @Override
    public void prefetchInputSchemas(final Set<String> schemaReferenceSet) {
        log.info("Prefetching schemas for references: {}", schemaReferenceSet);
    }

    @Override
    public String getSchemaReference(final ProfileDefinition profile, final PMDefinition pmDefinition) {

        if (Strings.isBlank(profile.getAugmentation())) {
            return pmDefinition.getSource();
        }

        final SchemaReference reference = SchemaReference.of(pmDefinition.getSource());

        return SchemaReference.builder()
                .dataSpace(reference.getDataSpace())
                .dataCategory(reference.getDataCategory())
                .schemaId(profile.getAugmentation() + "_" + reference.getSchemaId())
                .build()
                .toString();
    }

    @Override
    public PmSchema getSchema(final String schemaReference) {

        return new PmSchema(KAFKA_TOPIC, PMSchemaDTO.builder().build());
    }
}
