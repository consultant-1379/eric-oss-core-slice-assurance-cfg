/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.configuration.index;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.ericsson.oss.air.csac.model.runtime.index.ContextFieldDto;
import com.ericsson.oss.air.csac.model.runtime.index.DeployedIndexDefinitionDto;
import com.ericsson.oss.air.csac.model.runtime.index.IndexSourceDto;
import com.ericsson.oss.air.csac.model.runtime.index.IndexWriterDto;
import com.ericsson.oss.air.csac.model.runtime.index.ValueFieldDto;
import com.ericsson.oss.air.exception.CsacValidationException;
import com.ericsson.oss.air.util.codec.Codec;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class IndexerTemplateConfigurationTest {

    private static final Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();

    private static final Codec CODEC = new Codec();

    private IndexerTemplateConfiguration testConfig;

    private final ContextFieldDto context1 = ContextFieldDto.builder()
            .name("context1")
            .build();

    private final ContextFieldDto context2 = ContextFieldDto.builder()
            .name("context2")
            .build();

    private final ValueFieldDto value1 = ValueFieldDto.builder()
            .name("value1")
            .build();

    private final ValueFieldDto value2 = ValueFieldDto.builder()
            .name("value2")
            .build();

    @BeforeEach
    void setUp() {

        this.testConfig = new IndexerTemplateConfiguration();
        this.testConfig.loadTemplates();
    }

    @Test
    void testLoadTemplates() {

        final IndexerTemplateConfiguration config = new IndexerTemplateConfiguration();
        config.loadTemplates();

        assertFalse(config.getIndexWriterTemplateMap().isEmpty());

        assertTrue(config.getIndexWriterTemplateMap().containsKey("pmstatsexporter"));

        assertFalse(config.getIndexDefinitionTemplateMap().isEmpty());

        assertTrue(config.getIndexDefinitionTemplateMap().containsKey("pmstatsexporter"));
    }

    @Test
    void testGetDefaultIndexWriterTemplate() {

        final Optional<IndexWriterDto> defaultWriter = this.testConfig.getIndexWriterTemplate();

        assertTrue(defaultWriter.isPresent());
    }

    @Test
    void testGetIndexWriterTemplate() {

        final Optional<IndexWriterDto> defaultWriter = this.testConfig.getIndexWriterTemplate(IndexSourceDto.IndexSourceType.PM_STATS_EXPORTER);

        assertTrue(defaultWriter.isPresent());
    }

    @Test
    void testGenerateValidWriterFromTemplate() {

        final IndexWriterDto actual = this.testConfig.getIndexWriterTemplate().get();

        actual.name("valid")
                .inputSchema("schema1")
                .contextFieldList(List.of(context1, context2))
                .valueFieldList(List.of(value1, value2));

        assertTrue(VALIDATOR.validate(actual).isEmpty());

        assertEquals(2, actual.infoFieldList().size());
    }

    @Test
    void testGetDefaultIndexDefinitionTemplate() {

        setIndexSource("pmstatsexporter");
        final DeployedIndexDefinitionDto defaultDefinition = this.testConfig.getIndexDefinitionTemplate();

        assertEquals("ESOA-2-Indexer", defaultDefinition.indexDefinitionName());
    }

    @Test
    void testGetIndexDefinitionTemplate_defaultTemplate() {

        final Optional<DeployedIndexDefinitionDto> defaultDefinition = this.testConfig.getIndexDefinitionTemplate(
                IndexSourceDto.IndexSourceType.PM_STATS_EXPORTER);

        assertTrue(defaultDefinition.isPresent());

        assertEquals("pm-stats-calc-handling-avro-scheduled", defaultDefinition.get().indexSource().getIndexSourceName());
    }

    @Test
    void testGenerateValidIndexDefinitionFromTemplate() throws Exception {

        final IndexWriterDto writer1 = this.testConfig.getIndexWriterTemplate().get();

        writer1.name("kpi_simple_snssai_15")
                .inputSchema("kpi_simple_snssai_15")
                .contextFieldList(List.of(context1, context2))
                .valueFieldList(List.of(value1, value2));

        final IndexWriterDto writer2 = this.testConfig.getIndexWriterTemplate().get();

        writer2.name("kpi_simple_snssai_nodefdn_15")
                .inputSchema("kpi_simple_snssai_nodefdn_15")
                .contextFieldList(List.of(context1, context2))
                .valueFieldList(List.of(value1, value2));

        setIndexSource("pmstatsexporter");
        final DeployedIndexDefinitionDto actual = this.testConfig.getIndexDefinitionTemplate();

        actual.indexWriter(writer1).indexWriter(writer2);

        assertTrue(VALIDATOR.validate(actual).isEmpty());
        assertNotEquals(writer1, writer2);
        assertEquals(2, actual.indexWriters().size());
    }

    @Test
    void testGetIndexDefinitionTemplate_updatedSourceName() {

        setIndexSource("pmstatsexporter");
        final DeployedIndexDefinitionDto defaultDefinition = this.testConfig.getIndexDefinitionTemplate();

        assertEquals("name", defaultDefinition.indexSource().getIndexSourceName());
    }

    @Test
    void testGetIndexDefinitionTemplate_nonExistedSource() {

        setIndexSource("test");

        assertThrows(CsacValidationException.class,
                () -> this.testConfig.getIndexDefinitionTemplate());
    }

    private void setIndexSource(final String type) {

        final IndexerTemplateConfiguration.SourceName sourceName = new IndexerTemplateConfiguration.SourceName();
        sourceName.setName("name");

        final Map<String, IndexerTemplateConfiguration.SourceName> source = new HashMap<>();
        source.put(type, sourceName);
        ReflectionTestUtils.setField(this.testConfig, "source", source);
    }

}