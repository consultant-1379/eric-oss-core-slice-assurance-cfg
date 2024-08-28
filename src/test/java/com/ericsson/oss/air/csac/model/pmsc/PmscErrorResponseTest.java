/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.model.pmsc;

import static org.junit.Assert.assertNotNull;

import java.text.ParseException;

import jakarta.validation.Validation;

import com.ericsson.oss.air.util.codec.Codec;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class PmscErrorResponseTest {

    private static final String PMSC_ERROR_RESPONSE = "{\"timeStamp\":\"2023-07-25T14:59:21.734\",\"status\":409,\"error\":\"Conflict\","
            + "\"message\":\"KPI name must be unique but 'rolling_bulk_sum_test_integer_2' is already defined in the database\"}";
    private Codec codec = new Codec(Validation.buildDefaultValidatorFactory().getValidator());

    @Test
    public void pmscErrorResponse_deserialized_successfully() throws JsonProcessingException, ParseException {
        final PmscErrorResponse actualPmscErrorResponse = this.codec.withValidation().readValue(PMSC_ERROR_RESPONSE, PmscErrorResponse.class);

        assertNotNull(actualPmscErrorResponse);
        assertNotNull(actualPmscErrorResponse.getTimeStamp());
        Assertions.assertEquals(409, actualPmscErrorResponse.getStatus());
        Assertions.assertEquals("Conflict", actualPmscErrorResponse.getError());
        Assertions.assertEquals("KPI name must be unique but 'rolling_bulk_sum_test_integer_2' is already defined in the database",
                actualPmscErrorResponse.getMessage());
    }

}