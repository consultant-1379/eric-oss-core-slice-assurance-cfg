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

import static com.ericsson.oss.air.csac.repository.impl.jdbc.DeployedProfileDAOJdbcImpl.COLUMN_AGG_FIELDS;
import static com.ericsson.oss.air.csac.repository.impl.jdbc.DeployedProfileDAOJdbcImpl.COLUMN_DESCRIPTION;
import static com.ericsson.oss.air.csac.repository.impl.jdbc.DeployedProfileDAOJdbcImpl.COLUMN_KPI_REFS;
import static com.ericsson.oss.air.csac.repository.impl.jdbc.DeployedProfileDAOJdbcImpl.COLUMN_NAME;

import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.ericsson.oss.air.csac.model.KPIReference;
import com.ericsson.oss.air.csac.model.ProfileDefinition;
import org.springframework.jdbc.core.RowMapper;

public class RichProfileDefinitionMapper implements RowMapper<ProfileDefinition> {

    @Override
    public ProfileDefinition mapRow(final ResultSet rs, final int rowNum) throws SQLException {

        final Array aggFieldsSqlArray = rs.getArray(COLUMN_AGG_FIELDS);
        final List<String> aggFields = Arrays.asList((String[]) aggFieldsSqlArray.getArray());

        final Array kpiRefSqlArray = rs.getArray(COLUMN_KPI_REFS);
        final List<String> kpiRefs = Arrays.asList((String[]) kpiRefSqlArray.getArray());

        List<KPIReference> kpiRefsList = kpiRefs.stream()
                .map(ref -> KPIReference.builder().ref(ref).build())
                .collect(Collectors.toList());

        return ProfileDefinition.builder()
                .name(rs.getString(COLUMN_NAME))
                .description(rs.getString(COLUMN_DESCRIPTION))
                .context(aggFields)
                .kpis(kpiRefsList)
                .build();
    }
}
