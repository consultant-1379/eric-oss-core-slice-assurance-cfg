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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import com.ericsson.oss.air.csac.model.AugmentationDefinition;
import com.ericsson.oss.air.csac.model.AugmentationRule;
import com.ericsson.oss.air.csac.model.AugmentationRuleField;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AugmentationDefinitionDAOImplTest {

    private AugmentationDefinitionDAOImpl testDao;

    private final AugmentationRuleField field1 = AugmentationRuleField.builder()
            .inputFields(List.of("input"))
            .output("field")
            .build();

    private final AugmentationRule testRule = AugmentationRule.builder()
            .fields(List.of(field1))
            .inputSchemaReference("input|schema|reference")
            .build();

    private final AugmentationDefinition testDef = AugmentationDefinition.builder()
            .name("ardq")
            .augmentationRules(List.of(testRule))
            .build();

    private final AugmentationDefinition testDef2 = AugmentationDefinition.builder()
            .name("ardq2")
            .augmentationRules(List.of(testRule))
            .build();

    private final AugmentationDefinition testDef3 = AugmentationDefinition.builder()
            .name("ardq3")
            .augmentationRules(List.of(testRule))
            .build();

    @BeforeEach
    public void setUp() throws Exception {
        this.testDao = new AugmentationDefinitionDAOImpl();

        this.testDao.save(this.testDef);

    }

    @Test
    void save() {

        // the save is invoked during set up
        assertEquals(1, this.testDao.findAll().size());
    }

    @Test
    void saveAll() {

        this.testDao.saveAll(List.of(testDef2, testDef3));
        assertEquals(3, this.testDao.findAll().size());

    }

    @Test
    void findAll() {

        assertEquals(1, this.testDao.findAll().size());
        assertEquals(this.testDef, this.testDao.findAll().get(0));
    }

    @Test
    void findById() {

        assertTrue(this.testDao.findById("ardq").isPresent());
        assertTrue(this.testDao.findById("not_ardq").isEmpty());
    }

    @Test
    void delete() {

        this.testDao.delete("not_ardq");
        assertFalse(this.testDao.findAll().isEmpty());

        this.testDao.delete("ardq");
        assertTrue(this.testDao.findAll().isEmpty());
    }

    @Test
    void totalAugmentationDefinitions() {

        assertEquals(1, this.testDao.totalAugmentationDefinitions());
    }

    @Test
    void testFindAll_start0_rows1() {

        assertEquals(1, this.testDao.findAll(0, 1).size());

    }

    @Test
    void testFindAll_start0_rows10() {

        assertEquals(1, this.testDao.findAll(0, 10).size());

    }

    @Test
    void testFindAll_start0_negativeRows() {

        assertEquals(0, this.testDao.findAll(0, -1).size());

    }

    @Test
    void clear() {

        assertEquals(1, this.testDao.findAll(0, 10).size());

        this.testDao.clear();

        assertEquals(0, this.testDao.findAll(0, 10).size());
    }
}