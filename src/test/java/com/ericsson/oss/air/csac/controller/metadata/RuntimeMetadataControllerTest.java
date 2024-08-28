/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.controller.metadata;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import com.ericsson.oss.air.api.model.RtKpiMetadataDto;
import com.ericsson.oss.air.csac.handler.request.RuntimeMetadataRequestHandler;
import com.ericsson.oss.air.csac.handler.status.ProvisioningTracker;
import com.ericsson.oss.air.csac.model.runtime.ProvisioningState;
import com.ericsson.oss.air.csac.model.runtime.metadata.KpiContextId;
import com.ericsson.oss.air.exception.CsacConflictStateException;
import com.ericsson.oss.air.exception.CsacInternalErrorException;
import com.ericsson.oss.air.exception.CsacNotFoundException;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class RuntimeMetadataControllerTest {

    @Mock
    private RuntimeMetadataRequestHandler runtimeMetadataRequestHandler;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ProvisioningTracker provisioningTracker;

    @InjectMocks
    private RuntimeMetadataController runtimeMetadataController;

    @Test
    void getRtContextMetadataList() {
        when(this.provisioningTracker.currentProvisioningState()).thenReturn(ProvisioningState.ofState(ProvisioningState.State.COMPLETED));
        Assert.assertEquals(HttpStatus.OK, this.runtimeMetadataController.getRtContextMetadataList().getStatusCode());

        when(this.provisioningTracker.currentProvisioningState()).thenReturn(ProvisioningState.ofState(ProvisioningState.State.INITIAL));
        Assert.assertThrows(CsacConflictStateException.class, () -> this.runtimeMetadataController.getRtContextMetadataList().getStatusCode());

        when(this.provisioningTracker.currentProvisioningState()).thenReturn(ProvisioningState.ofState(ProvisioningState.State.ERROR));
        Assert.assertThrows(CsacInternalErrorException.class, () -> this.runtimeMetadataController.getRtContextMetadataList().getStatusCode());
    }

    @Test
    void getRtKpiMetadataForContext() {

        when(this.provisioningTracker.currentProvisioningState()).thenReturn(ProvisioningState.ofState(ProvisioningState.State.COMPLETED));
        when(this.runtimeMetadataRequestHandler.getContextKpiMetadata(any(KpiContextId.class))).thenReturn(new ArrayList<>());
        Assert.assertThrows(CsacNotFoundException.class,
                () -> this.runtimeMetadataController.getRtKpiMetadataForContext("contextId").getStatusCode());

        when(this.provisioningTracker.currentProvisioningState()).thenReturn(ProvisioningState.ofState(ProvisioningState.State.COMPLETED));
        when(this.runtimeMetadataRequestHandler.getContextKpiMetadata(any(KpiContextId.class))).thenReturn(List.of(mock(RtKpiMetadataDto.class)));
        Assert.assertEquals(HttpStatus.OK, this.runtimeMetadataController.getRtKpiMetadataForContext("contextId").getStatusCode());

        when(this.provisioningTracker.currentProvisioningState()).thenReturn(ProvisioningState.ofState(ProvisioningState.State.INITIAL));
        Assert.assertThrows(CsacConflictStateException.class,
                () -> this.runtimeMetadataController.getRtKpiMetadataForContext("contextId").getStatusCode());

        when(this.provisioningTracker.currentProvisioningState()).thenReturn(ProvisioningState.ofState(ProvisioningState.State.ERROR));
        Assert.assertThrows(CsacInternalErrorException.class,
                () -> this.runtimeMetadataController.getRtKpiMetadataForContext("contextId").getStatusCode());
    }
}