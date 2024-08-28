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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.ericsson.oss.air.csac.model.PMSchemaDefinition;
import com.ericsson.oss.air.csac.repository.PMSchemaDefinitionDao;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

/**
 * In-memory implementation of the {@link com.ericsson.oss.air.csac.repository.PMSchemaDefinitionDao} API.
 */
@Repository
@Profile({ "dry-run" })
public class PMSchemaDefinitionDaoImpl implements PMSchemaDefinitionDao, Clearable {

    private final Map<String, PMSchemaDefinition> pmSchemaMap = new HashMap<>();

    @Override
    public <S extends PMSchemaDefinition> S save(final S entity) {
        this.pmSchemaMap.put(entity.getName(), entity);
        return entity;
    }

    @Override
    public <S extends PMSchemaDefinition> Iterable<S> saveAll(final Iterable<S> entities) {
        entities.forEach(entity -> this.pmSchemaMap.put(entity.getName(), entity));
        return (Iterable<S>) this.pmSchemaMap.values();
    }

    @Override
    public Optional<PMSchemaDefinition> findById(final String id) {
        return Optional.ofNullable(this.pmSchemaMap.get(id));
    }

    @Override
    public boolean existsById(final String id) {
        return this.pmSchemaMap.containsKey(id);
    }

    @Override
    public Iterable<PMSchemaDefinition> findAll() {
        return this.pmSchemaMap.values();
    }

    @Override
    public Iterable<PMSchemaDefinition> findAllById(final Iterable<String> ids) {

        final List<PMSchemaDefinition> collection = new ArrayList<>();
        for (final String key : ids) {
            if (this.pmSchemaMap.containsKey(key)) {
                collection.add(this.pmSchemaMap.get(key));
            }
        }
        return Collections.unmodifiableList(collection);
    }

    @Override
    public long count() {
        return this.pmSchemaMap.size();
    }

    @Override
    public void clear() {
        this.pmSchemaMap.clear();
    }
}
