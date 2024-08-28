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
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URI;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner;
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.util.UriComponentsBuilder;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles("test")
@AutoConfigureStubRunner(repositoryRoot = "https://arm.seli.gic.ericsson.se/artifactory/proj-eric-oss-dev-local",
                         stubsMode = StubRunnerProperties.StubsMode.REMOTE,
                         ids = "com/ericsson/oss/dmi:eric-oss-data-catalog:+:stubs:9590")
public class DataCatalogRestClientContractTest {

    private final static String DATA_CATALOG_URL = "http://localhost:9590/catalog/v1/data-type";

    @Disabled("This test case was disabled as it is not stable")
    @Test
    public void get_All_DataType_by_Query_Parameters() {
        final String expectedResponse =
                "[{\"id\":1,\"mediumType\":\"file\",\"mediumId\":2,\"schemaName\":\"SCHMNM\",\"schemaVersion\":\"2\",\"isExternal\":true,"
                        + "\"consumedDataSpace\":\"5G\",\"consumedDataCategory\":\"5G\",\"consumedDataProvider\":\"5G\",\"consumedSchemaName\":\"5G\","
                        + "\"consumedSchemaVersion\":\"2\",\"fileFormat\":{\"id\":2,\"dataService\":{\"id\":1,\"dataServiceName\":\"dsName\"},\"dataType\":{\"id\":2,"
                        + "\"mediumType\":\"file\",\"mediumId\":3,\"schemaName\":\"SCHMNM\",\"schemaVersion\":\"2\",\"isExternal\":true,\"consumedDataSpace\":\"5G\","
                        + "\"consumedDataCategory\":\"5G\",\"consumedDataProvider\":\"5G\",\"consumedSchemaName\":\"5G\",\"consumedSchemaVersion\":\"2\"},"
                        + "\"bulkDataRepository\":{\"id\":1,\"name\":\"testBDR\",\"clusterName\":\"testCluster\",\"nameSpace\":\"testNS\","
                        + "\"accessEndpoints\":[\"http://endpoint1:1234/\"],\"fileFormatIds\":[1,2,3],\"fileRepoType\":\"S3\"},\"reportOutputPeriodList\":[0],"
                        + "\"notificationTopic\":{\"id\":2,\"name\":\"name\",\"specificationReference\":\"specRef\",\"encoding\":\"JSON\",\"dataProviderType\":{\"id\":6,"
                        + "\"dataSpace\":{\"id\":2,\"name\":\"5G\",\"dataProviderTypeIds\":[6]},\"dataCategoryType\":{\"id\":1,\"dataCategoryName\":\"PM_COUNTERs\"},"
                        + "\"notificationTopicIds\":[2],\"messageDataTopicIds\":[],\"providerVersion\":\"V2\",\"providerTypeId\":\"v2\"},\"fileFormatIds\":[2,3],"
                        + "\"messageBus\":{\"id\":1,\"name\":\"name\",\"clusterName\":\"clusterName\",\"nameSpace\":\"nameSpace\",\"accessEndpoints\":[\"http://endpoint1:1234/\"],"
                        + "\"notificationTopicIds\":[1,2],\"messageStatusTopicIds\":[1,2],\"messageDataTopicIds\":[1,2]}},\"specificationReference\":\"specRef\","
                        + "\"dataEncoding\":\"JSON\"}},{\"id\":2,\"mediumType\":\"file\",\"mediumId\":3,\"schemaName\":\"SCHMNM\",\"schemaVersion\":\"2\",\"isExternal\":true,"
                        + "\"consumedDataSpace\":\"5G\",\"consumedDataCategory\":\"5G\",\"consumedDataProvider\":\"5G\",\"consumedSchemaName\":\"5G\",\"consumedSchemaVersion\":\"2\","
                        + "\"fileFormat\":{\"id\":3,\"dataService\":{\"id\":2,\"dataServiceName\":\"ds\"},\"dataType\":{\"id\":3,\"mediumType\":\"stream\",\"mediumId\":3,"
                        + "\"schemaName\":\"SCH2\",\"schemaVersion\":\"2\",\"isExternal\":true,\"consumedDataSpace\":\"4G\",\"consumedDataCategory\":\"4G\","
                        + "\"consumedDataProvider\":\"4G\",\"consumedSchemaName\":\"4G\",\"consumedSchemaVersion\":\"2\"},\"bulkDataRepository\":{\"id\":1,\"name\":\"testBDR\","
                        + "\"clusterName\":\"testCluster\",\"nameSpace\":\"testNS\",\"accessEndpoints\":[\"http://endpoint1:1234/\"],\"fileFormatIds\":[1,2,3],\"fileRepoType\":\"S3\"},"
                        + "\"reportOutputPeriodList\":[0],\"notificationTopic\":{\"id\":2,\"name\":\"name\",\"specificationReference\":\"specRef\",\"encoding\":\"JSON\","
                        + "\"dataProviderType\":{\"id\":6,\"dataSpace\":{\"id\":2,\"name\":\"5G\",\"dataProviderTypeIds\":[6]},\"dataCategoryType\":{\"id\":1,"
                        + "\"dataCategoryName\":\"PM_COUNTERs\"},\"notificationTopicIds\":[2],\"messageDataTopicIds\":[],\"providerVersion\":\"V2\",\"providerTypeId\":\"v2\"},"
                        + "\"fileFormatIds\":[2,3],\"messageBus\":{\"id\":1,\"name\":\"name\",\"clusterName\":\"clusterName\",\"nameSpace\":\"nameSpace\","
                        + "\"accessEndpoints\":[\"http://endpoint1:1234/\"],\"notificationTopicIds\":[1,2],\"messageStatusTopicIds\":[1,2],\"messageDataTopicIds\":[1,2]}},"
                        + "\"specificationReference\":\"specRef\",\"dataEncoding\":\"JSON\"}},{\"id\":3,\"mediumType\":\"stream\",\"mediumId\":3,\"schemaName\":\"SCH2\","
                        + "\"schemaVersion\":\"2\",\"isExternal\":true,\"consumedDataSpace\":\"4G\",\"consumedDataCategory\":\"4G\",\"consumedDataProvider\":\"4G\","
                        + "\"consumedSchemaName\":\"4G\",\"consumedSchemaVersion\":\"2\",\"messageSchema\":{\"id\":3,\"messageDataTopic\":{\"id\":2,\"name\":\"topic102\","
                        + "\"encoding\":\"JSON\",\"messageSchemaIds\":[3,4],\"messageBus\":{\"id\":1,\"name\":\"name\",\"clusterName\":\"clusterName\",\"nameSpace\":\"nameSpace\","
                        + "\"accessEndpoints\":[\"http://endpoint1:1234/\"],\"notificationTopicIds\":[1,2],\"messageStatusTopicIds\":[1,2],\"messageDataTopicIds\":[1,2]},"
                        + "\"dataProviderType\":{\"id\":7,\"dataSpace\":{\"id\":3,\"name\":\"4G\",\"dataProviderTypeIds\":[7]},\"dataCategoryType\":{\"id\":2,"
                        + "\"dataCategoryName\":\"CM_EXPORTS1\"},\"notificationTopicIds\":[],\"messageDataTopicIds\":[2],\"providerVersion\":\"Vv101\",\"providerTypeId\":\"vv101\"},"
                        + "\"messageStatusTopic\":{\"id\":2,\"name\":\"topic102\",\"specificationReference\":\"SpecRef101\",\"encoding\":\"JSON\",\"messageDataTopicIds\":[2],"
                        + "\"messageBus\":{\"id\":1,\"name\":\"name\",\"clusterName\":\"clusterName\",\"nameSpace\":\"nameSpace\",\"accessEndpoints\":[\"http://endpoint1:1234/\"],"
                        + "\"notificationTopicIds\":[1,2],\"messageStatusTopicIds\":[1,2],\"messageDataTopicIds\":[1,2]}}},\"dataService\":{\"id\":3,"
                        + "\"dataServiceInstance\":[{\"id\":1,\"dataService\":{\"id\":1,\"dataServiceName\":\"dsName\"},\"dataServiceInstanceName\":\"dsinstance\","
                        + "\"controlEndPoint\":\"http://localhost:8080\",\"consumedDataSpace\":\"5G\",\"consumedDataCategory\":\"5G\",\"consumedDataProvider\":\"5G\","
                        + "\"consumedSchemaVersion\":\"1\",\"consumedSchemaName\":\"SCHm\"},{\"id\":4,\"dataService\":{\"id\":2,\"dataServiceName\":\"ds\"},"
                        + "\"dataServiceInstanceName\":\"dsinst101\",\"controlEndPoint\":\"http://localhost:8082\",\"consumedDataSpace\":\"4G\","
                        + "\"consumedDataCategory\":\"4G\",\"consumedDataProvider\":\"4G\",\"consumedSchemaVersion\":\"2\",\"consumedSchemaName\":\"SCH2\"},{\"id\":2,"
                        + "\"dataService\":{\"id\":2,\"dataServiceName\":\"ds\"},\"dataServiceInstanceName\":\"dsinstance\",\"controlEndPoint\":\"http://localhost:8080\","
                        + "\"consumedDataSpace\":\"5G\",\"consumedDataCategory\":\"5G\",\"consumedDataProvider\":\"5G\",\"consumedSchemaVersion\":\"1\","
                        + "\"consumedSchemaName\":\"SCHm\"}],\"predicateParameter\":[{\"id\":4,\"parameterName\":\"pd101\",\"isPassedToConsumedService\":true,"
                        + "\"dataService\":{\"id\":2,\"dataServiceName\":\"ds\"}},{\"id\":1,\"parameterName\":\"pname\",\"isPassedToConsumedService\":false,"
                        + "\"dataService\":{\"id\":1,\"dataServiceName\":\"dsName\"}},{\"id\":2,\"parameterName\":\"pname\",\"isPassedToConsumedService\":false,"
                        + "\"dataService\":{\"id\":2,\"dataServiceName\":\"ds\"}}],\"dataServiceName\":\"dataservicename102\"},\"dataType\":{\"id\":3,\"mediumType\":\"stream\","
                        + "\"mediumId\":3,\"schemaName\":\"SCH2\",\"schemaVersion\":\"2\",\"isExternal\":true,\"consumedDataSpace\":\"4G\",\"consumedDataCategory\":\"4G\","
                        + "\"consumedDataProvider\":\"4G\",\"consumedSchemaName\":\"4G\",\"consumedSchemaVersion\":\"2\"},\"specificationReference\":\"SpecRef101\"}},{\"id\":4,"
                        + "\"mediumType\":\"stream\",\"mediumId\":4,\"schemaName\":\"SCH2\",\"schemaVersion\":\"2\",\"isExternal\":true,\"consumedDataSpace\":\"4G\","
                        + "\"consumedDataCategory\":\"4G\",\"consumedDataProvider\":\"4G\",\"consumedSchemaName\":\"4G\",\"consumedSchemaVersion\":\"2\",\"messageSchema\":{\"id\":4,"
                        + "\"messageDataTopic\":{\"id\":2,\"name\":\"topic102\",\"encoding\":\"JSON\",\"messageSchemaIds\":[3,4],\"messageBus\":{\"id\":1,\"name\":\"name\","
                        + "\"clusterName\":\"clusterName\",\"nameSpace\":\"nameSpace\",\"accessEndpoints\":[\"http://endpoint1:1234/\"],\"notificationTopicIds\":[1,2],"
                        + "\"messageStatusTopicIds\":[1,2],\"messageDataTopicIds\":[1,2]},\"dataProviderType\":{\"id\":7,\"dataSpace\":{\"id\":3,\"name\":\"4G\","
                        + "\"dataProviderTypeIds\":[7]},\"dataCategoryType\":{\"id\":2,\"dataCategoryName\":\"CM_EXPORTS1\"},\"notificationTopicIds\":[],\"messageDataTopicIds\":[2],"
                        + "\"providerVersion\":\"Vv101\",\"providerTypeId\":\"vv101\"},\"messageStatusTopic\":{\"id\":2,\"name\":\"topic102\",\"specificationReference\":\"SpecRef101\","
                        + "\"encoding\":\"JSON\",\"messageDataTopicIds\":[2],\"messageBus\":{\"id\":1,\"name\":\"name\",\"clusterName\":\"clusterName\",\"nameSpace\":\"nameSpace\","
                        + "\"accessEndpoints\":[\"http://endpoint1:1234/\"],\"notificationTopicIds\":[1,2],\"messageStatusTopicIds\":[1,2],\"messageDataTopicIds\":[1,2]}}},"
                        + "\"dataService\":{\"id\":2,\"dataServiceInstance\":[{\"id\":1,\"dataService\":{\"id\":1,\"dataServiceName\":\"dsName\"},"
                        + "\"dataServiceInstanceName\":\"dsinstance\",\"controlEndPoint\":\"http://localhost:8080\",\"consumedDataSpace\":\"5G\",\"consumedDataCategory\":\"5G\","
                        + "\"consumedDataProvider\":\"5G\",\"consumedSchemaVersion\":\"1\",\"consumedSchemaName\":\"SCHm\"},{\"id\":4,\"dataService\":{\"id\":2,"
                        + "\"dataServiceName\":\"ds\"},\"dataServiceInstanceName\":\"dsinst101\",\"controlEndPoint\":\"http://localhost:8082\",\"consumedDataSpace\":\"4G\","
                        + "\"consumedDataCategory\":\"4G\",\"consumedDataProvider\":\"4G\",\"consumedSchemaVersion\":\"2\",\"consumedSchemaName\":\"SCH2\"},{\"id\":2,"
                        + "\"dataService\":{\"id\":2,\"dataServiceName\":\"ds\"},\"dataServiceInstanceName\":\"dsinstance\",\"controlEndPoint\":\"http://localhost:8080\","
                        + "\"consumedDataSpace\":\"5G\",\"consumedDataCategory\":\"5G\",\"consumedDataProvider\":\"5G\",\"consumedSchemaVersion\":\"1\","
                        + "\"consumedSchemaName\":\"SCHm\"}],\"predicateParameter\":[{\"id\":4,\"parameterName\":\"pd101\",\"isPassedToConsumedService\":true,"
                        + "\"dataService\":{\"id\":2,\"dataServiceName\":\"ds\"}},{\"id\":1,\"parameterName\":\"pname\",\"isPassedToConsumedService\":false,\"dataService\":{\"id\":1,"
                        + "\"dataServiceName\":\"dsName\"}},{\"id\":2,\"parameterName\":\"pname\",\"isPassedToConsumedService\":false,\"dataService\":{\"id\":2,"
                        + "\"dataServiceName\":\"ds\"}}],\"dataServiceName\":\"ds\"},\"dataType\":{\"id\":4,\"mediumType\":\"stream\",\"mediumId\":4,\"schemaName\":\"SCH2\","
                        + "\"schemaVersion\":\"2\",\"isExternal\":true,\"consumedDataSpace\":\"4G\",\"consumedDataCategory\":\"4G\",\"consumedDataProvider\":\"4G\","
                        + "\"consumedSchemaName\":\"4G\",\"consumedSchemaVersion\":\"2\"},\"specificationReference\":\"SpecRef101\"}}]";

        final String endpointUrl = UriComponentsBuilder
                .fromUriString(DATA_CATALOG_URL)
                .queryParam("dataSpace", "5G")
                .queryParam("dataCategory", "PM_COUNTERs")
                .queryParam("dataProvider", "v2")
                .queryParam("schemaName", "SCHMNM")
                .queryParam("schemaVersion", "2")
                .queryParam("serviceName", "dsName")
                .queryParam("isExternal", "true")
                .toUriString();

        final ResponseEntity<String> result = new TestRestTemplate().exchange(
                RequestEntity.get(URI.create(endpointUrl)).build(),
                String.class);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertEquals(expectedResponse, result.getBody());
    }

    @Test
    public void get_DataTypeById_With_Invalid_Id_400_Bad_Request() {
        final ResponseEntity<String> result = new TestRestTemplate().exchange(RequestEntity.get(URI.create(DATA_CATALOG_URL + "/abcd")).build(),
                String.class);

        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
    }

    @Test
    public void get_DataType_with_Non_Existing_Id_404_NotFound() {
        final ResponseEntity<String> result = new TestRestTemplate().exchange(RequestEntity.get(URI.create(DATA_CATALOG_URL + "/100")).build(),
                String.class);

        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
    }

}