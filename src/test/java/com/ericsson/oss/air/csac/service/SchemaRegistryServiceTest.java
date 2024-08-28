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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import com.ericsson.oss.air.csac.model.pmschema.PMSchemaDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class SchemaRegistryServiceTest {

    private static final PMSchemaDTO TEST_SCHEMA = PMSchemaDTO.builder().name("test_schema").fieldPaths(List.of("PM1")).build();

    private SchemaRegistryService schemaRegistryService;

    @Mock
    private SchemaRegRestClient schemaRegRestClient;

    @BeforeEach
    void setUp() {
        this.schemaRegistryService = new SchemaRegistryService(schemaRegRestClient);
    }

    @Test
    public void getSchemaBySubjectLatest_ReturnsSuccessResponse() {
        Mockito.when(this.schemaRegRestClient.getSchema("something|something|NetworkSlice")).thenReturn(TEST_SCHEMA);
        assertThat(this.schemaRegistryService.getSchemaLatest("something|something|NetworkSlice")).isEqualTo(TEST_SCHEMA);
    }

}
