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

import java.sql.SQLException;

import com.ericsson.oss.air.csac.model.AugmentationDefinition;
import com.ericsson.oss.air.csac.model.TestResourcesUtils;
import com.ericsson.oss.air.csac.repository.impl.jdbc.AugmentationDefinitionDAOJdbcImpl;
import com.ericsson.oss.air.exception.CsacDAOException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.h2.tools.SimpleResultSet;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class AugmentationDefinitionMapperTest {

    @Test
    void mapRowTest() throws JsonProcessingException, SQLException {
        final AugmentationDefinition expectedAugDef = TestResourcesUtils.VALID_AUGMENTATION_DEF_OBJ;
        final String augDefStr = new ObjectMapper().writeValueAsString(expectedAugDef);

        final AugmentationDefinitionMapper mapper = new AugmentationDefinitionMapper();

        final SimpleResultSet resultSet = new SimpleResultSet();
        resultSet.addColumn(AugmentationDefinitionDAOJdbcImpl.COLUMN_DEF, 0, 0, 0);
        resultSet.addRow(augDefStr);
        resultSet.next();

        final AugmentationDefinition actualAugDef = mapper.mapRow(resultSet, 0);
        Assertions.assertNotNull(actualAugDef);
        Assertions.assertEquals(expectedAugDef, actualAugDef);
    }

    @Test
    void mapRow_SQLException() throws SQLException {
        final AugmentationDefinitionMapper mapper = new AugmentationDefinitionMapper();

        final SimpleResultSet resultSet = new SimpleResultSet();
        resultSet.addColumn("RANDOM_COLUMN_NAME", 0, 0, 0);
        resultSet.next();
        Assertions.assertThrows(SQLException.class, () -> mapper.mapRow(resultSet, 0));
    }

    @Test
    void mapRow_JsonProcessingException() throws SQLException {
        final AugmentationDefinitionMapper mapper = new AugmentationDefinitionMapper();

        final SimpleResultSet resultSet = new SimpleResultSet();
        resultSet.addColumn(AugmentationDefinitionDAOJdbcImpl.COLUMN_DEF, 0, 0, 0);
        resultSet.addRow("blah!");
        resultSet.next();

        Assertions.assertThrows(CsacDAOException.class, () -> mapper.mapRow(resultSet, 0));
    }

}
