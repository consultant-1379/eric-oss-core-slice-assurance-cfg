/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.model.pmschema;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.Schema;
import org.springframework.util.ObjectUtils;

/**
 * Custom deserializer for the PM schema DTOs, which are Avro schemas.
 */
@Slf4j
public class PMSchemaDTODeserializer extends StdDeserializer<PMSchemaDTO>{

    private static final long serialVersionUID = 6797741389621706519L;
    private static final String SCHEMA = "schema";
    private static final String DELIMITER = ".";


    /**
     * Creates an instance of PMSchemaDTODeserializer with no arguments
     */
    public PMSchemaDTODeserializer() {
        this(null);
    }

    /**
     * Creates an instance of PMSchemaDTODeserializer.
     *
     * @param vc
     *      the value class of the deserializer
     */
    public PMSchemaDTODeserializer(final Class<?> vc) {
        super(vc);
    }

    @Override
    public PMSchemaDTO deserialize(final JsonParser jsonParser, final DeserializationContext ctxt) throws IOException {

        final ObjectMapper mapper = (ObjectMapper) jsonParser.getCodec();

        JsonNode schemaNode = mapper.readTree(jsonParser);

        // Added for the cases where the schema is represented as an escaped JSON string
        if(schemaNode.has(SCHEMA)) {
            schemaNode = mapper.readTree(schemaNode.path(SCHEMA).asText());
        }

        // Throws an AvroRuntimeException if the schema cannot be parsed
        // For example, an exception will be thrown if the PM record schema does not have a name nor fields
        final Schema pmSchema = new Schema.Parser().parse(schemaNode.toString());

        final List<String> fieldPaths = getSchemaFieldPaths(pmSchema);

        return PMSchemaDTO.builder()
                .name(pmSchema.getName())
                .namespace(pmSchema.getNamespace())
                .doc(pmSchema.getDoc())
                .aliases(pmSchema.getAliases())
                .fields(pmSchema.getFields())
                .fieldPaths(fieldPaths)
                .build();
    }


    private StringBuilder getFieldPathBuilder(final Schema schema, final StringBuilder parentPathBuilder) {
        return this.getFieldPathBuilder(schema, parentPathBuilder, null);
    }

    private StringBuilder getFieldPathBuilder(final Schema schema, final StringBuilder parentPathBuilder, final String fieldName) {

        final StringBuilder fieldPathBuilder = new StringBuilder(parentPathBuilder.toString());

        if(!ObjectUtils.isEmpty(fieldName)) {
            fieldPathBuilder.append(fieldName);
        }

        if(Schema.Type.RECORD == schema.getType()){
            fieldPathBuilder.append(DELIMITER);
        }

        return fieldPathBuilder;
    }

    private List<String> getSchemaFieldPaths(final Schema schema) {
        return this.getSchemaFieldPaths(schema, new StringBuilder());
    }

    /*
    (non-javadoc)
     * Gets the paths a.k.a. the fully qualified names to all the fields in the PM schema. Each sub-schema is
     * separated by a '.'.
     */
    private List<String> getSchemaFieldPaths(final Schema schema, final StringBuilder pathBuilder) {

        final List<String> fieldPaths = new ArrayList<>();

        switch (schema.getType()){
            case RECORD:
                fieldPaths.addAll(schema.getFields().stream()
                        .map(field -> getSchemaFieldPaths(field.schema(), getFieldPathBuilder(field.schema(), pathBuilder, field.name())))
                        .flatMap(Collection::stream)
                        .collect(Collectors.toList()));
                break;
            case UNION:
                fieldPaths.addAll(schema.getTypes().stream()
                        .map(schemaElement -> getSchemaFieldPaths(schemaElement, getFieldPathBuilder(schemaElement, pathBuilder)))
                        .flatMap(Collection::stream)
                        .collect(Collectors.toList()));
                break;
            case NULL:
                break;
            default:
                fieldPaths.add(pathBuilder.toString());
        }

        return fieldPaths;

    }
}
