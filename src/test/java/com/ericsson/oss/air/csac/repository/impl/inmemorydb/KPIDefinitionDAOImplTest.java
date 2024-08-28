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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.ericsson.oss.air.csac.model.InputMetric;
import com.ericsson.oss.air.csac.model.KPIDefinition;
import com.ericsson.oss.air.csac.model.PMDefinition;
import com.ericsson.oss.air.csac.model.TestResourcesUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class KPIDefinitionDAOImplTest {

    private static final String NAME_ORIG = "name";
    private static final String NAME_UPDATED = "new_name";

    private static final String DESCRIPTION_ORIG = "desc";
    private static final String DESCRIPTION_UPDATED = "new desc";

    private static final String EXPRESSION_ORIG = "expr";
    private static final String EXPRESSION_UPDATED = "new expr";

    private static final String DISPLAY_NAME_ORIG = "display name";
    private static final String DISPLAY_NAME_UPDATED = "new display name";

    private static final String AGGREGATION_TYPE_ORIG = "aggr type";
    private static final String AGGREGATION_TYPE_UPDATED = "new aggr type";

    private static final boolean IS_VISIBLE_ORIG = true;
    private static final boolean IS_VISIBLE_UPDATED = false;

    private static final List<InputMetric> INPUT_METRICS_ORIG = new ArrayList<>() {{
        add(new InputMetric("id", "alias", InputMetric.Type.PM_DATA));
    }};
    private static final List<InputMetric> INPUT_METRICS_UPDATED = new ArrayList<>() {{
        add(new InputMetric("new id", "new_alias", InputMetric.Type.KPI));
    }};
    private static final List<InputMetric> INPUT_METRICS_GENERATED = new ArrayList<>() {{
        add(new InputMetric("id", "alias", InputMetric.Type.PM_DATA));
        add(new InputMetric("new id", "new_alias", InputMetric.Type.KPI));
    }};

    private static final KPIDefinition KPI_DEF_ORIG = KPIDefinition.builder()
            .name(NAME_ORIG)
            .description(DESCRIPTION_ORIG)
            .displayName(DISPLAY_NAME_ORIG)
            .expression(EXPRESSION_ORIG)
            .aggregationType(AGGREGATION_TYPE_ORIG)
            .isVisible(IS_VISIBLE_ORIG)
            .inputMetrics(INPUT_METRICS_ORIG)
            .build();

    private static final KPIDefinition KPI_DEF_UPDATED = new KPIDefinition(NAME_ORIG, DESCRIPTION_UPDATED, DISPLAY_NAME_UPDATED, EXPRESSION_UPDATED,
            AGGREGATION_TYPE_UPDATED, null, IS_VISIBLE_UPDATED, INPUT_METRICS_UPDATED);
    private static final KPIDefinition KPI_DEF_NEW = new KPIDefinition(NAME_UPDATED, DESCRIPTION_UPDATED, DISPLAY_NAME_UPDATED, EXPRESSION_UPDATED,
            AGGREGATION_TYPE_UPDATED, null, IS_VISIBLE_UPDATED, INPUT_METRICS_UPDATED);

    private KPIDefinitionDAOImpl kpiDefDAO;

    @BeforeEach
    public void setUp() {
        kpiDefDAO = new KPIDefinitionDAOImpl();
    }

    @AfterEach
    public void tearDown() {
        kpiDefDAO.clearInternalMap();
    }

    @Test
    void saveKPIDefinition() {
        kpiDefDAO.saveKPIDefinition(KPI_DEF_ORIG);

        assertEquals(1, kpiDefDAO.totalKPIDefinitions());
        assertEquals(KPI_DEF_ORIG, kpiDefDAO.findByKPIDefName(NAME_ORIG));
    }

    @Test
    void findByKPIDefName() {
        kpiDefDAO.saveKPIDefinition(KPI_DEF_ORIG);

        assertEquals(KPI_DEF_ORIG, kpiDefDAO.findByKPIDefName(NAME_ORIG));
    }

    @Test
    void getKPIDefDescription() {
        kpiDefDAO.saveKPIDefinition(KPI_DEF_UPDATED);

        assertEquals(DESCRIPTION_UPDATED, kpiDefDAO.getKPIDefDescription(NAME_ORIG));
        assertNull(kpiDefDAO.getKPIDefDescription("foo"));
    }

    @Test
    void getKPIDefDisplayName() {
        kpiDefDAO.saveKPIDefinition(KPI_DEF_UPDATED);

        assertEquals(DISPLAY_NAME_UPDATED, kpiDefDAO.getKPIDefDisplayName(NAME_ORIG));
        assertNull(kpiDefDAO.getKPIDefDisplayName("foo"));
    }

    @Test
    void getKPIDefExpression() {
        kpiDefDAO.saveKPIDefinition(KPI_DEF_UPDATED);

        assertEquals(EXPRESSION_UPDATED, kpiDefDAO.getKPIDefExpression(NAME_ORIG));
        assertNull(kpiDefDAO.getKPIDefExpression("foo"));
    }

    @Test
    void getKPIDefAggregationType() {
        kpiDefDAO.saveKPIDefinition(KPI_DEF_UPDATED);

        assertEquals(AGGREGATION_TYPE_UPDATED, kpiDefDAO.getKPIDefAggregationType(NAME_ORIG));
        assertNull(kpiDefDAO.getKPIDefAggregationType("foo"));
    }

    @Test
    void getKPIDefIsVisible() {
        kpiDefDAO.saveKPIDefinition(KPI_DEF_UPDATED);

        assertEquals(IS_VISIBLE_UPDATED, kpiDefDAO.getKPIDefIsVisible(NAME_ORIG));
        assertNull(kpiDefDAO.getKPIDefIsVisible("foo"));
    }

    @Test
    void getKPIDefInputMetrics() {
        kpiDefDAO.saveKPIDefinition(KPI_DEF_UPDATED);

        assertEquals(1, kpiDefDAO.getKPIDefInputMetrics(NAME_ORIG).size());
        assertEquals(INPUT_METRICS_UPDATED, kpiDefDAO.getKPIDefInputMetrics(NAME_ORIG));
        assertNull(kpiDefDAO.getKPIDefInputMetrics("foo"));
    }

    @Test
    void updateKPIDefinition() {
        kpiDefDAO.saveKPIDefinition(KPI_DEF_ORIG);

        KPIDefinitionDAOImpl kpiDefinitionDAO = new KPIDefinitionDAOImpl();

        kpiDefinitionDAO.saveKPIDefinition(KPI_DEF_ORIG);
        assertEquals(KPI_DEF_ORIG, kpiDefinitionDAO.findByKPIDefName(NAME_ORIG));

        kpiDefinitionDAO.saveKPIDefinition(KPI_DEF_UPDATED);
        assertEquals(1, kpiDefinitionDAO.totalKPIDefinitions());
        assertEquals(KPI_DEF_UPDATED, kpiDefinitionDAO.findByKPIDefName(NAME_ORIG));
    }

    @Test
    void updateKPIDefinitionDescription() {
        kpiDefDAO.saveKPIDefinition(KPI_DEF_ORIG);

        kpiDefDAO.updateKPIDefinitionDescription(NAME_ORIG, DESCRIPTION_UPDATED);
        assertEquals(DESCRIPTION_UPDATED, kpiDefDAO.getKPIDefDescription(NAME_ORIG));

        // should be NO change
        kpiDefDAO.updateKPIDefinitionDescription("foo", DESCRIPTION_UPDATED);
        assertEquals(DESCRIPTION_UPDATED, kpiDefDAO.getKPIDefDescription(NAME_ORIG));
    }

    @Test
    void updateKPIDefinitionDisplayName() {
        kpiDefDAO.saveKPIDefinition(KPI_DEF_ORIG);

        kpiDefDAO.updateKPIDefinitionDisplayName(NAME_ORIG, DISPLAY_NAME_UPDATED);
        assertEquals(DISPLAY_NAME_UPDATED, kpiDefDAO.getKPIDefDisplayName(NAME_ORIG));

        // should be NO change
        kpiDefDAO.updateKPIDefinitionDisplayName("foo", DISPLAY_NAME_UPDATED);
        assertEquals(DISPLAY_NAME_UPDATED, kpiDefDAO.getKPIDefDisplayName(NAME_ORIG));
    }

    @Test
    void updateKPIDefinitionExpression() {
        kpiDefDAO.saveKPIDefinition(KPI_DEF_ORIG);

        kpiDefDAO.updateKPIDefinitionExpression(NAME_ORIG, EXPRESSION_UPDATED);
        assertEquals(EXPRESSION_UPDATED, kpiDefDAO.getKPIDefExpression(NAME_ORIG));

        // should be NO change
        kpiDefDAO.updateKPIDefinitionExpression("foo", EXPRESSION_UPDATED);
        assertEquals(EXPRESSION_UPDATED, kpiDefDAO.getKPIDefExpression(NAME_ORIG));

    }

    @Test
    void updateKPIDefinitionAggregationType() {
        kpiDefDAO.saveKPIDefinition(KPI_DEF_ORIG);

        kpiDefDAO.updateKPIDefinitionAggregationType(NAME_ORIG, AGGREGATION_TYPE_UPDATED);
        assertEquals(AGGREGATION_TYPE_UPDATED, kpiDefDAO.getKPIDefAggregationType(NAME_ORIG));

        // should be NO change
        kpiDefDAO.updateKPIDefinitionAggregationType("foo", AGGREGATION_TYPE_UPDATED);
        assertEquals(AGGREGATION_TYPE_UPDATED, kpiDefDAO.getKPIDefAggregationType(NAME_ORIG));
    }

    @Test
    void updateKPIDefinitionIsVisible() {
        kpiDefDAO.saveKPIDefinition(KPI_DEF_ORIG);

        kpiDefDAO.updateKPIDefinitionIsVisible(NAME_ORIG, IS_VISIBLE_UPDATED);
        assertEquals(IS_VISIBLE_UPDATED, kpiDefDAO.getKPIDefIsVisible(NAME_ORIG));

        // should be NO change
        kpiDefDAO.updateKPIDefinitionIsVisible("foo", IS_VISIBLE_UPDATED);
        assertEquals(IS_VISIBLE_UPDATED, kpiDefDAO.getKPIDefIsVisible(NAME_ORIG));
    }

    @Test
    void updateKPIDefinitionInputMetricAlias() {
        kpiDefDAO.saveKPIDefinition(KPI_DEF_ORIG);

        kpiDefDAO.updateKPIDefinitionInputMetricAlias(NAME_ORIG, "id", "new alias");
        assertEquals("new alias", kpiDefDAO.getKPIDefInputMetrics(NAME_ORIG).get(0).getAlias());

        kpiDefDAO.updateKPIDefinitionInputMetricAlias(NAME_ORIG, "other", "alias");
        assertEquals("new alias", kpiDefDAO.getKPIDefInputMetrics(NAME_ORIG).get(0).getAlias());
    }

    @Test
    void updateKPIDefinitionInputMetricType() {
        kpiDefDAO.saveKPIDefinition(KPI_DEF_ORIG);

        kpiDefDAO.updateKPIDefinitionInputMetricType(NAME_ORIG, "id", InputMetric.Type.KPI);
        assertEquals(InputMetric.Type.KPI, kpiDefDAO.getKPIDefInputMetrics(NAME_ORIG).get(0).getType());

        kpiDefDAO.updateKPIDefinitionInputMetricType(NAME_ORIG, "other", InputMetric.Type.PM_DATA);
        assertEquals(InputMetric.Type.KPI, kpiDefDAO.getKPIDefInputMetrics(NAME_ORIG).get(0).getType());
    }

    @Test
    void replaceKPIDefinitionInputMetrics() {
        kpiDefDAO.saveKPIDefinition(KPI_DEF_ORIG.toBuilder().build());

        kpiDefDAO.replaceKPIDefinitionInputMetrics(NAME_ORIG, INPUT_METRICS_UPDATED);
        assertEquals(INPUT_METRICS_UPDATED, kpiDefDAO.getKPIDefInputMetrics(NAME_ORIG));
    }

    @Test
    void appendKPIDefinitionInputMetric() {
        kpiDefDAO.saveKPIDefinition(KPI_DEF_ORIG);

        List<InputMetric> INPUT_METRICS_ORIG = new ArrayList<>() {{
            add(new InputMetric("id", "alias", InputMetric.Type.PM_DATA));
        }};

        final KPIDefinition KPI_DEF_ORIG = new KPIDefinition(NAME_ORIG, DESCRIPTION_ORIG, DISPLAY_NAME_ORIG, EXPRESSION_ORIG, AGGREGATION_TYPE_ORIG,
                null,
                IS_VISIBLE_ORIG, INPUT_METRICS_ORIG);

        kpiDefDAO.saveKPIDefinition(KPI_DEF_ORIG);

        kpiDefDAO.appendKPIDefinitionInputMetric(NAME_ORIG, new InputMetric("new id", "new_alias", InputMetric.Type.KPI));
        assertEquals(2, kpiDefDAO.getKPIDefInputMetrics(NAME_ORIG).size());
        assertEquals(INPUT_METRICS_GENERATED, kpiDefDAO.getKPIDefInputMetrics(NAME_ORIG));
    }

    @Test
    void insertKPIDefinitions() {
        final List<KPIDefinition> kpiDefs = new ArrayList<>();
        kpiDefs.add(KPI_DEF_ORIG);
        kpiDefs.add(KPI_DEF_NEW);
        kpiDefDAO.insertKPIDefinitions(kpiDefs);

        assertEquals(KPI_DEF_ORIG, kpiDefDAO.findByKPIDefName(NAME_ORIG));
        assertEquals(KPI_DEF_NEW, kpiDefDAO.findByKPIDefName(NAME_UPDATED));
    }

    @Test
    void isMatched() {
        this.kpiDefDAO.saveKPIDefinition(KPI_DEF_ORIG);
        assertTrue(this.kpiDefDAO.isMatched(KPI_DEF_ORIG));
        assertFalse(this.kpiDefDAO.isMatched(KPI_DEF_NEW));
    }

    @Test
    void getAffectedKPIDefs() {

        final KPIDefinition kpiDefinition = KPIDefinition.builder()
                .name(NAME_ORIG)
                .description(DESCRIPTION_ORIG)
                .inputMetrics(List.of(new InputMetric("id", "alias", InputMetric.Type.PM_DATA)))
                .build();

        this.kpiDefDAO.saveKPIDefinition(kpiDefinition);
        Set<KPIDefinition> affectedKPIDefs = null;
        affectedKPIDefs = this.kpiDefDAO.getAffectedKPIDefs(Set.of(TestResourcesUtils.VALID_PM_DEF_OBJ));
        assertEquals(0, affectedKPIDefs.size());

        PMDefinition pmDefWithMatchedId = TestResourcesUtils.VALID_PM_DEF_OBJ.toBuilder().name("id").build();
        affectedKPIDefs = this.kpiDefDAO.getAffectedKPIDefs(Set.of(pmDefWithMatchedId));
        assertEquals(1, affectedKPIDefs.size());

    }

    @Test
    void getAffectedKPIDefsByGivenKpiDefList() {

        Set<KPIDefinition> affectedKPIDefs = null;
        PMDefinition pmDefWithMatchedId = null;
        affectedKPIDefs = KPIDefinitionDAOImpl.getAffectedKPIDefs(Set.of(TestResourcesUtils.VALID_PM_DEF_OBJ), List.of(KPI_DEF_ORIG));
        assertEquals(0, affectedKPIDefs.size());

        pmDefWithMatchedId = TestResourcesUtils.VALID_PM_DEF_OBJ.toBuilder().name("id").build();
        affectedKPIDefs = KPIDefinitionDAOImpl.getAffectedKPIDefs(Set.of(pmDefWithMatchedId), List.of(KPI_DEF_UPDATED));
        assertEquals(0, affectedKPIDefs.size());

        pmDefWithMatchedId = TestResourcesUtils.VALID_PM_DEF_OBJ.toBuilder().name("id").build();
        affectedKPIDefs = KPIDefinitionDAOImpl.getAffectedKPIDefs(Set.of(pmDefWithMatchedId), List.of(KPI_DEF_ORIG));
        assertEquals(1, affectedKPIDefs.size());
    }

    @Test
    void testGetAllKpiDefNames() {
        final List<KPIDefinition> kpiDefs = new ArrayList<>();
        kpiDefs.add(KPI_DEF_ORIG);
        kpiDefs.add(KPI_DEF_NEW);
        kpiDefDAO.insertKPIDefinitions(kpiDefs);
        assertEquals(2, this.kpiDefDAO.getAllKpiDefNames().size());
    }

    @Test
    void findAllKPIDefs() {
        kpiDefDAO.saveKPIDefinition(KPI_DEF_ORIG);
        kpiDefDAO.saveKPIDefinition(KPI_DEF_NEW);
        kpiDefDAO.saveKPIDefinition(
                new KPIDefinition("name1", "description1", "displayName1", "expression1", "agg1", null, true, INPUT_METRICS_ORIG)
        );
        List<KPIDefinition> kpiDefinitions = kpiDefDAO.findAllKPIDefs(0, 5);
        assertEquals(3, kpiDefinitions.size());
    }

    @Test
    void findAllKPIDefs_negativeRows() {
        kpiDefDAO.saveKPIDefinition(KPI_DEF_ORIG);
        kpiDefDAO.saveKPIDefinition(KPI_DEF_NEW);
        kpiDefDAO.saveKPIDefinition(
                new KPIDefinition("name1", "description1", "displayName1", "expression1", "agg1", null, true, INPUT_METRICS_ORIG)
        );
        List<KPIDefinition> kpiDefinitions = kpiDefDAO.findAllKPIDefs(0, -1);
        assertEquals(0, kpiDefinitions.size());
    }

    @Test
    void findAllKPIDefs_lessRowsThanFound_returnsQueriedRowsOnly() {
        kpiDefDAO.saveKPIDefinition(KPI_DEF_ORIG);
        kpiDefDAO.saveKPIDefinition(KPI_DEF_NEW);
        kpiDefDAO.saveKPIDefinition(
                new KPIDefinition("name1", "description1", "displayName1", "expression1", "agg1", null, true, INPUT_METRICS_ORIG)
        );
        List<KPIDefinition> kpiDefinitions = kpiDefDAO.findAllKPIDefs(0, 1);
        assertEquals(1, kpiDefinitions.size());

        kpiDefinitions = kpiDefDAO.findAllKPIDefs(0, 2);
        assertEquals(2, kpiDefinitions.size());
    }

    @Test
    void findAllKPIDefs_startFromSecondPage() {
        kpiDefDAO.saveKPIDefinition(KPI_DEF_ORIG);
        kpiDefDAO.saveKPIDefinition(KPI_DEF_NEW);
        kpiDefDAO.saveKPIDefinition(
                new KPIDefinition("name1", "description1", "displayName1", "expression1", "agg1", null, true, INPUT_METRICS_ORIG)
        );
        List<KPIDefinition> kpiDefinitions = kpiDefDAO.findAllKPIDefs(1, 2);
        assertEquals(1, kpiDefinitions.size());
    }

    @Test
    void findAllKPIDefs_startFromSecondPage_butOnlyOnePageData() {
        kpiDefDAO.saveKPIDefinition(KPI_DEF_ORIG);
        kpiDefDAO.saveKPIDefinition(KPI_DEF_NEW);
        kpiDefDAO.saveKPIDefinition(
                new KPIDefinition("name1", "description1", "displayName1", "expression1", "agg1", null, true, INPUT_METRICS_ORIG)
        );
        List<KPIDefinition> kpiDefinitions = kpiDefDAO.findAllKPIDefs(1, 5);
        assertEquals(0, kpiDefinitions.size());
    }

    @Test
    void findAllKPIDefs_EmptyKPIDefMap_EmptyListReturned() {
        assertEquals(0, kpiDefDAO.totalKPIDefinitions());

        List<KPIDefinition> kpiDefinitions = kpiDefDAO.findAllKPIDefs(0, 10);
        assertEquals(0, kpiDefinitions.size());

        kpiDefinitions = kpiDefDAO.findAllKPIDefs(0, -1);
        assertEquals(0, kpiDefinitions.size());
    }

    @Test
    void testFindAll() {

        kpiDefDAO.saveKPIDefinition(KPI_DEF_ORIG);
        kpiDefDAO.saveKPIDefinition(KPI_DEF_NEW);

        assertEquals(2, kpiDefDAO.findAll().size());

    }

    @Test
    void clear() {

        kpiDefDAO.saveKPIDefinition(KPI_DEF_ORIG);
        kpiDefDAO.saveKPIDefinition(KPI_DEF_NEW);

        assertEquals(2, kpiDefDAO.findAll().size());

        kpiDefDAO.clear();

        assertEquals(0, kpiDefDAO.findAll().size());
    }
}