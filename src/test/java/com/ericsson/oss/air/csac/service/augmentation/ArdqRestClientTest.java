/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.service.augmentation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import com.ericsson.oss.air.csac.service.exception.HttpResponseErrorHandler;
import com.ericsson.oss.air.exception.CsacValidationException;
import com.ericsson.oss.air.util.logging.FaultHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
public class ArdqRestClientTest {

    private static final String ARDQ_URL = "http://card:8080";

    public static final String ARDQ_URI = "/v1/augmentation-info/augmentation/query/types";

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private FaultHandler faultHandler;

    @Mock
    private ObjectMapper mapper;

    @InjectMocks
    private ArdqRestClient ardqRestClient;

    @Test
    void getArdqQueryTypes() {
        final String responseString = "[{\"queryType\":\"core\"},{\"queryType\":\"ran\"}]";
        final ResponseEntity<String> response = new ResponseEntity<>(responseString, HttpStatus.OK);

        when(this.restTemplate.getForEntity(ARDQ_URL + ARDQ_URI, String.class)).thenReturn(response);

        final List<String> result = this.ardqRestClient.getArdqQueryTypes(ARDQ_URL);

        verify(this.restTemplate, times(1)).setErrorHandler(any(HttpResponseErrorHandler.class));
        verify(this.restTemplate, times(1)).getForEntity(ARDQ_URL + ARDQ_URI, String.class);

        assertEquals(List.of("core", "ran"), result);
    }

    @Test
    void getArdqQueryTypes_noQueryTypes_returnEmptyList() {
        final String responseString = "[]";
        final ResponseEntity<String> response = new ResponseEntity<>(responseString, HttpStatus.OK);

        when(this.restTemplate.getForEntity(ARDQ_URL + ARDQ_URI, String.class)).thenReturn(response);

        final List<String> result = this.ardqRestClient.getArdqQueryTypes(ARDQ_URL);

        verify(this.restTemplate, times(1)).setErrorHandler(any(HttpResponseErrorHandler.class));
        verify(this.restTemplate, times(1)).getForEntity(ARDQ_URL + ARDQ_URI, String.class);

        assertTrue(result.isEmpty());
    }

    @Test
    void getArdqTypes_throwsException_whenInvalidJsonResponse() {

        final String responseString = "{:";
        final ResponseEntity<String> response = new ResponseEntity<>(responseString, HttpStatus.OK);

        when(this.restTemplate.getForEntity(ARDQ_URL + ARDQ_URI, String.class)).thenReturn(response);

        assertThrows(CsacValidationException.class, () -> this.ardqRestClient.getArdqQueryTypes(ARDQ_URL));

    }

}
