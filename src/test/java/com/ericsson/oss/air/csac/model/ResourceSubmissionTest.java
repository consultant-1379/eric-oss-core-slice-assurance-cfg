/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.model;

import static com.ericsson.oss.air.csac.handler.augmentation.LiveAugmentationHandlerTest.ARDQ_URL;
import static com.ericsson.oss.air.csac.handler.augmentation.LiveAugmentationHandlerTest.URL_REFERENCE;
import static com.ericsson.oss.air.csac.model.AugmentationDefinitionTest.TEST_AUGMENTATION;
import static com.ericsson.oss.air.csac.model.TestResourcesUtils.PM_COUNTER_NAME;
import static com.ericsson.oss.air.csac.model.TestResourcesUtils.PM_SCHEMA_CONTEXT;
import static com.ericsson.oss.air.csac.model.TestResourcesUtils.VALID_DEF_WITH_ONE_COUNTER_STR;
import static com.ericsson.oss.air.csac.model.TestResourcesUtils.VALID_PM_SCHEMA_DEFINITION_PM_COUNTER;
import static com.ericsson.oss.air.csac.model.TestResourcesUtils.VALID_PM_SCHEMA_DEFINITION_WO_PM_COUNTERS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import com.ericsson.oss.air.csac.model.pmschema.SchemaURI;
import com.ericsson.oss.air.exception.CsacValidationException;
import com.ericsson.oss.air.util.codec.Codec;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;
import org.junit.jupiter.api.Test;

class ResourceSubmissionTest {

    // define PMDefinition
    private static final String VALID_PM_DEF_NAME = "valid_pmdef_name";

    private static final String INVALID_PM_DEF_NAME = " invalid pmdef_name";

    private static final String VALID_PM_DEF_SOURCE = "pmdef_source";

    private static final String PM_DESCRIPTION = "pmdef description";

    private static final String VALID_PM_DEF =
            "{\"name\":\"" + VALID_PM_DEF_NAME + "\", \"source\":\"" + VALID_PM_DEF_SOURCE + "\", \"description\":\"" + PM_DESCRIPTION + "\"}";

    private static final String INVALID_PM_DEF =
            "{\"name\":\"" + INVALID_PM_DEF_NAME + "\", \"source\":\"" + VALID_PM_DEF_SOURCE + "\", \"description\":\"" + PM_DESCRIPTION + "\"}";

    private static final PMDefinition VALID_PM_DEF_OBJ = new PMDefinition(VALID_PM_DEF_NAME, VALID_PM_DEF_SOURCE, PM_DESCRIPTION);

    public static final PMDefinition VALID_PM_DEF_OBJ_OVERWRITE = new PMDefinition(VALID_PM_DEF_NAME, "SOURCE_OVERWRITE", "PM_DESCRIPTION_OVERWRITE");

    private static final List<PMDefinition> VALID_LIST_PM_DEF_OBJ = List.of(VALID_PM_DEF_OBJ);

    // define KPIDefinition
    private static final String VALID_KPI_DEF_NAME = "valid_kpidef_name";

    private static final String KPI_DEF_DESCRIPTION = "kpidef decription";

    private static final String VALID_KPI_DEF_DISPLAY_NAME = "kpidef_display_name";

    private static final String VALID_KPI_DEF_EXPRESSION = "kpidef_expression";

    private static final String VALID_KPI_DEF_AGGREGATION_TYPE = "MAX";

    private static final boolean VALID_KPI_DEF_IS_VISIBLE = true;

    // define InputMetric List
    private static final String VALID_ID = "input_metric_id";

    private static final String VALID_ALIAS = "input_alias";

    private static final String VALID_TYPE = "pm_data";

    private static final String VALID_KPI_DEF =
            "{\"name\":\"" + VALID_KPI_DEF_NAME + "\", " + "\"description\":\"" + KPI_DEF_DESCRIPTION + "\", " + "\"display_name\":\""
                    + VALID_KPI_DEF_DISPLAY_NAME + "\", " + "\"expression\":\"" + VALID_KPI_DEF_EXPRESSION + "\", " + "\"aggregation_type\":\""
                    + VALID_KPI_DEF_AGGREGATION_TYPE + "\", " + "\"is_visible\":\"" + VALID_KPI_DEF_IS_VISIBLE + "\", " + "\"input_metrics\":["
                    + "{\"id\":\""
                    + VALID_ID + "\", " + "\"alias\":\"" + VALID_ALIAS + "\", " + "\"type\":\"" + VALID_TYPE + "\"}]}";

    private static final String INVALID_KPI_DEF =
            "{\"name\":\"" + VALID_KPI_DEF_NAME + "\", " + "\"description\":\"" + KPI_DEF_DESCRIPTION + "\", " + "\"display_name\":\""
                    + VALID_KPI_DEF_DISPLAY_NAME + "\", " + "\"expression\":\"\", " + "\"aggregation_type\":\"" + VALID_KPI_DEF_AGGREGATION_TYPE
                    + "\", "
                    + "\"is_visible\":\"" + VALID_KPI_DEF_IS_VISIBLE + "\", " + "\"input_metrics\":[" + "{\"id\":\"" + VALID_ID + "\", "
                    + "\"alias\":\""
                    + VALID_ALIAS + "\", " + "\"type\":\"" + VALID_TYPE + "\"}]}";

    private static final InputMetric VALID_INPUT_METRIC = new InputMetric(VALID_ID, VALID_ALIAS, InputMetric.Type.fromString(VALID_TYPE));

    private static final List<InputMetric> VALID_LIST_INPUT_METRICS = List.of(VALID_INPUT_METRIC);

    private static final KPIDefinition VALID_KPI_DEF_OBJ = new KPIDefinition(VALID_KPI_DEF_NAME, KPI_DEF_DESCRIPTION, VALID_KPI_DEF_DISPLAY_NAME,
            VALID_KPI_DEF_EXPRESSION, VALID_KPI_DEF_AGGREGATION_TYPE, null, VALID_KPI_DEF_IS_VISIBLE, VALID_LIST_INPUT_METRICS);

    private static final KPIDefinition VALID_KPI_DEF_OBJ_OVERWRITE = new KPIDefinition(VALID_KPI_DEF_NAME, "KPI_DEF_DESCRIPTION_OVERWRITE",
            "DISPLAY_NAME_OVERWRITE",
            VALID_KPI_DEF_EXPRESSION, VALID_KPI_DEF_AGGREGATION_TYPE, null, VALID_KPI_DEF_IS_VISIBLE, VALID_LIST_INPUT_METRICS);

    private static final List<KPIDefinition> VALID_LIST_KPI_DEF_OBJ = List.of(VALID_KPI_DEF_OBJ);

    // define ProfileDefinition
    private static final String VALID_PROFILE_DEF_NAME = "valid_profiledef_name";

    private static final String PROFILE_DEF_DESCRIPTION = "profiledef decription";

    private static final String VALID_PROFILE_DEF_AGGREGATION_FIELD = "field";

    private static final List<String> VALID_PROFILE_DEF_AGGREGATION_FIELDS = List.of(VALID_PROFILE_DEF_AGGREGATION_FIELD);

    private static final String VALID_REF = "kpi_reference";

    // define KPI References
    private static final KPIReference VALID_KPI_REF = KPIReference.builder().ref(VALID_REF).build();

    private static final List<KPIReference> VALID_LIST_KPI_REFS = List.of(VALID_KPI_REF);

    private static final String VALID_PROFILE_DEF =
            "{\"name\":\"" + VALID_PROFILE_DEF_NAME + "\", " + "\"description\":\"" + PROFILE_DEF_DESCRIPTION + "\", " + "\"aggregation_fields\":[\""
                    + VALID_PROFILE_DEF_AGGREGATION_FIELD + "\"], " + "\"kpis\":[" + "{\"ref\":\"" + VALID_REF + "\"}]}";

    private static final String INVALID_PROFILE_DEF =
            "{\"name\":\"" + VALID_PROFILE_DEF_NAME + "\", " + "\"description\":\"" + PROFILE_DEF_DESCRIPTION + "\", " + "\"aggregation_fields\":[], "
                    + "\"kpis\":[" + "{\"ref\":\"" + VALID_REF + "\"}]}";

    private static final ProfileDefinition VALID_PROFILE_DEF_OBJ = ProfileDefinition.builder()
            .name(VALID_PROFILE_DEF_NAME)
            .description(PROFILE_DEF_DESCRIPTION)
            .context(VALID_PROFILE_DEF_AGGREGATION_FIELDS)
            .kpis(VALID_LIST_KPI_REFS)
            .build();

    private static final ProfileDefinition VALID_PROFILE_DEF_OBJ_OVERWRITE = ProfileDefinition.builder()
            .name(VALID_PROFILE_DEF_NAME)
            .description("DESCRIPTION_OVERWRITE")
            .context(VALID_PROFILE_DEF_AGGREGATION_FIELDS)
            .kpis(VALID_LIST_KPI_REFS)
            .build();

    private static final List<ProfileDefinition> VALID_LIST_PROFILE_DEF_OBJ = List.of(VALID_PROFILE_DEF_OBJ);

    // define ResourceSubmisstion
    private static final String VALID_RESOURCE_SUBMISSION =
            "{\"pm_defs\": [" + VALID_PM_DEF + "], " + "\"kpi_defs\": [" + VALID_KPI_DEF + "], " + "\"profile_defs\": [" + VALID_PROFILE_DEF + "]}";

    private static final String INVALID_RESOURCE_SUBMISSION_WITH_PM_DEF =
            "{\"pm_defs\": [" + INVALID_PM_DEF + "], " + "\"kpi_defs\": [" + VALID_KPI_DEF + "], " + "\"profile_defs\": [" + VALID_PROFILE_DEF + "]}";

    private static final String INVALID_RESOURCE_SUBMISSION_WITH_KPI_DEF =
            "{\"pm_defs\": [" + VALID_PM_DEF + "], " + "\"kpi_defs\": [" + INVALID_KPI_DEF + "], " + "\"profile_defs\": [" + VALID_PROFILE_DEF + "]}";

    private static final String INVALID_RESOURCE_SUBMISSION_WITH_PROFILE_DEF =
            "{\"pm_defs\": [" + VALID_PM_DEF + "], " + "\"kpi_defs\": [" + VALID_KPI_DEF + "], " + "\"profile_defs\": [" + INVALID_PROFILE_DEF + "]}";

    private final AugmentationRuleField ruleField = AugmentationRuleField.builder()
            .output("outputField")
            .inputFields(List.of("inputField1", "inputField2"))
            .build();

    private final AugmentationRule validAugmentationRule = AugmentationRule.builder()
            .inputSchemaReference("input|schema|reference")
            .fields(List.of(ruleField))
            .build();

    private final AugmentationDefinition validAugmentation = AugmentationDefinition.builder()
            .name(TEST_AUGMENTATION)
            .url(ARDQ_URL)
            .augmentationRules(List.of(this.validAugmentationRule))
            .build();

    private final AugmentationDefinition newAugmentation = AugmentationDefinition.builder()
            .name("newAugmentation")
            .url(URL_REFERENCE)
            .augmentationRules(List.of(this.validAugmentationRule))
            .build();

    private final ResourceSubmission resourceSubmissionOnlyAugmentation = ResourceSubmission.builder()
            .augmentationDefinitions(List.of(this.validAugmentation))
            .build();

    private static final String VALID_RS_AUGMENTATION_ONLY = "{\"augmentations\":[{\"ardq_id\":\"testAugmentation\",\"ardq_url\":\"http://test.com:8080\",\"ardq_rules\":[{\"input_schema\":\"input|schema|reference\",\"fields\":[{\"output\":\"outputField\",\"input\":[\"inputField1\",\"inputField2\"]}]}]}]}";

    // PMSchemaDefinition constants
    private static final String NEW_PM_SCHEMA_DEF_NAME = "newPMSchemaDef";

    private static final PMSchemaDefinition NEW_PM_SCHEMA_DEF = PMSchemaDefinition.builder()
            .name(NEW_PM_SCHEMA_DEF_NAME)
            .uri(SchemaURI.fromString("dc:foo|bar|foobar"))
            .context(PM_SCHEMA_CONTEXT)
            .build();

    private static final ResourceSubmission RESOURCE_SUBMISSION_ONLY_PM_SCHEMAS = ResourceSubmission.builder()
            .pmSchemaDefs(List.of(VALID_PM_SCHEMA_DEFINITION_PM_COUNTER))
            .build();

    private static final String VALID_RS_PM_SCHEMA_ONLY = "{\"pm_schemas\":[" + VALID_DEF_WITH_ONE_COUNTER_STR + "]}";

    private final Codec codec = new Codec(Validation.buildDefaultValidatorFactory().getValidator());

    @Test
    public void testValidResourceSubmission() throws JsonProcessingException {

        final ResourceSubmission expectedResSub = ResourceSubmission.builder()
                .pmDefs(VALID_LIST_PM_DEF_OBJ)
                .kpiDefs(VALID_LIST_KPI_DEF_OBJ)
                .profileDefs(VALID_LIST_PROFILE_DEF_OBJ)
                .build();

        final ResourceSubmission resSub = this.codec.withValidation().readValue(VALID_RESOURCE_SUBMISSION, ResourceSubmission.class);

        assertNotNull(resSub);
        assertEquals(expectedResSub, resSub);
    }

    @Test
    public void testResourceSubmissionfWithInvalidPMDef() {
        assertThrows(ConstraintViolationException.class,
                () -> this.codec.withValidation().readValue(INVALID_RESOURCE_SUBMISSION_WITH_PM_DEF, ResourceSubmission.class));
    }

    @Test
    public void testResourceSubmissionfWithInvalidKPIDef() {
        assertThrows(ConstraintViolationException.class,
                () -> this.codec.withValidation().readValue(INVALID_RESOURCE_SUBMISSION_WITH_KPI_DEF, ResourceSubmission.class));
    }

    @Test
    public void testResourceSubmissionfWithInvalidProfileDef() {
        assertThrows(ConstraintViolationException.class,
                () -> this.codec.withValidation().readValue(INVALID_RESOURCE_SUBMISSION_WITH_PROFILE_DEF, ResourceSubmission.class));
    }

    @Test
    void testHasPmDefs() {

        ResourceSubmission rs1 = ResourceSubmission.builder().pmDefs(TestResourcesUtils.VALID_LIST_PM_DEF_OBJ).build();
        assertTrue(rs1.hasPmDefs());

        ResourceSubmission rs2 = new ResourceSubmission();
        assertFalse(rs2.hasPmDefs());

        ResourceSubmission rs3 = ResourceSubmission.builder().pmDefs(Collections.emptyList()).build();
        assertFalse(rs3.hasPmDefs());
    }

    @Test
    void testHasKpiDefs() {

        ResourceSubmission rs1 = ResourceSubmission.builder().kpiDefs(TestResourcesUtils.VALID_LIST_KPI_DEF_OBJ).build();
        assertTrue(rs1.hasKpiDefs());

        ResourceSubmission rs2 = new ResourceSubmission();
        assertFalse(rs2.hasKpiDefs());

        ResourceSubmission rs3 = ResourceSubmission.builder().kpiDefs(Collections.emptyList()).build();
        assertFalse(rs3.hasKpiDefs());
    }

    @Test
    void testHasProfileDefs() {

        ResourceSubmission rs1 = ResourceSubmission.builder().profileDefs(TestResourcesUtils.VALID_LIST_PROFILE_DEF_OBJ).build();
        assertTrue(rs1.hasProfileDefs());

        ResourceSubmission rs2 = new ResourceSubmission();
        assertFalse(rs2.hasProfileDefs());

        ResourceSubmission rs3 = ResourceSubmission.builder().profileDefs(Collections.emptyList()).build();
        assertFalse(rs3.hasProfileDefs());
    }

    @Test
    void testMergeResourceSubmission_newRs_merged() {

        ResourceSubmission masterRs = ResourceSubmission.builder()
                .pmDefs(List.of(TestResourcesUtils.VALID_PM_DEF_OBJ))
                .kpiDefs(List.of(TestResourcesUtils.VALID_SIMPLE_KPI_DEF_OBJ))
                .profileDefs(List.of(TestResourcesUtils.VALID_PROFILE_DEF_OBJ))
                .build();

        ResourceSubmission newRs = ResourceSubmission.builder()
                .pmDefs(List.of(VALID_PM_DEF_OBJ))
                .kpiDefs(List.of(VALID_KPI_DEF_OBJ))
                .profileDefs(List.of(VALID_PROFILE_DEF_OBJ))
                .build();

        masterRs.mergeResourceSubmission(newRs);

        List<String> pmNames = masterRs.getPmDefs().stream().map(PMDefinition::getName).collect(Collectors.toList());
        assertTrue(pmNames.contains(TestResourcesUtils.VALID_PM_DEF_OBJ.getName()));
        assertTrue(pmNames.contains(VALID_PM_DEF_OBJ.getName()));

        List<String> kpiNames = masterRs.getKpiDefs().stream().map(KPIDefinition::getName).collect(Collectors.toList());
        assertTrue(kpiNames.contains(TestResourcesUtils.VALID_SIMPLE_KPI_DEF_OBJ.getName()));
        assertTrue(kpiNames.contains(VALID_KPI_DEF_OBJ.getName()));

        List<String> profNames = masterRs.getProfileDefs().stream().map(ProfileDefinition::getName).collect(Collectors.toList());
        assertTrue(profNames.contains(TestResourcesUtils.VALID_PROFILE_DEF_OBJ.getName()));
        assertTrue(profNames.contains(VALID_PROFILE_DEF_OBJ.getName()));
    }

    @Test
    void testMergeResourceSubmission_nullNewRs_exceptionThrown() {
        ResourceSubmission masterRs = ResourceSubmission.builder()
                .pmDefs(List.of(TestResourcesUtils.VALID_PM_DEF_OBJ))
                .kpiDefs(List.of(TestResourcesUtils.VALID_SIMPLE_KPI_DEF_OBJ))
                .profileDefs(List.of(TestResourcesUtils.VALID_PROFILE_DEF_OBJ))
                .build();

        assertThrows(CsacValidationException.class,
                () -> masterRs.mergeResourceSubmission(null));
    }

    @Test
    void testMergeResourceSubmission_emptyNewRs_masterRsUnchanged() {
        ResourceSubmission masterRs = ResourceSubmission.builder()
                .pmDefs(List.of(TestResourcesUtils.VALID_PM_DEF_OBJ))
                .kpiDefs(List.of(TestResourcesUtils.VALID_SIMPLE_KPI_DEF_OBJ))
                .profileDefs(List.of(TestResourcesUtils.VALID_PROFILE_DEF_OBJ))
                .build();

        masterRs.mergeResourceSubmission(new ResourceSubmission());

        assertEquals(masterRs.getPmDefs(), List.of(TestResourcesUtils.VALID_PM_DEF_OBJ));
        assertEquals(masterRs.getKpiDefs(), List.of(TestResourcesUtils.VALID_SIMPLE_KPI_DEF_OBJ));
        assertEquals(masterRs.getProfileDefs(), List.of(TestResourcesUtils.VALID_PROFILE_DEF_OBJ));
    }

    @Test
    void testMergeResourceSubmission_emptyExistingRs_newRsReturned() {
        ResourceSubmission newRs = ResourceSubmission.builder()
                .pmDefs(List.of(TestResourcesUtils.VALID_PM_DEF_OBJ))
                .kpiDefs(List.of(TestResourcesUtils.VALID_SIMPLE_KPI_DEF_OBJ))
                .profileDefs(List.of(TestResourcesUtils.VALID_PROFILE_DEF_OBJ))
                .build();

        ResourceSubmission masterRs = new ResourceSubmission();
        masterRs.mergeResourceSubmission(newRs);

        assertEquals(masterRs.getPmDefs(), List.of(TestResourcesUtils.VALID_PM_DEF_OBJ));
        assertEquals(masterRs.getKpiDefs(), List.of(TestResourcesUtils.VALID_SIMPLE_KPI_DEF_OBJ));
        assertEquals(masterRs.getProfileDefs(), List.of(TestResourcesUtils.VALID_PROFILE_DEF_OBJ));
    }

    @Test
    void testMergeResourceSubmission_emptyNewRs_emptyMasterRs_emptyRsReturned() {
        ResourceSubmission masterRs = new ResourceSubmission();
        masterRs.mergeResourceSubmission(new ResourceSubmission());

        assertFalse(masterRs.hasPmDefs());
        assertFalse(masterRs.hasKpiDefs());
        assertFalse(masterRs.hasProfileDefs());
    }

    @Test
    void testMergeResourceSubmission_newRsWithUpdatedMetrics_MasterRsOverwrittenWithLatestMetrics() {
        ResourceSubmission masterRs = ResourceSubmission.builder()
                .pmDefs(List.of(VALID_PM_DEF_OBJ))
                .kpiDefs(List.of(VALID_KPI_DEF_OBJ))
                .profileDefs(List.of(VALID_PROFILE_DEF_OBJ))
                .build();

        ResourceSubmission newRs = ResourceSubmission.builder()
                .pmDefs(List.of(VALID_PM_DEF_OBJ_OVERWRITE))
                .kpiDefs(List.of(VALID_KPI_DEF_OBJ_OVERWRITE))
                .profileDefs(List.of(VALID_PROFILE_DEF_OBJ_OVERWRITE))
                .build();

        assertEquals(VALID_PM_DEF_OBJ_OVERWRITE.getName(), VALID_PM_DEF_OBJ.getName());
        assertEquals(VALID_KPI_DEF_OBJ_OVERWRITE.getName(), VALID_KPI_DEF_OBJ.getName());
        assertEquals(VALID_PROFILE_DEF_OBJ_OVERWRITE.getName(), VALID_PROFILE_DEF_OBJ.getName());

        masterRs.mergeResourceSubmission(newRs);

        assertEquals(1, masterRs.getPmDefs().size());
        assertEquals(1, masterRs.getKpiDefs().size());
        assertEquals(1, masterRs.getProfileDefs().size());

        assertEquals(VALID_PM_DEF_OBJ_OVERWRITE.getName(), masterRs.getPmDefs().get(0).getName());
        assertEquals(VALID_PM_DEF_OBJ_OVERWRITE.getSource(), masterRs.getPmDefs().get(0).getSource());
        assertEquals(VALID_PM_DEF_OBJ_OVERWRITE.getDescription(), masterRs.getPmDefs().get(0).getDescription());

        assertEquals(VALID_KPI_DEF_OBJ_OVERWRITE.getName(), masterRs.getKpiDefs().get(0).getName());
        assertEquals(VALID_KPI_DEF_OBJ_OVERWRITE.getDisplayName(), masterRs.getKpiDefs().get(0).getDisplayName());
        assertEquals(VALID_KPI_DEF_OBJ_OVERWRITE.getDescription(), masterRs.getKpiDefs().get(0).getDescription());

        assertEquals(VALID_PROFILE_DEF_OBJ_OVERWRITE.getName(), masterRs.getProfileDefs().get(0).getName());
        assertEquals(VALID_PROFILE_DEF_OBJ_OVERWRITE.getDescription(), masterRs.getProfileDefs().get(0).getDescription());
    }

    @Test
    public void testAllFields_valid() throws JsonProcessingException {

        final PMDefinition expectedPm = PMDefinition.builder()
                .name(VALID_PM_DEF_NAME)
                .source(VALID_PM_DEF_SOURCE)
                .description(PM_DESCRIPTION)
                .build();

        final InputMetric expectedInputMetric = new InputMetric(VALID_ID, VALID_ALIAS, InputMetric.Type.fromString(VALID_TYPE));

        final KPIDefinition expectedKpi = KPIDefinition.builder()
                .name(VALID_KPI_DEF_NAME)
                .description(KPI_DEF_DESCRIPTION)
                .displayName(VALID_KPI_DEF_DISPLAY_NAME)
                .expression(VALID_KPI_DEF_EXPRESSION)
                .aggregationType(VALID_KPI_DEF_AGGREGATION_TYPE)
                .isVisible(VALID_KPI_DEF_IS_VISIBLE)
                .inputMetrics(List.of(expectedInputMetric))
                .build();

        final ProfileDefinition expectedProfile = ProfileDefinition.builder()
                .name(VALID_PROFILE_DEF_NAME)
                .description(PROFILE_DEF_DESCRIPTION)
                .context(List.of(VALID_PROFILE_DEF_AGGREGATION_FIELD))
                .kpis(List.of(KPIReference.builder().ref(VALID_REF).build()))
                .build();

        final ResourceSubmission expected = ResourceSubmission.builder()
                .pmDefs(List.of(expectedPm))
                .kpiDefs(List.of(expectedKpi))
                .profileDefs(List.of(expectedProfile))
                .build();

        final ResourceSubmission actual = this.codec.withValidation().readValue(VALID_RESOURCE_SUBMISSION, ResourceSubmission.class);

        assertEquals(expected, actual);
    }

    @Test
    public void testToString_noFields_valid() throws JsonProcessingException {
        final ResourceSubmission resSub = this.codec.withValidation().readValue("{}", ResourceSubmission.class);

        assertEquals(new ResourceSubmission(), resSub);

        assertFalse(resSub.hasAugmentationDefs());
        assertFalse(resSub.hasPmSchemaDefs());
    }

    @Test
    public void testResourceSubmission_augmentationOnly() throws Exception {

        final ResourceSubmission actual = this.codec.withValidation().readValue(VALID_RS_AUGMENTATION_ONLY, ResourceSubmission.class);

        assertEquals(this.resourceSubmissionOnlyAugmentation, actual);

        assertTrue(this.resourceSubmissionOnlyAugmentation.hasAugmentationDefs());
    }

    @Test
    public void testResourceSubmission_mergeWithAugmentation() throws Exception {

        final ResourceSubmission masterSubmission = ResourceSubmission.builder()
                .augmentationDefinitions(List.of(this.validAugmentation))
                .build();

        final ResourceSubmission newSubmssion = ResourceSubmission.builder()
                .augmentationDefinitions(List.of(this.newAugmentation))
                .build();

        masterSubmission.mergeResourceSubmission(newSubmssion);

        assertEquals(2, masterSubmission.getAugmentationDefinitions().size());

        // get the list of augmentations sorted by name
        final List<AugmentationDefinition> sorted = masterSubmission.getAugmentationDefinitions().stream()
                .sorted((a, b) -> a.getName().compareTo(b.getName())).collect(Collectors.toList());

        assertEquals(this.newAugmentation, sorted.get(0));
        assertEquals(this.validAugmentation, sorted.get(1));

    }

    @Test
    public void testResourceSubmission_mergedWithNoNewAugmentations() throws Exception {

        final ResourceSubmission masterSubmission = ResourceSubmission.builder()
                .augmentationDefinitions(List.of(this.validAugmentation))
                .build();

        final ResourceSubmission newSubmssion = ResourceSubmission.builder().build();

        masterSubmission.mergeResourceSubmission(newSubmssion);

        assertEquals(1, masterSubmission.getAugmentationDefinitions().size());
    }

    @Test
    public void testResourceSubmission_pmSchemaDefOnly() throws Exception {

        final ResourceSubmission actual = this.codec.withValidation().readValue(VALID_RS_PM_SCHEMA_ONLY, ResourceSubmission.class);

        assertEquals(RESOURCE_SUBMISSION_ONLY_PM_SCHEMAS, actual);

        assertTrue(RESOURCE_SUBMISSION_ONLY_PM_SCHEMAS.hasPmSchemaDefs());
    }

    @Test
    public void testResourceSubmission_mergeWithPMSchemaDef() throws Exception {

        final ResourceSubmission masterSubmission = ResourceSubmission.builder()
                .pmSchemaDefs(List.of(VALID_PM_SCHEMA_DEFINITION_PM_COUNTER))
                .build();

        final ResourceSubmission newSubmission = ResourceSubmission.builder()
                .pmSchemaDefs(List.of(NEW_PM_SCHEMA_DEF))
                .build();

        masterSubmission.mergeResourceSubmission(newSubmission);

        assertEquals(2, masterSubmission.getPmSchemaDefs().size());

        // get the list of PM schema definitions sorted by name
        final List<PMSchemaDefinition> sorted = masterSubmission.getPmSchemaDefs().stream()
                .sorted(Comparator.comparing(PMSchemaDefinition::getName)).toList();

        assertEquals(NEW_PM_SCHEMA_DEF, sorted.get(0));
        assertEquals(VALID_PM_SCHEMA_DEFINITION_PM_COUNTER, sorted.get(1));

    }

    @Test
    public void testResourceSubmission_mergedWithNoNewPMSchemaDefs() throws Exception {

        final ResourceSubmission masterSubmission = ResourceSubmission.builder()
                .pmSchemaDefs(List.of(VALID_PM_SCHEMA_DEFINITION_PM_COUNTER))
                .build();

        final ResourceSubmission newSubmission = ResourceSubmission.builder().build();

        masterSubmission.mergeResourceSubmission(newSubmission);

        assertEquals(1, masterSubmission.getPmSchemaDefs().size());
    }

    @Test
    public void addPmCountersToPmDefList_NoPMSchemas() {

        final ResourceSubmission resourceSubmission = ResourceSubmission.builder().build();

        resourceSubmission.addPmCountersToPmDefList();

        assertFalse(resourceSubmission.hasPmSchemaDefs());
        assertFalse(resourceSubmission.hasPmDefs());
    }

    @Test
    public void addPmCountersToPmDefList_PMSchemaNoPmCounters() {

        final ResourceSubmission resourceSubmission = ResourceSubmission.builder()
                .pmSchemaDefs(List.of(VALID_PM_SCHEMA_DEFINITION_WO_PM_COUNTERS))
                .build();

        resourceSubmission.addPmCountersToPmDefList();

        assertTrue(resourceSubmission.hasPmSchemaDefs());
        assertFalse(resourceSubmission.hasPmDefs());
    }

    @Test
    public void addPmCountersToPmDefList_OnePmCounter_NoPmDefs() {

        final ResourceSubmission resourceSubmission = ResourceSubmission.builder()
                .pmSchemaDefs(List.of(VALID_PM_SCHEMA_DEFINITION_PM_COUNTER))
                .build();

        resourceSubmission.addPmCountersToPmDefList();

        assertTrue(resourceSubmission.hasPmSchemaDefs());
        assertTrue(resourceSubmission.hasPmDefs());
        assertEquals(1, resourceSubmission.getPmDefs().size());
    }

    @Test
    public void addPmCountersToPmDefList_NoPmCounter_OnePmDef() {

        final ResourceSubmission resourceSubmission = ResourceSubmission.builder()
                .pmSchemaDefs(List.of(VALID_PM_SCHEMA_DEFINITION_WO_PM_COUNTERS))
                .pmDefs(List.of(VALID_PM_DEF_OBJ))
                .build();

        resourceSubmission.addPmCountersToPmDefList();

        assertTrue(resourceSubmission.hasPmSchemaDefs());
        assertTrue(resourceSubmission.hasPmDefs());
        assertEquals(1, resourceSubmission.getPmDefs().size());
    }

    @Test
    public void addPmCountersToPmDefList_OnePmCounter_DifferentPmDef() {

        final ResourceSubmission resourceSubmission = ResourceSubmission.builder()
                .pmSchemaDefs(List.of(VALID_PM_SCHEMA_DEFINITION_PM_COUNTER))
                .pmDefs(List.of(VALID_PM_DEF_OBJ))
                .build();

        resourceSubmission.addPmCountersToPmDefList();

        assertTrue(resourceSubmission.hasPmSchemaDefs());
        assertTrue(resourceSubmission.hasPmDefs());
        assertEquals(2, resourceSubmission.getPmDefs().size());

    }

    @Test
    public void addPmCountersToPmDefList_OnePmCounter_SamePmDef() {

        final String testSource = "testSource";
        final String testDescription = "testDescription";

        final PMDefinition pmDefinition = PMDefinition.builder()
                .name(PM_COUNTER_NAME)
                .source(testSource)
                .description(testDescription)
                .build();

        final ResourceSubmission resourceSubmission = ResourceSubmission.builder()
                .pmSchemaDefs(List.of(VALID_PM_SCHEMA_DEFINITION_PM_COUNTER))
                .pmDefs(List.of(pmDefinition))
                .build();

        resourceSubmission.addPmCountersToPmDefList();

        assertTrue(resourceSubmission.hasPmSchemaDefs());
        assertTrue(resourceSubmission.hasPmDefs());
        assertEquals(1, resourceSubmission.getPmDefs().size());
        assertEquals(pmDefinition, resourceSubmission.getPmDefs().get(0));

    }
}