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

import com.ericsson.oss.air.csac.model.TestResourcesUtils;
import com.ericsson.oss.air.csac.model.runtime.index.DeployedIndexDefinitionDto;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner;
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles("test")
@AutoConfigureStubRunner(repositoryRoot = "https://arm.seli.gic.ericsson.se/artifactory/proj-eric-oss-dev-local",
                         stubsMode = StubRunnerProperties.StubsMode.REMOTE,
                         ids = "com/ericsson/oss/air:eric-oss-assurance-indexer:+:stubs:9590")
public class AISRestClientContractTest {

    private static final String AIS_URL = "http://localhost:9590/v1/indexer-info/indexer";

    @Test
    public void createOrUpdateIndexDefinition_post_successResponse() {

        ResponseEntity<Void> result;

        result = new TestRestTemplate().exchange(
                RequestEntity.post(AIS_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(TestResourcesUtils.AIS_INDEX_DEFINITION_CREATE_OR_UPDATE_REQUEST_BODY, DeployedIndexDefinitionDto.class),
                Void.class
        );

        assertEquals(HttpStatus.OK, result.getStatusCode());
    }

    @Test
    public void createOrUpdateIndexDefinition_put_successResponse() {

        ResponseEntity<Void> result;

        result = new TestRestTemplate().exchange(
                RequestEntity.put(AIS_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(TestResourcesUtils.AIS_INDEX_DEFINITION_CREATE_OR_UPDATE_REQUEST_BODY, DeployedIndexDefinitionDto.class),
                Void.class
        );

        assertEquals(HttpStatus.OK, result.getStatusCode());
    }

    @Test
    public void deleteIndexDefinition_200_ok() {

        ResponseEntity<String> result;

        result = new TestRestTemplate().exchange(
                RequestEntity
                        .delete(AIS_URL + "?name=nameOfIndexerA")
                        .build(),
                String.class
        );

        assertEquals(HttpStatus.OK, result.getStatusCode());
    }

    @Test
    public void deleteIndexDefinition_404_notFound() {

        ResponseEntity<Void> result;

        result = new TestRestTemplate().exchange(
                RequestEntity
                        .delete(AIS_URL + "?name=invalidIndexName")
                        .build(),
                Void.class
        );

        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
    }

}
