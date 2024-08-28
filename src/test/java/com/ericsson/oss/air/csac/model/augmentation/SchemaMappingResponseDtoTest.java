/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.model.augmentation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;

import com.ericsson.oss.air.util.codec.Codec;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;

public class SchemaMappingResponseDtoTest {

    private Codec codec = new Codec(Validation.buildDefaultValidatorFactory().getValidator());

    @Test
    void readValue_SchemaMappingDto_ReturnsSchemaMappingDto() throws JsonProcessingException {

        final SchemaMappingResponseDto schemaMappingResponseDto = this.codec.withValidation()
                .readValue("{\"inputSchema\":\"foo\",\"outputSchema\":\"bar\"}",
                        SchemaMappingResponseDto.class);

        assertNotNull(schemaMappingResponseDto);
        assertEquals("bar", schemaMappingResponseDto.getOutputSchema());
        assertEquals("foo", schemaMappingResponseDto.getInputSchema());
    }


    @Test
    void readValue_SchemaMappingWithBlankArdqUrl_ThrowsException() {

        assertThrows(ConstraintViolationException.class,
                () -> this.codec.withValidation().readValue(
                        "{\"inputSchema\":\"  \",\"outputSchema\":\"bar2\"}",
                        SchemaMappingResponseDto.class));
    }

    @Test
    void readValue_SchemaMappingWithMissingInputSchema_ThrowsException() {

        assertThrows(ConstraintViolationException.class,
                () -> this.codec.withValidation().readValue(
                        "{\"outputSchema\":\"bar\"}",
                        SchemaMappingResponseDto.class));
    }
}
