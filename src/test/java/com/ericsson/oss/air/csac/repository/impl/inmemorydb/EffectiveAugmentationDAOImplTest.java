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

class EffectiveAugmentationDAOImplTest {

    private EffectiveAugmentationDAOImpl testDao;

    private final AugmentationDefinition testDef = AugmentationDefinition.builder()
            .name("test")
            .url("http://test.org:8080")
            .augmentationRules(List.of(AugmentationRule.builder()
                    .inputSchemaReference("schema")
                    .fields(List.of(AugmentationRuleField.builder()
                            .output("field")
                            .inputFields(List.of("input")).build())
                    ).build())).build();

    private final List<String> testProfiles = List.of("profile");

    @BeforeEach
    public void setUp() throws Exception {
        this.testDao = new EffectiveAugmentationDAOImpl();
        this.testDao.save(this.testDef, this.testProfiles);
    }

    @Test
    void findAll() {

        final List<AugmentationDefinition> expected = List.of(this.testDef);

        final List<AugmentationDefinition> actual = this.testDao.findAll();
        assertEquals(1, actual.size());
        assertEquals(expected, actual);
    }

    @Test
    void findById() {

        assertTrue(this.testDao.findById("test").isPresent());
        assertTrue(this.testDao.findById("notAnArdq").isEmpty());
    }

    @Test
    void delete() {

        this.testDao.delete("test");

        assertEquals(0, this.testDao.findAll().size());
        assertEquals(0, this.testDao.findAllProfileNames("test").size());
    }

    @Test
    void findAllProfileNames() {

        final List<String> expected = List.of("profile");
        final List<String> actual = this.testDao.findAllProfileNames("test");

        assertEquals(1, actual.size());
        assertEquals(expected, actual);

    }

    @Test
    void totalEffectiveAugmentations() {
        assertEquals(1, this.testDao.totalEffectiveAugmentations());
    }

    @Test
    void clear() {

        assertFalse(this.testDao.findAll().isEmpty());

        this.testDao.clear();

        assertTrue(this.testDao.findAll().isEmpty());
    }
}