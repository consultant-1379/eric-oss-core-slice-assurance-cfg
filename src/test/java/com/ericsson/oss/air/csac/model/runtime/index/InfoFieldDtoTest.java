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

class InfoFieldDtoTest {

    private static final Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();

    private static final Codec CODEC = new Codec();

    @Test
    void defaultInfoField() throws Exception {

        final String expected = "{\"name\":\"info\",\"type\":\"string\"}";

        final InfoFieldDto actual = InfoFieldDto.builder()
                .name("info")
                .build();

        assertTrue(VALIDATOR.validate(actual).isEmpty());

        assertEquals(InfoFieldDto.InfoFieldType.STRING, actual.getType());
        assertEquals(expected, new Codec().writeValueAsString(actual));
    }

    @Test
    void customInfoTypeField() throws Exception {

        final String expected = "{\"name\":\"info\",\"type\":\"time\"}";

        final InfoFieldDto actual = InfoFieldDto.builder()
                .name("info")
                .type(InfoFieldDto.InfoFieldType.TIME)
                .build();

        assertTrue(VALIDATOR.validate(actual).isEmpty());

        assertEquals(InfoFieldDto.InfoFieldType.TIME, actual.getType());
        assertEquals(expected, new Codec().writeValueAsString(actual));
    }

    @Test
    void deserializeValidInfoField() throws Exception {

        final String actualStr = "{\"name\":\"info\",\"displayName\":\"Info Field\",\"type\":\"time\",\"recordName\":\"info_field\",\"description\":\"An info field\"}";

        final InfoFieldDto actual = CODEC.readValue(actualStr, InfoFieldDto.class);

        final InfoFieldDto expected = InfoFieldDto.builder()
                .name("info")
                .type(InfoFieldDto.InfoFieldType.TIME)
                .infoFieldDisplayName("Info Field")
                .recordName("info_field")
                .description("An info field")
                .build();

        assertTrue(VALIDATOR.validate(actual).isEmpty());

        assertEquals(expected, actual);
    }

    @Test
    void deserializeInvalidInfoField_invalidType() throws Exception {

        final String expectedStr = "{\"name\":\"info\",\"displayName\":\"Info Field\",\"type\":\"notatype\",\"recordName\":\"info_field\",\"description\":\"An info field\"}";

        assertThrows(JsonProcessingException.class, () -> CODEC.readValue(expectedStr, InfoFieldDto.class));
    }

    @Test
    void deserializeInvalidInfoField_missingName() throws Exception {

        final String actualStr = "{\"displayName\":\"Info Field\",\"type\":\"time\",\"recordName\":\"info_field\",\"description\":\"An info field\"}";

        final InfoFieldDto actual = CODEC.readValue(actualStr, InfoFieldDto.class);

        assertFalse(VALIDATOR.validate(actual).isEmpty());
    }

}