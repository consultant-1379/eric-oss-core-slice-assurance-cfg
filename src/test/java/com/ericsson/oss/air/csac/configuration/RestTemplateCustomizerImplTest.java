/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.configuration;

import static com.ericsson.oss.air.util.logging.TestLoggingUtils.FACILITY_KEY;
import static com.ericsson.oss.air.util.logging.TestLoggingUtils.FACILITY_VALUE;
import static com.ericsson.oss.air.util.logging.TestLoggingUtils.SUBJECT_KEY;
import static com.ericsson.oss.air.util.logging.TestLoggingUtils.SUBJECT_VALUE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Optional;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.ericsson.oss.air.csac.configuration.security.SecurityConfigurationRegistry;
import com.ericsson.oss.air.csac.configuration.security.SslContextConfigurator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
class RestTemplateCustomizerImplTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private SslContextConfigurator sslContextConfigurator;

    @Mock
    private SecurityConfigurationRegistry configurationRegistry;

    private RestTemplateCustomizerImpl unsecuredRestTemplateCustomizer;

    private RestTemplateCustomizerImpl securedRestTemplateCustomizer;

    private Logger log;

    private ListAppender<ILoggingEvent> listAppender;

    @BeforeEach
    public void setUp() {

        this.unsecuredRestTemplateCustomizer = new RestTemplateCustomizerImpl(Optional.empty(), Optional.empty());
        this.securedRestTemplateCustomizer = new RestTemplateCustomizerImpl(Optional.of(this.sslContextConfigurator),
                Optional.of(this.configurationRegistry));

        this.log = (Logger) LoggerFactory.getLogger(RestTemplateCustomizerImpl.class);
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
    void init_Unsecured() {

        this.unsecuredRestTemplateCustomizer.init();

        verify(this.configurationRegistry, times(0)).register(this.unsecuredRestTemplateCustomizer);
    }

    @Test
    void init_Unsecured_AbsentConfigRegistry() {

        final RestTemplateCustomizerImpl restTemplateCustomizer = new RestTemplateCustomizerImpl(Optional.of(this.sslContextConfigurator),
                Optional.empty());

        restTemplateCustomizer.init();

        verify(this.configurationRegistry, times(0)).register(this.unsecuredRestTemplateCustomizer);
    }

    @Test
    void init_Secured() {

        this.securedRestTemplateCustomizer.init();

        verify(this.configurationRegistry, times(1)).register(this.securedRestTemplateCustomizer);
    }

    @Test
    void customize_Unsecured() {

        this.unsecuredRestTemplateCustomizer.customize(this.restTemplate);

        verify(this.restTemplate, times(0)).setRequestFactory(any());

    }

    @Test
    void customize_Secured() {

        this.securedRestTemplateCustomizer.customize(this.restTemplate);

        verify(this.restTemplate, times(1)).setRequestFactory(any());

    }

    @Test
    void reload_Unsecured() {
        this.unsecuredRestTemplateCustomizer.customize(this.restTemplate);
        this.unsecuredRestTemplateCustomizer.reload();

        verify(this.restTemplate, times(0)).setRequestFactory(any());
        assertEquals(0, this.unsecuredRestTemplateCustomizer.getRestTemplateList().size());
    }

    @Test
    void reload_Secured() {

        final RestTemplate testTemplateB = mock(RestTemplate.class);

        this.securedRestTemplateCustomizer.customize(this.restTemplate);
        this.securedRestTemplateCustomizer.customize(testTemplateB);
        this.securedRestTemplateCustomizer.reload();

        verify(this.restTemplate, times(2)).setRequestFactory(any());
        verify(testTemplateB, times(2)).setRequestFactory(any());
        assertEquals(2, this.securedRestTemplateCustomizer.getRestTemplateList().size());

        final List<ILoggingEvent> loggingEventList = this.listAppender.list;

        assertFalse(loggingEventList.isEmpty());
        assertEquals(1, loggingEventList.size());

        final ILoggingEvent startEvent = this.listAppender.list.get(0);

        assertEquals(Level.WARN, startEvent.getLevel());
        assertEquals("Reloaded REST clients with new SSL context", startEvent.getFormattedMessage());
        assertFalse(startEvent.getMDCPropertyMap().isEmpty());
        assertEquals(2, startEvent.getMDCPropertyMap().size());
        assertEquals(FACILITY_VALUE, startEvent.getMDCPropertyMap().get(FACILITY_KEY));
        assertEquals(SUBJECT_VALUE, startEvent.getMDCPropertyMap().get(SUBJECT_KEY));

    }

}