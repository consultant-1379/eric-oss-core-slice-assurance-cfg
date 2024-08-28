/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.handler.request;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;

import com.ericsson.oss.air.api.model.RtProvisioningStateDto;
import com.ericsson.oss.air.csac.model.runtime.ProvisioningState;
import com.ericsson.oss.air.csac.repository.ProvisioningStateDao;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProvisioningStateRequestHandlerTest {

    @Mock
    private ProvisioningStateDao mockDao;

    private ProvisioningStateRequestHandler testHandler;

    private final ProvisioningState initialState = ProvisioningState.builder()
            .withId(1)
            .withProvisioningStartTime(Instant.ofEpochMilli(1705599004000L))
            .withProvisioningEndTime(Instant.ofEpochMilli(1705599004000L))
            .withProvisioningState(ProvisioningState.State.INITIAL)
            .build();

    final ProvisioningState startedState = ProvisioningState.builder()
            .withId(2)
            .withProvisioningState(ProvisioningState.State.STARTED)
            .withProvisioningStartTime(Instant.ofEpochMilli(1705599005000L))
            .build();

    private static final String expectedInitialStateStr = "{\n" +
            "  \"id\" : 1,\n" +
            "  \"provisioningState\" : \"INITIAL\",\n" +
            "  \"provisioningStartTime\" : 1705599004000,\n" +
            "  \"provisioningEndTime\" : 1705599004000\n" +
            "}";

    private static final String expectedStartedStateStr = "{\n" +
            "  \"id\" : 2,\n" +
            "  \"provisioningState\" : \"STARTED\",\n" +
            "  \"provisioningStartTime\" : 1705599005000,\n" +
            "  \"provisioningEndTime\" : null\n" +
            "}";

    private static RtProvisioningStateDto EXPECTED_INITIAL_STATE;
    private static RtProvisioningStateDto EXPECTED_STARTED_STATE;

    @BeforeAll
    static void setUpAll() throws Exception {

        final ObjectMapper mapper = new ObjectMapper();
        EXPECTED_INITIAL_STATE = mapper.readValue(expectedInitialStateStr, RtProvisioningStateDto.class);
        EXPECTED_STARTED_STATE = mapper.readValue(expectedStartedStateStr, RtProvisioningStateDto.class);
    }

    @BeforeEach
    void setUp() {
        this.testHandler = new ProvisioningStateRequestHandler(this.mockDao);
    }

    @Test
    void getLatestProvisioningState() throws Exception {

        when(this.mockDao.findLatest()).thenReturn(this.startedState);

        final RtProvisioningStateDto actual = this.testHandler.getLatestProvisioningState();

        assertEquals(EXPECTED_STARTED_STATE, actual);
    }

    @Test
    void getProvisioningStates() {

        when(this.mockDao.findAll()).thenReturn(List.of(this.initialState, this.startedState));

        final List<RtProvisioningStateDto> expected = List.of(EXPECTED_INITIAL_STATE, EXPECTED_STARTED_STATE);

        final List<RtProvisioningStateDto> actual = this.testHandler.getProvisioningStates();

        assertEquals(expected, actual);

    }
}