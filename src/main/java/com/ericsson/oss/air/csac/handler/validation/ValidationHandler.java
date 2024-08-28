/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.handler.validation;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.ericsson.oss.air.csac.configuration.metrics.CustomMetricsRegistry;
import com.ericsson.oss.air.csac.handler.validation.augmentation.AugmentationValidator;
import com.ericsson.oss.air.csac.handler.validation.pmsc.PmscConfigurationValidator;
import com.ericsson.oss.air.csac.model.PMDefinition;
import com.ericsson.oss.air.csac.model.ResourceSubmission;
import com.ericsson.oss.air.exception.CsacValidationException;
import io.micrometer.core.instrument.Counter;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * This handler provides validation operations, primarily for context validation of dictionary resources.
 */
@Component
public class ValidationHandler {

    @Autowired
    private PMValidator pmValidator;

    @Autowired
    private KPIContextValidator kpiContextValidator;

    @Autowired
    private AugmentationValidator augmentationValidator;

    @Autowired
    private ProfileContextValidator profileContextValidator;

    @Autowired
    private PmscConfigurationValidator pmscConfigurationValidator;

    private Validator validator;

    private static final Counter pmValidationErrorCounter = CustomMetricsRegistry.registerPMValidationErrorCount();
    private static final Counter kpiValidationErrorCounter = CustomMetricsRegistry.registerKPIValidationErrorCount();

    /**
     * Sets the {@code Validator} for the {@code ValidationHandler} class.
     *
     * @param validator validator to set for this class.
     */
    @Autowired
    public void setValidator(final Validator validator) {
        this.validator = validator;
    }

    /**
     * Checks the application configuration properties.  This method delegates to validators specific to the properties being validated.
     *
     * @throws CsacValidationException if the application properties are invalid.
     */
    public void validateAppConfig() {
        this.augmentationValidator.validateAppConfig();
        this.pmscConfigurationValidator.validateAppConfig();
    }

    /**
     * Validates the PM definitions in the submitted resources after bean validation. Returns a map keyed on PM schema names each with a list of their
     * corresponding PM definitions.
     *
     * @param submittedPMDefinitions the submitted resources
     * @return a map keyed on PM schema names each with a list of their corresponding PM definitions
     */
    public Map<String, List<PMDefinition>> getValidPMDefinitions(final List<PMDefinition> submittedPMDefinitions) {
        try {
            return this.pmValidator.getValidPMDefinitions(submittedPMDefinitions);
        } catch (final RuntimeException e) {
            pmValidationErrorCounter.increment();
            throw e;
        }
    }

    /**
     * Validates the context of KPI definitions in the submitted resources after bean validation
     *
     * @param resourceSubmission the submitted resources
     */
    public void validateKPIDefinitions(final ResourceSubmission resourceSubmission) {
        try {
            this.kpiContextValidator.validateKPIDefinitions(resourceSubmission);
        } catch (final RuntimeException e) {
            kpiValidationErrorCounter.increment();
            throw e;
        }
    }

    /**
     * Validates the context of Profile definitions in the submitted resources after bean validation
     *
     * @param resourceSubmission the submitted resources
     */
    public void validateProfileDefinitions(final ResourceSubmission resourceSubmission) {
        this.profileContextValidator.validateProfileDefinitions(resourceSubmission);
    }

    /**
     * Validates the context of the augmentation definitions in the provided resource submission.
     *
     * @param resourceSubmission ResourceSubmission containing the augmentation definitions to validate.
     * @throws CsacValidationException if the context validation fails.
     */
    public void validateAugmentations(final ResourceSubmission resourceSubmission) {

        resourceSubmission.getAugmentationDefinitions().stream().
                forEach(this.augmentationValidator::validate);
    }

    /**
     * Static bean validation method.
     *
     * @param entity entity bean to validate
     * @param <T>    entity bean type
     */
    public <T> void checkEntity(final T entity) {

        if (Objects.isNull(entity)) {
            throw new CsacValidationException("Invalid entity: may not be null");
        }

        final Set<ConstraintViolation<T>> violations = validator.validate(entity);

        if (!violations.isEmpty()) {
            final List<String> violationList = violations.stream().map(ConstraintViolation::getMessage).toList();
            throw new CsacValidationException("Invalid " + entity.getClass().getName() + ": " + violationList);
        }
    }

}
