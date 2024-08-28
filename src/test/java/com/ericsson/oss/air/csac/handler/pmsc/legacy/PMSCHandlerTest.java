/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.handler.pmsc.legacy;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static com.ericsson.oss.air.csac.model.TestResourcesUtils.DEPLOYED_COMPLEX_KPI_OBJ;
import static com.ericsson.oss.air.csac.model.TestResourcesUtils.DEPLOYED_SIMPLE_KPI_OBJ;
import static com.ericsson.oss.air.csac.model.TestResourcesUtils.VALID_PROFILE_DEF_OBJ;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import com.ericsson.oss.air.csac.model.ProfileDefinition;
import com.ericsson.oss.air.csac.model.pmsc.KpiDefinitionDTOWithRelationship;
import com.ericsson.oss.air.csac.service.KPICalculator;
import com.ericsson.oss.air.util.codec.Codec;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.exc.InputCoercionException;

@SpringBootTest
@ActiveProfiles("test")
class PMSCHandlerTest {

    @MockBean
    private Provisioner provisioner;

    @MockBean
    private KPICalculator kpiCalculator;

    @MockBean
    private Codec codec;

    private PMSCHandler pmscHandler;

    @BeforeEach
    void setUp() {
        this.pmscHandler = new PMSCHandler(this.kpiCalculator, this.provisioner, this.codec);
    }

    @Test
    void submit_emptyData() {

        final List<ProfileDefinition> defs = new ArrayList<>();
        this.pmscHandler.submit(defs);

        verify(this.provisioner, times(0)).provision(any(), any());
        verify(this.kpiCalculator, times(0)).calculateAffectedKPIs(any());
    }

    @Test
    void submit_validSimpleKPI() {
        when(this.kpiCalculator.calculateAffectedKPIs(any())).thenReturn(List.of(
                new KpiDefinitionDTOWithRelationship(DEPLOYED_SIMPLE_KPI_OBJ, "name", VALID_PROFILE_DEF_OBJ)));
        final List<ProfileDefinition> defs = List.of(VALID_PROFILE_DEF_OBJ);

        this.pmscHandler.submit(defs);

        verify(this.provisioner, times(1)).provision(any(), any());
        verify(this.kpiCalculator, times(1)).calculateAffectedKPIs(any());
    }

    @Test
    void submit_validComplexKPI() {
        when(this.kpiCalculator.calculateAffectedKPIs(any())).thenReturn(List.of(
                new KpiDefinitionDTOWithRelationship(DEPLOYED_COMPLEX_KPI_OBJ, "name", VALID_PROFILE_DEF_OBJ)));
        final List<ProfileDefinition> defs = List.of(VALID_PROFILE_DEF_OBJ);

        this.pmscHandler.submit(defs);

        verify(this.provisioner, times(1)).provision(any(), any());
        verify(this.kpiCalculator, times(1)).calculateAffectedKPIs(any());
    }

    @Test
    void submit_serializeException() throws JsonProcessingException {

        when(this.codec.writeValueAsStringPretty(any())).thenThrow(new InputCoercionException(null, "my mock exception", null, null));
        when(this.kpiCalculator.calculateAffectedKPIs(any())).thenReturn(new ArrayList<>());
        final List<ProfileDefinition> defs = List.of(ProfileDefinition.builder().build());
        this.pmscHandler.submit(defs);

        verify(this.provisioner, times(1)).provision(any(), any());
        verify(this.kpiCalculator, times(1)).calculateAffectedKPIs(any());
    }
}