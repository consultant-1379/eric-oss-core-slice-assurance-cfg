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

import static org.junit.jupiter.api.Assertions.assertEquals;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

public class LegacyPmscResetOperatorTest {

    private ListAppender<ILoggingEvent> listAppender;

    @BeforeEach
    void setup() {
        final Logger logger = (Logger) LoggerFactory.getLogger(LegacyPmscResetOperator.class);
        logger.setLevel(Level.WARN);
        this.listAppender = new ListAppender<>();
        this.listAppender.start();
        logger.addAppender(listAppender);
    }

    private final LegacyPmscResetOperator operator = new LegacyPmscResetOperator();

    @Test
    void apply() {

        this.operator.apply();

        assertEquals("Configuration reset is not supported for the legacy PMSC service", this.listAppender.list.get(0).getMessage());
    }
}
