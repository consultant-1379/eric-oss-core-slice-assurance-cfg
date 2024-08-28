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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import org.apache.avro.Schema;

/**
 * Represents an Avro schema for PMs. The schemas are of Avro complex type "record".
 * */
@Getter
@Builder
@EqualsAndHashCode
@JsonDeserialize(using = PMSchemaDTODeserializer.class)
@JsonInclude(Include.NON_EMPTY)
@JsonPropertyOrder({ "type", "name", "namespace", "doc", "aliases", "fields" })
public class PMSchemaDTO{

    /**
     * Avro schemas for PMs are always of type "record"
     */
    @JsonProperty(value = "type")
    private static final String TYPE = "record";

    /**
     * Name of the Avro schema, required for Avro record schemas
     */
    @JsonProperty(value = "name")
    private String name;

    /**
     * Value that qualifies the name of the Avro schema, optional for Avro record schemas
     */
    @JsonProperty(value = "namespace")
    private String namespace;

    /**
     * Documentation for the use of this schema, optional for Avro record schemas
     */
    @JsonProperty(value = "doc")
    private String doc;

    /**
     * Provides alternate names for the record, optional for Avro record schemas
     */
    @JsonProperty(value = "aliases")
    private Set<String> aliases;

    /**
     * Fields of the record, required for Avro record schemas
     */
    @JsonProperty(value = "fields")
    private List<Schema.Field> fields;

    /**
     * List of all the fields' paths a.k.a. the fully qualified names in the Avro record schema. Each sub-schema is
     * separated by a '.'.
     */
    @JsonIgnore
    @Builder.Default
    @NonNull
    private List<String> fieldPaths = new ArrayList<>();

    /**
     * Get the type of the PM Avro schema.
     *
     * @return the type of the PM Avro schema
     */
    public String getType() {
        return TYPE;
    }

}
