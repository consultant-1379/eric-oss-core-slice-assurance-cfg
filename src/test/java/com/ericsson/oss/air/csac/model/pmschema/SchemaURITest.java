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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SchemaURITest {

    @Mock
    private JsonGenerator jsonGenerator;

    private static final SchemaURI PM_SCHEMA_URI = SchemaURI.fromString("dc:5G|PM_COUNTERS|smf_nsmf_pdu_session_snssai_apn_1");

    @Test
    void schemaUri_fromString_invalid() {

        final String invalidUri = "5G|PM_COUNTERS|smf_nsmf_pdu_session_snssai_apn_1";
        final IllegalArgumentException expectedException = new IllegalArgumentException("Invalid schema URI: " + invalidUri);

        final IllegalArgumentException actualException = assertThrows(IllegalArgumentException.class, () -> SchemaURI.fromString(invalidUri));

        assertEquals(expectedException.getMessage(), actualException.getMessage());
    }

    @Test
    void schemaUri_toString() {
        assertEquals("dc:5G|PM_COUNTERS|smf_nsmf_pdu_session_snssai_apn_1", PM_SCHEMA_URI.toString());
    }

    @Test
    void schemaUri_getScheme() {
        assertEquals(SchemaURI.CustomScheme.DC, PM_SCHEMA_URI.getScheme());
    }

    @Test
    void schemaUri_getSchemeSpecificPart() {
        assertEquals("5G|PM_COUNTERS|smf_nsmf_pdu_session_snssai_apn_1", PM_SCHEMA_URI.getSchemeSpecificPart());
    }

    @Test
    void customScheme_toString() {
        assertEquals("dc", SchemaURI.CustomScheme.DC.toString());
    }

    @Test
    void customScheme_fromString() {
        assertEquals(SchemaURI.CustomScheme.DC, SchemaURI.CustomScheme.fromString("dc"));
        assertEquals(SchemaURI.CustomScheme.DC, SchemaURI.CustomScheme.fromString("DC"));
    }

    @Test
    void customScheme_validate_valid() {
        SchemaURI.CustomScheme.DC.validate("5G|PM_COUNTERS|smf_nsmf_pdu_session_snssai_apn_1");
    }

    @Test
    void customScheme_validate_invalid() {

        final String schemeSpecificPart = "5GPM_COUNTERS|smf_nsmf_pdu_session_snssai_apn_1";
        final IllegalArgumentException expectedException = new IllegalArgumentException(
                "Invalid scheme specific part for scheme [dc]: 5GPM_COUNTERS|smf_nsmf_pdu_session_snssai_apn_1");

        final IllegalArgumentException actualException = assertThrows(IllegalArgumentException.class,
                () -> SchemaURI.CustomScheme.DC.validate(schemeSpecificPart));

        assertEquals(expectedException.getMessage(), actualException.getMessage());
    }

    @Test
    void serialize_SchemaURI() throws IOException {

        final SchemaURISerializer schemaURISerializer = new SchemaURISerializer();
        schemaURISerializer.serialize(PM_SCHEMA_URI, this.jsonGenerator, null);

        verify(this.jsonGenerator, times(1)).writeString(PM_SCHEMA_URI.toString());
    }

}