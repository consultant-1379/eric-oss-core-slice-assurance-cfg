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
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.ericsson.oss.air.api.model.InputMetricOverrideDto;
import com.ericsson.oss.air.api.model.KpiRefsDto;
import com.ericsson.oss.air.api.model.ProfileDefinitionDto;
import com.ericsson.oss.air.api.model.ProfileDefinitionListDto;
import com.ericsson.oss.air.csac.model.KPIReference;
import com.ericsson.oss.air.csac.model.ProfileDefinition;
import com.ericsson.oss.air.csac.repository.DeployedProfileDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ProfileDefRequestHandler {

    @Autowired
    private DeployedProfileDAO deployedProfileDAO;

    /**
     * Retrieve deployed ProfileDefinitions based on start and rows
     *
     * @param start
     *         Requested start page
     * @param rows
     *         Requested number of rows per page
     * @return @{@link ProfileDefinitionListDto} object with counts, rows, start, total and list of @{@link ProfileDefinition}
     */
    public ProfileDefinitionListDto getProfileDefinitions(final Integer start, final Integer rows) {

        final ProfileDefinitionListDto profDefListDto = new ProfileDefinitionListDto().start(start).rows(rows);

        final List<ProfileDefinition> profileDefs = deployedProfileDAO.findAllProfileDefinitions(getStart(start, rows), getRows(rows));

        final List<ProfileDefinitionDto> profileDefinitionDtoList = profileDefs.stream()
                .map(profile -> {
                    List<KpiRefsDto> kpiRefsDtoList = new ArrayList<>();
                    profile.getKpis().forEach(kpiReference -> {
                        KpiRefsDto kpiRefsDto = new KpiRefsDto();
                        kpiRefsDto.setRef(kpiReference.getRef());
                        kpiRefsDto.setAggregationPeriod(kpiReference.getAggregationPeriod());

                        final List<InputMetricOverrideDto> metricOverrideDtos = mapInputMetricOverrides(kpiReference);
                        if (!metricOverrideDtos.isEmpty()) {
                            kpiRefsDto.setInputMetrics(metricOverrideDtos);
                        }

                        kpiRefsDtoList.add(kpiRefsDto);
                    });
                    return new ProfileDefinitionDto()
                            .name(profile.getName())
                            .description(profile.getDescription())
                            .context(profile.getContext())
                            .kpis(kpiRefsDtoList);

                })
                .collect(Collectors.toList());
        profDefListDto.setProfileDefs(profileDefinitionDtoList);
        profDefListDto.setTotal(deployedProfileDAO.totalProfileDefinitions());
        profDefListDto.setCount(profileDefs.size());

        return profDefListDto;
    }

    private List<InputMetricOverrideDto> mapInputMetricOverrides(final KPIReference kpiReference) {

        if (Objects.isNull(kpiReference.getInputMetricOverrides())) {
            return Collections.emptyList();
        }

        return kpiReference.getInputMetricOverrides().stream()
                .map(metricOverride -> new InputMetricOverrideDto().id(metricOverride.getId())
                        .aggregationPeriod(metricOverride.getAggregationPeriod()))
                .collect(Collectors.toList());
    }
}
