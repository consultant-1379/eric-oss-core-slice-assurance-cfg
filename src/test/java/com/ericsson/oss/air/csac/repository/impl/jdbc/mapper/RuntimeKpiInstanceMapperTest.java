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

import com.ericsson.oss.air.csac.model.pmsc.KpiDefinitionDTO;
import com.ericsson.oss.air.csac.model.runtime.RuntimeKpiInstance;
import com.ericsson.oss.air.exception.CsacDAOException;
import com.ericsson.oss.air.util.codec.Codec;
import org.h2.tools.SimpleResultSet;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.ericsson.oss.air.csac.repository.impl.jdbc.DeployedKpiDefDAOJdbcImpl.KPI_DEF_NAME_COLUMN;
import static com.ericsson.oss.air.csac.repository.impl.jdbc.DeployedKpiDefDAOJdbcImpl.KPI_INSTANCE_ID_COLUMN;
import static com.ericsson.oss.air.csac.repository.impl.jdbc.DeployedKpiDefDAOJdbcImpl.PMSC_KPI_DEF_COLUMN;
import static com.ericsson.oss.air.csac.repository.impl.jdbc.DeployedProfileDAOJdbcImpl.COLUMN_AGG_FIELDS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RuntimeKpiInstanceMapperTest {

    private static final String KPI_DEFINITION_DTO_STR = "{\n" +
            "                    \"name\": \"csac_01b45930_46d2_4991_a5b2_938ccd647bca\",\n" +
            "                    \"alias\": \"csac_complex_snssai\",\n" +
            "                    \"expression\": \"SUM(kpi_csac_simple_snssai_15.csac_cc42516f_a1fa_4a2c_b3bd_d6bb97a7a1a5) FROM kpi_db://kpi_csac_simple_snssai_15\",\n" +
            "                    \"object_type\": \"FLOAT\",\n" +
            "                    \"aggregation_type\": \"SUM\",\n" +
            "                    \"aggregation_period\": 15,\n" +
            "                    \"aggregation_elements\": [\n" +
            "                        \"kpi_csac_simple_snssai_15.snssai\"\n" +
            "                    ],\n" +
            "                    \"is_visible\": true,\n" +
            "                    \"execution_group\": \"csac_execution_group\"\n" +
            "                }";

    @Test
    void mapRow() throws Exception {

        final SimpleResultSet resultSet = new SimpleResultSet();

        resultSet.addColumn(KPI_INSTANCE_ID_COLUMN, 0, 0, 0);
        resultSet.addColumn(PMSC_KPI_DEF_COLUMN, 0, 0, 0);
        resultSet.addColumn(COLUMN_AGG_FIELDS, 0, 0, 0);
        resultSet.addColumn(KPI_DEF_NAME_COLUMN, 0, 0, 0);

        final String[] aggFieldsArray = { "snssai" };

        resultSet.addRow("csac_01b45930_46d2_4991_a5b2_938ccd647bca", KPI_DEFINITION_DTO_STR, aggFieldsArray, "PDUSesMaxNbr");
        resultSet.next();

        final RuntimeKpiInstance actual = new RuntimeKpiInstanceMapper().mapRow(resultSet, 0);

        assertNotNull(actual);

        assertEquals("csac_01b45930_46d2_4991_a5b2_938ccd647bca", actual.getInstanceId());
        assertEquals("PDUSesMaxNbr", actual.getKpDefinitionName());
        assertEquals(List.of("snssai"), actual.getContextFieldList());

        final KpiDefinitionDTO expectedDefinition = new Codec().readValue(KPI_DEFINITION_DTO_STR, KpiDefinitionDTO.class);

        assertEquals(expectedDefinition, actual.getRuntimeDefinition());

    }

    @Test
    void mapRow_invalidJson() throws Exception {

        final SimpleResultSet resultSet = new SimpleResultSet();

        resultSet.addColumn(KPI_INSTANCE_ID_COLUMN, 0, 0, 0);
        resultSet.addColumn(PMSC_KPI_DEF_COLUMN, 0, 0, 0);
        resultSet.addColumn(COLUMN_AGG_FIELDS, 0, 0, 0);
        resultSet.addColumn(KPI_DEF_NAME_COLUMN, 0, 0, 0);

        final String[] aggFieldsArray = { "snssai" };

        resultSet.addRow("csac_01b45930_46d2_4991_a5b2_938ccd647bca", "{\"foo\": \"bar\"}", aggFieldsArray, "PDUSesMaxNbr");
        resultSet.next();

        assertThrows(CsacDAOException.class, () -> new RuntimeKpiInstanceMapper().mapRow(resultSet, 0));
    }
}