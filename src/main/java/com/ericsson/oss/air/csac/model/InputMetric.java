/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.model;

import java.util.Locale;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@EqualsAndHashCode
@JsonInclude(Include.NON_EMPTY)
@JsonPropertyOrder({ "id", "alias", "type" })
public class InputMetric {

    /**
     * Metric Id. Must be either a PM name or a KPI name.
     */
    @NotBlank
    @JsonProperty(value = "id")
    private String id;
    /**
     * The KPI expression may use this alias in the definition rather than the metric name.
     */
    @Pattern(regexp = "[A-Za-z_][A-Za-z0-9_]*")
    @JsonProperty(value = "alias")
    private String alias;
    /**
     * One of 'pm_data' or 'kpi'.
     */
    @JsonProperty(value = "type")
    private Type type;

    /**
     * The ENUM of InputMetric types
     */
    public enum Type {
        PM_DATA("pm_data"),
        KPI("kpi");

        private final String name;

        Type(final String name) {
            this.name = name;
        }

        @JsonCreator
        public static Type fromString(final String type) {
            return Type.valueOf(type.trim().toUpperCase(Locale.ENGLISH));
        }

        @Override
        public String toString() {
            return this.name;
        }
    }

}
