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

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * This class represents the output table for simple KPI in {@link KpiOutputTableListDto}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SimpleKpiOutputTableDto extends KpiOutputTableDto {

    @JsonProperty("inp_data_identifier")
    private String inputDataIdentifier;

    @Builder(builderMethodName = "customSimpleKpiOutputTableDtoBuilder")
    public SimpleKpiOutputTableDto(final AggregationPeriod aggregationPeriod,
                                   final String alias,
                                   final int dataReliabilityOffset,
                                   final List<String> aggregationElements,
                                   final List<PmscKpiDefinitionDto> kpiDefinitions,
                                   final String inputDataIdentifier) {

        super(aggregationPeriod, alias, dataReliabilityOffset, aggregationElements, kpiDefinitions);

        this.inputDataIdentifier = inputDataIdentifier;
    }
}
