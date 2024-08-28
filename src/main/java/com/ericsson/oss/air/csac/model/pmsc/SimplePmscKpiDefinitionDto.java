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

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * This DTO class represents the new PM Stats Calculator model for simple KPI definitions.
 */
@NoArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class SimplePmscKpiDefinitionDto extends PmscKpiDefinitionDto {

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JsonProperty("inp_data_identifier")
    @Getter
    private String inputDataIdentifier;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JsonProperty("aggregation_elements")
    @Getter
    private List<String> aggregationElements;
}
