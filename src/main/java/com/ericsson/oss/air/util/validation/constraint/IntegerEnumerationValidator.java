/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.util.validation.constraint;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import lombok.SneakyThrows;

/**
 * Custom bean validator that allows checking of Integer values to ensure that they match one of the values in the provided list of legal values.
 */
public class IntegerEnumerationValidator implements ConstraintValidator<IntegerEnumeration, Integer> {

    private List<Integer> values;

    private boolean checkIfNotNull;

    private boolean isRange = false;

    @Override
    public void initialize(final IntegerEnumeration constraintAnnotation) {

        ConstraintValidator.super.initialize(constraintAnnotation);

        this.values = getValuesList(constraintAnnotation);

        this.checkIfNotNull = constraintAnnotation.notNull();

    }

    @SneakyThrows
    private List<Integer> getValuesList(final IntegerEnumeration constraintAnnotation) {

        if (constraintAnnotation.values().length > 0) {
            return Arrays.stream(constraintAnnotation.values()).boxed().collect(Collectors.toList());
        } else if (constraintAnnotation.range().length == 2) {

            if (constraintAnnotation.range()[0] > constraintAnnotation.range()[1]) {
                throw new IllegalStateException(
                        "Invalid range constraints: min(" + constraintAnnotation.range()[0] + "), max(" + constraintAnnotation.range()[1] + ")");
            }

            this.isRange = true;
            return List.of(constraintAnnotation.range()[0], constraintAnnotation.range()[1]);
        } else if (IntegerEnumeration.NullSupplier.class != constraintAnnotation.supplier()) {
            return constraintAnnotation.supplier().getConstructor().newInstance().get();
        }

        throw new IllegalStateException("No valid list of enumerated values specified");
    }

    @Override
    public boolean isValid(final Integer candidate, final ConstraintValidatorContext constraintValidatorContext) {

        // build the custom message
        buildCustomMessage(constraintValidatorContext);

        if (this.checkIfNotNull && Objects.isNull(candidate)) {
            return true;
        }

        return (this.isRange) ? checkRangeValues(candidate) : this.values.contains(candidate);
    }

    private void buildCustomMessage(final ConstraintValidatorContext constraintValidatorContext) {

        constraintValidatorContext.disableDefaultConstraintViolation();

        if (this.isRange) {
            constraintValidatorContext.buildConstraintViolationWithTemplate(
                            "Must be between " + this.values.get(0) + " and " + this.values.get(1))
                    .addConstraintViolation();
        } else {
            constraintValidatorContext.buildConstraintViolationWithTemplate("Must be one of " + this.values.toString())
                    .addConstraintViolation();
        }

    }

    private boolean checkRangeValues(final Integer candidate) {

        return this.values.get(0) <= candidate && this.values.get(1) >= candidate;
    }
}
