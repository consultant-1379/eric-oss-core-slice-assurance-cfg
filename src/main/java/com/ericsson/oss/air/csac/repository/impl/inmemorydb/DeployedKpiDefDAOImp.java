/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.repository.impl.inmemorydb;

import static com.ericsson.oss.air.util.RestEndpointUtil.getSafeSublistIndex;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import com.ericsson.oss.air.csac.model.KPIReference;
import com.ericsson.oss.air.csac.model.ProfileDefinition;
import com.ericsson.oss.air.csac.model.pmsc.KpiDefinitionDTO;
import com.ericsson.oss.air.csac.model.runtime.RuntimeKpiInstance;
import com.ericsson.oss.air.csac.model.runtime.metadata.KpiContextId;
import com.ericsson.oss.air.csac.repository.DeployedKpiDefDAO;
import lombok.NoArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Repository;

@Repository
@NoArgsConstructor
@Profile("dry-run")
public class DeployedKpiDefDAOImp implements DeployedKpiDefDAO, Clearable {

    private final Map<String, KpiDefinitionDTO> deployedKpiDefMap = new HashMap<>();

    private final Map<String, String> originalNameMap = new HashMap<>();

    private final Map<String, List<String>> aggFieldsMap = new HashMap<>();

    @Override
    public void createDeployedKpi(final KpiDefinitionDTO kpiDefinitionDTO, final String kpiDefName, final ProfileDefinition profileDefinition) {
        final List<String> aggregationFields = profileDefinition.getContext();
        this.createDeployedKpi(kpiDefinitionDTO, kpiDefName, aggregationFields);
    }

    @Override
    public void createDeployedKpi(final KpiDefinitionDTO kpiDefinitionDTO, final String kpiDefName, final List<String> aggregationFields) {
        this.deployedKpiDefMap.put(kpiDefinitionDTO.getName(), kpiDefinitionDTO);
        this.originalNameMap.put(kpiDefinitionDTO.getName(), kpiDefName);
        this.aggFieldsMap.put(kpiDefinitionDTO.getName(), aggregationFields);
    }

    @Override
    public void updateDeployedKpi(final KpiDefinitionDTO kpiDefinitionDTO) {
        this.deployedKpiDefMap.put(kpiDefinitionDTO.getName(), kpiDefinitionDTO);
    }

    @Override
    public void deleteDeployedKpi(final String pmscKpiName) {
        this.deployedKpiDefMap.remove(pmscKpiName);
        this.originalNameMap.remove(pmscKpiName);
        this.aggFieldsMap.remove(pmscKpiName);
    }

    @Override
    public KpiDefinitionDTO getDeployedKpi(final String pmscKpiName) {
        return this.deployedKpiDefMap.get(pmscKpiName);
    }

    @Override
    public List<KpiDefinitionDTO> getDeployedKpiByProfile(final ProfileDefinition profileDefinition) {
        return profileDefinition.getKpis()
                .stream()
                .map(KPIReference::getRef)
                .map(kpiName -> this.getDeployedKpiByAggregation(kpiName, profileDefinition.getContext()))
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    public List<KpiDefinitionDTO> getDeployedKpiByDefinitionName(final String kpiDefinitionName) {
        return this.originalNameMap.entrySet()
                .stream()
                .filter(nameMapEntry -> nameMapEntry.getValue().equals(kpiDefinitionName))
                .map(Map.Entry::getKey)
                .map(this::getDeployedKpi)
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    public KpiDefinitionDTO getDeployedKpiByAggregation(final String kpiDefinitionName, final List<String> aggregationFields) {

        final Optional<String> matchedKpiDefName = this.aggFieldsMap.entrySet()
                .stream()
                .filter(entry -> entry.getValue().equals(aggregationFields))
                .map(Map.Entry::getKey)
                .filter(pmscKpiName -> this.originalNameMap.get(pmscKpiName).equals(kpiDefinitionName))
                .filter(Objects::nonNull)
                .findFirst();

        if (matchedKpiDefName.isEmpty()) {
            return null;
        }
        return this.getDeployedKpi(matchedKpiDefName.get());
    }

    @Override
    public List<RuntimeKpiInstance> findAllByContextId(final KpiContextId contextId, final boolean visibleOnly) {
        return this.findAllRuntimeKpis(visibleOnly).stream()
                .filter(runtimeKpiInstance -> KpiContextId.of(new HashSet<>(runtimeKpiInstance.getContextFieldList())).equals(contextId))
                .toList();
    }

    @Override
    public List<KpiDefinitionDTO> getAllDeployedKpis() {
        return new ArrayList<>(this.deployedKpiDefMap.values());
    }

    @Override
    public int totalDeployedKpiDefinitions() {
        return this.deployedKpiDefMap.size();
    }

    @Override
    public List<RuntimeKpiInstance> findAllRuntimeKpis() {

        return this.findAllRuntimeKpis(false);
    }

    @Override
    public List<RuntimeKpiInstance> findAllRuntimeKpis(final Integer start, final Integer rows) {

        if (rows <= 0 || this.originalNameMap.isEmpty()) {
            return Collections.emptyList();
        }

        final List<RuntimeKpiInstance> runtimeKpiDefinitions = new ArrayList<>();

        final Pair<Integer, Integer> safeIndex = getSafeSublistIndex(this.originalNameMap.size(), start, rows);

        // iterate over list of rt kpi names
        for (final Map.Entry<String, String> nameMapEntry : new ArrayList<>(this.originalNameMap.entrySet()).subList(safeIndex.getFirst(),
                safeIndex.getSecond())) {

            final RuntimeKpiInstance rki = new RuntimeKpiInstance();

            rki.setInstanceId(nameMapEntry.getKey());
            rki.setKpDefinitionName(nameMapEntry.getValue());
            rki.setContextFieldList(this.aggFieldsMap.get(nameMapEntry.getKey()));
            rki.setRuntimeDefinition(this.deployedKpiDefMap.get(nameMapEntry.getKey()));

            runtimeKpiDefinitions.add(rki);
        }

        return Collections.unmodifiableList(runtimeKpiDefinitions);
    }

    @Override
    public List<RuntimeKpiInstance> findAllRuntimeKpis(final boolean visibleOnly) {

        return this.originalNameMap.entrySet()
                .stream()
                .filter(nameMapEntry -> {

                    if (!visibleOnly) {
                        return true;
                    }
                    return this.deployedKpiDefMap.get(nameMapEntry.getKey()).getIsVisible();

                })
                .map(nameMapEntry -> RuntimeKpiInstance.builder()
                        .withInstanceId(nameMapEntry.getKey())
                        .withKpDefinitionName(nameMapEntry.getValue())
                        .withContextFieldList(this.aggFieldsMap.get(nameMapEntry.getKey()))
                        .withRuntimeDefinition(this.deployedKpiDefMap.get(nameMapEntry.getKey()))
                        .build())
                .toList();
    }

    @Override
    public void clear() {
        this.aggFieldsMap.clear();
        this.originalNameMap.clear();
        this.deployedKpiDefMap.clear();
    }
}
