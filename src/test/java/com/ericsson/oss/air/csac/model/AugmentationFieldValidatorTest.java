/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import com.ericsson.oss.air.util.validation.config.ValidationConfiguration;
import jakarta.validation.ConstraintViolation;
import org.junit.jupiter.api.Test;

public class AugmentationFieldValidatorTest {

    @Test
    public void isValid_validAugmentationRuleField() {
        //AugmentationRuleField with `output_fields` field
        final AugmentationRuleField field1 = AugmentationRuleField.builder()
                .outputFields(List.of("output1", "output2"))
                .inputFields(List.of("input1", "input2"))
                .build();

        final List<ConstraintViolation<?>> violations1 = new ArrayList<>();
        violations1.addAll(new ValidationConfiguration().getValidator().validate(field1));

        assertEquals(0, violations1.size());

        //AugmentationRuleField with `output` field
        final AugmentationRuleField field2 = AugmentationRuleField.builder()
                .output("output1")
                .inputFields(List.of("input1", "input2"))
                .build();

        final List<ConstraintViolation<?>> violations2 = new ArrayList<>();
        violations2.addAll(new ValidationConfiguration().getValidator().validate(field2));

        assertEquals(0, violations2.size());

        //AugmentationRuleField with `output_fields` and `output` field
        final AugmentationRuleField field3 = AugmentationRuleField.builder()
                .output("output1")
                .outputFields(List.of("output2", "output3"))
                .inputFields(List.of("input1", "input2"))
                .build();

        final List<ConstraintViolation<?>> violations3 = new ArrayList<>();
        violations3.addAll(new ValidationConfiguration().getValidator().validate(field3));

        assertEquals(0, violations3.size());
    }

    @Test
    public void isValid_inValidAugmentationRuleField() {
        //AugmentationRuleField without `output_fields` or `output` fields
        final AugmentationRuleField field = AugmentationRuleField.builder()
                .inputFields(List.of("input1", "input2"))
                .build();

        final List<ConstraintViolation<?>> violations = new ArrayList<>();
        violations.addAll(new ValidationConfiguration().getValidator().validate(field));

        assertEquals(1, violations.size());
        assertEquals("Missing required field. Either of the fields 'output' or 'output_fields' should exist in augmentation",
                violations.get(0).getMessage());
    }

    @Test
    public void isValid_validAugmentationRule() {
        final AugmentationRuleField ruleField = AugmentationRuleField.builder()
                .output("output1")
                .inputFields(List.of("input1", "input2"))
                .build();
        //AugmentationRule with `input_schema` field
        final AugmentationRule rule1 = AugmentationRule.builder()
                .inputSchemaReference("ref1")
                .fields(List.of(ruleField))
                .build();

        final List<ConstraintViolation<?>> violations1 = new ArrayList<>();
        violations1.addAll(new ValidationConfiguration().getValidator().validate(rule1));

        assertEquals(0, violations1.size());

        //AugmentationRule with `input_schemas` field
        final AugmentationRule rule2 = AugmentationRule.builder()
                .inputSchemas(List.of("ref1", "ref2"))
                .fields(List.of(ruleField))
                .build();

        final List<ConstraintViolation<?>> violations2 = new ArrayList<>();
        violations2.addAll(new ValidationConfiguration().getValidator().validate(rule2));

        assertEquals(0, violations2.size());

        //AugmentationRule with `input_schema` and `input_schemas` fields
        final AugmentationRule rule3 = AugmentationRule.builder()
                .inputSchemaReference("ref1")
                .inputSchemas(List.of("ref1", "ref2"))
                .fields(List.of(ruleField))
                .build();

        final List<ConstraintViolation<?>> violations3 = new ArrayList<>();
        violations3.addAll(new ValidationConfiguration().getValidator().validate(rule3));

        assertEquals(0, violations3.size());
    }

    @Test
    public void isValid_inValidAugmentationRule() {
        final AugmentationRuleField ruleField = AugmentationRuleField.builder()
                .output("output1")
                .inputFields(List.of("input1", "input2"))
                .build();

        //AugmentationRule without `input_schema` or `input_schemas` fields
        final AugmentationRule rule = AugmentationRule.builder()
                .fields(List.of(ruleField))
                .build();

        final List<ConstraintViolation<?>> violations = new ArrayList<>();
        violations.addAll(new ValidationConfiguration().getValidator().validate(rule));

        assertEquals(1, violations.size());
        assertEquals("Missing required field. Either of the fields 'input_schema' or 'input_schemas' should exist in augmentation",
                violations.get(0).getMessage());
    }

}
