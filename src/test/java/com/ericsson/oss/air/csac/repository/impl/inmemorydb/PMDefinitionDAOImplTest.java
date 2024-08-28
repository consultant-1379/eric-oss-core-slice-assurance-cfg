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
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import com.ericsson.oss.air.csac.model.PMDefinition;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PMDefinitionDAOImplTest {

    private static final String NAME_ORIG = "name";
    private static final String NAME_NEW = "new_name";

    private static final String SOURCE = "source";
    private static final String SOURCE_UPDATED = "new source";
    private static final String DESCRIPTION = "desc";
    private static final String DESCRIPTION_UPDATED = "new desc";

    private static final PMDefinition PM_DEF_ORIG = new PMDefinition(NAME_ORIG, SOURCE, DESCRIPTION);
    private static final PMDefinition PM_DEF = new PMDefinition(NAME_NEW, SOURCE, DESCRIPTION);
    private static final PMDefinition PM_DEF_UPDATED = new PMDefinition(NAME_ORIG, SOURCE_UPDATED, DESCRIPTION_UPDATED);

    private static final String SCHEMA_NAME = "schema_name";
    private static final String SCHEMA_NAME_UPDATED = "schema_name_updated";

    private PMDefinitionDAOImpl pmDefDAO;

    @BeforeEach
    void setup() {
        pmDefDAO = new PMDefinitionDAOImpl();
    }

    @AfterEach
    void tearDown() {
        pmDefDAO.clearInternalMaps();
    }

    @Test
    void savePMDefinition_nonExistedSchemaName() {
        pmDefDAO.savePMDefinition(PM_DEF, SCHEMA_NAME);

        assertEquals(1, pmDefDAO.totalPMDefinitions());
        assertEquals(1, pmDefDAO.totalSchemaNamesWithPMDefinitions());

        assertEquals(NAME_NEW, pmDefDAO.findByPMDefName(NAME_NEW).getName());
        assertEquals(PM_DEF, pmDefDAO.findByPMDefName(NAME_NEW));

        assertEquals(NAME_NEW, pmDefDAO.findAllBySchemaName(SCHEMA_NAME).get(0).getName());
        assertEquals(PM_DEF, pmDefDAO.findAllBySchemaName(SCHEMA_NAME).get(0));
    }

    @Test
    void savePMDefinition_existedSchemaNameWithDiffPMName() {
        HashSet<PMDefinition> pmDefinitions = null;
        HashSet<PMDefinition> expectedPmDefs = null;

        pmDefDAO.savePMDefinition(PM_DEF, SCHEMA_NAME);

        // after 1st save, check pmDefMapByPMName
        assertEquals(1, pmDefDAO.totalPMDefinitions());
        assertEquals(NAME_NEW, pmDefDAO.findByPMDefName(NAME_NEW).getName());
        assertEquals(PM_DEF, pmDefDAO.findByPMDefName(NAME_NEW));

        // after 1st save, check find all by schema name
        assertEquals(1, pmDefDAO.totalSchemaNamesWithPMDefinitions());

        pmDefinitions = new HashSet<>(pmDefDAO.findAllBySchemaName(SCHEMA_NAME));
        expectedPmDefs = new HashSet<>(List.of(PM_DEF));
        assertEquals(expectedPmDefs, pmDefinitions);

        pmDefDAO.savePMDefinition(PM_DEF_UPDATED, SCHEMA_NAME);

        // after 2nd save, check pmDefMapByPMName
        assertEquals(2, pmDefDAO.totalPMDefinitions());
        assertEquals(NAME_NEW, pmDefDAO.findByPMDefName(NAME_NEW).getName());
        assertEquals(PM_DEF, pmDefDAO.findByPMDefName(NAME_NEW));
        assertEquals(NAME_ORIG, pmDefDAO.findByPMDefName(NAME_ORIG).getName());
        assertEquals(PM_DEF_UPDATED, pmDefDAO.findByPMDefName(NAME_ORIG));

        // after 2nd save, check all by schema name
        assertEquals(1, pmDefDAO.totalSchemaNamesWithPMDefinitions());
        assertEquals(2, pmDefDAO.findPMDefNamesBySchemaName(SCHEMA_NAME).size());
        // Actual name is returned
        pmDefinitions = new HashSet<>(pmDefDAO.findAllBySchemaName(SCHEMA_NAME));
        expectedPmDefs = new HashSet<>(Arrays.asList(PM_DEF_UPDATED, PM_DEF));

        assertEquals(expectedPmDefs, pmDefinitions);
    }

    @Test
    void savePMDefinition_existedSchemaNameWithExistedPMName() {
        pmDefDAO.savePMDefinition(PM_DEF_ORIG, SCHEMA_NAME);

        // after 1st save, check pmDefMapByPMName
        assertEquals(1, pmDefDAO.totalPMDefinitions());
        assertEquals(NAME_ORIG, pmDefDAO.findByPMDefName(NAME_ORIG).getName());
        assertEquals(PM_DEF_ORIG, pmDefDAO.findByPMDefName(NAME_ORIG));

        // after 1st save, check pmDefMapBySchemaName
        assertEquals(1, pmDefDAO.totalSchemaNamesWithPMDefinitions());
        assertEquals(NAME_ORIG, pmDefDAO.findAllBySchemaName(SCHEMA_NAME).get(0).getName());
        assertEquals(PM_DEF_ORIG, pmDefDAO.findAllBySchemaName(SCHEMA_NAME).get(0));

        pmDefDAO.savePMDefinition(PM_DEF_UPDATED, SCHEMA_NAME);

        // after 2nd save, check pmDefMapByPMName
        assertEquals(1, pmDefDAO.totalPMDefinitions());
        assertEquals(NAME_ORIG, pmDefDAO.findByPMDefName(NAME_ORIG).getName());

        // after 2nd save, check pmDefMapBySchemaName
        assertEquals(1, pmDefDAO.totalSchemaNamesWithPMDefinitions());
        assertEquals(1, pmDefDAO.findPMDefNamesBySchemaName(SCHEMA_NAME).size());
        assertEquals(NAME_ORIG, pmDefDAO.findAllBySchemaName(SCHEMA_NAME).get(0).getName());
    }

    @Test
    void findSchemaByPMDefName() {
        pmDefDAO.savePMDefinition(PM_DEF, SCHEMA_NAME);

        assertEquals(SCHEMA_NAME, pmDefDAO.findSchemaByPMDefName(NAME_NEW));
    }

    @Test
    void findSchemaByPMDefName_onePMNameWithMultiSchemaNames() {
        pmDefDAO.savePMDefinition(PM_DEF, SCHEMA_NAME);
        pmDefDAO.savePMDefinition(PM_DEF, SCHEMA_NAME_UPDATED);

        assertEquals(SCHEMA_NAME_UPDATED, pmDefDAO.findSchemaByPMDefName(NAME_NEW));
    }

    @Test
    void findPMDefNameBySchemaName() {
        pmDefDAO.savePMDefinition(PM_DEF, SCHEMA_NAME);

        assertEquals(new HashSet<>(List.of(PM_DEF.getName())), pmDefDAO.findPMDefNamesBySchemaName(SCHEMA_NAME));
    }

    @Test
    void findAllByPMDefName() {
        pmDefDAO.savePMDefinition(PM_DEF, SCHEMA_NAME);

        assertEquals(PM_DEF, pmDefDAO.findByPMDefName(NAME_NEW));
    }

    @Test
    void findAllBySchemaName() {
        pmDefDAO.savePMDefinition(PM_DEF, SCHEMA_NAME);

        assertEquals(1, pmDefDAO.findPMDefNamesBySchemaName(SCHEMA_NAME).size());
        assertEquals(NAME_NEW, pmDefDAO.findAllBySchemaName(SCHEMA_NAME).get(0).getName());
        assertEquals(PM_DEF, pmDefDAO.findAllBySchemaName(SCHEMA_NAME).get(0));
    }

    @Test
    void getPMDefinitionSource() {
        pmDefDAO.savePMDefinition(PM_DEF_ORIG, SCHEMA_NAME);

        assertEquals(SOURCE, pmDefDAO.getPMDefinitionSource(NAME_ORIG));
        assertNull(pmDefDAO.getPMDefinitionSource("foo"));
    }

    @Test
    void getPMDefinitionDescription() {
        pmDefDAO.savePMDefinition(PM_DEF_ORIG, SCHEMA_NAME);

        assertEquals(DESCRIPTION, pmDefDAO.getPMDefinitionDescription(NAME_ORIG));
        assertNull(pmDefDAO.getPMDefinitionDescription("foo"));

    }

    @Test
    void updatePMDefinition_withExistedPMDef() {
        pmDefDAO = new PMDefinitionDAOImpl();

        // 1st entry
        pmDefDAO.savePMDefinition(PM_DEF_ORIG, SCHEMA_NAME);
        assertEquals(1, pmDefDAO.totalPMDefinitions());
        assertEquals(1, pmDefDAO.totalSchemaNamesWithPMDefinitions());

        // 2nd entry
        pmDefDAO.savePMDefinition(PM_DEF, SCHEMA_NAME);
        assertEquals(2, pmDefDAO.totalPMDefinitions());
        assertEquals(1, pmDefDAO.totalSchemaNamesWithPMDefinitions());

        // update the 1st entry
        pmDefDAO.updatePMDefinition(PM_DEF_UPDATED);

        // check pmDefMapByPMName MAP
        assertEquals(2, pmDefDAO.totalPMDefinitions());

        // check the 1st entry updated
        assertEquals(NAME_ORIG, pmDefDAO.findByPMDefName(NAME_ORIG).getName());
        assertEquals(SOURCE_UPDATED, pmDefDAO.findByPMDefName(NAME_ORIG).getSource());
        assertEquals(PM_DEF_UPDATED, pmDefDAO.findByPMDefName(NAME_ORIG));

        // check the 2nd entry unchanged
        assertEquals(NAME_NEW, pmDefDAO.findByPMDefName(NAME_NEW).getName());
        assertEquals(SOURCE, pmDefDAO.findByPMDefName(NAME_NEW).getSource());
        assertEquals(PM_DEF, pmDefDAO.findByPMDefName(NAME_NEW));

        // check Schema Reference Set size
        assertEquals(1, pmDefDAO.totalSchemaNamesWithPMDefinitions());

        final HashSet<PMDefinition> pmDefinitions = new HashSet<>(pmDefDAO.findAllBySchemaName(SCHEMA_NAME));
        final HashSet<PMDefinition> expectedPmDefs = new HashSet<>(Arrays.asList(PM_DEF_UPDATED, PM_DEF));

        assertEquals(expectedPmDefs, pmDefinitions);
    }

    @Test
    void updatePMDefinition_empty() {
        pmDefDAO = new PMDefinitionDAOImpl();
        assertEquals(0, pmDefDAO.totalPMDefinitions());
        assertEquals(0, pmDefDAO.totalSchemaNamesWithPMDefinitions());

        pmDefDAO.updatePMDefinition(PM_DEF);
        assertEquals(0, pmDefDAO.totalPMDefinitions());
        assertEquals(0, pmDefDAO.totalSchemaNamesWithPMDefinitions());
    }

    @Test
    void updatePMDefinition_nonExisted() {
        pmDefDAO = new PMDefinitionDAOImpl();
        pmDefDAO.savePMDefinition(PM_DEF_ORIG, SCHEMA_NAME);
        assertEquals(1, pmDefDAO.totalPMDefinitions());
        assertEquals(1, pmDefDAO.totalSchemaNamesWithPMDefinitions());

        pmDefDAO.updatePMDefinition(PM_DEF);
        assertEquals(1, pmDefDAO.totalPMDefinitions());
        assertEquals(1, pmDefDAO.totalSchemaNamesWithPMDefinitions());
    }

    @Test
    void insertPMDefinitions() {
        final Map<String, List<PMDefinition>> MAP_SCHEMA_NAME_PMDEFS = new HashMap<>();
        MAP_SCHEMA_NAME_PMDEFS.put(SCHEMA_NAME, Arrays.asList(PM_DEF_ORIG, PM_DEF));
        pmDefDAO.insertPMDefinitions(MAP_SCHEMA_NAME_PMDEFS);

        assertEquals(2, pmDefDAO.totalPMDefinitions());
        assertEquals(1, pmDefDAO.totalSchemaNamesWithPMDefinitions());

        final HashSet<PMDefinition> pmDefinitions = new HashSet<>(pmDefDAO.findAllBySchemaName(SCHEMA_NAME));
        final HashSet<PMDefinition> expectedPmDefs = new HashSet<>(Arrays.asList(PM_DEF_ORIG, PM_DEF));
        assertEquals(expectedPmDefs, pmDefinitions);
    }

    @Test
    void isMatched() {
        this.pmDefDAO.savePMDefinition(PM_DEF_ORIG, SCHEMA_NAME);
        Assertions.assertTrue(this.pmDefDAO.isMatched(PM_DEF_ORIG));
        Assertions.assertFalse(this.pmDefDAO.isMatched(PM_DEF_UPDATED));
        Assertions.assertFalse(this.pmDefDAO.isMatched(PM_DEF));
    }

    @Test
    void findAllPMDefinitions_3totalWith2RowsPage_2recordsOn1stPage_1recordOn2ndPage() {
        pmDefDAO = new PMDefinitionDAOImpl();
        pmDefDAO.savePMDefinition(PM_DEF_ORIG, SCHEMA_NAME);
        pmDefDAO.savePMDefinition(PM_DEF, SCHEMA_NAME);
        pmDefDAO.savePMDefinition(new PMDefinition("test", "test source", "test descr"), SCHEMA_NAME);

        // 1st page with 2 records (start=0 and rows=2)
        List<PMDefinition> pmDefinitionList = pmDefDAO.findAllPMDefinitions(0, 2);

        assertEquals(2, pmDefinitionList.size());
        assertEquals("test", pmDefinitionList.get(0).getName());
        assertEquals(NAME_ORIG, pmDefinitionList.get(1).getName());

        // 2nd page with 1 record (start=1 and rows=1)
        pmDefinitionList = pmDefDAO.findAllPMDefinitions(1, 1);
        assertEquals(1, pmDefinitionList.size());

        // 2nd page with 1 record (start=1 and rows=2)
        pmDefinitionList = pmDefDAO.findAllPMDefinitions(1, 2);
        assertEquals(1, pmDefinitionList.size());
    }

    @Test
    void findAllPMDefinitions_3totalWith3orMoreRowsPage_3recordsOn1stPage() {
        pmDefDAO = new PMDefinitionDAOImpl();
        pmDefDAO.savePMDefinition(PM_DEF_ORIG, SCHEMA_NAME);
        pmDefDAO.savePMDefinition(PM_DEF, SCHEMA_NAME);
        pmDefDAO.savePMDefinition(new PMDefinition("test", "test source", "test descr"), SCHEMA_NAME);

        // 1st page with 3 records (start=0 and rows=3)
        List<PMDefinition> pmDefinitionList = pmDefDAO.findAllPMDefinitions(0, 3);
        assertEquals(3, pmDefinitionList.size());

        // 1st page with 3 records (start=0 and rows=4)
        pmDefinitionList = pmDefDAO.findAllPMDefinitions(0, 4);
        assertEquals(3, pmDefinitionList.size());
    }

    @Test
    void findAllPMDefinitions_3totalWith3orMoreRowsPage_0recordsOn2ndPage_0recordsOn3rdPage() {
        pmDefDAO = new PMDefinitionDAOImpl();
        pmDefDAO.savePMDefinition(PM_DEF_ORIG, SCHEMA_NAME);
        pmDefDAO.savePMDefinition(PM_DEF, SCHEMA_NAME);
        pmDefDAO.savePMDefinition(new PMDefinition("test", "test source", "test descr"), SCHEMA_NAME);

        // 2nd page with 0 record (start=1 and rows=3)
        List<PMDefinition> pmDefinitionList = pmDefDAO.findAllPMDefinitions(1, 3);
        assertEquals(0, pmDefinitionList.size());

        // 2nd page with 0 record (start=1 and rows=4)
        pmDefinitionList = pmDefDAO.findAllPMDefinitions(1, 4);
        assertEquals(0, pmDefinitionList.size());

        // 3nd page with 0 record (start=2 and rows=3)
        pmDefinitionList = pmDefDAO.findAllPMDefinitions(2, 3);
        assertEquals(0, pmDefinitionList.size());
    }

    @Test
    void findAllPMDefinitions_2totalWithNegativeRowsPage_0record() {
        pmDefDAO = new PMDefinitionDAOImpl();
        pmDefDAO.savePMDefinition(PM_DEF_ORIG, SCHEMA_NAME);
        pmDefDAO.savePMDefinition(PM_DEF, SCHEMA_NAME);

        // 1st page with 0 record (start=0 and rows=-1)
        List<PMDefinition> pmDefinitionList = pmDefDAO.findAllPMDefinitions(0, -1);
        assertEquals(0, pmDefinitionList.size());

        // 2nd page with 0 record (start=1 and rows=-1)
        pmDefinitionList = pmDefDAO.findAllPMDefinitions(1, -1);
        assertEquals(0, pmDefinitionList.size());
    }

    @Test
    void findAllPMDefinitions_EmptyPMDefMap_EmptyListReturned() {
        pmDefDAO = new PMDefinitionDAOImpl();

        // With default values
        List<PMDefinition> pmDefinitionList = pmDefDAO.findAllPMDefinitions(0, 10);
        assertEquals(0, pmDefinitionList.size());

        // 1st page with 0 record (start=0 and rows=-1)
        pmDefinitionList = pmDefDAO.findAllPMDefinitions(0, -1);
        assertEquals(0, pmDefinitionList.size());
    }

    @Test
    void testGetAllPmDefNames() {
        final Map<String, List<PMDefinition>> MAP_SCHEMA_NAME_PMDEFS = new HashMap<>();
        MAP_SCHEMA_NAME_PMDEFS.put(SCHEMA_NAME, Arrays.asList(PM_DEF_ORIG, PM_DEF));
        pmDefDAO.insertPMDefinitions(MAP_SCHEMA_NAME_PMDEFS);
        assertEquals(2, this.pmDefDAO.getAllPmDefNames().size());
    }

    @Test
    void clear() {

        final Map<String, List<PMDefinition>> MAP_SCHEMA_NAME_PMDEFS = new HashMap<>();
        MAP_SCHEMA_NAME_PMDEFS.put(SCHEMA_NAME, Arrays.asList(PM_DEF_ORIG, PM_DEF));
        pmDefDAO.insertPMDefinitions(MAP_SCHEMA_NAME_PMDEFS);

        assertEquals(2, this.pmDefDAO.getAllPmDefNames().size());

        this.pmDefDAO.clear();

        assertEquals(0, this.pmDefDAO.getAllPmDefNames().size());
    }
}