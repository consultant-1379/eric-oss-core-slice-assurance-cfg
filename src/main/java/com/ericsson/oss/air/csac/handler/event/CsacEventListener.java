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

import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicLong;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

/**
 * The event listener is responsible for external notifications regarding the consistency check event {@link ConsistencyCheckEvent}.
 * <p>
 * When received an event, it acts accordingly:
 * <ul>
 * <li>log indicating the nature of the consistency check event</li>
 * <li>updating the consistency check event metric</li>
 * </ul>
 */
@Component
@Slf4j
public class CsacEventListener {

    private static final Integer MAX_POOL_SIZE = 100;

    private static final String CONSISTENCY_CHECK_EVENT = "Consistency Check Event: {}";

    @Autowired
    @Qualifier("registerRuntimeConfigConsistencyCheckError")
    private AtomicLong eventCountMetric;

    /**
     * Returns a bean of type {@link Executor} which had been initialized with maximum pool size to 100.
     *
     * @return a bean of type {@link Executor}
     */
    @Bean("asyncEventHandlerExecutor")
    public Executor getAsyncEventHandlerExecutor() {

        final ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setMaxPoolSize(MAX_POOL_SIZE);
        taskExecutor.initialize();

        return taskExecutor;
    }

    /**
     * When receiving the ConsistencyCheckEvent event, it logs and updates the corresponding metric.
     *
     * @param event received consistency check event
     */
    @Async("asyncEventHandlerExecutor")
    @EventListener
    public void onConsistencyCheckEvent(final ConsistencyCheckEvent event) {

        final ConsistencyCheckEvent.Payload payload = event.getPayload();

        switch (Objects.requireNonNull(payload.getType())) {
            case OK -> log.info(CONSISTENCY_CHECK_EVENT, "No consistency check failure.");

            case SUSPECT -> {
                log.warn(CONSISTENCY_CHECK_EVENT, "Possible configuration inconsistency.");
                this.eventCountMetric.addAndGet(payload.getCount());
            }

            case FAILURE -> {
                log.error(CONSISTENCY_CHECK_EVENT, "Known configuration inconsistency.");
                this.eventCountMetric.addAndGet(payload.getCount());
            }

            case CLEAR -> {
                log.info(CONSISTENCY_CHECK_EVENT, "Consistency check failure has been cleared.");
                this.eventCountMetric.set(0L);
            }
        }
    }

}