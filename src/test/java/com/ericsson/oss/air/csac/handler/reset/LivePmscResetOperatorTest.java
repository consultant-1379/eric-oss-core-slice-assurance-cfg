/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.handler.reset;

import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.concurrent.atomic.AtomicLong;

import com.ericsson.oss.air.csac.repository.cache.ResolvedKpiCache;
import com.ericsson.oss.air.csac.service.kpi.pmsc.PmscRestClient;
import com.ericsson.oss.air.util.concurrent.AtomicDouble;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
public class LivePmscResetOperatorTest {

    @Mock
    private PmscRestClient pmscRestClient;

    @Mock
    private ResolvedKpiCache resolvedKpiCache;

    @Spy
    private AtomicLong errorCounter;

    @Spy
    private AtomicDouble elapsedTime;

    @InjectMocks
    private LivePmscResetOperator livePmscResetOperator;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(livePmscResetOperator, "elapsedTime", this.elapsedTime);
        ReflectionTestUtils.setField(livePmscResetOperator, "errorCounter", this.errorCounter);
    }

    @Test
    void apply() {

        this.livePmscResetOperator.apply();

        verify(this.pmscRestClient, times(1)).deleteAll();
        verify(this.resolvedKpiCache, times(1)).deleteAll();
        verify(this.elapsedTime, times(1)).set(anyDouble());
        verify(this.errorCounter, times(0)).incrementAndGet();
    }

    @Test
    void apply_pmsc_error() {

        doThrow(RuntimeException.class).when(this.pmscRestClient).deleteAll();

        assertThrows(RuntimeException.class, () -> this.livePmscResetOperator.apply());

        verify(this.pmscRestClient, times(1)).deleteAll();
        verify(this.resolvedKpiCache, times(0)).deleteAll();
        verify(this.elapsedTime, times(1)).set(anyDouble());
        verify(this.errorCounter, times(1)).incrementAndGet();

    }

    @Test
    void apply_kpiCache_error() {

        doThrow(RuntimeException.class).when(this.resolvedKpiCache).deleteAll();

        assertThrows(RuntimeException.class, () -> this.livePmscResetOperator.apply());

        verify(this.pmscRestClient, times(1)).deleteAll();
        verify(this.resolvedKpiCache, times(1)).deleteAll();
        verify(this.elapsedTime, times(1)).set(anyDouble());
        verify(this.errorCounter, times(1)).incrementAndGet();

    }
}


