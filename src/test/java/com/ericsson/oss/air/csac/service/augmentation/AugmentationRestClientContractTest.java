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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner;
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK,
                classes = { AugmentationRestClientContractTest.class })
@ActiveProfiles("test")
@AutoConfigureStubRunner(repositoryRoot = "https://arm.seli.gic.ericsson.se/artifactory/proj-eric-oss-dev-local",
                         stubsMode = StubRunnerProperties.StubsMode.REMOTE,
                         ids = "com/ericsson/oss/air:eric-oss-assurance-augmentation:+:stubs:8080")
public class AugmentationRestClientContractTest {

    private final static String AAS_URL = "http://localhost:8080/v1/augmentation/registration/";

    private final static String AAS_ARDQ_URL = "http://localhost:8080/v1/augmentation/registration/ardq";

    private final static String AAS_BODY = "{\"ardqId\":\"cardq\",\"ardqUrl\":\"http://eric-oss-cardq:8080\",\"rules\":[{\"inputSchema\":\"5G|PM_COUNTERS|AMF_Mobility_NetworkSlice_1\",\"fields\":[{\"output\":\"nsi\",\"input\":[\"snssai\",\"moFDN\"]}]}]}";

    private final static String AAS_BODY_400_BAD_REQUEST = "{\"ardqId\":\"cardq\",\"ardqUrl\":\"http://eric-oss-cardq\",\"rules\":[{\"inputSchema\":\"5G|PM_COUNTERS|AMF_Mobility_NetworkSlice_1\",\"fields\":[{\"output\":\"nsi\",\"input\":[\"snssai\",\"moFDN\"]}]}]}";

    private final static String AAS_BODY_409_CONFLICT = "{\"ardqId\":\"cardq\",\"ardqUrl\":\"http://eric-oss-cardq:8080\",\"ardqType\":\"core\",\"rules\":[{\"inputSchema\":\"5G|PM_COUNTERS|AMF_Mobility_NetworkSlice_1\",\"fields\":[{\"output\":\"outputField1\",\"input\":[\"inputField1\",\"inputField2\"]}]}]}";

    private final static String AAS_BODY_404_NOT_FOUND = "{\"ardqId\":\"nonExistedArdqId\",\"ardqUrl\":\"http://eric-oss-cardq:8080\",\"rules\":[{\"inputSchema\":\"5G|PM_COUNTERS|AMF_Mobility_NetworkSlice_1\",\"fields\":[{\"output\":\"nsi\",\"input\":[\"snssai\",\"moFDN\"]}]}]}";

    private final static HttpHeaders HEADERS = new HttpHeaders();

    @BeforeEach
    public void setUp() throws Exception {
        HEADERS.setContentType(MediaType.APPLICATION_JSON);
    }

    @Test
    public void getRegistrationByArdqId() {
        final String expectedResponse = "{\"ardqId\":\"cardq\",\"ardqUrl\":\"http://eric-oss-cardq:8080\",\"rules\":[{\"inputSchema\":\"5G|PM_COUNTERS|AMF_Mobility_NetworkSlice_1\",\"fields\":[{\"output\":\"nsi\",\"input\":[\"snssai\",\"moFDN\"]}]}],\"schemaMappings\":[{\"inputSchema\":\"5G|PM_COUNTERS|AMF_Mobility_NetworkSlice_1\",\"outputSchema\":\"5G|PM_COUNTERS|cardq_AMF_Mobility_NetworkSlice_1\"}]}";

        final ResponseEntity<String> response = new TestRestTemplate().exchange(
                AAS_ARDQ_URL + "/cardq",
                HttpMethod.GET,
                new HttpEntity<>(null, HEADERS),
                String.class
        );
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedResponse, response.getBody());
    }

    @Test
    public void getArdqRegistrationById_notFound() {
        final ResponseEntity<String> response = new TestRestTemplate().exchange(
                AAS_ARDQ_URL + "/invalidId",
                HttpMethod.GET,
                new HttpEntity<>(null, HEADERS),
                String.class
        );

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void getAllArdqIds() {
        final Set<String> expected = Set.of("cardq", "other-ardq-id");

        final ResponseEntity<List<String>> response = new TestRestTemplate().exchange(
                AAS_URL + "ardq-ids",
                HttpMethod.GET,
                new HttpEntity<>(null, HEADERS),
                new ParameterizedTypeReference<List<String>>() {
                }
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expected, new HashSet<>(response.getBody()));
    }

    @Test
    public void createArdqRegistration_201_created() {
        final ResponseEntity<String> response = new TestRestTemplate().exchange(
                AAS_ARDQ_URL,
                HttpMethod.POST,
                new HttpEntity<>(AAS_BODY, HEADERS),
                String.class
        );

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }

    @Test
    public void createArdqRegistration_400_badRequest() {
        final ResponseEntity<String> response = new TestRestTemplate().exchange(
                AAS_ARDQ_URL,
                HttpMethod.POST,
                new HttpEntity<>(AAS_BODY_400_BAD_REQUEST, HEADERS),
                String.class
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void createArdqRegistration_409_conflict() {
        final ResponseEntity<String> response = new TestRestTemplate().exchange(
                AAS_ARDQ_URL,
                HttpMethod.POST,
                new HttpEntity<>(AAS_BODY_409_CONFLICT, HEADERS),
                String.class
        );

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    @Test
    public void updateArdqRegistration_200_ok() {
        final ResponseEntity<String> response = new TestRestTemplate().exchange(
                AAS_ARDQ_URL,
                HttpMethod.PUT,
                new HttpEntity<>(AAS_BODY, HEADERS),
                String.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void updateArdqRegistration_400_badRequest() {
        final ResponseEntity<String> response = new TestRestTemplate().exchange(
                AAS_ARDQ_URL,
                HttpMethod.PUT,
                new HttpEntity<>(AAS_BODY_400_BAD_REQUEST, HEADERS),
                String.class
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void updateArdqRegistration_404_notFound() {
        final ResponseEntity<String> response = new TestRestTemplate().exchange(
                AAS_ARDQ_URL,
                HttpMethod.PUT,
                new HttpEntity<>(AAS_BODY_404_NOT_FOUND, HEADERS),
                String.class
        );

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void deleteArdqRegistrationById_204_noContent() {
        final ResponseEntity<String> response = new TestRestTemplate().exchange(
                AAS_ARDQ_URL + "/cardq",
                HttpMethod.DELETE,
                new HttpEntity<>(null, HEADERS),
                String.class
        );

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    public void deleteArdqRegistrationById_404_notFound() {
        final ResponseEntity<String> response = new TestRestTemplate().exchange(
                AAS_ARDQ_URL + "/invalidId",
                HttpMethod.DELETE,
                new HttpEntity<>(null, HEADERS),
                String.class
        );

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
}
