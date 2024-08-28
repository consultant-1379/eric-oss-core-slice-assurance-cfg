/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.repository.impl.inmemorydb;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import com.ericsson.oss.air.csac.model.runtime.index.DeployedIndexDefinitionDto;
import com.ericsson.oss.air.csac.repository.DeployedIndexDefinitionDao;
import lombok.NoArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

/**
 * In-memory implementation of the {@link DeployedIndexDefinitionDao}.
 */
@Repository
@NoArgsConstructor
@Profile({ "dry-run" })
public class DeployedIndexDefinitionDaoImpl implements DeployedIndexDefinitionDao, Clearable {

    private final Map<String, DeployedIndexDefinitionDto> impl = new HashMap<>();

    @Override
    public <S extends DeployedIndexDefinitionDto> Stream<S> stream() {
        return (Stream<S>) this.impl.values().stream();
    }

    @Override
    public <S extends DeployedIndexDefinitionDto> S save(final S entity) {
        return (S) this.impl.put(entity.indexDefinitionName(), entity);
    }

    @Override
    public <S extends DeployedIndexDefinitionDto> Iterable<S> saveAll(final Iterable<S> entities) {

        for (final S entity : entities) {
            this.impl.put(entity.indexDefinitionName(), entity);
        }

        return (Iterable<S>) this.impl.values();
    }

    @Override
    public Optional<DeployedIndexDefinitionDto> findById(final String s) {
        return Optional.ofNullable(this.impl.get(s));
    }

    @Override
    public boolean existsById(final String s) {
        return this.impl.containsKey(s);
    }

    @Override
    public Iterable<DeployedIndexDefinitionDto> findAll() {
        return this.impl.values();
    }

    @Override
    public Iterable<DeployedIndexDefinitionDto> findAllById(final Iterable<String> strings) {

        final List<DeployedIndexDefinitionDto> collection = new ArrayList<>();
        for (final String key : strings) {
            if (this.impl.containsKey(key)) {
                collection.add(this.impl.get(key));
            }
        }
        return Collections.unmodifiableList(collection);
    }

    @Override
    public long count() {
        return this.impl.size();
    }

    @Override
    public void deleteById(final String s) {
        this.impl.remove(s);
    }

    @Override
    public void delete(final DeployedIndexDefinitionDto entity) {
        this.impl.remove(entity.indexDefinitionName());
    }

    @Override
    public void deleteAllById(final Iterable<? extends String> strings) {

        for (final String key : strings) {
            this.impl.remove(key);
        }
    }

    @Override
    public void deleteAll(final Iterable<? extends DeployedIndexDefinitionDto> entities) {

        for (final DeployedIndexDefinitionDto elem : entities) {
            this.impl.remove(elem.indexDefinitionName());
        }
    }

    @Override
    public void deleteAll() {
        this.impl.clear();
    }

    @Override
    public void clear() {
        this.deleteAll();
    }
}
