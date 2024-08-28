/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.handler.validation.pmsc;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.HashMap;
import java.util.Map;

import com.ericsson.oss.air.csac.configuration.kpi.pmsc.PmscProperties;
import com.ericsson.oss.air.exception.CsacValidationException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PmscConfigurationValidatorTest {

    private PmscConfigurationValidator pmscValidator;

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

    private static final String INVALID_PMSC_PROPS_JSON = "{\n" +
            "      \"enabled\": true,\n" +
            "      \"restClient\": {\n" +
            "        \"legacy\": false,\n" +
            "        \"url\": \"http://eric-oss-pm-stats-calc:8080\"\n" +
            "      },\n" +
            "      \"model\": {\n" +
            "        \"legacy\": false\n" +
            "      },\n" +
            "      \"data\": {\n" +
            "        \"reliabilityOffset\": 30\n" +
            "      },\n" +
            "      \"aggregationPeriod\": {\n" +
            "        \"default\": 15\n" +
            "      }\n" +
            "}";

    @BeforeEach
    void setUp() throws Exception {

        final Map<String, Object> props = new ObjectMapper().readValue(VALID_PMSC_PROPS_JSON, HashMap.class);
        final PmscProperties pmscProperties = new PmscProperties();
        pmscProperties.setPmsc(props);

        this.pmscValidator = new PmscConfigurationValidator(pmscProperties);
    }

    @Test
    void validateAppConfig() {

        assertDoesNotThrow(() -> this.pmscValidator.validateAppConfig());
    }

    @Test
    void checkDataReliabilityOffset() {

        assertDoesNotThrow(() -> this.pmscValidator.checkDataReliabilityOffset());
    }

    @Test
    void checkDataReliabilityOffset_invalidValue() throws Exception {

        final Map<String, Object> props = new ObjectMapper().readValue(INVALID_PMSC_PROPS_JSON, HashMap.class);
        final PmscProperties pmscProperties = new PmscProperties();
        pmscProperties.setPmsc(props);

        final PmscConfigurationValidator validator = new PmscConfigurationValidator(pmscProperties);

        assertThrows(CsacValidationException.class, () -> validator.checkDataReliabilityOffset());
    }
}