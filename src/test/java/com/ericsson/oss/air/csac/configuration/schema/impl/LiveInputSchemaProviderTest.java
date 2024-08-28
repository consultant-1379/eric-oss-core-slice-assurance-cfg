/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.configuration.schema.impl;

import static com.ericsson.oss.air.csac.model.TestResourcesUtils.AUGMENTED_PROFILE_DEF_OBJ;
import static com.ericsson.oss.air.csac.model.TestResourcesUtils.VALID_PM_DEF_OBJ;
import static com.ericsson.oss.air.csac.model.TestResourcesUtils.VALID_PM_DEF_SOURCE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.ericsson.oss.air.csac.model.AugmentationDefinition;
import com.ericsson.oss.air.csac.model.PMDefinition;
import com.ericsson.oss.air.csac.model.ProfileDefinition;
import com.ericsson.oss.air.csac.model.datacatalog.MessageDataTopicDto;
import com.ericsson.oss.air.csac.model.datacatalog.MessageSchemaDTO;
import com.ericsson.oss.air.csac.model.pmschema.PMSchemaDTO;
import com.ericsson.oss.air.csac.repository.AugmentationDefinitionDAO;
import com.ericsson.oss.air.csac.service.DataCatalogService;
import com.ericsson.oss.air.csac.service.SchemaRegistryService;
import com.ericsson.oss.air.csac.service.augmentation.AugmentationProvisioningService;
import com.ericsson.oss.air.exception.CsacValidationException;
import com.ericsson.oss.air.util.logging.FaultHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class LiveInputSchemaProviderTest {

    private static final String KAFKA_TOPIC = "test-topic";

    @Mock
    private DataCatalogService dataCatalogService;

    @Mock
    private SchemaRegistryService schemaRegistryService;

    @Mock
    private AugmentationDefinitionDAO augmentationDefinitionDAO;

    @Mock
    private AugmentationProvisioningService augmentationService;

    @InjectMocks
    private LiveInputSchemaProvider inputSchemaProvider;

    private final FaultHandler faultHandler = new FaultHandler();

    @BeforeEach
    void setUp() {
        this.inputSchemaProvider = new LiveInputSchemaProvider(dataCatalogService, schemaRegistryService, faultHandler, augmentationDefinitionDAO,
                augmentationService);
    }

    @Test
    void filteredSet_newSetContainsExistingSetValue() {
        final Set<String> newSet = new HashSet<>();
        newSet.add("ref1");
        newSet.add("ref2");
        newSet.add("ref3");
        final Set<String> cachedSet = Set.of("ref1");
        assertEquals(Set.of("ref2", "ref3"), this.inputSchemaProvider.filteredSet(newSet, cachedSet));
    }

    @Test
    void fetchSubject_invalidSubjectFormat() {
        assertThrows(CsacValidationException.class, () -> this.inputSchemaProvider.fetchSubject("invalidSpecRef"));

    }

    @Test
    void getSchema_alreadyCached() {
        final Set<String> schemaReferenceSet = new HashSet<>();
        schemaReferenceSet.add("5G|PM_COUNTERS|schemaA");
        schemaReferenceSet.add("5G|PM_COUNTERS|schemaB");
        schemaReferenceSet.add("5G|PM_COUNTERS|schemaC");
        final Map<String, MessageSchemaDTO> messageSchemasResponse = new HashMap<>();
        final MessageDataTopicDto messageDataTopicDto = MessageDataTopicDto.builder().name(KAFKA_TOPIC).build();
        messageSchemasResponse.put("5G|PM_COUNTERS|schemaA",
                MessageSchemaDTO.builder().messageDataTopic(messageDataTopicDto).specificationReference("5G_PM_COUNTERS_schemaA/1").build());
        messageSchemasResponse.put("5G|PM_COUNTERS|schemaB",
                MessageSchemaDTO.builder().messageDataTopic(messageDataTopicDto).specificationReference("5G_PM_COUNTERS_schemaB/1").build());
        messageSchemasResponse.put("5G|PM_COUNTERS|schemaC",
                MessageSchemaDTO.builder().messageDataTopic(messageDataTopicDto).specificationReference("5G_PM_COUNTERS_schemaC/1").build());
        when(this.dataCatalogService.getMessageSchemas(schemaReferenceSet)).thenReturn(messageSchemasResponse);
        final PMSchemaDTO pmSchemaDTO = PMSchemaDTO.builder().name("schema").fieldPaths(List.of("schema")).build();
        when(this.schemaRegistryService.getSchemaLatest(anyString())).thenReturn(pmSchemaDTO);
        this.inputSchemaProvider.prefetchInputSchemas(schemaReferenceSet);
        verify(this.dataCatalogService, times(1)).getMessageSchemas(anySet());
        verify(this.schemaRegistryService, times(3)).getSchemaLatest(anyString());

        assertEquals("schema", this.inputSchemaProvider.getSchema("5G|PM_COUNTERS|schemaA").getName());
        verify(this.dataCatalogService, times(0)).getMessageSchema(anyString());

    }

    @Test
    void getSchema_newSchemaRef() {
        final MessageDataTopicDto messageDataTopicDto = MessageDataTopicDto.builder().name(KAFKA_TOPIC).build();
        when(this.dataCatalogService.getMessageSchema("5G|PM_COUNTERS|schemaX")).thenReturn(
                MessageSchemaDTO.builder().messageDataTopic(messageDataTopicDto).specificationReference("5G_PM_COUNTERS_schemaX/1").build());
        when(this.schemaRegistryService.getSchemaLatest("5G_PM_COUNTERS_schemaX")).thenReturn(
                PMSchemaDTO.builder().name("schemaX").fieldPaths(List.of("field")).build());
        assertEquals("schemaX", this.inputSchemaProvider.getSchema("5G|PM_COUNTERS|schemaX").getName());
        verify(this.dataCatalogService, times(1)).getMessageSchema(anyString());
        // prefetchInputSchemas 3 times, plus 1 for new schema reference retrieval
        verify(this.schemaRegistryService, times(1)).getSchemaLatest(anyString());

    }

    @Test
    void getSchema_newSchemaRef_noMessageSchemaMatchFromDC_throwException() {
        when(this.dataCatalogService.getMessageSchema("5G|PM_COUNTERS|schemaX")).thenReturn(null);

        assertThrows(CsacValidationException.class, () -> this.inputSchemaProvider.getSchema("5G|PM_COUNTERS|schemaX"));
        verify(this.dataCatalogService, times(1)).getMessageSchema(anyString());
    }

    @Test
    void getSchema_newSchemaRef_DCRuntimeException() {
        when(this.dataCatalogService.getMessageSchema("5G|PM_COUNTERS|schemaX")).thenThrow(RuntimeException.class);

        assertThrows(RuntimeException.class, () -> this.inputSchemaProvider.getSchema("5G|PM_COUNTERS|schemaX"));
        verify(this.dataCatalogService, times(1)).getMessageSchema(anyString());
    }

    @Test
    void getSchemaReference_profileNotAugmented_parseSchemaReturned() {
        final ProfileDefinition profileDefinition = ProfileDefinition.builder().name("profile").context(List.of("field")).build();
        final PMDefinition pmDefinition = PMDefinition.builder().name("pm_def").source("G5|PM_COUNTERS|schema").build();
        assertEquals("G5|PM_COUNTERS|schema", this.inputSchemaProvider.getSchemaReference(profileDefinition, pmDefinition));
    }

    @Test
    void getSchemaReference_profileWithNoAugmentationFound_parseSchemaReturned() {
        when(this.augmentationDefinitionDAO.findById("cardq")).thenReturn(Optional.empty());
        assertEquals(VALID_PM_DEF_SOURCE, this.inputSchemaProvider.getSchemaReference(AUGMENTED_PROFILE_DEF_OBJ, VALID_PM_DEF_OBJ));
    }

    @Test
    void getSchemaReference_profileAugmented_AasSchemaReturned() {
        when(this.augmentationDefinitionDAO.findById("cardq")).thenReturn(
                Optional.of(AugmentationDefinition.builder().name("cardq").url("localhost:8080").build()));
        when(this.augmentationService.getSchemaMappings("cardq")).thenReturn(Map.of(VALID_PM_DEF_SOURCE, "cardq_AMF_Mobility_NetworkSlice_1"));

        assertEquals("cardq_AMF_Mobility_NetworkSlice_1", this.inputSchemaProvider.getSchemaReference(
                AUGMENTED_PROFILE_DEF_OBJ, VALID_PM_DEF_OBJ));
    }

    @Test
    void getSchemaReference_profileAugmentedButNoSchemaMappingForParserSchema_parseSchemaReturned() {
        final String expectedSchemaRef = "5G|PM_COUNTERS|smf_nsmf_pdu_session_snssai_apn_1";
        final PMDefinition pmDefinition = new PMDefinition(
                "pmCounters.create_sm_context_resp_succ",
                expectedSchemaRef,
                "Valid description");
        when(this.augmentationDefinitionDAO.findById("cardq")).thenReturn(
                Optional.of(AugmentationDefinition.builder().name("cardq").url("localhost:8080").build()));
        when(this.augmentationService.getSchemaMappings("cardq")).thenReturn(Collections.emptyMap());

        final String actualSchemaRef = this.inputSchemaProvider.getSchemaReference(AUGMENTED_PROFILE_DEF_OBJ, pmDefinition);

        assertEquals(actualSchemaRef, expectedSchemaRef);
    }

    @Test
    void prefetchInputSchema_nullMessageSchema_throwException() {
        final Set<String> schemaReferenceSet = new HashSet<>();
        schemaReferenceSet.add("5G|PM_COUNTERS|schemaA");
        schemaReferenceSet.add("5G|PM_COUNTERS|schemaB");
        final Map<String, MessageSchemaDTO> messageSchemasResponse = new HashMap<>();
        messageSchemasResponse.put("5G|PM_COUNTERS|schemaA", MessageSchemaDTO.builder().specificationReference("5G_PM_COUNTERS_schemaA/1").build());
        messageSchemasResponse.put("5G|PM_COUNTERS|schemaB", null);
        when(this.dataCatalogService.getMessageSchemas(schemaReferenceSet)).thenReturn(messageSchemasResponse);

        assertThrows(CsacValidationException.class, () -> this.inputSchemaProvider.prefetchInputSchemas(schemaReferenceSet));
        verify(this.dataCatalogService, times(1)).getMessageSchemas(anySet());
        verify(this.schemaRegistryService, times(0)).getSchemaLatest(anyString());
    }
}
