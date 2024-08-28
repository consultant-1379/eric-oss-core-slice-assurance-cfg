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

import static com.ericsson.oss.air.csac.repository.impl.jdbc.DeployedKpiDefDAOJdbcImpl.KPI_DEF_NAME_COLUMN;
import static com.ericsson.oss.air.csac.repository.impl.jdbc.DeployedKpiDefDAOJdbcImpl.KPI_INSTANCE_ID_COLUMN;
import static com.ericsson.oss.air.csac.repository.impl.jdbc.DeployedKpiDefDAOJdbcImpl.PMSC_KPI_DEF_COLUMN;
import static com.ericsson.oss.air.csac.repository.impl.jdbc.DeployedProfileDAOJdbcImpl.COLUMN_AGG_FIELDS;

import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

import com.ericsson.oss.air.csac.model.runtime.RuntimeKpiDefinition;
import com.ericsson.oss.air.csac.model.runtime.RuntimeKpiInstance;
import com.ericsson.oss.air.exception.CsacDAOException;
import com.ericsson.oss.air.util.codec.PolymorphicObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.RowMapper;

/**
 * ResultSet row mapper for {@link RuntimeKpiInstance} objects returned from the runtime data store.
 */
@Slf4j
public class RuntimeKpiInstanceMapper implements RowMapper<RuntimeKpiInstance> {

    @Override
    public RuntimeKpiInstance mapRow(final ResultSet rs, final int rowNum) throws SQLException {

        try {

            final RuntimeKpiInstance rtKpiInstance = new RuntimeKpiInstance();

            final String instanceId = rs.getString(KPI_INSTANCE_ID_COLUMN);
            final String jsonString = rs.getString(PMSC_KPI_DEF_COLUMN);
            final Array aggFields = rs.getArray(COLUMN_AGG_FIELDS);
            final String kpiDefName = rs.getString(KPI_DEF_NAME_COLUMN);

            rtKpiInstance.setInstanceId(instanceId);
            rtKpiInstance.setContextFieldList(Arrays.asList((String[]) aggFields.getArray()));
            rtKpiInstance.setKpDefinitionName(kpiDefName);

            final RuntimeKpiDefinition kpiDef = PolymorphicObjectMapper.mapper().readValue(jsonString, RuntimeKpiDefinition.class);

            rtKpiInstance.setRuntimeDefinition(kpiDef);

            return rtKpiInstance;

        } catch (final JsonProcessingException e) {
            log.error("Unable to deserialize", e);
            throw new CsacDAOException(e);
        }
    }
}
