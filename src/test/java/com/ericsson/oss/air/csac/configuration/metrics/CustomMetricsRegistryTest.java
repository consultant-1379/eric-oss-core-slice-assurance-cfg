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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.ericsson.oss.air.csac.model.TestResourcesUtils;
import com.ericsson.oss.air.csac.model.runtime.index.DeployedIndexDefinitionDto;
import com.ericsson.oss.air.csac.repository.AugmentationDefinitionDAO;
import com.ericsson.oss.air.csac.repository.DeployedIndexDefinitionDao;
import com.ericsson.oss.air.csac.repository.DeployedKpiDefDAO;
import com.ericsson.oss.air.csac.repository.DeployedProfileDAO;
import com.ericsson.oss.air.csac.repository.EffectiveAugmentationDAO;
import com.ericsson.oss.air.csac.repository.KPIDefinitionDAO;
import com.ericsson.oss.air.csac.repository.PMDefinitionDAO;
import com.ericsson.oss.air.csac.repository.impl.inmemorydb.AugmentationDefinitionDAOImpl;
import com.ericsson.oss.air.csac.repository.impl.inmemorydb.DeployedIndexDefinitionDaoImpl;
import com.ericsson.oss.air.csac.repository.impl.inmemorydb.DeployedKpiDefDAOImp;
import com.ericsson.oss.air.csac.repository.impl.inmemorydb.DeployedProfileDAOImpl;
import com.ericsson.oss.air.csac.repository.impl.inmemorydb.EffectiveAugmentationDAOImpl;
import com.ericsson.oss.air.csac.repository.impl.inmemorydb.KPIDefinitionDAOImpl;
import com.ericsson.oss.air.csac.repository.impl.inmemorydb.PMDefinitionDAOImpl;
import com.ericsson.oss.air.util.concurrent.AtomicDouble;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

class CustomMetricsRegistryTest {

    private final MeterRegistry registry = new SimpleMeterRegistry();

    private final GaugeDaoFunctionFactory gaugeDaoFunctionFactory = new GaugeDaoFunctionFactory();

    private CustomMetricsRegistry metricsRegister;

    @BeforeEach
    public void setUp() {
        metricsRegister = new CustomMetricsRegistry(registry, gaugeDaoFunctionFactory);
    }

    @AfterEach
    public void tearDown() {
        registry.clear();
    }

    @Test
    void pmMetricsTest() {
        final PMDefinitionDAO pmDefinitionDAO = new PMDefinitionDAOImpl();

        metricsRegister.registerPMCount(pmDefinitionDAO);

        final Gauge pmCountMetric = registry.find(CustomMetrics.DICTIONARY_COUNT_PM_DEFS.getMetricName()).gauge();
        assertEquals(0, pmCountMetric.value());

        pmDefinitionDAO.savePMDefinition(TestResourcesUtils.VALID_PM_DEF_OBJ, "SCHEMA_NAME");
        assertEquals(1, pmCountMetric.value());
    }

    @Test
    void kpiMetricsTest_simpleMeterRegistryTest() {
        final KPIDefinitionDAO kpiDefinitionDAO = new KPIDefinitionDAOImpl();

        metricsRegister.registerKPICount(kpiDefinitionDAO);

        final Gauge kpiCountMetric = registry.find(CustomMetrics.DICTIONARY_COUNT_KPI_DEFS.getMetricName()).gauge();
        assertEquals(0, kpiCountMetric.value());

        kpiDefinitionDAO.saveKPIDefinition(TestResourcesUtils.VALID_SIMPLE_KPI_DEF_OBJ);
        assertEquals(1, kpiCountMetric.value());
    }

    @Test
    void registerDeployedProfileCount() {
        final DeployedProfileDAO deployedProfileDAO = new DeployedProfileDAOImpl();
        metricsRegister.registerDeployedProfileCount(deployedProfileDAO);
        final Gauge profileCountMetric = registry.find(CustomMetrics.DEPLOYED_COUNT_PROFILE_DEFS.getMetricName()).gauge();
        assertEquals(0, profileCountMetric.value());

        deployedProfileDAO.saveProfileDefinition(TestResourcesUtils.VALID_PROFILE_DEF_OBJ);
        final Gauge deployedProfileCountMetric = registry.find(CustomMetrics.DEPLOYED_COUNT_PROFILE_DEFS.getMetricName()).gauge();
        assertEquals(1, deployedProfileCountMetric.value());
    }

    @Test
    void registerDeployedKPICount() {
        final DeployedKpiDefDAO deployedKpiDefDAO = new DeployedKpiDefDAOImp();
        metricsRegister.registerDeployedKPICount(deployedKpiDefDAO);
        final Gauge kpiCountMetric = registry.find(CustomMetrics.DEPLOYED_COUNT_KPI_INSTANCES.getMetricName()).gauge();
        assertEquals(0, kpiCountMetric.value());

        deployedKpiDefDAO.createDeployedKpi(TestResourcesUtils.DEPLOYED_SIMPLE_KPI_OBJ, TestResourcesUtils.VALID_SIMPLE_KPI_DEF_NAME,
                                            TestResourcesUtils.VALID_PROFILE_DEF_OBJ);
        final Gauge deployedKPICountMetric = registry.find(CustomMetrics.DEPLOYED_COUNT_KPI_INSTANCES.getMetricName()).gauge();
        assertEquals(1, deployedKPICountMetric.value());
    }

    @Test
    void registerPmscProcessingTime() {
        final AtomicDouble expected = this.metricsRegister.provisioningPmscTime();

        final Gauge actual = this.registry.find(CustomMetrics.PROVISIONING_PMSC_TIME_SECONDS.getMetricName()).gauge();
        assertNotNull(actual);

        expected.set(Math.PI);
        assertEquals(Math.PI, actual.value());
    }

    @Test
    void registerKpiProcessingTime() {
        final AtomicDouble expected = this.metricsRegister.provisioningKpiTime();

        final Gauge actual = this.registry.find(CustomMetrics.PROVISIONING_KPI_TIME_SECONDS.getMetricName()).gauge();
        assertNotNull(actual);

        expected.set(Math.PI);
        assertEquals(Math.PI, actual.value());
    }

    @Test
    void registerCsacProcessingTime() {
        final AtomicDouble expected = this.metricsRegister.provisioningTotalTime();

        final Gauge actual = this.registry.find(CustomMetrics.PROVISIONING_TOTAL_TIME_SECONDS.getMetricName()).gauge();
        assertNotNull(actual);

        expected.set(Math.PI);
        assertEquals(Math.PI, actual.value());
    }

    @Test
    void registerAugmentationCountTest() {
        final AugmentationDefinitionDAO augmentationDefinitionDAO = new AugmentationDefinitionDAOImpl();

        metricsRegister.registerAugmentationCount(augmentationDefinitionDAO);

        final Gauge augDefinitionCountMetric = registry.find(CustomMetrics.DICTIONARY_COUNT_AUG_DEFS.getMetricName()).gauge();
        assertEquals(0, augDefinitionCountMetric.value());

        augmentationDefinitionDAO.save(TestResourcesUtils.VALID_AUGMENTATION_DEF_OBJ);
        assertEquals(1, augDefinitionCountMetric.value());
    }

    @Test
    void registerDeployedAugmentationCount() {
        final EffectiveAugmentationDAO effectiveAugmentationDAO = new EffectiveAugmentationDAOImpl();

        metricsRegister.registerDeployedAugmentationCount(effectiveAugmentationDAO);

        final Gauge deployedAugDefinitionCountMetric = registry.find(CustomMetrics.DEPLOYED_COUNT_AUG_DEFS.getMetricName()).gauge();
        assertEquals(0, deployedAugDefinitionCountMetric.value());

        effectiveAugmentationDAO.save(TestResourcesUtils.VALID_AUGMENTATION_DEF_OBJ, List.of(TestResourcesUtils.VALID_PROFILE_DEF_NAME));
        assertEquals(1, deployedAugDefinitionCountMetric.value());
    }

    @Test
    void registerAasProcessingTime() {
        final AtomicDouble expected = this.metricsRegister.provisioningAasTime();

        final Gauge actual = this.registry.find(CustomMetrics.PROVISIONING_AAS_TIME_SECONDS.getMetricName()).gauge();
        assertNotNull(actual);

        expected.set(Math.PI);
        assertEquals(Math.PI, actual.value());
    }

    @Test
    void registerDeployedIndexCount() {
        final DeployedIndexDefinitionDao deployedIndexDefinitionDao = new DeployedIndexDefinitionDaoImpl();

        metricsRegister.registerDeployedIndexCount(deployedIndexDefinitionDao);

        final Gauge deployedIndexDefinitionCountMetric = registry.find(CustomMetrics.DEPLOYED_COUNT_INDEX_DEFS.getMetricName()).gauge();
        assertEquals(0, deployedIndexDefinitionCountMetric.value());

        final DeployedIndexDefinitionDto dto = DeployedIndexDefinitionDto.builder().indexDefinitionName("dto1").build();

        deployedIndexDefinitionDao.save(dto);
        assertEquals(1, deployedIndexDefinitionCountMetric.value());
    }

    @Test
    void registerIndexProcessingTime() {
        final AtomicDouble expected = this.metricsRegister.provisioningIndexTime();

        final Gauge actual = this.registry.find(CustomMetrics.PROVISIONING_INDEX_TIME_SECONDS.getMetricName()).gauge();
        assertNotNull(actual);

        expected.set(Math.PI);
        assertEquals(Math.PI, actual.value());
    }

    @Test
    void registerRuntimeConfigConsistencyCheckError() {
        final AtomicLong expected = this.metricsRegister.registerRuntimeConfigConsistencyCheckError();

        final Gauge actual = this.registry.find(CustomMetrics.RUNTIME_CONFIG_CONSISTENCY_CHECK_ERRORS.getMetricName()).gauge();
        assertEquals(0, actual.value());

        expected.set(3L);
        assertEquals(expected.doubleValue(), actual.value());
    }

    @Test
    void configurationResetDbErrorCounter() {

        final AtomicLong expected = this.metricsRegister.configurationResetDbErrorCounter();

        final Gauge actual = this.registry.find(CustomMetrics.CONFIGURATION_RESET_DB_ERRORS.getMetricName()).gauge();
        assertNotNull(actual);

        expected.set(1L);
        assertEquals(expected.doubleValue(), actual.value());
    }

    @Test
    void configurationResetDbTime() {

        final AtomicDouble expected = this.metricsRegister.configurationResetDbTime();

        final Gauge actual = this.registry.find(CustomMetrics.CONFIGURATION_RESET_DB_TIME_SECONDS.getMetricName()).gauge();
        assertNotNull(actual);

        expected.set(Math.PI);
        assertEquals(Math.PI, actual.value());
    }

    @Test
    void configurationResetKpiErrorCounter() {

        final AtomicLong expected = this.metricsRegister.configurationResetKpiErrorCounter();

        final Gauge actual = this.registry.find(CustomMetrics.CONFIGURATION_RESET_KPI_ERRORS.getMetricName()).gauge();
        assertNotNull(actual);

        expected.set(1L);
        assertEquals(expected.doubleValue(), actual.value());
    }

    @Test
    void configurationResetKpiTime() {

        final AtomicDouble expected = this.metricsRegister.configurationResetKpiTime();

        final Gauge actual = this.registry.find(CustomMetrics.CONFIGURATION_RESET_KPI_TIME_SECONDS.getMetricName()).gauge();
        assertNotNull(actual);

        expected.set(Math.PI);
        assertEquals(Math.PI, actual.value());
    }

    @Test
    void configurationResetAugErrorCounter() {

        final AtomicLong expected = this.metricsRegister.configurationResetAugErrorCounter();

        final Gauge actual = this.registry.find(CustomMetrics.CONFIGURATION_RESET_AUG_ERRORS.getMetricName()).gauge();
        assertNotNull(actual);

        expected.set(1L);
        assertEquals(expected.doubleValue(), actual.value());
    }

    @Test
    void configurationResetAugTime() {

        final AtomicDouble expected = this.metricsRegister.configurationResetAugTime();

        final Gauge actual = this.registry.find(CustomMetrics.CONFIGURATION_RESET_AUG_TIME_SECONDS.getMetricName()).gauge();
        assertNotNull(actual);

        expected.set(Math.PI);
        assertEquals(Math.PI, actual.value());
    }

    @Test
    void configurationResetTotalTime() {

        final AtomicDouble expected = this.metricsRegister.configurationResetTotalTime();

        final Gauge actual = this.registry.find(CustomMetrics.CONFIGURATION_RESET_TOTAL_TIME_SECONDS.getMetricName()).gauge();
        assertNotNull(actual);

        expected.set(Math.PI);
        assertEquals(Math.PI, actual.value());
    }

    @Test
    void configurationResetTotalErrors() {

        final AtomicLong expected = this.metricsRegister.configurationResetTotalErrors();

        final Gauge actual = this.registry.find(CustomMetrics.CONFIGURATION_RESET_TOTAL_ERRORS.getMetricName()).gauge();

        assertNotNull(actual);

        expected.set(1L);
        assertEquals(expected.doubleValue(), actual.value());
    }

    @Test
    void configurationResetIndexErrorCounter() {

        final AtomicLong expected = this.metricsRegister.configurationResetIndexErrorCounter();

        final Gauge actual = this.registry.find(CustomMetrics.CONFIGURATION_RESET_INDEX_ERRORS.getMetricName()).gauge();

        assertNotNull(actual);

        expected.set(1L);
        assertEquals(expected.doubleValue(), actual.value());
    }

    @Test
    void configurationResetIndexTime() {

        final AtomicDouble expected = this.metricsRegister.configurationResetIndexTime();

        final Gauge actual = this.registry.find(CustomMetrics.CONFIGURATION_RESET_INDEX_TIME_SECONDS.getMetricName()).gauge();
        assertNotNull(actual);

        expected.set(Math.PI);
        assertEquals(Math.PI, actual.value());
    }
}
