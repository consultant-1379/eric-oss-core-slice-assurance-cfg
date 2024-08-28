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

import static com.ericsson.oss.air.csac.configuration.metrics.CustomMetrics.CUSTOM_METRIC_TAG;

import java.util.concurrent.atomic.AtomicLong;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.ericsson.oss.air.csac.repository.AugmentationDefinitionDAO;
import com.ericsson.oss.air.csac.repository.DeployedIndexDefinitionDao;
import com.ericsson.oss.air.csac.repository.DeployedKpiDefDAO;
import com.ericsson.oss.air.csac.repository.DeployedProfileDAO;
import com.ericsson.oss.air.csac.repository.EffectiveAugmentationDAO;
import com.ericsson.oss.air.csac.repository.KPIDefinitionDAO;
import com.ericsson.oss.air.csac.repository.PMDefinitionDAO;
import com.ericsson.oss.air.util.concurrent.AtomicDouble;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Metrics;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Registers custom CSAC metrics in the Micrometer MeterRegistry.
 */
@Configuration
@RequiredArgsConstructor
@Getter
public class CustomMetricsRegistry {

    private final MeterRegistry registry;

    private final GaugeDaoFunctionFactory gaugeDaoFunctionFactory;

    /**
     * Register kpi instance runtime error counter.
     *
     * @return a Counter
     */
    public static Counter registerKpiRuntimeErrorCount() {
        return Metrics.counter(CustomMetrics.RUNTIME_KPI_INSTANCES_ERRORS.getMetricName(), CUSTOM_METRIC_TAG,
                               CustomMetrics.RUNTIME_KPI_INSTANCES_ERRORS.getMetricName());
    }

    /**
     * Registers a metric to display the count of resource files failed to load.
     *
     * @return a Counter to count the failures in loading resource files.
     */
    public static Counter registerResourceFileLoadErrorCount() {
        return Metrics.counter(CustomMetrics.CSAC_FILE_LOAD_ERRORS.getMetricName(), CUSTOM_METRIC_TAG,
                               CustomMetrics.CSAC_FILE_LOAD_ERRORS.getMetricName());
    }

    /**
     * Registers a metric to display the count of PM validation failures.
     *
     * @return a Counter to count the failures for PM validation.
     */
    public static Counter registerPMValidationErrorCount() {
        return Metrics.counter(CustomMetrics.DICTIONARY_PM_DEFS_ERROR.getMetricName(), CUSTOM_METRIC_TAG,
                               CustomMetrics.DICTIONARY_PM_DEFS_ERROR.getMetricName());
    }

    /**
     * Registers a metric to display the count of KPI validation failures.
     *
     * @return a Counter to count the failures for KPI validation.
     */
    public static Counter registerKPIValidationErrorCount() {
        return Metrics.counter(CustomMetrics.DICTIONARY_KPI_DEFS_ERROR.getMetricName(), CUSTOM_METRIC_TAG,
                               CustomMetrics.DICTIONARY_KPI_DEFS_ERROR.getMetricName());
    }

    /**
     * Registers a metric to display the count of augmentation provisioning failures.
     *
     * @return a Counter to count the failures for augmentation provisioning.
     */
    public static Counter registerRuntimeAugmentationErrorCount() {
        return Metrics.counter(CustomMetrics.RUNTIME_AUGMENTATION_ERRORS.getMetricName(), CUSTOM_METRIC_TAG,
                               CustomMetrics.RUNTIME_AUGMENTATION_ERRORS.getMetricName());
    }

    /**
     * Registers a metric to display the count of index provisioning failures.
     *
     * @return a Counter to count the failures for index provisioning.
     */
    public static Counter registerRuntimeIndexErrorCount() {
        return Metrics.counter(CustomMetrics.RUNTIME_INDEX_ERRORS.getMetricName(), CUSTOM_METRIC_TAG,
                               CustomMetrics.RUNTIME_INDEX_ERRORS.getMetricName());
    }

    /**
     * Register the metric to display the count of PM Definitions in the Data Dictionary
     *
     * @param pmDefinitionDAO The PMDefinitionDAO object for which the count must be registered
     */
    @Autowired
    void registerPMCount(final PMDefinitionDAO pmDefinitionDAO) {
        Gauge.builder(CustomMetrics.DICTIONARY_COUNT_PM_DEFS.getMetricName(), pmDefinitionDAO,
                      this.gaugeDaoFunctionFactory.createGaugeDaoFunction(pmDefinitionDAO::totalPMDefinitions))
                .tag(CUSTOM_METRIC_TAG, CustomMetrics.DICTIONARY_COUNT_PM_DEFS.getMetricName()).register(this.registry);
    }

    /**
     * Register the metric to display the count of KPI Definitions in the Data Dictionary
     *
     * @param kpiDefinitionDAO The KPIDefinitionDAO object for which the count must be registered
     */
    @Autowired
    void registerKPICount(final KPIDefinitionDAO kpiDefinitionDAO) {
        Gauge.builder(CustomMetrics.DICTIONARY_COUNT_KPI_DEFS.getMetricName(), kpiDefinitionDAO,
                      this.gaugeDaoFunctionFactory.createGaugeDaoFunction(kpiDefinitionDAO::totalKPIDefinitions))
                .tag(CUSTOM_METRIC_TAG, CustomMetrics.DICTIONARY_COUNT_KPI_DEFS.getMetricName()).register(this.registry);
    }

    /**
     * Register the metric to display the count of deployed profile definitions in the run time data resource
     *
     * @param deployedProfileDAO The DeployedProfileDAO object for which the count must be registered
     */
    @Autowired
    void registerDeployedProfileCount(final DeployedProfileDAO deployedProfileDAO) {
        Gauge.builder(CustomMetrics.DEPLOYED_COUNT_PROFILE_DEFS.getMetricName(), deployedProfileDAO,
                      this.gaugeDaoFunctionFactory.createGaugeDaoFunction(deployedProfileDAO::totalProfileDefinitions))
                .tag(CUSTOM_METRIC_TAG, CustomMetrics.DEPLOYED_COUNT_PROFILE_DEFS.getMetricName()).register(this.registry);
    }

    /**
     * Register the metric to display the count of deployed KPI definitions in the run time data resource
     *
     * @param deployedKpiDefDAO The DeployedKpiDefDAO object for which the count must be registered
     */
    @Autowired
    void registerDeployedKPICount(final DeployedKpiDefDAO deployedKpiDefDAO) {
        Gauge.builder(CustomMetrics.DEPLOYED_COUNT_KPI_INSTANCES.getMetricName(), deployedKpiDefDAO,
                      this.gaugeDaoFunctionFactory.createGaugeDaoFunction(deployedKpiDefDAO::totalDeployedKpiDefinitions))
                .tag(CUSTOM_METRIC_TAG, CustomMetrics.DEPLOYED_COUNT_KPI_INSTANCES.getMetricName()).register(this.registry);
    }

    /**
     * Register the metric to display the time taken for CSAC to complete provisioning of the AAS
     *
     * @return {@link AtomicDouble} elapsed time
     */
    @Bean
    public AtomicDouble provisioningAasTime() {

        return this.registerTimers(CustomMetrics.PROVISIONING_AAS_TIME_SECONDS.getMetricName());
    }

    /**
     * Register the metric to display the time taken for CSAC to complete provisioning of the PMSC
     *
     * @return {@link AtomicDouble} elapsed time
     * @deprecated since 2.2.0.  Use {@link #provisioningKpiTime()}} instead.
     */
    @Bean
    @Deprecated(since = "2.2.0")
    @SuppressWarnings("java:S1133")
    public AtomicDouble provisioningPmscTime() {

        return this.registerTimers(CustomMetrics.PROVISIONING_PMSC_TIME_SECONDS.getMetricName());
    }

    /**
     * Register the metric to display the time taken for CSAC to complete KPI provisioning.
     *
     * @return {@link AtomicDouble} elapsed time
     */
    @Bean
    public AtomicDouble provisioningKpiTime() {

        return this.registerTimers(CustomMetrics.PROVISIONING_KPI_TIME_SECONDS.getMetricName());
    }

    /**
     * Register the metric to display the time taken for CSAC to complete provisioning of the Indexer
     *
     * @return {@link AtomicDouble} elapsed time
     */
    @Bean
    public AtomicDouble provisioningIndexTime() {

        return this.registerTimers(CustomMetrics.PROVISIONING_INDEX_TIME_SECONDS.getMetricName());
    }

    /**
     * Register the metric to display the time taken for CSAC to complete provisioning of all target services
     *
     * @return {@link AtomicDouble} elapsed time
     */
    @Bean
    public AtomicDouble provisioningTotalTime() {

        return this.registerTimers(CustomMetrics.PROVISIONING_TOTAL_TIME_SECONDS.getMetricName());
    }

    /**
     * Register the metric to display the number of augmentation definitions currently in the data dictionary
     *
     * @param augmentationDefinitionDAO The AugmentationDefinitionDAO object for which the count must be registered
     */
    @Autowired
    void registerAugmentationCount(final AugmentationDefinitionDAO augmentationDefinitionDAO) {
        Gauge.builder(CustomMetrics.DICTIONARY_COUNT_AUG_DEFS.getMetricName(), augmentationDefinitionDAO,
                      this.gaugeDaoFunctionFactory.createGaugeDaoFunction(augmentationDefinitionDAO::totalAugmentationDefinitions))
                .tag(CUSTOM_METRIC_TAG, CustomMetrics.DICTIONARY_COUNT_AUG_DEFS.getMetricName()).register(this.registry);
    }

    /**
     * Register the metric to display the number of effective augmentation definitions currently in the runtime data store
     *
     * @param effectiveAugmentationDAO The EffectiveAugmentationDAO object for which the count must be registered
     */
    @Autowired
    void registerDeployedAugmentationCount(final EffectiveAugmentationDAO effectiveAugmentationDAO) {
        Gauge.builder(CustomMetrics.DEPLOYED_COUNT_AUG_DEFS.getMetricName(), effectiveAugmentationDAO,
                      this.gaugeDaoFunctionFactory.createGaugeDaoFunction(effectiveAugmentationDAO::totalEffectiveAugmentations))
                .tag(CUSTOM_METRIC_TAG, CustomMetrics.DEPLOYED_COUNT_AUG_DEFS.getMetricName()).register(this.registry);
    }

    /**
     * Register the metric to display the number of index definitions currently in the runtime data store
     *
     * @param deployedIndexDefinitionDao The DeployedIndexDefinitionDao object for which the count must be registered
     */
    @Autowired
    void registerDeployedIndexCount(final DeployedIndexDefinitionDao deployedIndexDefinitionDao) {
        Gauge.builder(CustomMetrics.DEPLOYED_COUNT_INDEX_DEFS.getMetricName(), deployedIndexDefinitionDao,
                      this.gaugeDaoFunctionFactory.createGaugeDaoFunction(deployedIndexDefinitionDao::count))
                .tag(CUSTOM_METRIC_TAG, CustomMetrics.DEPLOYED_COUNT_INDEX_DEFS.getMetricName()).register(this.registry);
    }

    /**
     * Registers a metric to report the count of runtime configuration consistency check errors.
     *
     * @return {@link AtomicLong}  error event counter
     */
    @Bean
    public AtomicLong registerRuntimeConfigConsistencyCheckError() {

        return registerErrorCounters(CustomMetrics.RUNTIME_CONFIG_CONSISTENCY_CHECK_ERRORS.getMetricName());
    }

    @Bean
    public AtomicLong configurationResetDbErrorCounter() {

        return registerErrorCounters(CustomMetrics.CONFIGURATION_RESET_DB_ERRORS.getMetricName());
    }

    @Bean
    public AtomicDouble configurationResetDbTime() {

        return this.registerTimers(CustomMetrics.CONFIGURATION_RESET_DB_TIME_SECONDS.getMetricName());
    }

    /**
     * Registers the metric to report the total number of errors encountered when performing this reset operation
     *
     * @return {@link AtomicLong} error counter
     */
    @Bean
    public AtomicLong configurationResetKpiErrorCounter() {

        return registerErrorCounters(CustomMetrics.CONFIGURATION_RESET_KPI_ERRORS.getMetricName());
    }

    /**
     * Registers the metric to report the elapsed time taken when performing this reset operation
     *
     * @return {@link AtomicDouble} elapsed time
     */
    @Bean
    public AtomicDouble configurationResetKpiTime() {

        return this.registerTimers(CustomMetrics.CONFIGURATION_RESET_KPI_TIME_SECONDS.getMetricName());
    }

    /**
     * Registers the metric to report the total number of errors encountered when performing the augmentation reset operation
     *
     * @return {@link AtomicLong} error counter
     */
    @Bean
    public AtomicLong configurationResetAugErrorCounter() {

        return registerErrorCounters(CustomMetrics.CONFIGURATION_RESET_AUG_ERRORS.getMetricName());
    }

    /**
     * Registers the metric to report the elapsed time taken when performing the augmentation reset operation
     *
     * @return {@link AtomicDouble} elapsed time
     */
    @Bean
    public AtomicDouble configurationResetAugTime() {

        return this.registerTimers(CustomMetrics.CONFIGURATION_RESET_AUG_TIME_SECONDS.getMetricName());
    }

    /**
     * Registers the metric to report the total number of errors encountered when performing the indexer reset operation
     *
     * @return {@link AtomicLong} error counter
     */
    @Bean
    public AtomicLong configurationResetIndexErrorCounter() {

        return registerErrorCounters(CustomMetrics.CONFIGURATION_RESET_INDEX_ERRORS.getMetricName());
    }

    /**
     * Registers the metric to report the elapsed time taken when performing the indexer reset operation
     *
     * @return {@link AtomicDouble} elapsed time
     */
    @Bean
    public AtomicDouble configurationResetIndexTime() {

        return this.registerTimers(CustomMetrics.CONFIGURATION_RESET_INDEX_TIME_SECONDS.getMetricName());
    }

    /**
     * Registers the metric to report the elapsed time taken when performing the complete reset operation
     *
     * @return {@link AtomicDouble} elapsed time
     */
    @Bean
    public AtomicDouble configurationResetTotalTime() {

        return this.registerTimers(CustomMetrics.CONFIGURATION_RESET_TOTAL_TIME_SECONDS.getMetricName());
    }

    /**
     * Registers the metric to report the total number of errors encountered when performing the complete reset operation.
     *
     * @return {@link AtomicLong} error counter
     */
    @Bean
    public AtomicLong configurationResetTotalErrors() {

        return registerErrorCounters(CustomMetrics.CONFIGURATION_RESET_TOTAL_ERRORS.getMetricName());
    }

    private AtomicDouble registerTimers(final String metricName) {
        final AtomicDouble gaugeValue = new AtomicDouble();

        Gauge.builder(metricName, gaugeValue, AtomicDouble::get)
                .tag(CUSTOM_METRIC_TAG, metricName)
                .register(this.registry);

        return gaugeValue;
    }

    private AtomicLong registerErrorCounters(final String metricName) {
        final AtomicLong gaugeValue = new AtomicLong();

        Gauge.builder(metricName, gaugeValue, AtomicLong::get)
                .tag(CUSTOM_METRIC_TAG, metricName)
                .register(this.registry);

        return gaugeValue;
    }
}
