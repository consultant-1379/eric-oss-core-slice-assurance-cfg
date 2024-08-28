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

import static com.ericsson.oss.air.csac.model.PMDefinition.PM_NAME_PATTERN;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.ericsson.oss.air.csac.model.pmschema.SchemaURI;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Resource definition bean for PM schema definitions.
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@JsonInclude(Include.NON_EMPTY)
@JsonPropertyOrder({ "name", "uri", "pm_counters", "context" })
@Builder
public class PMSchemaDefinition implements ResourceDefinition {

    /**
     * PM Schema name. This is unique.
     */
    @NotBlank
    @JsonProperty(value = "name")
    private String name;

    /**
     * URI for the PM schema. This follows the standard convention for URIs, comprising scheme:[//[user info:]authority/][path].
     */
    @NotNull
    @JsonProperty(value = "uri")
    private SchemaURI uri;

    /**
     * List of PM counter types associated with this schema.
     */
    @Valid
    @JsonProperty(value = "pm_counters")
    @Builder.Default
    private List<PMCounter> pmCounters = Collections.emptyList();

    /**
     * List of PM context field names associated with this schema. This context provides the PM index and uniquely
     * identifies each PM value when consuming multiple PM records.
     */
    @NotEmpty
    @JsonProperty(value = "context")
    private List<@NotBlank String> context;

    /**
     * Returns {@code true} if this PM schema has at least one defined PM counter.
     *
     * @return {@code true} if this PM schema has at least one defined PM counter.
     */
    public boolean hasPmCounters() {
        return Objects.nonNull(this.pmCounters) && !this.pmCounters.isEmpty();
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    @JsonInclude(Include.NON_EMPTY)
    @JsonPropertyOrder({ "name", "description" })
    @Builder
    public static class PMCounter {

        /**
         * PM counter name in the Avro schema. This must include the full path to the field from root, not including the root ".".
         */
        @NotBlank
        @Pattern(regexp = PM_NAME_PATTERN)
        @JsonProperty(value = "name")
        private String name;

        /**
         * Documentation for this PM counter.
         */
        @JsonProperty(value = "description")
        private String description;

        /**
         * Creates a {@link PMDefinition} from this {@code PMCounter} and the specified {@link SchemaURI}.
         *
         * @param schemaURI the unique identifier of the schema associated with the PM counter
         * @return a {@link PMDefinition}
         */
        public PMDefinition toPmDefinition(final SchemaURI schemaURI) {

            Objects.requireNonNull(schemaURI, "Missing PM Schema URI for PM Counter: " + this.name);

            return PMDefinition.builder()
                    .name(this.name)
                    .description(this.description)
                    .source(schemaURI.getSchemeSpecificPart())
                    .build();
        }

    }
}
