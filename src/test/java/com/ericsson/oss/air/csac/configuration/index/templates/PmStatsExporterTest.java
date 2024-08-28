/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.configuration.index.templates;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import com.ericsson.oss.air.csac.model.runtime.index.DeployedIndexDefinitionDto;
import com.ericsson.oss.air.csac.model.runtime.index.IndexWriterDto;
import com.ericsson.oss.air.util.codec.Codec;
import org.junit.jupiter.api.Test;

class PmStatsExporterTest {

    private static final Codec CODEC = new Codec();

    @Test
    void validateWriterTemplate() throws Exception {

        assertDoesNotThrow(() -> CODEC.readValue(PmStatsExporter.INDEX_WRITER_TEMPLATE, IndexWriterDto.class));
    }

    @Test
    void validateIndexTemplate() throws Exception {

        assertDoesNotThrow(() -> CODEC.readValue(PmStatsExporter.DEPLOYED_INDEX_DEFINITION_TEMPLATE, DeployedIndexDefinitionDto.class));
    }

}