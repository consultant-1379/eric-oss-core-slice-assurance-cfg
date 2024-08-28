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
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

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

/**
 * Resource bean for profile definitions.
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@JsonInclude(Include.NON_EMPTY)
@JsonPropertyOrder({ "name", "description", "aggregation_fields", "kpis" })
@Builder(toBuilder = true)
public class ProfileDefinition implements ResourceDefinition {

    /**
     * Unique profile name.
     */
    @NotBlank
    @JsonProperty(value = "name")
    @Getter(AccessLevel.NONE)
    private String name;

    /**
     * Optional description of the profile.
     */
    @JsonProperty(value = "description")
    private String description;

    @JsonInclude(Include.NON_NULL)
    @JsonProperty(value = "augmentation")
    private String augmentation;

    /**
     * list of aggregation fields.
     */
    @NotEmpty
    private List<String> context;

    /**
     * list of KPI references.
     */
    @Valid
    @JsonProperty(value = "kpis")
    private List<KPIReference> kpis;

    @Override
    public String getName() {
        return name;
    }

    /**
     * Returns the profile context.
     *
     * @return profile context
     */
    @JsonGetter
    public List<String> getContext() {
        return Collections.unmodifiableList(this.context);
    }

    /**
     * Sets the context for this profile.
     *
     * @param contextFields
     *         context for this profile
     */
    @JsonSetter
    public void setContext(final List<String> contextFields) {
        this.context = new ArrayList<>(contextFields);
    }

    /**
     * Sets the context of this profile using the legacy 'aggregation_fields' JSON property.
     *
     * @param aggregationFields
     *         legacy profile aggregation fields.
     */
    @JsonProperty("aggregation_fields")
    public void setAggregationFields(final List<String> aggregationFields) {
        setContext(aggregationFields);
    }
}
