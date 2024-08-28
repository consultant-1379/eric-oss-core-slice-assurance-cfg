/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.service.kpi.pmsc.legacy;

import static com.ericsson.oss.air.csac.service.kpi.pmsc.legacy.PMSCRestClientTest.KPI_CALCULATION_DTO_OBJECT;
import static com.ericsson.oss.air.csac.service.kpi.pmsc.legacy.PMSCRestClientTest.KPI_DEFINITIONS_SUBMISSION;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.ericsson.oss.air.exception.CsacValidationException;
import com.ericsson.oss.air.util.codec.Codec;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class DryrunPmscClientTest {

    private final DryrunPmscClient testClient = new DryrunPmscClient().setCodec(new Codec());

    @Test
    void updatePMSCKpisDefinitions() {

        final ResponseEntity<Void> actual = this.testClient.updatePMSCKpisDefinitions(KPI_DEFINITIONS_SUBMISSION);

        assertEquals(HttpStatus.OK, actual.getStatusCode());
    }

    @Test
    void createPMSCKpisCalculation() {

        final ResponseEntity<Void> actual = this.testClient.createPMSCKpisCalculation(KPI_CALCULATION_DTO_OBJECT);

        assertEquals(HttpStatus.OK, actual.getStatusCode());
    }

    @Test
    void checkUrlString() throws Exception {

        assertDoesNotThrow(() -> this.testClient.checkUrl("http://test.com:8080"));
        assertThrows(CsacValidationException.class, () -> this.testClient.checkUrl("Not A Url"));
        assertThrows(CsacValidationException.class, () -> this.testClient.checkUrl("https://test.com/ java-%%$^&& iuyi"));
    }
}