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

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.ericsson.oss.air.csac.repository.ResetDAO;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

/**
 * In-memory implementation of the {@link ResetDAO} API.  This class will clear all or part of the in-memory persistent store.
 */
@Repository
@NoArgsConstructor
@Profile({ "dry-run" })
public class ResetDAOImpl implements ResetDAO {

    private final Map<SchemaType, List<Clearable>> clearableMap = new EnumMap<>(SchemaType.class);

    /**
     * Constructs an in-memory {@code ResetDAOImpl} referencing the provided in-memory dictionary and runtime DAO implementations.
     *
     * @param pmSchemaDao  in-memory dictionary PM Schema definition DAO
     * @param pmDao        in-memory dictionary PM definition DAO
     * @param kpiDao       in-memory dictionary KPI definition DAO
     * @param augDao       in-memory dictionary augmentation definition DAO
     * @param profileDao   in-memory dictionary profile definition DAO
     * @param rtIdxDao     in-memory runtime index definition DAO
     * @param rtAugDao     in-memory runtime augmentation definition DAO
     * @param rtKpiDao     in-memory runtime KPI definition DAO
     * @param rtProfileDao in-memory runtime profile definition DAO
     */
    @Autowired
    public ResetDAOImpl(final PMSchemaDefinitionDaoImpl pmSchemaDao,
                        final PMDefinitionDAOImpl pmDao,
                        final KPIDefinitionDAOImpl kpiDao,
                        final AugmentationDefinitionDAOImpl augDao,
                        final ProfileDefinitionDAOImpl profileDao,
                        final DeployedIndexDefinitionDaoImpl rtIdxDao,
                        final EffectiveAugmentationDAOImpl rtAugDao,
                        final DeployedKpiDefDAOImp rtKpiDao,
                        final DeployedProfileDAOImpl rtProfileDao) {

        this.clearableMap.put(SchemaType.DICTIONARY, List.of(pmSchemaDao, augDao, kpiDao, pmDao, profileDao));
        this.clearableMap.put(SchemaType.RUNTIME, List.of(rtIdxDao, rtAugDao, rtKpiDao, rtProfileDao));
    }

    @Override
    public void clear(final SchemaType schemaType) {

        Objects.requireNonNull(schemaType);

        final List<Clearable> clearableList = this.clearableMap.get(schemaType);

        for (final Clearable clearable : clearableList) {
            clearable.clear();
        }
    }
}
