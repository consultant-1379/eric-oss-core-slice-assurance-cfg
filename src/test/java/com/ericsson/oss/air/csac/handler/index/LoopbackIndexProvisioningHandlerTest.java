/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.handler.index;

import static com.ericsson.oss.air.csac.handler.index.LoopbackIndexProvisioningHandler.INDEX_PROVISIONING_IS_DISABLED;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.ericsson.oss.air.util.operator.StatefulSequentialOperator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

class LoopbackIndexProvisioningHandlerTest {

    private ListAppender<ILoggingEvent> listAppender;

    @BeforeEach
    void setup() {
        final Logger logger = (Logger) LoggerFactory.getLogger(LoopbackIndexProvisioningHandler.class);
        logger.setLevel(Level.INFO);
        this.listAppender = new ListAppender<>();
        this.listAppender.start();
        logger.addAppender(listAppender);
    }

    private final IndexProvisioningHandler testHandler = new LoopbackIndexProvisioningHandler();

    @Test
    void getRollbackOperator() {

        assertEquals(StatefulSequentialOperator.noop(), this.testHandler.getRollback());

    }

    @Test
    void apply() {

        this.testHandler.apply(List.of());

        assertEquals(INDEX_PROVISIONING_IS_DISABLED, this.listAppender.list.get(0).getMessage());
    }
}