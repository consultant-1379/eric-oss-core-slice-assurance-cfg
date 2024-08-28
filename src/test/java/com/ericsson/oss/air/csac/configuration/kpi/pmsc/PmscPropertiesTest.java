/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.configuration.kpi.pmsc;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PmscPropertiesTest {

    private static final String VALID_PMSC_PROPS_JSON = "{\n" +
            "      \"enabled\": true,\n" +
            "      \"restClient\": {\n" +
            "        \"legacy\": false,\n" +
            "        \"url\": \"http://eric-oss-pm-stats-calc:8080\"\n" +
            "      },\n" +
            "      \"model\": {\n" +
            "        \"legacy\": false\n" +
            "      },\n" +
            "      \"data\": {\n" +
            "        \"reliabilityOffset\": 0\n" +
            "      },\n" +
            "      \"aggregationPeriod\": {\n" +
            "        \"default\": 15\n" +
            "      }\n" +
            "}";

    private static final String VALID_PMSC_PROPS_NO_AGGREGATION_PERIODJSON = "{\n" +
            "      \"enabled\": true,\n" +
            "      \"restClient\": {\n" +
            "        \"legacy\": false,\n" +
            "        \"url\": \"http://eric-oss-pm-stats-calc:8080\"\n" +
            "      },\n" +
            "      \"model\": {\n" +
            "        \"legacy\": false\n" +
            "      },\n" +
            "      \"data\": {\n" +
            "        \"reliabilityOffset\": 0\n" +
            "      }\n" +
            "}";

    private static final String VALID_PMSC_PROPS_STRING_OFFSET_JSON = "{\n" +
            "      \"enabled\": true,\n" +
            "      \"restClient\": {\n" +
            "        \"legacy\": false,\n" +
            "        \"url\": \"http://eric-oss-pm-stats-calc:8080\"\n" +
            "      },\n" +
            "      \"model\": {\n" +
            "        \"legacy\": false\n" +
            "      },\n" +
            "      \"data\": {\n" +
            "        \"reliabilityOffset\": \"-15\"\n" +
            "      },\n" +
            "      \"aggregationPeriod\": {\n" +
            "        \"default\": 15\n" +
            "      }\n" +
            "}";

    private PmscProperties pmscProperties;

    @BeforeEach
    void setUp() throws Exception {

        final Map<String, Object> props = new ObjectMapper().readValue(VALID_PMSC_PROPS_JSON, HashMap.class);
        this.pmscProperties = new PmscProperties();
        this.pmscProperties.setPmsc(props);

    }

    @Test
    void getDataReliabilityOffset() {

        final int expected = 0;

        assertEquals(expected, this.pmscProperties.getDataReliabilityOffset());
    }

    @Test
    void getDataReliabilityOffset_stringValue() throws Exception {
        final Map<String, Object> props = new ObjectMapper().readValue(VALID_PMSC_PROPS_STRING_OFFSET_JSON, HashMap.class);
        final PmscProperties properties = new PmscProperties();
        properties.setPmsc(props);

        final int expected = -15;

        assertEquals(expected, properties.getDataReliabilityOffset());
    }

    @Test
    void getDefaultAggregationPeriod() {

        final int expected = 15;

        assertEquals(expected, this.pmscProperties.getDefaultAggregationPeriod());
    }

    @Test
    void getDefaultAggregationPeriod_noProperty() throws Exception {

        final Map<String, Object> props = new ObjectMapper().readValue(VALID_PMSC_PROPS_NO_AGGREGATION_PERIODJSON, HashMap.class);
        final PmscProperties properties = new PmscProperties();
        properties.setPmsc(props);

        final int expected = 15;

        assertEquals(expected, properties.getDefaultAggregationPeriod());
    }
}