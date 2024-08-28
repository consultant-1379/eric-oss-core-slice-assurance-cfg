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
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.net.ssl.SSLContext;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.ericsson.adp.security.certm.certificatewatcher.CertificateWatcherService;
import com.ericsson.adp.security.certm.certificatewatcher.KeyStoreItem;
import com.ericsson.adp.security.certm.certificatewatcher.TlsContext;
import com.ericsson.adp.security.certm.certificatewatcher.TrustStoreItem;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;
import reactor.test.publisher.TestPublisher;

@ExtendWith(MockitoExtension.class)
class TlsConfigurationTest {

    @Mock
    private CertificateWatcherService certificateWatcherService;

    @Mock
    private SecurityArtifactCombinator<TlsContext, SSLContext> securityArtifactCombinator;

    @InjectMocks
    private TlsConfiguration tlsConfiguration;

    @Mock
    private KeyStoreItem mockKeyStoreItem;

    @Mock
    private TrustStoreItem mockTrustStoreItem;

    @Mock
    private SSLContext mockSslContext;

    @Component
    private static class TestCertificateWatcherService implements CertificateWatcherService {

        @Override
        public Flux<TlsContext> observe(String certificateId) {
            return Flux.empty();
        }
    }

    @Component
    private static class TestSecurityArtifactCombinator implements SecurityArtifactCombinator {

        @Override
        public Optional combine(Object[] artifacts) {
            return Optional.empty();
        }
    }

    private final ApplicationContextRunner runner = new ApplicationContextRunner();

    private Logger log;

    private ListAppender<ILoggingEvent> listAppender;

    @BeforeEach
    public void setUp() {

        this.log = (Logger) LoggerFactory.getLogger(TlsConfiguration.class);
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
    public void runApplication_NoTlsConfigurationBean() {
        this.runner.run(context -> assertThat(context).doesNotHaveBean("tlsConfiguration"));
    }

    @Test
    public void runApplication_HasTlsConfigurationAndSubscriptionBeans_OnlyRootWritePath() {
        this.runner.withPropertyValues("adp-certificate.discovery.root-write-path=tmp")
                .withBean(TestCertificateWatcherService.class)
                .withBean(TestSecurityArtifactCombinator.class)
                .withUserConfiguration(TlsConfiguration.class)
                .run(context -> {
                    assertThat(context).hasBean("tlsConfiguration");
                    assertThat(context).hasBean("sslContextPublisher");
                });
    }

    @Test
    public void runApplication_HasTlsConfigurationAndSubscriptionBeans_AllCertProperties() {
        this.runner.withPropertyValues("adp-certificate.discovery.root-write-path=tmp",
                        "adp-certificate.discovery.root-read-path=/opt/certs",
                        "adp-certificate.discovery.keystore-relative-dir=tmpkeystore",
                        "adp-certificate.discovery.truststore-relative-dir=tmptruststore")
                .withBean(TestCertificateWatcherService.class)
                .withBean(TestSecurityArtifactCombinator.class)
                .withUserConfiguration(TlsConfiguration.class)
                .run(context -> {
                    assertThat(context).hasBean("tlsConfiguration");
                    assertThat(context).hasBean("sslContextPublisher");
                });
    }

    @Test
    public void sslContextPublisher_CombinesTlsContexts_ReturnsSslContextImmediately() {

        // Set up a test publisher per certificate ID
        final Map<CertificateIdEnum, TestPublisher> publisherMap = new HashMap<>();

        for (final CertificateIdEnum certificateIdEnum : CertificateIdEnum.values()) {

            final TestPublisher<TlsContext> testPublisher = TestPublisher.create();
            publisherMap.put(certificateIdEnum, testPublisher);

            when(this.certificateWatcherService.observe(certificateIdEnum.getId())).thenReturn(testPublisher.flux());
        }

        // Mock dependent component
        when(this.securityArtifactCombinator.combine(Mockito.any(TlsContext.class), Mockito.any(TlsContext.class), Mockito.any(TlsContext.class),
                Mockito.any(TlsContext.class))).thenReturn(Optional.of(this.mockSslContext));

        final AdpCertificateDiscoveryProperties properties = new AdpCertificateDiscoveryProperties();
        properties.setRootWritePath("/tmp");

        StepVerifier
                .create(this.tlsConfiguration.sslContextPublisher(properties).log())
                .then(() -> {

                    publisherMap.keySet().forEach(certIdEnum -> {

                        final TlsContext.TlsContextBuilder tlsContextBuilder = TlsContext.builder();
                        tlsContextBuilder.name(certIdEnum.getId());

                        if (Set.of(CertificateIdEnum.ROOTCA, CertificateIdEnum.PMCA).contains(certIdEnum)) {
                            tlsContextBuilder.trustStore(this.mockTrustStoreItem);
                        }
                        if (Set.of(CertificateIdEnum.SERVER, CertificateIdEnum.LOG).contains(certIdEnum)) {
                            tlsContextBuilder.keyStore(this.mockKeyStoreItem);
                        }

                        publisherMap.get(certIdEnum).next(tlsContextBuilder.build());
                    });

                })
                .expectNextMatches(sslContextOptional -> {
                    assertTrue(sslContextOptional.isPresent());
                    assertEquals(this.mockSslContext, sslContextOptional.get());
                    return true;
                })
                .thenCancel()
                .verify();

        final List<ILoggingEvent> loggingEventList = this.listAppender.list;

        assertFalse(loggingEventList.isEmpty());
        assertEquals(6, loggingEventList.size());

        final Set<String> certChangeMessages = new java.util.HashSet<>(Set.of("Certificate change detected for Log Transformer, content: [keystore]",
                "Certificate change detected for Root Certificate Authority, content: [truststore]",
                "Certificate change detected for PM Server Certificate Authority, content: [truststore]",
                "Certificate change detected for Embedded Server, content: [keystore]"));

        // All logged events are audit events
        for (final ILoggingEvent loggingEvent : loggingEventList) {
            assertEquals(Level.WARN, loggingEvent.getLevel());
            assertFalse(loggingEvent.getMDCPropertyMap().isEmpty());
            assertEquals(2, loggingEvent.getMDCPropertyMap().size());
            assertEquals(FACILITY_VALUE, loggingEvent.getMDCPropertyMap().get(FACILITY_KEY));
            assertEquals(SUBJECT_VALUE, loggingEvent.getMDCPropertyMap().get(SUBJECT_KEY));

            certChangeMessages.remove(loggingEvent.getFormattedMessage());
        }

        assertTrue(certChangeMessages.isEmpty());
        assertEquals("Subscribing to certificate changes in certs", loggingEventList.get(0).getFormattedMessage());
        assertEquals("Loading new certificates from certs", loggingEventList.get(5).getFormattedMessage());

    }

}