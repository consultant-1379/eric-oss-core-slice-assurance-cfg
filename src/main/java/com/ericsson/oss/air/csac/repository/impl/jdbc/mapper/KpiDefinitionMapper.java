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

import com.ericsson.oss.air.csac.model.KPIDefinition;
import org.springframework.jdbc.core.RowMapper;

/**
 * The type KpiDefinition mapper.
 */
public class KpiDefinitionMapper implements RowMapper<KPIDefinition> {

    @Override
    public KPIDefinition mapRow(final ResultSet kpiResultSet, final int rowNum) throws SQLException {
        return KPIDefinition.builder()
                .name(kpiResultSet.getString(COLUMN_NAME))
                .description(kpiResultSet.getString(COLUMN_DESCRIPTION))
                .displayName(kpiResultSet.getString(COLUMN_DISPLAY_NAME))
                .expression(kpiResultSet.getString(COLUMN_EXPRESSION))
                .aggregationType(kpiResultSet.getString(COLUMN_AGGREGATION_TYPE))
                .isVisible(kpiResultSet.getBoolean(COLUMN_IS_VISIBLE))
                .build();
    }
}
