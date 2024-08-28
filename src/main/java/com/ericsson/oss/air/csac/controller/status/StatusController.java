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

import java.util.List;

import com.ericsson.oss.air.api.StatusApi;
import com.ericsson.oss.air.api.model.RtProvisioningStateDto;
import com.ericsson.oss.air.csac.handler.request.ProvisioningStateRequestHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for CSAC status REST endpoints.
 */

@RestController
public class StatusController implements StatusApi {

    @Autowired
    private ProvisioningStateRequestHandler provisioningStateRequestHandler;

    @Override
    public ResponseEntity<RtProvisioningStateDto> getCurrentProvisioningStatus() {
        return ResponseEntity.ok(this.provisioningStateRequestHandler.getLatestProvisioningState());
    }

    @Override
    public ResponseEntity<List<RtProvisioningStateDto>> getProvisioningStatus() {
        return ResponseEntity.ok(this.provisioningStateRequestHandler.getProvisioningStates());
    }
}
