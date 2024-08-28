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

import static com.ericsson.oss.air.csac.model.TestResourcesUtils.*;
import static com.ericsson.oss.air.csac.model.pmsc.PMSCKpiUtil.CSAC_KPI_CAL_SOURCE;
import static com.ericsson.oss.air.csac.model.pmsc.PMSCKpiUtil.CSAC_PARAMETER_EXECUTION_ID;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import jakarta.validation.Validation;

import com.ericsson.oss.air.util.codec.Codec;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;

class PMSCKpiUtilTest {

    private final String SERIALIZED_KPI_CALCULATION =
            "{\"source\":\"CSAC_KPI_CAL_SOURCE\",\"kpi_names\":[\"csac_3d3b5e94_51c6_401d_b1fd_440361beb32c_simple_kpi\",\"csac_5601f9b5_afe8_4c49_b1e5_ae7f1d11537c_complex_kpi\"],\"parameters\":{\"param.execution_id\":\"CSAC_KPI_CAL_SOURCE_1\",\"param.date_for_filter\":\""
                    + java.time.LocalDate.now() + "\"}}";
    private final Codec codec = new Codec(Validation.buildDefaultValidatorFactory().getValidator());

    @Test
    void createKpiCalculation() throws JsonProcessingException {
        final KpiCalculationDTO kpis = PMSCKpiUtil.createKpiCalculation(
                List.of(DEPLOYED_SIMPLE_KPI_OBJ, DEPLOYED_COMPLEX_KPI_OBJ));

        assertEquals(CSAC_KPI_CAL_SOURCE, kpis.getSource());
        assertEquals(CSAC_PARAMETER_EXECUTION_ID, kpis.getParameter().getId());

        assertEquals(2, kpis.getKpiNames().size());
        assertEquals(DEPLOYED_SIMPLE_KPI_NAME, kpis.getKpiNames().get(0));
        assertEquals(DEPLOYED_COMPLEX_KPI_NAME, kpis.getKpiNames().get(1));

        assertEquals(this.SERIALIZED_KPI_CALCULATION, this.codec.withValidation().writeValueAsString(kpis));
    }

}
