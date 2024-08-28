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

import static com.ericsson.oss.air.csac.model.TestResourcesUtils.VALID_PM_SCHEMA_DEFINITION_PM_COUNTER;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import com.ericsson.oss.air.csac.model.AugmentationDefinition;
import com.ericsson.oss.air.csac.model.KPIDefinition;
import com.ericsson.oss.air.csac.model.PMDefinition;
import com.ericsson.oss.air.csac.model.ProfileDefinition;
import com.ericsson.oss.air.csac.model.pmsc.KpiDefinitionDTO;
import com.ericsson.oss.air.csac.model.runtime.index.DeployedIndexDefinitionDto;
import com.ericsson.oss.air.csac.repository.ResetDAO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ResetDAOImplTest {

    private PMSchemaDefinitionDaoImpl pmSchemaDefDao;

    private AugmentationDefinitionDAOImpl augDao;

    private KPIDefinitionDAOImpl kpiDao;

    private PMDefinitionDAOImpl pmDefDao;

    private ProfileDefinitionDAOImpl profileDao;

    private DeployedIndexDefinitionDaoImpl rtIdxDao;

    private DeployedKpiDefDAOImp rtKpiDao;

    private DeployedProfileDAOImpl rtProfileDao;

    private EffectiveAugmentationDAOImpl rtAugDao;

    private ResetDAOImpl resetDao;

    @BeforeEach
    void setUp() {

        this.pmSchemaDefDao = new PMSchemaDefinitionDaoImpl();
        this.augDao = new AugmentationDefinitionDAOImpl();
        this.kpiDao = new KPIDefinitionDAOImpl();
        this.pmDefDao = new PMDefinitionDAOImpl();
        this.profileDao = new ProfileDefinitionDAOImpl();

        this.augDao.save(new AugmentationDefinition());
        this.kpiDao.saveKPIDefinition(new KPIDefinition());
        this.pmDefDao.savePMDefinition(new PMDefinition(), "schema");

        final ProfileDefinition profileDef = new ProfileDefinition();
        profileDef.setContext(List.of("context"));

        this.profileDao.save(profileDef);

        this.rtAugDao = new EffectiveAugmentationDAOImpl();
        this.rtIdxDao = new DeployedIndexDefinitionDaoImpl();
        this.rtKpiDao = new DeployedKpiDefDAOImp();
        this.rtProfileDao = new DeployedProfileDAOImpl();

        final AugmentationDefinition augDef = new AugmentationDefinition();
        augDef.setUrl("url");
        this.rtAugDao.save(augDef, List.of("profile"));
        this.rtIdxDao.save(new DeployedIndexDefinitionDto());

        this.rtKpiDao.createDeployedKpi(new KpiDefinitionDTO(), "kpi", profileDef);
        this.rtProfileDao.saveProfileDefinition(profileDef);

        this.pmSchemaDefDao.save(VALID_PM_SCHEMA_DEFINITION_PM_COUNTER);

        this.resetDao = new ResetDAOImpl(pmSchemaDefDao, pmDefDao, kpiDao, augDao, profileDao, rtIdxDao, rtAugDao, rtKpiDao, rtProfileDao);
    }

    @Test
    void clear() {

        assertEquals(1, this.pmSchemaDefDao.count());
        assertEquals(1, this.augDao.findAll().size());
        assertEquals(1, this.kpiDao.findAll().size());
        assertEquals(1, this.pmDefDao.totalPMDefinitions());
        assertEquals(1, this.profileDao.findAll().size());

        assertEquals(1, this.rtIdxDao.count());
        assertEquals(1, this.rtAugDao.findAll().size());
        assertEquals(1, this.rtKpiDao.findAllRuntimeKpis().size());
        assertEquals(1, this.rtProfileDao.getProfileDefinitions().size());

        this.resetDao.clear();

        assertEquals(0, this.pmSchemaDefDao.count());
        assertEquals(0, this.augDao.findAll().size());
        assertEquals(0, this.kpiDao.findAll().size());
        assertEquals(0, this.pmDefDao.totalPMDefinitions());
        assertEquals(0, this.profileDao.findAll().size());

        assertEquals(0, this.rtIdxDao.count());
        assertEquals(0, this.rtAugDao.findAll().size());
        assertEquals(0, this.rtKpiDao.findAllRuntimeKpis().size());
        assertEquals(0, this.rtProfileDao.getProfileDefinitions().size());
    }

    @Test
    void clear_schemaType_RUNTIME() {

        assertEquals(1, this.pmSchemaDefDao.count());
        assertEquals(1, this.augDao.findAll().size());
        assertEquals(1, this.kpiDao.findAll().size());
        assertEquals(1, this.pmDefDao.totalPMDefinitions());
        assertEquals(1, this.profileDao.findAll().size());

        assertEquals(1, this.rtIdxDao.count());
        assertEquals(1, this.rtAugDao.findAll().size());
        assertEquals(1, this.rtKpiDao.findAllRuntimeKpis().size());
        assertEquals(1, this.rtProfileDao.getProfileDefinitions().size());

        this.resetDao.clear(ResetDAO.SchemaType.RUNTIME);

        assertEquals(1, this.pmSchemaDefDao.count());
        assertEquals(1, this.augDao.findAll().size());
        assertEquals(1, this.kpiDao.findAll().size());
        assertEquals(1, this.pmDefDao.totalPMDefinitions());
        assertEquals(1, this.profileDao.findAll().size());

        assertEquals(0, this.rtIdxDao.count());
        assertEquals(0, this.rtAugDao.findAll().size());
        assertEquals(0, this.rtKpiDao.findAllRuntimeKpis().size());
        assertEquals(0, this.rtProfileDao.getProfileDefinitions().size());
    }

    @Test
    void clear_schemaType_DICTIONARY() {

        assertEquals(1, this.pmSchemaDefDao.count());
        assertEquals(1, this.augDao.findAll().size());
        assertEquals(1, this.kpiDao.findAll().size());
        assertEquals(1, this.pmDefDao.totalPMDefinitions());
        assertEquals(1, this.profileDao.findAll().size());

        assertEquals(1, this.rtIdxDao.count());
        assertEquals(1, this.rtAugDao.findAll().size());
        assertEquals(1, this.rtKpiDao.findAllRuntimeKpis().size());
        assertEquals(1, this.rtProfileDao.getProfileDefinitions().size());

        this.resetDao.clear(ResetDAO.SchemaType.DICTIONARY);

        assertEquals(0, this.pmSchemaDefDao.count());
        assertEquals(0, this.augDao.findAll().size());
        assertEquals(0, this.kpiDao.findAll().size());
        assertEquals(0, this.pmDefDao.totalPMDefinitions());
        assertEquals(0, this.profileDao.findAll().size());

        assertEquals(1, this.rtIdxDao.count());
        assertEquals(1, this.rtAugDao.findAll().size());
        assertEquals(1, this.rtKpiDao.findAllRuntimeKpis().size());
        assertEquals(1, this.rtProfileDao.getProfileDefinitions().size());
    }
}