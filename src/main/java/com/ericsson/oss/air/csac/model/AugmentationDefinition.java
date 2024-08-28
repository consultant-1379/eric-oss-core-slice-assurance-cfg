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

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.With;

/**
 * Resource definition bean for augmentation definitions.
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@With
public class AugmentationDefinition implements ResourceDefinition {

    @NotBlank
    @Pattern(regexp = "[a-zA-Z][_a-zA-Z0-9]*")
    @JsonProperty("ardq_id")
    private String name;

    // excluded from equals because the dictionary definition may have a placeholder, e.g. '${cardq}', while the runtime definition will have
    // the full ARDQ URL.
    @EqualsAndHashCode.Exclude
    @NotBlank
    @JsonProperty("ardq_url")
    private String url;

    @JsonProperty("ardq_type")
    private String type;

    @NotEmpty
    @JsonProperty("ardq_rules")
    @Valid
    private List<AugmentationRule> augmentationRules;
}
