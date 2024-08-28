/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.service.augmentation;

import static com.ericsson.oss.air.csac.model.TestResourcesUtils.REGISTRATION_RESPONSE_DTO;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import com.ericsson.oss.air.csac.configuration.augmentation.AugmentationProperties;
import com.ericsson.oss.air.csac.model.augmentation.ArdqRegistrationResponseDto;
import com.ericsson.oss.air.csac.model.augmentation.AugmentationFieldRequestDto;
import com.ericsson.oss.air.csac.model.augmentation.AugmentationRequestDto;
import com.ericsson.oss.air.csac.model.augmentation.AugmentationRuleRequestDto;
import com.ericsson.oss.air.csac.service.exception.HttpResponseErrorHandler;
import com.ericsson.oss.air.util.codec.Codec;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
public class AugmentationRestClientTest {

    private static final String AAS_URL = "http://localhost:8080";

    private static final String ARDQ_REGISTRATION_URI = "/v1/augmentation/registration/ardq";
    private static final String ARDQ_ID = "cardq";

    private static final String ARDQ_URL = "http://card:8080";

    private final AugmentationFieldRequestDto augmentationFieldRequestDto = AugmentationFieldRequestDto.builder()
            .output("nsi")
            .input(List.of("snssai", "moFdn"))
            .build();

    private final AugmentationRuleRequestDto augmentationRuleRequestDto = AugmentationRuleRequestDto.builder()
            .inputSchema("5G|PM_COUNTERS|AMF_Mobility_NetworkSlice_1")
            .fields(List.of(augmentationFieldRequestDto))
            .build();

    private final AugmentationRequestDto augmentationRequestDto = AugmentationRequestDto.builder()
            .ardqId(ARDQ_ID)
            .ardqUrl(ARDQ_URL)
            .rules(List.of(augmentationRuleRequestDto))
            .build();

    private final Codec codec = new Codec();

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private AugmentationProperties augmentationProperties;

    private AugmentationRestClient augmentationRestClient;

    @BeforeEach
    void setup() {
        this.augmentationRestClient = new AugmentationRestClient(restTemplate, augmentationProperties, codec);

        when(this.augmentationProperties.getAasUrl()).thenReturn(AAS_URL);
    }

    @Test
    public void create() {

        final ResponseEntity<Void> response = ResponseEntity.created(null).build();
        when(this.restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), eq(Void.class))).thenReturn(response);

        this.augmentationRestClient.create(augmentationRequestDto);

        verify(this.restTemplate, times(1)).setErrorHandler(any(HttpResponseErrorHandler.class));

        verify(this.restTemplate, times(1))
                .exchange(AAS_URL + ARDQ_REGISTRATION_URI, HttpMethod.POST, getRequestEntity(), Void.class);

    }

    @Test
    public void update() {

        when(this.restTemplate.exchange(anyString(), eq(HttpMethod.PUT), any(), eq(Void.class))).thenReturn(
                ResponseEntity.noContent().build());

        this.augmentationRestClient.update(augmentationRequestDto);

        verify(this.restTemplate, times(1)).setErrorHandler(any(HttpResponseErrorHandler.class));

        verify(this.restTemplate, times(1))
                .exchange(AAS_URL + ARDQ_REGISTRATION_URI, HttpMethod.PUT, getRequestEntity(), Void.class);
    }

    @Test
    public void delete() {

        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        final HttpEntity<?> httpEntity = new HttpEntity<>(headers);

        when(this.restTemplate.exchange(anyString(), eq(HttpMethod.DELETE), any(), eq(Void.class))).thenReturn(
                ResponseEntity.noContent().build());

        this.augmentationRestClient.delete(ARDQ_ID);

        verify(this.restTemplate, times(1)).setErrorHandler(any(HttpResponseErrorHandler.class));
        verify(this.restTemplate, times(1))
                .exchange(AAS_URL + ARDQ_REGISTRATION_URI + '/' + ARDQ_ID, HttpMethod.DELETE, httpEntity, Void.class);
    }

    @Test
    public void getArdqRegistrationById() {
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        final HttpEntity<String> entity = new HttpEntity<>(headers);
        final String requestUrl = "http://localhost:8080/v1/augmentation/registration/ardq/cardq";
        when(this.restTemplate.exchange(requestUrl, HttpMethod.GET, entity, ArdqRegistrationResponseDto.class)).thenReturn(
                new ResponseEntity<>(REGISTRATION_RESPONSE_DTO, HttpStatus.OK));
        final ArdqRegistrationResponseDto response = this.augmentationRestClient.getArdqRegistrationById("cardq");
        Assertions.assertEquals("cardq", response.getArdqId());
        Assertions.assertEquals("localhost:8080", response.getArdqUrl());
        Assertions.assertEquals(1, response.getSchemaMappings().size());
        Assertions.assertEquals("foo", response.getSchemaMappings().get(0).getInputSchema());
        Assertions.assertEquals("bar", response.getSchemaMappings().get(0).getOutputSchema());
        verify(this.restTemplate, times(1)).setErrorHandler(any(HttpResponseErrorHandler.class));
        verify(this.restTemplate, times(1)).exchange(requestUrl, HttpMethod.GET, entity, ArdqRegistrationResponseDto.class);
    }

    private HttpEntity getRequestEntity() {
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        final HttpEntity<AugmentationRequestDto> entity = new HttpEntity<>(augmentationRequestDto, headers);
        return entity;
    }

    @Test
    void getAllArdqIds() {

        final ParameterizedTypeReference<List<String>> param = new ParameterizedTypeReference<List<String>>() {
        };

        when(this.restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(param))).thenReturn(
                new ResponseEntity<>(List.of("ardq1"), HttpStatus.OK));

        final List<String> actual = this.augmentationRestClient.getAllArdqIds();

        verify(this.restTemplate, times(1)).setErrorHandler(any(HttpResponseErrorHandler.class));
        verify(this.restTemplate, times(1)).exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(param));
    }
}
