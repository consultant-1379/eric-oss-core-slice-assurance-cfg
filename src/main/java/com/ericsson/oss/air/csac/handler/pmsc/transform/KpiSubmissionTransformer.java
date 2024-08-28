/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.handler.pmsc.transform;

import java.util.List;
import java.util.function.Function;

import com.ericsson.oss.air.csac.model.pmsc.KpiDefinitionDTO;
import com.ericsson.oss.air.csac.model.pmsc.KpiDefinitionSubmission;

/**
 * Classes that implement this interface will transform a list of {@link KpiDefinitionDTO}'s into a
 * {@link KpiDefinitionSubmission} that can be used to instantiate KPI's in the PMSC.
 */
@FunctionalInterface
public interface KpiSubmissionTransformer extends Function<List<KpiDefinitionDTO>, KpiDefinitionSubmission> {
}
