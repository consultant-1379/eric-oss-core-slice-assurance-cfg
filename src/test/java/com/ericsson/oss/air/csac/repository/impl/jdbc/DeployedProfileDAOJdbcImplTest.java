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

import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.sql.DataSource;

import com.ericsson.oss.air.csac.configuration.JdbcConfig;
import com.ericsson.oss.air.csac.model.KPIDefinition;
import com.ericsson.oss.air.csac.model.ProfileDefinition;
import com.ericsson.oss.air.csac.model.TestResourcesUtils;
import com.ericsson.oss.air.csac.repository.impl.jdbc.mapper.RichProfileDefinitionMapper;
import com.ericsson.oss.air.exception.CsacDAOException;
import com.ericsson.oss.air.util.logging.FaultHandler;
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
public class DeployedProfileDAOJdbcImplTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Mock
    private FaultHandler faultHandler;

    @Mock
    private JdbcConfig jdbcConfig;

    @Mock
    private DataSource dataSource;

    @Mock
    private Connection connection;

    @InjectMocks
    private DeployedProfileDAOJdbcImpl dao;

    @BeforeEach
    void setup() {
        lenient().when(this.jdbcConfig.getDictionarySchemaName()).thenReturn("dict");
        lenient().when(this.jdbcConfig.getRuntimeDatastoreSchemaName()).thenReturn("rtds");
    }

    @Test
    void saveProfileDefinition_ShouldInsertProfileContentsTo3Tables() throws SQLException {
        Mockito.when(this.jdbcTemplate.getDataSource()).thenReturn(this.dataSource);
        Mockito.when(this.dataSource.getConnection()).thenReturn(this.connection);
        final ProfileDefinition profile = TestResourcesUtils.VALID_PROFILE_DEF_OBJ;

        this.dao.saveProfileDefinition(profile);

        Mockito.verify(this.namedParameterJdbcTemplate, Mockito.times(3))
                .batchUpdate(ArgumentMatchers.anyString(), ArgumentMatchers.any(SqlParameterSource[].class));
    }

    @Test
    void saveProfileDefinition_NullInputTest() throws SQLException {
        assertThrows(NullPointerException.class, () -> this.dao.saveProfileDefinition(null));
    }

    @Test
    void insertProfileDefinitions_ShouldInsertProfileContentsTo3Tables() throws SQLException {
        Mockito.when(this.jdbcTemplate.getDataSource()).thenReturn(this.dataSource);
        Mockito.when(this.dataSource.getConnection()).thenReturn(this.connection);
        final ProfileDefinition profile = TestResourcesUtils.VALID_PROFILE_DEF_OBJ;

        this.dao.insertProfileDefinitions(List.of(profile));

        Mockito.verify(this.namedParameterJdbcTemplate, Mockito.times(3))
                .batchUpdate(ArgumentMatchers.anyString(), ArgumentMatchers.any(SqlParameterSource[].class));
    }

    @Test
    void findByProfileDefName_IfProfileDefNameDoNotExist_ShouldNotInvokeAllJdbcCallAndReturnNull() {
        final String profileName = "blah!";
        Mockito.when(this.jdbcTemplate.query(ArgumentMatchers.anyString(), ArgumentMatchers.any(ArgumentPreparedStatementSetter.class),
                        ArgumentMatchers.any(RichProfileDefinitionMapper.class)))
                .thenReturn(new ArrayList<>());

        final ProfileDefinition profile = this.dao.findByProfileDefName(profileName);
        Assertions.assertNull(profile);

        Mockito.verify(this.jdbcTemplate, Mockito.times(1))
                .query(ArgumentMatchers.anyString(),
                        ArgumentMatchers.any(ArgumentPreparedStatementSetter.class),
                        ArgumentMatchers.any(RichProfileDefinitionMapper.class));
    }

    @Test
    void findByProfileNames_EmptyInput_ShouldReturnEmptyResult() {
        final Set<ProfileDefinition> profiles = this.dao.findByProfileDefNames(new HashSet<>());

        assertTrue(ObjectUtils.isEmpty(profiles));
    }

    @Test
    void findAllProfileDefinitions() {
        //Mock totalProfileDefinitions()
        Mockito.when(this.jdbcTemplate.queryForObject(
                        String.format(DeployedProfileDAOJdbcImpl.COUNT_STATEMENT, this.jdbcConfig.getRuntimeDatastoreSchemaName()), Integer.class))
                .thenReturn(20);

        final String selectWithCTE = "WITH rich_profile AS (SELECT name, description, kpi_refs, agg_fields\n"
                + "                      FROM rtds.rt_profile profile\n"
                + "                          LEFT JOIN(SELECT prof_name, array_agg(kpi_def_name) AS kpi_refs FROM rtds.rt_prof_kpi GROUP BY prof_name ) kpi ON profile.name = kpi.prof_name\n"
                + "                          LEFT JOIN rtds.rt_prof_agg agg ON profile.name = agg.prof_name)\n"
                + " SELECT * FROM rich_profile OFFSET ? LIMIT ?";

        //Mock main query
        Mockito.when(this.jdbcTemplate.query(ArgumentMatchers.eq(selectWithCTE),
                        ArgumentMatchers.any(RichProfileDefinitionMapper.class),
                        ArgumentMatchers.eq(10),
                        ArgumentMatchers.eq(10)))
                .thenReturn(TestResourcesUtils.VALID_LIST_PROFILE_DEF_OBJ);

        Mockito.verify(this.jdbcTemplate, Mockito.times(0))
                .query(ArgumentMatchers.anyString(),
                        ArgumentMatchers.any(RichProfileDefinitionMapper.class),
                        ArgumentMatchers.eq(10),
                        ArgumentMatchers.eq(10));

        this.dao.findAllProfileDefinitions(1, 10);

        Mockito.verify(this.jdbcTemplate, Mockito.times(1))
                .queryForObject(String.format(DeployedProfileDAOJdbcImpl.COUNT_STATEMENT, this.jdbcConfig.getRuntimeDatastoreSchemaName()),
                        Integer.class);

        Mockito.verify(this.jdbcTemplate, Mockito.times(1))
                .query(ArgumentMatchers.eq(selectWithCTE),
                        ArgumentMatchers.any(RichProfileDefinitionMapper.class),
                        ArgumentMatchers.eq(10),
                        ArgumentMatchers.eq(10));

    }

    @Test
    void getProfileDefinitions_ShouldReturnAllProfiles() {
        final List<ProfileDefinition> expectedProfiles = TestResourcesUtils.VALID_LIST_PROFILE_DEF_OBJ;

        //Mock totalProfileDefinitions()
        Mockito.when(this.jdbcTemplate.queryForObject(
                        String.format(DeployedProfileDAOJdbcImpl.COUNT_STATEMENT, this.jdbcConfig.getRuntimeDatastoreSchemaName()), Integer.class))
                .thenReturn(20);

        Mockito.when(this.jdbcTemplate.query(ArgumentMatchers.anyString(),
                        ArgumentMatchers.any(RichProfileDefinitionMapper.class),
                        ArgumentMatchers.anyInt(),
                        ArgumentMatchers.anyInt()))
                .thenReturn(expectedProfiles);

        final Set<ProfileDefinition> affectedProfiles = this.dao.getProfileDefinitions();
        final ProfileDefinition affectedProfile = affectedProfiles.stream().findFirst().orElseThrow();

        Assertions.assertEquals(expectedProfiles.size(), affectedProfiles.size());
        Assertions.assertEquals(expectedProfiles.get(0).getName(), affectedProfile.getName());
    }

    @Test
    void getProfileDefinitions_NoDeployedProfiles_ShouldReturnEmptyResult() {
        Mockito.when(this.jdbcTemplate.queryForObject(
                        String.format(DeployedProfileDAOJdbcImpl.COUNT_STATEMENT, this.jdbcConfig.getRuntimeDatastoreSchemaName()), Integer.class))
                .thenReturn(0);

        final Set<ProfileDefinition> profiles = this.dao.getProfileDefinitions();

        assertTrue(ObjectUtils.isEmpty(profiles));
    }

    @Test
    void getAffectedProfiles_ValidInputKpis_ShouldReturnAffectedProfiles() {
        final List<KPIDefinition> kpis = List.of(TestResourcesUtils.VALID_SIMPLE_KPI_DEF_OBJ);
        final List<ProfileDefinition> expectedProfiles = TestResourcesUtils.VALID_LIST_PROFILE_DEF_OBJ;

        final String selectProfileQuery = "WITH rich_profile AS (SELECT name, description, kpi_refs, agg_fields\n"
                + "                      FROM rtds.rt_profile profile\n"
                + "                          LEFT JOIN(SELECT prof_name, array_agg(kpi_def_name) AS kpi_refs FROM rtds.rt_prof_kpi GROUP BY prof_name ) kpi ON profile.name = kpi.prof_name\n"
                + "                          LEFT JOIN rtds.rt_prof_agg agg ON profile.name = agg.prof_name)\n"
                + " SELECT * FROM rich_profile INNER JOIN rtds.rt_prof_kpi kpi ON rich_profile.name = kpi.prof_name WHERE kpi.kpi_def_name IN (?);";

        //Mock Response from findByProfileNames method
        Mockito.when(this.jdbcTemplate.query(ArgumentMatchers.eq(selectProfileQuery),
                        ArgumentMatchers.any(ArgumentPreparedStatementSetter.class),
                        ArgumentMatchers.any(RichProfileDefinitionMapper.class)))
                .thenReturn(expectedProfiles);

        final Set<ProfileDefinition> affectedProfiles = this.dao.getAffectedProfiles(kpis);
        final ProfileDefinition affectedProfile = affectedProfiles.stream().findFirst().orElseThrow();

        Assertions.assertEquals(expectedProfiles.size(), affectedProfiles.size());
        Assertions.assertEquals(expectedProfiles.get(0).getName(), affectedProfile.getName());

    }

    @Test
    void getAffectedProfiles_EmptyInputKpis_ShouldReturnEmptyResult() {
        final Set<ProfileDefinition> profiles = this.dao.getAffectedProfiles(new ArrayList<>());

        assertTrue(ObjectUtils.isEmpty(profiles));
    }

    @Test
    void isMatched_IdenticalProfileMatched() {
        final ProfileDefinition profile1 = TestResourcesUtils.VALID_PROFILE_DEF_OBJ;
        final ProfileDefinition profile2 = TestResourcesUtils.VALID_PROFILE_DEF_OBJ;

        //Mock Response from findByProfileNames method
        Mockito.when(this.jdbcTemplate.query(ArgumentMatchers.anyString(), ArgumentMatchers.any(ArgumentPreparedStatementSetter.class),
                ArgumentMatchers.any(RichProfileDefinitionMapper.class))).thenReturn(
                singletonList(profile2));

        assertTrue(this.dao.isMatched(profile1));
    }

    @Test
    void isMatched_ProfileDoNotMatch() {
        final ProfileDefinition profile1 = TestResourcesUtils.VALID_PROFILE_DEF_OBJ;
        final ProfileDefinition profile2 = TestResourcesUtils.VALID_PROFILE_DEF_OBJ.toBuilder()
                .name("blah!")
                .build();

        //Mock Response from findByProfileNames method
        Mockito.when(this.jdbcTemplate.query(ArgumentMatchers.anyString(), ArgumentMatchers.any(ArgumentPreparedStatementSetter.class),
                ArgumentMatchers.any(RichProfileDefinitionMapper.class))).thenReturn(
                singletonList(profile2));

        Assertions.assertFalse(this.dao.isMatched(profile1));
    }

    @Test
    void isMatched_NullDoNotMatch() {
        final ProfileDefinition profile = TestResourcesUtils.VALID_PROFILE_DEF_OBJ;

        //Mock Response from findByProfileNames method
        Mockito.when(this.jdbcTemplate.query(ArgumentMatchers.anyString(),
                        ArgumentMatchers.any(ArgumentPreparedStatementSetter.class),
                        ArgumentMatchers.any(RichProfileDefinitionMapper.class)))
                .thenReturn(new ArrayList<>());

        Assertions.assertFalse(this.dao.isMatched(profile));
    }

    @Test
    void totalProfileDefinitions_ShouldReturnTotal() {
        Mockito.when(this.jdbcTemplate.queryForObject(
                        String.format(DeployedProfileDAOJdbcImpl.COUNT_STATEMENT, this.jdbcConfig.getRuntimeDatastoreSchemaName()), Integer.class))
                .thenReturn(1);

        Assertions.assertEquals(1, this.dao.totalProfileDefinitions());
    }

    @Test
    void getProfileAggregationSqlParameterSourceList_NullInput_ThrowException() {
        assertThrows(NullPointerException.class, () -> this.dao.getProfileAggregationSqlParameterSourceList(null));
    }

    @Test
    void getProfileAggregationSqlParameterSourceList_NullDatasource_ThrowException() {
        List<ProfileDefinition> profileDefs = List.of(TestResourcesUtils.VALID_PROFILE_DEF_OBJ);

        Mockito.when(this.jdbcTemplate.getDataSource()).thenReturn(null);

        assertThrows(CsacDAOException.class, () -> this.dao.getProfileAggregationSqlParameterSourceList(profileDefs));
    }

    @Test
    void getProfileAggregationSqlParameterSourceList_createSqlArrayThrowException() throws SQLException {
        final List<ProfileDefinition> profileDefs = List.of(TestResourcesUtils.VALID_PROFILE_DEF_OBJ);

        Mockito.when(this.jdbcTemplate.getDataSource()).thenReturn(this.dataSource);
        Mockito.when(this.dataSource.getConnection()).thenReturn(this.connection);
        Mockito.when(this.connection.createArrayOf(ArgumentMatchers.matches("VARCHAR"), ArgumentMatchers.any()))
                .thenThrow(new SQLException());

        assertThrows(CsacDAOException.class, () -> this.dao.getProfileAggregationSqlParameterSourceList(profileDefs));
    }

    @Test
    void getProfileKpiRefsParameterSourceList_NullInput_ThrowException() {
        assertThrows(NullPointerException.class, () -> DeployedProfileDAOJdbcImpl.getProfileKpiRefsParameterSourceList(null));
    }

    @Test
    void getProfileKpiRefsParameterSourceList_profileWithNoKpiRefs_ReturnEmptyParametersSourceList() {
        ProfileDefinition profileDef = TestResourcesUtils.VALID_PROFILE_DEF_OBJ.toBuilder()
                .kpis(new ArrayList<>()).build();

        assertTrue(ObjectUtils.isEmpty(DeployedProfileDAOJdbcImpl.getProfileKpiRefsParameterSourceList(profileDef)));
    }
}
