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

public class ArdqRegistrationResponseDtoTest {

    private Codec codec = new Codec(Validation.buildDefaultValidatorFactory().getValidator());

    @Test
    void readValue_ArdqRegistrationDto_ReturnsArdqRegistrationDto() throws JsonProcessingException {

        final ArdqRegistrationResponseDto ardqRegistrationDto = this.codec.withValidation()
                .readValue("{\"ardqId\":\"cardq\"," +
                                "\"ardqUrl\":\"localhost:8080\"," +
                                "\"rules\":[{\"foo\":\"bar\"}]," +
                                "\"schemaMappings\":[{\"inputSchema\":\"foo\",\"outputSchema\":\"bar\"}]}",
                        ArdqRegistrationResponseDto.class);

        assertNotNull(ardqRegistrationDto);
        assertEquals("cardq", ardqRegistrationDto.getArdqId());
        assertNotNull(ardqRegistrationDto.getArdqUrl());
        assertNotNull(ardqRegistrationDto.getRules());
        assertEquals("foo", ardqRegistrationDto.getSchemaMappings().get(0).getInputSchema());
    }


    @Test
    void readValue_RegistrationWithBlankArdqUrl_ThrowsException() {

        assertThrows(ConstraintViolationException.class,
                () -> this.codec.withValidation().readValue(
                        "{\"ardqId\":\"cardq\"," +
                                "\"ardqUrl\":\"  \"," +
                                "\"rules\":[{\"foo\":\"bar\"}]," +
                                "\"schemaMappings\":[{\"inputSchema\":\"bar1\", \"outputSchema\":\"bar2\"}]}",
                        ArdqRegistrationResponseDto.class));
    }

    @Test
    void readValue_RegistrationWithMissingArdqId_ThrowsException() {

        assertThrows(ConstraintViolationException.class,
                () -> this.codec.withValidation().readValue(
                        "{\"ardqUrl\":\"localhost:8080\",\"rules\":[{\"foo\":\"bar\"}]}",
                        ArdqRegistrationResponseDto.class));
    }
}
