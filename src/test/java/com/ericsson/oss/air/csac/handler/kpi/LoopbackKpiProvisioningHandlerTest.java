/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.handler.kpi;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;

class LoopbackKpiProvisioningHandlerTest {

    private ListAppender<ILoggingEvent> listAppender;

    private LoopbackKpiProvisioningHandler loopbackKpiProvisioningHandler = new LoopbackKpiProvisioningHandler();

    @BeforeEach
    void setUp() throws Exception {
        final Logger logger = (Logger) LoggerFactory.getLogger(LoopbackKpiProvisioningHandler.class);
        logger.setLevel(Level.INFO);
        this.listAppender = new ListAppender<>();
        this.listAppender.start();
        logger.addAppender(listAppender);
    }

    @Test
    void doApply() {

        this.loopbackKpiProvisioningHandler.doApply(Collections.emptyList());

        assertEquals("KPI provisioning disabled", this.listAppender.list.get(0).getMessage());
    }
}