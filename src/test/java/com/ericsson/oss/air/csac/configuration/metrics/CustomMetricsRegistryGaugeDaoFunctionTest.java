/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.configuration.metrics;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.DoubleSupplier;
import java.util.function.ToDoubleFunction;

import com.ericsson.oss.air.csac.repository.AugmentationDefinitionDAO;
import com.ericsson.oss.air.csac.repository.DeployedIndexDefinitionDao;
import com.ericsson.oss.air.csac.repository.DeployedKpiDefDAO;
import com.ericsson.oss.air.csac.repository.DeployedProfileDAO;
import com.ericsson.oss.air.csac.repository.EffectiveAugmentationDAO;
import com.ericsson.oss.air.csac.repository.KPIDefinitionDAO;
import com.ericsson.oss.air.csac.repository.PMDefinitionDAO;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.internal.DefaultGauge;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest(classes = { CustomMetricsRegistry.class, PMDefinitionDAO.class, DeployedProfileDAO.class, KPIDefinitionDAO.class,
        DeployedKpiDefDAO.class, AugmentationDefinitionDAO.class, EffectiveAugmentationDAO.class,
        DeployedIndexDefinitionDao.class, CustomMetricsRegistryGaugeDaoFunctionTest.TestConfig.class })
class CustomMetricsRegistryGaugeDaoFunctionTest {

    @MockBean
    private PMDefinitionDAO pmDefinitionDAO;

    @MockBean
    private DeployedProfileDAO deployedProfileDAO;

    @MockBean
    private KPIDefinitionDAO kpiDefinitionDAO;

    @MockBean
    private DeployedKpiDefDAO deployedKpiDefDAO;

    @MockBean
    private AugmentationDefinitionDAO augmentationDefinitionDAO;

    @MockBean
    private EffectiveAugmentationDAO effectiveAugmentationDAO;

    @MockBean
    private DeployedIndexDefinitionDao deployedIndexDefinitionDao;

    @MockBean
    private GaugeDaoFunctionFactory gaugeDaoFunctionFactory;

    @Autowired
    private CustomMetricsRegistry customMetricsRegistry;

    // The number of registered gauges that do not track time
    private static final AtomicInteger NON_TIME_GAUGE_COUNT = new AtomicInteger(0);

    @TestConfiguration
    static class TestConfig {

        @Bean
        MeterRegistry meterRegistry() {

            return new SimpleMeterRegistry() {

                @Override
                protected <T> Gauge newGauge(final Meter.Id id, final T t, final ToDoubleFunction<T> toDoubleFunction) {

                    if (!(t instanceof Number)) {
                        NON_TIME_GAUGE_COUNT.incrementAndGet();
                    }

                    return new DefaultGauge(id, t, toDoubleFunction);
                }
            };
        }
    }

    // NOTE: this test was implemented to ensure that all registered metrics of gauge type that use DAO methods use the GaugeDaoFunctionFactory.
    // If not, then the defect ESOA-3715 may reoccur.
    @Test
    void registerGaugesWithDao_AllUseGaugeDaoFunctionFactory() {

        verify(this.gaugeDaoFunctionFactory, times(NON_TIME_GAUGE_COUNT.get())).createGaugeDaoFunction(ArgumentMatchers.any(DoubleSupplier.class));
    }

}
