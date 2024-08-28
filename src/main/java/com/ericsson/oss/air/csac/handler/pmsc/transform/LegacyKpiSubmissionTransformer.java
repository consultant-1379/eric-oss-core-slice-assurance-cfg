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


import com.ericsson.oss.air.csac.model.pmsc.KpiDefinitionDTO;
import com.ericsson.oss.air.csac.model.pmsc.KpiDefinitionSubmission;
import com.ericsson.oss.air.csac.model.pmsc.LegacyKpiSubmissionDto;
import com.ericsson.oss.air.csac.model.pmsc.PMSCKpiUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

/**
 * A Class transform a list of {@link KpiDefinitionDTO}'s into a {@link KpiDefinitionSubmission} in a legacy format that can be used to instantiate
 * KPI's in the PMSC.
 */
@Component
@Slf4j
@ConditionalOnProperty(value = "provisioning.pmsc.model.legacy",
                       havingValue = "true")
@Primary
public class LegacyKpiSubmissionTransformer implements KpiSubmissionTransformer {

    @Override
    public KpiDefinitionSubmission apply(final List<KpiDefinitionDTO> kpiDefinitionDTOS) {
        log.info("Creating KPI definition submission in legacy format");

        return new LegacyKpiSubmissionDto(PMSCKpiUtil.CSAC_KPI_DEF_SOURCE, kpiDefinitionDTOS);
    }
}
