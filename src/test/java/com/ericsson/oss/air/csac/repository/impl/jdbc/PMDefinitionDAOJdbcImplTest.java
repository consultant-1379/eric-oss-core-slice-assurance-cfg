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

import static com.ericsson.oss.air.csac.repository.impl.jdbc.PMDefinitionDAOJdbcImpl.COUNT_STATEMENT;
import static com.ericsson.oss.air.csac.repository.impl.jdbc.PMDefinitionDAOJdbcImpl.SELECT_ALL_NAMES_STATEMENT;
import static com.ericsson.oss.air.csac.repository.impl.jdbc.PMDefinitionDAOJdbcImpl.SELECT_PM_DEF_BY_NAME_STATEMENT;
import static com.ericsson.oss.air.csac.repository.impl.jdbc.PMDefinitionDAOJdbcImpl.SELECT_SCHEMA_STATEMENT;
import static java.util.Collections.singletonList;
import static org.mockito.Mockito.lenient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.ericsson.oss.air.csac.configuration.JdbcConfig;
import com.ericsson.oss.air.csac.model.PMDefinition;
import com.ericsson.oss.air.csac.model.TestResourcesUtils;
import com.ericsson.oss.air.csac.repository.impl.jdbc.mapper.PMDefinitionMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

@ExtendWith(MockitoExtension.class)
class PMDefinitionDAOJdbcImplTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Mock
    private JdbcConfig jdbcConfig;

    @InjectMocks
    private PMDefinitionDAOJdbcImpl dao;

    @BeforeEach
    void setUp() {
        lenient().when(this.jdbcConfig.getDictionarySchemaName()).thenReturn("dict");
        lenient().when(this.jdbcConfig.getRuntimeDatastoreSchemaName()).thenReturn("rtds");
    }

    @Test
    void savePMDefinition() {

        final String schemaName = "schemaName";
        final PMDefinition pmDef = TestResourcesUtils.VALID_PM_DEF_OBJ;
        this.dao.savePMDefinition(pmDef, schemaName);

        Mockito.verify(this.namedParameterJdbcTemplate, Mockito.times(2))
                .batchUpdate(ArgumentMatchers.anyString(), ArgumentMatchers.any(SqlParameterSource[].class));
    }

    @Test
    void updatePMDefinition() {

        final PMDefinition pmDef = TestResourcesUtils.VALID_PM_DEF_OBJ;
        this.dao.updatePMDefinition(pmDef);

        Mockito.verify(this.namedParameterJdbcTemplate, Mockito.times(1))
                .update(ArgumentMatchers.anyString(), ArgumentMatchers.any(SqlParameterSource.class));

    }

    @Test
    void insertPMDefinitions() {
        final Map<String, List<PMDefinition>> pmMap = new HashMap<>();
        pmMap.put("schema", TestResourcesUtils.VALID_LIST_PM_DEF_OBJ);
        this.dao.insertPMDefinitions(pmMap);

        Mockito.verify(this.namedParameterJdbcTemplate, Mockito.times(2))
                .batchUpdate(ArgumentMatchers.anyString(), ArgumentMatchers.any(SqlParameterSource[].class));
    }

    @Test
    void insertPMDefinitions_emptyInput() {
        final Map<String, List<PMDefinition>> pmMap = new HashMap<>();
        this.dao.insertPMDefinitions(pmMap);

        Mockito.verify(this.namedParameterJdbcTemplate, Mockito.times(0))
                .batchUpdate(ArgumentMatchers.anyString(), ArgumentMatchers.any(SqlParameterSource[].class));
    }

    @Test
    void findSchemaByPMDefName() {
        final String pmDefName = TestResourcesUtils.VALID_PM_DEF_NAME;
        final String schema = "schema";
        final String sql = String.format(SELECT_SCHEMA_STATEMENT, this.jdbcConfig.getDictionarySchemaName());
        Mockito.when(this.jdbcTemplate.queryForList(sql, String.class, pmDefName))
                .thenReturn(List.of(schema));
        Assertions.assertEquals(schema, this.dao.findSchemaByPMDefName(pmDefName));
    }

    @Test
    void findByPMDefName() {
        final String pmDefName = TestResourcesUtils.VALID_PM_DEF_NAME;
        Mockito.when(this.jdbcTemplate.query(ArgumentMatchers.eq(
                                String.format(SELECT_PM_DEF_BY_NAME_STATEMENT, this.jdbcConfig.getDictionarySchemaName())),
                        ArgumentMatchers.any(PMDefinitionMapper.class),
                        ArgumentMatchers.eq(pmDefName)))
                .thenReturn(singletonList(TestResourcesUtils.VALID_PM_DEF_OBJ));
        Assertions.assertEquals(TestResourcesUtils.VALID_PM_DEF_OBJ, this.dao.findByPMDefName(pmDefName));
    }

    @Test
    void findAllPMDefinitions() {

        Mockito.when(this.jdbcTemplate.queryForObject(String.format(COUNT_STATEMENT, this.jdbcConfig.getDictionarySchemaName()), Integer.class))
                .thenReturn(20);
        Mockito.when(this.jdbcTemplate.query(ArgumentMatchers.eq("SELECT * FROM dict.pm_def OFFSET 0 LIMIT 10"),
                        ArgumentMatchers.any(PMDefinitionMapper.class)))
                .thenReturn(TestResourcesUtils.VALID_LIST_PM_DEF_OBJ);

        final List<PMDefinition> allPMDefinitions = this.dao.findAllPMDefinitions(0, 10);
        Assertions.assertEquals(TestResourcesUtils.VALID_LIST_PM_DEF_OBJ.size(), allPMDefinitions.size());

    }

    @Test
    void getAllPmDefNames() {

        final Set<String> pmNameSetSample = Set.of("pmName1", "pmName2");
        Mockito.when(
                        this.jdbcTemplate.queryForList(String.format(SELECT_ALL_NAMES_STATEMENT, this.jdbcConfig.getDictionarySchemaName()), String.class))
                .thenReturn(new ArrayList<>(pmNameSetSample));

        final Set<String> pmNameSet = this.dao.getAllPmDefNames();
        Assertions.assertEquals(pmNameSetSample, pmNameSet);
    }

    @Test
    void getAllPmDefNames_emptyResponse() {

        Mockito.when(
                        this.jdbcTemplate.queryForList(String.format(SELECT_ALL_NAMES_STATEMENT, this.jdbcConfig.getDictionarySchemaName()), String.class))
                .thenReturn(new ArrayList<>());
        final Set<String> allPmDefNames = this.dao.getAllPmDefNames();
        Assertions.assertTrue(allPmDefNames.isEmpty());
    }

    @Test
    void totalPMDefinitions() {

        Mockito.when(this.jdbcTemplate.queryForObject(String.format(COUNT_STATEMENT, this.jdbcConfig.getDictionarySchemaName()), Integer.class))
                .thenReturn(10);

        final Integer count = this.dao.totalPMDefinitions();
        Assertions.assertEquals(10, count);
    }
}
