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

import java.util.Optional;

import com.ericsson.oss.air.csac.model.PMSchemaDefinition;

/**
 * API for all {@link PMSchemaDefinition} repository operations.
 */
public interface PMSchemaDefinitionDao extends DaoRepository<PMSchemaDefinition, String> {

    /**
     * Check if a matching PM Schema definition exists. Returns true if all the fields match. Otherwise, returns false.
     *
     * @param pmSchemaDefinition the PM Schema definition to be matched
     * @return boolean returns true if all the fields match
     */
    default boolean isMatched(final PMSchemaDefinition pmSchemaDefinition) {

        final Optional<PMSchemaDefinition> matchedDefinition = this.findById(pmSchemaDefinition.getName());

        return matchedDefinition.isPresent() && matchedDefinition.get().equals(pmSchemaDefinition);
    }

    @Override
    default void deleteById(final String id) {
        throw new UnsupportedOperationException();
    }

    @Override
    default void delete(final PMSchemaDefinition entity) {
        throw new UnsupportedOperationException();
    }

    @Override
    default void deleteAllById(final Iterable<? extends String> ids) {
        throw new UnsupportedOperationException();
    }

    @Override
    default void deleteAll(final Iterable<? extends PMSchemaDefinition> entities) {
        throw new UnsupportedOperationException();
    }

    @Override
    default void deleteAll() {
        throw new UnsupportedOperationException();
    }

}
