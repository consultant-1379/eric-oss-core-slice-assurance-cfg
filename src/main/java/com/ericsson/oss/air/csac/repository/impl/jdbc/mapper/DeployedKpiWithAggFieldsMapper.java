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

import static com.ericsson.oss.air.csac.repository.impl.jdbc.DeployedKpiDefDAOJdbcImpl.KPI_INSTANCE_ID_COLUMN;
import static com.ericsson.oss.air.csac.repository.impl.jdbc.DeployedProfileDAOJdbcImpl.COLUMN_AGG_FIELDS;

import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.RowMapper;

public class DeployedKpiWithAggFieldsMapper implements RowMapper<Map<String, List<String>>> {

    private final Map<String, List<String>> kpiInstAggFieldsMap;

    public DeployedKpiWithAggFieldsMapper(final Map<String, List<String>> kpiInstAggFieldsMap) {
        this.kpiInstAggFieldsMap = kpiInstAggFieldsMap;
    }

    @Override
    public Map<String, List<String>> mapRow(final ResultSet rs, final int rowNum) throws SQLException {
        final String kpiInstanceId = rs.getString(KPI_INSTANCE_ID_COLUMN);
        final Array aggFieldsSqlArray = rs.getArray(COLUMN_AGG_FIELDS);
        final List<String> aggFields = Arrays.asList((String[]) aggFieldsSqlArray.getArray());
        kpiInstAggFieldsMap.put(kpiInstanceId, aggFields);
        return kpiInstAggFieldsMap;
    }
}
