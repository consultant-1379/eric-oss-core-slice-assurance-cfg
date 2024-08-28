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
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

/**
 * Augmentation rule field bean.  This bean is an element of {@link AugmentationDefinition}.
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@Builder
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@AugmentationFieldConstraint(entity = "output",
                             entityList = "outputFields",
                             message = "Missing required field. Either of the fields 'output' or 'output_fields' should exist in augmentation")
public class AugmentationRuleField {

    @Pattern(regexp = "^[A-Za-z_][A-Za-z0-9_]*$")
    @JsonProperty(value = "output")
    private String output;

    @JsonProperty(value = "output_fields")
    private List<String> outputFields;

    @NotEmpty
    @JsonProperty(value = "input",
                  required = true)
    private List<String> inputFields;

    /**
     * Output fields for an augmentation can be provided through 'output' and 'output_fields' fields.
     * This method returns all the output fields defined in an {@link AugmentationRuleField}.
     *
     * @return list of output fields.
     */
    public List<String> getAllOutputFields() {
        final List<String> outputFieldList = new ArrayList<>();

        if (!ObjectUtils.isEmpty(this.output)) {
            outputFieldList.add(this.output);
        }

        if (!CollectionUtils.isEmpty(this.outputFields)) {
            outputFieldList.addAll(this.outputFields);
        }

        return outputFieldList;
    }
}
