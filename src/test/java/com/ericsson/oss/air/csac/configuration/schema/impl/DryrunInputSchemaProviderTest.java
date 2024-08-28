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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Set;

import com.ericsson.oss.air.csac.model.PMDefinition;
import com.ericsson.oss.air.csac.model.ProfileDefinition;
import com.ericsson.oss.air.csac.model.pmschema.PmSchema;
import com.ericsson.oss.air.csac.model.pmschema.SchemaReference;
import org.junit.jupiter.api.Test;

class DryrunInputSchemaProviderTest {

    private final DryrunInputSchemaProvider testProvider = new DryrunInputSchemaProvider();

    @Test
    void prefetchInputSchemas() {

        assertDoesNotThrow(() -> this.testProvider.prefetchInputSchemas(Set.of()));
    }

    @Test
    void getSchemaReference_unaugmented() {

        final ProfileDefinition unaugmentedProfile = ProfileDefinition.builder()
                .name("testProfile")
                .build();

        final PMDefinition pmDefinition = PMDefinition.builder()
                .source("5G|PM_COUNTERS|Schema_1")
                .build();

        final SchemaReference actual = SchemaReference.of("5G|PM_COUNTERS|Schema_1");

        assertEquals(actual.toString(), this.testProvider.getSchemaReference(unaugmentedProfile, pmDefinition));
    }

    @Test
    void getSchemaReference_augmented() {

        final ProfileDefinition augmentedProfile = ProfileDefinition.builder()
                .name("testProfile")
                .augmentation("ardq")
                .build();

        final PMDefinition pmDefinition = PMDefinition.builder()
                .source("5G|PM_COUNTERS|Schema_1")
                .build();

        final SchemaReference actual = SchemaReference.of("5G|PM_COUNTERS|ardq_Schema_1");

        assertEquals(actual.toString(), this.testProvider.getSchemaReference(augmentedProfile, pmDefinition));
    }

    @Test
    void getSchema() {

        final PmSchema pmSchema = this.testProvider.getSchema("5G|PM_COUNTERS|Schema_1");

        assertNotNull(pmSchema);
        assertNotNull(pmSchema.kafkaTopic());
        assertNotNull(pmSchema.pmSchemaDTO());
    }
}