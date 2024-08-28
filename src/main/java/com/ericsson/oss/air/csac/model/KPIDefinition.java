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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;

import com.ericsson.oss.air.util.validation.constraint.IntegerEnumeration;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonSetter;
import lombok.AccessLevel;
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
@EqualsAndHashCode
@JsonInclude(Include.NON_EMPTY)
@JsonPropertyOrder({ "name", "description", "display_name", "expression", "aggregation_type", "aggregation_period", "is_visible", "input_metrics" })
@Builder(toBuilder = true)
public class KPIDefinition implements ResourceDefinition {

    private static final Comparator<InputMetric> INPUT_METRIC_COMPARATOR = (o1, o2) -> o1.getId().compareTo(o2.getId());

    @NotBlank
    @Pattern(regexp = "[a-zA-Z0-9][a-zA-Z0-9_]*")
    @JsonProperty(value = "name")
    @Getter(AccessLevel.NONE)
    private String name;

    /**
     * Returns the name of this KPI definition.  Must be unique for all registered KPI definitions.
     *
     * @return the name of this KPI definition.
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * Freeform string. If not provided, return an empty string.
     */
    @JsonProperty(value = "description")
    private String description;

    /**
     * Human-readable name for the KPI.
     */
    @JsonProperty(value = "display_name")
    private String displayName;

    /**
     * ANSI SQL expression for the KPI calculation.
     */
    @NotBlank
    @JsonProperty(value = "expression")
    private String expression;

    /**
     * Aggregation type required by PMSC, e.g. 'AVG', 'MAX', etc..
     */
    @NotBlank
    @JsonProperty(value = "aggregation_type")
    private String aggregationType;

    /**
     * Aggregation period override.  If set, must be supported by the KPI calculation service being provisioned.
     */
    @IntegerEnumeration(supplier = AggregationPeriodSupplier.class)
    @JsonProperty(value = "aggregation_period")
    private Integer aggregationPeriod;

    /**
     * Whether the KPI is visible in the UI.
     */
    @JsonProperty(value = "is_visible")
    private Boolean isVisible = true;

    @Valid
    @NotEmpty
    private @Builder.Default List<InputMetric> inputMetrics = new ArrayList<>();

    /**
     * Adds a single input metric to this KPI definition.
     *
     * @param inputMetric
     */
    public void addInputMetric(final InputMetric inputMetric) {
        this.inputMetrics.add(inputMetric);
    }

    /**
     * Sets the input metric list.
     *
     * @param inputMetricsInput
     *         input metric list to set.
     */
    @JsonSetter("input_metrics")
    public void setInputMetrics(final List<InputMetric> inputMetricsInput) {

        this.inputMetrics.clear();
        this.inputMetrics.addAll(inputMetricsInput);
    }

    /**
     * Retrieves an ordered list of input metrics. Input metrics are ordered by input metric Id.
     *
     * @return ordered list of input metrics
     */
    @JsonGetter("input_metrics")
    public List<InputMetric> getInputMetrics() {
        final List<InputMetric> result = new ArrayList<>(this.inputMetrics);

        Collections.sort(result, INPUT_METRIC_COMPARATOR);

        return result;
    }
}
