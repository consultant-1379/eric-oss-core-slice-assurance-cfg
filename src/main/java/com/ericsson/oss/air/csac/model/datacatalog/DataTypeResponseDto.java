/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.model.datacatalog;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
@JsonDeserialize
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder({ "id", "mediumId", "mediumType", "schemaName", "schemaVersion", "isExternal", "consumedDataSpace", "consumedDataCategory",
        "consumedDataProvider", "consumedSchemaName", "consumedSchemaVersion", "fileFormat", "messageSchema" })
public class DataTypeResponseDto {

    private Integer id;

    private Integer mediumId;

    private String mediumType;

    private String schemaName;

    private String schemaVersion;

    private Boolean isExternal;

    private String consumedDataSpace;

    private String consumedDataCategory;

    private String consumedDataProvider;

    private String consumedSchemaName;

    private String consumedSchemaVersion;

    private Object fileFormat;

    private MessageSchemaDTO messageSchema;
}
