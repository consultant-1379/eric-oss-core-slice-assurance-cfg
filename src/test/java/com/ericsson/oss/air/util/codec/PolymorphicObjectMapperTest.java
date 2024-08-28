/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.util.codec;

import com.ericsson.oss.air.csac.model.pmsc.KpiDefinitionDTO;
import com.ericsson.oss.air.csac.model.pmsc.KpiTypeEnum;
import com.ericsson.oss.air.csac.model.runtime.RuntimeKpiDefinition;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.ericsson.oss.air.csac.model.TestResourcesUtils.DEPLOYED_COMPLEX_KPI_STR;
import static com.ericsson.oss.air.csac.model.TestResourcesUtils.DEPLOYED_SIMPLE_KPI_STR;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PolymorphicObjectMapperTest {

    private RuntimeKpiDefinition testKpiDefinition;

    private static final String TEST_BEAN_DEF = "{\"sField\":\"field1\",\"nField\":1,\"bField\":true}";

    private static final String OLD_KPI_DEF_DTO_WITH_ALIAS = "{\n" +
            "  \"name\": \"csac_d2c1fe14_f524_4c3c_9e1b_3f7f56ae06db\",\n" +
            "  \"alias\": \"csac_simple_gnodeb_cell\",\n" +
            "  \"expression\": \"SUM(PM-RadioNode-NR-NRCellDU-1.pmCounters.pmEbsnMacVolUlResUe)\",\n" +
            "  \"object_type\": \"FLOAT\",\n" +
            "  \"aggregation_type\": \"SUM\",\n" +
            "  \"aggregation_period\": 15,\n" +
            "  \"aggregation_elements\": [\n" +
            "    \"PM-RadioNode-NR-NRCellDU-1.gNodeB\",\n" +
            "    \"PM-RadioNode-NR-NRCellDU-1.cell\"\n" +
            "  ],\n" +
            "  \"is_visible\": false,\n" +
            "  \"inp_data_category\": \"pm_data\",\n" +
            "  \"inp_data_identifier\": \"5G|PM_COUNTERS|PM-RadioNode-NR-NRCellDU-1\"\n" +
            "}";

    private static final TestBean TEST_BEAN = new TestBean("field1", 1, true);

    private final Codec codec = new Codec();

    @BeforeEach
    void setUp() throws Exception {

        final KpiDefinitionDTO kpi = this.codec.readValue(DEPLOYED_SIMPLE_KPI_STR, KpiDefinitionDTO.class);

        this.testKpiDefinition = kpi;
    }

    @Test
    void testWriteValueAsString() throws Exception {

        assertEquals(TEST_BEAN_DEF, PolymorphicObjectMapper.mapper().writeValueAsString(TEST_BEAN));
    }

    @Test
    void testWriteValueAsStringPretty() throws Exception {

        final String expected = "{" + System.lineSeparator() +
                "  \"sField\" : \"field1\"," + System.lineSeparator() +
                "  \"nField\" : 1," + System.lineSeparator() +
                "  \"bField\" : true" + System.lineSeparator() + "}";

        assertEquals(expected, PolymorphicObjectMapper.mapper().writeValueAsStringPretty(TEST_BEAN));

    }

    @Test
    void testReadValue_SimpleKpi() throws Exception {

        final RuntimeKpiDefinition actual = PolymorphicObjectMapper.mapper().readValue(DEPLOYED_SIMPLE_KPI_STR, RuntimeKpiDefinition.class);

        assertNotNull(actual);
        assertEquals(KpiTypeEnum.SIMPLE, actual.getKpiType());
    }

    @Test
    void testReadValue_ComplexKpi() throws Exception {

        final RuntimeKpiDefinition actual = PolymorphicObjectMapper.mapper().readValue(DEPLOYED_COMPLEX_KPI_STR, RuntimeKpiDefinition.class);

        assertNotNull(actual);
        assertEquals(KpiTypeEnum.COMPLEX, actual.getKpiType());
    }

    // test for backwards-compatibility.  Previously, the 'alias' field was persisted with the KpiDefinitionDTO but it is a calculated field
    // in CSAC so does not need to be persisted.
    @Test
    void testReadValue_OldSimpleKpi() throws Exception {

        final RuntimeKpiDefinition actual = PolymorphicObjectMapper.mapper().readValue(OLD_KPI_DEF_DTO_WITH_ALIAS, RuntimeKpiDefinition.class);

        assertNotNull(actual);
        assertEquals(KpiTypeEnum.SIMPLE, actual.getKpiType());
    }

    @Test
    void testReadValue_jsonProcessingException() throws Exception {

        final String invalid = "{\"foo\": \"bar\"}";

        assertThrows(JsonProcessingException.class, () -> PolymorphicObjectMapper.mapper().readValue(invalid, RuntimeKpiDefinition.class));
    }
}