/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.handler.augmentation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

public class LoopbackAugmentationHandlerTest {

    private ListAppender<ILoggingEvent> listAppender;

    private AugmentationHandler augmentationHandler = new LoopbackAugmentationHandler();

    @BeforeEach
    void setup() {
        final Logger logger = (Logger) LoggerFactory.getLogger(LoopbackAugmentationHandler.class);
        logger.setLevel(Level.INFO);
        this.listAppender = new ListAppender<>();
        this.listAppender.start();
        logger.addAppender(listAppender);
    }

    @Test
    void submitTest() {
        this.augmentationHandler.submit(new ArrayList<>());

        assertEquals("Provisioning AAS is disabled.", this.listAppender.list.get(0).getMessage());
    }
}
