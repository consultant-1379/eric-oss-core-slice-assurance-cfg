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

import jakarta.validation.Validation;
import jakarta.validation.Validator;

import com.ericsson.oss.air.util.codec.Codec;
import org.junit.jupiter.api.Test;

class IndexTargetDtoTest {

    private static final Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();

    private static final Codec CODEC = new Codec();

    @Test
    void minimumIndexTarget() throws Exception {

        final String expected = "{\"name\":\"target\"}";

        final IndexTargetDto actual = IndexTargetDto.builder()
                .indexTargetName("target")
                .build();

        assertTrue(VALIDATOR.validate(actual).isEmpty());

        assertEquals(expected, new Codec().writeValueAsString(actual));
    }

    @Test
    void customIndexTarget() throws Exception {

        final String expected = "{\"name\":\"target\",\"displayName\":\"Index Target\",\"description\":\"Index description\"}";

        final IndexTargetDto actual = IndexTargetDto.builder()
                .indexTargetName("target")
                .indexTargetDisplayName("Index Target")
                .indexTargetDescription("Index description")
                .build();

        assertTrue(VALIDATOR.validate(actual).isEmpty());

        assertEquals(expected, new Codec().writeValueAsString(actual));

    }

    @Test
    void deserializeValidIndexTarget() throws Exception {

        final String expectedStr = "{\"name\":\"target\",\"displayName\":\"Index Target\",\"description\":\"Index description\"}";

        final IndexTargetDto actual = CODEC.readValue(expectedStr, IndexTargetDto.class);

        final IndexTargetDto expected = IndexTargetDto.builder()
                .indexTargetName("target")
                .indexTargetDisplayName("Index Target")
                .indexTargetDescription("Index description")
                .build();

        assertTrue(VALIDATOR.validate(actual).isEmpty());

        assertEquals(expected, actual);

    }

    @Test
    void deserializeInvalidIndexTarget_emptyName() throws Exception {

        final String expectedStr = "{\"name\":\"\",\"displayName\":\"Index Target\",\"description\":\"Index description\"}";

        final IndexTargetDto actual = CODEC.readValue(expectedStr, IndexTargetDto.class);

        assertFalse(VALIDATOR.validate(actual).isEmpty());

    }

    @Test
    void deserializeInvalidIndexTarget_missingName() throws Exception {

        final String expectedStr = "{\"displayName\":\"Index Target\",\"description\":\"Index description\"}";

        final IndexTargetDto actual = CODEC.readValue(expectedStr, IndexTargetDto.class);

        assertFalse(VALIDATOR.validate(actual).isEmpty());

    }

}