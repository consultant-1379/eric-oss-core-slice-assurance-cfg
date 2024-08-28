/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.model.pmschema;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

/**
 * Serializes the {@link SchemaURI} to a URI string.
 */
public class SchemaURISerializer extends JsonSerializer<SchemaURI> {

    @Override
    public void serialize(final SchemaURI schemaURI, final JsonGenerator jsonGenerator, final SerializerProvider serializerProvider)
            throws IOException {

        jsonGenerator.writeString(schemaURI.toString());

    }
}
