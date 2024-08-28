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

class ContextFieldDtoTest {

    private static final Codec CODEC = new Codec();

    private static final Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void defaultContextField() throws Exception {

        final String expected = "{\"name\":\"context\",\"nameType\":\"straight\"}";

        final ContextFieldDto actual = ContextFieldDto.builder()
                .name("context")
                .build();

        assertTrue(VALIDATOR.validate(actual).isEmpty());

        assertEquals(expected, CODEC.writeValueAsString(actual));
    }

    @Test
    void customContextField() throws Exception {

        final String expected = "{\"name\":\"context\",\"displayName\":\"Context Field\",\"nameType\":\"colonSeparated\",\"recordName\":\"context_field\",\"description\":\"Context field description\"}";

        final ContextFieldDto actual = ContextFieldDto.builder()
                .name("context")
                .contextFieldDisplayName("Context Field")
                .nameType(ContextFieldDto.ContextNameType.COLON_SEPARATED)
                .recordName("context_field")
                .description("Context field description")
                .build();

        assertTrue(VALIDATOR.validate(actual).isEmpty());

        assertEquals(expected, CODEC.writeValueAsString(actual));
    }

    @Test
    void deserializeValidContextField() throws Exception {

        final String actualStr = "{\"name\":\"context\",\"displayName\":\"Context Field\",\"nameType\":\"colonSeparated\",\"recordName\":\"context_field\",\"description\":\"Context field description\"}";

        final ContextFieldDto actual = CODEC.readValue(actualStr, ContextFieldDto.class);

        final ContextFieldDto expected = ContextFieldDto.builder()
                .name("context")
                .contextFieldDisplayName("Context Field")
                .nameType(ContextFieldDto.ContextNameType.COLON_SEPARATED)
                .recordName("context_field")
                .description("Context field description")
                .build();

        assertTrue(VALIDATOR.validate(actual).isEmpty());

        assertEquals(expected, actual);
    }

    @Test
    void deserializeInvalidContextField_missingName() throws Exception {

        final String actualStr = "{\"displayName\":\"Context Field\",\"nameType\":\"colonSeparated\",\"recordName\":\"context_field\",\"description\":\"Context field description\"}";

        final ContextFieldDto actual = CODEC.readValue(actualStr, ContextFieldDto.class);

        assertFalse(VALIDATOR.validate(actual).isEmpty());
    }

    @Test
    void deserializeInvalidContextField_invalidNameType() throws Exception {

        final String actualStr = "{\"name\":\"context\",\"displayName\":\"Context Field\",\"nameType\":\"notAType\",\"recordName\":\"context_field\",\"description\":\"Context field description\"}";

        assertThrows(JsonProcessingException.class, () -> CODEC.readValue(actualStr, ContextFieldDto.class));
    }
}