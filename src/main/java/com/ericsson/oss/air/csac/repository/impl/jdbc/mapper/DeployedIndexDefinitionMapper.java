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

import static com.ericsson.oss.air.csac.repository.impl.jdbc.DeployedIndexDefinitionDaoJdbcImpl.COLUMN_IDX_DEF;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.ericsson.oss.air.csac.model.runtime.index.DeployedIndexDefinitionDto;
import com.ericsson.oss.air.exception.CsacDAOException;
import com.ericsson.oss.air.util.codec.Codec;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.RowMapper;

/**
 * This class maps a record from the runtime deployed index definition persistent store to a {@link DeployedIndexDefinitionDto} instances.
 */
@Slf4j
public class DeployedIndexDefinitionMapper implements RowMapper<DeployedIndexDefinitionDto> {

    private static final Codec CODEC = new Codec();

    @Override
    public DeployedIndexDefinitionDto mapRow(final ResultSet rs, final int rowNum) throws SQLException {

        final String jsonString = rs.getString(COLUMN_IDX_DEF);

        try {
            return CODEC.readValue(jsonString, DeployedIndexDefinitionDto.class);
        } catch (final JsonProcessingException e) {
            log.error("Unable to deserialize deployed index definition", e);
            throw new CsacDAOException(e);
        }
    }
}
