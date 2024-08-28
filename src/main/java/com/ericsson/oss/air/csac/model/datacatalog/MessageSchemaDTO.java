/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.model.datacatalog;

import java.util.regex.Pattern;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

/**
 * A schema to describe the structure for each message. Having a schema allows for independence and understanding
 * between the data producers and data consumers. Data Producers provides data which is compliant to the schema
 * and Data Consumers understands how to read the data. A message Schema consists of a specific Data Provider Type,
 * Data Space, Data Service, Data Category, and Topic, subscription and, Input Data Specification (IDS) for data type as stream.
 */
@Data
@SuperBuilder
@Jacksonized
@JsonPropertyOrder({ "id", "messageDataTopic", "dataService", "dataType", "specificationReference" })
public class MessageSchemaDTO {

    public static final Pattern SPEC_REF_PATTERN = Pattern.compile("^(.+)/\\d+$");

    @JsonProperty(value = "id")
    private String id;

    @JsonProperty(value = "messageDataTopic")
    private MessageDataTopicDto messageDataTopic;

    @JsonProperty(value = "dataService")
    private Object dataService;

    @JsonProperty(value = "dataType")
    private Object dataType;

    @NotBlank
    @JsonProperty(value = "specificationReference")
    private String specificationReference;

}
