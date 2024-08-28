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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;
import java.util.function.Supplier;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

/**
 * Custom annotation for integer enumeration value validation. When using the annotation, one of
 *
 * <ul>
 * <li>values</li>
 * <li>min and max</li>
 * <li>supplier</li>
 * </ul>
 *
 * must be provided.
 */
@Target({ ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER,
        ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = IntegerEnumerationValidator.class)
@Documented
public @interface IntegerEnumeration {

    /**
     * Null supplier used to indicate that no supplier class has been set.  This class is intentionally not functional.
     */
    class NullSupplier implements Supplier<List<Integer>> {

        @Override
        public List<Integer> get() {
            throw new UnsupportedOperationException();
        }
    }

    String message() default "Must be one of {values}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    /**
     * Enumerated list of legal values for the validation.
     *
     * @return enumerated list of legal values for the validation
     */
    int[] values() default {};

    /**
     * For more flexible KPI calculation engines, the valid period may be expressed as a range.
     *
     * @return minimum and maximum values in the allowed aggregation period value range.
     */
    int[] range() default {};

    /**
     * Configuration-driven KPI calculation engines may provide a supplier that provides the list of valid aggregation period values.
     *
     * @return supplier that provides a list of valid aggregation period values.
     */
    Class<? extends Supplier<List<Integer>>> supplier() default NullSupplier.class;

    /**
     * Check the value only if it is not null. If set to false, the value is checked whether it is null or not.
     *
     * @return {@code true} if the value is only checked if it is not null.
     */
    boolean notNull() default true;
}
