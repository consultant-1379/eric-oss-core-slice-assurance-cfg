/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.model.runtime;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data bean representing the run time kpi multi key
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true,
         setterPrefix = "with")
public class RuntimeKpiKey {

    private String kpDefinitionName;

    private List<String> aggregationFields;

    private Integer aggregationPeriod;

    private String referenceKey;
}
