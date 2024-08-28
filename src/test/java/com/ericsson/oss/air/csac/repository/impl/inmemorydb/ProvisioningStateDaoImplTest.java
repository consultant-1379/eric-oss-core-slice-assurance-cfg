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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.ericsson.oss.air.csac.model.runtime.ProvisioningState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ProvisioningStateDaoImplTest {

    private ProvisioningStateDaoImpl provisioningStateDao;

    @BeforeEach
    void setUp() {
        this.provisioningStateDao = new ProvisioningStateDaoImpl();
    }

    @Test
    void findLatest() {

        final ProvisioningState initial = this.provisioningStateDao.findLatest();

        assertEquals(1, initial.getId());
        assertEquals(ProvisioningState.State.INITIAL, initial.getProvisioningState());
        assertNotNull(initial.getProvisioningStartTime());
        assertNotNull(initial.getProvisioningEndTime());
    }

    @Test
    void save_new() {

        this.provisioningStateDao.save(ProvisioningState.started());

        final ProvisioningState actual = this.provisioningStateDao.findLatest();

        assertEquals(ProvisioningState.State.STARTED, actual.getProvisioningState());
        assertNotNull(actual.getProvisioningStartTime());
        assertNull(actual.getProvisioningEndTime());
        assertEquals(2, actual.getId());
    }

    @Test
    void save_update() {

        this.provisioningStateDao.save(ProvisioningState.started());

        final ProvisioningState expected = this.provisioningStateDao.findLatest();

        this.provisioningStateDao.save(ProvisioningState.completed());

        final ProvisioningState actual = this.provisioningStateDao.findLatest();

        assertEquals(ProvisioningState.State.COMPLETED, actual.getProvisioningState());
        assertNotNull(actual.getProvisioningStartTime());
        assertNotNull(actual.getProvisioningEndTime());
        assertEquals(2, actual.getId());
        assertEquals(expected, actual);
    }

    @Test
    void saveAll() {

        assertThrows(UnsupportedOperationException.class, () -> this.provisioningStateDao.saveAll(List.of()));
    }

    @Test
    void findById() {

        this.provisioningStateDao.save(ProvisioningState.started());

        // should return the initial state
        final Optional<ProvisioningState> actual = this.provisioningStateDao.findById(1);

        assertTrue(actual.isPresent());
        assertEquals(ProvisioningState.State.INITIAL, actual.get().getProvisioningState());

        assertTrue(this.provisioningStateDao.findById(Integer.MAX_VALUE).isEmpty());
    }

    @Test
    void existsById() {

        assertTrue(this.provisioningStateDao.existsById(1));

        this.provisioningStateDao.save(ProvisioningState.started());

        assertTrue(this.provisioningStateDao.existsById(2));

        assertFalse(this.provisioningStateDao.existsById(Integer.MAX_VALUE));
    }

    @Test
    void findAll() {

        this.provisioningStateDao.save(ProvisioningState.started());
        this.provisioningStateDao.save(ProvisioningState.completed());

        this.provisioningStateDao.save(ProvisioningState.started());
        this.provisioningStateDao.save(ProvisioningState.completed());

        final List<ProvisioningState> expected = ((List<ProvisioningState>) this.provisioningStateDao.findAll()).stream().sorted()
                .collect(Collectors.toList());
        final List<ProvisioningState> actual = (List<ProvisioningState>) this.provisioningStateDao.findAll();

        assertFalse(actual.isEmpty());
        assertEquals(3, actual.size());
        assertEquals(expected, actual);
    }

    @Test
    void findAllById() {

        this.provisioningStateDao.save(ProvisioningState.started());
        this.provisioningStateDao.save(ProvisioningState.completed());

        this.provisioningStateDao.save(ProvisioningState.started());
        this.provisioningStateDao.save(ProvisioningState.completed());

        this.provisioningStateDao.save(ProvisioningState.started());
        this.provisioningStateDao.save(ProvisioningState.completed());

        this.provisioningStateDao.save(ProvisioningState.started());
        this.provisioningStateDao.save(ProvisioningState.completed());

        assertEquals(5, this.provisioningStateDao.count());

        final Set<Integer> idSet = Set.of(1, 3);

        final List<ProvisioningState> actual = (List<ProvisioningState>) this.provisioningStateDao.findAllById(idSet);

        assertEquals(2, actual.size());
        assertEquals(1, actual.get(0).getId());
        assertEquals(3, actual.get(1).getId());

    }

    @Test
    void count() {

        assertEquals(1, this.provisioningStateDao.count());

        this.provisioningStateDao.save(ProvisioningState.started());
        this.provisioningStateDao.save(ProvisioningState.completed());

        this.provisioningStateDao.save(ProvisioningState.started());
        this.provisioningStateDao.save(ProvisioningState.completed());

        assertEquals(3, this.provisioningStateDao.count());
    }

    @Test
    void deleteById() {
        assertThrows(UnsupportedOperationException.class, () -> this.provisioningStateDao.deleteById(1));
    }

    @Test
    void delete() {
        assertThrows(UnsupportedOperationException.class, () -> this.provisioningStateDao.delete(ProvisioningState.started()));
    }

    @Test
    void deleteAllById() {
        assertThrows(UnsupportedOperationException.class, () -> this.provisioningStateDao.deleteAllById(Set.of()));
    }

    @Test
    void deleteAll() {
        assertThrows(UnsupportedOperationException.class, () -> this.provisioningStateDao.deleteAll());
    }

    @Test
    void deleteAll_entities() {
        assertThrows(UnsupportedOperationException.class, () -> this.provisioningStateDao.deleteAll(List.of(ProvisioningState.started())));
    }
}