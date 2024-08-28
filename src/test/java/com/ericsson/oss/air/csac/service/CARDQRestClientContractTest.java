/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URI;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner;
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK,
                classes = { CARDQRestClientContractTest.class })
@ActiveProfiles("test")
@AutoConfigureStubRunner(repositoryRoot = "https://arm.seli.gic.ericsson.se/artifactory/proj-bos-assurance-release-local",
                         stubsMode = StubRunnerProperties.StubsMode.REMOTE,
                         ids = "com.ericsson.oss.air:eric-oss-core-reporting-dimension-query-main::stubs:9590")
public class CARDQRestClientContractTest {

    private final static String CARDQ_URL = "http://localhost:9590/v1/augmentation-info/augmentation/query/types";

    @Test
    public void queryAugmentationTypes_successResponse() throws Exception {
        ResponseEntity<String> result = new TestRestTemplate().exchange(RequestEntity.get(URI.create(CARDQ_URL)).build(), String.class);

        // In the interests of being future-proof, this test will only ensure that all keys in the response are "queryType" and that
        // the value of "core" exists exactly once.
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jNode = mapper.readTree(result.getBody());
        List<String> queryTypes = jNode.findValuesAsText("queryType");
        long coreCount = queryTypes.stream().filter(value -> value.equals("core")).count();

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(jNode.size(), queryTypes.size()); // to ensure all keys are queryType
        assertEquals(1, coreCount); // exactly one of the values is "core"
    }
}
