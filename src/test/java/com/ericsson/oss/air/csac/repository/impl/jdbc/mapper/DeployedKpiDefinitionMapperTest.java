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

import java.sql.SQLException;

import com.ericsson.oss.air.csac.model.TestResourcesUtils;
import com.ericsson.oss.air.csac.model.pmsc.KpiDefinitionDTO;
import com.ericsson.oss.air.csac.repository.impl.jdbc.DeployedKpiDefDAOJdbcImpl;
import com.ericsson.oss.air.exception.CsacDAOException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.h2.tools.SimpleResultSet;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class DeployedKpiDefinitionMapperTest {

    @Test
    void mapRowTest() throws JsonProcessingException, SQLException {
        final KpiDefinitionDTO expectedKpiDTO = new ObjectMapper().readValue(
                TestResourcesUtils.DEPLOYED_SIMPLE_KPI_STR,
                KpiDefinitionDTO.class);
        final DeployedKpiDefinitionMapper mapper = new DeployedKpiDefinitionMapper();

        final SimpleResultSet resultSet = new SimpleResultSet();
        resultSet.addColumn(DeployedKpiDefDAOJdbcImpl.PMSC_KPI_DEF_COLUMN, 0, 0, 0);
        resultSet.addRow(TestResourcesUtils.DEPLOYED_SIMPLE_KPI_STR);
        resultSet.next();

        final KpiDefinitionDTO actualKpiDTO = mapper.mapRow(resultSet, 0);
        Assertions.assertNotNull(actualKpiDTO);
        Assertions.assertEquals(expectedKpiDTO, actualKpiDTO);
    }

    @Test
    void mapRow_SQLException() throws SQLException {
        final DeployedKpiDefinitionMapper mapper = new DeployedKpiDefinitionMapper();

        final SimpleResultSet resultSet = new SimpleResultSet();
        resultSet.addColumn("RANDOM_COLUMN_NAME", 0, 0, 0);
        resultSet.next();
        Assertions.assertThrows(SQLException.class, () -> mapper.mapRow(resultSet, 0));
    }

    @Test
    void mapRow_JsonProcessingException() throws JsonProcessingException, SQLException {
        final DeployedKpiDefinitionMapper mapper = new DeployedKpiDefinitionMapper();

        final SimpleResultSet resultSet = new SimpleResultSet();
        resultSet.addColumn(DeployedKpiDefDAOJdbcImpl.PMSC_KPI_DEF_COLUMN, 0, 0, 0);
        resultSet.addRow("blah!");
        resultSet.next();

        Assertions.assertThrows(CsacDAOException.class, () -> mapper.mapRow(resultSet, 0));
    }
}
