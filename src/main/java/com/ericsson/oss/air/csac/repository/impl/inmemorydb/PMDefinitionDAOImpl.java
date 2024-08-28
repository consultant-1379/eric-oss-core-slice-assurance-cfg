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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.ericsson.oss.air.csac.model.PMDefinition;
import com.ericsson.oss.air.csac.repository.PMDefinitionDAO;
import lombok.NoArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Repository;
import org.springframework.util.ObjectUtils;

/**
 * PMDefinition data dictionary to store for sharable resources.
 * <p>
 * This data dictionary will contain all valid resources regardless of whether they have been instantiated in a downstream system.
 */
@Repository
@NoArgsConstructor
@Profile({ "dry-run" })
public class PMDefinitionDAOImpl implements PMDefinitionDAO, Clearable {

    private final Map<String, PMDefinition> pmDefMapByPMName = new HashMap<>();
    /* SchemaRefMap maps schemaName to a list of pmDefName */
    private final Map<String, Set<String>> schemaRefMap = new HashMap<>();

    @Override
    public void savePMDefinition(final PMDefinition pmDefinition, final String schemaName) {
        this.pmDefMapByPMName.put(pmDefinition.getName(), pmDefinition);
        this.saveToSchemaMap(pmDefinition.getName(), schemaName);
    }

    /**
     * Update schemaNameToPmDefMap with given @{@link PMDefinition} name and schema name
     *
     * @param pmDefName
     * @param schemaName
     *         schema name
     * @{@link PMDefinition} name
     */
    private void saveToSchemaMap(final String pmDefName, final String schemaName) {

        /* Remove from previous schema reference if schemaName is changed */
        final String previousSchemaName = this.findSchemaByPMDefName(pmDefName);
        if (!ObjectUtils.isEmpty(previousSchemaName) && !schemaName.equals(previousSchemaName)) {
            this.schemaRefMap.get(previousSchemaName).remove(pmDefName);
        }

        /* Find pmDefName set object from schemaRefMap, create a new HashSet if it is missing */
        Set<String> pmDefNameSet = this.schemaRefMap.get(schemaName);
        if (ObjectUtils.isEmpty(pmDefNameSet)) {
            pmDefNameSet = new HashSet<>();
            this.schemaRefMap.put(schemaName, pmDefNameSet);
        }

        pmDefNameSet.add(pmDefName);

    }

    @Override
    public String findSchemaByPMDefName(final String pmDefName) {
        final Optional<Map.Entry<String, Set<String>>> first = this.schemaRefMap.entrySet().stream()
                .filter(entry -> entry.getValue().contains(pmDefName)).findFirst();
        return first.map(Map.Entry::getKey).orElse(null);
    }

    /**
     * Find PMDefinition names from a schema name
     *
     * @param schemaName
     *         schema name
     * @return a Set of PMDefinition name
     */
    public Set<String> findPMDefNamesBySchemaName(final String schemaName) {
        return this.schemaRefMap.get(schemaName);
    }

    @Override
    public PMDefinition findByPMDefName(final String pmdefName) {
        return this.pmDefMapByPMName.get(pmdefName);
    }

    /**
     * Find a list of PMDefinition Objects by a schema name
     *
     * @param schemaName
     *         schema name
     * @return list of PMDefinition object
     */
    public List<PMDefinition> findAllBySchemaName(final String schemaName) {
        final Set<String> pmDefNameSet = this.schemaRefMap.get(schemaName);
        return pmDefNameSet.stream().map(this.pmDefMapByPMName::get).collect(Collectors.toList());
    }

    /**
     * Get PMDefinition source
     *
     * @param pmdefName
     *         PMDefinition name
     * @return PMDefinition source
     */
    public String getPMDefinitionSource(final String pmdefName) {
        final PMDefinition pmDef = this.pmDefMapByPMName.get(pmdefName);
        return Objects.nonNull(pmDef) ? pmDef.getSource() : null;
    }

    /**
     * Get PMDefinition description
     *
     * @param pmdefName
     *         PMDefinition name
     * @return PMDefinition description
     */
    public String getPMDefinitionDescription(final String pmdefName) {
        final PMDefinition pmDef = this.pmDefMapByPMName.get(pmdefName);
        return Objects.nonNull(pmDef) ? pmDef.getDescription() : null;
    }

    @Override
    public Set<String> getAllPmDefNames() {
        return pmDefMapByPMName.values().stream().map(PMDefinition::getName).collect(Collectors.toSet());
    }

    @Override
    public void updatePMDefinition(final PMDefinition pmDefinition) {
        final String name = pmDefinition.getName();
        if (!ObjectUtils.isEmpty(this.pmDefMapByPMName.get(name))) {
            this.pmDefMapByPMName.put(name, pmDefinition);
        }

    }

    @Override
    public Integer totalPMDefinitions() {
        return this.pmDefMapByPMName.size();
    }

    /**
     * Get total number of Schema Names associated with PMDefinitions
     *
     * @return size of the internal map pmDefMapBySchemaName
     */
    public int totalSchemaNamesWithPMDefinitions() {
        return this.schemaRefMap.size();
    }

    @Override
    public void insertPMDefinitions(final Map<String, List<PMDefinition>> validatedPMDefinions) {
        validatedPMDefinions.forEach((schemaName, pmDefinitions) ->
                pmDefinitions.forEach(pmDefinition -> this.savePMDefinition(pmDefinition, schemaName))
        );
    }

    @Override
    public List<PMDefinition> findAllPMDefinitions(final Integer start, final Integer rows) {
        // return empty list if rows equals to zero or negative
        // return empty list if the DAO object is empty
        if (rows <= 0 || this.pmDefMapByPMName.size() == 0) {
            return new ArrayList<>();
        }

        final List<PMDefinition> allPMDefs = new ArrayList<>(this.pmDefMapByPMName.values());

        final Pair<Integer, Integer> safeIndex = getSafeSublistIndex(allPMDefs.size(), start, rows);
        return allPMDefs.subList(safeIndex.getFirst(), safeIndex.getSecond());
    }

    /**
     * Clear the internal map pmDefMapByPMName and schemaNameToPmDefMap
     */
    protected void clearInternalMaps() {
        this.pmDefMapByPMName.clear();
        this.schemaRefMap.clear();
    }

    @Override
    public void clear() {
        this.clearInternalMaps();
    }
}
