/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.configuration.schema;

import java.util.Set;

import com.ericsson.oss.air.csac.model.PMDefinition;
import com.ericsson.oss.air.csac.model.ProfileDefinition;
import com.ericsson.oss.air.csac.model.pmschema.PmSchema;

public interface InputSchemaProvider {
    void prefetchInputSchemas(Set<String> schemaReferenceSet);

    String getSchemaReference(ProfileDefinition profile, PMDefinition pmDefinition);

    PmSchema getSchema(String schemaReference);
}
