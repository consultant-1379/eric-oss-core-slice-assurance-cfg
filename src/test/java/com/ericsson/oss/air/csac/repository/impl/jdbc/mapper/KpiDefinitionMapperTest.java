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

import static com.ericsson.oss.air.csac.repository.impl.jdbc.KPIDefinitionDAOJdbcImpl.COLUMN_AGGREGATION_TYPE;
import static com.ericsson.oss.air.csac.repository.impl.jdbc.KPIDefinitionDAOJdbcImpl.COLUMN_DESCRIPTION;
import static com.ericsson.oss.air.csac.repository.impl.jdbc.KPIDefinitionDAOJdbcImpl.COLUMN_DISPLAY_NAME;
import static com.ericsson.oss.air.csac.repository.impl.jdbc.KPIDefinitionDAOJdbcImpl.COLUMN_EXPRESSION;
import static com.ericsson.oss.air.csac.repository.impl.jdbc.KPIDefinitionDAOJdbcImpl.COLUMN_IS_VISIBLE;
import static com.ericsson.oss.air.csac.repository.impl.jdbc.KPIDefinitionDAOJdbcImpl.COLUMN_NAME;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.sql.SQLException;
import java.util.ArrayList;

import com.ericsson.oss.air.csac.model.KPIDefinition;
import com.ericsson.oss.air.csac.model.TestResourcesUtils;
import org.h2.tools.SimpleResultSet;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class KpiDefinitionMapperTest {

    @Test
    void mapRow() throws SQLException {
        final KPIDefinition kpi = TestResourcesUtils.VALID_COMPLEX_KPI_DEF_OBJ.toBuilder().inputMetrics(new ArrayList<>()).build();
        final KpiDefinitionMapper kpiDefinitionMapper = new KpiDefinitionMapper();
        final SimpleResultSet resultSet = new SimpleResultSet();
        resultSet.addColumn(COLUMN_NAME, 0, 0, 0);
        resultSet.addColumn(COLUMN_DESCRIPTION, 0, 0, 0);
        resultSet.addColumn(COLUMN_DISPLAY_NAME, 0, 0, 0);
        resultSet.addColumn(COLUMN_EXPRESSION, 0, 0, 0);
        resultSet.addColumn(COLUMN_AGGREGATION_TYPE, 0, 0, 0);
        resultSet.addColumn(COLUMN_IS_VISIBLE, 0, 0, 0);

        resultSet.addRow(kpi.getName(), kpi.getDescription(), kpi.getDisplayName(), kpi.getExpression(), kpi.getAggregationType(),
                kpi.getIsVisible());
        resultSet.next();

        final KPIDefinition kpiDefinition = kpiDefinitionMapper.mapRow(resultSet, 0);
        assertNotNull(kpiDefinition);
        Assertions.assertEquals(kpi, kpiDefinition);
    }

    @Test
    void mapRow_SQLException() throws SQLException {
        final KpiDefinitionMapper mapper = new KpiDefinitionMapper();

        final SimpleResultSet resultSet = new SimpleResultSet();
        resultSet.addColumn("RANDOM_COLUMN_NAME", 0, 0, 0);
        resultSet.next();

        assertThrows(SQLException.class, () -> mapper.mapRow(resultSet, 0));

    }
}