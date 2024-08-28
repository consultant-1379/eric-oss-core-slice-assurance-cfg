/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.model.pmsc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import jakarta.validation.Validation;

import com.ericsson.oss.air.util.codec.Codec;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;

class ParameterDTOTest {

    private static final String PARAM_EXECUTION_ID = "param.execution_id";
    private static final String PARAM_DATE_FOR_FILTER = "param.date_for_filter";

    private static final String PARAMETER = "{\"" + PARAM_EXECUTION_ID + "\":\"id\",\"" + PARAM_DATE_FOR_FILTER + "\":\"filter\"}";
    private Codec codec = new Codec(Validation.buildDefaultValidatorFactory().getValidator());

    @Test
    public void testParameterDTO() throws JsonProcessingException {
        final ParameterDTO expectedParaDTO = new ParameterDTO("id", "filter");
        final ParameterDTO paraDTO = this.codec.withValidation().readValue(PARAMETER, ParameterDTO.class);

        assertNotNull(paraDTO);
        assertEquals(expectedParaDTO, paraDTO);
    }

}