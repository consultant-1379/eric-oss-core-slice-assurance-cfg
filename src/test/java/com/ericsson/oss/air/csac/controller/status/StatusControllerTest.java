/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.controller.status;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;

import com.ericsson.oss.air.api.model.RtProvisioningStateDto;
import com.ericsson.oss.air.csac.handler.request.ProvisioningStateRequestHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class StatusControllerTest {

    @MockBean
    private ProvisioningStateRequestHandler mockHandler;

    private final long initialTs = Instant.now().toEpochMilli();

    @Autowired
    private StatusController controller;

    @Autowired
    private TestRestTemplate restTemplate;

    private final RtProvisioningStateDto initialState = new RtProvisioningStateDto()
            .id(1)
            .provisioningState("INITIAL")
            .provisioningStartTime(initialTs)
            .provisioningEndTime(initialTs);

    private final RtProvisioningStateDto completedState = new RtProvisioningStateDto()
            .id(1)
            .provisioningState("COMPLETED")
            .provisioningStartTime(initialTs + 1000)
            .provisioningEndTime(initialTs + 2000);

    @Test
    void getCurrentProvisioningStatus() {

        when(this.mockHandler.getLatestProvisioningState()).thenReturn(this.completedState);

        final ResponseEntity<RtProvisioningStateDto> actual = this.restTemplate.getForEntity("/v1/runtime/status/current",
                RtProvisioningStateDto.class);

        assertEquals(HttpStatus.OK, actual.getStatusCode());
        assertEquals(this.completedState, actual.getBody());
    }

    @Test
    void getProvisioningStatus() {

        when(this.mockHandler.getProvisioningStates()).thenReturn(List.of(this.initialState, this.completedState));

        final HttpHeaders headers = new HttpHeaders();
        headers.set("accept", "application/json");

        final HttpEntity requestEntity = new HttpEntity<>(null, headers);

        final ResponseEntity<List<RtProvisioningStateDto>> actual = this.restTemplate.exchange(
                "/v1/runtime/status",
                HttpMethod.GET,
                requestEntity,
                new ParameterizedTypeReference<List<RtProvisioningStateDto>>() {
                });

        assertEquals(HttpStatus.OK, actual.getStatusCode());

        assertEquals(2, actual.getBody().size());
    }
}