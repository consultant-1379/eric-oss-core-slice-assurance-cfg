/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.repository.impl.jdbc;

import java.util.Optional;

import com.ericsson.oss.air.csac.model.PMSchemaDefinition;
import com.ericsson.oss.air.csac.repository.PMSchemaDefinitionDao;
import com.ericsson.oss.air.csac.repository.impl.inmemorydb.PMSchemaDefinitionDaoImpl;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

/**
 * JDBC implementation of the {@link PMSchemaDefinitionDao} API.
 */
@Slf4j
@Repository
@Primary
@AllArgsConstructor
@Profile({ "prod", "test" })
public class PMSchemaDefinitionDaoJdbcImpl implements PMSchemaDefinitionDao {

    // ESOA-16346 will replace this with the JDBC implementation.
    private final PMSchemaDefinitionDao stubDaoImpl = new PMSchemaDefinitionDaoImpl();

    @Override
    public <S extends PMSchemaDefinition> S save(final S entity) {
        return this.stubDaoImpl.save(entity);
    }

    @Override
    public <S extends PMSchemaDefinition> Iterable<S> saveAll(final Iterable<S> entities) {
        return this.stubDaoImpl.saveAll(entities);
    }

    @Override
    public Optional<PMSchemaDefinition> findById(final String id) {
        return this.stubDaoImpl.findById(id);
    }

    @Override
    public boolean existsById(final String id) {
        return this.stubDaoImpl.existsById(id);
    }

    @Override
    public Iterable<PMSchemaDefinition> findAll() {
        return this.stubDaoImpl.findAll();
    }

    @Override
    public Iterable<PMSchemaDefinition> findAllById(final Iterable<String> ids) {
        return this.stubDaoImpl.findAllById(ids);
    }

    @Override
    public long count() {
        return this.stubDaoImpl.count();
    }

}
