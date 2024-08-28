/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.handler.request;

import static com.ericsson.oss.air.util.RestEndpointUtil.getRows;
import static com.ericsson.oss.air.util.RestEndpointUtil.getStart;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.ericsson.oss.air.api.model.InputMetricDto;
import com.ericsson.oss.air.api.model.KpiDefinitionDto;
import com.ericsson.oss.air.api.model.KpiDefinitionListDto;
import com.ericsson.oss.air.csac.model.InputMetric;
import com.ericsson.oss.air.csac.model.KPIDefinition;
import com.ericsson.oss.air.csac.model.KPIReference;
import com.ericsson.oss.air.csac.model.ProfileDefinition;
import com.ericsson.oss.air.csac.repository.KPIDefinitionDAO;
import com.ericsson.oss.air.csac.repository.ProfileDefinitionDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class KPIDefRequestHandler {

    @Autowired
    private KPIDefinitionDAO kpiDefinitionDAO;

    @Autowired
    private ProfileDefinitionDAO profileDefinitionDAO;

    /**
     * Retrieve KPIDefinitions from repository based on the start and rows parameters
     *
     * @param start the requested start page location
     * @param rows  the requested number of rows per page
     * @return KpiDefinitionListDto object with total, count, start, rows and list of KPI definitions
     */
    public KpiDefinitionListDto getKPIDefinitions(final Integer start, final Integer rows) {

        final List<KPIDefinition> kpiDefinitions = kpiDefinitionDAO.findAllKPIDefs(getStart(start, rows), getRows(rows));

        final List<ProfileDefinition> profileDefinitions = profileDefinitionDAO.findAll();

        final List<KpiDefinitionDto> kpiDefinitionDtoList = kpiDefinitions.stream()
                .map(kpi -> {
                    final List<InputMetricDto> inputMetricDtos = new ArrayList<>();

                    kpi.getInputMetrics().forEach(inputMetric -> {
                        InputMetricDto inputMetricDto = new InputMetricDto(
                                inputMetric.getId(),
                                inputMetric.getType() == InputMetric.Type.KPI ? InputMetricDto.TypeEnum.KPI : InputMetricDto.TypeEnum.PM_DATA);

                        inputMetricDto.setAlias(inputMetric.getAlias());
                        inputMetricDtos.add(inputMetricDto);
                    });

                    final List<String> profileNames = profileDefinitions.stream()
                            .filter(profileDefinition -> profileDefinition.getKpis().stream()
                                    .map(KPIReference::getRef)
                                    .collect(Collectors.toList())
                                    .contains(kpi.getName()))
                            .map(ProfileDefinition::getName)
                            .collect(Collectors.toList());

                    final KpiDefinitionDto kpiDefinitionDto = new KpiDefinitionDto(kpi.getName(), kpi.getExpression(), kpi.getAggregationType(),
                            inputMetricDtos);
                    kpiDefinitionDto.setDescription(kpi.getDescription());
                    kpiDefinitionDto.setDisplayName(kpi.getDisplayName());
                    kpiDefinitionDto.setAggregationPeriod(kpi.getAggregationPeriod());
                    kpiDefinitionDto.setIsVisible(kpi.getIsVisible());
                    kpiDefinitionDto.setProfiles(profileNames);

                    return kpiDefinitionDto;
                })
                .collect(Collectors.toList());

        return new KpiDefinitionListDto(this.kpiDefinitionDAO.totalKPIDefinitions(), kpiDefinitions.size(), start, rows, kpiDefinitionDtoList);
    }

}
