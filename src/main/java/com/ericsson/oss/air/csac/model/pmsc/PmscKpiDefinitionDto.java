/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.model.pmsc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;

/**
 * This DTO class is used for generating resource submission for PMSC in JSON format, and it represents the data structure of new PMSC KPI definition
 * model
 */
@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "name", "expression", "object_type", "aggregation_type", "exportable" })
@SuperBuilder
@EqualsAndHashCode
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class PmscKpiDefinitionDto implements PmscKpiDefinition {

    @NonNull
    @JsonProperty("name")
    private String name;

    @NonNull
    @JsonProperty("expression")
    private String expression;

    @NonNull
    @JsonProperty("object_type")
    private String objectType;

    @NonNull
    @JsonProperty("aggregation_type")
    private String aggregationType;

    @JsonProperty("exportable")
    private Boolean exportable;
}
