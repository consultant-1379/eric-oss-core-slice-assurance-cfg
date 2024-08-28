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

import jakarta.validation.Validation;
import jakarta.validation.Validator;

import com.ericsson.oss.air.util.codec.Codec;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;

class ValueFieldDtoTest {

    private static final Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();

    private static final Codec CODEC = new Codec();

    @Test
    void defaultValueField() throws Exception {

        final String expected = "{\"name\":\"value\",\"type\":\"float\"}";

        final ValueFieldDto actual = ValueFieldDto.builder()
                .name("value")
                .build();

        assertTrue(VALIDATOR.validate(actual).isEmpty());

        assertEquals(ValueFieldDto.ValueFieldType.FLOAT, actual.getType());
        assertEquals(expected, new Codec().writeValueAsString(actual));

    }

    @Test
    void customValueField() throws Exception {

        final String expected = "{\"name\":\"value\",\"displayName\":\"Value Field\",\"unit\":\"unit\",\"type\":\"integer\",\"recordName\":\"value_field\",\"description\":\"Value field description\"}";

        final ValueFieldDto actual = ValueFieldDto.builder()
                .name("value")
                .valueFieldDisplayName("Value Field")
                .unit("unit")
                .recordName("value_field")
                .type(ValueFieldDto.ValueFieldType.INTEGER)
                .description("Value field description")
                .build();

        assertTrue(VALIDATOR.validate(actual).isEmpty());

        assertEquals(ValueFieldDto.ValueFieldType.INTEGER, actual.getType());
        assertEquals(expected, new Codec().writeValueAsString(actual));

    }

    @Test
    void deserializeValidValueField() throws Exception {

        final String actualStr = "{\"name\":\"value\",\"displayName\":\"Value Field\",\"unit\":\"unit\",\"type\":\"integer\",\"recordName\":\"value_field\",\"description\":\"Value field description\"}";

        final ValueFieldDto actual = CODEC.readValue(actualStr, ValueFieldDto.class);

        final ValueFieldDto expected = ValueFieldDto.builder()
                .name("value")
                .valueFieldDisplayName("Value Field")
                .unit("unit")
                .recordName("value_field")
                .type(ValueFieldDto.ValueFieldType.INTEGER)
                .description("Value field description")
                .build();

        assertTrue(VALIDATOR.validate(actual).isEmpty());

        assertEquals(expected, actual);
    }

    @Test
    void deserializeInvalidValueField_invalidType() throws Exception {

        final String actualStr = "{\"name\":\"value\",\"displayName\":\"Value Field\",\"unit\":\"unit\",\"type\":\"notAType\",\"recordName\":\"value_field\",\"description\":\"Value field description\"}";

        assertThrows(JsonProcessingException.class, () -> CODEC.readValue(actualStr, ValueFieldDto.class));
    }

    @Test
    void deserializeInvalidValueField_missingName() throws Exception {

        final String actualStr = "{\"displayName\":\"Value Field\",\"unit\":\"unit\",\"type\":\"integer\",\"recordName\":\"value_field\",\"description\":\"Value field description\"}";

        final ValueFieldDto actual = CODEC.readValue(actualStr, ValueFieldDto.class);

        assertFalse(VALIDATOR.validate(actual).isEmpty());
    }

}