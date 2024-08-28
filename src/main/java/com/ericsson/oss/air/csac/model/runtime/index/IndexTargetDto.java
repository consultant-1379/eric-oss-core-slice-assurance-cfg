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

import jakarta.validation.constraints.NotBlank;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Data transfer object (DTO) representing an Assurance Indexer target definition.
 */
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Builder
@Getter
public class IndexTargetDto {

    @NotBlank
    @JsonProperty("name")
    private String indexTargetName;

    @JsonProperty("displayName")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String indexTargetDisplayName;

    @JsonProperty("description")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String indexTargetDescription;
}
