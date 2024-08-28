/*******************************************************************************
 * COPYRIGHT Ericsson 2023
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
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.ericsson.oss.air.csac.model.InputMetric;
import com.ericsson.oss.air.csac.model.KPIDefinition;
import com.ericsson.oss.air.csac.model.PMDefinition;
import com.ericsson.oss.air.csac.repository.KPIDefinitionDAO;
import lombok.NoArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Repository;
import org.springframework.util.ObjectUtils;

/**
 * KPIDefinition data dictionary to store for sharable resources.
 * <p>
 * This data dictionary will contain all valid resources regardless of whether they have been instantiated in a downstream system.
 */
@Repository
@NoArgsConstructor
@Profile({ "dry-run" })
public class KPIDefinitionDAOImpl implements KPIDefinitionDAO, Clearable {

    private final Map<String, KPIDefinition> kpiDefMap = new HashMap<>();

    /**
     * A method returns a set of affected @{@link KPIDefinition} by given @{@link PMDefinition}
     */
    private static Set<KPIDefinition> getAffectedKPIDefsImpl(final PMDefinition pmDef, final Set<KPIDefinition> kpiDefinitionList) {
        final Set<KPIDefinition> affectedKPIDefs = new HashSet<>();

        kpiDefinitionList.forEach(kpiDef -> {
            for (final InputMetric inputMetric : kpiDef.getInputMetrics()) {
                if (inputMetric.getType() == InputMetric.Type.PM_DATA && inputMetric.getId().equals(pmDef.getName())) {
                    affectedKPIDefs.add(kpiDef);
                    break;
                }
            }
        });

        return affectedKPIDefs;
    }

    /**
     * A method returns a set of affected @{@link KPIDefinition} by given a set of  @{@link PMDefinition} within given a list of KPIDefinitions
     *
     * @param pmDefs
     *         a set of pmDefs
     * @param kpiDefinitionList
     *         a list of KPIDefinitions
     * @return a set of @{@link KPIDefinition} that affected by given  @{@link PMDefinition}
     */
    public static Set<KPIDefinition> getAffectedKPIDefs(final Set<PMDefinition> pmDefs, final List<KPIDefinition> kpiDefinitionList) {
        if (ObjectUtils.isEmpty(kpiDefinitionList)) {
            return new HashSet<>();
        }
        return pmDefs.stream().map(pmDef -> getAffectedKPIDefsImpl(pmDef, new HashSet<>(kpiDefinitionList))).flatMap(Collection::stream)
                .collect(Collectors.toSet());
    }

    @Override
    public void saveKPIDefinition(final KPIDefinition kpiDefinition) {
        this.kpiDefMap.put(kpiDefinition.getName(), kpiDefinition);
    }

    @Override
    public KPIDefinition findByKPIDefName(final String kpiDefName) {
        return this.kpiDefMap.get(kpiDefName);
    }

    /**
     * Get KPIDefinition description
     *
     * @param kpiDefName
     *         KPIDefinition name
     * @return KPIDefinition description
     */
    public String getKPIDefDescription(final String kpiDefName) {
        final KPIDefinition kpiDef = this.kpiDefMap.get(kpiDefName);
        return Objects.nonNull(kpiDef) ? kpiDef.getDescription() : null;
    }

    /**
     * Get KPIDefinition display name
     *
     * @param kpiDefName
     *         KPIDefinition name
     * @return KPIDefinition display name
     */
    public String getKPIDefDisplayName(final String kpiDefName) {
        final KPIDefinition kpiDef = this.kpiDefMap.get(kpiDefName);
        return Objects.nonNull(kpiDef) ? kpiDef.getDisplayName() : null;
    }

    /**
     * Get KPIDefinition expression
     *
     * @param kpiDefName
     *         KPIDefinition name
     * @return KPIDefinition expression
     */
    public String getKPIDefExpression(final String kpiDefName) {
        final KPIDefinition kpiDef = this.kpiDefMap.get(kpiDefName);
        return Objects.nonNull(kpiDef) ? kpiDef.getExpression() : null;
    }

    /**
     * Get KPIDefinition aggregation type
     *
     * @param kpiDefName
     *         KPIDefinition name
     * @return KPIDefinition aggregation type
     */
    public String getKPIDefAggregationType(final String kpiDefName) {
        final KPIDefinition kpiDef = this.kpiDefMap.get(kpiDefName);
        return Objects.nonNull(kpiDef) ? kpiDef.getAggregationType() : null;
    }

    /**
     * Get KPIDefinition is visible
     *
     * @param kpiDefName
     *         KPIDefinition name
     * @return KPIDefinition is visible
     */
    public Boolean getKPIDefIsVisible(final String kpiDefName) {
        final KPIDefinition kpiDef = this.kpiDefMap.get(kpiDefName);
        return Objects.nonNull(kpiDef) ? kpiDef.getIsVisible() : null;
    }

    /**
     * Get KPIDefinition input metrics
     *
     * @param kpiDefName
     *         KPIDefinition name
     * @return list of input metric
     */
    public List<InputMetric> getKPIDefInputMetrics(final String kpiDefName) {
        final KPIDefinition kpiDef = this.kpiDefMap.get(kpiDefName);
        return Objects.nonNull(kpiDef) ? kpiDef.getInputMetrics() : null;
    }

    @Override
    public Set<String> getAllKpiDefNames() {
        return this.kpiDefMap.values().stream().map(KPIDefinition::getName).collect(Collectors.toSet());
    }

    /**
     * Update KPIDefinition description
     *
     * @param name
     *         KPIDefinition name
     * @param description
     *         KPIDefinition description
     */
    public void updateKPIDefinitionDescription(final String name, final String description) {
        final KPIDefinition kpiDef = this.kpiDefMap.get(name);

        if (Objects.nonNull(kpiDef)) {
            kpiDef.setDescription(description);
        }
    }

    /**
     * Update KPIDefinition displayName
     *
     * @param name
     *         KPIDefinition name
     * @param displayName
     *         KPIDefinition displayName
     */
    public void updateKPIDefinitionDisplayName(final String name, final String displayName) {
        final KPIDefinition kpiDef = this.kpiDefMap.get(name);

        if (Objects.nonNull(kpiDef)) {
            kpiDef.setDisplayName(displayName);
        }
    }

    /**
     * Update KPIDefinition expression
     *
     * @param name
     *         KPIDefinition name
     * @param expression
     *         KPIDefinition expression
     */
    public void updateKPIDefinitionExpression(final String name, final String expression) {
        final KPIDefinition kpiDef = this.kpiDefMap.get(name);

        if (Objects.nonNull(kpiDef)) {
            kpiDef.setExpression(expression);
        }
    }

    /**
     * Update KPIDefinition aggregation type
     *
     * @param name
     *         KPIDefinition name
     * @param aggrType
     *         KPIDefinition aggregation type
     */
    public void updateKPIDefinitionAggregationType(final String name, final String aggrType) {
        final KPIDefinition kpiDef = this.kpiDefMap.get(name);

        if (Objects.nonNull(kpiDef)) {
            kpiDef.setAggregationType(aggrType);
        }
    }

    /**
     * Update KPIDefinition is visible
     *
     * @param name
     *         KPIDefinition name
     * @param isVisible
     *         KPIDefinition is visible
     */
    public void updateKPIDefinitionIsVisible(final String name, final boolean isVisible) {
        final KPIDefinition kpiDef = this.kpiDefMap.get(name);

        if (Objects.nonNull(kpiDef)) {
            kpiDef.setIsVisible(isVisible);
        }
    }

    /**
     * Update KPIDefinition input metric's alias
     *
     * @param name
     *         KPIDefinition name
     * @param id
     *         InputMetric name
     * @param alias
     *         InputMetric alias
     */
    public void updateKPIDefinitionInputMetricAlias(final String name, final String id, final String alias) {
        final KPIDefinition kpiDef = this.kpiDefMap.get(name);
        final List<InputMetric> inputMetrics = kpiDef.getInputMetrics();

        inputMetrics.forEach(inputMetric -> {
            if (inputMetric.getId().equals(id)) {
                inputMetric.setAlias(alias);
            }
        });
    }

    /**
     * Update KPIDefinition input metric's type
     *
     * @param name
     *         KPIDefinition name
     * @param id
     *         InputMetric name
     * @param type
     *         InputMetric type
     */
    public void updateKPIDefinitionInputMetricType(final String name, final String id, final InputMetric.Type type) {
        final KPIDefinition kpiDef = this.kpiDefMap.get(name);
        final List<InputMetric> inputMetrics = kpiDef.getInputMetrics();

        inputMetrics.stream().filter(inputMetric -> inputMetric.getId().equals(id)).forEach(input -> input.setType(type));
    }

    /**
     * Replace the input metric list in KPIDefinition object
     *
     * @param name
     *         KPIDefinition name
     * @param inputMetrics
     *         list of InputMetric object
     */
    public void replaceKPIDefinitionInputMetrics(final String name, final List<InputMetric> inputMetrics) {
        final KPIDefinition kpiDef = this.kpiDefMap.get(name);
        kpiDef.setInputMetrics(inputMetrics);
        this.kpiDefMap.put(name, kpiDef);
    }

    /**
     * Append the input metric object in existed KPIDefiniton object's input metric
     *
     * @param name
     *         KPIDefinition name
     * @param inputMetric
     *         InputMetric object
     */
    public void appendKPIDefinitionInputMetric(final String name, final InputMetric inputMetric) {
        final KPIDefinition kpiDef = this.kpiDefMap.get(name);
        final List<InputMetric> inputMetricsList = kpiDef.getInputMetrics();
        inputMetricsList.add(inputMetric);

        kpiDef.setInputMetrics(inputMetricsList);
        this.kpiDefMap.put(name, kpiDef);
    }

    @Override
    public Integer totalKPIDefinitions() {
        return this.kpiDefMap.size();
    }

    @Override
    public void insertKPIDefinitions(final List<KPIDefinition> validatedKPIDefinitions) {
        validatedKPIDefinitions.forEach(this::saveKPIDefinition);
    }

    @Override
    public Set<KPIDefinition> getAffectedKPIDefs(final Set<PMDefinition> pmDefs) {
        if (ObjectUtils.isEmpty(pmDefs)) {
            return new HashSet<>();
        }

        return KPIDefinitionDAOImpl.getAffectedKPIDefs(pmDefs, new ArrayList<>(this.kpiDefMap.values()));
    }

    @Override
    public List<KPIDefinition> findAllKPIDefs(final Integer start, final Integer rows) {
        // return empty list if rows equals to zero or negative
        // return empty list if the DAO object is empty
        if (rows <= 0 || this.kpiDefMap.size() == 0) {
            return new ArrayList<>();
        }

        final List<KPIDefinition> allKPIDefs = new ArrayList<>(this.kpiDefMap.values());

        final Pair<Integer, Integer> safeIndex = getSafeSublistIndex(allKPIDefs.size(), start, rows);
        return allKPIDefs.subList(safeIndex.getFirst(), safeIndex.getSecond());
    }

    @Override
    public List<KPIDefinition> findAll() {
        return new ArrayList<>(this.kpiDefMap.values());
    }

    /**
     * Clear the internal map kpiDefMap
     */
    protected void clearInternalMap() {
        this.kpiDefMap.clear();
    }

    @Override
    public void clear() {
        this.kpiDefMap.clear();
    }
}
