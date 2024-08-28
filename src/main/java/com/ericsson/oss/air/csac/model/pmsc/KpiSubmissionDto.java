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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.*;

/**
 * This class represents the new PMSC KPI submission model.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "on_demand", "scheduled_complex", "scheduled_simple" })
public class KpiSubmissionDto implements KpiDefinitionSubmission {

    @JsonProperty("on_demand")
    private KpiOutputTableListDto onDemand;

    @JsonProperty("scheduled_complex")
    private KpiOutputTableListDto scheduledComplex;

    @JsonProperty("scheduled_simple")
    private KpiOutputTableListDto scheduledSimple;
}