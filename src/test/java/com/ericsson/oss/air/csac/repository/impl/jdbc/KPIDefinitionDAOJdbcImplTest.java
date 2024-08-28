/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.repository.impl.jdbc;

import static com.ericsson.oss.air.csac.model.TestResourcesUtils.VALID_LIST_KPI_DEF_OBJ;
import static com.ericsson.oss.air.csac.model.TestResourcesUtils.VALID_PM_DEF_OBJ;
import static com.ericsson.oss.air.csac.repository.impl.jdbc.KPIDefinitionDAOJdbcImpl.SELECT_ALL_KPI_STATEMENT;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.lenient;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.ericsson.oss.air.csac.configuration.JdbcConfig;
import com.ericsson.oss.air.csac.model.KPIDefinition;
import com.ericsson.oss.air.csac.model.PMDefinition;
import com.ericsson.oss.air.csac.model.TestResourcesUtils;
import com.ericsson.oss.air.csac.repository.impl.jdbc.mapper.InputMetricWithKpiDefsMapper;
import com.ericsson.oss.air.csac.repository.impl.jdbc.mapper.KpiDefinitionMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.ArgumentPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.util.ObjectUtils;

@ExtendWith(MockitoExtension.class)
class KPIDefinitionDAOJdbcImplTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Mock
    private JdbcConfig jdbcConfig;

    @InjectMocks
    private KPIDefinitionDAOJdbcImpl dao;

    @BeforeEach
    void setUp() {
        lenient().when(this.jdbcConfig.getDictionarySchemaName()).thenReturn("dict");
        lenient().when(this.jdbcConfig.getRuntimeDatastoreSchemaName()).thenReturn("rtds");
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void saveKPIDefinition() {
        final KPIDefinition kpi = TestResourcesUtils.VALID_SIMPLE_KPI_DEF_OBJ;
        this.dao.saveKPIDefinition(kpi);

        Mockito.verify(this.namedParameterJdbcTemplate, Mockito.times(2))
                .batchUpdate(ArgumentMatchers.anyString(), ArgumentMatchers.any(SqlParameterSource[].class));

    }

    @Test
    void insertKPIDefinitions() {
        this.dao.insertKPIDefinitions(VALID_LIST_KPI_DEF_OBJ);

        final String queryInsertKpi = "INSERT INTO dict.kpi_def(name, description, display_name, expression, aggregation_type, is_visible) VALUES(:name, :description, :displayName, :expression, :aggregationType, :isVisible) ON CONFLICT (name) DO UPDATE SET description = EXCLUDED.description,  display_name = EXCLUDED.display_name, expression = EXCLUDED.expression, aggregation_type = EXCLUDED.aggregation_type, is_visible = EXCLUDED.is_visible";
        final String queryInsertMetric = "INSERT INTO dict.kpi_input_metric(kpi_name, id, alias, inp_type) VALUES(:kpi_name, :id, :alias, :inp_type::dict.inp_metric_type) ON CONFLICT (kpi_name, id) DO NOTHING";

        Mockito.verify(this.namedParameterJdbcTemplate, Mockito.times(1))
                .batchUpdate(ArgumentMatchers.eq(queryInsertKpi), ArgumentMatchers.any(SqlParameterSource[].class));
        Mockito.verify(this.namedParameterJdbcTemplate, Mockito.times(1))
                .batchUpdate(ArgumentMatchers.eq(queryInsertMetric), ArgumentMatchers.any(SqlParameterSource[].class));
    }

    @Test
    void findByKPIDefName() {
        final String kpiName = "kpiName";
        final KPIDefinition kpiDefWithoutMetrics = TestResourcesUtils.VALID_SIMPLE_KPI_DEF_OBJ.toBuilder().build();

        Mockito.when(this.jdbcTemplate.query(ArgumentMatchers.anyString(), ArgumentMatchers.any(ArgumentPreparedStatementSetter.class),
                        ArgumentMatchers.any(KpiDefinitionMapper.class)))
                .thenReturn(singletonList(kpiDefWithoutMetrics));

        Mockito.when(this.jdbcTemplate.query(ArgumentMatchers.anyString(), ArgumentMatchers.any(ArgumentPreparedStatementSetter.class),
                        ArgumentMatchers.any(InputMetricWithKpiDefsMapper.class)))
                .thenReturn(TestResourcesUtils.SIMPLE_INPUT_METRIC_LIST);

        final KPIDefinition byKPIDefName = this.dao.findByKPIDefName(kpiName);
        Assertions.assertEquals(kpiDefWithoutMetrics, byKPIDefName);

    }

    @Test
    void findByKPIDefName_EmptyResourceException() {
        final String kpiName = "kpiName";

        Mockito.when(this.jdbcTemplate.query(ArgumentMatchers.anyString(), ArgumentMatchers.any(ArgumentPreparedStatementSetter.class),
                        ArgumentMatchers.any(KpiDefinitionMapper.class)))
                .thenReturn(new ArrayList<>());

        final KPIDefinition byKPIDefName = this.dao.findByKPIDefName(kpiName);
        Assertions.assertNull(byKPIDefName);
    }

    @Test
    void findByKPIDefName_EmptyResponse() {
        final String kpiName = "kpiName";

        final KPIDefinition byKPIDefName = this.dao.findByKPIDefName(kpiName);
        Assertions.assertNull(byKPIDefName);

    }

    @Test
    void findByKPIDefNames_emptyInput() {
        final Set<KPIDefinition> byKPIDefNames = this.dao.findByKPIDefNames(new HashSet<>());
        Assertions.assertTrue(ObjectUtils.isEmpty(byKPIDefNames));
    }

    @Test
    void findByKPIDefNames_emptyResponse() {
        Mockito.when(this.jdbcTemplate.query(ArgumentMatchers.anyString(), ArgumentMatchers.any(ArgumentPreparedStatementSetter.class),
                        ArgumentMatchers.any(KpiDefinitionMapper.class)))
                .thenReturn(new ArrayList<>());

        final Set<KPIDefinition> byKPIDefNames = this.dao.findByKPIDefNames(Set.of("kpi1", "kpi2"));
        Assertions.assertTrue(ObjectUtils.isEmpty(byKPIDefNames));
    }

    @Test
    void findByKPIDefNames_matchedResult() {
        Mockito.when(this.jdbcTemplate.query(ArgumentMatchers.anyString(), ArgumentMatchers.any(ArgumentPreparedStatementSetter.class),
                        ArgumentMatchers.any(KpiDefinitionMapper.class)))
                .thenReturn(VALID_LIST_KPI_DEF_OBJ);

        Mockito.when(this.jdbcTemplate.query(ArgumentMatchers.anyString(), ArgumentMatchers.any(ArgumentPreparedStatementSetter.class),
                        ArgumentMatchers.any(InputMetricWithKpiDefsMapper.class)))
                .thenReturn(new ArrayList<>());

        final Set<KPIDefinition> byKPIDefNames = this.dao.findByKPIDefNames(Set.of("kpi1", "kpi2"));
        assertEquals(new HashSet<>(VALID_LIST_KPI_DEF_OBJ), byKPIDefNames);
    }

    @Test
    void getAffectedKPIDefs_emptyInput() {
        final Set<KPIDefinition> byKPIDefNames = this.dao.getAffectedKPIDefs(new HashSet<>());
        Assertions.assertTrue(ObjectUtils.isEmpty(byKPIDefNames));
    }

    @Test
    void getAffectedKPIDefs_emptyResponse() {
        final Set<KPIDefinition> byKPIDefNames = this.dao.getAffectedKPIDefs(Set.of(VALID_PM_DEF_OBJ));
        Assertions.assertTrue(ObjectUtils.isEmpty(byKPIDefNames));
    }

    @Test
    void getAffectedKPIDefs() {

        final List<PMDefinition> pmDefs = TestResourcesUtils.VALID_LIST_PM_DEF_OBJ;
        Mockito.when(this.jdbcTemplate.queryForList(ArgumentMatchers.anyString(), ArgumentMatchers.eq(String.class), ArgumentMatchers.anyString()))
                .thenReturn(List.of("KpiName"));

        // Mock response from findByKPIDefNames
        Mockito.when(this.jdbcTemplate.query(ArgumentMatchers.anyString(), ArgumentMatchers.any(ArgumentPreparedStatementSetter.class),
                ArgumentMatchers.any(KpiDefinitionMapper.class))).thenReturn(
                VALID_LIST_KPI_DEF_OBJ);
        final Set<KPIDefinition> affectedKPIDefs = this.dao.getAffectedKPIDefs(new HashSet<>(pmDefs));
        final KPIDefinition kpiDefinition = affectedKPIDefs.stream().findFirst().orElseThrow();

        assertEquals(VALID_LIST_KPI_DEF_OBJ.size(), affectedKPIDefs.size());
        assertEquals(VALID_LIST_KPI_DEF_OBJ.get(0).getName(), kpiDefinition.getName());

    }

    @Test
    void getAllKpiDefNames() {
        Mockito.when(this.jdbcTemplate.queryForList(
                        String.format(KPIDefinitionDAOJdbcImpl.SELECT_ALL_NAMES_STATEMENT, this.jdbcConfig.getDictionarySchemaName()), String.class))
                .thenReturn(List.of("kpi1"));
        assertEquals(Set.of("kpi1"), this.dao.getAllKpiDefNames());
    }

    @Test
    void getAllKpiDefNames_emptyRequest() {
        Mockito.when(this.jdbcTemplate.queryForList(
                        String.format(KPIDefinitionDAOJdbcImpl.SELECT_ALL_NAMES_STATEMENT, this.jdbcConfig.getDictionarySchemaName()), String.class))
                .thenReturn(new ArrayList<>());
        assertEquals(Set.of(), this.dao.getAllKpiDefNames());
    }

    @Test
    void totalKPIDefinitions() {
        Mockito.when(this.jdbcTemplate.queryForObject(
                String.format(KPIDefinitionDAOJdbcImpl.COUNT_STATEMENT, this.jdbcConfig.getDictionarySchemaName()), Integer.class)).thenReturn(4);
        assertEquals(4, this.dao.totalKPIDefinitions());
    }

    @Test
    void findAllKPIDefs() {

        // Mock total count check
        Mockito.when(this.jdbcTemplate.queryForObject(
                String.format(KPIDefinitionDAOJdbcImpl.COUNT_STATEMENT, this.jdbcConfig.getDictionarySchemaName()), Integer.class)).thenReturn(20);

        // Mock the main query
        Mockito.when(this.jdbcTemplate.query(ArgumentMatchers.eq("SELECT * FROM dict.kpi_def OFFSET 10 LIMIT 10"),
                        ArgumentMatchers.any(KpiDefinitionMapper.class)))
                .thenReturn(VALID_LIST_KPI_DEF_OBJ);

        // Mock the secondary query for input metrics
        Mockito.when(this.jdbcTemplate.query(ArgumentMatchers.eq("SELECT * FROM dict.kpi_input_metric WHERE kpi_name in (?)"),
                        ArgumentMatchers.any(ArgumentPreparedStatementSetter.class),
                        ArgumentMatchers.any(InputMetricWithKpiDefsMapper.class)))
                .thenReturn(TestResourcesUtils.SIMPLE_INPUT_METRIC_LIST);

        this.dao.findAllKPIDefs(1, 10);

        Mockito.verify(this.jdbcTemplate, Mockito.times(1))
                .queryForObject(String.format(KPIDefinitionDAOJdbcImpl.COUNT_STATEMENT, this.jdbcConfig.getDictionarySchemaName()), Integer.class);

        Mockito.verify(this.jdbcTemplate, Mockito.times(1))
                .query(ArgumentMatchers.eq("SELECT * FROM dict.kpi_def OFFSET 10 LIMIT 10"), ArgumentMatchers.any(KpiDefinitionMapper.class));

        Mockito.verify(this.jdbcTemplate, Mockito.times(1))
                .query(ArgumentMatchers.eq("SELECT * FROM dict.kpi_input_metric WHERE kpi_name in (?)"),
                        ArgumentMatchers.any(ArgumentPreparedStatementSetter.class),
                        ArgumentMatchers.any(InputMetricWithKpiDefsMapper.class));

    }

    @Test
    void isMatched_shouldNotMatch() {
        final KPIDefinition kpi = KPIDefinition.builder().name("RandomName").build();
        Assertions.assertFalse(this.dao.isMatched(kpi));
    }

    @Test
    void testFindAll() {

        // Mock the main query
        Mockito.when(this.jdbcTemplate.query(ArgumentMatchers.eq("SELECT * FROM dict.kpi_def"),
                        ArgumentMatchers.any(KpiDefinitionMapper.class)))
                .thenReturn(VALID_LIST_KPI_DEF_OBJ);

        // Mock the secondary query for input metrics
        Mockito.when(this.jdbcTemplate.query(ArgumentMatchers.eq("SELECT * FROM dict.kpi_input_metric WHERE kpi_name in (?)"),
                        ArgumentMatchers.any(ArgumentPreparedStatementSetter.class),
                        ArgumentMatchers.any(InputMetricWithKpiDefsMapper.class)))
                .thenReturn(TestResourcesUtils.SIMPLE_INPUT_METRIC_LIST);

        this.dao.findAll();

        Mockito.verify(this.jdbcTemplate, Mockito.times(1))
                .query(ArgumentMatchers.eq(String.format(SELECT_ALL_KPI_STATEMENT, this.jdbcConfig.getDictionarySchemaName())),
                        ArgumentMatchers.any(KpiDefinitionMapper.class));

        Mockito.verify(this.jdbcTemplate, Mockito.times(1))
                .query(ArgumentMatchers.eq("SELECT * FROM dict.kpi_input_metric WHERE kpi_name in (?)"),
                        ArgumentMatchers.any(ArgumentPreparedStatementSetter.class),
                        ArgumentMatchers.any(InputMetricWithKpiDefsMapper.class));

    }
}