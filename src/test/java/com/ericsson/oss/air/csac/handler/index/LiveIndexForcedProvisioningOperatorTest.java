/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.handler.index;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ericsson.oss.air.csac.model.runtime.RuntimeKpiInstance;
import com.ericsson.oss.air.csac.service.index.IndexerProvisioningService;

@ExtendWith(MockitoExtension.class)
class LiveIndexForcedProvisioningOperatorTest {

    @Mock
    private LiveIndexProvisioningHandler indexProvisioningHandler;

    @Mock
    private IndexerProvisioningService indexerService;

    @InjectMocks
    private LiveIndexForcedProvisioningOperator testOperator;

    @Test
    void doApply() {

        Map<String, List<RuntimeKpiInstance>> writerData = new HashMap<>();
        writerData.put("writer", List.of());

        when(this.indexProvisioningHandler.getWriterData()).thenReturn(writerData);
        this.testOperator.apply(null);

        verify(this.indexProvisioningHandler, times(1)).getWriterData();
        verify(this.indexProvisioningHandler, times(1)).getDefaultIndexDefinition(any());
        verify(this.indexerService, times(1)).create(any());
    }

    @Test
    void doApply_noKpis() {

        Map<String, List<RuntimeKpiInstance>> writerData = new HashMap<>();

        when(this.indexProvisioningHandler.getWriterData()).thenReturn(writerData);
        this.testOperator.apply(null);

        verify(this.indexProvisioningHandler, times(1)).getWriterData();
        verify(this.indexProvisioningHandler, times(0)).getDefaultIndexDefinition(any());
        verify(this.indexerService, times(0)).create(any());
    }
}