/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.model;

import java.util.ArrayList;
import java.util.List;

import com.ericsson.oss.air.csac.model.validation.AugmentationFieldConstraint;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

/**
 * Augmentation rule bean.  This bean is an element of {@link AugmentationDefinition}.
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@Builder
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@AugmentationFieldConstraint(entity = "inputSchemaReference",
                             entityList = "inputSchemas",
                             message = "Missing required field. Either of the fields 'input_schema' or 'input_schemas' should exist in augmentation")
public class AugmentationRule {

    @JsonProperty(value = "input_schema")
    private String inputSchemaReference;

    @JsonProperty(value = "input_schemas")
    private List<String> inputSchemas;

    @Valid
    @NotEmpty
    @JsonProperty(value = "fields",
                  required = true)
    private List<AugmentationRuleField> fields;

    /**
     * Input schemas in an augmentation rule can be provided through 'input_schema' and 'input_schemas' fields.
     * This method returns all the input schemas defined in an {@link AugmentationRule}.
     *
     * @return list of input schema references.
     */
    public List<String> getAllInputSchemas() {
        final List<String> inputSchemaList = new ArrayList<>();

        if (!ObjectUtils.isEmpty(this.inputSchemaReference)) {
            inputSchemaList.add(this.inputSchemaReference);
        }

        if (!CollectionUtils.isEmpty(this.inputSchemas)) {
            inputSchemaList.addAll(this.inputSchemas);
        }

        return inputSchemaList;
    }
}
