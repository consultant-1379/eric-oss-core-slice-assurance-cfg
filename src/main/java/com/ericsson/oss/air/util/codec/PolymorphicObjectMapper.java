/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.util.codec;

import com.ericsson.oss.air.csac.model.runtime.RuntimeKpiDefinition;
import com.ericsson.oss.air.csac.model.runtime.SubTypes;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

/**
 * This class provides manual polymorphic deserialization of objects when the base type is provided. To use this object mapper, the base type must be
 * annotated with {@link SubTypes} which will specify which of the subtypes can be deserialized and cast to an object of the base type.  For example
 *
 * <pre>
 *     final RuntimeKpiDefintion rtKpiDefinition = PolymorphicObjectMapper.mapper().readValue(kpiDefinitionDtoAsJsonString, RuntimeKpiDefinition.class);
 * </pre>
 */
@Slf4j
public class PolymorphicObjectMapper {

    private static final PolymorphicObjectMapper INSTANCE = new PolymorphicObjectMapper();

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static PolymorphicObjectMapper mapper() {
        return INSTANCE;
    }

    /**
     * Returns a minified string representation of the object as JSON.
     *
     * @param value
     *         value to write as a String.
     * @return a minified string representation of the object as JSON
     * @throws JsonProcessingException
     *         if the object could not be serialized to JSON
     */
    public String writeValueAsString(final Object value) throws JsonProcessingException {
        return MAPPER.writeValueAsString(value);
    }

    /**
     * Returns a pretty-printed string representation of the object as JSON.
     *
     * @param value
     *         value to write as a String.
     * @return a pretty-printed string representation of the object as JSON
     * @throws JsonProcessingException
     *         if the object could not be serialized to JSON
     */
    public String writeValueAsStringPretty(final Object value) throws JsonProcessingException {
        return MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(value);
    }

    /**
     * Deserializes a JSON string to a subtype of the provided base type.
     *
     * @param src
     *         JSON string to deserialize
     * @param baseType
     *         base type of the deserialized object
     * @param <T>
     *         base type to return. Must be able to cast the result to &lt;T&gt;
     * @return deserialized object
     * @throws JsonProcessingException
     *         if the JSON string could not be deserialized.
     */
    public <T> T readValue(final String src, final Class<?> baseType) throws JsonProcessingException {

        T result = null;
        boolean finished = false;

        JsonProcessingException lastException = null;

        // true polymorphism isn't really practical, so we'll just iterate over all the possible subtypes of the provided base type
        // to avoid the cost of reflection, let's annotate the interface with sub-types
        for (final Class<?> subType : RuntimeKpiDefinition.class.getAnnotation(SubTypes.class).value()) {

            try {
                final Object obj = MAPPER.readValue(src, subType);

                result = (T) obj;
                finished = true;
                break;

            } catch (final JsonProcessingException jpe) {
                log.debug("Unable to deserialize as {}", subType.getName());
                lastException = jpe;
            }

        }
        if (!finished && Objects.nonNull(lastException)) {
            throw lastException;
        }

        return result;
    }
}
