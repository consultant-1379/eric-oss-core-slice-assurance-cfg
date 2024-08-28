/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.handler.validation;

import static com.ericsson.oss.air.csac.model.TestResourcesUtils.VALID_COMPLEX_KPI_DEF_NAME;
import static com.ericsson.oss.air.csac.model.TestResourcesUtils.VALID_SIMPLE_KPI_DEF_NAME;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;

import com.ericsson.oss.air.csac.model.AugmentationDefinition;
import com.ericsson.oss.air.csac.model.InputMetric;
import com.ericsson.oss.air.csac.model.InputMetricOverride;
import com.ericsson.oss.air.csac.model.KPIDefinition;
import com.ericsson.oss.air.csac.model.KPIReference;
import com.ericsson.oss.air.csac.model.ProfileDefinition;
import com.ericsson.oss.air.csac.model.ResourceSubmission;
import com.ericsson.oss.air.csac.model.TestResourcesUtils;
import com.ericsson.oss.air.csac.repository.KPIDefinitionDAO;
import com.ericsson.oss.air.exception.CsacValidationException;
import com.ericsson.oss.air.util.logging.FaultHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ProfileContextValidatorTest {

    public static final List<ProfileDefinition> PROFILE_LIST = List.of(ProfileDefinition.builder()
            .name("profile_def_name")
            .description("Description")
            .augmentation(TestResourcesUtils.VALID_AUGMENTATION_NAME)
            .context(List.of("field1", "field2"))
            .kpis(List.of(KPIReference.builder().ref(VALID_SIMPLE_KPI_DEF_NAME).build(),
                    KPIReference.builder().ref(TestResourcesUtils.VALID_COMPLEX_KPI_DEF_NAME).build()))
            .build());

    @Mock
    private KPIDefinitionDAO kpiDefinitionDAO;

    @Mock
    private FaultHandler faultHandler;

    @InjectMocks
    private ProfileContextValidator profileContextValidator;

    ResourceSubmission rs;

    @BeforeEach
    public void setUp() {

        rs = ResourceSubmission.builder()
                .augmentationDefinitions(TestResourcesUtils.VALID_RESOURCE_SUBMISSION.getAugmentationDefinitions())
                .pmDefs(TestResourcesUtils.VALID_RESOURCE_SUBMISSION.getPmDefs())
                .kpiDefs(TestResourcesUtils.VALID_RESOURCE_SUBMISSION.getKpiDefs())
                .profileDefs(PROFILE_LIST)
                .build();
    }

    @Test
    void validateProfileDefinitions_noProfilesPresent_valid() {
        rs.setProfileDefs(null);
        this.profileContextValidator.validateProfileDefinitions(rs);
    }

    @Test
    void validateProfileDefinitions_noAugmentationInProfile_valid() {

        final ProfileDefinition profileDefinition = ProfileDefinition.builder()
                .name("profile_def_name")
                .description("Description")
                .context(List.of("field1", "field2"))
                .kpis(List.of(KPIReference.builder().ref(VALID_SIMPLE_KPI_DEF_NAME).build(),
                        KPIReference.builder().ref(TestResourcesUtils.VALID_COMPLEX_KPI_DEF_NAME).build()))
                .build();

        rs.setProfileDefs(List.of(profileDefinition));

        Mockito.when(kpiDefinitionDAO.findAll()).thenReturn(List.of(TestResourcesUtils.VALID_COMPLEX_KPI_DEF_OBJ));

        this.profileContextValidator.validateProfileDefinitions(rs);
    }

    @Test
    void validateProfileDefinitions_noAugmentationDefinitionsInRs_throwException() {

        rs.setAugmentationDefinitions(null);

        assertThrows(CsacValidationException.class,
                () -> this.profileContextValidator.validateProfileDefinitions(rs));

        rs.setAugmentationDefinitions(new ArrayList<>());

        assertThrows(CsacValidationException.class,
                () -> this.profileContextValidator.validateProfileDefinitions(rs));
    }

    @Test
    void validateProfileDefinitions_noAssociatedAugmentationDefinitionsInRs_throwException() {

        final AugmentationDefinition augmentationDefinition =
                AugmentationDefinition.builder()
                        .name("invalidAugmentationName")
                        .url("http://localhost:8080")
                        .type(TestResourcesUtils.ARDQ_TYPE)
                        .augmentationRules(TestResourcesUtils.VALID_AUGMENTATION_RULE_LIST)
                        .build();

        rs.setAugmentationDefinitions(List.of(augmentationDefinition));

        assertThrows(CsacValidationException.class,
                () -> this.profileContextValidator.validateProfileDefinitions(rs));
    }

    @Test
    void validateProfileDefinitions_noKpiReferencedInProfile_valid() {
        Mockito.when(kpiDefinitionDAO.findAll()).thenReturn(List.of());

        rs.getProfileDefs().get(0).setKpis(List.of());

        this.profileContextValidator.validateProfileDefinitions(rs);
    }

    @Test
    void validateProfileDefinitions_referencedKpiPresentInDD_valid() {
        Mockito.when(kpiDefinitionDAO.findAll()).thenReturn(List.of(TestResourcesUtils.VALID_COMPLEX_KPI_DEF_OBJ));

        this.profileContextValidator.validateProfileDefinitions(rs);
    }

    @Test
    void validate_referencedKpiPresentInDD_valid() {
        Mockito.when(kpiDefinitionDAO.findAll()).thenReturn(List.of(TestResourcesUtils.VALID_COMPLEX_KPI_DEF_OBJ));

        this.profileContextValidator.validate(rs);
    }

    @Test
    void validateProfileDefinitions_referencedKpiPresentInRs_valid() {
        Mockito.when(kpiDefinitionDAO.findAll()).thenReturn(List.of());

        rs.getProfileDefs().get(0).setKpis(List.of(KPIReference.builder().ref(VALID_SIMPLE_KPI_DEF_NAME).build()));

        this.profileContextValidator.validateProfileDefinitions(rs);
    }

    @Test
    void validateProfileDefinitions_referencedKpiNotPresent_exceptionThrown() {
        Mockito.when(kpiDefinitionDAO.findAll()).thenReturn(List.of());

        assertThrows(CsacValidationException.class,
                () -> this.profileContextValidator.validateProfileDefinitions(TestResourcesUtils.VALID_RESOURCE_SUBMISSION));

    }

    @Test
    void validateProfileDefinitions_kpiReferenceInputMetricOverride() {

        Mockito.when(kpiDefinitionDAO.findAll()).thenReturn(List.of(TestResourcesUtils.VALID_COMPLEX_KPI_DEF_OBJ));

        final ProfileDefinition profileDefinition = ProfileDefinition.builder()
                .name("profile")
                .context(List.of())
                .kpis(List.of(KPIReference.builder()
                        .ref(TestResourcesUtils.VALID_COMPLEX_KPI_DEF_OBJ.getName())
                        .inputMetricOverrides(List.of(InputMetricOverride.builder()
                                .id(TestResourcesUtils.COMPLEX_INPUT_METRIC.getId())
                                .build())).build()
                )).build();

        final ResourceSubmission resourceSubmission = ResourceSubmission.builder().profileDefs(List.of(profileDefinition)).build();

        assertDoesNotThrow(() -> this.profileContextValidator.validate(resourceSubmission));
    }

    @Test
    void validateProfileDefinitions_kpiReferenceEmptyInputMetricOverrideList() {

        Mockito.when(kpiDefinitionDAO.findAll()).thenReturn(List.of(TestResourcesUtils.VALID_COMPLEX_KPI_DEF_OBJ));

        final ProfileDefinition profileDefinition = ProfileDefinition.builder()
                .name("profile")
                .kpis(List.of(KPIReference.builder()
                        .ref(TestResourcesUtils.VALID_COMPLEX_KPI_DEF_OBJ.getName())
                        .inputMetricOverrides(List.of()).build()
                )).build();

        final ResourceSubmission resourceSubmission = ResourceSubmission.builder().profileDefs(List.of(profileDefinition)).build();

        assertDoesNotThrow(() -> this.profileContextValidator.validate(resourceSubmission));
    }

    @Test
    void validateProfileDefinitions_kpiReferenceInvalidInputMetricOverride() {

        Mockito.when(kpiDefinitionDAO.findAll()).thenReturn(List.of(TestResourcesUtils.VALID_COMPLEX_KPI_DEF_OBJ));

        final ProfileDefinition profileDefinition = ProfileDefinition.builder()
                .name("profile")
                .context(List.of())
                .kpis(List.of(KPIReference.builder()
                        .ref(TestResourcesUtils.VALID_COMPLEX_KPI_DEF_OBJ.getName())
                        .inputMetricOverrides(List.of(InputMetricOverride.builder()
                                .id("notAnInputMetric")
                                .build())).build()
                )).build();

        final ResourceSubmission resourceSubmission = ResourceSubmission.builder().profileDefs(List.of(profileDefinition)).build();

        assertThrows(CsacValidationException.class, () -> this.profileContextValidator.validate(resourceSubmission));
    }

    @Test
    void validateProfileDefinitions_invalidInputMetricOverrideContextField_noCommonField() {
        Mockito.when(kpiDefinitionDAO.findAll()).thenReturn(List.of(TestResourcesUtils.VALID_COMPLEX_KPI_DEF_OBJ));

        final ProfileDefinition profileDefinition = ProfileDefinition.builder()
                .name("profile")
                .context(List.of("field1", "field2"))
                .kpis(List.of(KPIReference.builder()
                        .ref(TestResourcesUtils.VALID_COMPLEX_KPI_DEF_OBJ.getName())
                        .inputMetricOverrides(List.of(InputMetricOverride.builder()
                                .id(VALID_SIMPLE_KPI_DEF_NAME)
                                .context(List.of("field3"))
                                .build())).build()
                )).build();

        final ResourceSubmission resourceSubmission = ResourceSubmission.builder().profileDefs(List.of(profileDefinition)).build();

        assertThrows(CsacValidationException.class, () -> this.profileContextValidator.validate(resourceSubmission));
    }

    @Test
    void validateProfileDefinitions_validInputMetricOverrideContextField_regardlessOrder() {
        final KPIDefinition complexKpiDef = KPIDefinition.builder().name(VALID_COMPLEX_KPI_DEF_NAME).expression("SUM(p1, p2)").aggregationType("SUM")
                .inputMetrics(List.of(InputMetric.builder().alias("p1").id(VALID_SIMPLE_KPI_DEF_NAME).type(InputMetric.Type.KPI).build(),
                        InputMetric.builder().alias("p2").id("kpi2").type(InputMetric.Type.KPI).build())).build();
        Mockito.when(kpiDefinitionDAO.findAll()).thenReturn(List.of(complexKpiDef));

        final ProfileDefinition profileDefinition = ProfileDefinition.builder()
                .name("profile")
                .context(List.of("field1", "field2", "field3", "field4"))
                .kpis(List.of(KPIReference.builder()
                        .ref(TestResourcesUtils.VALID_COMPLEX_KPI_DEF_OBJ.getName())
                        .inputMetricOverrides(
                                List.of(InputMetricOverride.builder().id(VALID_SIMPLE_KPI_DEF_NAME).context(List.of("field3", "field1")).build(),
                                        InputMetricOverride.builder().id("kpi2").context(List.of("field1", "field3")).build())).build()
                )).build();

        final ResourceSubmission resourceSubmission = ResourceSubmission.builder().profileDefs(List.of(profileDefinition)).build();

        assertDoesNotThrow(() -> this.profileContextValidator.validate(resourceSubmission));
    }

    @Test
    void validateProfileDefinitions_validInputMetricOverrideContextField() {
        Mockito.when(kpiDefinitionDAO.findAll()).thenReturn(List.of(TestResourcesUtils.VALID_COMPLEX_KPI_DEF_OBJ));

        final ProfileDefinition profileDefinition = ProfileDefinition.builder()
                .name("profile")
                .context(List.of("field1", "field2"))
                .kpis(List.of(KPIReference.builder()
                        .ref(TestResourcesUtils.VALID_COMPLEX_KPI_DEF_OBJ.getName())
                        .inputMetricOverrides(List.of(InputMetricOverride.builder()
                                .id(VALID_SIMPLE_KPI_DEF_NAME)
                                .context(List.of("field1"))
                                .build())).build()
                )).build();

        final ResourceSubmission resourceSubmission = ResourceSubmission.builder().profileDefs(List.of(profileDefinition)).build();

        assertDoesNotThrow(() -> this.profileContextValidator.validate(resourceSubmission));
    }

    @Test
    void validateProfileDefinitions_validInputMetricOverrideContextField_emptyList() {
        Mockito.when(kpiDefinitionDAO.findAll()).thenReturn(List.of(TestResourcesUtils.VALID_COMPLEX_KPI_DEF_OBJ));

        final ProfileDefinition profileDefinition = ProfileDefinition.builder()
                .name("profile")
                .context(List.of("field1", "field2"))
                .kpis(List.of(KPIReference.builder()
                        .ref(TestResourcesUtils.VALID_COMPLEX_KPI_DEF_OBJ.getName())
                        .inputMetricOverrides(List.of(InputMetricOverride.builder()
                                .id(VALID_SIMPLE_KPI_DEF_NAME)
                                .context(List.of())
                                .build())).build()
                )).build();

        final ResourceSubmission resourceSubmission = ResourceSubmission.builder().profileDefs(List.of(profileDefinition)).build();

        assertDoesNotThrow(() -> this.profileContextValidator.validate(resourceSubmission));
    }
}
