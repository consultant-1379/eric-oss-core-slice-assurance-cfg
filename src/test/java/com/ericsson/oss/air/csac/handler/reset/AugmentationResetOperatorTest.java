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

import com.ericsson.oss.air.csac.service.augmentation.AugmentationProvisioningService;
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
class AugmentationResetOperatorTest {

    @Spy
    private AtomicLong errorCountMetric;

    @Spy
    private AtomicDouble elapsedTime;

    @Mock
    private AugmentationProvisioningService augService;

    @InjectMocks
    private AugmentationResetOperator resetOperator;

    @BeforeEach
    void setUp() {

        ReflectionTestUtils.setField(resetOperator, "elapsedTime", this.elapsedTime);
        ReflectionTestUtils.setField(resetOperator, "errorCountMetric", this.errorCountMetric);
    }

    @Test
    void apply() {

        this.resetOperator.apply();

        verify(this.augService, times(1)).deleteAll();
        verify(this.elapsedTime, times(1)).set(anyDouble());
        verify(this.errorCountMetric, times(0)).incrementAndGet();
    }

    @Test
    void apply_error() throws Exception {

        doThrow(RuntimeException.class).when(this.augService).deleteAll();

        assertThrows(RuntimeException.class, () -> this.resetOperator.apply());

        verify(this.augService, times(1)).deleteAll();
        verify(this.elapsedTime, times(1)).set(anyDouble());
        verify(this.errorCountMetric, times(1)).incrementAndGet();

    }
}