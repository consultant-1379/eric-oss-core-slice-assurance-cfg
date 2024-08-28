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

import static com.ericsson.oss.air.util.RestEndpointUtil.getSafeSublistIndex;

import java.sql.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

import javax.sql.DataSource;

import com.ericsson.oss.air.csac.configuration.JdbcConfig;
import com.ericsson.oss.air.csac.model.ProfileDefinition;
import com.ericsson.oss.air.csac.model.pmsc.KpiDefinitionDTO;
import com.ericsson.oss.air.csac.model.runtime.RuntimeKpiInstance;
import com.ericsson.oss.air.csac.model.runtime.metadata.KpiContextId;
import com.ericsson.oss.air.csac.repository.DeployedKpiDefDAO;
import com.ericsson.oss.air.csac.repository.impl.jdbc.mapper.DeployedKpiDefinitionMapper;
import com.ericsson.oss.air.csac.repository.impl.jdbc.mapper.RuntimeKpiInstanceMapper;
import com.ericsson.oss.air.exception.CsacDAOException;
import com.ericsson.oss.air.util.logging.FaultHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.data.util.Pair;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;
import org.springframework.util.ObjectUtils;

@Slf4j
@Repository
@Primary
@AllArgsConstructor
@Profile({ "prod", "test" })
public class DeployedKpiDefDAOJdbcImpl implements DeployedKpiDefDAO {

    //COLUMN NAMES
    public static final String PMSC_KPI_DEF_COLUMN = "pmsc_kpi_def";

    public static final String KPI_INSTANCE_ID_COLUMN = "kpi_instance_id";

    public static final String KPI_DEF_NAME_COLUMN = "kpi_def_name";

    //TABLE NAMES
    public static final String RT_KPI_INST_TABLE = "%1$s.rt_kpi_inst";

    public static final String RT_PROF_KPI_TABLE = "%1$s.rt_prof_kpi";

    public static final String RT_PROF_AGG_TABLE = "%1$s.rt_prof_agg";

    //SQL Statement
    public static final String COUNT_STATEMENT = "SELECT COUNT(*) FROM " + RT_KPI_INST_TABLE;

    public static final String INSERT_KPI_INSTANCE_STATEMENT = "INSERT INTO " + RT_KPI_INST_TABLE
            + " (kpi_instance_id, kpi_def_name, agg_fields, pmsc_kpi_def)"
            + " VALUES (?, ?, ?, to_json(?::json))"
            + " ON CONFLICT (kpi_def_name, agg_fields) DO UPDATE"
            + " SET pmsc_kpi_def = EXCLUDED.pmsc_kpi_def";

    public static final String GET_DEPLOYED_KPI_BY_PROFILE_NAME =
            "SELECT prof_kpi.prof_name, prof_agg.agg_fields, prof_kpi.kpi_def_name, kpi_instance_id, pmsc_kpi_def "
                    + "FROM " + RT_PROF_KPI_TABLE + " prof_kpi "
                    + "INNER JOIN " + RT_PROF_AGG_TABLE + " prof_agg ON prof_kpi.prof_name = prof_agg.prof_name "
                    + "INNER JOIN " + RT_KPI_INST_TABLE + " kpi ON prof_agg.agg_fields = kpi.agg_fields AND prof_kpi.kpi_def_name = kpi.kpi_def_name "
                    + "WHERE prof_kpi.prof_name = '%2$s';";

    public static final String DELETE_KPI_INST_BY_PMSC_KPI_NAME_STATEMENT = "DELETE FROM " + RT_KPI_INST_TABLE
            + " WHERE kpi_instance_id = ?";

    public static final String GET_KPI_BY_NAME = "SELECT " + PMSC_KPI_DEF_COLUMN + " FROM " + RT_KPI_INST_TABLE
            + " WHERE " + KPI_INSTANCE_ID_COLUMN + " = :name";

    public static final String FIND_ALL_RT_KPIS = "SELECT * FROM " + RT_KPI_INST_TABLE;

    public static final String FIND_ALL_VISIBLE_RT_KPIS = FIND_ALL_RT_KPIS + " WHERE pmsc_kpi_def->>'is_visible' = true";

    public static final String FIND_ALL_BY_OFFSET_AND_LIMIT_STATEMENT = FIND_ALL_RT_KPIS + " ORDER BY kpi_instance_id OFFSET %2$d LIMIT %3$d";

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Autowired
    private FaultHandler faultHandler;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private JdbcConfig jdbcConfig;

    @Override
    public void createDeployedKpi(final KpiDefinitionDTO kpiDefinitionDTO, final String kpiDefName,
                                  final ProfileDefinition profileDefinition) {
        final List<String> aggregationFields = profileDefinition.getContext();
        this.createDeployedKpi(kpiDefinitionDTO, kpiDefName, aggregationFields);
    }

    @Override
    public void createDeployedKpi(final KpiDefinitionDTO kpiDefinitionDTO, final String kpiDefName, final List<String> aggregationFields) {
        final DataSource datasource = this.jdbcTemplate.getDataSource();

        if (Objects.isNull(datasource)) {
            throw new CsacDAOException("Cannot create datasource.");
        }
        try (final Connection connection = datasource.getConnection()) {
            final Array aggregationFieldsArray = connection.createArrayOf("VARCHAR", aggregationFields.toArray());
            final String kpiDef = this.mapper.writeValueAsString(kpiDefinitionDTO);
            final PreparedStatementSetter preparedStatementSetter = new PreparedStatementSetter() {
                @Override
                public void setValues(final PreparedStatement ps) throws SQLException {
                    ps.setString(1, kpiDefinitionDTO.getName());
                    ps.setString(2, kpiDefName);
                    ps.setArray(3, aggregationFieldsArray);
                    ps.setString(4, kpiDef);
                }
            };
            this.jdbcTemplate.update(String.format(INSERT_KPI_INSTANCE_STATEMENT, this.jdbcConfig.getRuntimeDatastoreSchemaName()),
                    preparedStatementSetter);
        } catch (final JsonProcessingException | SQLException e) {
            this.faultHandler.fatal(e);
            throw new CsacDAOException(e);
        }
    }

    @Override
    public void updateDeployedKpi(final KpiDefinitionDTO kpiDefinitionDTO) {
        try {
            final String kpiDef = this.mapper.writeValueAsString(kpiDefinitionDTO);
            final String rtKpiInstTable = String.format(RT_KPI_INST_TABLE, this.jdbcConfig.getRuntimeDatastoreSchemaName());
            final String updateDeployedKpiStmt = String.format("UPDATE %s SET %s = '%s' WHERE %s = '%s'",
                    rtKpiInstTable,
                    PMSC_KPI_DEF_COLUMN,
                    kpiDef,
                    KPI_INSTANCE_ID_COLUMN,
                    kpiDefinitionDTO.getName());

            this.jdbcTemplate.update(updateDeployedKpiStmt);

        } catch (final JsonProcessingException e) {
            this.faultHandler.fatal(e);
            throw new CsacDAOException(e);
        }
    }

    @Override
    public void deleteDeployedKpi(final String pmscKpiName) {
        this.jdbcTemplate.update(String.format(DELETE_KPI_INST_BY_PMSC_KPI_NAME_STATEMENT, this.jdbcConfig.getRuntimeDatastoreSchemaName()),
                pmscKpiName);
    }

    @Override
    public KpiDefinitionDTO getDeployedKpi(final String pmscKpiName) {

        final SqlParameterSource namedParameter = new MapSqlParameterSource()
                .addValue("name", pmscKpiName);

        final List<KpiDefinitionDTO> rs = this.namedParameterJdbcTemplate.query(
                String.format(GET_KPI_BY_NAME, this.jdbcConfig.getRuntimeDatastoreSchemaName()),
                namedParameter,
                new DeployedKpiDefinitionMapper());

        if (ObjectUtils.isEmpty(rs)) {
            return null;
        }

        return rs.get(0);
    }

    @Override
    public List<KpiDefinitionDTO> getDeployedKpiByProfile(final ProfileDefinition profileDefinition) {
        final String getKpiInstanceStmt = String.format(GET_DEPLOYED_KPI_BY_PROFILE_NAME, this.jdbcConfig.getRuntimeDatastoreSchemaName(),
                profileDefinition.getName());
        return this.jdbcTemplate.query(getKpiInstanceStmt, new DeployedKpiDefinitionMapper());
    }

    @Override
    public List<KpiDefinitionDTO> getDeployedKpiByDefinitionName(final String kpiDefinitionName) {
        final String kpiInstTable = String.format(RT_KPI_INST_TABLE, this.jdbcConfig.getRuntimeDatastoreSchemaName());
        final String getDeployedKpiByKpiDefNameStmt = String.format("SELECT * FROM %s WHERE %s = '%s'",
                kpiInstTable,
                KPI_DEF_NAME_COLUMN,
                kpiDefinitionName);

        return this.jdbcTemplate.query(getDeployedKpiByKpiDefNameStmt, new DeployedKpiDefinitionMapper());
    }

    @Override
    public KpiDefinitionDTO getDeployedKpiByAggregation(final String kpiDefinitionName, final List<String> aggregationFields) {
        final String kpiInstTable = String.format(RT_KPI_INST_TABLE, this.jdbcConfig.getRuntimeDatastoreSchemaName());
        final String fields = String.join(", ", aggregationFields);
        final String getKpiStatement = String.format("SELECT * FROM %s WHERE kpi_def_name = '%s' AND agg_fields = '{%s}'",
                kpiInstTable,
                kpiDefinitionName,
                fields);

        final List<KpiDefinitionDTO> kpis = this.jdbcTemplate.query(getKpiStatement, new DeployedKpiDefinitionMapper());

        if (kpis.isEmpty()) {
            return null;
        }

        return kpis.get(0);

    }

    @Override
    public List<KpiDefinitionDTO> getAllDeployedKpis() {
        return this.jdbcTemplate.query(String.format(FIND_ALL_RT_KPIS, this.jdbcConfig.getRuntimeDatastoreSchemaName()),
                new DeployedKpiDefinitionMapper());
    }

    @Override
    public int totalDeployedKpiDefinitions() {
        return this.jdbcTemplate.queryForObject(String.format(COUNT_STATEMENT, this.jdbcConfig.getRuntimeDatastoreSchemaName()), Integer.class);
    }

    @Override
    public List<RuntimeKpiInstance> findAllRuntimeKpis() {
        return this.jdbcTemplate.query(String.format(FIND_ALL_RT_KPIS, this.jdbcConfig.getRuntimeDatastoreSchemaName()),
                new RuntimeKpiInstanceMapper());
    }

    @Override
    public List<RuntimeKpiInstance> findAllRuntimeKpis(final Integer start, final Integer rows) {

        final Integer total = this.totalDeployedKpiDefinitions();

        final Pair<Integer, Integer> safeIndex = getSafeSublistIndex(total, start, rows);
        final String sql = String.format(FIND_ALL_BY_OFFSET_AND_LIMIT_STATEMENT, this.jdbcConfig.getRuntimeDatastoreSchemaName(),
                safeIndex.getFirst(), rows);

        return this.jdbcTemplate.query(sql, new RuntimeKpiInstanceMapper());
    }

    @Override
    public List<RuntimeKpiInstance> findAllRuntimeKpis(final boolean visibleOnly) {

        return visibleOnly ?
                this.jdbcTemplate.query(String.format(FIND_ALL_VISIBLE_RT_KPIS, this.jdbcConfig.getRuntimeDatastoreSchemaName()),
                        new RuntimeKpiInstanceMapper()) :
                this.findAllRuntimeKpis();

    }

    @Override
    public List<RuntimeKpiInstance> findAllByContextId(final KpiContextId contextId, final boolean visibleOnly) {

        final List<RuntimeKpiInstance> allRuntimeKpis = this.findAllRuntimeKpis(visibleOnly);

        return allRuntimeKpis.stream()
                .filter(runtimeKpiInstance -> {
                    final KpiContextId kpiContextId = KpiContextId.of(new HashSet<>(runtimeKpiInstance.getContextFieldList()));
                    return kpiContextId.equals(contextId);
                })
                .toList();
    }

}
