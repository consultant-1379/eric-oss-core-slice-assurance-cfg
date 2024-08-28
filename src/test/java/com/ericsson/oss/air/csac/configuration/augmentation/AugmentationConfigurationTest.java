/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.configuration.augmentation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Optional;

import com.ericsson.oss.air.exception.CsacValidationException;
import com.ericsson.oss.air.util.logging.FaultHandler;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;

@ExtendWith(MockitoExtension.class)
public class AugmentationConfigurationTest {

    private static final String ARDQ_ID = "cardq";

    private static final String URL_REFERENCE = "${" + ARDQ_ID + "}";

    private static final String TEST_ARDQ_URL = "http://localhost:8080";

    @Mock
    private AugmentationProperties augmentationProperties;

    @Mock
    private Environment env;

    @Mock
    private FaultHandler faultHandler;

    @InjectMocks
    private AugmentationConfiguration configuration;

    @Test
    void getResolvedUrl_urlReference() {
        when(this.augmentationProperties.getArdqUrl(ARDQ_ID))
                .thenReturn(Optional.of(TEST_ARDQ_URL));

        assertEquals(TEST_ARDQ_URL, this.configuration.getResolvedUrl(URL_REFERENCE));
    }

    @Test
    void getResolvedUrl_url() {
        assertEquals(TEST_ARDQ_URL, this.configuration.getResolvedUrl(TEST_ARDQ_URL));
    }

    @Test
    void getResolvedUrl_noUrlForGivenReference_throwsException() {
        assertThrows(CsacValidationException.class, () -> this.configuration.getResolvedUrl(URL_REFERENCE));
    }

    @Test
    void isDryRunModeEnabled_dryrunMode() {

        when(this.env.getActiveProfiles()).thenReturn(new String[] { "dry-run", "test" });

        assertTrue(this.configuration.isDryRunModeEnabled());
    }

    @Test
    void isDryRunModeEnabled_prodMode() {

        when(this.env.getActiveProfiles()).thenReturn(new String[] { "prod" });

        assertFalse(this.configuration.isDryRunModeEnabled());
    }
}
