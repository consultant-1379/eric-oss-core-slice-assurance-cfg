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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Simple data bean representing a schema reference in the form
 *
 * {@code <data space>|<data category>|<schema Id>}
 */
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class SchemaReference {

    static final Pattern SCHEMA_REF_PATTERN = Pattern.compile("(.+)\\|(.+)\\|(.+)");

    private String dataSpace;
    private String dataCategory;
    private String schemaId;

    /**
     * Returns a {@code SchemaReference} parsed from the provided schema reference string.
     *
     * @param schemaReference
     *         schema reference string
     * @return a {@code SchemaReference} parsed from the provided schema reference string
     */
    public static SchemaReference of(final String schemaReference) {

        final Matcher refMatcher = SCHEMA_REF_PATTERN.matcher(schemaReference);
        if (!refMatcher.matches()) {
            throw new IllegalArgumentException("Invalid schema reference: " + schemaReference);
        }

        return SchemaReference.builder()
                .dataSpace(refMatcher.group(1))
                .dataCategory(refMatcher.group(2))
                .schemaId(refMatcher.group(3))
                .build();
    }

    @Override
    public String toString() {
        return String.join("|", this.dataSpace, this.dataCategory, this.schemaId);
    }
}
