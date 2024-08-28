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

import java.util.List;

import jakarta.validation.constraints.NotBlank;

import com.ericsson.oss.air.util.validation.constraint.IntegerEnumeration;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents input metric property overrides that can be applied on a per-KPI reference basis in a given profile.
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@Builder
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class InputMetricOverride {

    @NotBlank
    @JsonProperty(value = "id")
    private String id;

    @IntegerEnumeration(supplier = AggregationPeriodSupplier.class)
    @JsonProperty(value = "aggregation_period")
    private Integer aggregationPeriod;

    @JsonProperty(value = "context")
    private List<String> context;
}
