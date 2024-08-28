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

import java.util.Arrays;
import java.util.MissingFormatArgumentException;

import org.springframework.util.ObjectUtils;

/**
 * Utility class for handling SQL statements.
 */
public class SqlEncoder {

    private SqlEncoder() {
        // no-op.  Hides the Hides implicit default ctor.
    }

    /**
     * Returns a string after substituting the provided arguments in the specified template.  The template must be provided in the form required by
     * {@code String.format}.  The main purpose of this class is to provide detailed validation of the template and arguments.
     *
     * @param sqlTemplate
     *         template string
     * @param args
     *         arguments for substitution
     * @return a string after substituting the provided arguments in the specified template
     * @throws MissingFormatArgumentException
     *         if the template or any arguments are missing or null.
     */
    public static String encode(final String sqlTemplate, final Object... args) {

        if (ObjectUtils.isEmpty(sqlTemplate)) {
            throw new MissingFormatArgumentException("Missing template for SQL encoding");
        }

        if (ObjectUtils.isEmpty(args) || Arrays.stream(args).anyMatch(ObjectUtils::isEmpty)) {
            throw new MissingFormatArgumentException("Missing arguments for SQL encoding");
        }

        return String.format(sqlTemplate, args);
    }
}
