/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.repository.impl.inmemorydb;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.ericsson.oss.air.csac.model.runtime.ProvisioningState;
import com.ericsson.oss.air.csac.repository.ProvisioningStateDao;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

/**
 * In-memory implementation of the {@link com.ericsson.oss.air.csac.repository.ProvisioningStateDao} API.
 */
@Repository
@Profile({ "dry-run" })
public class ProvisioningStateDaoImpl implements ProvisioningStateDao {

    private final Map<Integer, ProvisioningState> rep = new HashMap<>();

    /**
     * Returns a default in-memory {@code ProvisioningStateDao} implementation seeded with an initial provisioning state record.
     */
    public ProvisioningStateDaoImpl() {

        // seed the representation with a dummy INITIAL entry to match the database
        final ProvisioningState initial = ProvisioningState.builder()
                .withProvisioningState(ProvisioningState.State.INITIAL)
                .withProvisioningStartTime(Instant.now())
                .withProvisioningEndTime(Instant.now())
                .withId(1)
                .build();

        this.rep.put(initial.getId(), initial);
    }

    @Override
    public ProvisioningState findLatest() {
        return this.rep.get(getMaxKey());
    }

    @Override
    public <S extends ProvisioningState> S save(final S entity) {

        final ProvisioningState latest = findLatest();

        latest.getProvisioningState().checkStateTransition(entity.getProvisioningState());

        return entity.getProvisioningState() == ProvisioningState.State.STARTED
                ? saveNew(entity)
                : saveUpdate(entity);
    }

    @Override
    public Optional<ProvisioningState> findById(final Integer id) {
        return Optional.ofNullable(this.rep.get(id));
    }

    @Override
    public boolean existsById(final Integer id) {
        return this.rep.containsKey(id);
    }

    @Override
    public Iterable<ProvisioningState> findAll() {
        return this.rep.values().stream().sorted().collect(Collectors.toList());
    }

    @Override
    public Iterable<ProvisioningState> findAllById(final Iterable<Integer> ids) {

        final Set<Integer> idSet = StreamSupport.stream(ids.spliterator(), false).collect(Collectors.toSet());

        return this.rep.entrySet().stream().filter(e -> idSet.contains(e.getKey())).map(Map.Entry::getValue).sorted().collect(Collectors.toList());
    }

    @Override
    public long count() {
        return this.rep.size();
    }

    /*
     * (non-javadoc)
     *
     * Returns the maximum key value from the internal representation.
     */
    private Integer getMaxKey() {
        return Collections.max(this.rep.keySet());
    }

    /*
     * (non-javadoc)
     *
     * Returns the next available key.
     */
    private Integer getNextKey() {
        return Integer.valueOf(getMaxKey().intValue() + 1);
    }

    /*
     * (non-javadoc)
     *
     * Saves the provided provisioning state as a new entry in the internal representation.
     */
    private <S extends ProvisioningState> S saveNew(final S entity) {

        final ProvisioningState provisioningState = ProvisioningState.builder()
                .withProvisioningState(entity.getProvisioningState())
                .withProvisioningStartTime(Instant.now())
                .withId(getNextKey())
                .build();

        this.rep.put(provisioningState.getId(), provisioningState);

        return (S) provisioningState;
    }

    /*
     * Saves the provided provisioning state as an update to the latest provisioning state.
     */
    private <S extends ProvisioningState> S saveUpdate(final S entity) {

        final ProvisioningState latest = findLatest();

        final ProvisioningState update = ProvisioningState.builder()
                .withProvisioningState(entity.getProvisioningState())
                .withProvisioningStartTime(latest.getProvisioningStartTime())
                .withProvisioningEndTime(Instant.now())
                .withId(latest.getId())
                .build();

        this.rep.put(update.getId(), update);

        return (S) update;
    }
}
