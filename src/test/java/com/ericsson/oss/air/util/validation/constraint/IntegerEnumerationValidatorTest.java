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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ValidationException;

import com.ericsson.oss.air.csac.model.AggregationPeriodSupplier;
import com.ericsson.oss.air.util.codec.Codec;
import com.ericsson.oss.air.util.validation.config.ValidationConfiguration;
import lombok.AllArgsConstructor;
import org.junit.jupiter.api.Test;

class IntegerEnumerationValidatorTest {

    private final Codec codec = new Codec(new ValidationConfiguration().getValidator());

    @AllArgsConstructor
    static class TestBean {

        @IntegerEnumeration(values = { 1 })
        private Integer nullableValue;

        @IntegerEnumeration(values = { 1 },
                            notNull = false)
        private Integer nonNullableValue;
    }

    @AllArgsConstructor
    static class TestBeanWithProvider {

        @IntegerEnumeration(supplier = AggregationPeriodSupplier.class)
        private Integer value;
    }

    @AllArgsConstructor
    static class TestBeanWithRange {

        @IntegerEnumeration(range = { 1, 10 })
        private Integer value;
    }

    @AllArgsConstructor
    static class TestBeanWithInvalidRange {

        @IntegerEnumeration(range = { 10, 1 })
        private Integer value;
    }

    static class UnusableSupplier extends AggregationPeriodSupplier {

        public UnusableSupplier(final int requiredParam) {

        }
    }

    @AllArgsConstructor
    static class TestBeanWithUnusableSupplier {

        @IntegerEnumeration(supplier = UnusableSupplier.class)
        private Integer value;
    }

    @AllArgsConstructor
    static class TestBeanWithNoValueSupplier {

        @IntegerEnumeration
        private Integer value;
    }

    @Test
    void testValid_nullField() {

        final TestBean actual = new TestBean(null, 1);

        assertEquals(0, new ValidationConfiguration().getValidator().validate(actual).size());
    }

    @Test
    void testValid() {

        final TestBean actual = new TestBean(1, 1);

        assertEquals(0, new ValidationConfiguration().getValidator().validate(actual).size());
    }

    @Test
    void testInvalid_nonNullable() {

        final TestBean actual = new TestBean(1, null);

        final List<ConstraintViolation<?>> violations = new ArrayList<>();
        violations.addAll(new ValidationConfiguration().getValidator().validate(actual));

        assertNotEquals(0, violations.size());
        assertEquals("Must be one of [1]", violations.get(0).getMessage());
    }

    @Test
    void testInvalid_nullableOutOfRange() {

        final TestBean actual = new TestBean(2, 1);

        final List<ConstraintViolation<?>> violations = new ArrayList<>();
        violations.addAll(new ValidationConfiguration().getValidator().validate(actual));

        assertNotEquals(0, violations.size());
        assertEquals("Must be one of [1]", violations.get(0).getMessage());
    }

    @Test
    void testInvalid_nonNullableOutOfRange() {

        final TestBean actual = new TestBean(1, 2);

        final List<ConstraintViolation<?>> violations = new ArrayList<>();
        violations.addAll(new ValidationConfiguration().getValidator().validate(actual));

        assertNotEquals(0, violations.size());
        assertEquals("Must be one of [1]", violations.get(0).getMessage());
    }

    @Test
    void testValid_usingProvider() {

        final TestBeanWithProvider actual = new TestBeanWithProvider(15);

        assertEquals(0, new ValidationConfiguration().getValidator().validate(actual).size());

    }

    @Test
    void testInvalid_usingProvider() {

        final TestBeanWithProvider actual = new TestBeanWithProvider(5);

        final List<ConstraintViolation<?>> violations = new ArrayList<>();
        violations.addAll(new ValidationConfiguration().getValidator().validate(actual));

        assertNotEquals(0, violations.size());
        assertEquals("Must be one of [15, 60, 1440]", violations.get(0).getMessage());

    }

    @Test
    void testValid_usingRange() {

        final TestBeanWithRange actual = new TestBeanWithRange(5);

        assertEquals(0, new ValidationConfiguration().getValidator().validate(actual).size());
    }

    @Test
    void testInvalid_usingRangeTooLarge() {

        final TestBeanWithRange actual = new TestBeanWithRange(15);

        final List<ConstraintViolation<?>> violations = new ArrayList<>();
        violations.addAll(new ValidationConfiguration().getValidator().validate(actual));

        assertNotEquals(0, violations.size());
        assertEquals("Must be between 1 and 10", violations.get(0).getMessage());
    }

    @Test
    void testInvalid_usingRangeTooSmall() {

        final TestBeanWithRange actual = new TestBeanWithRange(0);

        final List<ConstraintViolation<?>> violations = new ArrayList<>();
        violations.addAll(new ValidationConfiguration().getValidator().validate(actual));

        assertNotEquals(0, violations.size());
        assertEquals("Must be between 1 and 10", violations.get(0).getMessage());
    }

    @Test
    void testInvalid_throwsException() {

        final TestBeanWithUnusableSupplier actual = new TestBeanWithUnusableSupplier(1);

        assertThrows(NoSuchMethodException.class, () -> new ValidationConfiguration().getValidator().validate(actual));

    }

    @Test
    void testInvalid_noValidValueSupplier() {

        final TestBeanWithNoValueSupplier actual = new TestBeanWithNoValueSupplier(1);

        final Exception ex = assertThrows(ValidationException.class, () -> new ValidationConfiguration().getValidator().validate(actual));

        assertTrue(ex.getCause() instanceof IllegalStateException);
    }

    @Test
    void testInvalid_invalidRangeValues() {

        final TestBeanWithInvalidRange actual = new TestBeanWithInvalidRange(1);

        final Exception ex = assertThrows(ValidationException.class, () -> new ValidationConfiguration().getValidator().validate(actual));

        assertTrue(ex.getCause() instanceof IllegalStateException);
    }
}