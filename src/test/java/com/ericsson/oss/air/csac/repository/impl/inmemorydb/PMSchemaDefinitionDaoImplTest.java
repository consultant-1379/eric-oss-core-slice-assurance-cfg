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

import static com.ericsson.oss.air.csac.model.TestResourcesUtils.PM_SCHEMA_NAME;
import static com.ericsson.oss.air.csac.model.TestResourcesUtils.VALID_PM_SCHEMA_DEFINITION_PM_COUNTER;
import static com.ericsson.oss.air.csac.model.TestResourcesUtils.VALID_PM_SCHEMA_DEFINITION_WO_PM_COUNTERS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import com.ericsson.oss.air.csac.model.PMSchemaDefinition;
import com.ericsson.oss.air.csac.model.pmschema.SchemaURI;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.IteratorUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PMSchemaDefinitionDaoImplTest {

    private static final String PM_SCHEMA_NAME_A = "pm_schema_name_a";

    private static final PMSchemaDefinition PM_SCHEMA_DEFINITION_A = PMSchemaDefinition.builder()
            .name(PM_SCHEMA_NAME_A)
            .uri(SchemaURI.fromString("dc:foo|bar|" + PM_SCHEMA_NAME_A))
            .context(List.of("testContext"))
            .build();

    private static final String PM_SCHEMA_NAME_B = "pm_schema_name_b";

    private static final PMSchemaDefinition PM_SCHEMA_DEFINITION_B = PMSchemaDefinition.builder()
            .name(PM_SCHEMA_NAME_B)
            .uri(SchemaURI.fromString("dc:foo|bar|" + PM_SCHEMA_NAME_B))
            .context(List.of("testContext"))
            .build();

    private static final String PM_SCHEMA_NAME_C = "pm_schema_name_c";

    private static final PMSchemaDefinition PM_SCHEMA_DEFINITION_C = PMSchemaDefinition.builder()
            .name(PM_SCHEMA_NAME_C)
            .uri(SchemaURI.fromString("dc:foo|bar|" + PM_SCHEMA_NAME_C))
            .context(List.of("testContext"))
            .build();

    private PMSchemaDefinitionDaoImpl testDao;

    @BeforeEach
    void setUp() {
        this.testDao = new PMSchemaDefinitionDaoImpl();
    }

    @Test
    void isMatched_true() {

        this.testDao.save(VALID_PM_SCHEMA_DEFINITION_PM_COUNTER);

        assertTrue(this.testDao.isMatched(VALID_PM_SCHEMA_DEFINITION_PM_COUNTER));
    }

    @Test
    void isMatched_false() {
        assertFalse(this.testDao.isMatched(VALID_PM_SCHEMA_DEFINITION_PM_COUNTER));
    }

    @Test
    void deleteById() {
        assertThrows(UnsupportedOperationException.class, () -> this.testDao.deleteById(PM_SCHEMA_NAME));
    }

    @Test
    void delete() {
        assertThrows(UnsupportedOperationException.class, () -> this.testDao.delete(VALID_PM_SCHEMA_DEFINITION_PM_COUNTER));
    }

    @Test
    void deleteAllById() {
        assertThrows(UnsupportedOperationException.class, () -> this.testDao.deleteAllById(List.of(PM_SCHEMA_NAME)));
    }

    @Test
    void deleteAll() {
        assertThrows(UnsupportedOperationException.class, () -> this.testDao.deleteAll());
    }

    @Test
    void deleteAll_entities() {
        assertThrows(UnsupportedOperationException.class, () -> this.testDao.deleteAll(List.of(VALID_PM_SCHEMA_DEFINITION_PM_COUNTER)));
    }

    @Test
    void save_new() {

        assertEquals(0, this.testDao.count());

        final PMSchemaDefinition savedEntity = this.testDao.save(VALID_PM_SCHEMA_DEFINITION_PM_COUNTER);

        assertEquals(1, this.testDao.count());
        assertEquals(VALID_PM_SCHEMA_DEFINITION_PM_COUNTER, savedEntity);
    }

    @Test
    void save_update() {

        assertEquals(0, this.testDao.count());

        this.testDao.save(VALID_PM_SCHEMA_DEFINITION_PM_COUNTER);
        final PMSchemaDefinition updatedEntity = this.testDao.save(VALID_PM_SCHEMA_DEFINITION_WO_PM_COUNTERS);

        assertEquals(1, this.testDao.count());
        assertEquals(VALID_PM_SCHEMA_DEFINITION_WO_PM_COUNTERS, updatedEntity);
    }

    @Test
    void saveAll_new() {

        assertEquals(0, this.testDao.count());

        final List<PMSchemaDefinition> expected = List.of(VALID_PM_SCHEMA_DEFINITION_PM_COUNTER, PM_SCHEMA_DEFINITION_A);

        final Iterable<PMSchemaDefinition> actual = this.testDao.saveAll(List.of(VALID_PM_SCHEMA_DEFINITION_PM_COUNTER, PM_SCHEMA_DEFINITION_A));

        assertEquals(2, this.testDao.count());
        assertTrue(CollectionUtils.isEqualCollection(expected, IteratorUtils.toList(actual.iterator())));
    }

    @Test
    void saveAll_update() {

        assertEquals(0, this.testDao.count());

        final List<PMSchemaDefinition> expected = List.of(VALID_PM_SCHEMA_DEFINITION_WO_PM_COUNTERS, PM_SCHEMA_DEFINITION_A);

        this.testDao.saveAll(List.of(VALID_PM_SCHEMA_DEFINITION_PM_COUNTER, PM_SCHEMA_DEFINITION_A));
        final Iterable<PMSchemaDefinition> actual = this.testDao.saveAll(List.of(VALID_PM_SCHEMA_DEFINITION_WO_PM_COUNTERS));

        assertEquals(2, this.testDao.count());
        assertTrue(CollectionUtils.isEqualCollection(expected, IteratorUtils.toList(actual.iterator())));
    }

    @Test
    void findById() {

        this.testDao.saveAll(List.of(VALID_PM_SCHEMA_DEFINITION_PM_COUNTER, PM_SCHEMA_DEFINITION_A));

        assertTrue(this.testDao.findById(PM_SCHEMA_NAME_A).isPresent());

    }

    @Test
    void existsById() {

        this.testDao.saveAll(List.of(VALID_PM_SCHEMA_DEFINITION_PM_COUNTER, PM_SCHEMA_DEFINITION_A));

        assertTrue(this.testDao.existsById(PM_SCHEMA_NAME));
    }

    @Test
    void findAll() {

        this.testDao.saveAll(List.of(VALID_PM_SCHEMA_DEFINITION_PM_COUNTER, PM_SCHEMA_DEFINITION_A));

        final List<PMSchemaDefinition> expected = List.of(VALID_PM_SCHEMA_DEFINITION_PM_COUNTER, PM_SCHEMA_DEFINITION_A);

        final List<PMSchemaDefinition> actual = IteratorUtils.toList(this.testDao.findAll().iterator());

        assertEquals(2, actual.size());
        assertTrue(CollectionUtils.isEqualCollection(expected, actual));

    }

    @Test
    void findAllById() {

        this.testDao.saveAll(List.of(PM_SCHEMA_DEFINITION_A, PM_SCHEMA_DEFINITION_B, PM_SCHEMA_DEFINITION_C));

        final List<PMSchemaDefinition> expected = List.of(PM_SCHEMA_DEFINITION_A, PM_SCHEMA_DEFINITION_B);

        final List<PMSchemaDefinition> actual = IteratorUtils.toList(
                this.testDao.findAllById(List.of(PM_SCHEMA_NAME_A, PM_SCHEMA_NAME_B, PM_SCHEMA_NAME)).iterator());

        assertEquals(2, actual.size());
        assertTrue(CollectionUtils.isEqualCollection(expected, actual));

    }

    @Test
    void count() {

        assertEquals(0, this.testDao.count());

        this.testDao.saveAll(List.of(VALID_PM_SCHEMA_DEFINITION_PM_COUNTER, PM_SCHEMA_DEFINITION_A, PM_SCHEMA_DEFINITION_B, PM_SCHEMA_DEFINITION_C));

        assertEquals(4, this.testDao.count());

    }

    @Test
    void clear() {

        assertEquals(0, this.testDao.count());

        this.testDao.saveAll(List.of(VALID_PM_SCHEMA_DEFINITION_PM_COUNTER));

        assertEquals(1, this.testDao.count());

        this.testDao.clear();

        assertEquals(0, this.testDao.count());

    }
}