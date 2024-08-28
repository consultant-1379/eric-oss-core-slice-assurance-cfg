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

import java.util.Objects;

import com.ericsson.oss.air.csac.model.runtime.ProvisioningState;
import com.ericsson.oss.air.csac.repository.ProvisioningStateDao;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Component;

/**
 * This component tracks the provisioning state in CSAC.  Provisioning is tracked by creating or updating provisioning status
 * markers in the CSAC peristent store.  CSAC components that initiate provisioning or are otherwise dependent on the current provisioning
 * status must use this component to ensure that the operations they are performing are safe.
 */
@Component
@AllArgsConstructor
@Slf4j
public class ProvisioningTracker implements ApplicationListener<ContextClosedEvent> {

    private ProvisioningStateDao provisioningStateDao;

    /**
     * Creates the marker indicating the start of a new provisioning operation.
     */
    public void startProvisioning() {
        log.info("Provisioning started");
        this.persistProvisioningState(ProvisioningState.started());
    }

    /**
     * Create the marker indicating the successful end of the current provisioning operation.
     */
    public void stopProvisioning() {
        log.info("Provisioning completed");
        this.persistProvisioningState(ProvisioningState.completed());
    }

    /**
     * Create the marker indicating the unsuccessful end of the current provisioning operation.
     *
     * @param cause cause of the provisioning failure.
     */
    public void stopProvisioning(final Throwable cause) {

        Objects.requireNonNull(cause);

        log.error("Provisioning error: {}", cause.getMessage());
        this.persistProvisioningState(ProvisioningState.error());
    }

    /**
     * Create the marker indicating the unsuccessful end of the current provisioning operation caused by system interrupt signal
     */
    public void interruptProvisioning() {
        log.info("Changing provisioning state from {} -> {}", ProvisioningState.State.STARTED, ProvisioningState.State.INTERRUPT);
        this.persistProvisioningState(ProvisioningState.ofState(ProvisioningState.State.INTERRUPT));
    }

    public void resetProvisioning() {
        log.info("Resetting provisioning state");

        final ProvisioningState resetState = ProvisioningState.builder()
                .withProvisioningState(ProvisioningState.State.RESET)
                .build();

        this.persistProvisioningState(resetState);
    }

    /**
     * Returns the current provisioning state.
     *
     * @return the current provisioning state.
     */
    public ProvisioningState currentProvisioningState() {
        return this.provisioningStateDao.findLatest();
    }

    private void persistProvisioningState(final ProvisioningState state) {
        this.provisioningStateDao.save(state);
    }

    /**
     * Monitor the ContextClosedEvent and modify the state of the provisioning tracker if it is currently set to STARTED
     *
     * @param event ContextClosedEvent event
     */
    @Override
    public void onApplicationEvent(final @NonNull ContextClosedEvent event) {
        log.info("Application Context Close event received");
        final ProvisioningState latest = this.provisioningStateDao.findLatest();
        if (latest.getProvisioningState().equals(ProvisioningState.State.STARTED)) {
            this.interruptProvisioning();
        }
    }
}
