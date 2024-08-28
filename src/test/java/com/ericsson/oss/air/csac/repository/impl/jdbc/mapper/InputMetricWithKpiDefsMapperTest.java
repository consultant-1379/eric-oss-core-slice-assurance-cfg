/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.repository.impl.jdbc.mapper;

import static com.ericsson.oss.air.csac.repository.impl.jdbc.KPIDefinitionDAOJdbcImpl.COLUMN_IM_ALIAS;
import static com.ericsson.oss.air.csac.repository.impl.jdbc.KPIDefinitionDAOJdbcImpl.COLUMN_IM_ID;
import static com.ericsson.oss.air.csac.repository.impl.jdbc.KPIDefinitionDAOJdbcImpl.COLUMN_IM_INP_TYPE;
import static com.ericsson.oss.air.csac.repository.impl.jdbc.KPIDefinitionDAOJdbcImpl.COLUMN_KPI_NAME;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.sql.SQLException;
import java.util.ArrayList;

import com.ericsson.oss.air.csac.model.InputMetric;
import com.ericsson.oss.air.csac.model.KPIDefinition;
import com.ericsson.oss.air.csac.model.TestResourcesUtils;
import org.h2.tools.SimpleResultSet;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class InputMetricWithKpiDefsMapperTest {

    @Test
    void mapRowTest() throws SQLException {
        // InputMetricWithKpiDefsMapper
        final InputMetric imSample = TestResourcesUtils.SIMPLE_INPUT_METRIC;
        final KPIDefinition kpiSource = TestResourcesUtils.VALID_SIMPLE_KPI_DEF_OBJ.toBuilder().inputMetrics(new ArrayList<>()).build();

        final InputMetricWithKpiDefsMapper mapper = new InputMetricWithKpiDefsMapper(singletonList(kpiSource));

        final SimpleResultSet resultSet = new SimpleResultSet();
        resultSet.addColumn(COLUMN_KPI_NAME, 0, 0, 0);
        resultSet.addColumn(COLUMN_IM_ID, 0, 0, 0);
        resultSet.addColumn(COLUMN_IM_ALIAS, 0, 0, 0);
        resultSet.addColumn(COLUMN_IM_INP_TYPE, 0, 0, 0);

        resultSet.addRow(kpiSource.getName(), imSample.getId(), imSample.getAlias(), imSample.getType());
        resultSet.next();

        assertEquals(0, kpiSource.getInputMetrics().size());

        final InputMetric inputMetric = mapper.mapRow(resultSet, 0);
        Assertions.assertNotNull(inputMetric);
        Assertions.assertEquals(imSample, inputMetric);
        Assertions.assertNotNull(kpiSource.getInputMetrics());
    }

    @Test
    void mapRow_SQLException() throws SQLException {
        final InputMetricWithKpiDefsMapper mapper = new InputMetricWithKpiDefsMapper(new ArrayList<>());

        final SimpleResultSet resultSet = new SimpleResultSet();
        resultSet.addColumn("RANDOM_COLUMN_NAME", 0, 0, 0);
        resultSet.next();

        assertThrows(SQLException.class, () -> mapper.mapRow(resultSet, 0));

    }

}