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

import java.util.List;

import jakarta.validation.constraints.NotBlank;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Builder;
import lombok.Data;

/**
 * This class represents an Ardq Registration response from AAS
 */
@Data
@Builder
@JsonDeserialize(builder = ArdqRegistrationResponseDto.ArdqRegistrationResponseDtoBuilder.class)
@JsonPropertyOrder({ "ardqId", "ardqUrl", "rules", "schemaMappings" })
public class ArdqRegistrationResponseDto {

    @NotBlank
    @JsonProperty(value = "ardqId")
    private String ardqId;

    @NotBlank
    @JsonProperty(value = "ardqUrl")
    private String ardqUrl;

    @JsonProperty(value = "rules")
    private List<Object> rules;

    @JsonProperty(value = "schemaMappings")
    private List<SchemaMappingResponseDto> schemaMappings;
}
