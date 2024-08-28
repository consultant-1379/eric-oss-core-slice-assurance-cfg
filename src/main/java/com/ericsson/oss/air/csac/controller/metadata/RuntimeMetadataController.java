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

import java.util.List;

import com.ericsson.oss.air.api.RuntimeMetadataApi;
import com.ericsson.oss.air.api.model.RtContextMetadataDto;
import com.ericsson.oss.air.api.model.RtKpiMetadataDto;
import com.ericsson.oss.air.csac.handler.request.RuntimeMetadataRequestHandler;
import com.ericsson.oss.air.csac.handler.status.ProvisioningTracker;
import com.ericsson.oss.air.csac.model.runtime.ProvisioningState;
import com.ericsson.oss.air.csac.model.runtime.metadata.KpiContextId;
import com.ericsson.oss.air.exception.CsacConflictStateException;
import com.ericsson.oss.air.exception.CsacInternalErrorException;
import com.ericsson.oss.air.exception.CsacNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/**
 * The type Runtime metadata controller.
 */
@RestController
@Slf4j
public class RuntimeMetadataController implements RuntimeMetadataApi {

    public static final String CONFLICT_ERROR_MESSAGE = "Conflict with provisioning state: %s. Runtime metadata computation failed. Retry may be successful later";

    public static final String INTERNAL_ERROR_MESSAGE = "Conflict with provisioning state: %s. Runtime metadata computation failed. Manual intervention is required";

    @Autowired
    private RuntimeMetadataRequestHandler runtimeMetadataRequestHandler;

    @Autowired
    private ProvisioningTracker provisioningTracker;

    @Override
    public ResponseEntity<List<RtContextMetadataDto>> getRtContextMetadataList() {
        final ProvisioningState.State currentProvisioningState = this.provisioningTracker.currentProvisioningState().getProvisioningState();
        switch (currentProvisioningState) {
            case INITIAL, STARTED -> throw new CsacConflictStateException(this.getConflictMessage(currentProvisioningState));
            case ERROR, INTERRUPT, RESET -> throw new CsacInternalErrorException(this.getInternalErrorMessage(currentProvisioningState));
            default -> {
                final List<RtContextMetadataDto> contextMetadataList = this.runtimeMetadataRequestHandler.getContextMetadata();
                return ResponseEntity.ok(contextMetadataList);
            }
        }
    }

    @Override
    public ResponseEntity<List<RtKpiMetadataDto>> getRtKpiMetadataForContext(final String contextId) {
        final ProvisioningState.State currentProvisioningState = this.provisioningTracker.currentProvisioningState().getProvisioningState();
        switch (currentProvisioningState) {
            case INITIAL, STARTED -> throw new CsacConflictStateException(this.getConflictMessage(currentProvisioningState));
            case ERROR, INTERRUPT, RESET -> throw new CsacInternalErrorException(this.getInternalErrorMessage(currentProvisioningState));
            default -> {
                final List<RtKpiMetadataDto> contextMetadataList = this.runtimeMetadataRequestHandler.getContextKpiMetadata(
                        KpiContextId.of(contextId));

                if (contextMetadataList.isEmpty()) {
                    throw new CsacNotFoundException("The requested resource was not found on this server.");
                }
                return ResponseEntity.ok(contextMetadataList);
            }
        }
    }

    private String getConflictMessage(final ProvisioningState.State state) {
        return String.format(CONFLICT_ERROR_MESSAGE, state.name());
    }

    private String getInternalErrorMessage(final ProvisioningState.State state) {
        return String.format(INTERNAL_ERROR_MESSAGE, state.name());
    }

}
