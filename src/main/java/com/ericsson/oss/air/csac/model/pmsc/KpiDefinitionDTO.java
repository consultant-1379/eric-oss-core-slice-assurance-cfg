/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.model.pmsc;

import static com.ericsson.oss.air.csac.model.pmsc.KPIDefinitionDTOMapping.DOT_CHAR;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.ericsson.oss.air.csac.handler.pmsc.util.KpiId;
import com.ericsson.oss.air.csac.model.runtime.RuntimeKpiDefinition;
import com.ericsson.oss.air.exception.CsacValidationException;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonSetter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.logging.log4j.util.Strings;
import org.springframework.util.ObjectUtils;

/**
 * This DTO class is used for generating resource submission for PMSC in JSON format, and it represents the data structure of legacy PMSC KPI
 * Definition Object
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true,
         setterPrefix = "with")
@JsonPropertyOrder({ "name", "alias", "expression", "object_type", "aggregation_type", "aggregation_period", "aggregationElements", "is_visible",
        "inp_data_category", "inp_data_identifier", "execution_group" })
public class KpiDefinitionDTO implements PmscKpiDefinition, RuntimeKpiDefinition {

    private static final String NAME_PREFIX = "csac";

    @JsonProperty("name")
    private String name;

    private String alias;

    @JsonProperty("expression")
    private String expression;

    @JsonProperty("object_type")
    private String objectType;

    @JsonProperty("aggregation_type")
    private String aggregationType;

    @JsonProperty("aggregation_period")
    private Integer aggregationPeriod;

    @JsonProperty("aggregation_elements")
    private List<String> aggregationElements = new ArrayList<>();

    @JsonProperty("is_visible")
    private Boolean isVisible;

    @JsonProperty("inp_data_category")
    private String inpDataCategory;

    @JsonProperty("inp_data_identifier")
    private String inpDataIdentifier;

    @JsonProperty("execution_group")
    private String executionGroup;

    @JsonIgnore
    private KpiTypeEnum kpiType;

    public KpiDefinitionDTO(final KpiDefinitionDTO kpiDefinitionDTO) {
        this(kpiDefinitionDTO.getName(), kpiDefinitionDTO.getAlias(), kpiDefinitionDTO.getExpression(), kpiDefinitionDTO.getObjectType(),
                kpiDefinitionDTO.getAggregationType(),
                kpiDefinitionDTO.getAggregationPeriod(), kpiDefinitionDTO.getAggregationElements(), kpiDefinitionDTO.getIsVisible(),
                kpiDefinitionDTO.getInpDataCategory(), kpiDefinitionDTO.getInpDataIdentifier(), kpiDefinitionDTO.getExecutionGroup(),
                kpiDefinitionDTO.getKpiType());
    }

    /**
     * A method to compare the content of this DTO with another KpiDefinitionDTO without comparing name and kpiType
     *
     * @param compareKpiDto another KpiDefinitionDTO to compare
     * @return true if given object's content matches this one.
     */
    public boolean contentEquals(final KpiDefinitionDTO compareKpiDto) {
        final KpiDefinitionDTO originalDTOWithoutName = this.toBuilder().withName("").withKpiType(null).build();
        final KpiDefinitionDTO givenDTOWithoutName = compareKpiDto.toBuilder().withName("").withKpiType(null).build();

        return originalDTOWithoutName.equals(givenDTOWithoutName);
    }

    /**
     * Gets fact table name.
     *
     * @return the fact table name
     */

    @JsonIgnore
    @Override
    public String getFactTableName() {
        String postFix = "";
        if (!ObjectUtils.isEmpty(this.aggregationPeriod)) {
            postFix = "_" + this.aggregationPeriod;
        }
        return "kpi_" + this.getAlias() + postFix;
    }

    /**
     * Returns the alias for this KPI definition DTO.  The alias is used in the generation of the KPI output table for this KPI definition. The alias
     * must match the following regular expression to be valid: {@code ^[a-z][a-z0-9_]{0,55}$}.
     * <p>
     * The generated alias is based on the runtime KPI name for Complex KPIs, and the input schema reference for Simple KPIs.
     *
     * @return the alias
     */
    @JsonGetter("alias")
    public String getAlias() {

        if (Strings.isBlank(this.alias)) {
            this.alias = this.kpiType == KpiTypeEnum.SIMPLE
                    ? generateAliasWith(this.getInpDataIdentifier())
                    : generateAliasFromName();
        }

        return this.alias;
    }

    @JsonSetter("alias")
    public void setAlias(final String alias) {
        this.alias = alias;
    }

    String generateAliasFromName() {
        return new KpiId().generateId(List.of(this.getName()));
    }

    String generateAliasWith(final String... additionalElements) {

        final List<String> aliasContent = new ArrayList<>();
        aliasContent.add(NAME_PREFIX);
        aliasContent.add(this.getKpiType().toString());

        if (!ObjectUtils.isEmpty(additionalElements)) {
            Collections.addAll(aliasContent, additionalElements);
        }

        final List<String> rawFields = this.getAggregationElements().stream().map(s -> {
            final String[] split = s.split("\\.");
            if (split.length != 2) {
                throw new CsacValidationException("Invalid Aggregation Element: " + s);
            }
            return split[1];
        }).collect(Collectors.toList());

        aliasContent.addAll(rawFields);

        final String generatedAlias = new KpiId().generateAlias(aliasContent);

        // check the alias before returning. This is intended to identify further defects in CSAC and cannot be corrected by a config change alone.
        PmscKpiDefinition.checkKpiAlias(generatedAlias);

        return generatedAlias;
    }

    /**
     * get KpiType. If kpiType is not provided, use the field executionGroup to determine the type
     *
     * @return KpiTypeEnum
     */
    @Override
    @JsonIgnore
    public KpiTypeEnum getKpiType() {

        if (!ObjectUtils.isEmpty(this.kpiType)) {
            return this.kpiType;
        }

        return (ObjectUtils.isEmpty(this.executionGroup)) ? KpiTypeEnum.SIMPLE : KpiTypeEnum.COMPLEX;
    }

    /**
     * Returns this KPI's aggregation elements without being qualified with the schema name.
     *
     * @return this KPI's aggregation elements without being qualified with the schema name.
     */
    @JsonIgnore
    public List<String> getUnqualifiedAggregationElements() {

        if (ObjectUtils.isEmpty(this.aggregationElements)) {
            return Collections.emptyList();
        }

        return this.aggregationElements.stream()
                .map(element ->  element.substring(element.lastIndexOf(DOT_CHAR) + 1))
                .collect(Collectors.toList());

    }

}
