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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jakarta.validation.constraints.NotBlank;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.NonNull;
import lombok.Value;

/**
 * This class represents a unique PM schema identifier consisting of its subject and its version.
 */
@Value
@JsonPropertyOrder({ "subject", "version"})
public class PMSchemaIdentifier {

    private static final Pattern SCHEMA_IDENTIFIER_PATTERN = Pattern.compile("(.+)/(\\d+|latest)");

    /*
     The registered name of the schema in the Schema Registry
     */
    @NotBlank
    @NonNull
    @JsonProperty(value = "subject")
    String subject;

    /*
     The version of the schema, which can either be "latest" or an integer
     */
    @NotBlank
    @NonNull
    @JsonProperty(value = "version")
    String version;


    /**
     * Parses the provided string and returns a {@link PMSchemaIdentifier}.
     *
     * @param schemaIdentifierString
     *      string to be parsed
     * @return a {@link PMSchemaIdentifier}
     * @throws IllegalArgumentException
     *      if a {@link PMSchemaIdentifier} cannot be created from the provided string
     */
    public static PMSchemaIdentifier parse(final String schemaIdentifierString){

        final Matcher matcher = SCHEMA_IDENTIFIER_PATTERN.matcher(schemaIdentifierString);

        if( !matcher.matches() ) {
            throw new IllegalArgumentException( "Invalid schema identifier [" + schemaIdentifierString + "]" );
        }

        return new PMSchemaIdentifier( matcher.group(1), matcher.group(2) );

    }

    /**
     * Returns true if the provided string can be used to create a {@link PMSchemaIdentifier}.
     *
     * @param schemaIdentifierString
     *      the string to check
     * @return true if the provided string can be used to create a {@link PMSchemaIdentifier} and false, if otherwise
     */
    public static boolean isPMSchemaIdentifier(final String schemaIdentifierString) {
        return SCHEMA_IDENTIFIER_PATTERN.matcher(schemaIdentifierString).matches();
    }
}
