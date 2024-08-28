/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.repository;

import java.util.Optional;

/**
 * DAO base interface derived from the Spring Data {@code CrudRepository}.  This interface provides the list of methods shared by all DAO classes that
 * implement this interface.
 *
 * @param <T>
 *         persisted resource type
 * @param <K>
 *         primary key type for the persisted resource type
 */
public interface DaoRepository<T, K> {

    /**
     * Saves the provided entity to the DAO.
     *
     * @param entity
     *         entity to save
     * @param <S>
     *         generic entity type
     * @return the saved entity
     */
    <S extends T> S save(S entity);

    /**
     * Saves all entities in the provided {@code Iterable} to the DAO.
     *
     * @param entities
     *         iterable containing the entities to save
     * @param <S>
     *         generic entity type
     * @return an {@code Iterable} containing all the persisted entities
     */
    <S extends T> Iterable<S> saveAll(Iterable<S> entities);

    /**
     * Returns an {@code Optional} containing the entity represented by {@code id}.  If the entity does not exist, an empty {@code Optional} is
     * returned.
     *
     * @param id
     *         Id of the entity to return.
     * @return an {@code Optional} containing the entity represented by {@code id} or an empty {@code Optional} if the entity does not exist
     */
    Optional<T> findById(K id);

    /**
     * Returns {@code true} if the entity represented {@code id} exists in the DAO.
     *
     * @param id
     *         Id of the entity to return.
     * @return {@code true} if the entity represented {@code id} exists in the DAO, otherwise {@code false}
     */
    boolean existsById(K id);

    /**
     * Returns an {@code Iterable} containing all the entities in the DAO.
     *
     * @return an {@code Iterable} containing all the entities in the DAO
     */
    Iterable<T> findAll();

    /**
     * Returns an {@code Iterable} containing all the entities in the DAO which match the provided {@code Iterable} of Ids.
     *
     * @param ids
     *         an {@code Iterable} containing the Ids to search for
     * @return an {@code Iterable} containing all the entities in the DAO which match the provided {@code Iterable} of Ids
     */
    Iterable<T> findAllById(Iterable<K> ids);

    /**
     * Returns the number of entities in the DAO.
     *
     * @return the number of entities in the DAO
     */
    long count();

    /**
     * Deletes the entity represented by the specified Id from the DAO, if it exists.
     *
     * @param id
     *         Id of the entity to delete
     */
    void deleteById(K id);

    /**
     * Deletes the specified entity from the DAO, if it exists.
     *
     * @param entity
     *         entity to delete
     */
    void delete(T entity);

    /**
     * Deletes all entities matching the provided Ids from the DAO, if they exist.
     *
     * @param ids
     *         {@code Iterable} containing the entity Ids to delete
     */
    void deleteAllById(Iterable<? extends K> ids);

    /**
     * Deletes all the specified entities from the DAO, if they exist.
     *
     * @param entities
     *         {@code Iterable} containing the entities to delete
     */
    void deleteAll(Iterable<? extends T> entities);

    /**
     * Deletes all entities from the DAO.
     */
    void deleteAll();
}
