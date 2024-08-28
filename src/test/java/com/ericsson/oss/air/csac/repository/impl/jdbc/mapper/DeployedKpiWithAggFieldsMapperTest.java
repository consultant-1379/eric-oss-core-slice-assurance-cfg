/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.repository.impl.jdbc.mapper;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.sql.SQLException;
import java.util.*;

import com.ericsson.oss.air.csac.repository.impl.jdbc.DeployedKpiDefDAOJdbcImpl;
import com.ericsson.oss.air.csac.repository.impl.jdbc.DeployedProfileDAOJdbcImpl;
import org.h2.tools.SimpleResultSet;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.util.ObjectUtils;

public class DeployedKpiWithAggFieldsMapperTest {

    @Test
    void mapRowTest() throws SQLException {
        final Map<String, List<String>> KpiInstAggFieldsMap = new HashMap<>();
        final String kpiInstanceId = "kpiInstance1";
        final List<String> aggFields = new ArrayList<>();
        aggFields.add("aggField1");
        aggFields.add("aggField2");

        final DeployedKpiWithAggFieldsMapper mapper = new DeployedKpiWithAggFieldsMapper(KpiInstAggFieldsMap);
        final SimpleResultSet resultSet = new SimpleResultSet();
        final String[] aggFieldsInput = Arrays.copyOf(aggFields.toArray(), aggFields.toArray().length, String[].class);
        resultSet.addColumn(DeployedKpiDefDAOJdbcImpl.KPI_INSTANCE_ID_COLUMN, 0, 0, 0);
        resultSet.addColumn(DeployedProfileDAOJdbcImpl.COLUMN_AGG_FIELDS, 0, 0, 0);
        resultSet.addRow(kpiInstanceId, aggFieldsInput);
        resultSet.next();

        mapper.mapRow(resultSet, 0);

        Assertions.assertFalse(ObjectUtils.isEmpty(KpiInstAggFieldsMap));
        Assertions.assertEquals(aggFields.get(0), KpiInstAggFieldsMap.get(kpiInstanceId).get(0));
    }

    @Test
    void mapRowTest_SQLException() throws SQLException {
        final Map<String, List<String>> KpiInstAggFieldsMap = new HashMap<>();
        final DeployedKpiWithAggFieldsMapper mapper = new DeployedKpiWithAggFieldsMapper(KpiInstAggFieldsMap);

        final SimpleResultSet resultSet = new SimpleResultSet();
        resultSet.addColumn("RANDOM_COLUMN_NAME", 0, 0, 0);
        resultSet.next();
        assertThrows(SQLException.class, () -> mapper.mapRow(resultSet, 0));
    }
}
