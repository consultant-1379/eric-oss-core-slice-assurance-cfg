/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.service.kpi.pmsc;

import java.util.List;
import java.util.stream.Collectors;

import com.ericsson.oss.air.csac.model.pmsc.KpiDefinitionDTO;
import com.ericsson.oss.air.csac.model.pmsc.KpiDefinitionSubmission;
import com.ericsson.oss.air.csac.model.pmsc.KpiSubmissionDto;
import com.ericsson.oss.air.csac.model.pmsc.PmscKpiDefinitionDto;
import com.ericsson.oss.air.csac.repository.DeployedKpiDefDAO;
import com.ericsson.oss.air.csac.repository.impl.inmemorydb.DeployedKpiDefDAOImp;
import com.ericsson.oss.air.util.codec.Codec;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

/**
 * Dry-run implementation of the {@link PmscRestClient}. This implementation logs its activity but will not submit requests to a live PMSC service.
 */
@Service
@Profile({ "dry-run" })
@Slf4j
@RequiredArgsConstructor
public class DryRunPmscRestClient implements PmscRestClient {

    private final DeployedKpiDefDAO deployedKpiDefDAO;
    private final Codec codec = new Codec();

    @SneakyThrows
    @Override
    public ResponseEntity<Void> create(final KpiDefinitionSubmission kpiDefinitionSubmission) {
        log.info("Updating KPI definitions: {} ", this.codec.writeValueAsString(kpiDefinitionSubmission));
        return ResponseEntity.noContent().build();
    }

    @Override
    public void delete(List<KpiDefinitionDTO> kpiDefinitionDtoList) {
        final List<String> kpiNames = kpiDefinitionDtoList.stream().map(KpiDefinitionDTO::getName).collect(Collectors.toList());
        deleteById(kpiNames);
    }

    @Override
    public void deleteById(List<String> ids) {
        log.info("Deleting KPI Definitions: {} ", ids);
    }

    @Override
    public void deleteAll() {
        final List<KpiDefinitionDTO> deployedPmscKpis = deployedKpiDefDAO.getAllDeployedKpis();
        delete(deployedPmscKpis);
    }

    @Override
    public List<PmscKpiDefinitionDto> getAll() {
        log.info("Retrieving all KPIs whose names match the CSAC convention for runtime KPI names");
        return List.of();
    }

}
