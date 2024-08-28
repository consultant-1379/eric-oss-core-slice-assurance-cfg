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

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.ericsson.oss.air.csac.model.KPIReference;
import com.ericsson.oss.air.csac.model.ProfileDefinition;
import com.ericsson.oss.air.csac.repository.impl.jdbc.DeployedProfileDAOJdbcImpl;
import org.h2.tools.SimpleResultSet;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class RichProfileDefinitionMapperTest {

    @Test
    void mapRowTest() throws SQLException {
        final String profileName = "profileName";
        final String description = "Description";
        final List<String> aggFields = List.of("aggField1", "aggField2");
        final List<String> kpiRefs = List.of("kpiRef1", "kpiRef2");

        final ProfileDefinition expectedProfile = ProfileDefinition.builder()
                .name(profileName)
                .description(description)
                .context(aggFields)
                .kpis(kpiRefs.stream().map(ref -> KPIReference.builder().ref(ref).build()).collect(Collectors.toList()))
                .build();
        final RichProfileDefinitionMapper mapper = new RichProfileDefinitionMapper();

        final SimpleResultSet resultSet = new SimpleResultSet();
        resultSet.addColumn(DeployedProfileDAOJdbcImpl.COLUMN_NAME, 0, 0, 0);
        resultSet.addColumn(DeployedProfileDAOJdbcImpl.COLUMN_DESCRIPTION, 0, 0, 0);
        resultSet.addColumn(DeployedProfileDAOJdbcImpl.COLUMN_AGG_FIELDS, 0, 0, 0);
        resultSet.addColumn(DeployedProfileDAOJdbcImpl.COLUMN_KPI_REFS, 0, 0, 0);

        final String[] aggFieldsInput = Arrays.copyOf(aggFields.toArray(), aggFields.toArray().length, String[].class);
        final String[] kpiRefsInput = Arrays.copyOf(kpiRefs.toArray(), kpiRefs.toArray().length, String[].class);
        resultSet.addRow(profileName, description, aggFieldsInput, kpiRefsInput);
        resultSet.next();

        final ProfileDefinition actualProfile = mapper.mapRow(resultSet, 0);

        Assertions.assertNotNull(actualProfile);
        Assertions.assertEquals(expectedProfile, actualProfile);
    }

    @Test
    void mapRow_SQLException() throws SQLException {
        final RichProfileDefinitionMapper mapper = new RichProfileDefinitionMapper();

        final SimpleResultSet resultSet = new SimpleResultSet();
        resultSet.addColumn("RANDOM_COLUMN_NAME", 0, 0, 0);
        resultSet.next();

        assertThrows(SQLException.class, () -> mapper.mapRow(resultSet, 0));
    }
}
