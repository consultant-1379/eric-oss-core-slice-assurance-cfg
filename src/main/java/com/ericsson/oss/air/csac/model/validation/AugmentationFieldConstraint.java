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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

/**
 * Custom annotation to validate:
 *
 * <ul>
 *     <li>`input_schema` and `input_schemas` in {@link com.ericsson.oss.air.csac.model.AugmentationRule}</li>
 *     <li>`output` and `output_fields` in {@link com.ericsson.oss.air.csac.model.AugmentationRuleField}</li>
 * </ul>
 *
 * <p>
 * When using the annotation, `entity` and `entityList` attributes must be provided.
 */
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = AugmentationFieldValidator.class)
@Documented
public @interface AugmentationFieldConstraint {

    String message() default "Missing required field.";

    String entity();

    String entityList();

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
