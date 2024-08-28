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

import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;

import com.ericsson.oss.air.util.codec.Codec;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;

class PMSchemaIdentifierTest {

    private static final Codec codec;

    static {
        codec = new Codec(Validation.buildDefaultValidatorFactory().getValidator()).withValidation();
    }

    @Test
    void parse_SubjectIntegerVersionString_ReturnsSchemaIdentifier() {

        final PMSchemaIdentifier schemaIdentifier = PMSchemaIdentifier.parse("dataSpace/dataProvider/dataCategory/namespace.name/10");

        assertNotNull(schemaIdentifier);
        assertEquals("dataSpace/dataProvider/dataCategory/namespace.name", schemaIdentifier.getSubject());
        assertEquals("10", schemaIdentifier.getVersion());
    }

    @Test
    void parse_SubjectLatestVersionString_ReturnsSchemaIdentifier() {

        final PMSchemaIdentifier schemaIdentifier = PMSchemaIdentifier.parse("dataSpace/dataProvider/dataCategory/namespace.name/latest");

        assertNotNull(schemaIdentifier);
        assertEquals("dataSpace/dataProvider/dataCategory/namespace.name", schemaIdentifier.getSubject());
        assertEquals("latest", schemaIdentifier.getVersion());
    }

    @Test
    void parse_UnmatchedSubjectVersionString_ThrowsException() {

        assertThrows(IllegalArgumentException.class,
            () -> PMSchemaIdentifier.parse("SubjectValueFoo"));
    }

    @Test
    void parse_SubjectVersionStringWithInvalidVersion_ThrowsException() {

        assertThrows(IllegalArgumentException.class,
            () -> PMSchemaIdentifier.parse("SubjectValue/foo"));
    }

    @Test
    void constructor_ValidSubjectValidVersion_ReturnsSchemaIdentifier() {

        final PMSchemaIdentifier schemaIdentifier = new PMSchemaIdentifier("foo", "2");

        assertNotNull(schemaIdentifier);
        assertEquals("foo", schemaIdentifier.getSubject());
        assertEquals("2", schemaIdentifier.getVersion());
    }

    @Test
    void constructor_NullSubject_ThrowsException() {

        assertThrows(NullPointerException.class,
            () -> new PMSchemaIdentifier(null, "2"));
    }

    @Test
    void constructor_NullVersion_ThrowsException() {

        assertThrows(NullPointerException.class,
            () -> new PMSchemaIdentifier("Subject", null));
    }


    @Test
    void readValue_SchemaIdentifier_ReturnsSchemaIdentifier() throws JsonProcessingException {

        final PMSchemaIdentifier schemaIdentifier = codec.readValue("{\"subject\":\"foo\",\"version\":\"latest\"}", PMSchemaIdentifier.class);

        assertNotNull(schemaIdentifier);
        assertEquals("foo", schemaIdentifier.getSubject());
        assertEquals("latest", schemaIdentifier.getVersion());
    }

    @Test
    void readValue_SchemaIdentifierWithBlankSubject_ThrowsException() {

        assertThrows(ConstraintViolationException.class,
            () -> codec.readValue("{\"subject\":\"   \",\"version\":\"latest\"}", PMSchemaIdentifier.class));
    }

    @Test
    void readValue_SchemaIdentifierWithBlankVersion_ThrowsException() {

        assertThrows(ConstraintViolationException.class,
            () -> codec.readValue("{\"subject\":\"foo\",\"version\":\"\"}", PMSchemaIdentifier.class));
    }


    @Test
    void isSchemaIdentifier_ValidStringIntegerVersion_ReturnsTrue() {

        assertTrue(PMSchemaIdentifier.isPMSchemaIdentifier("SubjectValue/123"));
    }

    @Test
    void isSchemaIdentifier_ValidStringLatestVersion_ReturnsTrue() {

        assertTrue(PMSchemaIdentifier.isPMSchemaIdentifier("SubjectValue/latest"));
    }

    @Test
    void isSchemaIdentifier_InvalidString_ReturnsTrue() {

        assertFalse(PMSchemaIdentifier.isPMSchemaIdentifier("Something/Something"));
    }

}
