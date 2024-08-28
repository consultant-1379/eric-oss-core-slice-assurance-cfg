/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/
package com.ericsson.oss.air.csac.service.index;

import static com.ericsson.oss.air.csac.model.TestResourcesUtils.DEPLOYED_INDEX_DEFINITION_DTO_A;
import static com.ericsson.oss.air.csac.model.TestResourcesUtils.DEPLOYED_INDEX_DEFINITION_DTO_B;
import static com.ericsson.oss.air.csac.service.index.LiveIndexerRestClient.AIS_INDEXER_URI;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.List;

import com.ericsson.oss.air.csac.model.TestResourcesUtils;
import com.ericsson.oss.air.csac.model.runtime.index.DeployedIndexDefinitionDto;
import com.ericsson.oss.air.csac.repository.DeployedIndexDefinitionDao;
import com.ericsson.oss.air.csac.service.exception.HttpResponseErrorHandler;
import com.ericsson.oss.air.exception.http.BadRequestException;
import com.ericsson.oss.air.exception.http.ServiceUnavailableException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@ExtendWith(MockitoExtension.class)
public class LiveIndexerRestClientTest {

    private static final String INDEXER_URL = "http://127.0.0.1:8080";

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private DeployedIndexDefinitionDao definitionDao;

    private LiveIndexerRestClient liveIndexerRestClient;

    @BeforeEach
    void setUp() {
        this.liveIndexerRestClient = new LiveIndexerRestClient(restTemplate, definitionDao);
        ReflectionTestUtils.setField(this.liveIndexerRestClient, "isLegacyIndexClient", true);
        ReflectionTestUtils.setField(this.liveIndexerRestClient, "indexerUrl", INDEXER_URL);
    }

    @Test
    void createIndexDefinition_emptyIndexDefinition_badRequest() {
        final DeployedIndexDefinitionDto emptyIndexDefinition = DeployedIndexDefinitionDto.builder().indexDefinitionName("test").build();
        when(this.restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), eq(Void.class))).thenThrow(BadRequestException.class);

        assertThrows(BadRequestException.class, () -> this.liveIndexerRestClient.create(emptyIndexDefinition));

        verify(this.restTemplate, times(1)).setErrorHandler(any(HttpResponseErrorHandler.class));
    }

    @Test
    void updateIndexDefinition_emptyIndexDefinition_badRequest() {
        final DeployedIndexDefinitionDto emptyIndexDefinition = DeployedIndexDefinitionDto.builder().indexDefinitionName("test").build();
        when(this.restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), eq(Void.class))).thenThrow(BadRequestException.class);

        assertThrows(BadRequestException.class, () -> this.liveIndexerRestClient.update(emptyIndexDefinition));

        verify(this.restTemplate, times(1)).setErrorHandler(any(HttpResponseErrorHandler.class));

    }

    @Test
    void createIndexDefinition_success() {
        final ResponseEntity<Void> response = ResponseEntity.ok().build();
        when(this.restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), eq(Void.class))).thenReturn(response);

        this.liveIndexerRestClient.create(TestResourcesUtils.DEPLOYED_INDEX_DEFINITION_DTO_A);

        verify(this.restTemplate, times(1)).setErrorHandler(any(HttpResponseErrorHandler.class));
        verify(this.restTemplate, times(1)).exchange(INDEXER_URL + AIS_INDEXER_URI, HttpMethod.POST, getRequestEntity(), Void.class);
    }

    @Test
    void updateIndexDefinition_success() {
        final ResponseEntity<Void> response = ResponseEntity.ok().build();
        when(this.restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), eq(Void.class))).thenReturn(response);

        this.liveIndexerRestClient.update(TestResourcesUtils.DEPLOYED_INDEX_DEFINITION_DTO_A);

        verify(this.restTemplate, times(1)).setErrorHandler(any(HttpResponseErrorHandler.class));
        verify(this.restTemplate, times(1)).exchange(INDEXER_URL + AIS_INDEXER_URI, HttpMethod.POST, getRequestEntity(), Void.class);
    }

    @Test
    void createIndexDefinition_validIndexDefinition_serviceUnavailable() {
        when(this.restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), eq(Void.class))).thenThrow(ServiceUnavailableException.class);

        assertThrows(ServiceUnavailableException.class,
                () -> this.liveIndexerRestClient.create(TestResourcesUtils.DEPLOYED_INDEX_DEFINITION_DTO_A));

        verify(this.restTemplate, times(1)).setErrorHandler(any(HttpResponseErrorHandler.class));
        verify(this.restTemplate, times(1)).exchange(INDEXER_URL + AIS_INDEXER_URI, HttpMethod.POST, getRequestEntity(), Void.class);
    }

    @Test
    void updateIndexDefinition_success_newIndexClient() {
        ReflectionTestUtils.setField(this.liveIndexerRestClient, "isLegacyIndexClient", false);

        final ResponseEntity<Void> response = ResponseEntity.ok().build();
        when(this.restTemplate.exchange(anyString(), eq(HttpMethod.PUT), any(), eq(Void.class))).thenReturn(response);

        this.liveIndexerRestClient.update(TestResourcesUtils.DEPLOYED_INDEX_DEFINITION_DTO_A);

        verify(this.restTemplate, times(1)).setErrorHandler(any(HttpResponseErrorHandler.class));
        verify(this.restTemplate, times(1)).exchange(INDEXER_URL + AIS_INDEXER_URI, HttpMethod.PUT, getRequestEntity(), Void.class);
    }

    @Test
    void deleteById() {
        final URI expected_uri = URI.create("http://127.0.0.1:8080/v1/indexer-info/indexer?name=idx-name");

        doNothing().when(this.restTemplate).delete(expected_uri);

        this.liveIndexerRestClient.deleteById(List.of("idx-name"));

        final URI actual_uri = UriComponentsBuilder.fromUriString(INDEXER_URL).path(AIS_INDEXER_URI).queryParam("name", "idx-name").build("idx-name");
        verify(this.restTemplate, times(1)).delete(actual_uri);
        assertEquals(expected_uri, actual_uri);
        verify(this.restTemplate, times(1)).setErrorHandler(any(HttpResponseErrorHandler.class));

    }

    @Test
    void deleteById_emptyList() {
        this.liveIndexerRestClient.deleteById(List.of());
        verify(this.restTemplate, times(0)).delete(any());
    }

    @Test
    void deleteById_validIndexDefinition_serviceUnavailable() {
        doThrow(ServiceUnavailableException.class).when(this.restTemplate).delete(any());

        assertThrows(ServiceUnavailableException.class, () -> this.liveIndexerRestClient.deleteById(List.of("idx-name")));

        verify(this.restTemplate, times(1)).setErrorHandler(any(HttpResponseErrorHandler.class));
        verify(this.restTemplate, times(1)).delete(URI.create("http://127.0.0.1:8080/v1/indexer-info/indexer?name=idx-name"));
    }

    @Test
    void delete() {
        final URI uri = URI.create("http://127.0.0.1:8080/v1/indexer-info/indexer?name=nameOfIndexerA");
        doNothing().when(this.restTemplate).delete(uri);

        this.liveIndexerRestClient.delete(List.of(DEPLOYED_INDEX_DEFINITION_DTO_A));
        verify(this.restTemplate, times(1)).delete(uri);
        verify(this.restTemplate, times(1)).setErrorHandler(any(HttpResponseErrorHandler.class));
    }

    @Test
    void deleteAll() {
        when(this.definitionDao.findAll()).thenReturn(List.of(DEPLOYED_INDEX_DEFINITION_DTO_A, DEPLOYED_INDEX_DEFINITION_DTO_B));
        this.liveIndexerRestClient.deleteAll();

        verify(this.restTemplate, times(1)).delete(URI.create("http://127.0.0.1:8080/v1/indexer-info/indexer?name=nameOfIndexerA"));
        verify(this.restTemplate, times(1)).delete(URI.create("http://127.0.0.1:8080/v1/indexer-info/indexer?name=nameOfIndexerB"));
        verify(this.restTemplate, times(2)).setErrorHandler(any(HttpResponseErrorHandler.class));
    }

    private HttpEntity getRequestEntity() {
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(TestResourcesUtils.DEPLOYED_INDEX_DEFINITION_DTO_A, headers);
    }
}
