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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Data transfer object (DTO) representing an Assurance Indexer source definition.
 */
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Builder
@Getter
@Setter
@JsonPropertyOrder({ "name", "type", "description" })
public class IndexSourceDto {

    /**
     * Enumeration of supported index source types.
     */
    public enum IndexSourceType {
        PM_STATS_EXPORTER("pmstatsexporter");

        private final String sourceType;

        IndexSourceType(final String type) {
            this.sourceType = type;
        }

        @JsonValue
        public String getSourceType() {
            return this.sourceType;
        }
    }

    @NotBlank
    @JsonProperty("name")
    private String indexSourceName;

    @NotNull
    @JsonProperty("type")
    @Builder.Default
    private IndexSourceType indexSourceType = IndexSourceType.PM_STATS_EXPORTER;

    @JsonProperty("description")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String indexSourceDescription;
}
