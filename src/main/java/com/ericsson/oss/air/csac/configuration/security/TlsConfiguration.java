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

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.net.ssl.SSLContext;

import com.ericsson.adp.security.certm.certificatewatcher.CertificateWatcherService;
import com.ericsson.adp.security.certm.certificatewatcher.TlsContext;
import com.ericsson.oss.air.util.logging.audit.AuditLogFactory;
import com.ericsson.oss.air.util.logging.audit.AuditLogger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Configures the required publisher for enabling CSAC to communicate as a server and as a client via mTLS. This
 * publisher will receive notifications upon PEM file changes at configured disk locations. Each notification is
 * represented as a collection of {@code TlsContext}'s, each which contains a keystore and a truststore. The keystore and/or the truststore
 * may be empty. The publisher will combine the received {@code TlsContext}'s and emit a {@code SSLContext}.
 * <p>
 * <p/>
 * <p>
 * {@code TlsConfiguration} is only needed when TLS/SSL is enabled for CSAC. It will be instantiated when
 * adp-certificate.discovery.root-write-path is present in the CSAC's configuration because root-write-path is the
 * sole mandatory parameter for configuring the ADP Certificate Reloader Library.
 *
 * @see <a href="https://gerrit-gamma.gic.ericsson.se/plugins/gitiles/adp-chassis/java/adp-chassis-library/+/master/adp-components/certm/certificate-watcher/README.md">Certificate Reloader Library README</a>
 */
@Configuration
@Slf4j
@ConditionalOnExpression("!T(org.springframework.util.ObjectUtils).isEmpty('${adp-certificate.discovery.root-write-path:}')")
@EnableConfigurationProperties(AdpCertificateDiscoveryProperties.class)
@RequiredArgsConstructor
public class TlsConfiguration {

    private static final AuditLogger AUDIT_LOGGER = AuditLogFactory.getLogger(TlsConfiguration.class);

    @Autowired
    private final CertificateWatcherService certificateWatcherService;

    @Autowired
    private final SecurityArtifactCombinator<TlsContext, SSLContext> securityArtifactCombinator;

    /*
     * From https://adp.ericsson.se/marketplace/service-identity-provider-tls/documentation/7.0.0/dpi/api-documentation#certificate-renewal-details-and-examples:
     *
     * "Note: To allow enough time for the renewal the minimum value of leadtime is 180 sec, which is always maintained
     * even if the calculations would result in a lower value. It also means, that in case the TTL is set to 180 seconds
     * (either by validLifetimeSeconds or overrideTtl) it would result in nearly instant certificate renewals performed
     * in every 10 seconds. Therefore, it is recommended to use higher TTL values in case longer renewal time is needed."
     *
     * This value defines the time in milliseconds that the publisher will periodically emit the latest generated SSL Context
     * from all the received TlsContext's after the first SSL Context has been emitted.
     *
     * A default of 5 seconds was chosen since it is half of 10 seconds, which is the minimum time of certificate renewals.
     */
    @Value("${security.sampleWindowDurationMs:5000}")
    private long sampleWindowDurationMs;

    /**
     * Returns the publisher that emits a {@code SSLContext}. The publisher is never terminated. It is guaranteed that a subscription will synchronously emit a
     * {@code SSLContext} right after a subscription.
     *
     * @param adpCertificateDiscoveryProperties the configuration properties for the 2PP Certificate Reloader library
     * @return the publisher that emits a {@code SSLContext} wrapped in an {@code Optional}
     */
    @Bean
    public Flux<Optional<SSLContext>> sslContextPublisher(final AdpCertificateDiscoveryProperties adpCertificateDiscoveryProperties) {

        final List<Flux<TlsContext>> certPublisherList = this.getCertPublisherList();
        final String rootReadPath = adpCertificateDiscoveryProperties.getRootReadPath();

        /*
         * The Certificate Reloader library states:
         *
         * "It is guaranteed that a subscription will synchronously emit a TlsContext right after a
         * subscription. In case there is an internal failure while generating the first TlsContext, an empty
         * TlsContext is emitted, but the Flux is not terminated."
         *
         * This combined publisher was designed with its upstream publishers' guarantee in mind and will also synchronously emit upon subscription.
         */
        final Flux<Optional<SSLContext>> combinedPublisher = Flux.combineLatest(certPublisherList, this::combineTlsContexts)
                .share()
                .cache(1);
        final Mono<Optional<SSLContext>> firstResult = combinedPublisher.shareNext();

        return Flux.merge(
                        // Ensures that the stream never terminates
                        Flux.never(),
                        combinedPublisher
                                .sample(Duration.ofMillis(this.sampleWindowDurationMs))
                                .skip(1),
                        //provides immediate result since sample will always delay any emitted item by sample window duration
                        firstResult
                                .doOnSubscribe(tlsContext -> AUDIT_LOGGER.warn("Subscribing to certificate changes in {}", rootReadPath))
                                .doOnNext(tlsContext -> AUDIT_LOGGER.warn("Loading new certificates from {}", rootReadPath)))
                .share()
                .cache(1);
    }

    private List<Flux<TlsContext>> getCertPublisherList() {

        return Stream.of(CertificateIdEnum.values())
                .map(certIdEnum -> this.certificateWatcherService.observe(certIdEnum.getId())
                        .doOnNext(tlsContext ->
                                AUDIT_LOGGER.warn("Certificate change detected for {}",
                                        generateTlsContextMessage(certIdEnum, tlsContext))))
                .collect(Collectors.toList());
    }

    private String generateTlsContextMessage(final CertificateIdEnum certificateIdEnum, final TlsContext tlsContext) {

        final StringBuilder stringBuilder = new StringBuilder(certificateIdEnum.getDisplayName());
        stringBuilder.append(", content: ");

        final List<String> contentList = new ArrayList<>();

        if (tlsContext.getKeyStore().isPresent()) {
            contentList.add("keystore");
        }

        if (tlsContext.getTrustStore().isPresent()) {
            contentList.add("truststore");
        }

        stringBuilder.append(contentList);

        return stringBuilder.toString();
    }

    /*
     * (non-javadoc)
     *
     * Adapts the Flux combinator function arguments (see https://projectreactor.io/docs/core/release/api/reactor/core/publisher/Flux.html#combineLatest-java.lang.Iterable-java.util.function.Function-)
     * to be utilized in CSAC's combinator. Returns SSL context in an Optional.
     */
    private Optional<SSLContext> combineTlsContexts(final Object[] tlsContextArray) {

        final TlsContext[] tlsContexts = Arrays.asList(tlsContextArray)
                .stream()
                .filter(Objects::nonNull)
                .map(TlsContext.class::cast)
                .collect(Collectors.toList())
                .toArray(new TlsContext[0]);

        return this.securityArtifactCombinator.combine(tlsContexts);
    }
}
