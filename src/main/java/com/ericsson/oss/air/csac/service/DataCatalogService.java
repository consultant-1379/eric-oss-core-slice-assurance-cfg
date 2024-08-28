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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.ericsson.oss.air.csac.model.datacatalog.MessageSchemaDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/**
 * Represents data catalog service
 */
@Service
@Slf4j
@ConditionalOnProperty(prefix = "validation.external.restClient.datacatalog",
                       name = "url")
@Profile({ "prod", "test" })
@RequiredArgsConstructor
public class DataCatalogService {

    private final DataCatalogRestClient dataCatalogRestClient;

    /**
     * Retrieves a list of message schema information for a list of schema references
     *
     * @param schemaReferenceSet a list of schema references
     * @return a map contains the information of schema reference and associated message schema from data catalog
     */
    public Map<String, MessageSchemaDTO> getMessageSchemas(final Set<String> schemaReferenceSet) {
        final Map<String, MessageSchemaDTO> messageSchemas = new HashMap<>();
        schemaReferenceSet.forEach(
                schemaReference -> messageSchemas.put(schemaReference, this.dataCatalogRestClient.getMessageSchema(schemaReference)));
        return messageSchemas;
    }

    /**
     * Retrieves message schema from the data catalog for the given schema reference
     *
     * @param schemaReferenceStr message schema reference
     * @return {@link MessageSchemaDTO}
     */
    public MessageSchemaDTO getMessageSchema(final String schemaReferenceStr) {
        return this.dataCatalogRestClient.getMessageSchema(schemaReferenceStr);
    }
}
