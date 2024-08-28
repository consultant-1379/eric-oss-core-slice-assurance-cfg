/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.handler.validation;

import com.ericsson.oss.air.csac.model.InputMetric;
import com.ericsson.oss.air.csac.model.KPIDefinition;
import com.ericsson.oss.air.csac.model.PMDefinition;
import com.ericsson.oss.air.csac.model.ResourceSubmission;
import com.ericsson.oss.air.csac.repository.KPIDefinitionDAO;
import com.ericsson.oss.air.csac.repository.PMDefinitionDAO;
import com.ericsson.oss.air.exception.CsacValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@Slf4j
public class KPIContextValidator implements CsacValidator<ResourceSubmission> {
    @Autowired
    KPIDefinitionDAO kpiDefinitionDAO;

    @Autowired
    PMDefinitionDAO pmDefinitionDAO;

    /**
     * A KPI is complex KPI if the input metrics are type KPI
     *
     * @param kpiDefinition
     *         the kpi definition
     * @return true if the input metric is type KPI
     */
    public static boolean isComplexKpi(final KPIDefinition kpiDefinition) {
        return kpiDefinition.getInputMetrics().get(0).getType() == InputMetric.Type.KPI;
    }

    /**
     * Validates the context of KPI definitions after bean validation
     *
     * @param resourceSubmission
     *         the submitted resource to validate
     */
    @Override
    public void validate(final ResourceSubmission resourceSubmission) {
        this.validateKPIDefinitions(resourceSubmission);
    }

    /**
     * Validate a kpi definition only references existing pm or kpi definitions as input metrics
     *
     * @param resourceSubmission
     *         the current resource submission
     */
    public void validateKPIDefinitions(final ResourceSubmission resourceSubmission) {

        final List<KPIDefinition> kpiDefinitions = resourceSubmission.getKpiDefs();

        kpiDefinitions.forEach(kpiDefinition -> {
            if (!isValidInputMetrics(kpiDefinition)) {
                final String errorMessage = "Input metrics have different type for this KPI definition: "
                        + kpiDefinition.getName();
                log.error(errorMessage);
                throw new CsacValidationException(errorMessage);
            }
            log.debug("Input metrics were validated successfully for KPI definition[name]: '{}' with type: '{}'", kpiDefinition.getName(),
                    kpiDefinition.getInputMetrics().get(0).getType());
            checkKPIContext(kpiDefinition, resourceSubmission);
        });
    }

    /**
     * Validate a kpi definition only references existing pm or kpi definitions as input metrics in current resource submission
     *
     * @param resourceSubmission
     *         the current resource submission
     * @return true if the KPI only references existing pm or kpi definitions as input metrics
     */
    private void checkKPIContext(final KPIDefinition kpi, final ResourceSubmission resourceSubmission) {
        if (isComplexKpi(kpi)) {
            checkComplexInputMetricContext(kpi, resourceSubmission);
        } else {
            checkSimpleInputMetricContext(kpi, resourceSubmission);
        }
    }

    /**
     * Validate a complex KPI definition only references existing KPI definition in input metrics either in current resource submission or dictionary
     *
     * @param kpi
     *         the KPI definition that needs to be validated
     * @param resourceSubmission
     */
    void checkComplexInputMetricContext(final KPIDefinition kpi, final ResourceSubmission resourceSubmission) {

        final Set<String> kpiDefNames = resourceSubmission.getKpiDefs().stream()
                .map(KPIDefinition::getName)
                .collect(Collectors.toSet());
        final Set<String> kpiNamesInDictionary = this.kpiDefinitionDAO.getAllKpiDefNames();
        kpiDefNames.addAll(kpiNamesInDictionary);

        kpi.getInputMetrics().forEach(inputMetric -> checkInputMetricId(inputMetric, kpiDefNames, kpi.getName()));
    }

    /**
     * Validate a simple KPI definition only references existing PM definition in input metrics either in current resource submission or the data
     * dictionary
     *
     * @param kpi
     *         the kpi definition that needs to be validated
     * @param resourceSubmission
     */
    void checkSimpleInputMetricContext(final KPIDefinition kpi, final ResourceSubmission resourceSubmission) {
        final Set<String> pmDefNames = resourceSubmission.getPmDefs().stream().map(PMDefinition::getName).collect(Collectors.toSet());
        final Set<String> existingPmDefNames = this.pmDefinitionDAO.getAllPmDefNames();
        pmDefNames.addAll(existingPmDefNames);
        kpi.getInputMetrics().forEach(inputMetric -> checkInputMetricId(inputMetric, pmDefNames, kpi.getName()));
    }

    /**
     * Validate a KPI definition only contains same type in input metrics
     *
     * @param kpiDefinition
     *         the kpi definition
     * @return true if input metrics are the same type
     */
    private boolean isValidInputMetrics(final KPIDefinition kpiDefinition) {

        final List<InputMetric> inputMetrics = kpiDefinition.getInputMetrics();
        final List<InputMetric.Type> types = new ArrayList<>();
        inputMetrics.forEach(inputMetric -> types.add(inputMetric.getType()));
        return types.stream().allMatch(t -> t.equals(types.get(0)));
    }

    /**
     * Validate the kpi only references existing PM or KPI definitions based on the input metric ID
     *
     * @param inputMetric
     *         the input metric contains reference
     * @param defNames
     *         all the definition names in resource submission and dictionary
     * @param kpiName
     *         the current kpi definition name
     * @throws CsacValidationException
     */
    private void checkInputMetricId(final InputMetric inputMetric, final Set<String> defNames, final String kpiName)
            throws CsacValidationException {
        if (!defNames.contains(inputMetric.getId())) {
            // throw exception only when the kpi does not exist neither in current resource submission nor dictionary
            final String errorMessage = "This KPI definition: " + kpiName +
                    " references non existing " + inputMetric.getType() + " definitions as input metric: " + inputMetric.getId();
            log.error(errorMessage);
            throw new CsacValidationException(errorMessage);
        }
    }

}
