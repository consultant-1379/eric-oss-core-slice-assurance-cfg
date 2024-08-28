/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.model.runtime.index;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;

import jakarta.validation.Validation;
import jakarta.validation.Validator;

import com.ericsson.oss.air.util.codec.Codec;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class DeployedIndexDefinitionDtoTest {

    private static final Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();

    private static final Codec CODEC = new Codec();

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

    private final InfoFieldDto info1 = InfoFieldDto.builder()
            .name("info1")
            .build();

    private final InfoFieldDto info2 = InfoFieldDto.builder()
            .name("info2")
            .build();

    private final IndexWriterDto writer1 = IndexWriterDto.builder()
            .name("writer")
            .inputSchema("schema")
            .contextFieldList(List.of(context1, context2))
            .valueFieldList(List.of(value1, value2))
            .infoFieldList(List.of(info1, info2))
            .build();

    private final IndexWriterDto invalidWriterMissingName = IndexWriterDto.builder()
            .inputSchema("schema")
            .contextFieldList(List.of(context1, context2))
            .valueFieldList(List.of(value1, value2))
            .infoFieldList(List.of(info1, info2))
            .build();

    private final IndexSourceDto source1 = IndexSourceDto.builder()
            .indexSourceName("source")
            .indexSourceType(IndexSourceDto.IndexSourceType.PM_STATS_EXPORTER)
            .indexSourceDescription("Index source description")
            .build();

    private final IndexSourceDto invalidSourceMissingName = IndexSourceDto.builder()
            .indexSourceType(IndexSourceDto.IndexSourceType.PM_STATS_EXPORTER)
            .indexSourceDescription("Index source description")
            .build();

    private final IndexTargetDto invalidTargetMissingName = IndexTargetDto.builder()
            .indexTargetDisplayName("Index Target")
            .indexTargetDescription("Index description")
            .build();

    private final IndexTargetDto target1 = IndexTargetDto.builder()
            .indexTargetName("target")
            .indexTargetDisplayName("Index Target")
            .indexTargetDescription("Index description")
            .build();

    private final String validIndexDefinitionStr = "{\n" +
            "  \"name\" : \"index\",\n" +
            "  \"description\" : \"Index description\",\n" +
            "  \"source\" : {\n" +
            "    \"name\" : \"source\",\n" +
            "    \"type\" : \"pmstatsexporter\",\n" +
            "    \"description\" : \"Index source description\"\n" +
            "  },\n" +
            "  \"target\" : {\n" +
            "    \"name\" : \"target\",\n" +
            "    \"displayName\" : \"Index Target\",\n" +
            "    \"description\" : \"Index description\"\n" +
            "  },\n" +
            "  \"writers\" : [ {\n" +
            "    \"name\" : \"writer\",\n" +
            "    \"inputSchema\" : \"schema\",\n" +
            "    \"context\" : [ {\n" +
            "      \"name\" : \"context1\",\n" +
            "      \"nameType\" : \"straight\"\n" +
            "    }, {\n" +
            "      \"name\" : \"context2\",\n" +
            "      \"nameType\" : \"straight\"\n" +
            "    } ],\n" +
            "    \"value\" : [ {\n" +
            "      \"name\" : \"value1\",\n" +
            "      \"type\" : \"float\"\n" +
            "    }, {\n" +
            "      \"name\" : \"value2\",\n" +
            "      \"type\" : \"float\"\n" +
            "    } ],\n" +
            "    \"info\" : [ {\n" +
            "      \"name\" : \"info1\",\n" +
            "      \"type\" : \"string\"\n" +
            "    }, {\n" +
            "      \"name\" : \"info2\",\n" +
            "      \"type\" : \"string\"\n" +
            "    } ]\n" +
            "  } ]\n" +
            "}";

    @Test
    void minimumIndexDefinition() throws Exception {

        final String expected = "{\"name\":\"index\",\"source\":{\"name\":\"source\",\"type\":\"pmstatsexporter\",\"description\":\"Index source description\"},\"target\":{\"name\":\"target\",\"displayName\":\"Index Target\",\"description\":\"Index description\"},\"writers\":[{\"name\":\"writer\",\"inputSchema\":\"schema\",\"context\":[{\"name\":\"context1\",\"nameType\":\"straight\"},{\"name\":\"context2\",\"nameType\":\"straight\"}],\"value\":[{\"name\":\"value1\",\"type\":\"float\"},{\"name\":\"value2\",\"type\":\"float\"}],\"info\":[{\"name\":\"info1\",\"type\":\"string\"},{\"name\":\"info2\",\"type\":\"string\"}]}]}";

        final DeployedIndexDefinitionDto actual = DeployedIndexDefinitionDto.builder()
                .indexDefinitionName("index")
                .indexSource(source1)
                .indexTarget(target1)
                .indexWriters(Set.of(writer1))
                .build();

        assertTrue(VALIDATOR.validate(actual).isEmpty());

        assertEquals(expected, CODEC.writeValueAsString(actual));
    }

    @Test
    void customIndexDefinition() throws Exception {

        final String expected = "{\"name\":\"index\",\"description\":\"Index description\",\"source\":{\"name\":\"source\",\"type\":\"pmstatsexporter\",\"description\":\"Index source description\"},\"target\":{\"name\":\"target\",\"displayName\":\"Index Target\",\"description\":\"Index description\"},\"writers\":[{\"name\":\"writer\",\"inputSchema\":\"schema\",\"context\":[{\"name\":\"context1\",\"nameType\":\"straight\"},{\"name\":\"context2\",\"nameType\":\"straight\"}],\"value\":[{\"name\":\"value1\",\"type\":\"float\"},{\"name\":\"value2\",\"type\":\"float\"}],\"info\":[{\"name\":\"info1\",\"type\":\"string\"},{\"name\":\"info2\",\"type\":\"string\"}]}]}";

        final DeployedIndexDefinitionDto actual = DeployedIndexDefinitionDto.builder()
                .indexDefinitionName("index")
                .indexDefinitionDescription("Index description")
                .indexSource(source1)
                .indexTarget(target1)
                .indexWriters(Set.of(writer1))
                .build();

        assertTrue(VALIDATOR.validate(actual).isEmpty());

        assertEquals(expected, CODEC.writeValueAsString(actual));
    }

    @Test
    void deserializedValidIndexDefinition() throws Exception {

        final DeployedIndexDefinitionDto expected = DeployedIndexDefinitionDto.builder()
                .indexDefinitionName("index")
                .indexDefinitionDescription("Index description")
                .indexSource(source1)
                .indexTarget(target1)
                .indexWriters(Set.of(writer1))
                .build();

        final DeployedIndexDefinitionDto actual = CODEC.readValue(validIndexDefinitionStr, DeployedIndexDefinitionDto.class);

        assertTrue(VALIDATOR.validate(actual).isEmpty());

        assertEquals(expected, actual);
    }

    @Test
    void validIndexDefinition_duplicateWriters() throws Exception {

        final DeployedIndexDefinitionDto actual = DeployedIndexDefinitionDto.builder()
                .indexDefinitionDescription("Index description")
                .indexSource(source1)
                .indexTarget(target1)
                .build();

        actual.indexWriter(writer1).indexWriter(writer1);

        assertEquals(1, actual.indexWriters().size());
    }

    @Test
    void invalidIndexDefinition_missingName() throws Exception {

        final DeployedIndexDefinitionDto actual = DeployedIndexDefinitionDto.builder()
                .indexDefinitionDescription("Index description")
                .indexSource(source1)
                .indexTarget(target1)
                .indexWriters(Set.of(writer1))
                .build();

        assertFalse(VALIDATOR.validate(actual).isEmpty());
    }

    @Test
    void invalidIndexDefinition_emptyName() throws Exception {

        final DeployedIndexDefinitionDto actual = DeployedIndexDefinitionDto.builder()
                .indexDefinitionName("")
                .indexDefinitionDescription("Index description")
                .indexSource(source1)
                .indexTarget(target1)
                .indexWriters(Set.of(writer1))
                .build();

        assertFalse(VALIDATOR.validate(actual).isEmpty());
    }

    @Test
    void invalidIndexDefinition_nullName() throws Exception {

        final DeployedIndexDefinitionDto actual = DeployedIndexDefinitionDto.builder()
                .indexDefinitionName(null)
                .indexDefinitionDescription("Index description")
                .indexSource(source1)
                .indexTarget(target1)
                .indexWriters(Set.of(writer1))
                .build();

        assertFalse(VALIDATOR.validate(actual).isEmpty());
    }

    @Test
    void invalidIndexDefinition_missingSource() throws Exception {

        final DeployedIndexDefinitionDto actual = DeployedIndexDefinitionDto.builder()
                .indexDefinitionName("index")
                .indexDefinitionDescription("Index description")
                .indexTarget(target1)
                .indexWriters(Set.of(writer1))
                .build();

        assertFalse(VALIDATOR.validate(actual).isEmpty());
    }

    @Test
    void invalidIndexDefinition_invalidSource() throws Exception {

        assertFalse(VALIDATOR.validate(invalidSourceMissingName).isEmpty());

        final DeployedIndexDefinitionDto actual = DeployedIndexDefinitionDto.builder()
                .indexDefinitionName("index")
                .indexDefinitionDescription("Index description")
                .indexSource(invalidSourceMissingName)
                .indexTarget(target1)
                .indexWriters(Set.of(writer1))
                .build();

        assertFalse(VALIDATOR.validate(actual).isEmpty());
    }

    @Test
    void invalidIndexDefinition_invalidTarget() throws Exception {

        assertFalse(VALIDATOR.validate(invalidTargetMissingName).isEmpty());

        final DeployedIndexDefinitionDto actual = DeployedIndexDefinitionDto.builder()
                .indexDefinitionName("index")
                .indexDefinitionDescription("Index description")
                .indexSource(source1)
                .indexTarget(invalidTargetMissingName)
                .indexWriters(Set.of(writer1))
                .build();

        assertFalse(VALIDATOR.validate(actual).isEmpty());
    }

    @Test
    void invalidIndexDefinition_emptyWriters() throws Exception {

        final DeployedIndexDefinitionDto actual = DeployedIndexDefinitionDto.builder()
                .indexDefinitionName("index")
                .indexDefinitionDescription("Index description")
                .indexSource(source1)
                .indexTarget(target1)
                .indexWriters(Set.of())
                .build();

        assertFalse(VALIDATOR.validate(actual).isEmpty());
    }

    @Test
    void invalidIndexDefinition_missingWriters() throws Exception {

        final DeployedIndexDefinitionDto actual = DeployedIndexDefinitionDto.builder()
                .indexDefinitionName("index")
                .indexDefinitionDescription("Index description")
                .indexSource(source1)
                .indexTarget(target1)
                .build();

        assertFalse(VALIDATOR.validate(actual).isEmpty());
    }

    @Test
    void invalidIndexDefinition_invalidWriters() throws Exception {

        final DeployedIndexDefinitionDto actual = DeployedIndexDefinitionDto.builder()
                .indexDefinitionName("index")
                .indexDefinitionDescription("Index description")
                .indexSource(source1)
                .indexTarget(target1)
                .indexWriters(Set.of(writer1, invalidWriterMissingName))
                .build();

        assertFalse(VALIDATOR.validate(actual).isEmpty());
    }

    @Disabled("This test generates the JSON for the DeployedIndexDefinitionDto template.  Leave it here in case the template needs to be updated or a new template created.")
    @Test
    void generateDeployedIndexDefinitionTemplate() throws Exception {

        final IndexSourceDto templateSource = IndexSourceDto.builder()
                .indexSourceName("pm-stats-calc-handling-avro-scheduled")
                .indexSourceDescription("ESOA 2.0 Indexer source specificiation")
                .indexSourceType(IndexSourceDto.IndexSourceType.PM_STATS_EXPORTER)
                .build();

        final IndexTargetDto templateTarget = IndexTargetDto.builder()
                .indexTargetName("index-oob")
                .indexTargetDisplayName("ESOA 2.0 KPI Index")
                .indexTargetDescription("Default index for all out-of-box (OOB) KPIs")
                .build();

        final DeployedIndexDefinitionDto template = DeployedIndexDefinitionDto.builder()
                .indexDefinitionName("ESOA-2-Indexer")
                .indexDefinitionDescription(
                        "ESOA 2.0 Indexer specification. This indexer will be used by default to index all exportable runtime KPIs.")
                .indexSource(templateSource)
                .indexTarget(templateTarget)
                .build();

        System.out.println(CODEC.writeValueAsStringPretty(template));
    }

}