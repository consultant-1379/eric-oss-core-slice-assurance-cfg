/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.repository.cache;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.ericsson.oss.air.csac.model.pmsc.KpiDefinitionDTO;
import com.ericsson.oss.air.csac.model.runtime.RuntimeKpiKey;
import com.ericsson.oss.air.csac.repository.DeployedKpiDefDAO;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * Repository bean that will keep a local copy of all newly instantiated runtime KPI instances
 */
@Repository
@NoArgsConstructor
public class ResolvedKpiCache {

    @Autowired
    private DeployedKpiDefDAO deployedKpiDefDAO;

    private final Map<RuntimeKpiKey, KpiDefinitionDTO> kpiDefCache = new HashMap<>();

    /**
     * Returns the target runtime KPI instance from the cache, or an empty
     * Optional if it does not yet exist.
     *
     * @param key a multi-value key required to uniquely identify the KPI
     *            instance, e.g. KPI definition name + aggregation fields +
     *            aggregation period.
     * @return optional containing the resolved KPI instance or an empty
     * optional if the target KPI instance does not exist.
     */
    public Optional<KpiDefinitionDTO> get(final RuntimeKpiKey key) {
        return Optional.ofNullable(this.kpiDefCache.get(key));
    }

    /**
     * Adds the KPI instance to the cache.  This should only be used for
     * new or updated runtime KPI instances.
     *
     * @param key           a multi-value key required to uniquely identify the KPI
     *                      instance, e.g. KPI definition name + aggregation fields +
     *                      aggregation period.
     * @param rtKpiInstance new or updated runtime KPI instance to persist.
     */
    public void put(final RuntimeKpiKey key, final KpiDefinitionDTO rtKpiInstance) {
        this.kpiDefCache.put(key, rtKpiInstance);
    }

    /**
     * Flushes all new or updated runtime KPI instances to the runtime data store.
     */
    public void flush() {
        kpiDefCache.forEach(
                (runtimeKpiKey, kpiDefinitionDTO) -> this.deployedKpiDefDAO.createDeployedKpi(kpiDefinitionDTO, runtimeKpiKey.getKpDefinitionName(),
                        runtimeKpiKey.getAggregationFields()));
    }

    /**
     * Deletes all KPI instances in the cache
     */
    public void deleteAll() {
        this.kpiDefCache.clear();
    }

    /**
     * Checks if cache is empty. Used in unit tests.
     *
     * @return true if cache is empty else false.
     */
    protected boolean isEmpty() {
        return this.kpiDefCache.isEmpty();
    }
}
