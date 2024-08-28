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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.ericsson.oss.air.csac.model.datacatalog.MessageSchemaDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class DataCatalogServiceTest {

    @Mock
    private DataCatalogRestClient restClient;

    private DataCatalogService service;

    private static final String SCHEMA_REF = "5G|PM_COUNTERS|schemaA";

    @BeforeEach
    void setUp() {
        this.service = new DataCatalogService(restClient);
    }

    @Test
    public void getMessageSchema() {

        final MessageSchemaDTO messageSchemaDTO = MessageSchemaDTO.builder().specificationReference("5G_PM_COUNTERS_schemaA/1").build();

        Mockito.when(this.restClient.getMessageSchema(SCHEMA_REF)).thenReturn(messageSchemaDTO);

        assertEquals(messageSchemaDTO, this.service.getMessageSchema(SCHEMA_REF));
    }

    @Test
    public void getMessageSchemas() {

        final MessageSchemaDTO messageSchemaDTO = MessageSchemaDTO.builder().specificationReference("5G_PM_COUNTERS_schemaA/1").build();
        final Map<String, MessageSchemaDTO> expectedMessageSchemasResponse = new HashMap<>();
        expectedMessageSchemasResponse.put("5G|PM_COUNTERS|schemaA", messageSchemaDTO);

        Mockito.when(this.restClient.getMessageSchema(SCHEMA_REF)).thenReturn(messageSchemaDTO);

        assertEquals(expectedMessageSchemasResponse, this.service.getMessageSchemas(Set.of(SCHEMA_REF)));
    }
}
