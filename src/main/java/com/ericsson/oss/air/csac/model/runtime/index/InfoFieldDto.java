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

import java.util.Locale;

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
 * Data transfer object (DTO) representing an info field in an Assurance Indexer writer definition.
 */
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Builder
@Getter
@JsonPropertyOrder({ "name", "displayName", "type", "recordName", "description" })
public class InfoFieldDto {

    public enum InfoFieldType {
        TIME,
        STRING;

        @JsonValue
        public String getInfoFieldType() {
            return this.name().toLowerCase(Locale.ROOT);
        }
    }

    @NotBlank
    @JsonProperty("name")
    private String name;

    @JsonProperty("displayName")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String infoFieldDisplayName;

    @JsonProperty("type")
    @Builder.Default
    private InfoFieldType type = InfoFieldType.STRING;

    @JsonProperty("recordName")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String recordName;

    @JsonProperty("description")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String description;
}
