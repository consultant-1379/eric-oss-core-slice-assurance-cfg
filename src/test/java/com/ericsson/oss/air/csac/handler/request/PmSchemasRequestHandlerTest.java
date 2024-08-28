/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.handler.request;

import static com.ericsson.oss.air.csac.model.TestResourcesUtils.DEPLOYED_COMPLEX_KPI_OBJ;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import com.ericsson.oss.air.api.model.RtPmSchemaInfoDto;
import com.ericsson.oss.air.api.model.RtPmSchemaInfoListDto;
import com.ericsson.oss.air.csac.configuration.schema.InputSchemaProvider;
import com.ericsson.oss.air.csac.model.AugmentationDefinition;
import com.ericsson.oss.air.csac.model.PMDefinition;
import com.ericsson.oss.air.csac.model.pmsc.KpiDefinitionDTO;
import com.ericsson.oss.air.csac.model.pmsc.KpiTypeEnum;
import com.ericsson.oss.air.csac.model.pmschema.PMSchemaDTO;
import com.ericsson.oss.air.csac.model.pmschema.PmSchema;
import com.ericsson.oss.air.csac.repository.DeployedKpiDefDAO;
import com.ericsson.oss.air.csac.repository.EffectiveAugmentationDAO;
import com.ericsson.oss.air.csac.repository.PMDefinitionDAO;
import com.ericsson.oss.air.csac.service.augmentation.AugmentationProvisioningService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.ListUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PmSchemasRequestHandlerTest {

    // Schema References
    private static final String ARDQ_NAME1 = "SliceOwner1";

    private static final String ARDQ_NAME2 = "SliceOwner2";

    private static final String SCHEMA_REF_A = "5G|PM_COUNTERS|AMF_Mobility_NetworkSlice_1";

    private static final String SCHEMA_REF_B = "5G|PM_COUNTERS|up_payload_dnn_slice_1";

    private static final String AUGMENTED_SCHEMA_REF_A = "5G|PM_COUNTERS|" + ARDQ_NAME1 + "_AMF_Mobility_NetworkSlice_1";

    private static final String AUGMENTED_SCHEMA_REF_B = "5G|PM_COUNTERS|" + ARDQ_NAME2 + "_up_payload_dnn_slice_1";


    // Kafka Topic
    private static final String KAFKA_TOPIC = "eric-oss-3gpp-pm-xml-core-parser-";

    // PM Definition names
    private static final String PM_DEF_NAME_A = "pmCounters.VS_NS_NbrRegisteredSub_5GS";

    private static final String PM_DEF_NAME_B1 = "pmCounters.ul_ipv4_received_bytes";

    private static final String PM_DEF_NAME_B2 = "pmCounters.ul_ipv6_received_bytes";

    private static final String PM_DEF_NAME_B3 = "pmCounters.ul_unstr_received_bytes";


    // PM Definitions
    private static final PMDefinition PM_DEF_A = PMDefinition.builder().name(PM_DEF_NAME_A).source(SCHEMA_REF_A).build();

    private static final PMDefinition PM_DEF_B1 = PMDefinition.builder().name(PM_DEF_NAME_B1).source(SCHEMA_REF_B).build();

    private static final PMDefinition PM_DEF_B2 = PMDefinition.builder().name(PM_DEF_NAME_B2).source(SCHEMA_REF_B).build();

    private static final PMDefinition PM_DEF_B3 = PMDefinition.builder().name(PM_DEF_NAME_B3).source(SCHEMA_REF_B).build();

    private static final List<PMDefinition> PM_DEFINITION_LIST = List.of(PM_DEF_A, PM_DEF_B1, PM_DEF_B2, PM_DEF_B3);


    // Contexts
    private static final String SNASSI_CONTEXT = "snassi";

    private static final String NODEFDN_CONTEXT ="nodeFDN";

    private static final String SITE_CONTEXT = "site";

    private static final String NSSI_CONTEXT = "nssi";

    private static final String TEST_SCHEMA_NAME = "schema_Name.Test.";

    private static final String QUALIFIED_SNASSI_CONTEXT = TEST_SCHEMA_NAME + SNASSI_CONTEXT;

    private static final String QUALIFIED_NODEFDN_CONTEXT = TEST_SCHEMA_NAME + NODEFDN_CONTEXT;

    private static final String QUALIFIED_SITE_CONTEXT = TEST_SCHEMA_NAME + SITE_CONTEXT;

    private static final String QUALIFIED_NSSI_CONTEXT = TEST_SCHEMA_NAME + NSSI_CONTEXT;

    private static final List<String> CONTEXT_LIST = List.of(SITE_CONTEXT, NSSI_CONTEXT);

    private static final List<String> QUALIFIED_CONTEXT_LIST = List.of(QUALIFIED_SITE_CONTEXT, QUALIFIED_NSSI_CONTEXT);


    // Unaugmented simple RT KPIs
    private static final KpiDefinitionDTO UNAUGMENTED_SIMPLE_RT_KPI1 = KpiDefinitionDTO.builder()
            .withKpiType(KpiTypeEnum.SIMPLE)
            .withInpDataIdentifier(SCHEMA_REF_A)
            .withAggregationElements(List.of(QUALIFIED_SNASSI_CONTEXT))
            .build();

    private static final KpiDefinitionDTO UNAUGMENTED_SIMPLE_RT_KPI2 = KpiDefinitionDTO.builder()
            .withKpiType(KpiTypeEnum.SIMPLE)
            .withInpDataIdentifier(SCHEMA_REF_A)
            .withAggregationElements(List.of(QUALIFIED_NODEFDN_CONTEXT))
            .build();

    private static final KpiDefinitionDTO UNAUGMENTED_SIMPLE_RT_KPI3 = KpiDefinitionDTO.builder()
            .withKpiType(KpiTypeEnum.SIMPLE)
            .withInpDataIdentifier(SCHEMA_REF_B)
            .withAggregationElements(List.of(QUALIFIED_SNASSI_CONTEXT))
            .build();

    // Augmented simple RT KPIs
    private static final KpiDefinitionDTO AUGMENTED_SIMPLE_RT_KPI1 = KpiDefinitionDTO.builder()
            .withKpiType(KpiTypeEnum.SIMPLE)
            .withInpDataIdentifier(AUGMENTED_SCHEMA_REF_A)
            .withAggregationElements(QUALIFIED_CONTEXT_LIST)
            .build();

    private static final KpiDefinitionDTO AUGMENTED_SIMPLE_RT_KPI2 = KpiDefinitionDTO.builder()
            .withName("Augmented_PM_DEF_B1")
            .withKpiType(KpiTypeEnum.SIMPLE)
            .withInpDataIdentifier(AUGMENTED_SCHEMA_REF_B)
            .withAggregationElements(List.of(QUALIFIED_SITE_CONTEXT))
            .build();

    private static final KpiDefinitionDTO AUGMENTED_SIMPLE_RT_KPI3 = KpiDefinitionDTO.builder()
            .withName("Augmented_PM_DEF_B2")
            .withKpiType(KpiTypeEnum.SIMPLE)
            .withInpDataIdentifier(AUGMENTED_SCHEMA_REF_B)
            .withAggregationElements(List.of(QUALIFIED_SITE_CONTEXT))
            .build();

    private static final KpiDefinitionDTO AUGMENTED_SIMPLE_RT_KPI4 = KpiDefinitionDTO.builder()
            .withName("Augmented_PM_DEF_B3")
            .withKpiType(KpiTypeEnum.SIMPLE)
            .withInpDataIdentifier(AUGMENTED_SCHEMA_REF_B)
            .withAggregationElements(List.of(QUALIFIED_SITE_CONTEXT))
            .build();

    // Augmentation Definitions
    private static final String ARDQ_URL ="testUrl";

    private static final AugmentationDefinition AUG_DEF1 = new AugmentationDefinition().withUrl(ARDQ_URL).withName(ARDQ_NAME1);

    private static final AugmentationDefinition AUG_DEF2 = new AugmentationDefinition().withUrl(ARDQ_URL).withName(ARDQ_NAME2);


    //Augmentation Schema Mappings
    private static final Map<String, String> AUG_SCHEMA_MAPPING1 = Map.of(SCHEMA_REF_A, AUGMENTED_SCHEMA_REF_A);
    
    private static final Map<String, String> AUG_SCHEMA_MAPPING2 = Map.of(SCHEMA_REF_B, AUGMENTED_SCHEMA_REF_B);


    // PM Schema
    private static final PmSchema PMSCHEMA = new PmSchema(KAFKA_TOPIC, PMSchemaDTO.builder().build());

    @Mock
    private InputSchemaProvider inputSchemaProvider;

    @Mock
    private DeployedKpiDefDAO deployedKpiDefDAO;

    @Mock
    private PMDefinitionDAO pmDefinitionDAO;

    @Mock
    private EffectiveAugmentationDAO effectiveAugmentationDAO;

    @Mock
    private AugmentationProvisioningService augmentationService;

    @InjectMocks
    private PmSchemasRequestHandler pmSchemasRequestHandler;


    @Test
    void getPmDefinitionIndex() {

        when(this.pmDefinitionDAO.findAllPMDefinitions(0, Integer.MAX_VALUE)).thenReturn(PM_DEFINITION_LIST);

        final Map<String, List<String>> expectedPmDefIndex = new HashMap<>();
        expectedPmDefIndex.put(SCHEMA_REF_A, List.of(PM_DEF_NAME_A));
        expectedPmDefIndex.put(SCHEMA_REF_B, List.of(PM_DEF_NAME_B1, PM_DEF_NAME_B2, PM_DEF_NAME_B3));

        final Map<String, List<String>> actualPmDefIndex = this.pmSchemasRequestHandler.createPmDefinitionIndex();

        assertEquals(expectedPmDefIndex.size(), actualPmDefIndex.size());
        assertTrue(CollectionUtils.isEqualCollection(expectedPmDefIndex.get(SCHEMA_REF_A), actualPmDefIndex.get(SCHEMA_REF_A)));
        assertTrue(CollectionUtils.isEqualCollection(expectedPmDefIndex.get(SCHEMA_REF_B), actualPmDefIndex.get(SCHEMA_REF_B)));

    }

    @Test
    void createAugMappingIndex_SingleAugmentation() {

        final Map<String, String> origMap = new HashMap<>();
        origMap.put(SCHEMA_REF_A, AUGMENTED_SCHEMA_REF_A);

        when(this.augmentationService.getSchemaMappings(AUG_DEF1.getName())).thenReturn(origMap);

        final Map<String, String> expectedAugMappingIndex = new HashMap<>();
        expectedAugMappingIndex.put(AUGMENTED_SCHEMA_REF_A, SCHEMA_REF_A);

        final Map<String, String> actualIndex = this.pmSchemasRequestHandler.createAugMappingIndex(List.of(AUG_DEF1));

        assertEquals(expectedAugMappingIndex.size(), actualIndex.size());
        assertEquals(expectedAugMappingIndex.get(AUGMENTED_SCHEMA_REF_A), actualIndex.get(AUGMENTED_SCHEMA_REF_A));
    }

    @Test
    void createAugMappingIndex_MultipleAugmentations_IncludesMultipleForOneArdqId() {

        final String augmentedSchemaRef = "5G|PM_COUNTERS|" + ARDQ_NAME1 + "_up_payload_dnn_slice_1";
        final Map<String, String> origMap = new HashMap<>();
        origMap.put(SCHEMA_REF_A, AUGMENTED_SCHEMA_REF_A);
        origMap.put(SCHEMA_REF_B, augmentedSchemaRef);

        when(this.augmentationService.getSchemaMappings(AUG_DEF1.getName())).thenReturn(origMap);
        when(this.augmentationService.getSchemaMappings(AUG_DEF2.getName())).thenReturn(AUG_SCHEMA_MAPPING2);

        final Map<String, String> expectedAugMappingIndex = new HashMap<>();
        expectedAugMappingIndex.put(AUGMENTED_SCHEMA_REF_A, SCHEMA_REF_A);
        expectedAugMappingIndex.put(AUGMENTED_SCHEMA_REF_B, SCHEMA_REF_B);
        expectedAugMappingIndex.put(augmentedSchemaRef, SCHEMA_REF_B);

        final Map<String, String> actualIndex = this.pmSchemasRequestHandler.createAugMappingIndex(List.of(AUG_DEF1, AUG_DEF2));

        assertEquals(expectedAugMappingIndex.size(), actualIndex.size());
        assertEquals(expectedAugMappingIndex.get(AUGMENTED_SCHEMA_REF_A), actualIndex.get(AUGMENTED_SCHEMA_REF_A));
        assertEquals(expectedAugMappingIndex.get(augmentedSchemaRef), actualIndex.get(augmentedSchemaRef));
        assertEquals(expectedAugMappingIndex.get(AUGMENTED_SCHEMA_REF_B), actualIndex.get(AUGMENTED_SCHEMA_REF_B));

    }

    @Test
    void createRtPmSchemaInfoDto_NoAugmentations() {

        final Map<String, List<String>> pmDefinitionIndex = Map.of(SCHEMA_REF_A, List.of(PM_DEF_NAME_A));
        when(this.inputSchemaProvider.getSchema(anyString())).thenReturn(PMSCHEMA);

        final RtPmSchemaInfoDto expectedSchemaDto = new RtPmSchemaInfoDto()
                .schemaRef(UNAUGMENTED_SIMPLE_RT_KPI1.getInpDataIdentifier())
                .schemaTopic(PMSCHEMA.kafkaTopic())
                .augmented(Boolean.FALSE)
                .pmdefs(List.of(PM_DEF_NAME_A))
                .contexts(List.of(SNASSI_CONTEXT));

        final RtPmSchemaInfoDto actualSchemaDto = this.pmSchemasRequestHandler.createRtPmSchemaInfoDto(UNAUGMENTED_SIMPLE_RT_KPI1, UNAUGMENTED_SIMPLE_RT_KPI1.getInpDataIdentifier(), pmDefinitionIndex, new HashMap<>());

        assertEquals(expectedSchemaDto, actualSchemaDto);
    }

    @Test
    void createRtPmSchemaInfoDto_WithAugmentation() {

        final Map<String, List<String>> pmDefinitionIndex = Map.of(SCHEMA_REF_B, List.of(PM_DEF_NAME_B1, PM_DEF_NAME_B2, PM_DEF_NAME_B3));
        final Map<String, String> augMappingIndex = Map.of(AUGMENTED_SCHEMA_REF_B, SCHEMA_REF_B);
        when(this.inputSchemaProvider.getSchema(anyString())).thenReturn(PMSCHEMA);

        final RtPmSchemaInfoDto expectedSchemaDto = new RtPmSchemaInfoDto()
                .schemaRef(AUGMENTED_SCHEMA_REF_B)
                .schemaTopic(PMSCHEMA.kafkaTopic())
                .addContextsItem(SITE_CONTEXT)
                .augmented(true)
                .addPmdefsItem(PM_DEF_NAME_B1)
                .addPmdefsItem(PM_DEF_NAME_B2)
                .addPmdefsItem(PM_DEF_NAME_B3);

        final RtPmSchemaInfoDto actualSchemaDto = this.pmSchemasRequestHandler.createRtPmSchemaInfoDto(AUGMENTED_SIMPLE_RT_KPI2, AUGMENTED_SIMPLE_RT_KPI2.getInpDataIdentifier(), pmDefinitionIndex, augMappingIndex);

        assertEquals(expectedSchemaDto, actualSchemaDto);

    }

    @Test
    void getPmSchemas_NoDeployedKpis() {

        when(this.deployedKpiDefDAO.getAllDeployedKpis()).thenReturn(ListUtils.EMPTY_LIST);

        final RtPmSchemaInfoListDto pmSchemaInfoList = this.pmSchemasRequestHandler.getPmSchemas();

        assertNotNull(pmSchemaInfoList);
        assertTrue(pmSchemaInfoList.getPmschemas().isEmpty());
    }

    @Test
    void getPmSchemas_NoSimpleDeployedKpis() {

        /* NOTE: this scenario is not realistic because there should be no complex deployed KPIs if there are no
         * simple deployed KPIs. This test is to verify that the complex deployed KPIs are ignored. It is simpler to execute
         * this test if there are no simple deployed KPIs and at least one complex deployed KPI.
         */
        when(this.deployedKpiDefDAO.getAllDeployedKpis()).thenReturn(List.of(DEPLOYED_COMPLEX_KPI_OBJ));

        final RtPmSchemaInfoListDto pmSchemaInfoList = this.pmSchemasRequestHandler.getPmSchemas();

        assertNotNull(pmSchemaInfoList);
        assertTrue(pmSchemaInfoList.getPmschemas().isEmpty());
    }

    @Test
    void getPmSchemas_DeployedSimpleKpis_NoAugmentation() {

        when(this.deployedKpiDefDAO.getAllDeployedKpis()).thenReturn(List.of(UNAUGMENTED_SIMPLE_RT_KPI1, UNAUGMENTED_SIMPLE_RT_KPI2, UNAUGMENTED_SIMPLE_RT_KPI3));
        when(this.inputSchemaProvider.getSchema(anyString())).thenReturn(PMSCHEMA);
        when(this.pmDefinitionDAO.findAllPMDefinitions(0, Integer.MAX_VALUE)).thenReturn(PM_DEFINITION_LIST);
        when(this.effectiveAugmentationDAO.findAll()).thenReturn(ListUtils.EMPTY_LIST);

        final RtPmSchemaInfoDto schemaInfoDto1 = new RtPmSchemaInfoDto()
                .schemaRef(SCHEMA_REF_A)
                .schemaTopic(KAFKA_TOPIC)
                .addContextsItem(NODEFDN_CONTEXT)
                .addContextsItem(SNASSI_CONTEXT)
                .augmented(false)
                .addPmdefsItem(PM_DEF_NAME_A);
        final RtPmSchemaInfoDto schemaInfoDto2 = new RtPmSchemaInfoDto()
                .schemaRef(SCHEMA_REF_B)
                .schemaTopic(KAFKA_TOPIC)
                .addContextsItem(SNASSI_CONTEXT)
                .augmented(false)
                .addPmdefsItem(PM_DEF_NAME_B1)
                .addPmdefsItem(PM_DEF_NAME_B2)
                .addPmdefsItem(PM_DEF_NAME_B3);
        final RtPmSchemaInfoListDto expectedPmSchemaInfoList = new RtPmSchemaInfoListDto().pmschemas(Arrays.asList(schemaInfoDto2, schemaInfoDto1));

        final RtPmSchemaInfoListDto actualPmSchemaInfoList = this.pmSchemasRequestHandler.getPmSchemas();

        assertNotNull(actualPmSchemaInfoList);
        assertEqualsPmSchemaInfoList(expectedPmSchemaInfoList, actualPmSchemaInfoList);
    }

    @Test
    void getPmSchemas_DeployedSimpleKpis_WithAugmentation() {

        when(this.deployedKpiDefDAO.getAllDeployedKpis()).thenReturn(List.of(UNAUGMENTED_SIMPLE_RT_KPI1, UNAUGMENTED_SIMPLE_RT_KPI2, UNAUGMENTED_SIMPLE_RT_KPI3,
                AUGMENTED_SIMPLE_RT_KPI1, AUGMENTED_SIMPLE_RT_KPI2, AUGMENTED_SIMPLE_RT_KPI3, AUGMENTED_SIMPLE_RT_KPI4));
        when(this.inputSchemaProvider.getSchema(anyString())).thenReturn(PMSCHEMA);
        when(this.pmDefinitionDAO.findAllPMDefinitions(0, Integer.MAX_VALUE)).thenReturn(PM_DEFINITION_LIST);
        when(this.effectiveAugmentationDAO.findAll()).thenReturn(List.of(AUG_DEF1, AUG_DEF2));
        when(this.augmentationService.getSchemaMappings(AUG_DEF1.getName())).thenReturn(AUG_SCHEMA_MAPPING1);
        when(this.augmentationService.getSchemaMappings(AUG_DEF2.getName())).thenReturn(AUG_SCHEMA_MAPPING2);

        final RtPmSchemaInfoDto schemaInfoDto1 = new RtPmSchemaInfoDto()
                .schemaRef(SCHEMA_REF_A)
                .schemaTopic(KAFKA_TOPIC)
                .addContextsItem(SNASSI_CONTEXT)
                .addContextsItem(NODEFDN_CONTEXT)
                .augmented(false)
                .addPmdefsItem(PM_DEF_NAME_A);
        final RtPmSchemaInfoDto schemaInfoDto2 = new RtPmSchemaInfoDto()
                .schemaRef(SCHEMA_REF_B)
                .schemaTopic(KAFKA_TOPIC)
                .addContextsItem(SNASSI_CONTEXT)
                .augmented(false)
                .addPmdefsItem(PM_DEF_NAME_B1)
                .addPmdefsItem(PM_DEF_NAME_B2)
                .addPmdefsItem(PM_DEF_NAME_B3);
        final RtPmSchemaInfoDto schemaInfoDto3 = new RtPmSchemaInfoDto()
                .schemaRef(AUGMENTED_SCHEMA_REF_A)
                .schemaTopic(KAFKA_TOPIC)
                .contexts(CONTEXT_LIST)
                .augmented(true)
                .addPmdefsItem(PM_DEF_NAME_A);
        final RtPmSchemaInfoDto schemaInfoDto4 = new RtPmSchemaInfoDto()
                .schemaRef(AUGMENTED_SCHEMA_REF_B)
                .schemaTopic(KAFKA_TOPIC)
                .addContextsItem(SITE_CONTEXT)
                .augmented(true)
                .addPmdefsItem(PM_DEF_NAME_B1)
                .addPmdefsItem(PM_DEF_NAME_B2)
                .addPmdefsItem(PM_DEF_NAME_B3);
        final RtPmSchemaInfoListDto expectedPmSchemaInfoList = new RtPmSchemaInfoListDto().pmschemas(Arrays.asList(schemaInfoDto1, schemaInfoDto2, schemaInfoDto3, schemaInfoDto4));

        final RtPmSchemaInfoListDto actualPmSchemaInfoList = this.pmSchemasRequestHandler.getPmSchemas();

        assertNotNull(actualPmSchemaInfoList);
        assertEqualsPmSchemaInfoList(expectedPmSchemaInfoList, actualPmSchemaInfoList);
    }

    private void assertEqualsPmSchemaInfoList(final RtPmSchemaInfoListDto expectedPmSchemaInfoList, final RtPmSchemaInfoListDto actualPmSchemaInfoList) {

        final List<RtPmSchemaInfoDto> expectedPmSchemas = expectedPmSchemaInfoList.getPmschemas();
        final List<RtPmSchemaInfoDto> actualPmSchemas = new ArrayList<>(actualPmSchemaInfoList.getPmschemas());

        assertEquals(expectedPmSchemas.size(), actualPmSchemas.size());

        final Comparator<RtPmSchemaInfoDto> comparator = Comparator.comparing(RtPmSchemaInfoDto::getSchemaRef);
        Collections.sort(expectedPmSchemas, comparator);
        Collections.sort(actualPmSchemas, comparator);

        IntStream.range(0, expectedPmSchemas.size())
                .forEach(idx -> {
                    final RtPmSchemaInfoDto expectedPmSchema = expectedPmSchemas.get(idx);
                    final RtPmSchemaInfoDto actualPmSchema = actualPmSchemas.get(idx);

                    assertEquals(expectedPmSchema.getSchemaRef(), actualPmSchema.getSchemaRef());
                    assertEquals(expectedPmSchema.getSchemaTopic(), actualPmSchema.getSchemaTopic());
                    assertEquals(expectedPmSchema.getAugmented(), actualPmSchema.getAugmented());
                    assertTrue(CollectionUtils.isEqualCollection(expectedPmSchema.getPmdefs(), actualPmSchema.getPmdefs()));
                    assertTrue(CollectionUtils.isEqualCollection(expectedPmSchema.getContexts(), actualPmSchema.getContexts()));});

    }
}