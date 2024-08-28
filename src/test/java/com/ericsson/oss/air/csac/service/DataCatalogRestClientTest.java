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

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.client.ExpectedCount.manyTimes;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.ericsson.oss.air.csac.service.exception.HttpResponseErrorHandler;
import com.ericsson.oss.air.exception.CsacValidationException;
import com.ericsson.oss.air.exception.http.InternalServerErrorException;
import com.ericsson.oss.air.exception.http.ServiceUnavailableException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

@RestClientTest(DataCatalogRestClient.class)
@ActiveProfiles("test")
public class DataCatalogRestClientTest {

    @Value("${validation.external.restClient.datacatalog.url}")
    private String dataCatalogUrl;

    @Autowired
    private DataCatalogRestClient client;

    @Autowired
    private MockRestServiceServer server;

    @SpyBean
    private RestTemplate restTemplate;

    @Test
    public void getMessageSchema_internalServerError() {
        final String validURL = dataCatalogUrl
                + "/catalog/v1/data-type?dataSpace=5G&dataCategory=PM_COUNTERS&schemaName=schemaA";
        this.server.expect(manyTimes(), requestTo(validURL)).andRespond(withServerError());
        assertThrows(InternalServerErrorException.class, () -> this.client.getMessageSchema("5G|PM_COUNTERS|schemaA"));

        verify(this.restTemplate, times(1)).setErrorHandler(any(HttpResponseErrorHandler.class));
    }

    @Test
    public void getMessageSchema_ServiceUnavailable() {
        final String validURL = dataCatalogUrl
                + "/catalog/v1/data-type?dataSpace=5G&dataCategory=PM_COUNTERS&schemaName=schemaA";
        this.server.expect(manyTimes(), requestTo(validURL)).andRespond(withStatus(HttpStatus.SERVICE_UNAVAILABLE));
        assertThrows(ServiceUnavailableException.class, () -> this.client.getMessageSchema("5G|PM_COUNTERS|schemaA"));

        verify(this.restTemplate, times(1)).setErrorHandler(any(HttpResponseErrorHandler.class));
    }

    @Test
    public void getMessageSchema_InvalidSchemaRefFormat() {
        final String invalidSchemaRef = "schemaRef";
        assertThrows(CsacValidationException.class, () -> this.client.getMessageSchema(invalidSchemaRef));
    }

    @Test
    public void getMessageSchema_EmptyResponse() {
        final String schemaReference = "G5|PM_COUNTERS|schemaA";
        final String validURL = dataCatalogUrl + "/catalog/v1/data-type?dataSpace=G5&dataCategory=PM_COUNTERS&schemaName=schemaA";
        this.server.expect(manyTimes(), requestTo(validURL)).andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));
        assertNull(this.client.getMessageSchema(schemaReference));

        verify(this.restTemplate, times(1)).setErrorHandler(any(HttpResponseErrorHandler.class));
    }

}
