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

import java.util.List;

import jakarta.validation.Validation;

import com.ericsson.oss.air.util.codec.Codec;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;

class KpiCalculationDTOTest {

    private static final String SOURCE = "source";
    private static final String KPI_NAMES = "kpi_names";
    private static final String PARAMETERS = "parameters";

    private static final String PARAM_EXECUTION_ID = "param.execution_id";
    private static final String PARAM_DATE_FOR_FILTER = "param.date_for_filter";

    private static final String PARAMETER = "{\"" + PARAM_EXECUTION_ID + "\":\"id\",\"" + PARAM_DATE_FOR_FILTER + "\":\"filter\"}";
    private static final String KPI_CALCULATION_DTO =
            "{\"" + SOURCE + "\":\"source\"," + "\"" + KPI_NAMES + "\":[\"kpi names\"],\"" + PARAMETERS + "\":" + PARAMETER + "}";
    private static final ParameterDTO PARAMETER_DTO = new ParameterDTO("id", "filter");
    private Codec codec = new Codec(Validation.buildDefaultValidatorFactory().getValidator());

    @Test
    public void testKpiCalculationDTO() throws JsonProcessingException {
        final KpiCalculationDTO expectedKpiCalDTO = new KpiCalculationDTO("source", List.of("kpi names"), PARAMETER_DTO);
        final KpiCalculationDTO kpiCalDTO = this.codec.withValidation().readValue(KPI_CALCULATION_DTO, KpiCalculationDTO.class);

        assertNotNull(kpiCalDTO);
        assertEquals(expectedKpiCalDTO, kpiCalDTO);
    }
}