/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.model;

import com.ericsson.oss.air.util.codec.Codec;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;

import static org.junit.jupiter.api.Assertions.*;

class InputMetricTest {

    private static final String VALID_ID = "input_metric_id";
    private static final String INVALID_ID = " ";

    private static final String VALID_ALIAS = "input_alias";
    private static final String INVALID_ALIAS = "9 invalid alias";

    private static final String VALID_TYPE = "pm_data";
    private static final String INVALID_TYPE = "test";

    private static final String VALID_INPUT_METRIC =
        "{\"id\":\"" + VALID_ID + "\", \"alias\":\"" + VALID_ALIAS + "\", \"type\":\"" + VALID_TYPE + "\"}";

    private static final String INPUT_METRIC_WITH_EMPTY_ID =
        "{\"id\":\"" + INVALID_ID + "\", \"alias\":\"" + VALID_ALIAS + "\", \"type\":\"" + VALID_TYPE + "\"}";

    private static final String INPUT_METRIC_WITH_INVALID_ALIAS =
        "{\"id\":\"" + VALID_ID + "\", \"alias\":\"" + INVALID_ALIAS + "\", \"type\":\"" + VALID_TYPE + "\"}";

    private static final String INPUT_METRIC_WITHOUT_ALIAS = "{\"id\":\"" + VALID_ID + "\", \"type\":\"" + VALID_TYPE + "\"}";

    private static final String INPUT_METRIC_WITH_INVALID_TYPE =
        "{\"id\":\"" + VALID_ID + "\", \"alias\":\"" + VALID_ALIAS + "\", \"type\":\"" + INVALID_TYPE + "\"}";

    private Codec codec = new Codec(Validation.buildDefaultValidatorFactory().getValidator());

    @Test
    public void testValidInputMetric() throws JsonProcessingException {
        final InputMetric expectedInputMetric = new InputMetric(VALID_ID, VALID_ALIAS, InputMetric.Type.fromString(VALID_TYPE));
        final InputMetric inputMetric = this.codec.withValidation().readValue(VALID_INPUT_METRIC, InputMetric.class);

        assertNotNull(inputMetric);
        assertEquals(expectedInputMetric, inputMetric);
    }

    @Test
    public void testValidInputMetricWOAlias() throws JsonProcessingException {
        final InputMetric expectedInputMetric = new InputMetric(VALID_ID, null, InputMetric.Type.fromString(VALID_TYPE));
        final InputMetric inputMetric = this.codec.withValidation().readValue(INPUT_METRIC_WITHOUT_ALIAS, InputMetric.class);

        assertNotNull(inputMetric);
        assertEquals(expectedInputMetric, inputMetric);
    }

    @Test
    public void testInputMetricWithInvalidId() {
        assertThrows(ConstraintViolationException.class, () -> this.codec.withValidation().readValue(INPUT_METRIC_WITH_EMPTY_ID, InputMetric.class));
    }

    @Test
    public void testInputMetricWithInvalidAlias() {
        assertThrows(ConstraintViolationException.class,
            () -> this.codec.withValidation().readValue(INPUT_METRIC_WITH_INVALID_ALIAS, InputMetric.class));
    }

    @Test
    public void testInputMetricWithInvalidType() {
        assertThrows(JsonProcessingException.class, () -> this.codec.withValidation().readValue(INPUT_METRIC_WITH_INVALID_TYPE, InputMetric.class));
    }

}