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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import com.ericsson.oss.air.csac.model.ProfileDefinition;
import com.ericsson.oss.air.csac.model.TestResourcesUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ProfileDefinitionDAOImplTest {

    private final ProfileDefinition profileDefinition1 = ProfileDefinition.builder()
            .name("profileDef1")
            .description("Test Profile Definition1")
            .context(List.of("context1"))
            .kpis(TestResourcesUtils.VALID_KPI_REFERENCE_LIST)
            .augmentation("aug1")
            .build();

    private final ProfileDefinition profileDefinition2 = ProfileDefinition.builder()
            .name("profileDef2")
            .description("Test Profile Definition2")
            .context(List.of("context2"))
            .kpis(TestResourcesUtils.VALID_KPI_REFERENCE_LIST)
            .build();

    private ProfileDefinitionDAOImpl testDao;

    @BeforeEach
    public void setUp() throws Exception {
        this.testDao = new ProfileDefinitionDAOImpl();

        this.testDao.save(profileDefinition1);

    }

    @Test
    void save() {

        // the save is invoked during set up
        assertEquals(1, this.testDao.findAll().size());
    }

    @Test
    void saveAll() {

        this.testDao.saveAll(List.of(profileDefinition2));
        assertEquals(2, this.testDao.findAll().size());

    }

    @Test
    void findAll() {

        assertEquals(1, this.testDao.findAll().size());
        assertEquals(profileDefinition1, this.testDao.findAll().get(0));
    }

    @Test
    void findById() {

        assertTrue(this.testDao.findById(profileDefinition1.getName()).isPresent());
        assertTrue(this.testDao.findById("Profile_do_not_exist").isEmpty());
    }

    @Test
    void totalProfileDefinitions() {

        assertEquals(1, this.testDao.totalProfileDefinitions());
    }

    @Test
    void clear() {

        assertEquals(1, this.testDao.totalProfileDefinitions());

        this.testDao.clear();

        assertEquals(0, this.testDao.totalProfileDefinitions());
    }
}
