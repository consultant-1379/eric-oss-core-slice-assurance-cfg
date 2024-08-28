/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.model.runtime;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.ericsson.oss.air.exception.CsacProvisioningStateTransitionException;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * Data bean representing a provisioning state.  This bean will be used primarily to exchange data with the persistent store
 */
@AllArgsConstructor
@Getter
@Builder(toBuilder = true,
         setterPrefix = "with")
public class ProvisioningState implements Comparable<ProvisioningState> {

    /**
     * Enumeration of provisioning states.  The {@code INITIAL} state will always be created on CSAC deployment or upgrade
     * and is not persistable by the {@link com.ericsson.oss.air.csac.repository.ProvisioningStateDao}.
     */
    public enum State {
        INITIAL("STARTED", "RESET"),
        STARTED("COMPLETED", "ERROR", "RESET", "INTERRUPT"),
        INTERRUPT("RESET"),
        COMPLETED("STARTED", "RESET"),
        RESET("STARTED"),
        ERROR("STARTED", "RESET");

        private final Set<String> validNextStates;

        State(final String... validNextStateList) {
            this.validNextStates = new HashSet<>(List.of(validNextStateList));
        }

        /**
         * Returns a set of states that this state can transition <i>to</i>.
         *
         * @return a set of states that this state can transition to.
         */
        public Set<State> getValidNextStates() {
            return this.validNextStates.stream().map(State::valueOf).collect(Collectors.toSet());
        }

        /**
         * Checks the validity of the transition from this state to the specified to-state.
         *
         * @param toState to-state for this transition
         */
        public void checkStateTransition(final State toState) {

            if (!this.getValidNextStates().contains(toState)) {
                throw new CsacProvisioningStateTransitionException("Invalid provisioning state transition: " + this + " -> " + toState);
            }
        }

        /**
         * Returns a {@code State} enum constant matching the provided string.  This method is case-insensitive.
         *
         * @param stateName case-insensitive provisioning state name
         * @return a {@code State} matching the provided string
         * @throws IllegalArgumentException if the provided name does not match any enum constant
         * @throws NullPointerException     if the provided name is null
         */
        public static State fromString(final String stateName) {
            return State.valueOf(stateName.toUpperCase(Locale.ROOT));
        }
    }

    private Integer id;

    private Instant provisioningStartTime;

    private Instant provisioningEndTime;

    /**
     * Shorthand method that returns a ProvisioningState bean representing {@code STARTED} state. All other fields will be populated
     * by the persistent store.
     *
     * @return a ProvisioningState bean representing {@code STARTED} state
     */
    public static ProvisioningState started() {
        return ofState(State.STARTED);
    }

    /**
     * Shorthand method that returns a ProvisioningState bean representing the specified provisioning state.
     * <p>
     * All other fields will be populated by the persistent store.
     *
     * @param provisioningState target state for this {@code ProvisioningState} instance
     * @return a ProvisioningState bean representing the specified provisioning state.
     */
    public static ProvisioningState ofState(final State provisioningState) {

        return ProvisioningState.builder().withProvisioningState(provisioningState).build();
    }

    /**
     * Shorthand method that returns a ProvisioningState bean representing {@code COMPLETED} state. All other fields will be populated
     * by the persistent store.
     *
     * @return a ProvisioningState bean representing {@code COMPLETED} state
     */
    public static ProvisioningState completed() {
        return ofState(State.COMPLETED);
    }

    /**
     * Shorthand method that returns a ProvisioningState bean representing {@code ERROR} state. All other fields will be populated
     * by the persistent store.
     *
     * @return a ProvisioningState bean representing {@code ERROR} state
     */
    public static ProvisioningState error() {
        return ofState(State.ERROR);
    }

    @NotNull
    @Builder.Default
    private State provisioningState = State.STARTED;

    @Override
    public int hashCode() {
        return this.id.hashCode();
    }

    @Override
    public boolean equals(final Object other) {

        if (Objects.isNull(other) || !other.getClass().equals(ProvisioningState.class)) {
            return false;
        }

        return this.id.equals(((ProvisioningState) other).id);
    }

    @Override
    public int compareTo(final ProvisioningState other) {
        return this.id.compareTo(other.id);
    }
}
