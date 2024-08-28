/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.handler.validation;

import com.ericsson.oss.air.csac.model.PMDefinition;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
class LoopbackPMValidatorTest {

    @Autowired
    private LoopbackPMValidator loopbackPMValidator;

    private static final PMDefinition PM_DEFINITION_1 = PMDefinition.builder()
            .name("pmCounters.VS_NS_NbrRegisteredSub_5GS")
            .source("5G|PM_COUNTERS|AMF_Mobility_NetworkSlice_1")
            .description("Some description")
            .build();

    private static final PMDefinition PM_DEFINITION_2 = PMDefinition.builder()
            .name("pmCounters.create_sm_context_resp_succ")
            .source("5G|PM_COUNTERS|smf_nsmf_pdu_session_snssai_apn_1")
            .description("Some description")
            .build();

    private static final PMDefinition PM_DEFINITION_3 = PMDefinition.builder()
            .name("pmCounters.create_sm_context_resp_fail")
            .source("5G|PM_COUNTERS|smf_nsmf_pdu_session_snssai_apn_1")
            .description("Some description")
            .build();

    private static final String AMF_SOURCE = "AMF_Mobility_NetworkSlice_1";
    private static final String SMF_SOURCE = "smf_nsmf_pdu_session_snssai_apn_1";

    @Test
    void validate() {
        this.loopbackPMValidator.validate(new ArrayList<>());
    }

    @Test
    void getValidPMDefinitions_emptyList() {
        Map<String, List<PMDefinition>> map = this.loopbackPMValidator.getValidPMDefinitions(new ArrayList<>());
        assertNotNull(map);
    }

    @Test
    void getValidPMDefinitionsTest() {
        final List<PMDefinition> pmDefinitionList = new ArrayList<>();

        pmDefinitionList.add(PM_DEFINITION_1);
        pmDefinitionList.add(PM_DEFINITION_2);

        final Map<String, List<PMDefinition>> validPMDefinitions = this.loopbackPMValidator.getValidPMDefinitions(pmDefinitionList);

        assertNotNull(validPMDefinitions);

        assertTrue(validPMDefinitions.containsKey(AMF_SOURCE));
        assertEquals(1, validPMDefinitions.get(AMF_SOURCE).size());

        assertTrue(validPMDefinitions.containsKey(SMF_SOURCE));
        assertEquals(1, validPMDefinitions.get(SMF_SOURCE).size());
    }

    @Test
    void getValidPMDefinitionsTest_MultiplePMDefsWithSameSource() {
        final List<PMDefinition> pmDefinitionList = new ArrayList<>();

        pmDefinitionList.add(PM_DEFINITION_1);
        pmDefinitionList.add(PM_DEFINITION_2);
        pmDefinitionList.add(PM_DEFINITION_3);

        final Map<String, List<PMDefinition>> validPMDefinitions = this.loopbackPMValidator.getValidPMDefinitions(pmDefinitionList);

        assertNotNull(validPMDefinitions);

        assertTrue(validPMDefinitions.containsKey(AMF_SOURCE));
        assertEquals(1, validPMDefinitions.get(AMF_SOURCE).size());

        assertTrue(validPMDefinitions.containsKey(SMF_SOURCE));
        assertEquals(2, validPMDefinitions.get(SMF_SOURCE).size());
    }

}