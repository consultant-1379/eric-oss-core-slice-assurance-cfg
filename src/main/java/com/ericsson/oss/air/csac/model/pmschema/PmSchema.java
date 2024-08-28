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

import java.util.List;

import lombok.NonNull;
import org.apache.avro.Schema;

/**
 * A data bean representing the Avro schema along with metadata required for CSAC business logic, such as
 * the Kafka topic for the schema's published Avro records.
 *
 */
public record PmSchema(@NonNull String kafkaTopic, @NonNull PMSchemaDTO pmSchemaDTO) {

    /*
     * Convenience methods to directly get fields from this record's pmSchemaDTO
     */

    /**
     * Returns the name of the Avro schema.
     *
     * @return the name of the Avro schema.
     */
    public String getName() {
        return this.pmSchemaDTO.getName();
    }

    /**
     * Returns the paths to the fields of the Avro schema.
     *
     * @return the paths to the fields of the Avro schema.
     */
    public List<String> getFieldPaths() {
        return this.pmSchemaDTO.getFieldPaths();
    }

    /**
     * Returns the fields of the Avro schema.
     *
     * @return the fields of the Avro schema.
     */
    public List<Schema.Field> getFields() {
        return this.pmSchemaDTO.getFields();
    }
}
