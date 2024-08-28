/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.model.pmsc.response;

import com.ericsson.oss.air.csac.model.pmsc.KpiOutputTableListDto;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import lombok.Data;

/**
 * Represents the data object for PMSC GET KPIs response
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PmscKpiResponseDto {

    @JsonProperty("on_demand")
    private KpiOutputTableListDto onDemand;

    @JsonProperty("scheduled_complex")
    @Valid
    private PmscScheduledComplexDto scheduledComplex;

    @JsonProperty("scheduled_simple")
    @Valid
    private PmscScheduledSimpleDto scheduledSimple;

}
