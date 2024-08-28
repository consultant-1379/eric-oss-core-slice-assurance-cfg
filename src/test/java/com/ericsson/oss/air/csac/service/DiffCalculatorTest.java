/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.service;

import static com.ericsson.oss.air.csac.model.TestResourcesUtils.VALID_PM_SCHEMA_DEFINITION_PM_COUNTER;
import static com.ericsson.oss.air.csac.model.TestResourcesUtils.VALID_PM_SCHEMA_DEFINITION_WO_PM_COUNTERS;
import static com.ericsson.oss.air.csac.model.TestResourcesUtils.VALID_PROFILE_DEF_OBJ;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;

import com.ericsson.oss.air.csac.configuration.augmentation.AugmentationConfiguration;
import com.ericsson.oss.air.csac.model.AugmentationDefinition;
import com.ericsson.oss.air.csac.model.KPIDefinition;
import com.ericsson.oss.air.csac.model.PMDefinition;
import com.ericsson.oss.air.csac.model.ProfileDefinition;
import com.ericsson.oss.air.csac.model.ResourceSubmission;
import com.ericsson.oss.air.csac.model.TestResourcesUtils;
import com.ericsson.oss.air.csac.repository.AugmentationDefinitionDAO;
import com.ericsson.oss.air.csac.repository.EffectiveAugmentationDAO;
import com.ericsson.oss.air.csac.repository.KPIDefinitionDAO;
import com.ericsson.oss.air.csac.repository.PMDefinitionDAO;
import com.ericsson.oss.air.csac.repository.PMSchemaDefinitionDao;
import com.ericsson.oss.air.csac.repository.ProfileDefinitionDAO;
import com.ericsson.oss.air.csac.repository.impl.inmemorydb.AugmentationDefinitionDAOImpl;
import com.ericsson.oss.air.csac.repository.impl.inmemorydb.DeployedProfileDAOImpl;
import com.ericsson.oss.air.csac.repository.impl.inmemorydb.EffectiveAugmentationDAOImpl;
import com.ericsson.oss.air.csac.repository.impl.inmemorydb.KPIDefinitionDAOImpl;
import com.ericsson.oss.air.csac.repository.impl.inmemorydb.PMDefinitionDAOImpl;
import com.ericsson.oss.air.csac.repository.impl.inmemorydb.PMSchemaDefinitionDaoImpl;
import com.ericsson.oss.air.csac.repository.impl.inmemorydb.ProfileDefinitionDAOImpl;
import com.ericsson.oss.air.util.codec.Codec;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.validation.Validation;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DiffCalculatorTest {

    private AugmentationDefinition augmentationDef;

    private ProfileDefinition augmentedProfileDef;

    private final Codec codec = new Codec(Validation.buildDefaultValidatorFactory().getValidator());

    private KPIDefinitionDAO kpiDefinitionDAO;

    private PMDefinitionDAO pmDefinitionDAO;

    private DeployedProfileDAOImpl deployedProfileDefinitionDAO;

    private ProfileDefinitionDAO profileDefinitionDAO;

    private AugmentationDefinitionDAO augmentationDefinitionDAO;

    private EffectiveAugmentationDAO effectiveAugmentationDAO;

    private PMSchemaDefinitionDao pmSchemaDefinitionDao;

    @Mock
    private AugmentationConfiguration augmentationConfiguration;

    private DiffCalculator diffCalculator;

    @BeforeEach
    void setUp() throws JsonProcessingException {
        this.kpiDefinitionDAO = new KPIDefinitionDAOImpl();
        this.pmDefinitionDAO = new PMDefinitionDAOImpl();
        this.deployedProfileDefinitionDAO = new DeployedProfileDAOImpl();
        this.profileDefinitionDAO = new ProfileDefinitionDAOImpl();
        this.augmentationDefinitionDAO = new AugmentationDefinitionDAOImpl();
        this.effectiveAugmentationDAO = new EffectiveAugmentationDAOImpl();
        this.pmSchemaDefinitionDao = new PMSchemaDefinitionDaoImpl();

        this.profileDefinitionDAO.save(VALID_PROFILE_DEF_OBJ);
        this.deployedProfileDefinitionDAO.saveProfileDefinition(VALID_PROFILE_DEF_OBJ);
        this.pmDefinitionDAO.savePMDefinition(TestResourcesUtils.VALID_PM_DEF_OBJ, "");
        this.kpiDefinitionDAO.saveKPIDefinition(TestResourcesUtils.VALID_SIMPLE_KPI_DEF_OBJ);

        this.diffCalculator = new DiffCalculator(this.deployedProfileDefinitionDAO, this.profileDefinitionDAO, this.kpiDefinitionDAO,
                this.pmDefinitionDAO, this.augmentationDefinitionDAO, this.effectiveAugmentationDAO, this.augmentationConfiguration,
                this.pmSchemaDefinitionDao);

        //Variables to test augmentation
        this.augmentationDef = codec.readValue(codec.writeValueAsString(TestResourcesUtils.VALID_AUGMENTATION_DEF_OBJ),
                AugmentationDefinition.class);
        this.augmentedProfileDef = codec.readValue(codec.writeValueAsString(TestResourcesUtils.AUGMENTED_PROFILE_DEF_OBJ),
                ProfileDefinition.class);
        this.augmentedProfileDef.setAugmentation(augmentationDef.getName());
    }

    @AfterEach
    void tearDown() {
        this.kpiDefinitionDAO = null;
        this.pmDefinitionDAO = null;
        this.profileDefinitionDAO = null;
        this.deployedProfileDefinitionDAO = null;
        this.augmentationDefinitionDAO = null;
        this.effectiveAugmentationDAO = null;
        this.diffCalculator = null;
        this.augmentationDef = null;
        this.augmentedProfileDef = null;
        this.pmSchemaDefinitionDao = null;
    }

    @Test
    void isChanged_NoChangesWithEmptyResource() {
        final ResourceSubmission resourceSubmission = new ResourceSubmission();
        assertFalse(this.diffCalculator.isChanged(resourceSubmission));
    }

    @Test
    void isChanged_NoChangesWithIdenticalResource() {
        final ResourceSubmission resourceSubmission = new ResourceSubmission();

        resourceSubmission.setProfileDefs(TestResourcesUtils.VALID_LIST_PROFILE_DEF_OBJ);
        resourceSubmission.setKpiDefs(TestResourcesUtils.VALID_LIST_KPI_DEF_OBJ);
        resourceSubmission.setPmDefs(TestResourcesUtils.VALID_LIST_PM_DEF_OBJ);
        assertFalse(this.diffCalculator.isChanged(resourceSubmission));
    }

    // Added to test fix for ESOA-7308
    @Test
    void isChanged_NoChangesWithIdenticalAugmentation_isIdempotent() {

        final ResourceSubmission resourceSubmission = new ResourceSubmission();

        final String unresolvedUrl = "${cardq}";
        final AugmentationDefinition inputAugDef = this.augmentationDef.withUrl(unresolvedUrl);
        final AugmentationDefinition dictAugDef = this.augmentationDef.withUrl(unresolvedUrl);

        resourceSubmission.setAugmentationDefinitions(List.of(inputAugDef));
        resourceSubmission.setProfileDefs(List.of(this.augmentedProfileDef));

        this.augmentationDefinitionDAO.save(dictAugDef);
        this.effectiveAugmentationDAO.save(this.augmentationDef, List.of(this.augmentedProfileDef.getName()));
        this.profileDefinitionDAO.save(this.augmentedProfileDef);
        this.deployedProfileDefinitionDAO.saveProfileDefinition(this.augmentedProfileDef);

        when(this.augmentationConfiguration.getResolvedUrl(unresolvedUrl)).thenReturn(this.augmentationDef.getUrl());

        assertFalse(this.diffCalculator.isChanged(resourceSubmission));

        // Checking for idempotence
        assertFalse(this.diffCalculator.isChanged(resourceSubmission));
    }

    @Test
    void isChanged_ProfileDefChanges() {
        final ResourceSubmission resourceSubmission = new ResourceSubmission();
        assertFalse(this.diffCalculator.isChanged(resourceSubmission));

        final ProfileDefinition updatedProfile = VALID_PROFILE_DEF_OBJ.toBuilder().description("Update Description").build();
        resourceSubmission.setProfileDefs(List.of(updatedProfile));
        assertTrue(this.diffCalculator.isChanged(resourceSubmission));
    }

    @Test
    void isChanged_KPIDefChanges() {
        final ResourceSubmission resourceSubmission = new ResourceSubmission();
        assertFalse(this.diffCalculator.isChanged(resourceSubmission));

        final KPIDefinition updatedKPIDef = TestResourcesUtils.VALID_SIMPLE_KPI_DEF_OBJ.toBuilder().displayName("new display name").build();
        resourceSubmission.setKpiDefs(List.of(updatedKPIDef));
        assertTrue(this.diffCalculator.isChanged(resourceSubmission));
    }

    @Test
    void isChanged_PMDefChanges() {
        final ResourceSubmission resourceSubmission = new ResourceSubmission();
        assertFalse(this.diffCalculator.isChanged(resourceSubmission));

        final PMDefinition updatedPMDef = TestResourcesUtils.VALID_PM_DEF_OBJ.toBuilder().description("UpdateDescription").build();
        resourceSubmission.setPmDefs(List.of(updatedPMDef));
        assertTrue(this.diffCalculator.isChanged(resourceSubmission));
    }

    @Test
    void isChanged_newAugmentationDef() {
        final ResourceSubmission resourceSubmission = new ResourceSubmission();
        assertFalse(this.diffCalculator.isChanged(resourceSubmission));

        resourceSubmission.setAugmentationDefinitions(List.of(augmentationDef));
        resourceSubmission.setProfileDefs(List.of(augmentedProfileDef));

        assertTrue(this.diffCalculator.isChanged(resourceSubmission));
    }

    @Test
    void isChanged_updatedAugmentationDef() {
        final AugmentationDefinition updatedAugDef = augmentationDef.toBuilder().type("updatedType").build();
        final ResourceSubmission resourceSubmission = new ResourceSubmission();

        resourceSubmission.setAugmentationDefinitions(List.of(updatedAugDef));
        resourceSubmission.setProfileDefs(List.of(this.augmentedProfileDef));

        this.augmentationDefinitionDAO.save(this.augmentationDef);
        this.effectiveAugmentationDAO.save(this.augmentationDef, List.of(this.augmentedProfileDef.getName()));

        assertTrue(this.diffCalculator.isChanged(resourceSubmission));
    }

    @Test
    void isChanged_newPMSchemaDef() {
        final ResourceSubmission resourceSubmission = new ResourceSubmission();
        assertFalse(this.diffCalculator.isChanged(resourceSubmission));

        resourceSubmission.setPmSchemaDefs(List.of(VALID_PM_SCHEMA_DEFINITION_PM_COUNTER));

        assertTrue(this.diffCalculator.isChanged(resourceSubmission));
    }

    @Test
    void isChanged_updatedPMSchemaDef() {
        final ResourceSubmission resourceSubmission = new ResourceSubmission();
        resourceSubmission.setPmSchemaDefs(List.of(VALID_PM_SCHEMA_DEFINITION_PM_COUNTER));

        this.pmSchemaDefinitionDao.save(VALID_PM_SCHEMA_DEFINITION_WO_PM_COUNTERS);

        assertTrue(this.diffCalculator.isChanged(resourceSubmission));
    }

    @Test
    void isChanged_samePMSchemaDef() {
        final ResourceSubmission resourceSubmission = new ResourceSubmission();
        resourceSubmission.setPmSchemaDefs(List.of(VALID_PM_SCHEMA_DEFINITION_PM_COUNTER));

        this.pmSchemaDefinitionDao.save(VALID_PM_SCHEMA_DEFINITION_PM_COUNTER);

        assertFalse(this.diffCalculator.isChanged(resourceSubmission));
    }

    @Test
    void isChanged_restartAfterfailedNewAugmentation() {
        final ResourceSubmission resourceSubmission = new ResourceSubmission();

        resourceSubmission.setAugmentationDefinitions(List.of(this.augmentationDef));
        resourceSubmission.setProfileDefs(List.of(this.augmentedProfileDef));

        this.augmentationDefinitionDAO.save(this.augmentationDef);

        assertTrue(this.diffCalculator.isChanged(resourceSubmission));
    }

    @Test
    void isChanged_restartAfterfailedAugmentationUpdate() {
        final AugmentationDefinition updatedAugDef = augmentationDef.toBuilder().type("updatedType").build();
        final ResourceSubmission resourceSubmission = new ResourceSubmission();

        resourceSubmission.setAugmentationDefinitions(List.of(updatedAugDef));
        resourceSubmission.setProfileDefs(List.of(this.augmentedProfileDef));

        this.augmentationDefinitionDAO.save(updatedAugDef);
        this.effectiveAugmentationDAO.save(this.augmentationDef,
                List.of(this.augmentedProfileDef.getName()));

        assertTrue(this.diffCalculator.isChanged(resourceSubmission));
    }

    @Test
    void getAffectedProfiles_NoAffectedProfile() {
        Set<ProfileDefinition> affectedProfiles = null;
        ResourceSubmission rs = null;

        rs = TestResourcesUtils.VALID_RESOURCE_SUBMISSION.toBuilder().build();
        affectedProfiles = this.diffCalculator.getAffectedProfiles(rs);
        assertEquals(0, affectedProfiles.size());

        rs = TestResourcesUtils.VALID_RESOURCE_SUBMISSION.toBuilder().profileDefs(null).pmDefs(null).kpiDefs(null).augmentationDefinitions(null)
                .build();
        affectedProfiles = this.diffCalculator.getAffectedProfiles(rs);
        assertEquals(0, affectedProfiles.size());

    }

    @Test
    void getAffectedProfiles_singleAffectedProfileNewProfile() {
        // When no data in DAOs, current profile is the only affected profile
        final ProfileDefinition updatedProfile = VALID_PROFILE_DEF_OBJ.toBuilder().description("Update Description")
                .name("New profile").build();
        final ResourceSubmission resourceSubmission = new ResourceSubmission();
        resourceSubmission.setProfileDefs(List.of(updatedProfile));

        final Set<ProfileDefinition> affectedProfiles = this.diffCalculator.getAffectedProfiles(resourceSubmission);
        assertEquals(1, affectedProfiles.size());
        assertEquals(updatedProfile, affectedProfiles.toArray()[0]);
    }

    @Test
    void getAffectedProfiles_singleAffectedProfileByChangedKPI() {

        final KPIDefinition validKpiDefObj = TestResourcesUtils.VALID_SIMPLE_KPI_DEF_OBJ.toBuilder().displayName("new displayName").build();
        final ResourceSubmission changedRS = TestResourcesUtils.VALID_RESOURCE_SUBMISSION.toBuilder().build();
        changedRS.setKpiDefs(List.of(validKpiDefObj));

        final Set<ProfileDefinition> affectedProfiles = this.diffCalculator.getAffectedProfiles(changedRS);
        assertEquals(1, affectedProfiles.size());
        assertEquals(VALID_PROFILE_DEF_OBJ, affectedProfiles.toArray()[0]);
    }

    @Test
    void getAffectedProfiles_singleAffectedProfileByChangedPMDef() {
        final PMDefinition newPmDef = TestResourcesUtils.VALID_PM_DEF_OBJ.toBuilder().description("new name").build();
        final ResourceSubmission changedRS = TestResourcesUtils.VALID_RESOURCE_SUBMISSION.toBuilder().build();
        changedRS.setPmDefs(List.of(newPmDef));

        final Set<ProfileDefinition> affectedProfiles = this.diffCalculator.getAffectedProfiles(changedRS);
        assertEquals(1, affectedProfiles.size());
        assertEquals(VALID_PROFILE_DEF_OBJ, affectedProfiles.toArray()[0]);
    }

    @Test
    void getAffectedProfiles_singleAffectedProfileByAugDef() {
        final ResourceSubmission resourceSubmission = new ResourceSubmission();

        resourceSubmission.setAugmentationDefinitions(List.of(this.augmentationDef));
        resourceSubmission.setProfileDefs(List.of(this.augmentedProfileDef));

        final Set<ProfileDefinition> affectedProfiles = this.diffCalculator.getAffectedProfiles(resourceSubmission);
        assertEquals(1, affectedProfiles.size());
        assertEquals(this.augmentedProfileDef, affectedProfiles.toArray()[0]);
    }

}