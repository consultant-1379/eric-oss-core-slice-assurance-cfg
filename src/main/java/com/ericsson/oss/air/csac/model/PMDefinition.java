/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@JsonInclude(Include.NON_EMPTY)
@JsonPropertyOrder({ "name", "source", "description" })
@Builder(toBuilder = true)
public class PMDefinition implements ResourceDefinition {

    private static final String VALID_AVRO_FIELD_NAME_PATTERN = "[A-Za-z_][A-Za-z0-9_]*";

    static final String PM_NAME_PATTERN = "(" + VALID_AVRO_FIELD_NAME_PATTERN + ")+(\\." +
            VALID_AVRO_FIELD_NAME_PATTERN + ")*";

    /**
     * Fully qualified name of the counter as it will appear in the avro record
     */
    @NotBlank
    @Pattern(regexp = PM_NAME_PATTERN)
    @JsonProperty(value = "name")
    private String name;

    /**
     * Avro schema reference for Data Catalog lookup.
     * <p>
     * TODO IDUN-48178 Could @Pattern be added to this attribute or could the source be deserialized into a type other
     * than String?
     */
    @NotBlank
    @JsonProperty(value = "source")
    private String source;

    /**
     * Freeform string. If not provided, return an empty string.
     */
    @JsonProperty(value = "description")
    private String description;

}
