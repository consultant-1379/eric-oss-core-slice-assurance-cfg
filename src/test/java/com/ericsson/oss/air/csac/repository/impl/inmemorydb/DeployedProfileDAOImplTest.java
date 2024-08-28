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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import com.ericsson.oss.air.csac.model.InputMetric;
import com.ericsson.oss.air.csac.model.KPIDefinition;
import com.ericsson.oss.air.csac.model.KPIReference;
import com.ericsson.oss.air.csac.model.ProfileDefinition;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DeployedProfileDAOImplTest {

    private static final String PROFILE_DEF_NAME_1 = "profile_def_name_1";

    private static final String PROFILE_DEF_DESCRIPTION_1 = "profile_def_description_1";

    private static final String PROFILE_DEF_DESCRIPTION_2 = "profile_def_description_2";

    private static final List<String> PROFILE_DEF_AGGREGATED_FIELDS_1 = new ArrayList<>(
            Arrays.asList("profile_def_agg_field_1", "profile_def_agg_field_2"));

    private static final KPIReference KPI_REF_1 = KPIReference.builder().ref("kpi_ref_1").build();

    private static final KPIReference KPI_REF_2 = KPIReference.builder().ref("kpi_ref_2").build();

    private static final List<KPIReference> KPI_REFERENCES_1 = new ArrayList<>(Arrays.asList(KPI_REF_1, KPI_REF_2));

    private static final ProfileDefinition PROFILE_DEF_1 = ProfileDefinition.builder()
            .name(PROFILE_DEF_NAME_1)
            .description(PROFILE_DEF_DESCRIPTION_1)
            .context(PROFILE_DEF_AGGREGATED_FIELDS_1)
            .kpis(KPI_REFERENCES_1)
            .build();

    private static final ProfileDefinition PROFILE_DEF_MATCHING_NAME_ONLY = ProfileDefinition.builder()
            .name(PROFILE_DEF_NAME_1)
            .context(PROFILE_DEF_AGGREGATED_FIELDS_1)
            .kpis(KPI_REFERENCES_1)
            .build();

    private static final String KPI_DEF_NAME_UNAFFECTED = "kpi_def_name";
    private static final String KPI_DEF_NAME_AFFECTED = "kpi_ref_1";
    private static final String KPI_DEF_DESCRIPTION = "kpi_def_desc";
    private static final String KPI_DEF_EXPRESSION = "kpi_def_expr";
    private static final String KPI_DEF_DISPLAY_NAME = "kpi def display name";
    private static final String KPI_DEF_AGGREGATION_TYPE = "kpi def aggr type";
    private static final boolean IS_VISIBLE_ORIG = true;

    private static final List<InputMetric> INPUT_METRICS = new ArrayList<>() {{
        add(new InputMetric("id", "alias", InputMetric.Type.KPI));
    }};

    private static final KPIDefinition KPI_DEF_NOT_AFFECTING = new KPIDefinition(KPI_DEF_NAME_UNAFFECTED, KPI_DEF_DESCRIPTION, KPI_DEF_DISPLAY_NAME,
            KPI_DEF_EXPRESSION, KPI_DEF_AGGREGATION_TYPE, null, IS_VISIBLE_ORIG, INPUT_METRICS);

    private static final KPIDefinition KPI_DEF_AFFECTING = new KPIDefinition(KPI_DEF_NAME_AFFECTED, KPI_DEF_DESCRIPTION, KPI_DEF_DISPLAY_NAME,
            KPI_DEF_EXPRESSION, KPI_DEF_AGGREGATION_TYPE, null, IS_VISIBLE_ORIG, INPUT_METRICS);

    private DeployedProfileDAOImpl profDefDAO;

    @BeforeEach
    void setup() {
        profDefDAO = new DeployedProfileDAOImpl();
    }

    @AfterEach
    void tearDown() {
        profDefDAO.clearInternalMaps();
    }

    @Test
    void getProfileDefinitions_profileExists_returned() {
        profDefDAO.saveProfileDefinition(PROFILE_DEF_1);
        assertEquals(1, profDefDAO.getProfileDefinitions().size());
        assertEquals(profDefDAO.getProfileDefinitions(), Set.of(PROFILE_DEF_1));
    }

    @Test
    void saveProfileDefinition_newProfile_savedToDAO() {
        assertEquals(0, profDefDAO.getProfileDefinitions().size());
        profDefDAO.saveProfileDefinition(PROFILE_DEF_1);
        assertEquals(1, profDefDAO.getProfileDefinitions().size());
        assertTrue(profDefDAO.getProfileDefinitions().contains(PROFILE_DEF_1));
    }

    @Test
    void saveProfileDefinition_existingProfile_notSavedToDAO() {
        profDefDAO.saveProfileDefinition(PROFILE_DEF_1);
        assertEquals(1, profDefDAO.getProfileDefinitions().size());
        profDefDAO.saveProfileDefinition(PROFILE_DEF_1);
        assertEquals(1, profDefDAO.getProfileDefinitions().size());
        assertTrue(profDefDAO.getProfileDefinitions().contains(PROFILE_DEF_1));
    }

    @Test
    void findByProfileDefName_profilePresent_found() {
        profDefDAO.saveProfileDefinition(PROFILE_DEF_1);
        assertEquals(PROFILE_DEF_1, profDefDAO.findByProfileDefName(PROFILE_DEF_NAME_1));
    }

    @Test
    void findByProfileDefName_noMatchingProfile_returnsNull() {
        final String nonExistentProfileDefName = "Name_not_found";
        profDefDAO.saveProfileDefinition(PROFILE_DEF_1);
        assertNull(profDefDAO.findByProfileDefName(nonExistentProfileDefName));
    }

    @Test
    void isMatched_profilePresent_returnsTrue() {
        profDefDAO.saveProfileDefinition(PROFILE_DEF_1);
        assertTrue(profDefDAO.isMatched(PROFILE_DEF_1));
    }

    @Test
    void isMatched_profilePresentButWithDiffParams_returnsFalse() {
        profDefDAO.saveProfileDefinition(PROFILE_DEF_1);
        assertTrue(profDefDAO.isMatched(PROFILE_DEF_1));
    }

    @Test
    void isMatched_profileNotPresent_returnsFalse() {
        assertFalse(profDefDAO.isMatched(PROFILE_DEF_1));
    }

    @Test
    void isMatched_profileMatchesOnlyByName_returnsFalse() {
        profDefDAO.saveProfileDefinition(PROFILE_DEF_1);
        assertFalse(profDefDAO.isMatched(PROFILE_DEF_MATCHING_NAME_ONLY));
    }

    @Test
    void getAffectedProfiles_noAffectedProfiles_returnsEmpty() {
        profDefDAO.saveProfileDefinition(PROFILE_DEF_1);
        Set<ProfileDefinition> affectedProfiles = profDefDAO.getAffectedProfiles(List.of(KPI_DEF_NOT_AFFECTING));
        assertTrue(affectedProfiles.isEmpty());
    }

    @Test
    void getAffectedProfiles_affectedProfilesExist_returns() {
        profDefDAO.saveProfileDefinition(PROFILE_DEF_1);
        Set<ProfileDefinition> affectedProfiles = profDefDAO.getAffectedProfiles(List.of(KPI_DEF_AFFECTING));
        assertEquals(affectedProfiles, Set.of(PROFILE_DEF_1));
    }

    @Test
    void insertProfileDefinitions() {
        profDefDAO.insertProfileDefinitions(List.of(PROFILE_DEF_1));
        assertEquals(1, profDefDAO.getProfileDefinitions().size());
        assertEquals(profDefDAO.getProfileDefinitions(), Set.of(PROFILE_DEF_1));
    }

    @Test
    void findAllProfileDefs_moreRowsThanFound_returnsAllDefs() {
        profDefDAO.saveProfileDefinition(PROFILE_DEF_1);
        profDefDAO.saveProfileDefinition(ProfileDefinition.builder()
                .name("name1")
                .description("description1")
                .context(List.of("agg1", "agg2"))
                .kpis(List.of(KPIReference.builder().ref("kpi_ref_1").build(), KPIReference.builder().ref("kpi_ref_2").build()))
                .build()
        );
        profDefDAO.saveProfileDefinition(ProfileDefinition.builder()
                .name("name2")
                .description("description2")
                .context(List.of("agg3", "agg4"))
                .kpis(List.of(KPIReference.builder().ref("kpi_ref_3").build(), KPIReference.builder().ref("kpi_ref_4").build()))
                .build()
        );

        List<ProfileDefinition> profileDefinitions = profDefDAO.findAllProfileDefinitions(0, 5);
        assertEquals(3, profileDefinitions.size());
    }

    @Test
    void findAllProfileDefs_negativeRows_returnsEmptyList() {
        profDefDAO.saveProfileDefinition(PROFILE_DEF_1);
        profDefDAO.saveProfileDefinition(ProfileDefinition.builder()
                .name("name1")
                .description("description1")
                .context(List.of("agg1", "agg2"))
                .kpis(List.of(KPIReference.builder().ref("kpi_ref_1").build(), KPIReference.builder().ref("kpi_ref_2").build()))
                .build()
        );
        profDefDAO.saveProfileDefinition(ProfileDefinition.builder()
                .name("name2")
                .description("description2")
                .context(List.of("agg3", "agg4"))
                .kpis(List.of(KPIReference.builder().ref("kpi_ref_3").build(), KPIReference.builder().ref("kpi_ref_4").build()))
                .build()
        );
        List<ProfileDefinition> profileDefinitions = profDefDAO.findAllProfileDefinitions(0, -1);
        assertEquals(0, profileDefinitions.size());
    }

    @Test
    void findAllProfileDefs_lessRowsThanFound_returnsQueriedRowsOnly() {
        profDefDAO.saveProfileDefinition(PROFILE_DEF_1);
        profDefDAO.saveProfileDefinition(ProfileDefinition.builder()
                .name("name1")
                .description("description1")
                .context(List.of("agg1", "agg2"))
                .kpis(List.of(KPIReference.builder().ref("kpi_ref_1").build(), KPIReference.builder().ref("kpi_ref_2").build()))
                .build()
        );
        profDefDAO.saveProfileDefinition(ProfileDefinition.builder()
                .name("name2")
                .description("description2")
                .context(List.of("agg3", "agg4"))
                .kpis(List.of(KPIReference.builder().ref("kpi_ref_3").build(), KPIReference.builder().ref("kpi_ref_4").build()))
                .build()
        );
        List<ProfileDefinition> profileDefinitions = profDefDAO.findAllProfileDefinitions(0, 1);
        assertEquals(1, profileDefinitions.size());

        profileDefinitions = profDefDAO.findAllProfileDefinitions(0, 2);
        assertEquals(2, profileDefinitions.size());
    }

    @Test
    void findAllProfileDefs_startFromSecondPage() {
        profDefDAO.saveProfileDefinition(PROFILE_DEF_1);
        profDefDAO.saveProfileDefinition(ProfileDefinition.builder()
                .name("name1")
                .description("description1")
                .context(List.of("agg1", "agg2"))
                .kpis(List.of(KPIReference.builder().ref("kpi_ref_1").build(), KPIReference.builder().ref("kpi_ref_2").build()))
                .build()
        );
        profDefDAO.saveProfileDefinition(ProfileDefinition.builder()
                .name("name2")
                .description("description2")
                .context(List.of("agg3", "agg4"))
                .kpis(List.of(KPIReference.builder().ref("kpi_ref_3").build(), KPIReference.builder().ref("kpi_ref_4").build()))
                .build()
        );
        List<ProfileDefinition> profileDefinitions = profDefDAO.findAllProfileDefinitions(1, 2);
        assertEquals(1, profileDefinitions.size());
    }

    @Test
    void findAllProfileDefs_startFromSecondPage_butOnlyOnePageData_returnsEmptyList() {
        profDefDAO.saveProfileDefinition(PROFILE_DEF_1);
        profDefDAO.saveProfileDefinition(ProfileDefinition.builder()
                .name("name1")
                .description("description1")
                .context(List.of("agg1", "agg2"))
                .kpis(List.of(KPIReference.builder().ref("kpi_ref_1").build(), KPIReference.builder().ref("kpi_ref_2").build()))
                .build()
        );
        profDefDAO.saveProfileDefinition(ProfileDefinition.builder()
                .name("name2")
                .description("description2")
                .context(List.of("agg3", "agg4"))
                .kpis(List.of(KPIReference.builder().ref("kpi_ref_3").build(), KPIReference.builder().ref("kpi_ref_4").build()))
                .build()
        );
        List<ProfileDefinition> profileDefinitions = profDefDAO.findAllProfileDefinitions(1, 5);
        assertEquals(0, profileDefinitions.size());
    }

    @Test
    void findAllProfileDefs_EmptyProfDefMap_EmptyListReturned() {
        assertEquals(0, profDefDAO.totalProfileDefinitions());

        List<ProfileDefinition> profileDefinitions = profDefDAO.findAllProfileDefinitions(0, 10);
        assertEquals(0, profileDefinitions.size());

        profileDefinitions = profDefDAO.findAllProfileDefinitions(0, -1);
        assertEquals(0, profileDefinitions.size());
    }

    @Test
    void clear() {

        profDefDAO.saveProfileDefinition(PROFILE_DEF_1);

        assertFalse(profDefDAO.findAllProfileDefinitions(0, 10).isEmpty());

        profDefDAO.clear();

        assertTrue(profDefDAO.findAllProfileDefinitions(0, 10).isEmpty());
    }
}