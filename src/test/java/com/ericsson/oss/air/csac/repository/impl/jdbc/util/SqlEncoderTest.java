/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.repository.impl.jdbc.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.MissingFormatArgumentException;

import org.junit.jupiter.api.Test;

class SqlEncoderTest {

    @Test
    void encode() {

        final String template = "TRUNCATE %1$s.%2$s CASCADE";

        final String actual = SqlEncoder.encode(template, "schema", "table");

        assertEquals("TRUNCATE schema.table CASCADE", actual);
    }

    @Test
    void encode_missingArgs() throws Exception {

        final String template = "TRUNCATE %1$s.%2$s CASCADE";

        assertThrows(MissingFormatArgumentException.class, () -> SqlEncoder.encode(template));

    }

    @Test
    void encode_emptyArg() throws Exception {

        final String template = "TRUNCATE %1$s.%2$s CASCADE";

        assertThrows(MissingFormatArgumentException.class, () -> SqlEncoder.encode(template, "", "table"));

    }

    @Test
    void encode_missingTemplate_null() throws Exception {

        final String template = "TRUNCATE %1$s.%2$s CASCADE";

        assertThrows(MissingFormatArgumentException.class, () -> SqlEncoder.encode(null, "schema", "table"));
    }

    @Test
    void encode_missingTemplate_empty() throws Exception {

        final String template = "TRUNCATE %1$s.%2$s CASCADE";

        assertThrows(MissingFormatArgumentException.class, () -> SqlEncoder.encode("", "schema", "table"));
    }

    @Test
    void encode_notEnoughArgs() throws Exception {

        final String template = "TRUNCATE %1$s.%2$s CASCADE";

        assertThrows(MissingFormatArgumentException.class, () -> SqlEncoder.encode(template, "schema"));
    }
}