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

import java.io.IOException;

import com.ericsson.oss.air.CsacEntryPoint;
import com.ericsson.oss.air.csac.handler.reset.ResetConfigurationHandler;
import com.ericsson.oss.air.csac.handler.status.ProvisioningTracker;
import com.ericsson.oss.air.csac.model.runtime.ProvisioningState;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.endpoint.web.annotation.RestControllerEndpoint;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * This endpoint exposes operations that can be performed on CSAC resources:
 *
 * <ol>
 *     <li>reset: resets the dictionary and runtime resources provisioned by CSAC in both the CSAC repository and all provisioned target systems</li>
 *     <li>reload: reloads the dictionary resources and initiates all enabled provisioning operations</li>
 * </ol>
 * <p>
 * <br/><strong>Reset</strong><br/>
 * <p>
 * Reset removes all runtime configuration from all target systems and clears the CSAC runtime and dictionary data stores.  Scenarios which benefit
 * from this capability include
 * <ul>
 *     <li>resetting configuration during KPI development to allow deployment of updates to existing configuration</li>
 *     <li>resetting a live Assurance deployment in the event of a consistency check failure</li>
 * </ul>
 * <p>
 * <br/><strong>Reload</strong><br/>
 * <p>
 * Reload loads the dictionary resources and initiates the full provisioning flow in the same way as restarting CSAC.
 * <p>
 * A scenario where this might be needed is after modifying one of the CSAC resource files when doing local development or updating one of the CSAC
 * resource config maps if deploying to a Kubernetes cluster.  If provisioning includes targets that do not support incremental updates, the reset
 * operation should be executed first.
 */
@Component
@RestControllerEndpoint(id = "resource")
@RequiredArgsConstructor
public class ResourceActuator {

    private final CsacEntryPoint csacEntryPoint;

    private final ResetConfigurationHandler resetHandler;

    private final ProvisioningTracker provisioningTracker;

    /**
     * Forces CSAC to reload its resource configuration.
     *
     * @return Simple string message indicating that the configuration has been reloaded
     * @throws IOException if an error occurs while loading resource files
     */
    @PostMapping("/reload")
    public ResponseEntity<String> reloadResources() throws IOException {

        if (ProvisioningState.State.STARTED == this.provisioningTracker.currentProvisioningState().getProvisioningState()) {
            throw new UnsupportedOperationException(
                    "Unable to reload resources. Conflict with current provisioning state: " + ProvisioningState.State.STARTED.name());
        }

        this.csacEntryPoint.startProvisioning();

        return ResponseEntity.ok("CSAC resource configuration reloaded\n");
    }

    /**
     * Deletes all provisioned configuration in target systems and purges the CSAC runtime and dictionary data stores.  This method is fully
     * re-entrant.
     *
     * @return Empty response with a 204 - No Content stats code.
     */
    @DeleteMapping("/reset")
    public ResponseEntity<Void> resetResources(@Value("${provisioning.pmsc.restClient.legacy}") String legacyPmscEnabled) {

        if (ProvisioningState.State.STARTED == this.provisioningTracker.currentProvisioningState().getProvisioningState()) {
            throw new UnsupportedOperationException(
                    "Unable to reset resources. Conflict with current provisioning state: " + ProvisioningState.State.STARTED.name());
        }

        if (Boolean.valueOf(legacyPmscEnabled) == Boolean.TRUE) {
            throw new UnsupportedOperationException("Configuration reset is not supported for the legacy PMSC service");
        }

        // this will be replaced by the reset handler when it is available.
        this.resetHandler.apply();

        return ResponseEntity.noContent().build();
    }

}
