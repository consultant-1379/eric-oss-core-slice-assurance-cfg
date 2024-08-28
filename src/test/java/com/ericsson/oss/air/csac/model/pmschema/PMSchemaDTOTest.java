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

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import jakarta.validation.Validation;

import com.ericsson.oss.air.util.codec.Codec;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.avro.SchemaParseException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class PMSchemaDTOTest {

    private static final String TYPE = "record";
    private static final String NAMESPACE = "PM_COUNTERS";
    private static final String SCHEMA_NAME_A = "AMF_Mobility_NetworkSlice";
    private static final String PM_COUNTERS = "pmCounters";
    private static final String PM_NAME_A = "VS_NS_NbrRegisteredSub_5GS";
    private static final String PM_NAME_B = "pmFanSpeed";
    private static final String PM_NAME_C = "pmRadioBeamFailureDetected";
    private static final String OUTER_RECORD = "outerRecord";
    private static final String INNER_PROPERTY = "innerProperty";
    private static final String INNER_RECORD = "innerRecord";
    private static final String FIELD = "Field";
    private static final String BOOLEAN_FIELD = "boolean" + FIELD;
    private static final String INT_FIELD = "int" + FIELD;
    private static final String LONG_FIELD = "long" + FIELD;
    private static final String FLOAT_FIELD = "float" + FIELD;
    private static final String DOUBLE_FIELD = "double" + FIELD;
    private static final String BYTES_FIELD = "bytes" + FIELD;
    private static final String STRING_FIELD = "string" + FIELD;
    private static final String ENUM_FIELD = "enum" + FIELD;
    private static final String ARRAY_FIELD = "array" + FIELD;
    private static final String MAP_FIELD = "map" + FIELD;
    private static final String FIXED_FIELD = "fixed" + FIELD;


    private static final String FIELDS_WITH_UNIONS = "[\n" +
            "    {\n" +
            "      \"name\": \"" + PM_COUNTERS + "\",\n" +
            "      \"type\": [\n" +
            "        \"null\",\n" +
            "        {\n" +
            "          \"name\": \"pmMetricsSchema\",\n" +
            "          \"type\": \"record\",\n" +
            "          \"fields\": [\n" +
            "            {\n" +
            "              \"name\": \"" + PM_NAME_A + "\",\n" +
            "              \"type\": [\n" +
            "                \"null\",\n" +
            "                \"int\"\n" +
            "              ],\n" +
            "              \"default\": null,\n" +
            "              \"doc\": \"Number of AMF subscribers\"\n" +
            "            }\n" +
            "          ]\n" +
            "        }\n" +
            "      ],\n" +
            "      \"default\": null\n" +
            "    }\n" +
            "  ]\n";

    private static final String FIELDS_WITH_RECORD = "[\n" +
            "    {\n" +
            "       \"name\":\"" + PM_COUNTERS + "\",\n" +
            "       \"type\":{\n" +
            "          \"name\":\"" + PM_COUNTERS + "\",\n" +
            "          \"type\":\"record\",\n" +
            "          \"fields\":[\n" +
            "             {\n" +
            "                \"name\":\"" + PM_NAME_B + "\",\n" +
            "                \"type\":{\n" +
            "                   \"type\":\"array\",\n" +
            "                   \"items\":\"int\"\n" +
            "                }\n" +
            "             },\n" +
            "             {\n" +
            "                \"name\":\"" + PM_NAME_C + "\",\n" +
            "                \"type\":\"int\"\n" +
            "             }\n" +
            "          ]\n" +
            "       }\n" +
            "    }\n" +
            "  ]\n";

    private static final String FIELDS_WITH_NESTED_RECORD = "[\n" +
            "  {\n" +
            "    \"name\": \"" + OUTER_RECORD + "\",\n" +
            "    \"type\": {\n" +
            "      \"name\": \"outerRecordSchema\",\n" +
            "      \"type\": \"record\",\n" +
            "      \"fields\": [{\n" +
            "          \"name\": \""+ INNER_PROPERTY +"\",\n" +
            "          \"type\": \"double\"\n" +
            "        },\n" +
            "        {\n" +
            "          \"name\": \""+ INNER_RECORD +"\",\n" +
            "          \"type\": {\n" +
            "            \"type\": \"record\",\n" +
            "            \"name\": \"innerRecordSchema\",\n" +
            "            \"fields\": [{\n" +
            "                \"name\": \"" + PM_NAME_B + "\",\n" +
            "                \"type\": \"int\"\n" +
            "              },\n" +
            "              {\n" +
            "                \"name\": \"" + PM_NAME_C + "\",\n" +
            "                \"type\": \"int\"\n" +
            "              }\n" +
            "            ]\n" +
            "          }\n" +
            "        }\n" +
            "      ]\n" +
            "    }\n" +
            "  }" +
            "]\n";

    private static final String FIELDS_WITH_OTHER_TYPES = "[\n" +
            "    {\n" +
            "      \"name\": \"" + BOOLEAN_FIELD + "\",\n" +
            "      \"type\": \"boolean\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"name\": \"" + INT_FIELD + "\",\n" +
            "      \"type\": \"int\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"name\": \"" + LONG_FIELD + "\",\n" +
            "      \"type\": \"long\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"name\": \"" + FLOAT_FIELD + "\",\n" +
            "      \"type\":  \"float\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"name\": \"" + DOUBLE_FIELD + "\",\n" +
            "      \"type\":  \"double\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"name\": \"" + BYTES_FIELD + "\",\n" +
            "      \"type\":  \"bytes\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"name\": \"" + STRING_FIELD + "\",\n" +
            "      \"type\":  \"string\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"name\": \"" + ENUM_FIELD + "\",\n" +
            "      \"type\": {\n" +
            "        \"type\": \"enum\",\n" +
            "        \"name\": \"Suit\",\n" +
            "        \"symbols\": [\"SPADES\", \"HEARTS\", \"DIAMONDS\", \"CLUBS\"]\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"name\": \"" + ARRAY_FIELD + "\",\n" +
            "      \"type\": {\n" +
            "        \"type\": \"array\",\n" +
            "        \"items\": \"string\",\n" +
            "        \"default\": []\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"name\": \"" + MAP_FIELD + "\",\n" +
            "      \"type\": {\n" +
            "        \"type\": \"map\",\n" +
            "        \"values\": \"long\",\n" +
            "        \"default\": {}\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"name\": \"" + FIXED_FIELD + "\",\n" +
            "      \"type\": {\n" +
            "        \"type\": \"fixed\",\n" +
            "        \"size\": 16,\n" +
            "        \"name\": \"md5\"\n" +
            "      }\n" +
            "    }\n" +
            "  ]\n";

    private static final String INVALID_AVRO_SCHEMA = "{\n" +
            "  \"type\": \"record\",\n" +
            "  \"name\": \"AMF_Mobility_NetworkSlice\",\n" +
            "  \"namespace\": \"PM_COUNTERS\"\n" +
            "}";

    private Codec codec = new Codec(Validation.buildDefaultValidatorFactory().getValidator()).withValidation();

    private static String SCHEMA_WITH_UNIONS;
    private static String SCHEMA_WITH_RECORD;
    private static String SCHEMA_WITH_NESTED_RECORD;
    private static String SCHEMA_WITH_OTHER_TYPES;

    @BeforeAll
    static void setUpClass(){
        SCHEMA_WITH_UNIONS = getSchemaAsJsonString(FIELDS_WITH_UNIONS);
        SCHEMA_WITH_RECORD = getSchemaAsJsonString(FIELDS_WITH_RECORD);
        SCHEMA_WITH_NESTED_RECORD = getSchemaAsJsonString(FIELDS_WITH_NESTED_RECORD);
        SCHEMA_WITH_OTHER_TYPES = getSchemaAsJsonString(FIELDS_WITH_OTHER_TYPES);
    }


    @Test
    void buildPMSchemaDTO_DefaultValues_NonNullFieldPaths() throws IOException {
        final PMSchemaDTO pmSchemaDTO = PMSchemaDTO.builder().build();
        assertNotNull(pmSchemaDTO.getFieldPaths());
    }

    @Test
    void buildPMSchemaDTO_NullFieldPaths_ThrowsException() throws IOException {
        assertThrows(NullPointerException.class,
                () -> PMSchemaDTO.builder().fieldPaths(null).build());
    }


    @Test
    void ReadValue_SchemaWithUnions_OneFieldPath() throws JsonProcessingException {

        final PMSchemaDTO testPMSchema = this.codec.readValue(SCHEMA_WITH_UNIONS, PMSchemaDTO.class);
        final List<String> fieldPaths = testPMSchema.getFieldPaths();

        assertNotNull(fieldPaths);
        assertFalse(fieldPaths.isEmpty());
        assertEquals(1, fieldPaths.size());
        assertTrue(fieldPaths.contains(PM_COUNTERS + "." + PM_NAME_A));
        assertEquals(TYPE, testPMSchema.getType());
        assertEquals(SCHEMA_NAME_A, testPMSchema.getName());
        assertEquals(NAMESPACE, testPMSchema.getNamespace());
        assertNull(testPMSchema.getDoc());
        assertEquals(Collections.EMPTY_SET, testPMSchema.getAliases());
        assertFalse(testPMSchema.getFields().isEmpty());

    }

    @Test
    void ReadValue_SchemaWithRecord_TwoFieldPaths() throws JsonProcessingException {
        final List<String> expectedFieldPaths = Arrays.asList(PM_COUNTERS + "." + PM_NAME_B, PM_COUNTERS + "."  + PM_NAME_C);

        final PMSchemaDTO testPMSchema = this.codec.readValue(SCHEMA_WITH_RECORD, PMSchemaDTO.class);
        final List<String> fieldPaths = testPMSchema.getFieldPaths();

        assertNotNull(fieldPaths);
        assertFalse(fieldPaths.isEmpty());
        assertEquals(2, fieldPaths.size());
        assertTrue(fieldPaths.containsAll(expectedFieldPaths));
        assertEquals(TYPE, testPMSchema.getType());
        assertEquals(SCHEMA_NAME_A, testPMSchema.getName());
        assertEquals(NAMESPACE, testPMSchema.getNamespace());
        assertNull( testPMSchema.getDoc());
        assertEquals(Collections.EMPTY_SET, testPMSchema.getAliases());
        assertFalse(testPMSchema.getFields().isEmpty());

    }

    @Test
    void ReadValue_SchemaWithNestedRecord_ThreeFieldPaths() throws JsonProcessingException {
        final List<String> expectedFieldPaths = Arrays.asList(OUTER_RECORD + "." + INNER_PROPERTY,
                OUTER_RECORD + "." + INNER_RECORD + "." + PM_NAME_B,
                OUTER_RECORD + "." + INNER_RECORD + "." + PM_NAME_C);

        // TODO- test that real Avro record looks like what you think it is
        final PMSchemaDTO testPMSchema = this.codec.readValue(SCHEMA_WITH_NESTED_RECORD, PMSchemaDTO.class);
        final List<String> fieldPaths = testPMSchema.getFieldPaths();

        assertNotNull(fieldPaths);
        assertFalse(fieldPaths.isEmpty());
        assertEquals(3, fieldPaths.size());
        assertTrue(fieldPaths.containsAll( expectedFieldPaths ));
        assertEquals(TYPE, testPMSchema.getType());
        assertEquals(SCHEMA_NAME_A, testPMSchema.getName());
        assertEquals(NAMESPACE, testPMSchema.getNamespace());
        assertNull(testPMSchema.getDoc());
        assertEquals(Collections.EMPTY_SET, testPMSchema.getAliases());
        assertFalse(testPMSchema.getFields().isEmpty());


    }

    @Test
    void ReadValue_SchemaWithOtherFields_ManyFieldPaths() throws JsonProcessingException {
        final List<String> expectedFieldPaths = Arrays.asList(BOOLEAN_FIELD, INT_FIELD, LONG_FIELD, FLOAT_FIELD, DOUBLE_FIELD, BYTES_FIELD,  STRING_FIELD, ENUM_FIELD, ARRAY_FIELD, MAP_FIELD, FIXED_FIELD);

        final PMSchemaDTO testPMSchema = this.codec.readValue(SCHEMA_WITH_OTHER_TYPES, PMSchemaDTO.class);
        final List<String> fieldPaths = testPMSchema.getFieldPaths();

        assertNotNull(fieldPaths);
        assertFalse(fieldPaths.isEmpty());
        assertEquals(11, fieldPaths.size());
        assertTrue(fieldPaths.containsAll( expectedFieldPaths ));
        assertEquals(TYPE, testPMSchema.getType());
        assertEquals(SCHEMA_NAME_A, testPMSchema.getName());
        assertEquals(NAMESPACE, testPMSchema.getNamespace());
        assertNull(testPMSchema.getDoc());
        assertEquals(Collections.EMPTY_SET, testPMSchema.getAliases());
        assertFalse(testPMSchema.getFields().isEmpty());

    }


    @Test
    void ReadValue_Schema_ThrowsException()  {

        assertThrows(SchemaParseException.class,
                () -> this.codec.withValidation().readValue(INVALID_AVRO_SCHEMA, PMSchemaDTO.class));

    }


    @Test
    void ReadValue_WrappedSchema_OneFieldPath() throws JsonProcessingException {

        final PMSchemaDTO testPMSchema = this.codec.readValue(getWrappedSchemaString(SCHEMA_WITH_UNIONS), PMSchemaDTO.class);
        final List<String> actualPmNames = testPMSchema.getFieldPaths();

        assertNotNull(actualPmNames);
        assertFalse(actualPmNames.isEmpty());
        assertEquals(1, actualPmNames.size());
        assertTrue(actualPmNames.contains(PM_COUNTERS + "." + PM_NAME_A));
        assertEquals(TYPE, testPMSchema.getType());
        assertEquals(SCHEMA_NAME_A, testPMSchema.getName());
        assertEquals(NAMESPACE, testPMSchema.getNamespace());
        assertNull(testPMSchema.getDoc());
        assertEquals(Collections.EMPTY_SET, testPMSchema.getAliases());
        assertFalse(testPMSchema.getFields().isEmpty());

    }

    @Test
    void ReadValue_WrappedSchema_ThrowsException()  {

        assertThrows(SchemaParseException.class,
                () -> this.codec.withValidation().readValue(getWrappedSchemaString(INVALID_AVRO_SCHEMA), PMSchemaDTO.class));

    }

    private static String getSchemaAsJsonString(final String fields){
        return "{\n  \"type\": \"" + TYPE + "\",\n" +
                "  \"name\": \"" + SCHEMA_NAME_A + "\",\n" +
                "  \"namespace\": \"" + NAMESPACE + "\",\n" +
                "  \"fields\":" + fields + "}";
    }

    private String getWrappedSchemaString(final String testSchema) throws JsonProcessingException {
        return "{\"schema\": " + this.codec.writeValueAsString(testSchema)    + "\n" + "}";
    }

}
