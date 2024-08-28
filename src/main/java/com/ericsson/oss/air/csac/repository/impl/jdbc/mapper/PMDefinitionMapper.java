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

import java.sql.ResultSet;
import java.sql.SQLException;

import com.ericsson.oss.air.csac.model.PMDefinition;
import org.springframework.jdbc.core.RowMapper;

/**
 * The type PMDefinition mapper.
 */
public class PMDefinitionMapper implements RowMapper<PMDefinition> {

    @Override
    public PMDefinition mapRow(ResultSet rs, int rowNum) throws SQLException {
        return PMDefinition.builder()
                .name(rs.getString(COLUMN_NAME))
                .source(rs.getString(COLUMN_SOURCE))
                .description(rs.getString(COLUMN_DESCRIPTION))
                .build();

    }
}
