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

import java.util.ArrayList;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Data transfer object (DTO) representing an Assurance Indexer writer definition.
 */
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Builder
@Accessors(fluent = true)
@Getter
@Setter
@JsonPropertyOrder({ "name", "inputSchema", "context", "value", "info" })
public class IndexWriterDto {

    @NotBlank
    @JsonProperty("name")
    private String name;

    @NotBlank
    @JsonProperty("inputSchema")
    private String inputSchema;

    @NotEmpty
    @Valid
    @JsonProperty("context")
    @Builder.Default
    private List<ContextFieldDto> contextFieldList = new ArrayList<>();

    @NotEmpty
    @Valid
    @JsonProperty("value")
    @Builder.Default
    private List<ValueFieldDto> valueFieldList = new ArrayList<>();

    @JsonProperty("info")
    @Valid
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @Builder.Default
    private List<InfoFieldDto> infoFieldList = new ArrayList<>();

}
