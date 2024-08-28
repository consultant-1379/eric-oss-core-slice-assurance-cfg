/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.handler.status;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;

import com.ericsson.oss.air.csac.model.runtime.ProvisioningState;
import com.ericsson.oss.air.csac.repository.ProvisioningStateDao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextClosedEvent;

@ExtendWith(MockitoExtension.class)
class ProvisioningTrackerTest {

    @Mock
    private ProvisioningStateDao provisioningStateDao;

    @Mock
    private ApplicationContext applicationContext;

    private ProvisioningTracker provisioningTracker;

    private final ProvisioningState initialState = ProvisioningState.builder()
            .withId(1)
            .withProvisioningState(ProvisioningState.State.INITIAL)
            .withProvisioningStartTime(Instant.now())
            .withProvisioningEndTime(Instant.now())
            .build();

    @Captor
    private ArgumentCaptor<ProvisioningState> stateArgumentCaptor;

    @BeforeEach
    void setUp() {

        this.provisioningTracker = new ProvisioningTracker(this.provisioningStateDao);
    }

    @Test
    void startProvisioning() {

        this.provisioningTracker.startProvisioning();

        verify(this.provisioningStateDao, times(1)).save(any());
        verify(this.provisioningStateDao).save(stateArgumentCaptor.capture());

        assertEquals(ProvisioningState.State.STARTED, stateArgumentCaptor.getValue().getProvisioningState());
    }

    @Test
    void stopProvisioning() {

        this.provisioningTracker.stopProvisioning();

        verify(this.provisioningStateDao, times(1)).save(any());
        verify(this.provisioningStateDao).save(stateArgumentCaptor.capture());

        assertEquals(ProvisioningState.State.COMPLETED, stateArgumentCaptor.getValue().getProvisioningState());
    }

    @Test
    void stopProvisioning_error() {

        this.provisioningTracker.stopProvisioning(new IllegalArgumentException("Test exception"));

        verify(this.provisioningStateDao, times(1)).save(any());
        verify(this.provisioningStateDao).save(stateArgumentCaptor.capture());

        assertEquals(ProvisioningState.State.ERROR, stateArgumentCaptor.getValue().getProvisioningState());
    }

    @Test
    void currentProvisioningState() {

        when(this.provisioningStateDao.findLatest()).thenReturn(this.initialState);

        assertEquals(ProvisioningState.State.INITIAL, this.provisioningTracker.currentProvisioningState().getProvisioningState());

        verify(this.provisioningStateDao, times(1)).findLatest();
    }

    @Test
    void resetProvisioning() {

        this.provisioningTracker.resetProvisioning();

        verify(this.provisioningStateDao, times(1)).save(any());
        verify(this.provisioningStateDao).save(this.stateArgumentCaptor.capture());

        assertEquals(ProvisioningState.State.RESET, this.stateArgumentCaptor.getValue().getProvisioningState());
    }

    @Test
    void interruptProvisioning_error() {

        this.provisioningTracker.interruptProvisioning();
        verify(this.provisioningStateDao, times(1)).save(any());
    }

    @Test
    void onApplicationEvent_whenProvisioningStateIsCompleted() {

        when(this.provisioningStateDao.findLatest()).thenReturn(ProvisioningState.ofState(ProvisioningState.State.COMPLETED));
        final ContextClosedEvent contextClosedEvent = new ContextClosedEvent(this.applicationContext);
        this.provisioningTracker.onApplicationEvent(contextClosedEvent);

        verify(this.provisioningStateDao, times(0)).save(any());
    }

    @Test
    void onApplicationEvent_whenProvisioningStateIsStarted() {

        when(this.provisioningStateDao.findLatest()).thenReturn(ProvisioningState.ofState(ProvisioningState.State.STARTED));
        final ContextClosedEvent contextClosedEvent = new ContextClosedEvent(this.applicationContext);
        this.provisioningTracker.onApplicationEvent(contextClosedEvent);

        verify(this.provisioningStateDao, times(1)).save(any());
    }
}