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

import static com.ericsson.oss.air.csac.model.pmschema.SchemaReference.SCHEMA_REF_PATTERN;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

/**
 * Represents a PM Schema URI. This URI follows the standard convention for URIs, comprising
 * <code>
 * scheme:[//[user info:]authority/][path]
 * </code>
 * <p/>
 * Example in string form: "dc:5G|PM_COUNTERS|smf_nsmf_pdu_session_snssai_apn_1"
 * <p/>
 * The custom scheme 'dc' is a shorthand for the DMM Data Catalog. The Data Catalog schema requires the syntax as shown in the example.
 */
@EqualsAndHashCode
@Getter
@FieldDefaults(makeFinal = true,
               level = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@JsonSerialize(using = SchemaURISerializer.class)
public class SchemaURI {

    // RFC 2396 Section 3 and 3.1
    private static final Pattern SCHEMA_URI_PATTERN = Pattern.compile("^([a-z][a-z0-9+\\-.]*):(.+)");

    CustomScheme scheme;

    String schemeSpecificPart;

    /**
     * Returns a {@code SchemaURI} parsed from the provided schema URI string.
     *
     * @param uriString schema URI string
     * @return a {@code SchemaURI} parsed from the provided schema URI string
     */
    @JsonCreator
    public static SchemaURI fromString(final String uriString) {

        final Matcher matcher = SCHEMA_URI_PATTERN.matcher(uriString);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid schema URI: " + uriString);
        }

        final CustomScheme customScheme = CustomScheme.fromString(matcher.group(1));
        final String schemeSpecificPart = matcher.group(2);

        customScheme.validate(schemeSpecificPart);

        return new SchemaURI(customScheme, schemeSpecificPart);
    }

    @Override
    public String toString() {
        return this.scheme + ":" + this.schemeSpecificPart;
    }

    /**
     * Represents CSAC's supported schemes for identifying unique PM schemas
     */
    @RequiredArgsConstructor
    public enum CustomScheme {
        DC("dc", SCHEMA_REF_PATTERN);

        private final String scheme;

        private final Pattern schemeSpecificPartPattern;

        @Override
        public String toString() {
            return this.scheme;
        }

        /**
         * Returns the {@code CustomScheme} that matches the provided string.
         *
         * @param scheme the scheme in string format
         * @return the {@code CustomScheme} that matches the provided string
         */
        public static CustomScheme fromString(final String scheme) {
            return CustomScheme.valueOf(scheme.toUpperCase(Locale.ENGLISH));
        }

        /**
         * Validates that the provided {@code schemeSpecificPart} can be utilized with this scheme to uniquely identify a PM schema.
         *
         * @param schemeSpecificPart the part of the URI used in conjunction with the scheme to uniquely identify a PM schema
         * @throws IllegalArgumentException if the {@code schemeSpecificPart} is invalid
         */
        public void validate(final String schemeSpecificPart) {

            final Matcher refMatcher = this.schemeSpecificPartPattern.matcher(schemeSpecificPart);
            if (!refMatcher.matches()) {
                throw new IllegalArgumentException("Invalid scheme specific part for scheme [" + this.toString() + "]: "
                        + schemeSpecificPart);
            }
        }
    }

}
