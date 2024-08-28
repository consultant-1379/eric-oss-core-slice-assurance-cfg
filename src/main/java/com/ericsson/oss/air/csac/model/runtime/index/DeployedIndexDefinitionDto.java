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

import java.util.HashSet;
import java.util.Set;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

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
 * Data transfer object (DTO) representing an Assurance Indexer deployed index definition.
 */
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Builder
@Accessors(fluent = true)
@Getter
@Setter
@JsonPropertyOrder({ "name", "description", "source", "target", "writers" })
public class DeployedIndexDefinitionDto {

    @NotBlank
    @JsonProperty("name")
    private String indexDefinitionName;

    @JsonProperty("description")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String indexDefinitionDescription;

    @NotNull
    @Valid
    @JsonProperty("source")
    private IndexSourceDto indexSource;

    @NotNull
    @Valid
    @JsonProperty("target")
    private IndexTargetDto indexTarget;

    @NotEmpty
    @Valid
    @JsonProperty("writers")
    @Builder.Default
    private Set<IndexWriterDto> indexWriters = new HashSet<>();

    /**
     * Adds the provided index writer to the list of index writers in this {@code DeployedIndexDefinitionDto} if it is not already there.
     *
     * @param writerDto
     *         index writer to add to this index definition
     * @return this index definition
     */
    public DeployedIndexDefinitionDto indexWriter(final IndexWriterDto writerDto) {

        this.indexWriters.add(writerDto);

        return this;
    }
}
