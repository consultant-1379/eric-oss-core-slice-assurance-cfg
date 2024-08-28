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
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Data transfer object (DTO) representing a context field in an Assurance Indexer writer definition.
 */
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Builder
@Getter
@JsonPropertyOrder({ "name", "displayName", "nameType", "recordName", "description" })
public class ContextFieldDto {

    public enum ContextNameType {
        STRAIGHT("straight"),
        COLON_SEPARATED("colonSeparated");

        private final String nameType;

        ContextNameType(final String nameType) {
            this.nameType = nameType;
        }

        @JsonValue
        public String getNameType() {
            return this.nameType;
        }
    }

    @NotBlank
    @JsonProperty("name")
    private String name;

    @JsonProperty("displayName")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String contextFieldDisplayName;

    @JsonProperty("nameType")
    @Builder.Default
    private ContextNameType nameType = ContextNameType.STRAIGHT;

    @JsonProperty("recordName")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String recordName;

    @JsonProperty("description")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String description;

}
