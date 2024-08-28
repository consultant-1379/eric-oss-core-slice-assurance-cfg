/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.handler.validation.augmentation;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.ericsson.oss.air.csac.configuration.schema.InputSchemaProvider;
import com.ericsson.oss.air.csac.configuration.augmentation.AugmentationConfiguration;
import com.ericsson.oss.air.csac.configuration.augmentation.AugmentationProperties;
import com.ericsson.oss.air.csac.model.AugmentationDefinition;
import com.ericsson.oss.air.csac.model.AugmentationRule;
import com.ericsson.oss.air.csac.model.AugmentationRuleField;
import com.ericsson.oss.air.csac.model.pmschema.PmSchema;
import com.ericsson.oss.air.csac.service.augmentation.AugmentationProvisioningService;
import com.ericsson.oss.air.exception.CsacValidationException;
import lombok.RequiredArgsConstructor;
import org.apache.avro.Schema;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Validates augmentation configuration using external services.
 */
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(value = "validation.external.enabled",
                       havingValue = "true")
public class LiveAugmentationValidator implements AugmentationValidator {

    private final AugmentationProvisioningService augmentationProvisioningService;

    private final AugmentationProperties augmentationProperties;

    private final AugmentationConfiguration augmentationConfiguration;

    private final InputSchemaProvider inputSchemaProvider;

    /**
     * Checks the augmentation configuration in the application configuration, if any.  Augmentation configuration can be autowired via an
     * AugmentationProperties bean.
     *
     * @throws CsacValidationException
     *         if the augmentation properties are invalid.
     */
    public void validateAppConfig() {

        final Map<String, String> ardqConfigMap = this.augmentationProperties.getArdqConfig();

        if (this.augmentationProperties.isEnabled() || Objects.nonNull(ardqConfigMap)) {
            ardqConfigMap.values().stream().forEach(this.augmentationProvisioningService::checkUrl);
        }
    }

    /**
     * Performs the augmentation context validation on the specified AugmentationDefinition.
     *
     * @param augDefinition
     *         AugmentationDefinition to validate.
     * @throws CsacValidationException
     *         if the definition is invalid.
     */
    @Override
    public void validate(final AugmentationDefinition augDefinition) {

        this.augmentationProvisioningService.checkArdqType(augDefinition);

        if (!this.augmentationConfiguration.isDryRunModeEnabled()) {
            this.checkFields(augDefinition);
        }
    }

    /*
     * (non-javadoc)
     *
     * Checks the output and input fields in the provided augmentation definition
     * to ensure that the output field does not exist in the input schema and that
     * the input fields do exist in the input schema.  The schema reference is
     * specified in the augmentation definition.
     *
     * @param augDefinition augmentation definition to verify
     * @throws CsacValidationException if any fields are invalid
     */
    protected void checkFields(final AugmentationDefinition augDefinition) {

        final List<AugmentationRule> augmentationRuleList = augDefinition.getAugmentationRules();

        //iterates over each rule in augmentation definition
        for (final AugmentationRule augmentationRule : augmentationRuleList) {

            //iterated over each input schema reference in a rule
            for (final String inputSchemaReference : augmentationRule.getAllInputSchemas()) {

                final PmSchema schema = this.inputSchemaProvider.getSchema(inputSchemaReference);
                final Set<String> schemaFields = schema.getFields().stream().map(Schema.Field::name).collect(Collectors.toSet());

                final List<AugmentationRuleField> augmentationRuleFieldList = augmentationRule.getFields();

                //iterates over each field spec in a rule
                for (final AugmentationRuleField augmentationRuleField : augmentationRuleFieldList) {

                    final List<String> inputFields = augmentationRuleField.getInputFields();

                    //iterates over each output field in field spec
                    for (final String outputField : augmentationRuleField.getAllOutputFields()) {
                        this.validateInputFields(inputFields, schemaFields);
                        this.validateOutputField(outputField, schemaFields);
                    }
                }
            }
        }
    }

    private void validateInputFields(final List<String> inputFields, final Set<String> schemaFields) {

        inputFields.stream().forEach(inputField -> {

            if (!schemaFields.contains(inputField)) {

                final String errorMsg = String.format("Input field %s does not exist in the schema fields list %s", inputField, schemaFields);
                throw new CsacValidationException(errorMsg);
            }
        });
    }

    private void validateOutputField(final String outputField, final Set<String> schemaFields) {

        if (schemaFields.contains(outputField)) {
            final String errorMsg = String.format("Output field %s already exist in the schema fields list %s.", outputField, schemaFields);
            throw new CsacValidationException(errorMsg);
        }
    }
}
