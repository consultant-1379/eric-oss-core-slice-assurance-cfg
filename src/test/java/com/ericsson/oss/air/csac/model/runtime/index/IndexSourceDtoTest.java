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

class IndexSourceDtoTest {

    private static final Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();

    private static final Codec CODEC = new Codec();

    @Test
    void minimumIndexSource() throws Exception {

        final String expected = "{\"name\":\"source\",\"type\":\"pmstatsexporter\"}";

        final IndexSourceDto actual = IndexSourceDto.builder()
                .indexSourceName("source")
                .build();

        assertTrue(VALIDATOR.validate(actual).isEmpty());

        assertEquals(expected, new Codec().writeValueAsString(actual));
    }

    @Test
    void customIndexSource() throws Exception {

        final String expected = "{\"name\":\"source\",\"type\":\"pmstatsexporter\",\"description\":\"Index source description\"}";

        final IndexSourceDto actual = IndexSourceDto.builder()
                .indexSourceName("source")
                .indexSourceType(IndexSourceDto.IndexSourceType.PM_STATS_EXPORTER)
                .indexSourceDescription("Index source description")
                .build();

        assertTrue(VALIDATOR.validate(actual).isEmpty());

        assertEquals(expected, new Codec().writeValueAsString(actual));
    }

    @Test
    void deserializeValidIndexSource() throws Exception {

        final String actualStr = "{\"name\":\"source\",\"type\":\"pmstatsexporter\",\"description\":\"Index source description\"}";

        final IndexSourceDto actual = CODEC.readValue(actualStr, IndexSourceDto.class);

        final IndexSourceDto expected = IndexSourceDto.builder()
                .indexSourceName("source")
                .indexSourceType(IndexSourceDto.IndexSourceType.PM_STATS_EXPORTER)
                .indexSourceDescription("Index source description")
                .build();

        assertTrue(VALIDATOR.validate(actual).isEmpty());

        assertEquals(expected, actual);
    }

    @Test
    void deserializeInvalidIndexSource_emptyName() throws Exception {

        final String actualStr = "{\"name\":\"\",\"type\":\"pmstatsexporter\",\"description\":\"Index source description\"}";

        final IndexSourceDto actual = CODEC.readValue(actualStr, IndexSourceDto.class);

        assertFalse(VALIDATOR.validate(actual).isEmpty());
    }

    @Test
    void deserializeInvalidIndexSource_missingName() throws Exception {

        final String actualStr = "{\"type\":\"pmstatsexporter\",\"description\":\"Index source description\"}";

        final IndexSourceDto actual = CODEC.readValue(actualStr, IndexSourceDto.class);

        assertFalse(VALIDATOR.validate(actual).isEmpty());
    }

}