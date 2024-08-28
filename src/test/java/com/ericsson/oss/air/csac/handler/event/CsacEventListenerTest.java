/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.handler.event;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class CsacEventListenerTest {

    @Mock
    private ApplicationContext applicationContext;

    @Mock
    private AtomicLong eventCountMetric;

    private final CsacEventListener csacEventListener = new CsacEventListener();

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(this.csacEventListener, "eventCountMetric", this.eventCountMetric);
    }

    @Test
    public void testConditionalOn_BeanCreation() {
        assertDoesNotThrow(() -> this.applicationContext.getBean(Executor.class));
    }

    @Test
    void testGetAsyncEventHandlerExecutor() {
        final ThreadPoolTaskExecutor executor = (ThreadPoolTaskExecutor) this.csacEventListener.getAsyncEventHandlerExecutor();

        assertEquals(100, executor.getMaxPoolSize());
        assertTrue(executor.getKeepAliveSeconds() > 0);
    }

    @Test
    void testOnConsistencyCheckEvent_OK() {
        final ConsistencyCheckEvent event = createConsistencyCheckEvent(ConsistencyCheckEvent.Payload.Type.OK, 0);

        this.csacEventListener.onConsistencyCheckEvent(event);
        verify(this.eventCountMetric, never()).addAndGet(any(Integer.class));
    }

    @Test
    void testOnConsistencyCheckEvent_SUSPECT() {
        final ConsistencyCheckEvent event = createConsistencyCheckEvent(ConsistencyCheckEvent.Payload.Type.SUSPECT, 1);

        this.csacEventListener.onConsistencyCheckEvent(event);
        verify(this.eventCountMetric).addAndGet(1L);
    }

    @Test
    void testOnConsistencyCheckEvent_FAILURE() {
        final ConsistencyCheckEvent event = createConsistencyCheckEvent(ConsistencyCheckEvent.Payload.Type.FAILURE, 4);

        this.csacEventListener.onConsistencyCheckEvent(event);
        verify(this.eventCountMetric).addAndGet(4L);
    }

    @Test
    void testOnConsistencyCheckEvent_CLEAR() {
        final ConsistencyCheckEvent event = createConsistencyCheckEvent(ConsistencyCheckEvent.Payload.Type.CLEAR, 0);

        this.csacEventListener.onConsistencyCheckEvent(event);
        verify(this.eventCountMetric).set(0L);
    }

    private ConsistencyCheckEvent createConsistencyCheckEvent(final ConsistencyCheckEvent.Payload.Type payloadType, final int count) {
        ConsistencyCheckEvent.Payload payload = new ConsistencyCheckEvent.Payload(payloadType, count);
        final Object source = new Object();
        return new ConsistencyCheckEvent(source, payload);
    }

}