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

import jakarta.validation.constraints.NotBlank;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Builder;
import lombok.Data;

/**
 * This class represents a schema mapping response for the registration from AAS
 */
@Data
@Builder
@JsonDeserialize(builder = SchemaMappingResponseDto.SchemaMappingResponseDtoBuilder.class)
@JsonPropertyOrder({ "inputSchema", "outputSchema" })
public class SchemaMappingResponseDto {

    @NotBlank
    @JsonProperty(value = "inputSchema")
    private String inputSchema;

    @NotBlank
    @JsonProperty(value = "outputSchema")
    private String outputSchema;
}
