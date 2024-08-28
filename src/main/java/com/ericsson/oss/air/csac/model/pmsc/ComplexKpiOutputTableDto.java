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
import lombok.Builder;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ComplexKpiOutputTableDto extends KpiOutputTableDto {

    @Builder(builderMethodName = "customComplexKpiOutputTableDtoBuilder")
    public ComplexKpiOutputTableDto(final AggregationPeriod aggregationPeriod,
                                    final String alias,
                                    final int dataReliabilityOffset,
                                    final List<String> aggregationElements,
                                    final List<PmscKpiDefinitionDto> kpiDefinitions) {

        super(aggregationPeriod, alias, dataReliabilityOffset, aggregationElements, kpiDefinitions);

    }
}
