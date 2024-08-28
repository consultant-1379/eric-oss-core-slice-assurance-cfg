/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.util.codec;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.file.Path;
import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;

import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * This class specializes the {@link Codec}, adding bean validation using a {@code jakarta.validation.Validator}. Instances of this codec should not be
 * instantiated directly.  To use this codec, get an instance using the {@link Codec#withValidation()} method.  For example,
 *
 * <pre>
 * {@literal @}Autowired
 * private Codec codec;
 *
 * ...
 *
 * final MyBean myBean = this.codec.withValidation().readValue(myBeanSource, MyBean.class);
 * </pre>
 */
class ValidatingCodec extends Codec {

    private final Validator validator;

    /**
     * Constructs an instance of {@code ValidatingCodec} using the specified {@code jakarta.validation.Validator}.
     *
     * @param v validator to use with this codec
     */
    ValidatingCodec(final Validator v) {
        super();
        this.validator = v;
    }

    /**
     * Deserializes the provided JSON string to an object of type {@code clazz}.
     *
     * @param src   source JSON to deserialize
     * @param clazz object type to deserialize to
     * @return an object of type {@code clazz}
     * @throws JsonProcessingException if an error occurs while deserializing the source
     */
    @Override
    public <T> T readValue(final String src, final Class<T> clazz) throws JsonProcessingException {

        final T t = super.readValue(src, clazz);

        final Set<ConstraintViolation<T>> violationSet = this.validator.validate(t);

        if (!violationSet.isEmpty()) {
            throw new ConstraintViolationException(violationSet);
        }

        return t;
    }

    /**
     * Deserializes the JSON source specified by the provided {@code Path} to an object of type {@code clazz}.
     *
     * @param src   source JSON to deserialize
     * @param clazz object type to deserialize to
     * @return an object of type {@code clazz}
     * @throws IOException if an error occurs while deserializing the source
     */
    @Override
    public <T> T readValue(final Path src, final Class<T> clazz) throws IOException {

        final T t = super.readValue(src, clazz);

        final Set<ConstraintViolation<T>> violationSet = this.validator.validate(t);

        if (!violationSet.isEmpty()) {
            throw new ConstraintViolationException(violationSet);
        }

        return t;
    }

    /**
     * Deserializes the JSON source specified by the provided {@code InputStream} to an object of type {@code clazz}.
     *
     * @param src   source JSON to deserialize
     * @param clazz object type to deserialize to
     * @return an object of type {@code clazz}
     * @throws IOException if an error occurs while deserializing the source
     */
    @Override
    public <T> T readValue(final InputStream src, final Class<T> clazz) throws IOException {

        final T t = super.readValue(src, clazz);

        final Set<ConstraintViolation<T>> violationSet = this.validator.validate(t);

        if (!violationSet.isEmpty()) {
            throw new ConstraintViolationException(violationSet);
        }

        return t;
    }

    /**
     * Deserializes the JSON source specified by the provided {@code Reader} to an object of type {@code clazz}.
     *
     * @param src   source JSON to deserialize
     * @param clazz object type to deserialize to
     * @return an object of type {@code clazz}
     * @throws IOException if an error occurs while deserializing the source
     */
    @Override
    public <T> T readValue(final Reader src, final Class<T> clazz) throws IOException {

        final T t = super.readValue(src, clazz);

        final Set<ConstraintViolation<T>> violationSet = this.validator.validate(t);

        if (!violationSet.isEmpty()) {
            throw new ConstraintViolationException(violationSet);
        }

        return t;
    }

    /**
     * Deserializes the JSON source specified by the provided byte [] to an object of type {@code clazz}.
     *
     * @param src   source JSON to deserialize
     * @param clazz object type to deserialize to
     * @return an object of type {@code clazz}
     * @throws IOException if an error occurs while deserializing the source
     */
    @Override
    public <T> T readValue(final byte[] src, final Class<T> clazz) throws IOException {

        final T t = super.readValue(src, clazz);

        final Set<ConstraintViolation<T>> violationSet = this.validator.validate(t);

        if (!violationSet.isEmpty()) {
            throw new ConstraintViolationException(violationSet);
        }

        return t;
    }
}
