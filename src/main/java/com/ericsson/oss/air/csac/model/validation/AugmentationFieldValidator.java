/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.model.validation;

import com.ericsson.oss.air.csac.model.AugmentationRule;
import com.ericsson.oss.air.csac.model.AugmentationRuleField;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

/**
 * Custom bean validator for {@link com.ericsson.oss.air.csac.model.AugmentationRule} and {@link com.ericsson.oss.air.csac.model.AugmentationRuleField} to check following conditions based on the bean the constraint is defined
 *
 * <ul>
 *     <li>Either of `input_schema` or `input_schemas` fields must exist in {@link com.ericsson.oss.air.csac.model.AugmentationRule}</li>
 *     <li>Either of `output` or `output_fields` fields must exist in {@link com.ericsson.oss.air.csac.model.AugmentationRuleField}</li>
 * </ul>
 */
public class AugmentationFieldValidator implements ConstraintValidator<AugmentationFieldConstraint, Object> {

    @Override
    public boolean isValid(final Object value, final ConstraintValidatorContext constraintValidatorContext) {

        if (value instanceof AugmentationRuleField) {
            final AugmentationRuleField ruleField = (AugmentationRuleField) value;
            return !ObjectUtils.isEmpty(ruleField.getOutput()) || !CollectionUtils.isEmpty(ruleField.getOutputFields());
        }

        if (value instanceof AugmentationRule) {
            final AugmentationRule rule = (AugmentationRule) value;
            return !ObjectUtils.isEmpty(rule.getInputSchemaReference()) || !CollectionUtils.isEmpty(rule.getInputSchemas());
        }

        return false;
    }

}
