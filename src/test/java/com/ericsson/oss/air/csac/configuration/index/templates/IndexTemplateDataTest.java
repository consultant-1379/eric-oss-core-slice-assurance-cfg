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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.Test;

class IndexTemplateDataTest {

    @Test
    void getIndexTemplateSources() {

        assertEquals(1, IndexTemplateData.getIndexTemplateSources().size());
        assertTrue(IndexTemplateData.getIndexTemplateSources().contains(PmStatsExporter.SOURCE));
    }

    @Test
    void getIndexTemplate() {

        final Optional<String> writerTemplate = IndexTemplateData.getIndexTemplate(PmStatsExporter.SOURCE,
                IndexTemplateData.IndexTemplateHolder.IndexTemplateType.INDEX_WRITER);

        assertTrue(writerTemplate.isPresent());

        final Optional<String> indexTemplate = IndexTemplateData.getIndexTemplate(PmStatsExporter.SOURCE,
                IndexTemplateData.IndexTemplateHolder.IndexTemplateType.INDEX_DEFINITION);

        assertTrue(indexTemplate.isPresent());
    }

    @Test
    void getIndexTemplate_unknownSource() {

        final Optional<String> writerTemplate = IndexTemplateData.getIndexTemplate("not_a_source",
                IndexTemplateData.IndexTemplateHolder.IndexTemplateType.INDEX_WRITER);

        assertTrue(writerTemplate.isEmpty());
    }
}