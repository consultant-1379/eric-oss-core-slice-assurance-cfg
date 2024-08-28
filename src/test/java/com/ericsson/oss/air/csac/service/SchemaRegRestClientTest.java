/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.List;

import com.ericsson.oss.air.csac.model.pmschema.PMSchemaDTO;
import com.ericsson.oss.air.csac.service.exception.HttpResponseErrorHandler;
import com.ericsson.oss.air.exception.http.NotFoundException;
import com.ericsson.oss.air.exception.http.ServiceUnavailableException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
public class SchemaRegRestClientTest {

    private static final String SCHEMA_REGISTRY_URL = "http://localhost:8080";

    private static final PMSchemaDTO TEST_SCHEMA = PMSchemaDTO.builder().name("test_schema").fieldPaths(List.of("PM1")).build();

    @Mock
    private RestTemplate restTemplate;

    private SchemaRegRestClient schemaRegRestClient;

    @BeforeEach
    void setUp() {
        this.schemaRegRestClient = new SchemaRegRestClient(restTemplate, SCHEMA_REGISTRY_URL);
    }

    @Test
    public void getSchemaRestClientTest_success() {
        final URI uri = URI.create(SCHEMA_REGISTRY_URL + "/subjects/testSchema/versions/latest/schema");
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        final HttpEntity<URI> requestEntity = new HttpEntity<>(uri, headers);
        when(this.restTemplate.exchange(uri, HttpMethod.GET, requestEntity, PMSchemaDTO.class)).thenReturn(
                new ResponseEntity<>(TEST_SCHEMA, HttpStatus.OK));

        assertEquals(TEST_SCHEMA, this.schemaRegRestClient.getSchema("testSchema"));
        verify(this.restTemplate, times(1)).exchange(uri, HttpMethod.GET, requestEntity, PMSchemaDTO.class);
        verify(this.restTemplate, times(1)).setErrorHandler(any(HttpResponseErrorHandler.class));
    }

    @Test
    public void getSchemaRestClientTest_notFoundException() {
        when(this.restTemplate.exchange(eq(URI.create(SCHEMA_REGISTRY_URL + "/subjects/invalid/versions/latest/schema")), eq(HttpMethod.GET), any(),
                eq(PMSchemaDTO.class))).thenThrow(NotFoundException.class);

        assertThrows(NotFoundException.class, () -> this.schemaRegRestClient.getSchema("invalid"));
        verify(this.restTemplate, times(1)).setErrorHandler(any(HttpResponseErrorHandler.class));
    }

    @Test
    public void getSchemaRestClientTest_serviceUnavailableException() {
        when(this.restTemplate.exchange(eq(URI.create(SCHEMA_REGISTRY_URL + "/subjects/something/versions/latest/schema")), eq(HttpMethod.GET), any(),
                eq(PMSchemaDTO.class))).thenThrow(ServiceUnavailableException.class);

        assertThrows(ServiceUnavailableException.class, () -> this.schemaRegRestClient.getSchema("something"));
        verify(this.restTemplate, times(1)).setErrorHandler(any(HttpResponseErrorHandler.class));

    }
}
