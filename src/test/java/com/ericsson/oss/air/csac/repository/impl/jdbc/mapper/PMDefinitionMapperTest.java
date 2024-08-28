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

import static com.ericsson.oss.air.csac.repository.impl.jdbc.PMDefinitionDAOJdbcImpl.*;
import static org.junit.jupiter.api.Assertions.*;

import java.sql.SQLException;

import com.ericsson.oss.air.csac.model.PMDefinition;
import com.ericsson.oss.air.csac.model.TestResourcesUtils;
import org.h2.tools.SimpleResultSet;
import org.junit.jupiter.api.Test;

class PMDefinitionMapperTest {

    @Test
    void mapRow() throws SQLException {
        final PMDefinition pmSample = TestResourcesUtils.VALID_PM_DEF_OBJ;
        final PMDefinitionMapper pmMapper = new PMDefinitionMapper();

        final SimpleResultSet resultSet = new SimpleResultSet();
        resultSet.addColumn(COLUMN_NAME, 0, 0, 0);
        resultSet.addColumn(COLUMN_SOURCE, 0, 0, 0);
        resultSet.addColumn(COLUMN_DESCRIPTION, 0, 0, 0);

        resultSet.addRow(pmSample.getName(), pmSample.getSource(), pmSample.getDescription());
        resultSet.next();

        final PMDefinition pm = pmMapper.mapRow(resultSet, 0);
        assertNotNull(pm);
        assertEquals(pmSample, pm);

    }

    @Test
    void mapRow_SQLException() throws SQLException {
        final PMDefinitionMapper mapper = new PMDefinitionMapper();

        final SimpleResultSet resultSet = new SimpleResultSet();
        resultSet.addColumn("RANDOM_COLUMN_NAME", 0, 0, 0);
        resultSet.next();

        assertThrows(SQLException.class, () -> mapper.mapRow(resultSet, 0));

    }
}