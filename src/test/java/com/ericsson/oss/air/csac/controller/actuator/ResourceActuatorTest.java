/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.controller.actuator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ericsson.oss.air.CsacEntryPoint;
import com.ericsson.oss.air.csac.handler.reset.ResetConfigurationHandler;
import com.ericsson.oss.air.csac.handler.status.ProvisioningTracker;
import com.ericsson.oss.air.csac.model.runtime.ProvisioningState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class ResourceActuatorTest {

    @Mock
    private CsacEntryPoint csacEntryPoint;

    @Mock
    private ResetConfigurationHandler resetHandler;

    @Mock
    private ProvisioningTracker provisioningTracker;

    @InjectMocks
    private ResourceActuator testActuator;

    @BeforeEach
    void setUp() {
        lenient().when(this.provisioningTracker.currentProvisioningState()).thenReturn(ProvisioningState.completed());
    }

    @Test
    void reloadResources() throws Exception {

        assertEquals(HttpStatus.OK, this.testActuator.reloadResources().getStatusCode());

    }

    @Test
    void reloadResources_provisioningStateConflict() throws Exception {

        when(this.provisioningTracker.currentProvisioningState()).thenReturn(ProvisioningState.started());

        assertThrows(UnsupportedOperationException.class, () -> this.testActuator.reloadResources());

    }

    @Test
    void resetResources() throws Exception {

        assertEquals(HttpStatus.NO_CONTENT, this.testActuator.resetResources("false").getStatusCode());

        verify(this.resetHandler, times(1)).apply();
    }

    @Test
    void resetResources_unsupportedPmscVersion() throws Exception {

        assertThrows(UnsupportedOperationException.class, () -> this.testActuator.resetResources("true"));

        verify(this.resetHandler, times(0)).apply();
    }

    @Test
    void resetResources_provisioningStateConflict() {

        when(this.provisioningTracker.currentProvisioningState()).thenReturn(ProvisioningState.started());

        assertThrows(UnsupportedOperationException.class, () -> this.testActuator.resetResources("false"));
    }
}