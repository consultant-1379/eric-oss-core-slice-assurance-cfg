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


import com.ericsson.oss.air.api.model.PmscRtKpiDetailsDto;
import com.ericsson.oss.air.api.model.RtKpiInstanceDto;
import com.ericsson.oss.air.api.model.RtKpiInstanceListDto;
import com.ericsson.oss.air.csac.model.pmsc.KpiTypeEnum;
import com.ericsson.oss.air.csac.model.runtime.RuntimeKpiInstance;
import com.ericsson.oss.air.csac.repository.DeployedKpiDefDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Handler class for runtime KPI REST requests.
 */
@Component
public class RuntimeKpiRequestHandler {

    @Autowired
    private DeployedKpiDefDAO deployedKpiDefDAO;

    /**
     * Returns a list of runtime KPI instances requested by a REST client.  The returned list includes
     * <ul>
     *     <li>total number of runtime KPI instances</li>
     *     <li>number of runtime KPI instances requested per page</li>
     *     <li>number of runtime KPI instances found</li>
     *     <li>start page for the number of runtime KPI instances</li>
     *     <li>list of runtime KPI instances</li>
     * </ul>
     *
     * @param start
     *         start page for the query
     * @param rows
     *         number of rows requested per page
     * @return a list of runtime KPI instances requested by a REST client
     */
    public RtKpiInstanceListDto getRuntimeKpiDefinitions(final Integer start, final Integer rows) {

        final RtKpiInstanceListDto rtListDto = new RtKpiInstanceListDto().start(start).rows(rows);

        // get the list of runtime instances from the DB
        final List<RuntimeKpiInstance> runtimeInstanceList = this.deployedKpiDefDAO.findAllRuntimeKpis(getStart(start, rows), getRows(rows));

        for (final RuntimeKpiInstance rki : runtimeInstanceList) {
            final RtKpiInstanceDto rtKpiInstanceDto = new RtKpiInstanceDto();
            rtKpiInstanceDto.setKpiName(rki.getKpDefinitionName());
            rtKpiInstanceDto.setKpiContext(rki.getContextFieldList());

            final PmscRtKpiDetailsDto rtKpiDetailsDto = new PmscRtKpiDetailsDto();
            rtKpiDetailsDto.setRtDefinition(rki.getRuntimeDefinition());
            rtKpiDetailsDto.setRtName(rki.getInstanceId());
            rtKpiDetailsDto.setRtTable(rki.getRuntimeDefinition().getFactTableName());

            rtKpiInstanceDto.setKpiType(mapKpiType(rki.getRuntimeDefinition().getKpiType()));

            rtKpiInstanceDto.setDeploymentDetails(rtKpiDetailsDto);

            rtListDto.addKpiDefsItem(rtKpiInstanceDto);
        }

        rtListDto.setCount(rtListDto.getKpiDefs().size());
        rtListDto.setTotal(this.deployedKpiDefDAO.totalDeployedKpiDefinitions());

        return rtListDto;
    }

    private RtKpiInstanceDto.KpiTypeEnum mapKpiType(final KpiTypeEnum kpiTypeEnum) {

        return switch (kpiTypeEnum) {
            case COMPLEX -> RtKpiInstanceDto.KpiTypeEnum.COMPLEX;
            default -> RtKpiInstanceDto.KpiTypeEnum.SIMPLE;
        };

    }

}
