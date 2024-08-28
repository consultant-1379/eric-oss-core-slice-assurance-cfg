/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.model.datacatalog;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

/**
 * A topic used for data stream events.
 */
@Data
@SuperBuilder(toBuilder = true)
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder({ "id", "name", "encoding", "messageSchemaIds", "messageBus", "dataProviderType", "messageStatusTopic" })
public class MessageDataTopicDto {

    private Integer id;
    // The Kafka topic name
    private String name;
    private String encoding;
    private List<Integer> messageSchemaIds;
    private MessageBusDto messageBus;
    private DataProviderTypeDto dataProviderType;
    private MessageStatusTopicDto messageStatusTopic;

    @Data
    @SuperBuilder(toBuilder = true)
    @Jacksonized
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonPropertyOrder({ "id", "name", "clusterName", "nameSpace", "accessEndpoints", "notificationTopicIds", "messageStatusTopicIds", "messageDataTopicIds" })
    public static class MessageBusDto {
        private Integer id;
        private String name;
        private String clusterName;
        private String nameSpace;
        private List<String> accessEndpoints;
        private List<Integer> notificationTopicIds;
        private List<Integer> messageStatusTopicIds;
        private List<Integer> messageDataTopicIds;
    }

    @Data
    @SuperBuilder(toBuilder = true)
    @Jacksonized
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonPropertyOrder({ "id", "dataSpace", "dataCategoryType", "notificationTopicIds", "messageDataTopicIds", "providerVersion", "providerTypeId" })
    public static class DataProviderTypeDto {
        private Integer id;
        private DataSpace dataSpace;
        private DataCategoryType dataCategoryType;
        private List<Integer> notificationTopicIds;
        private List<Integer> messageDataTopicIds;
        private String providerVersion;
        private String providerTypeId;
    }

    @Data
    @SuperBuilder(toBuilder = true)
    @Jacksonized
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonPropertyOrder({ "id", "name", "specificationReference", "encoding", "messageDataTopicIds", "messageBus" })
    public static class MessageStatusTopicDto {
        private Integer id;
        private String name;
        private String specificationReference;
        private String encoding;
        private List<Integer> messageDataTopicIds;
        private MessageBusDto messageBus;
    }

    @Data
    @SuperBuilder(toBuilder = true)
    @Jacksonized
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonPropertyOrder({ "id", "name", "dataProviderTypeIds" })
    public static class DataSpace {
        private Integer id;
        private String name;
        private List<Integer> dataProviderTypeIds;
    }

    @Data
    @SuperBuilder(toBuilder = true)
    @Jacksonized
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonPropertyOrder({ "id", "dataCategoryName" })
    public static class DataCategoryType {
        private Integer id;
        private String dataCategoryName;
    }
}