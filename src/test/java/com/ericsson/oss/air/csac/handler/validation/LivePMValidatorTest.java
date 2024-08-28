/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.handler.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.ericsson.oss.air.csac.configuration.schema.impl.LiveInputSchemaProvider;
import com.ericsson.oss.air.csac.model.PMDefinition;
import com.ericsson.oss.air.csac.model.ResourceSubmission;
import com.ericsson.oss.air.csac.model.pmschema.PmSchema;
import com.ericsson.oss.air.csac.model.pmschema.PMSchemaDTO;
import com.ericsson.oss.air.exception.CsacValidationException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(properties = { "validation.external.enabled=true" })
@ActiveProfiles("test")
class LivePMValidatorTest {

    @MockBean
    private LiveInputSchemaProvider inputSchemaProvider;

    @Autowired
    private PMValidator pmValidator;

    private static final String KAFKA_TOPIC = "test-topic";

    private static final String PM_NAME_A = "myPM_A";

    private static final String PM_NAME_B = "myPM_B";

    private static final String PM_NAME_C = "myPM_C";

    private static final String PM_SCHEMA_NAME_A = "myPMSchema_A";

    private static final String PM_SCHEMA_NAME_AC = "myPMSchema_AC";

    private static final String PM_SCHEMA_NAME_B = "myPMSchema_B";

    // The source attribute value for the PM definitions that refers to its Avro schema
    private static final String SOURCE_A = "5G|PM_COUNTERS|AMF_Mobility_NetworkSlice_1";

    private static final String SOURCE_B = "5G|PM_COUNTERS|up_payload_dnn_slice_1";

    private static final PMDefinition PM_DEFN_A = new PMDefinition(PM_NAME_A, SOURCE_A, null);

    private static final PMDefinition PM_DEFN_B = new PMDefinition(PM_NAME_B, SOURCE_B, null);

    private static final PMDefinition PM_DEFN_C = new PMDefinition(PM_NAME_C, SOURCE_A, null);

    private static final ResourceSubmission RS_A = new ResourceSubmission();

    private static final ResourceSubmission RS_AC = new ResourceSubmission();

    private static final ResourceSubmission RS_C = new ResourceSubmission();

    private static final ResourceSubmission RS_ABC = new ResourceSubmission();

    private static PmSchema PM_SCHEMA_A;

    private static PmSchema PM_SCHEMA_AC;

    private static PmSchema PM_SCHEMA_B;

    @BeforeAll
    static void setUpClass() {

        RS_A.setPmDefs(List.of(PM_DEFN_A));
        RS_AC.setPmDefs(Arrays.asList(PM_DEFN_A, PM_DEFN_C));
        RS_C.setPmDefs(List.of(PM_DEFN_C));
        RS_ABC.setPmDefs(List.of(PM_DEFN_A, PM_DEFN_B, PM_DEFN_C));

        PM_SCHEMA_A = new PmSchema(KAFKA_TOPIC, PMSchemaDTO.builder()
                .fieldPaths(List.of(PM_NAME_A))
                .name(PM_SCHEMA_NAME_A)
                .build());

        PM_SCHEMA_AC = new PmSchema(KAFKA_TOPIC, PMSchemaDTO.builder()
                .fieldPaths(List.of(PM_NAME_A, PM_NAME_C))
                .name(PM_SCHEMA_NAME_AC)
                .build());

        PM_SCHEMA_B = new PmSchema(KAFKA_TOPIC, PMSchemaDTO.builder()
                .fieldPaths(List.of(PM_NAME_B))
                .name(PM_SCHEMA_NAME_B)
                .build());

    }

    @Test
    void getValidPMDefinitions_Valid() {
        when(this.inputSchemaProvider.getSchema(SOURCE_A)).thenReturn(PM_SCHEMA_A);

        final Map<String, List<PMDefinition>> schemaNamePMDefsMap = this.pmValidator.getValidPMDefinitions(RS_A.getPmDefs());

        assertNotNull(schemaNamePMDefsMap);
        assertFalse(schemaNamePMDefsMap.isEmpty());
        assertTrue(schemaNamePMDefsMap.containsKey(PM_SCHEMA_NAME_A));
        assertEquals(1, schemaNamePMDefsMap.size());
        assertEquals(List.of(PM_DEFN_A), schemaNamePMDefsMap.get(PM_SCHEMA_NAME_A));

    }

    @Test
    void getValidPMDefinitions_MultipleNamesInSchemaOnePMNameInSubmission_Valid() {

        when(this.inputSchemaProvider.getSchema(SOURCE_A)).thenReturn(PM_SCHEMA_AC);

        final Map<String, List<PMDefinition>> schemaNamePMDefsMap = pmValidator.getValidPMDefinitions(RS_A.getPmDefs());

        assertNotNull(schemaNamePMDefsMap);
        assertFalse(schemaNamePMDefsMap.isEmpty());
        assertTrue(schemaNamePMDefsMap.containsKey(PM_SCHEMA_NAME_AC));
        assertEquals(1, schemaNamePMDefsMap.size());
        assertEquals(List.of(PM_DEFN_A), schemaNamePMDefsMap.get(PM_SCHEMA_NAME_AC));

    }

    @Test
    void getValidPMDefinitions_MultipleNamesInSchemaMultiplePMNameInSubmission_Valid() {

        when(this.inputSchemaProvider.getSchema(SOURCE_A)).thenReturn(PM_SCHEMA_AC);

        final Map<String, List<PMDefinition>> schemaNamePMDefsMap = pmValidator.getValidPMDefinitions(RS_AC.getPmDefs());

        assertNotNull(schemaNamePMDefsMap);
        assertFalse(schemaNamePMDefsMap.isEmpty());
        assertTrue(schemaNamePMDefsMap.containsKey(PM_SCHEMA_NAME_AC));
        assertEquals(1, schemaNamePMDefsMap.size());
        assertEquals(List.of(PM_DEFN_A, PM_DEFN_C), schemaNamePMDefsMap.get(PM_SCHEMA_NAME_AC));

    }

    @Test
    void getValidPMDefinitions_twoSetsSchemaRefs_Valid() {

        when(this.inputSchemaProvider.getSchema(SOURCE_A)).thenReturn(PM_SCHEMA_AC);
        when(this.inputSchemaProvider.getSchema(SOURCE_B)).thenReturn(PM_SCHEMA_B);

        final Map<String, List<PMDefinition>> schemaNamePMDefsMap = pmValidator.getValidPMDefinitions(RS_ABC.getPmDefs());

        assertNotNull(schemaNamePMDefsMap);
        assertFalse(schemaNamePMDefsMap.isEmpty());
        assertTrue(schemaNamePMDefsMap.containsKey(PM_SCHEMA_NAME_AC));
        assertTrue(schemaNamePMDefsMap.containsKey(PM_SCHEMA_NAME_B));
        assertEquals(2, schemaNamePMDefsMap.size());
        assertEquals(List.of(PM_DEFN_A, PM_DEFN_C), schemaNamePMDefsMap.get(PM_SCHEMA_NAME_AC));
        assertEquals(List.of(PM_DEFN_B), schemaNamePMDefsMap.get(PM_SCHEMA_NAME_B));

    }

    @Test
    void getValidPMDefinitions_PMNameinSubmissionButNotInSchema_ThrowsException() {

        when(this.inputSchemaProvider.getSchema(SOURCE_A)).thenReturn(PM_SCHEMA_B);

        assertThrows(CsacValidationException.class, () -> pmValidator.getValidPMDefinitions(RS_A.getPmDefs()));

    }

    @Test
    void getValidPMDefinitions_MultiplePMNamesinSubmissionButNotInSchema_ThrowsException() {

        when(this.inputSchemaProvider.getSchema(SOURCE_A)).thenReturn(PM_SCHEMA_B);

        assertThrows(CsacValidationException.class, () -> this.pmValidator.getValidPMDefinitions(RS_AC.getPmDefs()));

    }

    @Test
    void getValidPMDefinitions_MultiplePMNamesinSubmissionButOnlyOneNameInSchema_ThrowsException() {
        final ResourceSubmission RS_AB = ResourceSubmission.builder().pmDefs(List.of(PM_DEFN_A, PM_DEFN_B)).build();

        when(this.inputSchemaProvider.getSchema(SOURCE_A)).thenReturn(PM_SCHEMA_A);
        when(this.inputSchemaProvider.getSchema(SOURCE_B)).thenReturn(PM_SCHEMA_A);

        assertThrows(CsacValidationException.class, () -> this.pmValidator.getValidPMDefinitions(RS_AB.getPmDefs()));

    }

    @Test
    void getValidPMDefinitions_NullPMDefinitions_Valid() {

        final Map<String, List<PMDefinition>> schemaNamePMDefsMap = this.pmValidator.getValidPMDefinitions(null);

        assertNotNull(schemaNamePMDefsMap);
        assertTrue(schemaNamePMDefsMap.isEmpty());

    }

    @Test
    void getValidPMDefinitions_EmptyPMDefinitions_Valid() {

        final Map<String, List<PMDefinition>> schemaNamePMDefsMap = this.pmValidator.getValidPMDefinitions(new ArrayList<>());

        assertNotNull(schemaNamePMDefsMap);
        assertTrue(schemaNamePMDefsMap.isEmpty());

    }

    @Test
    void getValidPMDefinitions_MissingMatchingSchemaFromSchemaRegistryClient_ThrowsException() {

        when(this.inputSchemaProvider.getSchema("notExist"))
                .thenReturn(null);

        assertThrows(CsacValidationException.class, () -> this.pmValidator.getValidPMDefinitions(RS_A.getPmDefs()));

    }

    @Test
    void validate_NullPMDefinitions_Valid() {
        this.pmValidator.validate(null);
    }

    @Test
    void validate_EmptyPMDefinitions_Valid() {
        this.pmValidator.validate(new ArrayList<>());
    }

    @Test
    void validate_DataCatalogClientThrowsException_ThrowsException() {
        assertThrows(RuntimeException.class, () -> this.pmValidator.validate(RS_AC.getPmDefs()));
    }

}
