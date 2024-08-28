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

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

/**
 * Enum defining the custom CSAC Metric Names for Data Dictionary item and run time data counts.  At runtime, all metric names will be prefixed with
 * the CSAC service acronym 'csac'.  For example '{@code pm_defs_dict_int_total}' will appear as '{@code  csac_pm_defs_dict_int_total}' in the
 * {@code /actuator/prometheus} output.
 * <p>
 * Custom naming conventions:
 * <ul>
 * <li>All metrics have the units appended to the name</li>
 * <li>For metrics represent a cumulative count, e.g. total number of PM definitions in the data dictionary, word 'total' is appended rather than the units</li>
 * <li>Maximum length of a custom metric name is 50 characters</li>
 * </ul>
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum CustomMetrics {

    DICTIONARY_COUNT_PM_DEFS("pm_defs_dict_int_total"),
    DICTIONARY_COUNT_KPI_DEFS("kpi_defs_dict_int_total"),
    DEPLOYED_COUNT_PROFILE_DEFS("deployed_profile_defs_int_total"),
    DEPLOYED_COUNT_KPI_INSTANCES("deployed_kpi_instances_int_total"),
    RUNTIME_AUGMENTATION_ERRORS("runtime_augmentation_errors_total"),
    RUNTIME_INDEX_ERRORS("runtime_index_instance_errors_total"),
    RUNTIME_KPI_INSTANCES_ERRORS("runtime_kpi_instance_errors_total"),
    RUNTIME_CONFIG_CONSISTENCY_CHECK_ERRORS("runtime_configuration_consistency_check_errors_total"),
    PROVISIONING_AAS_TIME_SECONDS("provisioning_aas_time_seconds"),
    PROVISIONING_INDEX_TIME_SECONDS("provisioning_index_time_seconds"),
    PROVISIONING_KPI_TIME_SECONDS("provisioning_kpi_time_seconds"),
    PROVISIONING_PMSC_TIME_SECONDS("provisioning_pmsc_time_seconds"),
    PROVISIONING_TOTAL_TIME_SECONDS("provisioning_total_time_seconds"),
    CSAC_FILE_LOAD_ERRORS("file_load_errors_total"),
    DICTIONARY_KPI_DEFS_ERROR("dictionary_kpi_definition_errors_total"),
    DICTIONARY_PM_DEFS_ERROR("dictionary_pm_definition_errors_total"),
    DICTIONARY_COUNT_AUG_DEFS("augmentation_defs_dict_int_total"),
    DEPLOYED_COUNT_AUG_DEFS("deployed_augmentation_defs_int_total"),
    DEPLOYED_COUNT_INDEX_DEFS("deployed_index_instances_int_total"),
    CONFIGURATION_RESET_DB_TIME_SECONDS("configuration_reset_db_time_seconds"),
    CONFIGURATION_RESET_DB_ERRORS("configuration_reset_db_errors_total"),
    CONFIGURATION_RESET_AUG_TIME_SECONDS("configuration_reset_augmentation_time_seconds"),
    CONFIGURATION_RESET_AUG_ERRORS("configuration_reset_augmentation_errors_total"),
    CONFIGURATION_RESET_KPI_ERRORS("configuration_reset_kpi_errors_total"),
    CONFIGURATION_RESET_KPI_TIME_SECONDS("configuration_reset_kpi_time_seconds"),
    CONFIGURATION_RESET_TOTAL_ERRORS("configuration_reset_errors_total"),
    CONFIGURATION_RESET_TOTAL_TIME_SECONDS("configuration_reset_total_time_seconds"),
    CONFIGURATION_RESET_INDEX_ERRORS("configuration_reset_index_errors_total"),
    CONFIGURATION_RESET_INDEX_TIME_SECONDS("configuration_reset_index_time_seconds");

    public static final String CUSTOM_METRIC_TAG = "csac_custom_metric";

    private final String metricName;

    /**
     * Get the metric name
     */
    public String getMetricName() {
        return this.metricName;
    }
}
