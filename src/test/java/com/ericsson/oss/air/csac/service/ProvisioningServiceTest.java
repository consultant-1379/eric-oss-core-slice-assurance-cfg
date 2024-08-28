/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;


import java.net.URL;


import com.ericsson.oss.air.exception.CsacValidationException;
import org.junit.jupiter.api.Test;

class ProvisioningServiceTest {

    private static final ProvisioningService TEST_PROVISIONING_SERVICE = new ProvisioningService() {
    };

    @Test
    void checkUrlString() throws Exception {

        assertDoesNotThrow(() -> TEST_PROVISIONING_SERVICE.checkUrl("http://test.com:8080"));
        assertThrows(CsacValidationException.class, () -> TEST_PROVISIONING_SERVICE.checkUrl("Not A Url"));
        assertThrows(CsacValidationException.class, () -> TEST_PROVISIONING_SERVICE.checkUrl("https://test.com/ java-%%$^&& iuyi"));
    }

    @Test
    void checkUrl() throws Exception {

        final URL url1 = new URL("http://test.com:8080");
        final URL url2 = new URL("https://test.com/ java-%%$^&& iuyi");

        assertDoesNotThrow(() -> TEST_PROVISIONING_SERVICE.checkUrl(url1));
        assertThrows(CsacValidationException.class, () -> TEST_PROVISIONING_SERVICE.checkUrl(url2));
    }
}