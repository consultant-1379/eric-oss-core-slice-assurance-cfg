/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.configuration.augmentation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class AugmentationPropertiesTest {

    public static final boolean ENABLED = true;

    private static final String AAS_URL = "http://127.0.0.1:8080";

    private static final String CARDQ_URL = "http://cardq:8080";

    private static final String TEST_ARDQ_URL = "http://testardq:8080";

    public static final String CARDQ_ID = "cardq";

    public static final String TEST_ARDQ_ID = "testardq";

    private static AugmentationProperties augmentationProperties;

    @BeforeEach
    public void setup() {
        final Map<String, String> ardqConfig = new HashMap<>();
        ardqConfig.put(CARDQ_ID, CARDQ_URL);
        ardqConfig.put(TEST_ARDQ_ID, TEST_ARDQ_URL);

        final Map<String, Object> aasConfig = new HashMap<>();
        aasConfig.put("enabled", ENABLED);
        aasConfig.put("url", AAS_URL);
        aasConfig.put("ardq", ardqConfig);

        augmentationProperties = new AugmentationProperties(aasConfig);
    }

    @Test
    void isEnabled_test() {
        assertTrue(augmentationProperties.isEnabled());
    }

    @Test
    void getAasUrl_test() {
        assertEquals(AAS_URL, augmentationProperties.getAasUrl());
    }

    @Test
    void getArdqUrl_test() {
        assertEquals(CARDQ_URL, augmentationProperties.getArdqUrl(CARDQ_ID).get());
        assertEquals(TEST_ARDQ_URL, augmentationProperties.getArdqUrl(TEST_ARDQ_ID).get());
    }

    @Test
    void getArdqUrl_ardqIdDoNotExists_returnEmptyOptional() {
        assertTrue(augmentationProperties.getArdqUrl("cArDq").isEmpty());
    }

    @Test
    void getArdqConfig_test() {
        final Map<String, String> ardqConfig = new HashMap<>();
        ardqConfig.put(CARDQ_ID, CARDQ_URL);
        ardqConfig.put(TEST_ARDQ_ID, TEST_ARDQ_URL);

        assertEquals(ardqConfig, augmentationProperties.getArdqConfig());
    }

}
