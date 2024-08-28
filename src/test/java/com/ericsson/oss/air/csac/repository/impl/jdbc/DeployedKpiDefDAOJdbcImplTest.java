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

import static com.ericsson.oss.air.csac.model.TestResourcesUtils.VALID_PROFILE_DEF_AGGREGATION_FIELD_LIST;
import static com.ericsson.oss.air.csac.repository.impl.jdbc.DeployedKpiDefDAOJdbcImpl.FIND_ALL_RT_KPIS;
import static com.ericsson.oss.air.csac.repository.impl.jdbc.DeployedKpiDefDAOJdbcImpl.FIND_ALL_VISIBLE_RT_KPIS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.sql.DataSource;

import com.ericsson.oss.air.csac.configuration.JdbcConfig;
import com.ericsson.oss.air.csac.model.ProfileDefinition;
import com.ericsson.oss.air.csac.model.TestResourcesUtils;
import com.ericsson.oss.air.csac.model.pmsc.KpiDefinitionDTO;
import com.ericsson.oss.air.csac.model.pmsc.KpiTypeEnum;
import com.ericsson.oss.air.csac.model.runtime.RuntimeKpiInstance;
import com.ericsson.oss.air.csac.model.runtime.metadata.KpiContextId;
import com.ericsson.oss.air.csac.repository.impl.jdbc.mapper.DeployedKpiDefinitionMapper;
import com.ericsson.oss.air.csac.repository.impl.jdbc.mapper.RuntimeKpiInstanceMapper;
import com.ericsson.oss.air.exception.CsacDAOException;
import com.ericsson.oss.air.util.logging.FaultHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

@ExtendWith(MockitoExtension.class)
public class DeployedKpiDefDAOJdbcImplTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Mock
    private DataSource dataSource;

    @Mock
    private Connection connection;

    @Mock
    private FaultHandler faultHandler;

    @Mock
    private ObjectMapper mapper = new ObjectMapper();

    @Mock
    private JdbcConfig jdbcConfig;

    @InjectMocks
    private DeployedKpiDefDAOJdbcImpl dao;

    @BeforeEach
    void setup() {
        lenient().when(this.jdbcConfig.getDictionarySchemaName()).thenReturn("dict");
        lenient().when(this.jdbcConfig.getRuntimeDatastoreSchemaName()).thenReturn("rtds");
    }

    @Test
    void createDeployedKpiTest() throws SQLException {
        final KpiDefinitionDTO kpiDefinitionDTO = TestResourcesUtils.DEPLOYED_SIMPLE_KPI_OBJ;
        final ProfileDefinition profileDefinition = TestResourcesUtils.VALID_PROFILE_DEF_OBJ;
        final String kpiDefName = TestResourcesUtils.VALID_SIMPLE_KPI_DEF_NAME;

        when(this.jdbcTemplate.getDataSource()).thenReturn(this.dataSource);
        when(this.dataSource.getConnection()).thenReturn(this.connection);

        this.dao.createDeployedKpi(kpiDefinitionDTO, kpiDefName, profileDefinition);

        Mockito.verify(this.jdbcTemplate, Mockito.times(1))
                .update(anyString(),
                        ArgumentMatchers.any(PreparedStatementSetter.class));
    }

    @Test
    void createDeployedKpiWithAggregationFieldsTest() throws SQLException {
        final KpiDefinitionDTO kpiDefinitionDTO = TestResourcesUtils.DEPLOYED_SIMPLE_KPI_OBJ;
        final String kpiDefName = TestResourcesUtils.VALID_SIMPLE_KPI_DEF_NAME;

        when(this.jdbcTemplate.getDataSource()).thenReturn(this.dataSource);
        when(this.dataSource.getConnection()).thenReturn(this.connection);

        this.dao.createDeployedKpi(kpiDefinitionDTO, kpiDefName, VALID_PROFILE_DEF_AGGREGATION_FIELD_LIST);

        Mockito.verify(this.jdbcTemplate, Mockito.times(1))
                .update(anyString(),
                        ArgumentMatchers.any(PreparedStatementSetter.class));
    }

    @Test
    void createDeployedKpi_IfDataSourceIsNull_ThrowException() {
        final KpiDefinitionDTO kpiDefinitionDTO = TestResourcesUtils.DEPLOYED_SIMPLE_KPI_OBJ;
        final ProfileDefinition profileDefinition = TestResourcesUtils.VALID_PROFILE_DEF_OBJ;
        final String kpiDefName = TestResourcesUtils.VALID_SIMPLE_KPI_DEF_NAME;

        when(this.jdbcTemplate.getDataSource()).thenReturn(null);

        assertThrows(CsacDAOException.class, () -> this.dao.createDeployedKpi(kpiDefinitionDTO, kpiDefName, profileDefinition));
    }

    @Test
    void createDeployedKpi_invalidKpiObjectToSerialize_ObjectMapperThrowException() throws SQLException, JsonProcessingException {
        final KpiDefinitionDTO kpiDefinitionDTO = TestResourcesUtils.DEPLOYED_SIMPLE_KPI_OBJ;
        final ProfileDefinition profileDefinition = TestResourcesUtils.VALID_PROFILE_DEF_OBJ;
        final String kpiDefName = TestResourcesUtils.VALID_SIMPLE_KPI_DEF_NAME;

        when(this.jdbcTemplate.getDataSource()).thenReturn(this.dataSource);
        when(this.dataSource.getConnection()).thenReturn(this.connection);
        when(this.mapper.writeValueAsString(kpiDefinitionDTO))
                .thenThrow(new JsonProcessingException("cannot serialize profile") {
                });

        assertThrows(CsacDAOException.class, () -> this.dao.createDeployedKpi(kpiDefinitionDTO, kpiDefName, profileDefinition));
    }

    @Test
    void createDeployedKpi_createSqlArrayThrowsException() throws SQLException, JsonProcessingException {
        final KpiDefinitionDTO kpiDefinitionDTO = TestResourcesUtils.DEPLOYED_SIMPLE_KPI_OBJ;
        final ProfileDefinition profileDefinition = TestResourcesUtils.VALID_PROFILE_DEF_OBJ;
        final String kpiDefName = TestResourcesUtils.VALID_SIMPLE_KPI_DEF_NAME;

        when(this.jdbcTemplate.getDataSource()).thenReturn(this.dataSource);
        when(this.dataSource.getConnection()).thenReturn(this.connection);
        when(this.connection.createArrayOf(ArgumentMatchers.matches("VARCHAR"), ArgumentMatchers.any()))
                .thenThrow(new SQLException());

        assertThrows(CsacDAOException.class, () -> this.dao.createDeployedKpi(kpiDefinitionDTO, kpiDefName, profileDefinition));
    }

    @Test
    void updateDeployedKpiTest() {
        final KpiDefinitionDTO kpiDefinitionDTO = TestResourcesUtils.DEPLOYED_SIMPLE_KPI_OBJ;

        this.dao.updateDeployedKpi(kpiDefinitionDTO);

        Mockito.verify(this.jdbcTemplate, Mockito.times(1))
                .update(anyString());
    }

    @Test
    void updateDeployedKpi_invalidObjectToSerialize_ObjectMapperThrowException() throws JsonProcessingException {
        final KpiDefinitionDTO kpiDefinitionDTO = TestResourcesUtils.DEPLOYED_SIMPLE_KPI_OBJ;

        when(this.mapper.writeValueAsString(kpiDefinitionDTO))
                .thenThrow(new JsonProcessingException("cannot serialize profile") {
                });

        assertThrows(CsacDAOException.class, () -> this.dao.updateDeployedKpi(kpiDefinitionDTO));
    }

    @Test
    void deleteDeployedKpiTest() {
        final String pmscKpiName = "kpiName";

        this.dao.deleteDeployedKpi(pmscKpiName);

        Mockito.verify(this.jdbcTemplate, Mockito.times(1))
                .update(anyString(), ArgumentMatchers.any(String.class));
    }

    @Test
    void getDeployedKpiTest() throws Exception {
        final String pmscKpiName = "kpiInstanceId";

        when(this.namedParameterJdbcTemplate.query(
                anyString(),
                any(SqlParameterSource.class),
                any(DeployedKpiDefinitionMapper.class)))
                .thenReturn(List.of(TestResourcesUtils.DEPLOYED_SIMPLE_KPI_OBJ));

        final KpiDefinitionDTO retrievedKpiDTO = this.dao.getDeployedKpi(pmscKpiName);

        assertNotNull(retrievedKpiDTO);
        assertEquals(TestResourcesUtils.DEPLOYED_SIMPLE_KPI_OBJ, retrievedKpiDTO);

    }

    @Test
    void getDeployedKpiTest_missingKpi() throws JsonProcessingException {
        final String pmscKpiName = "kpiInstanceId";

        when(this.namedParameterJdbcTemplate.query(
                anyString(),
                any(SqlParameterSource.class),
                any(DeployedKpiDefinitionMapper.class)))
                .thenReturn(Collections.emptyList());

        assertNull(this.dao.getDeployedKpi(pmscKpiName));

    }

    @Test
    void getDeployedKpiByProfile() throws JsonProcessingException {
        final ProfileDefinition profile = TestResourcesUtils.VALID_PROFILE_DEF_OBJ;
        final List<KpiDefinitionDTO> expectedKpiDtos = List.of(new ObjectMapper()
                .readValue(TestResourcesUtils.DEPLOYED_SIMPLE_KPI_STR, KpiDefinitionDTO.class));
        final String query = "SELECT prof_kpi.prof_name, prof_agg.agg_fields, prof_kpi.kpi_def_name, kpi_instance_id, pmsc_kpi_def FROM rtds.rt_prof_kpi prof_kpi INNER JOIN rtds.rt_prof_agg prof_agg ON prof_kpi.prof_name = prof_agg.prof_name INNER JOIN rtds.rt_kpi_inst kpi ON prof_agg.agg_fields = kpi.agg_fields AND prof_kpi.kpi_def_name = kpi.kpi_def_name WHERE prof_kpi.prof_name = 'profiledef_name';";

        when(this.jdbcTemplate.query(eq(query),
                any(DeployedKpiDefinitionMapper.class)))
                .thenReturn(expectedKpiDtos);

        final List<KpiDefinitionDTO> actualKpiDtos = this.dao.getDeployedKpiByProfile(profile);
        Assertions.assertNotNull(actualKpiDtos);
        assertEquals(expectedKpiDtos, actualKpiDtos);
    }

    @Test
    void getDeployedKpiByDefinitionName() throws JsonProcessingException {
        final String kpiDefName = TestResourcesUtils.VALID_SIMPLE_KPI_DEF_NAME;
        final List<KpiDefinitionDTO> expectedKpiDtos = List.of(new ObjectMapper()
                .readValue(TestResourcesUtils.DEPLOYED_SIMPLE_KPI_STR, KpiDefinitionDTO.class));

        when(this.jdbcTemplate.query(anyString(), ArgumentMatchers.any(DeployedKpiDefinitionMapper.class)))
                .thenReturn(expectedKpiDtos);

        final List<KpiDefinitionDTO> actualKpiDtos = this.dao.getDeployedKpiByDefinitionName(kpiDefName);
        Assertions.assertNotNull(actualKpiDtos);
        assertEquals(expectedKpiDtos, actualKpiDtos);
    }

    @Test
    void getDeployedKpiByAggregation_NoKpiInstancesWithGivenAggFields_ReturnNull() throws JsonProcessingException {
        final String kpiDefName = TestResourcesUtils.VALID_SIMPLE_KPI_DEF_NAME;
        final List<String> aggFields = List.of("agg1", "agg2");

        when(this.jdbcTemplate.query(anyString(), ArgumentMatchers.any(DeployedKpiDefinitionMapper.class)))
                .thenReturn(new ArrayList<>());

        final KpiDefinitionDTO retrievedKpiDTO = this.dao.getDeployedKpiByAggregation(kpiDefName, aggFields);

        assertNull(retrievedKpiDTO);
    }

    @Test
    void getDeployedKpiByAggregation() throws JsonProcessingException {

        final String kpiDefName = TestResourcesUtils.VALID_SIMPLE_KPI_DEF_NAME;
        final List<String> aggFields = List.of("agg1", "agg2");

        when(this.jdbcTemplate.query(anyString(), ArgumentMatchers.any(DeployedKpiDefinitionMapper.class)))
                .thenReturn(List.of(TestResourcesUtils.DEPLOYED_SIMPLE_KPI_OBJ));

        final KpiDefinitionDTO retrievedKpiDTO = this.dao.getDeployedKpiByAggregation(kpiDefName, aggFields);

        assertNotNull(retrievedKpiDTO);
    }

    @Test
    void getAllDeployedKpis_ShouldReturndeployedKpis() {
        final KpiDefinitionDTO expectedKpiDto = TestResourcesUtils.DEPLOYED_SIMPLE_KPI_OBJ;
        final List<KpiDefinitionDTO> KpiDtoList = List.of(expectedKpiDto);

        when(this.jdbcTemplate.query(anyString(),
                ArgumentMatchers.any(DeployedKpiDefinitionMapper.class)))
                .thenReturn(KpiDtoList);

        final List<KpiDefinitionDTO> actualKpiDefinitionDTOS = this.dao.getAllDeployedKpis();

        assertEquals(expectedKpiDto, actualKpiDefinitionDTOS.get(0));
    }

    @Test
    void totalDeployedKpiDefinitions_ShouldReturnTotal() {

        when(this.jdbcTemplate.queryForObject(
                String.format(DeployedKpiDefDAOJdbcImpl.COUNT_STATEMENT, this.jdbcConfig.getRuntimeDatastoreSchemaName()), Integer.class))
                .thenReturn(1);

        assertEquals(1, this.dao.totalDeployedKpiDefinitions());
    }

    @Test
    void findAllRuntimeKpis() throws Exception {

        final KpiDefinitionDTO kpiDef = KpiDefinitionDTO.builder()
                .withKpiType(KpiTypeEnum.SIMPLE)
                .withName("rtkpi1")
                .withAggregationPeriod(15)
                .withAggregationElements(List.of("fact_table_1.agg1"))
                .withAggregationType("SUM")
                .withExpression("SUM(fact_table_1.value1")
                .withObjectType("FLOAT")
                .withAlias("csac_simple_snssai")
                .withKpiType(KpiTypeEnum.SIMPLE)
                .withInpDataCategory("category")
                .withInpDataIdentifier("identifier")
                .build();

        final RuntimeKpiInstance rtKpi = RuntimeKpiInstance.builder()
                .withInstanceId("rtkpi1")
                .withContextFieldList(List.of("agg1"))
                .withKpDefinitionName("kpi1")
                .withRuntimeDefinition(kpiDef)
                .build();
        final List<RuntimeKpiInstance> rtKpiList = List.of(rtKpi);

        when(this.jdbcTemplate.query(anyString(), any(RuntimeKpiInstanceMapper.class))).thenReturn(
                rtKpiList);

        // little benefit in asserting the static values being returned by the mockers. Just assert that the expected list was returned
        assertEquals(1, this.dao.findAllRuntimeKpis().size());
    }

    @Test
    void findAllRuntimeKpis_parameterized() throws Exception {

        final KpiDefinitionDTO kpiDef = KpiDefinitionDTO.builder()
                .withKpiType(KpiTypeEnum.SIMPLE)
                .withName("rtkpi1")
                .withAggregationPeriod(15)
                .withAggregationElements(List.of("fact_table_1.agg1"))
                .withAggregationType("SUM")
                .withExpression("SUM(fact_table_1.value1")
                .withObjectType("FLOAT")
                .withAlias("csac_simple_snssai")
                .withKpiType(KpiTypeEnum.SIMPLE)
                .withInpDataCategory("category")
                .withInpDataIdentifier("identifier")
                .build();

        final RuntimeKpiInstance rtKpi = RuntimeKpiInstance.builder()
                .withInstanceId("rtkpi1")
                .withContextFieldList(List.of("agg1"))
                .withKpDefinitionName("kpi1")
                .withRuntimeDefinition(kpiDef)
                .build();
        final List<RuntimeKpiInstance> rtKpiList = List.of(rtKpi);

        when(this.jdbcTemplate.queryForObject(
                String.format(DeployedKpiDefDAOJdbcImpl.COUNT_STATEMENT, this.jdbcConfig.getRuntimeDatastoreSchemaName()), Integer.class))
                .thenReturn(1);
        when(this.jdbcTemplate.query(anyString(), any(RuntimeKpiInstanceMapper.class))).thenReturn(
                rtKpiList);

        // little benefit in asserting the static values being returned by the mockers. Just assert that the expected list was returned
        assertEquals(1, this.dao.findAllRuntimeKpis(0, 1).size());
    }

    @Test
    void findAllRuntimeKpis_isVisible() {

        final KpiDefinitionDTO kpiDef = KpiDefinitionDTO.builder()
                .withKpiType(KpiTypeEnum.SIMPLE)
                .withName("rtkpi1")
                .withAggregationPeriod(15)
                .withAggregationElements(List.of("fact_table_1.agg1"))
                .withAggregationType("SUM")
                .withExpression("SUM(fact_table_1.value1")
                .withObjectType("FLOAT")
                .withAlias("csac_simple_snssai")
                .withIsVisible(true)
                .withKpiType(KpiTypeEnum.SIMPLE)
                .withInpDataCategory("category")
                .withInpDataIdentifier("identifier")
                .build();
        final KpiDefinitionDTO kpiDef_invisible = KpiDefinitionDTO.builder()
                .withKpiType(KpiTypeEnum.SIMPLE)
                .withName("rtkpi2")
                .withAggregationPeriod(15)
                .withAggregationElements(List.of("fact_table_2.agg2"))
                .withAggregationType("SUM")
                .withExpression("SUM(fact_table_2.value2")
                .withObjectType("FLOAT")
                .withAlias("csac_simple_snssai")
                .withIsVisible(false)
                .withKpiType(KpiTypeEnum.SIMPLE)
                .withInpDataCategory("category")
                .withInpDataIdentifier("identifier")
                .build();
        final RuntimeKpiInstance rtKpi = RuntimeKpiInstance.builder()
                .withInstanceId("rtkpi1")
                .withContextFieldList(List.of("agg1"))
                .withKpDefinitionName("kpi1")
                .withRuntimeDefinition(kpiDef)
                .build();
        final RuntimeKpiInstance rtKpi_invisible = RuntimeKpiInstance.builder()
                .withInstanceId("rtkpi2")
                .withContextFieldList(List.of("agg2"))
                .withKpDefinitionName("kpi2")
                .withRuntimeDefinition(kpiDef_invisible)
                .build();

        when(this.jdbcTemplate.query(eq(String.format(FIND_ALL_VISIBLE_RT_KPIS, this.jdbcConfig.getRuntimeDatastoreSchemaName())),
                any(RuntimeKpiInstanceMapper.class))).thenReturn(
                List.of(rtKpi));
        when(this.jdbcTemplate.query(eq(String.format(FIND_ALL_RT_KPIS, this.jdbcConfig.getRuntimeDatastoreSchemaName())),
                any(RuntimeKpiInstanceMapper.class))).thenReturn(
                List.of(rtKpi, rtKpi_invisible));

        assertEquals(1, this.dao.findAllRuntimeKpis(true).size());
        assertEquals(2, this.dao.findAllRuntimeKpis(false).size());
    }

    @Test
    void findAllByContextId() throws Exception {

        final KpiDefinitionDTO kpiDef = KpiDefinitionDTO.builder()
                .withKpiType(KpiTypeEnum.SIMPLE)
                .withName("rtkpi1")
                .withAggregationPeriod(15)
                .withAggregationElements(List.of("fact_table_1.agg1"))
                .withAggregationType("SUM")
                .withExpression("SUM(fact_table_1.value1")
                .withObjectType("FLOAT")
                .withAlias("csac_simple_snssai")
                .withIsVisible(true)
                .withKpiType(KpiTypeEnum.SIMPLE)
                .withInpDataCategory("category")
                .withInpDataIdentifier("identifier")
                .build();
        final KpiDefinitionDTO kpiDef_invisible = KpiDefinitionDTO.builder()
                .withKpiType(KpiTypeEnum.SIMPLE)
                .withName("rtkpi2")
                .withAggregationPeriod(15)
                .withAggregationElements(List.of("fact_table_2.agg2"))
                .withAggregationType("SUM")
                .withExpression("SUM(fact_table_2.value2")
                .withObjectType("FLOAT")
                .withAlias("csac_simple_snssai")
                .withIsVisible(false)
                .withKpiType(KpiTypeEnum.SIMPLE)
                .withInpDataCategory("category")
                .withInpDataIdentifier("identifier")
                .build();
        final RuntimeKpiInstance rtKpi = RuntimeKpiInstance.builder()
                .withInstanceId("rtkpi1")
                .withContextFieldList(List.of("agg1"))
                .withKpDefinitionName("kpi1")
                .withRuntimeDefinition(kpiDef)
                .build();
        final RuntimeKpiInstance rtKpi_invisible = RuntimeKpiInstance.builder()
                .withInstanceId("rtkpi2")
                .withContextFieldList(List.of("agg2"))
                .withKpDefinitionName("kpi2")
                .withRuntimeDefinition(kpiDef_invisible)
                .build();

        when(this.jdbcTemplate.query(eq(String.format(FIND_ALL_VISIBLE_RT_KPIS, this.jdbcConfig.getRuntimeDatastoreSchemaName())),
                any(RuntimeKpiInstanceMapper.class))).thenReturn(
                List.of(rtKpi));
        when(this.jdbcTemplate.query(eq(String.format(FIND_ALL_RT_KPIS, this.jdbcConfig.getRuntimeDatastoreSchemaName())),
                any(RuntimeKpiInstanceMapper.class))).thenReturn(
                List.of(rtKpi, rtKpi_invisible));

        assertEquals(0, this.dao.findAllByContextId(KpiContextId.of(Set.of("random")), false).size());
        assertEquals(0, this.dao.findAllByContextId(KpiContextId.of(Set.of("agg1", "agg2")), false).size());

        assertEquals(1, this.dao.findAllByContextId(KpiContextId.of(Set.of("agg1")), true).size());
        assertEquals(0, this.dao.findAllByContextId(KpiContextId.of(Set.of("agg2")), true).size());
        assertEquals(1, this.dao.findAllByContextId(KpiContextId.of(Set.of("agg2")), false).size());
    }

}
