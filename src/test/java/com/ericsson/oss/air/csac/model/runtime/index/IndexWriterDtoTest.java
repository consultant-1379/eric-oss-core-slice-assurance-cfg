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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import jakarta.validation.Validation;
import jakarta.validation.Validator;

import com.ericsson.oss.air.util.codec.Codec;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class IndexWriterDtoTest {

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

    @Test
    void minimumWriterDto() throws Exception {

        final String expected = "{\"name\":\"writer\",\"inputSchema\":\"schema\",\"context\":[{\"name\":\"context1\",\"nameType\":\"straight\"},{\"name\":\"context2\",\"nameType\":\"straight\"}],\"value\":[{\"name\":\"value1\",\"type\":\"float\"},{\"name\":\"value2\",\"type\":\"float\"}]}";

        final IndexWriterDto actual = IndexWriterDto.builder()
                .name("writer")
                .inputSchema("schema")
                .contextFieldList(List.of(context1, context2))
                .valueFieldList(List.of(value1, value2))
                .build();

        assertTrue(actual.infoFieldList().isEmpty());
        assertTrue(VALIDATOR.validate(actual).isEmpty());

        assertEquals(expected, CODEC.writeValueAsString(actual));
    }

    @Test
    void validWriterDto() throws Exception {

        final String expected = "{\"name\":\"writer\",\"inputSchema\":\"schema\",\"context\":[{\"name\":\"context1\",\"nameType\":\"straight\"},{\"name\":\"context2\",\"nameType\":\"straight\"}],\"value\":[{\"name\":\"value1\",\"type\":\"float\"},{\"name\":\"value2\",\"type\":\"float\"}],\"info\":[{\"name\":\"info1\",\"type\":\"string\"},{\"name\":\"info2\",\"type\":\"string\"}]}";

        final IndexWriterDto actual = IndexWriterDto.builder()
                .name("writer")
                .inputSchema("schema")
                .contextFieldList(List.of(context1, context2))
                .valueFieldList(List.of(value1, value2))
                .infoFieldList(List.of(info1, info2))
                .build();

        assertTrue(VALIDATOR.validate(actual).isEmpty());

        assertEquals(expected, CODEC.writeValueAsString(actual));
    }

    @Test
    void deserializeValidWriter() throws Exception {

        final String actualStr = "{\"name\":\"writer\",\"inputSchema\":\"schema\",\"context\":[{\"name\":\"context1\",\"nameType\":\"straight\"},{\"name\":\"context2\",\"nameType\":\"straight\"}],\"value\":[{\"name\":\"value1\",\"type\":\"float\"},{\"name\":\"value2\",\"type\":\"float\"}],\"info\":[{\"name\":\"info1\",\"type\":\"string\"},{\"name\":\"info2\",\"type\":\"string\"}]}";

        final IndexWriterDto actual = CODEC.readValue(actualStr, IndexWriterDto.class);

        final IndexWriterDto expected = IndexWriterDto.builder()
                .name("writer")
                .inputSchema("schema")
                .contextFieldList(List.of(context1, context2))
                .valueFieldList(List.of(value1, value2))
                .infoFieldList(List.of(info1, info2))
                .build();

        assertTrue(VALIDATOR.validate(actual).isEmpty());

        assertEquals(expected, actual);

    }

    @Test
    void deserializeInvalidWriter_missingName() throws Exception {

        final String actualStr = "{\"inputSchema\":\"schema\",\"context\":[{\"name\":\"context1\",\"nameType\":\"straight\"},{\"name\":\"context2\",\"nameType\":\"straight\"}],\"value\":[{\"name\":\"value1\",\"type\":\"float\"},{\"name\":\"value2\",\"type\":\"float\"}],\"info\":[{\"name\":\"info1\",\"type\":\"string\"},{\"name\":\"info2\",\"type\":\"string\"}]}";

        final IndexWriterDto actual = CODEC.readValue(actualStr, IndexWriterDto.class);

        assertFalse(VALIDATOR.validate(actual).isEmpty());
    }

    @Test
    void deserializeInvalidWriter_missingSchema() throws Exception {

        final String actualStr = "{\"name\":\"writer\",\"context\":[{\"name\":\"context1\",\"nameType\":\"straight\"},{\"name\":\"context2\",\"nameType\":\"straight\"}],\"value\":[{\"name\":\"value1\",\"type\":\"float\"},{\"name\":\"value2\",\"type\":\"float\"}],\"info\":[{\"name\":\"info1\",\"type\":\"string\"},{\"name\":\"info2\",\"type\":\"string\"}]}";

        final IndexWriterDto actual = CODEC.readValue(actualStr, IndexWriterDto.class);

        assertFalse(VALIDATOR.validate(actual).isEmpty());
    }

    @Test
    void deserializeInvalidWriter_blankName() throws Exception {

        final String actualStr = "{\"name\":\"\",\"inputSchema\":\"schema\",\"context\":[{\"name\":\"context1\",\"nameType\":\"straight\"},{\"name\":\"context2\",\"nameType\":\"straight\"}],\"value\":[{\"name\":\"value1\",\"type\":\"float\"},{\"name\":\"value2\",\"type\":\"float\"}],\"info\":[{\"name\":\"info1\",\"type\":\"string\"},{\"name\":\"info2\",\"type\":\"string\"}]}";

        final IndexWriterDto actual = CODEC.readValue(actualStr, IndexWriterDto.class);

        assertFalse(VALIDATOR.validate(actual).isEmpty());
    }

    @Test
    void deserializeInvalidWriter_blankSchema() throws Exception {

        final String actualStr = "{\"name\":\"writer\",\"inputSchema\":\"\",\"context\":[{\"name\":\"context1\",\"nameType\":\"straight\"},{\"name\":\"context2\",\"nameType\":\"straight\"}],\"value\":[{\"name\":\"value1\",\"type\":\"float\"},{\"name\":\"value2\",\"type\":\"float\"}],\"info\":[{\"name\":\"info1\",\"type\":\"string\"},{\"name\":\"info2\",\"type\":\"string\"}]}";

        final IndexWriterDto actual = CODEC.readValue(actualStr, IndexWriterDto.class);

        assertFalse(VALIDATOR.validate(actual).isEmpty());
    }

    @Test
    void deserializeInvalidWriter_emptyContextFields() throws Exception {

        final String actualStr = "{\"name\":\"writer\",\"inputSchema\":\"schema\",\"context\":[],\"value\":[{\"name\":\"value1\",\"type\":\"float\"},{\"name\":\"value2\",\"type\":\"float\"}],\"info\":[{\"name\":\"info1\",\"type\":\"string\"},{\"name\":\"info2\",\"type\":\"string\"}]}";

        final IndexWriterDto actual = CODEC.readValue(actualStr, IndexWriterDto.class);

        assertFalse(VALIDATOR.validate(actual).isEmpty());
    }

    @Test
    void deserializeInvalidWriter_emptyValueFields() throws Exception {

        final String actualStr = "{\"name\":\"writer\",\"inputSchema\":\"schema\",\"context\":[{\"name\":\"context1\",\"nameType\":\"straight\"},{\"name\":\"context2\",\"nameType\":\"straight\"}],\"value\":[],\"info\":[{\"name\":\"info1\",\"type\":\"string\"},{\"name\":\"info2\",\"type\":\"string\"}]}";

        final IndexWriterDto actual = CODEC.readValue(actualStr, IndexWriterDto.class);

        assertFalse(VALIDATOR.validate(actual).isEmpty());
    }

    @Test
    void deserializeInvalidWriter_invalidContextField() throws Exception {

        final String actualStr = "{\"name\":\"writer\",\"inputSchema\":\"schema\",\"context\":[{\"name\":\"context1\",\"nameType\":\"notAType\"},{\"name\":\"context2\",\"nameType\":\"straight\"}],\"value\":[{\"name\":\"value1\",\"type\":\"float\"},{\"name\":\"value2\",\"type\":\"float\"}],\"info\":[{\"name\":\"info1\",\"type\":\"string\"},{\"name\":\"info2\",\"type\":\"string\"}]}";

        assertThrows(JsonProcessingException.class, () -> CODEC.readValue(actualStr, IndexWriterDto.class));

    }

    @Test
    void deserializeInvalidWriter_invalidValueField() throws Exception {

        final String actualStr = "{\"name\":\"writer\",\"inputSchema\":\"schema\",\"context\":[{\"name\":\"context1\",\"nameType\":\"straight\"},{\"name\":\"context2\",\"nameType\":\"straight\"}],\"value\":[{\"name\":\"value1\",\"type\":\"notAType\"},{\"name\":\"value2\",\"type\":\"float\"}],\"info\":[{\"name\":\"info1\",\"type\":\"string\"},{\"name\":\"info2\",\"type\":\"string\"}]}";

        assertThrows(JsonProcessingException.class, () -> CODEC.readValue(actualStr, IndexWriterDto.class));

    }

    @Test
    void deserializeInvalidWriter_invalidInfoField() throws Exception {

        final String actualStr = "{\"name\":\"writer\",\"inputSchema\":\"schema\",\"context\":[{\"name\":\"context1\",\"nameType\":\"straight\"},{\"name\":\"context2\",\"nameType\":\"straight\"}],\"value\":[{\"name\":\"value1\",\"type\":\"float\"},{\"name\":\"value2\",\"type\":\"float\"}],\"info\":[{\"name\":\"info1\",\"type\":\"string\"},{\"name\":\"info2\",\"type\":\"notAType\"}]}";

        assertThrows(JsonProcessingException.class, () -> CODEC.readValue(actualStr, IndexWriterDto.class));

    }

    @Disabled("This test generates the JSON for the IndexWriterDto template.  Leave it here in case the template needs to be updated or a new template created.")
    @Test
    void generateWriterTemplate() throws Exception {

        final InfoFieldDto beginTimestamp = InfoFieldDto.builder()
                .name("begin_timestamp")
                .type(InfoFieldDto.InfoFieldType.TIME)
                .recordName("aggregation_begin_time")
                .description("Aggregation period start timestamp")
                .build();

        final InfoFieldDto endTimestamp = InfoFieldDto.builder()
                .name("end_timestamp")
                .type(InfoFieldDto.InfoFieldType.TIME)
                .recordName("aggregation_end_time")
                .description("Aggregation period end timestamp")
                .build();

        final IndexWriterDto actual = IndexWriterDto.builder()
                .infoFieldList(List.of(beginTimestamp, endTimestamp))
                .build();

        System.out.println(CODEC.writeValueAsStringPretty(actual));

    }
}