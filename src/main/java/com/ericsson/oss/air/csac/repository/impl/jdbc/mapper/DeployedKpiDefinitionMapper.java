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

import static com.ericsson.oss.air.csac.repository.impl.jdbc.DeployedKpiDefDAOJdbcImpl.PMSC_KPI_DEF_COLUMN;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.ericsson.oss.air.csac.model.pmsc.KpiDefinitionDTO;
import com.ericsson.oss.air.exception.CsacDAOException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.RowMapper;

@Slf4j
public class DeployedKpiDefinitionMapper implements RowMapper<KpiDefinitionDTO> {

    private static final ObjectMapper MAPPER = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    @Override
    public KpiDefinitionDTO mapRow(final ResultSet rs, final int rowNum) throws SQLException {
        final String jsonString = rs.getString(PMSC_KPI_DEF_COLUMN);
        try {
            return MAPPER.readValue(jsonString, KpiDefinitionDTO.class);
        } catch (JsonProcessingException e) {
            log.error("Unable to deserialize \n", e);
            throw new CsacDAOException(e);
        }
    }
}
