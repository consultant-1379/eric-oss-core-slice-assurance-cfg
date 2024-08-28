/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.configuration.security;

import static com.ericsson.oss.air.util.logging.TestLoggingUtils.FACILITY_KEY;
import static com.ericsson.oss.air.util.logging.TestLoggingUtils.FACILITY_VALUE;
import static com.ericsson.oss.air.util.logging.TestLoggingUtils.SUBJECT_KEY;
import static com.ericsson.oss.air.util.logging.TestLoggingUtils.SUBJECT_VALUE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Optional;

import javax.net.ssl.SSLContext;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;

@ExtendWith(MockitoExtension.class)
class SslContextConfiguratorImplTest {

    @Mock
    private SecurityConfigurationRegistry configurationRegistry;

    private Logger log;

    private ListAppender<ILoggingEvent> listAppender;

    @BeforeEach
    public void setUp() {

        this.log = (Logger) LoggerFactory.getLogger(SslContextConfiguratorImpl.class);
        this.log.setLevel(Level.INFO);

        this.listAppender = new ListAppender<>();
        this.listAppender.start();

        this.log.addAppender(this.listAppender);
    }

    @AfterEach
    public void tearDown() throws Exception {
        this.listAppender.stop();
    }

    @Test
    void init_WithSslContext_Valid() throws NoSuchAlgorithmException {

        final SSLContext sslContext = SSLContext.getDefault();
        final Flux<Optional<SSLContext>> testFlux = Flux.just(Optional.of(sslContext));
        final SslContextConfiguratorImpl sslContextConfigurator = new SslContextConfiguratorImpl(testFlux, this.configurationRegistry);

        sslContextConfigurator.init();

        assertEquals(1, sslContextConfigurator.getSslContextStamp());
        assertEquals(sslContext, sslContextConfigurator.getSslContext());

        final List<ILoggingEvent> loggingEventList = this.listAppender.list;

        assertFalse(loggingEventList.isEmpty());
        assertEquals(2, loggingEventList.size());

        final ILoggingEvent auditEvent = this.listAppender.list.get(0);

        assertEquals(Level.WARN, auditEvent.getLevel());
        assertEquals("New SSL context set", auditEvent.getFormattedMessage());
        assertFalse(auditEvent.getMDCPropertyMap().isEmpty());
        assertEquals(2, auditEvent.getMDCPropertyMap().size());
        assertEquals(FACILITY_VALUE, auditEvent.getMDCPropertyMap().get(FACILITY_KEY));
        assertEquals(SUBJECT_VALUE, auditEvent.getMDCPropertyMap().get(SUBJECT_KEY));

        final ILoggingEvent nonAuditEvent = this.listAppender.list.get(1);

        assertEquals(Level.INFO, nonAuditEvent.getLevel());
        assertEquals("Initialized SSL context configurator", nonAuditEvent.getFormattedMessage());
        assertTrue(nonAuditEvent.getMDCPropertyMap().isEmpty());

    }

    @Test
    void init_EmptySslContext_Valid() {

        final Flux<Optional<SSLContext>> testFlux = Flux.just(Optional.empty());
        final SslContextConfiguratorImpl sslContextConfigurator = new SslContextConfiguratorImpl(testFlux, this.configurationRegistry);

        sslContextConfigurator.init();

        assertEquals(1, sslContextConfigurator.getSslContextStamp());
        assertNull(sslContextConfigurator.getSslContext());

        final List<ILoggingEvent> loggingEventList = this.listAppender.list;

        assertFalse(loggingEventList.isEmpty());
        assertEquals(3, loggingEventList.size());

        final ILoggingEvent emptyContextEvent = this.listAppender.list.get(0);

        assertEquals(Level.WARN, emptyContextEvent.getLevel());
        assertEquals("New SSL context is empty", emptyContextEvent.getFormattedMessage());
        assertFalse(emptyContextEvent.getMDCPropertyMap().isEmpty());
        assertEquals(2, emptyContextEvent.getMDCPropertyMap().size());
        assertEquals(FACILITY_VALUE, emptyContextEvent.getMDCPropertyMap().get(FACILITY_KEY));
        assertEquals(SUBJECT_VALUE, emptyContextEvent.getMDCPropertyMap().get(SUBJECT_KEY));

        final ILoggingEvent setContextEvent = this.listAppender.list.get(1);

        assertEquals(Level.WARN, setContextEvent.getLevel());
        assertEquals("New SSL context set", setContextEvent.getFormattedMessage());
        assertFalse(setContextEvent.getMDCPropertyMap().isEmpty());
        assertEquals(2, setContextEvent.getMDCPropertyMap().size());
        assertEquals(FACILITY_VALUE, setContextEvent.getMDCPropertyMap().get(FACILITY_KEY));
        assertEquals(SUBJECT_VALUE, setContextEvent.getMDCPropertyMap().get(SUBJECT_KEY));

        final ILoggingEvent nonAuditEvent = this.listAppender.list.get(2);

        assertEquals(Level.INFO, nonAuditEvent.getLevel());
        assertEquals("Initialized SSL context configurator", nonAuditEvent.getFormattedMessage());
        assertTrue(nonAuditEvent.getMDCPropertyMap().isEmpty());
    }

    @Test
    void init_NullSslContext_Valid() throws NoSuchAlgorithmException {

        final Flux<Optional<SSLContext>> testFlux = Flux.empty();
        final SslContextConfiguratorImpl sslContextConfigurator = new SslContextConfiguratorImpl(testFlux, this.configurationRegistry);

        sslContextConfigurator.init();

        assertEquals(1, sslContextConfigurator.getSslContextStamp());
        assertNull(sslContextConfigurator.getSslContext());

        final List<ILoggingEvent> loggingEventList = this.listAppender.list;

        assertFalse(loggingEventList.isEmpty());
        assertEquals(3, loggingEventList.size());

        final ILoggingEvent emptyContextEvent = this.listAppender.list.get(0);

        assertEquals(Level.WARN, emptyContextEvent.getLevel());
        assertEquals("New SSL context is empty", emptyContextEvent.getFormattedMessage());
        assertFalse(emptyContextEvent.getMDCPropertyMap().isEmpty());
        assertEquals(2, emptyContextEvent.getMDCPropertyMap().size());
        assertEquals(FACILITY_VALUE, emptyContextEvent.getMDCPropertyMap().get(FACILITY_KEY));
        assertEquals(SUBJECT_VALUE, emptyContextEvent.getMDCPropertyMap().get(SUBJECT_KEY));

        final ILoggingEvent setContextEvent = this.listAppender.list.get(1);

        assertEquals(Level.WARN, setContextEvent.getLevel());
        assertEquals("New SSL context set", setContextEvent.getFormattedMessage());
        assertFalse(setContextEvent.getMDCPropertyMap().isEmpty());
        assertEquals(2, setContextEvent.getMDCPropertyMap().size());
        assertEquals(FACILITY_VALUE, setContextEvent.getMDCPropertyMap().get(FACILITY_KEY));
        assertEquals(SUBJECT_VALUE, setContextEvent.getMDCPropertyMap().get(SUBJECT_KEY));

        final ILoggingEvent nonAuditEvent = this.listAppender.list.get(2);

        assertEquals(Level.INFO, nonAuditEvent.getLevel());
        assertEquals("Initialized SSL context configurator", nonAuditEvent.getFormattedMessage());
        assertTrue(nonAuditEvent.getMDCPropertyMap().isEmpty());
    }

    @Test
    void reload_WithSslContext_Valid() throws NoSuchAlgorithmException {

        final SSLContext sslContext = SSLContext.getDefault();

        final Flux<Optional<SSLContext>> testFlux = Flux.just(Optional.empty(), Optional.of(sslContext));
        final SslContextConfiguratorImpl sslContextConfigurator = new SslContextConfiguratorImpl(testFlux, this.configurationRegistry);

        sslContextConfigurator.init();

        assertEquals(2, sslContextConfigurator.getSslContextStamp());
        assertEquals(sslContext, sslContextConfigurator.getSslContext());

        final List<ILoggingEvent> loggingEventList = this.listAppender.list;

        assertFalse(loggingEventList.isEmpty());
        assertEquals(6, loggingEventList.size());

        // 1st 5 events are audit events
        for (final ILoggingEvent auditEvent : loggingEventList.subList(0, 4)) {
            assertEquals(Level.WARN, auditEvent.getLevel());
            assertFalse(auditEvent.getMDCPropertyMap().isEmpty());
            assertEquals(2, auditEvent.getMDCPropertyMap().size());
            assertEquals(FACILITY_VALUE, auditEvent.getMDCPropertyMap().get(FACILITY_KEY));
            assertEquals(SUBJECT_VALUE, auditEvent.getMDCPropertyMap().get(SUBJECT_KEY));
        }

        assertEquals("New SSL context is empty", loggingEventList.get(0).getFormattedMessage());
        assertEquals("New SSL context set", loggingEventList.get(1).getFormattedMessage());
        assertEquals("Refreshing SSL context", loggingEventList.get(2).getFormattedMessage());
        assertEquals("New SSL context set", loggingEventList.get(3).getFormattedMessage());
        assertEquals("Completed SSL context refresh", loggingEventList.get(4).getFormattedMessage());

        final ILoggingEvent nonAuditEvent = this.listAppender.list.get(5);

        assertEquals(Level.INFO, nonAuditEvent.getLevel());
        assertEquals("Initialized SSL context configurator", nonAuditEvent.getFormattedMessage());
        assertTrue(nonAuditEvent.getMDCPropertyMap().isEmpty());

    }

    @Test
    void reload_EmptySslContext_Valid() throws NoSuchAlgorithmException {

        final SSLContext sslContext = SSLContext.getDefault();

        final Flux<Optional<SSLContext>> testFlux = Flux.just(Optional.of(sslContext), Optional.empty());
        final SslContextConfiguratorImpl sslContextConfigurator = new SslContextConfiguratorImpl(testFlux, this.configurationRegistry);

        sslContextConfigurator.init();

        assertEquals(2, sslContextConfigurator.getSslContextStamp());
        assertNull(sslContextConfigurator.getSslContext());

        final List<ILoggingEvent> loggingEventList = this.listAppender.list;

        assertFalse(loggingEventList.isEmpty());
        assertEquals(6, loggingEventList.size());

        // 1st 5 events are audit events
        for (final ILoggingEvent auditEvent : loggingEventList.subList(0, 4)) {
            assertEquals(Level.WARN, auditEvent.getLevel());
            assertFalse(auditEvent.getMDCPropertyMap().isEmpty());
            assertEquals(2, auditEvent.getMDCPropertyMap().size());
            assertEquals(FACILITY_VALUE, auditEvent.getMDCPropertyMap().get(FACILITY_KEY));
            assertEquals(SUBJECT_VALUE, auditEvent.getMDCPropertyMap().get(SUBJECT_KEY));
        }

        assertEquals("New SSL context set", loggingEventList.get(0).getFormattedMessage());
        assertEquals("Refreshing SSL context", loggingEventList.get(1).getFormattedMessage());
        assertEquals("New SSL context is empty", loggingEventList.get(2).getFormattedMessage());
        assertEquals("New SSL context set", loggingEventList.get(3).getFormattedMessage());
        assertEquals("Completed SSL context refresh", loggingEventList.get(4).getFormattedMessage());

        final ILoggingEvent nonAuditEvent = this.listAppender.list.get(5);

        assertEquals(Level.INFO, nonAuditEvent.getLevel());
        assertEquals("Initialized SSL context configurator", nonAuditEvent.getFormattedMessage());
        assertTrue(nonAuditEvent.getMDCPropertyMap().isEmpty());
    }

}