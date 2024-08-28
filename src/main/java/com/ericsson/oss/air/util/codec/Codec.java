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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.validation.Validator;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * A simple codec used for deserializing JSON to java beans. This codec provides a number of readValue(..) methods for deserializing JSON from sources
 * including
 * <ul>
 * <li>java.lang.String</li>
 * <li>java.nio.Path</li>
 * <li>java.io.InputStream</li>
 * <li>java.io.Reader</li>
 * <li>byte []</li>
 * </ul>
 *
 * In addition, this codec can serialize an object to a JSON String in either minified or pretty-printed format.
 */
@Component
@NoArgsConstructor
public class Codec {

    @Autowired private Validator validator;

    @Getter(value = AccessLevel.PRIVATE, lazy = true) private final ValidatingCodec validatingCodec = new ValidatingCodec(validator);

    private final ObjectMapper mapper = new ObjectMapper();

    public Codec(final Validator validator) {
      this.validator = validator;
    }

    /**
     * Returns an instance of {@code Codec} that will validate beans after deserialization using any of the {@code readValue} methods.
     *
     * @return a validating instance of {@code Codec}
     */
    public Codec withValidation() {
        return getValidatingCodec();
    }

    /**
     * Returns a minified JSON string representation of the specified value.
     *
     * @param value
     *     value to serialize to String
     * @return a minified JSON string representation of the specified value
     * @throws JsonProcessingException
     *     if an error occurs while serializing the value to JSON
     */
    public String writeValueAsString(final Object value) throws JsonProcessingException {
        return this.mapper.writeValueAsString(value);
    }

    /**
     * Returns a pretty-printed JSON string representation of the specified value.
     *
     * @param value
     *     value to serialize to a pretty-printed String
     * @return a pretty-printed JSON string representation of the specified value
     * @throws JsonProcessingException
     *     if an error occurs while serializing the value to JSON
     */
    public String writeValueAsStringPretty(final Object value) throws JsonProcessingException {
        return this.mapper.writerWithDefaultPrettyPrinter().writeValueAsString(value);
    }

    /**
     * Deserializes the provided JSON string to an object of type {@code clazz}.
     *
     * @param src
     *     source JSON to deserialize
     * @param clazz
     *     object type to deserialize to
     * @return an object of type {@code clazz}
     * @throws JsonProcessingException
     *     if an error occurs while deserializing the source
     */
    public <T> T readValue(final String src, final Class<T> clazz) throws JsonProcessingException {
        return this.mapper.readValue(src, clazz);
    }

    /**
     * Deserializes the JSON source specified by the provided {@code Path} to an object of type {@code clazz}.
     *
     * @param src
     *     source JSON to deserialize
     * @param clazz
     *     object type to deserialize to
     * @return an object of type {@code clazz}
     * @throws IOException
     *     if an error occurs while deserializing the source
     */
    public <T> T readValue(final Path src, final Class<T> clazz) throws IOException {
        return this.mapper.readValue(Files.readAllBytes(src), clazz);
    }

    /**
     * Deserializes the JSON source specified by the provided {@code InputStream} to an object of type {@code clazz}.
     *
     * @param src
     *     source JSON to deserialize
     * @param clazz
     *     object type to deserialize to
     * @return an object of type {@code clazz}
     * @throws IOException
     *     if an error occurs while deserializing the source
     */
    public <T> T readValue(final InputStream src, final Class<T> clazz) throws IOException {
        return this.mapper.readValue(src, clazz);
    }

    /**
     * Deserializes the JSON source specified by the provided {@code Reader} to an object of type {@code clazz}.
     *
     * @param src
     *     source JSON to deserialize
     * @param clazz
     *     object type to deserialize to
     * @return an object of type {@code clazz}
     * @throws IOException
     *     if an error occurs while deserializing the source
     */
    public <T> T readValue(final Reader src, final Class<T> clazz) throws IOException {
        return this.mapper.readValue(src, clazz);
    }

    /**
     * Deserializes the JSON source specified by the provided byte [] to an object of type {@code clazz}.
     *
     * @param src
     *     source JSON to deserialize
     * @param clazz
     *     object type to deserialize to
     * @return an object of type {@code clazz}
     * @throws IOException
     *     if an error occurs while deserializing the source
     */
    public <T> T readValue(final byte[] src, final Class<T> clazz) throws IOException {
        return this.mapper.readValue(src, clazz);
    }
}
