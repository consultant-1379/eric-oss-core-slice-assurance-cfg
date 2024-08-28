/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

import com.ericsson.oss.air.util.codec.Codec;
import com.ericsson.oss.air.util.validation.config.ValidationConfiguration;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;

class KPIDefinitionTest {

    private static final String VALID_NAME = "kpidef_name";
    private static final String INVALID_NAME = " invalid kpidef_name";
    private static final String EMPTY_NAME = " invalid kpidef_name";

    private static final String DESCRIPTION = "kpidef decription";
    private static final String VALID_DISPLAY_NAME = "kpidef_display_name";
    private static final String VALID_EXPRESSION = "kpidef_expression";

    private static final String VALID_AGGREGATION_TYPE = "MAX";
    private static final String EMPTY_AGGREGATION_TYPE = "";

    private static final boolean VALID_IS_VISIBLE = true;

    // define InputMetric List
    private static final String VALID_ID = "input_metric_id";
    private static final String VALID_ALIAS = "input_alias";
    private static final String VALID_TYPE = "pm_data";

    private static final InputMetric VALID_INPUT_METRIC = new InputMetric(VALID_ID, VALID_ALIAS, InputMetric.Type.fromString(VALID_TYPE));
    private static final List<InputMetric> VALID_LIST_INPUT_METRICS = List.of(VALID_INPUT_METRIC);

    private static final String VALID_KPI_DEF =
            "{\"name\":\"" + VALID_NAME + "\", " + "\"description\":\"" + DESCRIPTION + "\", " + "\"display_name\":\"" + VALID_DISPLAY_NAME + "\", "
                    + "\"expression\":\"" + VALID_EXPRESSION + "\", " + "\"aggregation_type\":\"" + VALID_AGGREGATION_TYPE + "\", " + "\"is_visible\":\""
                    + VALID_IS_VISIBLE + "\", " + "\"input_metrics\":[" + "{\"id\":\"" + VALID_ID + "\", " + "\"alias\":\"" + VALID_ALIAS + "\", "
                    + "\"type\":\"" + VALID_TYPE + "\"}]}";

    private static final String VALID_KPI_DEF_WITH_AGG =
            "{\"name\":\"" + VALID_NAME + "\", " + "\"description\":\"" + DESCRIPTION + "\", " + "\"display_name\":\"" + VALID_DISPLAY_NAME + "\", "
                    + "\"expression\":\"" + VALID_EXPRESSION + "\", " + "\"aggregation_type\":\"" + VALID_AGGREGATION_TYPE + "\", " + "\"aggregation_period\": 15, " + "\"is_visible\":\""
                    + VALID_IS_VISIBLE + "\", " + "\"input_metrics\":[" + "{\"id\":\"" + VALID_ID + "\", " + "\"alias\":\"" + VALID_ALIAS + "\", "
                    + "\"type\":\"" + VALID_TYPE + "\"}]}";

    private static final String KPI_DEF_WITH_EMPTY_DESCRIPTION =
            "{\"name\":\"" + VALID_NAME + "\", " + "\"description\":\"\", " + "\"display_name\":\"" + VALID_DISPLAY_NAME + "\", " + "\"expression\":\""
                    + VALID_EXPRESSION + "\", " + "\"aggregation_type\":\"" + VALID_AGGREGATION_TYPE + "\", " + "\"is_visible\":\"" + VALID_IS_VISIBLE
                    + "\", " + "\"input_metrics\":[" + "{\"id\":\"" + VALID_ID + "\", " + "\"alias\":\"" + VALID_ALIAS + "\", " + "\"type\":\"" + VALID_TYPE
                    + "\"}]}";

    private static final String KPI_DEF_WITH_NO_DISCRIPTION =
            "{\"name\":\"" + VALID_NAME + "\", " + "\"display_name\":\"" + VALID_DISPLAY_NAME + "\", " + "\"expression\":\"" + VALID_EXPRESSION + "\", "
                    + "\"aggregation_type\":\"" + VALID_AGGREGATION_TYPE + "\", " + "\"is_visible\":\"" + VALID_IS_VISIBLE + "\", " + "\"input_metrics\":["
                    + "{\"id\":\"" + VALID_ID + "\", " + "\"alias\":\"" + VALID_ALIAS + "\", " + "\"type\":\"" + VALID_TYPE + "\"}]}";

    private static final String KPI_DEF_WITH_EMPTY_DISPLAY_NAME =
            "{\"name\":\"" + VALID_NAME + "\", " + "\"description\":\"" + DESCRIPTION + "\", " + "\"display_name\":\"\", " + "\"expression\":\""
                    + VALID_EXPRESSION + "\", " + "\"aggregation_type\":\"" + VALID_AGGREGATION_TYPE + "\", " + "\"is_visible\":\"" + VALID_IS_VISIBLE
                    + "\", " + "\"input_metrics\":[" + "{\"id\":\"" + VALID_ID + "\", " + "\"alias\":\"" + VALID_ALIAS + "\", " + "\"type\":\"" + VALID_TYPE
                    + "\"}]}";

    private static final String KPI_DEF_WITH_NO_DISPLAY_NAME =
            "{\"name\":\"" + VALID_NAME + "\", " + "\"description\":\"" + DESCRIPTION + "\", " + "\"expression\":\"" + VALID_EXPRESSION + "\", "
                    + "\"aggregation_type\":\"" + VALID_AGGREGATION_TYPE + "\", " + "\"is_visible\":\"" + VALID_IS_VISIBLE + "\", " + "\"input_metrics\":["
                    + "{\"id\":\"" + VALID_ID + "\", " + "\"alias\":\"" + VALID_ALIAS + "\", " + "\"type\":\"" + VALID_TYPE + "\"}]}";

    private static final String KPI_DEF_WITH_NO_IS_VISIBLE =
            "{\"name\":\"" + VALID_NAME + "\", " + "\"description\":\"" + DESCRIPTION + "\", " + "\"display_name\":\"" + VALID_DISPLAY_NAME + "\", "
                    + "\"expression\":\"" + VALID_EXPRESSION + "\", " + "\"aggregation_type\":\"" + VALID_AGGREGATION_TYPE + "\", " + "\"input_metrics\":["
                    + "{\"id\":\"" + VALID_ID + "\", " + "\"alias\":\"" + VALID_ALIAS + "\", " + "\"type\":\"" + VALID_TYPE + "\"}]}";

    private static final String KPI_DEF_WITH_INVALID_NAME =
            "{\"name\":\"" + INVALID_NAME + "\", " + "\"description\":\"" + DESCRIPTION + "\", " + "\"display_name\":\"" + VALID_DISPLAY_NAME + "\", "
                    + "\"expression\":\"" + VALID_EXPRESSION + "\", " + "\"aggregation_type\":\"" + VALID_AGGREGATION_TYPE + "\", " + "\"is_visible\":\""
                    + VALID_IS_VISIBLE + "\", " + "\"input_metrics\":[" + "{\"id\":\"" + VALID_ID + "\", " + "\"alias\":\"" + VALID_ALIAS + "\", "
                    + "\"type\":\"" + VALID_TYPE + "\"}]}";

    private static final String KPI_DEF_WITH_EMPTY_NAME =
            "{\"name\":\"" + EMPTY_NAME + "\", " + "\"description\":\"" + DESCRIPTION + "\", " + "\"display_name\":\"" + VALID_DISPLAY_NAME + "\", "
                    + "\"expression\":\"" + VALID_EXPRESSION + "\", " + "\"aggregation_type\":\"" + VALID_AGGREGATION_TYPE + "\", " + "\"is_visible\":\""
                    + VALID_IS_VISIBLE + "\", " + "\"input_metrics\":[" + "{\"id\":\"" + VALID_ID + "\", " + "\"alias\":\"" + VALID_ALIAS + "\", "
                    + "\"type\":\"" + VALID_TYPE + "\"}]}";

    private static final String KPI_DEF_WITH_EMPTY_EXPRESSION =
            "{\"name\":\"" + VALID_NAME + "\", " + "\"description\":\"" + DESCRIPTION + "\", " + "\"display_name\":\"" + VALID_DISPLAY_NAME + "\", "
                    + "\"expression\":\"\", " + "\"aggregation_type\":\"" + VALID_AGGREGATION_TYPE + "\", " + "\"is_visible\":\"" + VALID_IS_VISIBLE + "\", "
                    + "\"input_metrics\":[" + "{\"id\":\"" + VALID_ID + "\", " + "\"alias\":\"" + VALID_ALIAS + "\", " + "\"type\":\"" + VALID_TYPE + "\"}]}";

    private static final String KPI_DEF_WITH_EMPTY_AGGRERATION_TYPE =
            "{\"name\":\"" + VALID_NAME + "\", " + "\"description\":\"" + DESCRIPTION + "\", " + "\"display_name\":\"" + VALID_DISPLAY_NAME + "\", "
                    + "\"expression\":\"" + VALID_EXPRESSION + "\", " + "\"aggregation_type\":\"" + EMPTY_AGGREGATION_TYPE + "\", " + "\"is_visible\":\""
                    + VALID_IS_VISIBLE + "\", " + "\"input_metrics\":[" + "{\"id\":\"" + VALID_ID + "\", " + "\"alias\":\"" + VALID_ALIAS + "\", "
                    + "\"type\":\"" + VALID_TYPE + "\"}]}";

    private static final String KPI_DEF_WITH_INVALID_INPUT_METRIC =
            "{\"name\":\"" + VALID_NAME + "\", " + "\"description\":\"" + DESCRIPTION + "\", " + "\"display_name\":\"" + VALID_DISPLAY_NAME + "\", "
                    + "\"expression\":\"" + VALID_EXPRESSION + "\", " + "\"aggregation_type\":\"" + VALID_AGGREGATION_TYPE + "\", " + "\"is_visible\":\""
                    + VALID_IS_VISIBLE + "\", " + "\"input_metrics\":[" + "{\"id\":\"" + VALID_ID + "\", " + "\"alias\":\"" + " 777" + "\", " + "\"type\":\""
                    + VALID_TYPE + "\"}]}";

    private static final String KPI_DEF_WITH_EMPTY_INPUT_METRIC =
            "{\"name\":\"" + VALID_NAME + "\", " + "\"description\":\"" + DESCRIPTION + "\", " + "\"display_name\":\"" + VALID_DISPLAY_NAME + "\", "
                    + "\"expression\":\"" + VALID_EXPRESSION + "\", " + "\"aggregation_type\":\"" + VALID_AGGREGATION_TYPE + "\", " + "\"is_visible\":\""
                    + VALID_IS_VISIBLE + "\", " + "\"input_metrics\":[]}";

    private final Codec codec = new Codec(Validation.buildDefaultValidatorFactory().getValidator());

    @Test
    public void testValidKPIDef() throws JsonProcessingException {
        final KPIDefinition expectedKpiDef = new KPIDefinition(VALID_NAME, DESCRIPTION, VALID_DISPLAY_NAME, VALID_EXPRESSION, VALID_AGGREGATION_TYPE,
                null,
                VALID_IS_VISIBLE, VALID_LIST_INPUT_METRICS);
        final KPIDefinition kpiDef = this.codec.withValidation().readValue(VALID_KPI_DEF, KPIDefinition.class);

        assertNotNull(kpiDef);
        assertEquals(expectedKpiDef, kpiDef);
    }

    @Test
    public void testValidKPIDefWithAggPeriod() throws JsonProcessingException {
        final KPIDefinition expectedKpiDef = new KPIDefinition(VALID_NAME, DESCRIPTION, VALID_DISPLAY_NAME, VALID_EXPRESSION, VALID_AGGREGATION_TYPE,
                15,
                VALID_IS_VISIBLE, VALID_LIST_INPUT_METRICS);
        final KPIDefinition kpiDef = this.codec.withValidation().readValue(VALID_KPI_DEF_WITH_AGG, KPIDefinition.class);

        assertNotNull(kpiDef);
        assertEquals(expectedKpiDef, kpiDef);
    }

    @Test
    public void testValidKPIDefWithEmptyDescription() throws JsonProcessingException {
        final KPIDefinition expectedKpiDef = new KPIDefinition(VALID_NAME, "", VALID_DISPLAY_NAME, VALID_EXPRESSION, VALID_AGGREGATION_TYPE, null,
                VALID_IS_VISIBLE, VALID_LIST_INPUT_METRICS);

        final KPIDefinition kpiDef = this.codec.withValidation().readValue(KPI_DEF_WITH_EMPTY_DESCRIPTION, KPIDefinition.class);

        assertNotNull(kpiDef);
        assertEquals(this.codec.writeValueAsStringPretty(expectedKpiDef), this.codec.writeValueAsStringPretty(kpiDef));
        assertEquals(expectedKpiDef.getInputMetrics(), kpiDef.getInputMetrics());
    }

    @Test
    public void testValidKPIDefWithNoDescription() throws JsonProcessingException {
        final KPIDefinition expectedKpiDef = new KPIDefinition(VALID_NAME, null, VALID_DISPLAY_NAME, VALID_EXPRESSION, VALID_AGGREGATION_TYPE, null,
                VALID_IS_VISIBLE, VALID_LIST_INPUT_METRICS);

        final KPIDefinition kpiDef = this.codec.withValidation().readValue(KPI_DEF_WITH_NO_DISCRIPTION, KPIDefinition.class);

        assertNotNull(kpiDef);
        assertEquals(this.codec.writeValueAsStringPretty(expectedKpiDef), this.codec.writeValueAsStringPretty(kpiDef));
        assertEquals(expectedKpiDef.getInputMetrics(), kpiDef.getInputMetrics());
    }

    @Test
    public void testValidKPIDefWithEmptyDisplayName() throws JsonProcessingException {
        final KPIDefinition expectedKpiDef = new KPIDefinition(VALID_NAME, DESCRIPTION, "", VALID_EXPRESSION, VALID_AGGREGATION_TYPE, null,
                VALID_IS_VISIBLE, VALID_LIST_INPUT_METRICS);

        final KPIDefinition kpiDef = this.codec.withValidation().readValue(KPI_DEF_WITH_EMPTY_DISPLAY_NAME, KPIDefinition.class);

        assertNotNull(kpiDef);
        assertEquals(this.codec.writeValueAsStringPretty(expectedKpiDef), this.codec.writeValueAsStringPretty(kpiDef));
        assertEquals(expectedKpiDef.getInputMetrics(), kpiDef.getInputMetrics());
    }

    @Test
    public void testValidKPIDefWithNoDisplayName() throws JsonProcessingException {
        final KPIDefinition expectedKpiDef = new KPIDefinition(VALID_NAME, DESCRIPTION, null, VALID_EXPRESSION, VALID_AGGREGATION_TYPE, null,
                VALID_IS_VISIBLE, VALID_LIST_INPUT_METRICS);

        final KPIDefinition kpiDef = this.codec.withValidation().readValue(KPI_DEF_WITH_NO_DISPLAY_NAME, KPIDefinition.class);

        assertNotNull(kpiDef);
        assertEquals(this.codec.writeValueAsStringPretty(expectedKpiDef), this.codec.writeValueAsStringPretty(kpiDef));
        assertEquals(expectedKpiDef.getInputMetrics(), kpiDef.getInputMetrics());
    }

    @Test
    public void testValidKPIDefWithNoIsVisible() throws JsonProcessingException {
        final KPIDefinition expectedKpiDef = new KPIDefinition(VALID_NAME, DESCRIPTION, VALID_DISPLAY_NAME, VALID_EXPRESSION, VALID_AGGREGATION_TYPE,
                null,
                VALID_IS_VISIBLE, VALID_LIST_INPUT_METRICS);
        final KPIDefinition kpiDef = this.codec.withValidation().readValue(KPI_DEF_WITH_NO_IS_VISIBLE, KPIDefinition.class);

        assertNotNull(kpiDef);
        assertEquals(this.codec.writeValueAsStringPretty(expectedKpiDef), this.codec.writeValueAsStringPretty(kpiDef));
        assertEquals(expectedKpiDef, kpiDef);
    }

    @Test
    public void testKPIDefWithInvalidName() {
        assertThrows(ConstraintViolationException.class, () -> this.codec.withValidation().readValue(KPI_DEF_WITH_INVALID_NAME, KPIDefinition.class));
    }

    @Test
    public void testKPIDefWithEmptyName() {
        assertThrows(ConstraintViolationException.class, () -> this.codec.withValidation().readValue(KPI_DEF_WITH_EMPTY_NAME, KPIDefinition.class));
    }

    @Test
    public void testKPIDefWithEmptyExpression() {
        assertThrows(ConstraintViolationException.class,
                () -> this.codec.withValidation().readValue(KPI_DEF_WITH_EMPTY_EXPRESSION, KPIDefinition.class));
    }

    @Test
    public void testKPIDefWithEmptyAggregationType() {
        assertThrows(ConstraintViolationException.class,
                () -> this.codec.withValidation().readValue(KPI_DEF_WITH_EMPTY_AGGRERATION_TYPE, KPIDefinition.class));
    }

    @Test
    public void testKPIDefWithInvaidInputMetric() {
        assertThrows(ConstraintViolationException.class,
                () -> this.codec.withValidation().readValue(KPI_DEF_WITH_INVALID_INPUT_METRIC, KPIDefinition.class));
    }

    @Test
    public void testKPIDefWithEmptyInputMetric() {
        assertThrows(ConstraintViolationException.class,
                () -> this.codec.withValidation().readValue(KPI_DEF_WITH_EMPTY_INPUT_METRIC, KPIDefinition.class));
    }

    @Test
    public void testKpiDefinitionNameValidation() throws Exception {

        final List<String> validNames = List.of("abc", "ab_c", "abc_", "1bc", "1b_c", "ab1", "ab_1", "1b1", "1b_1");

        final List<String> invalidNames = List.of("_abc", "ab/c", "ab?c", "ab-c", "ab\\c", "ab:c", "ab*c");

        final KPIDefinition.KPIDefinitionBuilder builder = KPIDefinition.builder()
                .inputMetrics(VALID_LIST_INPUT_METRICS)
                .expression(VALID_EXPRESSION)
                .aggregationType(VALID_AGGREGATION_TYPE);

        final Validator validator = new ValidationConfiguration().getValidator();
        for (final String name : validNames) {
            final KPIDefinition kpi = builder.name(name).build();
            assertTrue(validator.validate(kpi).isEmpty());
        }

        for (final String name : invalidNames) {
            final KPIDefinition kpi = builder.name(name).build();
            assertFalse(validator.validate(kpi).isEmpty());
        }
    }

    @Test
    public void testInputMetricOrder_builder() throws Exception {

        final InputMetric inp1 = InputMetric.builder()
                .id("inp_1")
                .alias("p0")
                .type(InputMetric.Type.PM_DATA)
                .build();

        final InputMetric inp2 = InputMetric.builder()
                .id("inp_2")
                .alias("p1")
                .type(InputMetric.Type.PM_DATA)
                .build();

        assertNotEquals(inp1, inp2);

        final List<InputMetric> inputMetrics = List.of(inp2, inp1);

        final KPIDefinition actual = KPIDefinition.builder()
                .name("kpi")
                .inputMetrics(inputMetrics)
                .build();

        assertEquals(inp1, actual.getInputMetrics().get(0));
        assertEquals(inp2, actual.getInputMetrics().get(1));
    }
}