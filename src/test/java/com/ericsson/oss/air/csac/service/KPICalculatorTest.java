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

import static com.ericsson.oss.air.csac.model.TestResourcesUtils.DEFAULT_AGGREGATION_PERIOD;
import static com.ericsson.oss.air.csac.model.TestResourcesUtils.DEPLOYED_COMPLEX_KPI_OBJ;
import static com.ericsson.oss.air.csac.model.TestResourcesUtils.DEPLOYED_SIMPLE_KPI_OBJ;
import static com.ericsson.oss.air.csac.model.TestResourcesUtils.VALID_COMPLEX_KPI_DEF_NAME;
import static com.ericsson.oss.air.csac.model.TestResourcesUtils.VALID_COMPLEX_KPI_DEF_OBJ;
import static com.ericsson.oss.air.csac.model.TestResourcesUtils.VALID_LIST_PROFILE_DEF_OBJ;
import static com.ericsson.oss.air.csac.model.TestResourcesUtils.VALID_PM_DEF_SOURCE;
import static com.ericsson.oss.air.csac.model.TestResourcesUtils.VALID_PROFILE_DEF_AGGREGATION_FIELD;
import static com.ericsson.oss.air.csac.model.TestResourcesUtils.VALID_PROFILE_DEF_AGGREGATION_FIELD_LIST;
import static com.ericsson.oss.air.csac.model.TestResourcesUtils.VALID_PROFILE_DEF_OBJ;
import static com.ericsson.oss.air.csac.model.TestResourcesUtils.VALID_SCHEMA_NAME;
import static com.ericsson.oss.air.csac.model.TestResourcesUtils.VALID_SIMPLE_KPI_DEF_NAME;
import static com.ericsson.oss.air.csac.model.TestResourcesUtils.VALID_SIMPLE_KPI_DEF_OBJ;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.ericsson.oss.air.csac.configuration.schema.impl.DryrunInputSchemaProvider;
import com.ericsson.oss.air.csac.model.InputMetric;
import com.ericsson.oss.air.csac.model.InputMetricOverride;
import com.ericsson.oss.air.csac.model.KPIDefinition;
import com.ericsson.oss.air.csac.model.KPIReference;
import com.ericsson.oss.air.csac.model.ProfileDefinition;
import com.ericsson.oss.air.csac.model.TestResourcesUtils;
import com.ericsson.oss.air.csac.model.pmsc.AggregationPeriod;
import com.ericsson.oss.air.csac.model.pmsc.KPIDefinitionDTOMapping;
import com.ericsson.oss.air.csac.model.pmsc.KpiDefinitionDTO;
import com.ericsson.oss.air.csac.model.pmsc.KpiDefinitionDTOWithRelationship;
import com.ericsson.oss.air.csac.model.pmsc.KpiTypeEnum;
import com.ericsson.oss.air.csac.repository.AugmentationDefinitionDAO;
import com.ericsson.oss.air.csac.repository.DeployedKpiDefDAO;
import com.ericsson.oss.air.csac.repository.KPIDefinitionDAO;
import com.ericsson.oss.air.csac.repository.PMDefinitionDAO;
import com.ericsson.oss.air.csac.repository.cache.ResolvedKpiCache;
import com.ericsson.oss.air.csac.repository.impl.inmemorydb.DeployedKpiDefDAOImp;
import com.ericsson.oss.air.csac.repository.impl.inmemorydb.KPIDefinitionDAOImpl;
import com.ericsson.oss.air.csac.repository.impl.inmemorydb.PMDefinitionDAOImpl;
import com.ericsson.oss.air.exception.CsacValidationException;
import com.ericsson.oss.air.util.logging.FaultHandler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class KPICalculatorTest {

    private static final KpiDefinitionDTO UPDATED_DEPLOYED_SIMPLE_KPI_OBJ = DEPLOYED_SIMPLE_KPI_OBJ.toBuilder()
            .withAggregationElements(List.of("testSchema.field1", "testSchema.field2"))
            .withAggregationPeriod(15)
            .build();

    private PMDefinitionDAO pmDefinitionDAO;

    private KPIDefinitionDAO kpiDefinitionDAO;

    private DeployedKpiDefDAO deployedKpiDefDAO;

    private KPICalculator kpiCalculator;

    @Mock
    private AugmentationDefinitionDAO augmentationDefinitionDAO;

    @Mock
    private DataCatalogRestClient dataCatalogRestClient;

    @Mock
    private SchemaRegRestClient schemaRegRestClient;

    @Mock
    private FaultHandler faultHandler;

    @Mock
    private ResolvedKpiCache resolvedKpiCache;

    @Mock
    private DryrunInputSchemaProvider inputSchemaProvider;

    private static final String SCHEMA_NAME = "schemaName";

    private static final String SOURCE = "G5|PM_COUNTERS|schemaName";

    private static final KPIDefinition SIMPLE_KPI_A = KPIDefinition.builder()
            .name("kpi_A")
            .expression("AVG" + "(" + "pmCounters.s1" + ")")
            .aggregationType("AVG")
            .isVisible(true)
            .inputMetrics(List.of(new InputMetric("pmCounters.s1", "", InputMetric.Type.PM_DATA)))
            .build();

    private static final KPIDefinition SIMPLE_KPI_B = KPIDefinition.builder()
            .name("kpi_B")
            .expression("SUM" + "(" + "pmCounters.s2" + ")")
            .aggregationType("SUM")
            .isVisible(true)
            .inputMetrics(List.of(new InputMetric("pmCounters.s2", "", InputMetric.Type.PM_DATA)))
            .build();

    private static final KpiDefinitionDTO SIMPLE_KPI_DTO_A = KPIDefinitionDTOMapping.createSimpleKPIDTO(SIMPLE_KPI_A,
            List.of(VALID_PROFILE_DEF_AGGREGATION_FIELD), SCHEMA_NAME, SOURCE, 15);

    private static final KpiDefinitionDTO SIMPLE_KPI_DTO_B = KPIDefinitionDTOMapping.createSimpleKPIDTO(SIMPLE_KPI_B,
            List.of(VALID_PROFILE_DEF_AGGREGATION_FIELD), SCHEMA_NAME, SOURCE, 15);

    @BeforeEach
    void setUp() {
        this.kpiDefinitionDAO = new KPIDefinitionDAOImpl();
        this.pmDefinitionDAO = new PMDefinitionDAOImpl();
        this.deployedKpiDefDAO = new DeployedKpiDefDAOImp();
        this.resolvedKpiCache = new ResolvedKpiCache();
        this.inputSchemaProvider = new DryrunInputSchemaProvider();

        this.pmDefinitionDAO.savePMDefinition(TestResourcesUtils.VALID_PM_DEF_OBJ, VALID_SCHEMA_NAME);
        this.kpiDefinitionDAO.saveKPIDefinition(TestResourcesUtils.VALID_SIMPLE_KPI_DEF_OBJ);
        this.kpiDefinitionDAO.saveKPIDefinition(TestResourcesUtils.VALID_COMPLEX_KPI_DEF_OBJ);

        this.kpiCalculator = new KPICalculator(this.pmDefinitionDAO, this.kpiDefinitionDAO, this.deployedKpiDefDAO, this.inputSchemaProvider,
                this.resolvedKpiCache);
        this.kpiCalculator.setAggregationPeriod(DEFAULT_AGGREGATION_PERIOD);
    }

    @AfterEach
    void tearDown() {
        this.kpiDefinitionDAO = null;
        this.pmDefinitionDAO = null;
        this.kpiCalculator = null;
        this.inputSchemaProvider = null;
    }

    @Test
    void calculateAffectedKPI_NewKPIs() {
        final List<KpiDefinitionDTOWithRelationship> kpiDefinitionDTOS = this.kpiCalculator.calculateAffectedKPIs(VALID_LIST_PROFILE_DEF_OBJ);
        Assertions.assertEquals(2, kpiDefinitionDTOS.size());
    }

    @Test
    void calculateAffectedKPI_UnchangedKPIs_addedToAffectedKPIs() {
        this.deployedKpiDefDAO.createDeployedKpi(DEPLOYED_SIMPLE_KPI_OBJ, VALID_SIMPLE_KPI_DEF_NAME, VALID_PROFILE_DEF_OBJ);
        this.deployedKpiDefDAO.createDeployedKpi(DEPLOYED_COMPLEX_KPI_OBJ, VALID_COMPLEX_KPI_DEF_NAME, VALID_PROFILE_DEF_OBJ);

        final List<KpiDefinitionDTOWithRelationship> kpiDefinitionDTOS = this.kpiCalculator.calculateAffectedKPIs(VALID_LIST_PROFILE_DEF_OBJ);
        Assertions.assertEquals(2, kpiDefinitionDTOS.size());

        final var kpiNames = kpiDefinitionDTOS.stream().map(KpiDefinitionDTO::getName).collect(Collectors.toList());
        final var deployedKpiNames =
                this.deployedKpiDefDAO.getAllDeployedKpis().stream().map(KpiDefinitionDTO::getName).collect(Collectors.toList());
        assertThat(kpiNames).hasSameElementsAs(deployedKpiNames);
    }

    @Test
    void calculateAffectedKPI_ExistingKpisContentChange_changedKpiSentWithOldName() {
        this.deployedKpiDefDAO.createDeployedKpi(DEPLOYED_SIMPLE_KPI_OBJ, VALID_SIMPLE_KPI_DEF_NAME, VALID_PROFILE_DEF_OBJ);
        this.deployedKpiDefDAO.createDeployedKpi(DEPLOYED_COMPLEX_KPI_OBJ, VALID_COMPLEX_KPI_DEF_NAME, VALID_PROFILE_DEF_OBJ);

        assertTrue(this.deployedKpiDefDAO.getDeployedKpiByDefinitionName(VALID_SIMPLE_KPI_DEF_NAME).get(0).getIsVisible());

        final KPIDefinition updatedSimpleKPI = VALID_SIMPLE_KPI_DEF_OBJ.toBuilder().isVisible(false).build();
        this.kpiDefinitionDAO.saveKPIDefinition(updatedSimpleKPI);

        final List<KpiDefinitionDTOWithRelationship> kpiDefinitionDTOS = this.kpiCalculator.calculateAffectedKPIs(VALID_LIST_PROFILE_DEF_OBJ);
        Assertions.assertEquals(2, kpiDefinitionDTOS.size());

        final var kpiNames = kpiDefinitionDTOS.stream().map(KpiDefinitionDTO::getName).collect(Collectors.toList());
        final var deployedKpiNames =
                this.deployedKpiDefDAO.getAllDeployedKpis().stream().map(KpiDefinitionDTO::getName).collect(Collectors.toList());
        assertThat(kpiNames).hasSameElementsAs(deployedKpiNames);

        assertFalse(kpiDefinitionDTOS.stream().filter(dto -> VALID_SIMPLE_KPI_DEF_NAME.equals(dto.getKpiName()))
                .collect(Collectors.toList()).get(0).getIsVisible());
    }

    @Test
    void calculateAffectedKPI_newProfile_newAffectedKpiList() {
        this.deployedKpiDefDAO.createDeployedKpi(DEPLOYED_SIMPLE_KPI_OBJ, VALID_SIMPLE_KPI_DEF_NAME, VALID_PROFILE_DEF_OBJ);
        this.deployedKpiDefDAO.createDeployedKpi(DEPLOYED_COMPLEX_KPI_OBJ, VALID_COMPLEX_KPI_DEF_NAME, VALID_PROFILE_DEF_OBJ);

        final ProfileDefinition newProfile = VALID_PROFILE_DEF_OBJ.toBuilder().context(List.of("NewField1", "NewField2")).build();
        final List<KpiDefinitionDTOWithRelationship> kpiDefinitionDTOS = this.kpiCalculator.calculateAffectedKPIs(List.of(newProfile));

        Assertions.assertEquals(2, kpiDefinitionDTOS.size());

        final var kpiNames = kpiDefinitionDTOS.stream().map(KpiDefinitionDTO::getName).collect(Collectors.toList());
        final var deployedKpiNames =
                this.deployedKpiDefDAO.getAllDeployedKpis().stream().map(KpiDefinitionDTO::getName).collect(Collectors.toList());
        assertThat(kpiNames).doesNotContainAnyElementsOf(deployedKpiNames);
    }

    @Test
    void calculateAffectedKPI_ComplexKpiWithNewSimpleChildKpi() {
        final ProfileDefinition newProfile = VALID_PROFILE_DEF_OBJ.toBuilder()
                .kpis(List.of(KPIReference.builder().ref(VALID_COMPLEX_KPI_DEF_NAME).build())).build();
        final List<KpiDefinitionDTOWithRelationship> kpiDefinitionDTOS = this.kpiCalculator.calculateAffectedKPIs(List.of(newProfile));

        Assertions.assertEquals(2, kpiDefinitionDTOS.size());
    }

    @Test
    void calculateAffectedKPI_ComplexKpiInputMetricsOverride_ShouldCalculateTwoComplexKpis() {

        final ProfileDefinition newProfile = VALID_PROFILE_DEF_OBJ.toBuilder()
                .kpis(List.of(KPIReference.builder().ref(VALID_COMPLEX_KPI_DEF_NAME)
                        .inputMetricOverrides(List.of(InputMetricOverride.builder().id(VALID_SIMPLE_KPI_DEF_NAME)
                                .context(List.of("field1", "field2", "field3"))
                                .build())).build())).build();

        final List<KpiDefinitionDTOWithRelationship> kpiDefinitionDTOS = this.kpiCalculator.calculateAffectedKPIs(
                List.of(newProfile, VALID_PROFILE_DEF_OBJ));

        // simple_field1, complex_field1_field2 (override), simple_field1_field2, complex_field1_field2
        // Note: a version of a complex KPI with an inner join is different from a version of that KPI without the expression
        Assertions.assertEquals(4, kpiDefinitionDTOS.size());
        Assertions.assertEquals(3, kpiDefinitionDTOS.get(0).getAggregationElements().size());
        Assertions.assertEquals("AMF_Mobility_NetworkSlice_1.field1", kpiDefinitionDTOS.get(0).getAggregationElements().get(0));
        Assertions.assertEquals(2, kpiDefinitionDTOS.get(2).getAggregationElements().size());
        Assertions.assertEquals(List.of("AMF_Mobility_NetworkSlice_1.field1", "AMF_Mobility_NetworkSlice_1.field2"),
                kpiDefinitionDTOS.get(2).getAggregationElements());

    }

    @Test
    void calculateAffectedKPI_EmptyProfile() {
        final ProfileDefinition newProfile = VALID_PROFILE_DEF_OBJ.toBuilder().kpis(new ArrayList<>()).build();
        final List<KpiDefinitionDTOWithRelationship> kpiDefinitionDTOS = this.kpiCalculator.calculateAffectedKPIs(List.of(newProfile));

        Assertions.assertEquals(0, kpiDefinitionDTOS.size());
    }

    @Test
    void calculateAffectedKPI_SimpleKpiDTOMapping_valid() {
        this.pmDefinitionDAO.savePMDefinition(TestResourcesUtils.VALID_PM_DEF_OBJ, "NEW_SCHEMA");

        final ProfileDefinition newProfile = VALID_PROFILE_DEF_OBJ.toBuilder()
                .kpis(List.of(KPIReference.builder().ref(VALID_SIMPLE_KPI_DEF_NAME).build()))
                .build();

        final List<KpiDefinitionDTOWithRelationship> kpiDefinitionDTOS = this.kpiCalculator.calculateAffectedKPIs(singletonList(newProfile));
        Assertions.assertEquals(1, kpiDefinitionDTOS.size());

        final List<KpiDefinitionDTO> kpis = new ArrayList<>(kpiDefinitionDTOS);
        final KpiDefinitionDTO calculatedKpiDefDTO = kpis.get(0);

        assertEquals("MAX", calculatedKpiDefDTO.getAggregationType());
        assertEquals(KpiTypeEnum.SIMPLE, calculatedKpiDefDTO.getKpiType());

        assertEquals(15, calculatedKpiDefDTO.getAggregationPeriod());
        assertEquals(VALID_PM_DEF_SOURCE, calculatedKpiDefDTO.getInpDataIdentifier());
        assertEquals(List.of("NEW_SCHEMA.field1", "NEW_SCHEMA.field2"), calculatedKpiDefDTO.getAggregationElements());
        assertEquals("MAX(NEW_SCHEMA.pmdef_name)", calculatedKpiDefDTO.getExpression());

    }

    @Test
    void createComplexKpiDefinitionDto_SimpleInputMetricInSameProfile_valid() {

        final Map<String, KpiDefinitionDTO> inputSimpleKpiDefinitionDtoMap = new HashMap<>();
        inputSimpleKpiDefinitionDtoMap.put(VALID_SIMPLE_KPI_DEF_NAME, UPDATED_DEPLOYED_SIMPLE_KPI_OBJ);

        final KpiDefinitionDTO complexKpiDefinitionDto = this.kpiCalculator.createComplexKpiDefinitionDto(VALID_COMPLEX_KPI_DEF_OBJ,
                VALID_PROFILE_DEF_OBJ, inputSimpleKpiDefinitionDtoMap);

        Assertions.assertNotNull(complexKpiDefinitionDto);

    }

    @Test
    void createComplexKpiDefinitionDto_SimpleInputMetricNotInProfileButDeployed_valid() {

        this.deployedKpiDefDAO.createDeployedKpi(UPDATED_DEPLOYED_SIMPLE_KPI_OBJ, VALID_SIMPLE_KPI_DEF_NAME, VALID_PROFILE_DEF_OBJ);

        final ProfileDefinition updatedProfile = VALID_PROFILE_DEF_OBJ.toBuilder()
                .kpis(List.of(KPIReference.builder().ref(VALID_COMPLEX_KPI_DEF_NAME).build()))
                .build();

        final KpiDefinitionDTO complexKpiDefinitionDto = this.kpiCalculator.createComplexKpiDefinitionDto(VALID_COMPLEX_KPI_DEF_OBJ, updatedProfile,
                new HashMap<>());

        Assertions.assertNotNull(complexKpiDefinitionDto);

    }

    @Test
    void createComplexKpiDefinitionDto_SimpleInputMetricAbsent_ThrowsException() {
        Assertions.assertThrows(CsacValidationException.class,
                () -> this.kpiCalculator.createComplexKpiDefinitionDto(VALID_COMPLEX_KPI_DEF_OBJ, VALID_PROFILE_DEF_OBJ, new HashMap<>()));
    }

    @Test
    void validateAggregationPeriod_validValueSet_noException() {
        final Integer validAggregationPeriod = AggregationPeriod.FIFTEEN.getValue();
        this.kpiCalculator.setAggregationPeriod(validAggregationPeriod);
        Assertions.assertEquals(this.kpiCalculator.getAggregationPeriod(), validAggregationPeriod);
    }

    @Test
    void validateAggregationPeriod_invalidValueSet_exception() {
        final Integer invalidAggregationPeriod = 0;
        final var thrown = assertThrows(CsacValidationException.class,
                () -> this.kpiCalculator.setAggregationPeriod(invalidAggregationPeriod));

        Assertions.assertEquals(invalidAggregationPeriod + " is not a permitted value for the PMSC KPI aggregation period", thrown.getMessage());
    }

    @Test
    void createSimpleKPIDTO_withAugmentation() {
        this.pmDefinitionDAO.savePMDefinition(TestResourcesUtils.VALID_PM_DEF_OBJ, "NEW_SCHEMA");

        final ProfileDefinition newProfile = VALID_PROFILE_DEF_OBJ.toBuilder().augmentation("cardq")
                .kpis(List.of(KPIReference.builder().ref(VALID_SIMPLE_KPI_DEF_NAME).build()))
                .build();

        final List<KpiDefinitionDTOWithRelationship> kpiDefinitionDTOS = this.kpiCalculator.calculateAffectedKPIs(singletonList(newProfile));
        Assertions.assertEquals(1, kpiDefinitionDTOS.size());

        final List<KpiDefinitionDTO> kpis = new ArrayList<>(kpiDefinitionDTOS);
        final KpiDefinitionDTO calculatedKpiDefDTO = kpis.get(0);

        Assertions.assertNotNull(calculatedKpiDefDTO.getName());
        Assertions.assertNotNull(calculatedKpiDefDTO.getExpression());
        Assertions.assertNotNull(calculatedKpiDefDTO.getObjectType());
        Assertions.assertNotNull(calculatedKpiDefDTO.getAggregationElements());

        Assertions.assertEquals("MAX(cardq_AMF_Mobility_NetworkSlice_1.pmdef_name)", calculatedKpiDefDTO.getExpression());
        Assertions.assertEquals(DEPLOYED_SIMPLE_KPI_OBJ.getIsVisible(), calculatedKpiDefDTO.getIsVisible());
        Assertions.assertEquals(DEPLOYED_SIMPLE_KPI_OBJ.getInpDataCategory(), calculatedKpiDefDTO.getInpDataCategory());
        Assertions.assertEquals("5G|PM_COUNTERS|cardq_AMF_Mobility_NetworkSlice_1", calculatedKpiDefDTO.getInpDataIdentifier());
        Assertions.assertEquals(DEPLOYED_SIMPLE_KPI_OBJ.getAggregationPeriod(), calculatedKpiDefDTO.getAggregationPeriod());
        Assertions.assertEquals(List.of("cardq_AMF_Mobility_NetworkSlice_1.field1", "cardq_AMF_Mobility_NetworkSlice_1.field2"),
                calculatedKpiDefDTO.getAggregationElements());

        Assertions.assertNull(calculatedKpiDefDTO.getExecutionGroup());

    }

    @Test
    void calculateAffectedKPIs_simpleKpiGotOverrideInAnotherProfile_shouldRecalculate() throws Exception {

        final List<String> overrideContext = new ArrayList<>(VALID_PROFILE_DEF_AGGREGATION_FIELD_LIST);
        overrideContext.add("field3");

        final ProfileDefinition newProfile = ProfileDefinition.builder().name("profile").description("test profile")
                .context(List.of(VALID_PROFILE_DEF_AGGREGATION_FIELD, "field3"))
                .kpis(List.of(KPIReference.builder().ref(VALID_COMPLEX_KPI_DEF_NAME).inputMetricOverrides(
                        List.of(InputMetricOverride.builder().id(VALID_SIMPLE_KPI_DEF_NAME)
                                .context(overrideContext).build())).build()))
                .build();

        final List<KpiDefinitionDTOWithRelationship> kpiDefinitionDTOS = this.kpiCalculator.calculateAffectedKPIs(
                List.of(VALID_PROFILE_DEF_OBJ, newProfile));

        // complex_field1_field2, simple_field1_field2
        // complex_field1_field2 (override), simple_field1_field3
        Assertions.assertEquals(4, kpiDefinitionDTOS.size());

        // there should be 2 each simple and complex KPIs
        final long expectedComplex = kpiDefinitionDTOS.stream().filter(k -> k.getKpiType() == KpiTypeEnum.COMPLEX).count();
        final long expectedSimple = kpiDefinitionDTOS.stream().filter(k -> k.getKpiType() == KpiTypeEnum.SIMPLE).count();

        assertEquals(2, expectedComplex);
        assertEquals(2, expectedSimple);
    }

    @Test
    void calculateAffectedKPIs_twoSameProfileContainComplexKpisWithInputMetricsOverride_shouldOnlyCalculateOnce() {

        final ProfileDefinition newProfile = ProfileDefinition.builder().name("profile").description("test profile")
                .context(List.of(VALID_PROFILE_DEF_AGGREGATION_FIELD))
                .kpis(List.of(KPIReference.builder().ref(VALID_COMPLEX_KPI_DEF_NAME).inputMetricOverrides(
                        List.of(InputMetricOverride.builder().id(VALID_SIMPLE_KPI_DEF_NAME).context(VALID_PROFILE_DEF_AGGREGATION_FIELD_LIST)
                                .build())).build()))
                .build();

        final ProfileDefinition profileDup = ProfileDefinition.builder().name("profile_duplicate").description("test profile")
                .context(List.of(VALID_PROFILE_DEF_AGGREGATION_FIELD))
                .kpis(List.of(KPIReference.builder().ref(VALID_COMPLEX_KPI_DEF_NAME).inputMetricOverrides(
                        List.of(InputMetricOverride.builder().id(VALID_SIMPLE_KPI_DEF_NAME).context(VALID_PROFILE_DEF_AGGREGATION_FIELD_LIST)
                                .build())).build()))
                .build();

        final List<KpiDefinitionDTOWithRelationship> kpiDefinitionDTOS = this.kpiCalculator.calculateAffectedKPIs(
                List.of(newProfile, profileDup));

        // complex_field1_field2, simple_field1
        Assertions.assertEquals(2, kpiDefinitionDTOS.size());

        // there should be 1 each of simple and complex KPIs
        final long actualSimple = kpiDefinitionDTOS.stream().filter(k -> k.getKpiType() == KpiTypeEnum.SIMPLE).count();
        final long actualComplex = kpiDefinitionDTOS.stream().filter(k -> k.getKpiType() == KpiTypeEnum.COMPLEX).count();

        assertEquals(1, actualComplex);
        assertEquals(1, actualSimple);
    }

    @Test
    void retrieveFactTableName_noInputMetricOverride_fromCommonTable() {
        final Map<String, KpiDefinitionDTO> inputSimpleKpiDefinitionDtoMap = new HashMap<>();
        inputSimpleKpiDefinitionDtoMap.put("kpi_A", SIMPLE_KPI_DTO_A);

        final String tableName = this.kpiCalculator.retrieveFactTableName(List.of(), inputSimpleKpiDefinitionDtoMap);

        assertEquals(SIMPLE_KPI_DTO_A.getFactTableName(), tableName);
    }

    @Test
    void retrieveFactTableName_inputMetricOverrideNull_fromCommonTable() {
        final Map<String, KpiDefinitionDTO> inputSimpleKpiDefinitionDtoMap = new HashMap<>();
        inputSimpleKpiDefinitionDtoMap.put("kpi_A", SIMPLE_KPI_DTO_A);

        final String tableName = this.kpiCalculator.retrieveFactTableName(null, inputSimpleKpiDefinitionDtoMap);

        assertEquals(SIMPLE_KPI_DTO_A.getFactTableName(), tableName);
    }

    @Test
    void retrieveFactTableName_allInputMetricsGotOverride_fromOverrideTable() {
        final Map<String, KpiDefinitionDTO> inputSimpleKpiDefinitionDtoMap = new HashMap<>();
        inputSimpleKpiDefinitionDtoMap.put("kpi_A", SIMPLE_KPI_DTO_A);
        inputSimpleKpiDefinitionDtoMap.put("kpi_B", SIMPLE_KPI_DTO_B);

        final String tableName = this.kpiCalculator.retrieveFactTableName(
                List.of(InputMetricOverride.builder().id("kpi_A").context(List.of(VALID_PROFILE_DEF_AGGREGATION_FIELD)).build(),
                        InputMetricOverride.builder().id("kpi_B").context(List.of(VALID_PROFILE_DEF_AGGREGATION_FIELD)).build()),
                inputSimpleKpiDefinitionDtoMap);

        assertEquals(SIMPLE_KPI_DTO_A.getFactTableName(), tableName);
    }

    @Test
    void retrieveFactTableName_oneInputMetricOverride_fromNoneOverrideTable() {
        final Map<String, KpiDefinitionDTO> inputSimpleKpiDefinitionDtoMap = new HashMap<>();
        inputSimpleKpiDefinitionDtoMap.put("kpi_A", SIMPLE_KPI_DTO_A);
        inputSimpleKpiDefinitionDtoMap.put("kpi_B", SIMPLE_KPI_DTO_B);

        final String tableName = this.kpiCalculator.retrieveFactTableName(
                List.of(InputMetricOverride.builder().id("kpi_A").context(List.of(VALID_PROFILE_DEF_AGGREGATION_FIELD)).build()),
                inputSimpleKpiDefinitionDtoMap);

        assertEquals(SIMPLE_KPI_DTO_B.getFactTableName(), tableName);
    }

}
