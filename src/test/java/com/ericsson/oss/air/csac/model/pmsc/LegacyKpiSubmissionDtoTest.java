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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

class LegacyKpiSubmissionDtoTest {

    private static final String TEST_SOURCE = "Test Source";

    @Test
    void testKPIDefs() {
        final LegacyKpiSubmissionDto legacyKpiSubmissionDto = new LegacyKpiSubmissionDto();
        legacyKpiSubmissionDto.setSource(TEST_SOURCE);

        final ArrayList<KpiDefinitionDTO> emptyKpiDefsList = new ArrayList<>();
        legacyKpiSubmissionDto.setKpiDefinitionsList(emptyKpiDefsList);

        Assertions.assertEquals(TEST_SOURCE, legacyKpiSubmissionDto.getSource());
        Assertions.assertEquals(emptyKpiDefsList, legacyKpiSubmissionDto.getKpiDefinitionsList());
    }
}