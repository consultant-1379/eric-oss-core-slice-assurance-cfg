/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.repository;

/**
 * DAO API for clearing configuration from all persistent stores.
 */
public interface ResetDAO {

    enum SchemaType {
        DICTIONARY,
        RUNTIME
    }

    /**
     * Clears all data from all schema types.
     */
    default void clear() {
        this.clear(SchemaType.RUNTIME);
        this.clear(SchemaType.DICTIONARY);
    }

    /**
     * Clears all data from the specified schema type.  An error may occur if the specified schema has a dependency, for example a foreign key
     * relationship, on another schema that prevents it from being cleared.
     *
     * @param schemaType
     */
    void clear(SchemaType schemaType);
}
