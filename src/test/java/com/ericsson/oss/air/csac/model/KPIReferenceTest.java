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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;

import com.ericsson.oss.air.csac.model.pmsc.AggregationPeriod;
import com.ericsson.oss.air.util.codec.Codec;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;

class KPIReferenceTest {

    private static final String VALID_REF = "kpi_reference";
    private static final String EMPTY_REF = " ";

    private static final String VALID_KPI_REF = "{\"ref\":\"" + VALID_REF + "\"}";
    private static final String KPI_REF_EMPTY_REF = "{\"ref\":\"" + EMPTY_REF + "\"}";

    private final Codec codec = new Codec(Validation.buildDefaultValidatorFactory().getValidator());

    @Test
    public void testValidKPIRef() throws JsonProcessingException {
        final KPIReference expectedKpiRef = KPIReference.builder().ref(VALID_REF).build();
        final KPIReference kpiRef = this.codec.withValidation().readValue(VALID_KPI_REF, KPIReference.class);

        assertNotNull(kpiRef);
        assertEquals(expectedKpiRef, kpiRef);
    }

    @Test
    public void testKPIRefWithEmptyRef() {
        assertThrows(ConstraintViolationException.class, () -> this.codec.withValidation().readValue(KPI_REF_EMPTY_REF, KPIReference.class));
    }

    @Test
    public void testKpiReference_aggregationPeriodOverride() throws Exception {

        final String actualStr = "{\n" +
                "  \"ref\" : \"kpi1\",\n" +
                "  \"aggregation_period\" : 15\n" +
                "}";

        final KPIReference expected = KPIReference.builder()
                .aggregationPeriod(AggregationPeriod.FIFTEEN.getValue())
                .ref("kpi1")
                .build();

        final KPIReference actual = this.codec.withValidation().readValue(actualStr, KPIReference.class);

        assertEquals(expected, actual);
    }

    @Test
    public void testKpiReference_invalidAggregationPeriodOverride() throws Exception {

        final String actualStr = "{\n" +
                "  \"ref\" : \"kpi1\",\n" +
                "  \"aggregation_period\" : 5\n" +
                "}";

        final String expected = "aggregationPeriod: Must be one of [15, 60, 1440]";

        final Exception ex = assertThrows(ConstraintViolationException.class,
                () -> this.codec.withValidation().readValue(actualStr, KPIReference.class));

        assertEquals(expected, ex.getMessage());
    }

}