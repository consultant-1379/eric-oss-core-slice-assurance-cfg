/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.handler.request;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import com.ericsson.oss.air.api.model.RtContextFieldMetadataDto;
import com.ericsson.oss.air.api.model.RtContextMetadataDto;
import com.ericsson.oss.air.api.model.RtKpiMetadataDto;
import com.ericsson.oss.air.csac.model.KPIDefinition;
import com.ericsson.oss.air.csac.model.pmsc.KPIDefinitionDTOMapping;
import com.ericsson.oss.air.csac.model.runtime.RuntimeKpiInstance;
import com.ericsson.oss.air.csac.model.runtime.metadata.KpiContextId;
import com.ericsson.oss.air.csac.repository.DeployedKpiDefDAO;
import com.ericsson.oss.air.csac.repository.KPIDefinitionDAO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Handler class for runtime Metadata REST requests.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RuntimeMetadataRequestHandler {

    private final DeployedKpiDefDAO deployedKpiDefDAO;

    private final KPIDefinitionDAO kpiDefinitionDAO;

    /**
     * Gets a list of runtime KPI metadata
     *
     * @param kpiContextId the kpi context id
     * @return the context kpi metadata
     */
    public List<RtKpiMetadataDto> getContextKpiMetadata(final KpiContextId kpiContextId) {
        final List<RuntimeKpiInstance> allByContextId = this.deployedKpiDefDAO.findAllByContextId(kpiContextId, true);
        return allByContextId.stream()
                .map(runtimeKpiInstance -> {

                    final String type = KPIDefinitionDTOMapping.OBJECT_TYPE;
                    final KPIDefinition kpiDef = this.kpiDefinitionDAO.findByKPIDefName(runtimeKpiInstance.getKpDefinitionName());

                    return new RtKpiMetadataDto(kpiDef.getName(), type)
                            .displayName(kpiDef.getDisplayName())
                            .description(kpiDef.getDescription());

                })
                .sorted(Comparator.comparing(RtKpiMetadataDto::getName))
                .toList();
    }

    /**
     * Retrieves a list of runtime context metadata
     *
     * @return a list of {@link RtContextMetadataDto}
     */
    public List<RtContextMetadataDto> getContextMetadata() {
        final List<RuntimeKpiInstance> rtKpiInstanceList = this.deployedKpiDefDAO.findAllRuntimeKpis(true);

        final List<RtContextMetadataDto> rtContextMetadataDtoList = new ArrayList<>();
        rtKpiInstanceList.forEach(rtKpi -> {
            final List<String> contextList = rtKpi.getContextFieldList();
            final String contextId = KpiContextId.of(Set.copyOf(contextList)).getContextId();

            final List<String> calculatedContextIds = rtContextMetadataDtoList.stream().map(RtContextMetadataDto::getId).toList();
            if (!calculatedContextIds.contains(contextId)) {
                final List<RtContextFieldMetadataDto> rtContextFieldMetadataDtoList = contextList.stream()
                        .map(RtContextFieldMetadataDto::new)
                        .sorted(Comparator.comparing(RtContextFieldMetadataDto::getName))
                        .toList();
                rtContextMetadataDtoList.add(new RtContextMetadataDto(contextId, rtContextFieldMetadataDtoList));
            }
        });

        rtContextMetadataDtoList.sort(Comparator.comparing(RtContextMetadataDto::getId));
        return rtContextMetadataDtoList;

    }
}
