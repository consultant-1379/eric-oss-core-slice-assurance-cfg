/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.repository;

import com.ericsson.oss.air.csac.model.runtime.ProvisioningState;

/**
 * API for all {@link ProvisioningState} repository operations.
 */
public interface ProvisioningStateDao extends DaoRepository<ProvisioningState, Integer> {

    /**
     * Returns the latest provisioning state from the persistent store.
     *
     * @return the latest provisioning state from the persistent store
     */
    ProvisioningState findLatest();

    /*
     * (non-javadoc)
     *
     * By default, this operation is not permitted for this DAO.
     */
    @Override
    default <S extends ProvisioningState> Iterable<S> saveAll(Iterable<S> entities) {
        throw new UnsupportedOperationException();
    }

    @Override
    default void deleteById(final Integer id) {
        throw new UnsupportedOperationException();
    }

    @Override
    default void delete(final ProvisioningState entity) {
        throw new UnsupportedOperationException();
    }

    @Override
    default void deleteAllById(final Iterable<? extends Integer> ids) {
        throw new UnsupportedOperationException();
    }

    @Override
    default void deleteAll(Iterable<? extends ProvisioningState> entities) {
        throw new UnsupportedOperationException();
    }

    @Override
    default void deleteAll() {
        throw new UnsupportedOperationException();
    }
}
