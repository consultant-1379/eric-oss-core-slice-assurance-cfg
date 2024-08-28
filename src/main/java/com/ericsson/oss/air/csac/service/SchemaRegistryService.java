/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.service;

import com.ericsson.oss.air.csac.model.pmschema.PMSchemaDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

/**
 * Represents services between CSAC and Schema Registry
 */
@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(prefix = "validation.external.restClient.schemaregistry",
                       name = "url")
@Lazy
public class SchemaRegistryService {

    private final SchemaRegRestClient schemaRegRestClient;

    /**
     * Retrieve the latest version of the schema by its subject name
     *
     * @param subject the subject that schema saved in
     * @return a {@link PMSchemaDTO}
     */
    public PMSchemaDTO getSchemaLatest(final String subject) {
        return this.schemaRegRestClient.getSchema(subject);
    }

}
