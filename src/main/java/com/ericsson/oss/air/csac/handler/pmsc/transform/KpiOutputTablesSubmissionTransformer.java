/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.handler.pmsc.transform;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.ericsson.oss.air.csac.model.pmsc.AggregationPeriod;
import com.ericsson.oss.air.csac.model.pmsc.ComplexKpiOutputTableDto;
import com.ericsson.oss.air.csac.model.pmsc.ComplexPmscKpiDefinitionDto;
import com.ericsson.oss.air.csac.model.pmsc.KpiDefinitionDTO;
import com.ericsson.oss.air.csac.model.pmsc.KpiDefinitionSubmission;
import com.ericsson.oss.air.csac.model.pmsc.KpiOutputTableDto;
import com.ericsson.oss.air.csac.model.pmsc.KpiOutputTableListDto;
import com.ericsson.oss.air.csac.model.pmsc.KpiSubmissionDto;
import com.ericsson.oss.air.csac.model.pmsc.KpiTypeEnum;
import com.ericsson.oss.air.csac.model.pmsc.PmscKpiDefinitionDto;
import com.ericsson.oss.air.csac.model.pmsc.SimpleKpiOutputTableDto;
import com.ericsson.oss.air.csac.model.pmsc.SimplePmscKpiDefinitionDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * This class transforms a list of {@link KpiDefinitionDTO}'s into a {@link KpiDefinitionSubmission} that can be used to instantiate KPI's in the PM
 * Stats Calculator.
 */
@Component
@Slf4j
public class KpiOutputTablesSubmissionTransformer implements KpiSubmissionTransformer {

    private static int cDataReliabilityOffset;

    @Value("${provisioning.pmsc.data.reliabilityOffset:0}")
    @SuppressWarnings("java:S2696")
    public void setDataReliabilityOffset(final int offset) {

        KpiOutputTablesSubmissionTransformer.cDataReliabilityOffset = offset;
    }

    /**
     * Maps {@link KpiDefinitionDTO} instances to output {@link SimplePmscKpiDefinitionDto} instances. When constructing the Simple KPI definition
     * DTO, if the source KPI input data identifier is different from the table data identifier then the table's data identifier and aggregation
     * elements will be overridden. Aggregation elements are fields from the schema referenced in the input data identifier. The table's data
     * identifier and aggregation elements come from the first KPI in the table.
     */
    static final BiFunction<String, KpiDefinitionDTO, PmscKpiDefinitionDto> createSimpleKpiDefinitionDtoFn = (tableDataIdentifier, kpiDefinitionDto) -> {

        SimplePmscKpiDefinitionDto.SimplePmscKpiDefinitionDtoBuilder builder = SimplePmscKpiDefinitionDto.builder()
                .name(kpiDefinitionDto.getName())
                .expression(kpiDefinitionDto.getExpression())
                .objectType(kpiDefinitionDto.getObjectType())
                .aggregationType(kpiDefinitionDto.getAggregationType()).exportable(kpiDefinitionDto.getIsVisible());

        /**
         * If the input data identifier does not match the table input data identifier, make sure that it is added to
         * the output KPI definition along with the aggregation elements.
         */
        if (!tableDataIdentifier.equals(kpiDefinitionDto.getInpDataIdentifier())) {
            builder.inputDataIdentifier(kpiDefinitionDto.getInpDataIdentifier()).aggregationElements(kpiDefinitionDto.getAggregationElements());
        }

        return builder.build();
    };

    /**
     * Maps {@link KpiDefinitionDTO} instances to {@link ComplexPmscKpiDefinitionDto} instances.
     */
    static final Function<KpiDefinitionDTO, PmscKpiDefinitionDto> createComplexKpiDefinitionDtoFn = kpiDefinitionDTO -> ComplexPmscKpiDefinitionDto.builder()
            .name(kpiDefinitionDTO.getName())
            .expression(kpiDefinitionDTO.getExpression())
            .objectType(kpiDefinitionDTO.getObjectType())
            .aggregationType(kpiDefinitionDTO.getAggregationType())
            .exportable(kpiDefinitionDTO.getIsVisible())
            .executionGroup(kpiDefinitionDTO.getExecutionGroup())
            .build();

    /**
     * Creates a {@link SimpleKpiOutputTableDto} from a list of simple {@link KpiDefinitionDTO} grouped by aggregation period and alias.
     */
    static final Function<List<KpiDefinitionDTO>, KpiOutputTableDto> createSimpleKpiOutputTableDtoFn = kpiDefinitionDtoList -> {

        final Optional<KpiDefinitionDTO> firstKpiDefDto = kpiDefinitionDtoList.stream().findFirst();

        if (firstKpiDefDto.isEmpty()) {
            return null;
        }

        final KpiDefinitionDTO kpiDefinitionDTO = firstKpiDefDto.get();

        // the first input data identifier will be the table-level input data identifier
        final String tableInputDataIdentifier = kpiDefinitionDTO.getInpDataIdentifier();

        final List<PmscKpiDefinitionDto> pmsckpiDefinitionDtoList = kpiDefinitionDtoList.stream()
                .map(kpi -> createSimpleKpiDefinitionDtoFn.apply(tableInputDataIdentifier, kpi))
                .collect(Collectors.toList());

        return SimpleKpiOutputTableDto.customSimpleKpiOutputTableDtoBuilder()
                .aggregationPeriod(AggregationPeriod.valueOf(kpiDefinitionDTO.getAggregationPeriod()))
                .alias(kpiDefinitionDTO.getAlias())
                .dataReliabilityOffset(cDataReliabilityOffset)
                .aggregationElements(kpiDefinitionDTO.getAggregationElements())
                .kpiDefinitions(pmsckpiDefinitionDtoList)
                .inputDataIdentifier(kpiDefinitionDTO.getInpDataIdentifier())
                .build();
    };

    /**
     * Creates a {@link ComplexKpiOutputTableDto} from a list of complex {@link KpiDefinitionDTO} instances grouped by aggregation period and alias.
     */
    static final Function<List<KpiDefinitionDTO>, KpiOutputTableDto> createComplexKpiOutputTableDtoFn = kpiDefinitionDtoList -> {

        final Optional<KpiDefinitionDTO> firstKpiDefDto = kpiDefinitionDtoList.stream().findFirst();

        if (firstKpiDefDto.isEmpty()) {
            return null;
        }
        final KpiDefinitionDTO kpiDefinitionDTO = firstKpiDefDto.get();

        final List<PmscKpiDefinitionDto> pmscKpiDefinitionDTOs = kpiDefinitionDtoList.stream().map(createComplexKpiDefinitionDtoFn)
                .collect(Collectors.toList());

        return ComplexKpiOutputTableDto.customComplexKpiOutputTableDtoBuilder()
                .aggregationPeriod(AggregationPeriod.valueOf(kpiDefinitionDTO.getAggregationPeriod()))
                .alias(kpiDefinitionDTO.getAlias())
                .dataReliabilityOffset(cDataReliabilityOffset)
                .aggregationElements(kpiDefinitionDTO.getAggregationElements())
                .kpiDefinitions(pmscKpiDefinitionDTOs)
                .build();
    };

    private static KpiOutputTableListDto createScheduledSimple(final List<KpiDefinitionDTO> kpiDefinitionDTOs) {

        log.debug("Creating simple KPI output tables");

        // Group by type-specific table fields function
        final Function<KpiDefinitionDTO, List<Object>> simpleCompositeKeys = simpleKpi -> Arrays.asList(simpleKpi.getAlias(),
                simpleKpi.getAggregationPeriod());

        // Create a list of Kpi output table for simple KPI
        final List<KpiOutputTableDto> simpleKpiOutputTableDtoList = kpiDefinitionDTOs.stream()
                .filter(kpi -> kpi.getKpiType().equals(KpiTypeEnum.SIMPLE))
                .collect(Collectors.groupingBy(simpleCompositeKeys, Collectors.toList())).values().stream().map(createSimpleKpiOutputTableDtoFn)
                .collect(Collectors.toList());

        log.debug("Created {} simple KPI output tables", simpleKpiOutputTableDtoList.size());

        if (simpleKpiOutputTableDtoList.isEmpty()) {
            return null;
        }

        return new KpiOutputTableListDto(simpleKpiOutputTableDtoList);
    }

    private static KpiOutputTableListDto createScheduledComplex(final List<KpiDefinitionDTO> kpiDefinitionDtoList) {

        log.debug("Creating complex KPI output tables");

        // Group by type-specific table fields function
        final Function<KpiDefinitionDTO, List<Object>> complexCompositeKeys = complexKpi -> Arrays.asList(complexKpi.getAlias(),
                complexKpi.getAggregationPeriod());

        // Create a list of Kpi output table for complex KPI
        final List<KpiOutputTableDto> complexKpiOutputTableDtoList = kpiDefinitionDtoList.stream()
                .filter(kpi -> kpi.getKpiType().equals(KpiTypeEnum.COMPLEX))
                .collect(Collectors.groupingBy(complexCompositeKeys, Collectors.toList())).values().stream().map(createComplexKpiOutputTableDtoFn)
                .collect(Collectors.toList());

        log.debug("Created {} complex KPI output tables", complexKpiOutputTableDtoList.size());

        if (complexKpiOutputTableDtoList.isEmpty()) {
            return null;
        }

        return new KpiOutputTableListDto(complexKpiOutputTableDtoList);
    }

    @Override
    public KpiDefinitionSubmission apply(final List<KpiDefinitionDTO> kpiDefinitionDtoList) {

        log.debug("Creating KPI submission");

        final KpiSubmissionDto kpiSubmission = KpiSubmissionDto.builder()
                .scheduledSimple(createScheduledSimple(kpiDefinitionDtoList))
                .scheduledComplex(createScheduledComplex(kpiDefinitionDtoList))
                .build();

        log.debug("KPI submission created");

        return kpiSubmission;
    }

}
