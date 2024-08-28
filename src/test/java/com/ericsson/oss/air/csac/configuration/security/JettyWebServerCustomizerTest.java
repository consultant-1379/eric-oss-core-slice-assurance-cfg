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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.security.NoSuchAlgorithmException;
import java.util.List;

import javax.net.ssl.SSLContext;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.embedded.jetty.JettyServletWebServerFactory;

@ExtendWith(MockitoExtension.class)
class JettyWebServerCustomizerTest {

    @Mock
    private SslContextConfigurator sslContextConfigurator;

    @Mock
    private SecurityConfigurationRegistry configurationRegistry;

    @InjectMocks
    private JettyWebServerCustomizer customizer;

    private Logger log;

    private ListAppender<ILoggingEvent> listAppender;

    @BeforeEach
    public void setUp() {

        this.log = (Logger) LoggerFactory.getLogger(JettyWebServerCustomizer.class);
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
    void init_Valid() {

        this.customizer.init();

        verify(this.configurationRegistry, times(1)).register(this.customizer);
    }

    @Test
    void sslContextFactory_WithSslContext_ReturnsSslContextFactory() throws NoSuchAlgorithmException {

        final SSLContext expectedSslContext = SSLContext.getDefault();
        when(this.sslContextConfigurator.getSslContext()).thenReturn(expectedSslContext);

        final SslContextFactory.Server sslContextFactory = this.customizer.sslContextFactory();

        assertNotNull(sslContextFactory);
        assertTrue(sslContextFactory.getNeedClientAuth());
        assertEquals(expectedSslContext, sslContextFactory.getSslContext());
    }

    @Test
    void sslContextFactory_NullSslContext_ReturnsSslContextFactory() {

        final SslContextFactory.Server sslContextFactory = this.customizer.sslContextFactory();

        assertNotNull(sslContextFactory);
        assertTrue(sslContextFactory.getNeedClientAuth());
        assertNull(sslContextFactory.getSslContext());

    }

    @Test
    void customize_Valid() {

        final JettyServletWebServerFactory factory = new JettyServletWebServerFactory();

        this.customizer.customize(factory);

        assertNotNull(factory);
        assertEquals(1, factory.getServerCustomizers().size());

    }

    @Test
    void reload_NewSslContext_Valid() throws Exception {

        final SslContextFactory.Server sslContextFactory = this.customizer.sslContextFactory();
        assertNull(sslContextFactory.getSslContext());

        final JettyWebServerCustomizer testCustomizer = new JettyWebServerCustomizer(this.sslContextConfigurator, this.configurationRegistry) {

            @Override
            public SslContextFactory.Server sslContextFactory() {
                return sslContextFactory;
            }

        };

        final SSLContext expectedSslContext = SSLContext.getDefault();
        when(this.sslContextConfigurator.getSslContext()).thenReturn(expectedSslContext);

        testCustomizer.reload();

        assertEquals(expectedSslContext, sslContextFactory.getSslContext());
        assertTrue(sslContextFactory.getNeedClientAuth());

        final List<ILoggingEvent> loggingEventList = this.listAppender.list;

        assertFalse(loggingEventList.isEmpty());
        assertEquals(1, loggingEventList.size());

        final ILoggingEvent auditEvent = this.listAppender.list.get(0);

        assertEquals(Level.WARN, auditEvent.getLevel());
        assertEquals("Reloaded server with new SSL context", auditEvent.getFormattedMessage());
        assertFalse(auditEvent.getMDCPropertyMap().isEmpty());
        assertEquals(2, auditEvent.getMDCPropertyMap().size());
        assertEquals(FACILITY_VALUE, auditEvent.getMDCPropertyMap().get(FACILITY_KEY));
        assertEquals(SUBJECT_VALUE, auditEvent.getMDCPropertyMap().get(SUBJECT_KEY));

    }

    @Test
    void reload_ContextFactoryException_HandlesException() {

        final Exception exception = new Exception();

        final SslContextFactory.Server sslContextFactory = new SslContextFactory.Server() {

            @Override
            public void reload(final java.util.function.Consumer<SslContextFactory> consumer) throws Exception {
                throw exception;
            }
        };
        sslContextFactory.setNeedClientAuth(true);
        assertNull(sslContextFactory.getSslContext());

        final JettyWebServerCustomizer testCustomizer = new JettyWebServerCustomizer(this.sslContextConfigurator, this.configurationRegistry) {

            @Override
            public SslContextFactory.Server sslContextFactory() {
                return sslContextFactory;
            }

        };

        testCustomizer.reload();

        assertNull(sslContextFactory.getSslContext());
        assertTrue(sslContextFactory.getNeedClientAuth());

        final List<ILoggingEvent> loggingEventList = this.listAppender.list;

        assertFalse(loggingEventList.isEmpty());
        assertEquals(1, loggingEventList.size());

        final ILoggingEvent auditEvent = this.listAppender.list.get(0);

        assertEquals(Level.WARN, auditEvent.getLevel());
        assertEquals("Cannot refresh server SSL context: ", auditEvent.getFormattedMessage());
        assertEquals(exception.getClass().getName(), auditEvent.getThrowableProxy().getClassName());
        assertFalse(auditEvent.getMDCPropertyMap().isEmpty());
        assertEquals(2, auditEvent.getMDCPropertyMap().size());
        assertEquals(FACILITY_VALUE, auditEvent.getMDCPropertyMap().get(FACILITY_KEY));
        assertEquals(SUBJECT_VALUE, auditEvent.getMDCPropertyMap().get(SUBJECT_KEY));

    }
}