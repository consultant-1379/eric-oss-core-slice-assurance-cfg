/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.handler.reset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.concurrent.atomic.AtomicLong;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.ericsson.oss.air.csac.handler.status.ProvisioningTracker;
import com.ericsson.oss.air.csac.handler.util.VoidOperator;
import com.ericsson.oss.air.util.concurrent.AtomicDouble;
import com.ericsson.oss.air.util.logging.FaultHandler;

@ExtendWith(MockitoExtension.class)
class ResetConfigurationHandlerTest {

    @Spy
    private AtomicDouble configurationResetTotalTime;

    @Spy
    private AtomicLong configurationResetTotalErrors;

    @Spy
    private AtomicLong configurationResetAugErrorCounter;

    @Spy
    private AtomicLong configurationResetKpiErrorCounter;

    @Spy
    private AtomicLong configurationResetDbErrorCounter;

    @Spy
    private AtomicLong configurationResetIndexErrorCounter;

    @Mock
    private FaultHandler faultHandler;

    @Mock
    private AugmentationResetOperator augmentationResetOperator;

    @Mock
    private KpiConfigurationResetOperator kpiConfigurationResetOperator;

    @Mock
    private IndexerConfigurationResetOperator indexerConfigurationResetOperator;

    @Mock
    private DbResetOperator dbResetOperator;

    @Mock
    private ProvisioningTracker provisioningTracker;

    @InjectMocks
    private ResetConfigurationHandler resetConfigurationHandler;

    @BeforeEach
    void setUp() {

        ReflectionTestUtils.setField(this.resetConfigurationHandler, "configurationResetTotalTime", this.configurationResetTotalTime);
        ReflectionTestUtils.setField(this.resetConfigurationHandler, "configurationResetTotalErrors", this.configurationResetTotalErrors);
        ReflectionTestUtils.setField(this.resetConfigurationHandler, "configurationResetAugErrorCounter", this.configurationResetAugErrorCounter);
        ReflectionTestUtils.setField(this.resetConfigurationHandler, "configurationResetKpiErrorCounter", this.configurationResetKpiErrorCounter);
        ReflectionTestUtils.setField(this.resetConfigurationHandler, "configurationResetDbErrorCounter", this.configurationResetDbErrorCounter);
        ReflectionTestUtils.setField(this.resetConfigurationHandler, "configurationResetIndexErrorCounter", this.configurationResetIndexErrorCounter);

        lenient().when(this.augmentationResetOperator.andThen(any(VoidOperator.class))).thenReturn(this.indexerConfigurationResetOperator);
        lenient().when(this.indexerConfigurationResetOperator.andThen(any(VoidOperator.class))).thenReturn(this.dbResetOperator);
    }

    @Test
    void apply() {

        this.resetConfigurationHandler.apply();

        verify(this.dbResetOperator, times(1)).apply();
        verify(this.configurationResetTotalErrors, times(1)).set(anyLong());
        verify(this.configurationResetTotalTime, times(1)).set(anyDouble());
        verify(this.configurationResetAugErrorCounter, times(1)).get();
        verify(this.configurationResetDbErrorCounter, times(1)).get();
        verify(this.configurationResetKpiErrorCounter, times(1)).get();
        verify(this.configurationResetIndexErrorCounter, times(1)).get();
    }

    @Test
    void apply_error() {

        doThrow(RuntimeException.class).when(this.dbResetOperator).apply();

        assertThrows(RuntimeException.class, () -> this.resetConfigurationHandler.apply());

        verify(this.faultHandler, times(1)).error(eq("Unable to complete reset operation: "), any(RuntimeException.class));
    }

    @Test
    void getKpiResetOperator() {

        assertEquals(this.kpiConfigurationResetOperator, this.resetConfigurationHandler.getKpiResetOperator(true));
        assertNotNull(this.resetConfigurationHandler.getKpiResetOperator(false));
    }
}