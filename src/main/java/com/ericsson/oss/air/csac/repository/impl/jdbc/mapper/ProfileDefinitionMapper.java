/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.repository.impl.jdbc.mapper;

import static com.ericsson.oss.air.csac.repository.impl.jdbc.ProfileDefinitionDAOJdbcImpl.COLUMN_DEF;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.ericsson.oss.air.csac.model.ProfileDefinition;
import com.ericsson.oss.air.exception.CsacDAOException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.RowMapper;

/**
 * Maps row from dictionary datastore profile table to profile definition.
 */
@Slf4j
public class ProfileDefinitionMapper implements RowMapper<ProfileDefinition> {

    @Override
    public ProfileDefinition mapRow(ResultSet rs, int rowNum) throws SQLException {
        final ObjectMapper mapper = new ObjectMapper();
        final String jsonString = rs.getString(COLUMN_DEF);

        try {
            return mapper.readValue(jsonString, ProfileDefinition.class);
        } catch (final JsonProcessingException e) {
            log.error("Unable to deserialize \n", e);
            throw new CsacDAOException(e);
        }
    }
}
