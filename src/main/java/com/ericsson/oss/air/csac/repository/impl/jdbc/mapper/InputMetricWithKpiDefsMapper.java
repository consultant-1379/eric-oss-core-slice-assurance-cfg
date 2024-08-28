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

import static com.ericsson.oss.air.csac.repository.impl.jdbc.KPIDefinitionDAOJdbcImpl.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.ericsson.oss.air.csac.model.InputMetric;
import com.ericsson.oss.air.csac.model.KPIDefinition;
import org.springframework.jdbc.core.RowMapper;

/**
 * The type InputMetric with kpi defs mapper. The mapped result will be inserted into corresponding {@link KPIDefinition}
 */
public class InputMetricWithKpiDefsMapper implements RowMapper<InputMetric> {

    private final List<KPIDefinition> kpis;

    public InputMetricWithKpiDefsMapper(final List<KPIDefinition> kpis) {
        this.kpis = new ArrayList<>(kpis);
    }

    @Override
    public InputMetric mapRow(final ResultSet iMResultSet, final int rowNum) throws SQLException {
        final String kpiName = iMResultSet.getString(COLUMN_KPI_NAME);
        final String imId = iMResultSet.getString(COLUMN_IM_ID);
        final String imAlias = iMResultSet.getString(COLUMN_IM_ALIAS);
        final InputMetric.Type type = InputMetric.Type.fromString(iMResultSet.getString(COLUMN_IM_INP_TYPE));
        final InputMetric inputMetric = new InputMetric(imId, imAlias, type);

        this.kpis.stream()
                .filter(kpiDefinition -> kpiDefinition.getName().equals(kpiName))
                .findFirst()
                .ifPresent(kpiDefinition -> kpiDefinition.addInputMetric(inputMetric));

        return inputMetric;
    }
}
