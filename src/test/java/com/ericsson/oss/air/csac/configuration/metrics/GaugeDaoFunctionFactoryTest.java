/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.configuration.metrics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import com.ericsson.oss.air.csac.repository.PMDefinitionDAO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GaugeDaoFunctionFactoryTest {

    private GaugeDaoFunctionFactory gaugeDaoFunctionFactory = new GaugeDaoFunctionFactory();

    @Mock
    private PMDefinitionDAO pmDefinitionDAO;

    private GaugeDaoFunctionFactory.GaugeDaoFunction<PMDefinitionDAO> gaugeDaoFunction;

    @BeforeEach
    void setUp() {
        this.gaugeDaoFunction = this.gaugeDaoFunctionFactory.createGaugeDaoFunction(this.pmDefinitionDAO::totalPMDefinitions);
    }

    @Test
    void createGaugeDaoFunction_DaoFunctionReturnsValidValue() {

        final Double validValue = 2.0;
        when(this.pmDefinitionDAO.totalPMDefinitions()).thenReturn(validValue.intValue());

        assertEquals(validValue, this.gaugeDaoFunction.applyAsDouble(this.pmDefinitionDAO));
    }

    @Test
    void createGaugeDaoFunction_DaoFunctionThrowsException_ReturnsInitValue() {

        when(this.pmDefinitionDAO.totalPMDefinitions()).thenThrow(RuntimeException.class);

        assertEquals(0, this.gaugeDaoFunction.applyAsDouble(this.pmDefinitionDAO));
    }

    @Test
    void createGaugeDaoFunction_DaoFunctionReturnsNaN_ReturnsInitValue() {

        final GaugeDaoFunctionFactory.GaugeDaoFunction<PMDefinitionDAO> function = this.gaugeDaoFunctionFactory.createGaugeDaoFunction(
                () -> Double.NaN);

        assertEquals(0, function.applyAsDouble(this.pmDefinitionDAO));
    }
}