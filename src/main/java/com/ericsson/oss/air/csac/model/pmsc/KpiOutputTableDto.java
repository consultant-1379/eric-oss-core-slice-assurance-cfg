/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.model.pmsc;

import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.ObjectUtils;

/**
 * This class represents the KPI output table in {@link KpiOutputTableListDto}.
 */
@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "aggregation_period", "alias", "aggregation_elements", "data_reliability_offset", "kpi_definitions" })
public class KpiOutputTableDto {

    public static final String REQUIRED_FIELD_EXCEPTION_MSG = "%s is a required field and should not be null";

    @JsonProperty("aggregation_period")
    private AggregationPeriod aggregationPeriod;

    @JsonProperty("alias")
    private String alias;

    @JsonProperty("aggregation_elements")
    private List<String> aggregationElements;

    @JsonProperty("data_reliability_offset")
    private int dataReliabilityOffset;

    @JsonProperty("kpi_definitions")
    private List<PmscKpiDefinitionDto> kpiDefinitions;

    @Builder(builderMethodName = "customKpiOutputTableDtoBuilder")
    public KpiOutputTableDto(final AggregationPeriod aggregationPeriod,
                             final String alias,
                             final int dataReliabilityOffset,
                             final List<String> aggregationElements,
                             final List<PmscKpiDefinitionDto> kpiDefinitions) {

        if (ObjectUtils.isEmpty(aggregationPeriod)) {
            throw new NullPointerException(String.format(REQUIRED_FIELD_EXCEPTION_MSG, "aggregationPeriod"));
        }
        if (ObjectUtils.isEmpty(alias)) {
            throw new NullPointerException(String.format(REQUIRED_FIELD_EXCEPTION_MSG, "alias"));
        }
        if (ObjectUtils.isEmpty(aggregationElements)) {
            throw new NullPointerException(String.format(REQUIRED_FIELD_EXCEPTION_MSG, "aggregationElements"));
        }
        if (ObjectUtils.isEmpty(kpiDefinitions)) {
            throw new NullPointerException(String.format(REQUIRED_FIELD_EXCEPTION_MSG, "kpiDefinitions"));
        }

        this.aggregationPeriod = aggregationPeriod;
        this.alias = alias;
        this.dataReliabilityOffset = dataReliabilityOffset;
        this.aggregationElements = Collections.unmodifiableList(aggregationElements);
        this.kpiDefinitions = Collections.unmodifiableList(kpiDefinitions);
    }

}
