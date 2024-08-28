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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.apache.avro.Schema;
import org.apache.commons.collections.CollectionUtils;
import org.junit.jupiter.api.Test;

class PmSchemaTest {


    private static final String KAFKA_TOPIC = "test-topic";
    private static final String PM_NAME_A = "myPM_A";
    private static final String PM_SCHEMA_NAME_A = "myPMSchema_A";
    private static final Schema.Field field1 = new Schema.Field("field1", Schema.create(Schema.Type.STRING), null, null);
    private static final Schema.Field field2 = new Schema.Field("field2", Schema.create(Schema.Type.STRING), null, null);


    @Test
    void createPmSchema() {
       final PmSchema pmSchema = new PmSchema(KAFKA_TOPIC, PMSchemaDTO.builder()
               .fields(List.of(field1, field2))
               .fieldPaths(List.of(PM_NAME_A))
               .name(PM_SCHEMA_NAME_A)
               .build());

       assertEquals(KAFKA_TOPIC, pmSchema.kafkaTopic());
       assertTrue(CollectionUtils.isEqualCollection(pmSchema.getFields(), List.of(field1, field2)));
       assertTrue(CollectionUtils.isEqualCollection(pmSchema.getFieldPaths(), List.of(PM_NAME_A)));
       assertEquals(PM_SCHEMA_NAME_A, pmSchema.getName());
    }

    @Test
    void createPmSchema_NullKafkaTopic_ThrowsException() {
        assertThrows(NullPointerException.class, () -> new PmSchema(null, PMSchemaDTO.builder()
                .fields(List.of(field1, field2))
                .fieldPaths(List.of(PM_NAME_A))
                .name(PM_SCHEMA_NAME_A)
                .build()));
    }

    @Test
    void createPmSchema_NullPMSchemaDTO_ThrowsException() {
        assertThrows(NullPointerException.class, () -> new PmSchema(KAFKA_TOPIC, null));
    }

}