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

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.ericsson.oss.air.api.model.PmDefinitionDto;
import com.ericsson.oss.air.api.model.PmDefinitionListDto;
import com.ericsson.oss.air.csac.model.KPIDefinition;
import com.ericsson.oss.air.csac.model.PMDefinition;
import com.ericsson.oss.air.csac.repository.KPIDefinitionDAO;
import com.ericsson.oss.air.csac.repository.PMDefinitionDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PMDefRequestHandler {

    @Autowired
    private PMDefinitionDAO pmDefinitionDAO;

    @Autowired
    private KPIDefinitionDAO kpiDefinitionDAO;

    /**
     * Retrieve PMDefinitions from repository based on start and rows
     *
     * @param start Requested start page
     * @param rows  Requested number of rows per page
     * @return PmDefinitionListDto object with counts, rows, start, total and list of PMDefinitions
     */
    public PmDefinitionListDto getPMDefinitions(final Integer start, final Integer rows) {

        final PmDefinitionListDto pmDefListDto = new PmDefinitionListDto().start(start).rows(rows);
        final List<PMDefinition> pmDefs = pmDefinitionDAO.findAllPMDefinitions(getStart(start, rows), getRows(rows));

        // map list of PMDefinition to list of PmDefinitionDto
        final List<PmDefinitionDto> pmDefinitionDtoList = pmDefs.stream()
                .map(obj -> new PmDefinitionDto()
                        .name(obj.getName())
                        .source(obj.getSource())
                        .description(obj.getDescription())
                        .kpis(this.getKpiNames(obj)))
                .collect(Collectors.toList());

        pmDefListDto.setPmDefs(pmDefinitionDtoList);
        pmDefListDto.setCount(pmDefs.size());
        pmDefListDto.setTotal(pmDefinitionDAO.totalPMDefinitions());

        return pmDefListDto;
    }

    /**
     * Retrieve affected Kpi names from PMDefinition object
     *
     * @param pmDefinition pmDefinition to be referred
     * @return List of String for kpi names
     */
    List<String> getKpiNames(final PMDefinition pmDefinition) {

        return this.kpiDefinitionDAO.getAffectedKPIDefs(Set.of(pmDefinition)).stream().map(KPIDefinition::getName)
                .collect(Collectors.toList());
    }

}