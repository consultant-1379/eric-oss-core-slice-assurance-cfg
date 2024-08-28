/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.ericsson.oss.air.csac.handler.augmentation.AugmentationHandler;
import com.ericsson.oss.air.csac.handler.index.IndexProvisioningHandler;
import com.ericsson.oss.air.csac.handler.kpi.KpiProvisioningHandler;
import com.ericsson.oss.air.csac.model.ProfileDefinition;
import com.ericsson.oss.air.util.concurrent.AtomicDouble;

@ExtendWith(MockitoExtension.class)
class ServiceUpdateHandlerTest {

    private final List<ProfileDefinition> EMPTY_PROFILE_DEFINITION = new ArrayList<>();

    @Mock
    private AugmentationHandler augmentationHandler;

    @Mock
    private KpiProvisioningHandler pmscHandler;

    @Mock
    private IndexProvisioningHandler indexHandler;

    @Mock
    private AtomicDouble aasElapsedTime;

    @Mock
    private AtomicDouble pmscElapsedTime;

    @Mock
    private AtomicDouble kpiElapsedTime;

    @Mock
    private AtomicDouble indexElapsedTime;

    @Mock
    private AtomicDouble totalElapsedTime;

    @InjectMocks
    private ServiceUpdateHandler serviceUpdateHandler;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(serviceUpdateHandler, "provisioningAasTime", this.aasElapsedTime);
        ReflectionTestUtils.setField(serviceUpdateHandler, "provisioningPmscTime", this.pmscElapsedTime);
        ReflectionTestUtils.setField(serviceUpdateHandler, "provisioningKpiTime", this.kpiElapsedTime);
        ReflectionTestUtils.setField(serviceUpdateHandler, "provisioningIndexTime", this.indexElapsedTime);
        ReflectionTestUtils.setField(serviceUpdateHandler, "provisioningTotalTime", this.totalElapsedTime);
    }

    @Test
    void testNotify_success() {
        this.serviceUpdateHandler.notify(this.EMPTY_PROFILE_DEFINITION, Instant.now());
        verify(this.augmentationHandler, times(1)).submit(this.EMPTY_PROFILE_DEFINITION);
        verify(this.pmscHandler, times(1)).apply(this.EMPTY_PROFILE_DEFINITION);
        verify(this.indexHandler, times(1)).apply(this.EMPTY_PROFILE_DEFINITION);

        verify(this.aasElapsedTime, times(2)).set(anyDouble());
        verify(this.pmscElapsedTime, times(2)).set(anyDouble());
        verify(this.kpiElapsedTime, times(2)).set(anyDouble());
        verify(this.indexElapsedTime, times(2)).set(anyDouble());
        verify(this.totalElapsedTime, times(2)).set(anyDouble());
    }

    @Test
    void testNotify_success_valid_timeElapsed() {
        final Instant now = Instant.now();
        final Instant olderTime = now.minus(2, ChronoUnit.SECONDS);

        final AtomicDouble aasTimer = new AtomicDouble();
        final AtomicDouble pmscTimer = new AtomicDouble();
        final AtomicDouble kpiTimer = new AtomicDouble();
        final AtomicDouble indexTimer = new AtomicDouble();
        final AtomicDouble totalTimer = new AtomicDouble();

        ReflectionTestUtils.setField(serviceUpdateHandler, "provisioningAasTime", aasTimer);
        ReflectionTestUtils.setField(serviceUpdateHandler, "provisioningPmscTime", pmscTimer);
        ReflectionTestUtils.setField(serviceUpdateHandler, "provisioningKpiTime", kpiTimer);
        ReflectionTestUtils.setField(serviceUpdateHandler, "provisioningIndexTime", indexTimer);
        ReflectionTestUtils.setField(serviceUpdateHandler, "provisioningTotalTime", totalTimer);

        this.serviceUpdateHandler.notify(this.EMPTY_PROFILE_DEFINITION, olderTime);

        assertTrue(aasTimer.get() >= 2.0);
        assertTrue(pmscTimer.get() >= 2.0);
        assertTrue(kpiTimer.get() >= 2.0);
        assertTrue(indexTimer.get() >= 2.0);
        assertTrue(totalTimer.get() >= 2.0);
    }

    @Test
    void testNotify_pmsc_exception() {
        doThrow(RuntimeException.class).when(this.pmscHandler).apply(this.EMPTY_PROFILE_DEFINITION);

        assertThrows(RuntimeException.class,
                     () -> this.serviceUpdateHandler.notify(this.EMPTY_PROFILE_DEFINITION, Instant.now()));

        verify(this.aasElapsedTime, times(2)).set(anyDouble());
        verify(this.pmscElapsedTime, times(2)).set(anyDouble());
        verify(this.kpiElapsedTime, times(2)).set(anyDouble());
        verify(this.indexElapsedTime, times(1)).set(anyDouble());
        verify(this.totalElapsedTime, times(2)).set(anyDouble());
    }

    @Test
    void testNotify_pmsc_exceptionPassThrough() {
        doThrow(new RuntimeException()).when(this.pmscHandler).apply(this.EMPTY_PROFILE_DEFINITION);
        assertThrows(RuntimeException.class, () -> {
            this.serviceUpdateHandler.notify(new ArrayList<>(), Instant.now());
        });

        verify(this.aasElapsedTime, times(2)).set(anyDouble());
        verify(this.pmscElapsedTime, times(2)).set(anyDouble());
        verify(this.kpiElapsedTime, times(2)).set(anyDouble());
        verify(this.indexElapsedTime, times(1)).set(anyDouble());
        verify(this.totalElapsedTime, times(2)).set(anyDouble());
    }

    @Test
    void testNotify_pmsc_exception_valid_timeElapsed() {
        final Instant now = Instant.now();
        final Instant olderTime = now.minus(2, ChronoUnit.SECONDS);

        final AtomicDouble aasTimer = new AtomicDouble();
        final AtomicDouble pmscTimer = new AtomicDouble();
        final AtomicDouble kpiTimer = new AtomicDouble();
        final AtomicDouble indexTimer = new AtomicDouble();
        final AtomicDouble totalTimer = new AtomicDouble();

        ReflectionTestUtils.setField(serviceUpdateHandler, "provisioningAasTime", aasTimer);
        ReflectionTestUtils.setField(serviceUpdateHandler, "provisioningPmscTime", pmscTimer);
        ReflectionTestUtils.setField(serviceUpdateHandler, "provisioningKpiTime", kpiTimer);
        ReflectionTestUtils.setField(serviceUpdateHandler, "provisioningIndexTime", indexTimer);
        ReflectionTestUtils.setField(serviceUpdateHandler, "provisioningTotalTime", totalTimer);

        doThrow(new RuntimeException()).when(this.pmscHandler).apply(this.EMPTY_PROFILE_DEFINITION);

        assertThrows(RuntimeException.class, () -> this.serviceUpdateHandler.notify(this.EMPTY_PROFILE_DEFINITION, olderTime));

        assertTrue(aasTimer.get() >= 2.0);
        assertTrue(pmscTimer.get() >= 2.0);
        assertTrue(kpiTimer.get() >= 2.0);
        assertEquals(0.0, indexTimer.get());
        assertTrue(totalTimer.get() >= 2.0);
    }

    @Test
    void testNotify_aas_exception() {
        doThrow(RuntimeException.class).when(this.augmentationHandler).submit(this.EMPTY_PROFILE_DEFINITION);

        assertThrows(RuntimeException.class, () -> this.serviceUpdateHandler.notify(this.EMPTY_PROFILE_DEFINITION, Instant.now()));

        verify(this.aasElapsedTime, times(2)).set(anyDouble());
        verify(this.pmscElapsedTime, times(1)).set(anyDouble());
        verify(this.kpiElapsedTime, times(1)).set(anyDouble());
        verify(this.indexElapsedTime, times(1)).set(anyDouble());
        verify(this.totalElapsedTime, times(2)).set(anyDouble());
    }

    @Test
    void testNotify_aas_exception_valid_timeElapsed() {
        final Instant now = Instant.now();
        final Instant olderTime = now.minus(2, ChronoUnit.SECONDS);

        final AtomicDouble aasTimer = new AtomicDouble();
        final AtomicDouble pmscTimer = new AtomicDouble();
        final AtomicDouble kpiTimer = new AtomicDouble();
        final AtomicDouble indexTimer = new AtomicDouble();
        final AtomicDouble totalTimer = new AtomicDouble();

        ReflectionTestUtils.setField(serviceUpdateHandler, "provisioningAasTime", aasTimer);
        ReflectionTestUtils.setField(serviceUpdateHandler, "provisioningPmscTime", pmscTimer);
        ReflectionTestUtils.setField(serviceUpdateHandler, "provisioningKpiTime", kpiTimer);
        ReflectionTestUtils.setField(serviceUpdateHandler, "provisioningIndexTime", indexTimer);
        ReflectionTestUtils.setField(serviceUpdateHandler, "provisioningTotalTime", totalTimer);

        doThrow(RuntimeException.class).when(this.augmentationHandler).submit(this.EMPTY_PROFILE_DEFINITION);

        assertThrows(RuntimeException.class, () -> this.serviceUpdateHandler.notify(this.EMPTY_PROFILE_DEFINITION, olderTime));

        assertTrue(aasTimer.get() >= 2.0);
        assertEquals(0.0, pmscTimer.get());
        assertEquals(0.0, kpiTimer.get());
        assertEquals(0.0, indexTimer.get());
        assertTrue(totalTimer.get() >= 2.0);
    }

    @Test
    void testNotify_index_exception() {
        doThrow(RuntimeException.class).when(this.indexHandler).apply(this.EMPTY_PROFILE_DEFINITION);

        assertThrows(RuntimeException.class, () -> this.serviceUpdateHandler.notify(this.EMPTY_PROFILE_DEFINITION, Instant.now()));

        verify(this.aasElapsedTime, times(2)).set(anyDouble());
        verify(this.pmscElapsedTime, times(2)).set(anyDouble());
        verify(this.kpiElapsedTime, times(2)).set(anyDouble());
        verify(this.indexElapsedTime, times(2)).set(anyDouble());
        verify(this.totalElapsedTime, times(2)).set(anyDouble());
    }

    @Test
    void testNotify_index_exception_valid_timeElapsed() {
        final Instant now = Instant.now();
        final Instant olderTime = now.minus(2, ChronoUnit.SECONDS);

        final AtomicDouble aasTimer = new AtomicDouble();
        final AtomicDouble pmscTimer = new AtomicDouble();
        final AtomicDouble kpiTimer = new AtomicDouble();
        final AtomicDouble indexTimer = new AtomicDouble();
        final AtomicDouble totalTimer = new AtomicDouble();

        ReflectionTestUtils.setField(serviceUpdateHandler, "provisioningAasTime", aasTimer);
        ReflectionTestUtils.setField(serviceUpdateHandler, "provisioningPmscTime", pmscTimer);
        ReflectionTestUtils.setField(serviceUpdateHandler, "provisioningKpiTime", kpiTimer);
        ReflectionTestUtils.setField(serviceUpdateHandler, "provisioningIndexTime", indexTimer);
        ReflectionTestUtils.setField(serviceUpdateHandler, "provisioningTotalTime", totalTimer);

        doThrow(RuntimeException.class).when(this.indexHandler).apply(this.EMPTY_PROFILE_DEFINITION);

        assertThrows(RuntimeException.class, () -> this.serviceUpdateHandler.notify(this.EMPTY_PROFILE_DEFINITION, olderTime));

        assertTrue(aasTimer.get() >= 2.0);
        assertTrue(pmscTimer.get() >= 2.0);
        assertTrue(kpiTimer.get() >= 2.0);
        assertTrue(indexTimer.get() >= 2.0);
        assertTrue(totalTimer.get() >= 2.0);
    }
}
