/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.handler.victoriametrics;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ericsson.oss.air.csac.handler.event.ConsistencyCheckHandler;
import com.ericsson.oss.air.csac.repository.DeployedProfileDAO;
import com.ericsson.oss.air.csac.repository.cache.ResolvedKpiCache;
import com.ericsson.oss.air.csac.service.KPICalculator;
import com.ericsson.oss.air.exception.CsacConsistencyCheckException;

@ExtendWith(MockitoExtension.class)
class VmKpiProvisioningHandlerTest {

    @Mock
    private KPICalculator kpiCalculator;

    @Mock
    private DeployedProfileDAO deployedProfileDAO;

    @Mock
    private ResolvedKpiCache resolvedKpiCache;

    @Mock
    private ConsistencyCheckHandler consistencyCheckHandler;

    @InjectMocks
    private VmKpiProvisioningHandler vmKpiProvisioningHandler;

    @Test
    void doApply() {

        when(this.kpiCalculator.calculateAffectedKPIs(anyList())).thenReturn(Collections.emptyList());

        assertDoesNotThrow(() -> this.vmKpiProvisioningHandler.doApply(Collections.emptyList()));

        verify(this.kpiCalculator, times(1)).calculateAffectedKPIs(anyList());
        verify(this.resolvedKpiCache, times(1)).flush();
        verify(this.deployedProfileDAO, times(1)).insertProfileDefinitions(anyList());
        verify(this.consistencyCheckHandler, times(0)).notifyCheckFailure(any());
    }

    @Test
    void doApply_consistencyCheckException() throws Exception {

        doThrow(new CsacConsistencyCheckException(new RuntimeException())).when(this.deployedProfileDAO).insertProfileDefinitions(anyList());

        assertThrows(CsacConsistencyCheckException.class, () -> this.vmKpiProvisioningHandler.doApply(Collections.emptyList()));
    }
}