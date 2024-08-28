/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import com.ericsson.oss.air.util.codec.Codec;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;
import org.junit.jupiter.api.Test;

class AugmentationDefinitionTest {

    public static final String TEST_AUGMENTATION = "testAugmentation";

    private final AugmentationRuleField field1 = AugmentationRuleField.builder()
            .output("outputField")
            .inputFields(List.of("inputField1", "inputField2"))
            .build();

    private final AugmentationRuleField field2 = AugmentationRuleField.builder()
            .outputFields(List.of("outputField1", "outputField2"))
            .inputFields(List.of("inputField1", "inputField2"))
            .build();

    private final AugmentationRuleField field3 = AugmentationRuleField.builder()
            .output("outputField1")
            .outputFields(List.of("outputField2", "outputField3"))
            .inputFields(List.of("inputField1", "inputField2"))
            .build();

    private final AugmentationRule validRule1 = AugmentationRule.builder()
            .fields(List.of(this.field1))
            .inputSchemaReference("input|schema|reference")
            .build();

    private final AugmentationRule validRule2 = AugmentationRule.builder()
            .fields(List.of(this.field2))
            .inputSchemas(List.of("input|schema|reference1", "input|schema|reference2"))
            .build();

    private final AugmentationRule validRule3 = AugmentationRule.builder()
            .fields(List.of(this.field3))
            .inputSchemaReference("input|schema|reference1")
            .inputSchemas(List.of("input|schema|reference2", "input|schema|reference3"))
            .build();

    private final AugmentationDefinition validDefinitionWithInputSchemaListAndOutputFieldList = AugmentationDefinition.builder()
            .name(TEST_AUGMENTATION)
            .augmentationRules(List.of(this.validRule2))
            .url("http://test.com:8080")
            .type("test")
            .build();

    private final AugmentationDefinition validDefinitionWithAllSupportedFields = AugmentationDefinition.builder()
            .name(TEST_AUGMENTATION)
            .augmentationRules(List.of(this.validRule3))
            .url("http://test.com:8080")
            .type("test")
            .build();

    private final AugmentationDefinition validDefinitionNoType = AugmentationDefinition.builder()
            .name(TEST_AUGMENTATION)
            .augmentationRules(List.of(this.validRule1))
            .url("http://test.com:8080")
            .build();

    private final AugmentationDefinition validDefinitionWithType = AugmentationDefinition.builder()
            .name(TEST_AUGMENTATION)
            .augmentationRules(List.of(this.validRule1))
            .url("http://test.com:8080")
            .type("test")
            .build();

    private final AugmentationDefinition validDefinitionWithUrlRef = AugmentationDefinition.builder()
            .name(TEST_AUGMENTATION)
            .augmentationRules(List.of(this.validRule1))
            .url("${cardq}")
            .type("test")
            .build();

    private static final String VALID_DEF_NO_TYPE_STR = "{\"ardq_id\":\"testAugmentation\",\"ardq_url\":\"http://test.com:8080\",\"ardq_rules\":[{\"input_schema\":\"input|schema|reference\",\"fields\":[{\"output\":\"outputField\",\"input\":[\"inputField1\",\"inputField2\"]}]}]}";

    private static final String VALID_DEF_WITH_TYPE_STR = "{\"ardq_id\":\"testAugmentation\",\"ardq_url\":\"http://test.com:8080\",\"ardq_type\":\"test\",\"ardq_rules\":[{\"input_schema\":\"input|schema|reference\",\"fields\":[{\"output\":\"outputField\",\"input\":[\"inputField1\",\"inputField2\"]}]}]}";

    private static final String VALID_DEF_WITH_URL_REF = "{\"ardq_id\":\"testAugmentation\",\"ardq_url\":\"${cardq}\",\"ardq_type\":\"test\",\"ardq_rules\":[{\"input_schema\":\"input|schema|reference\",\"fields\":[{\"output\":\"outputField\",\"input\":[\"inputField1\",\"inputField2\"]}]}]}";

    private static final String VALID_DEF_WITH_LIST_OF_INPUT_SCHEMAS_AND_LIST_OF_OUTPUT_FIELDS_STR = "{\"ardq_id\":\"testAugmentation\",\"ardq_url\":\"http://test.com:8080\",\"ardq_type\":\"test\",\"ardq_rules\":[{\"input_schemas\":[\"input|schema|reference1\",\"input|schema|reference2\"],\"fields\":[{\"output_fields\":[\"outputField1\", \"outputField2\"],\"input\":[\"inputField1\",\"inputField2\"]}]}]}";

    private static final String VALID_DEF_WITH_ALL_SUPPORTED_FIELDS = "{\"ardq_id\":\"testAugmentation\",\"ardq_url\":\"http://test.com:8080\",\"ardq_type\":\"test\",\"ardq_rules\":[{\"input_schema\":\"input|schema|reference1\",\"input_schemas\":[\"input|schema|reference2\",\"input|schema|reference3\"],\"fields\":[{\"output\":\"outputField1\",\"output_fields\":[\"outputField2\", \"outputField3\"],\"input\":[\"inputField1\",\"inputField2\"]}]}]}";

    private static final String INVALID_DEF_NO_URL_STR = "{\"ardq_id\":\"testAugmentation\",\"ardq_type\":\"test\",\"ardq_rules\":[{\"input_schema\":\"input|schema|reference\",\"fields\":[{\"output\":\"outputField\",\"input\":[\"inputField1\",\"inputField2\"]}]}]}";

    private static final String INVALID_DEF_INVALID_NAME_STR = "{\"ardq_id\":\"test Augmentation\",\"ardq_url\":\"http://test.com:8080\",\"ardq_rules\":[{\"input_schema\":\"input|schema|reference\",\"fields\":[{\"output\":\"outputField\",\"input\":[\"inputField1\",\"inputField2\"]}]}]}";

    private static final String INVALID_DEF_INVALID_RULE_NO_FIELD_STR = "{\"ardq_id\":\"testAugmentation\",\"ardq_url\":\"http://test.com:8080\",\"ardq_rules\":[{\"input_schema\":\"input|schema|reference\"}]}";

    private static final String INVALID_DEF_INVALID_RULE_NO_INPUT_STR = "{\"ardq_id\":\"testAugmentation\",\"ardq_url\":\"http://test.com:8080\",\"ardq_rules\":[{\"input_schema\":\"input|schema|reference\",\"fields\":[{\"output\":\"outputField\",\"input\":[]}]}]}";

    private static final String INVALID_DEF_INVALID_RULE_MISSING_INPUT_STR = "{\"ardq_id\":\"testAugmentation\",\"ardq_url\":\"http://test.com:8080\",\"ardq_rules\":[{\"input_schema\":\"input|schema|reference\",\"fields\":[{\"output\":\"outputField\"}]}]}";

    private static final String INVALID_DEF_INVALID_FIELDS_BAD_FORMAT_OUTPUT = "{\"ardq_id\":\"testAugmentation\",\"ardq_url\":\"http://test.com:8080\",\"ardq_type\":\"test\",\"ardq_rules\":[{\"input_schema\":\"input|schema|reference\",\"fields\":[{\"output\":\"123outputField\",\"input\":[\"inputField1\",\"inputField2\"]}]}]}";

    private final Codec codec = new Codec(Validation.buildDefaultValidatorFactory().getValidator());

    @Test
    public void testValidAugmentationDefinition_NoType() throws Exception {

        final AugmentationDefinition actual = this.codec.withValidation().readValue(VALID_DEF_NO_TYPE_STR, AugmentationDefinition.class);

        assertEquals(this.validDefinitionNoType, actual);
    }

    @Test
    public void testValidAugmentationDefinition_withType() throws Exception {

        final AugmentationDefinition actual = this.codec.withValidation().readValue(VALID_DEF_WITH_TYPE_STR, AugmentationDefinition.class);

        assertEquals(this.validDefinitionWithType, actual);
    }

    @Test
    public void testValidAugmentationDefinition_withUrlReference() throws Exception {

        final AugmentationDefinition actual = this.codec.withValidation().readValue(VALID_DEF_WITH_URL_REF, AugmentationDefinition.class);

        assertEquals(this.validDefinitionWithUrlRef, actual);
    }

    @Test
    public void testValidAugmentationDefinition_NoUrl() {

        assertThrows(ConstraintViolationException.class,
                () -> this.codec.withValidation().readValue(INVALID_DEF_NO_URL_STR, AugmentationDefinition.class));
    }

    @Test
    public void testValidAugmentationDefinition_withInputSchemaListAndOutputFieldList() throws Exception {

        final AugmentationDefinition actual = this.codec.withValidation()
                .readValue(VALID_DEF_WITH_LIST_OF_INPUT_SCHEMAS_AND_LIST_OF_OUTPUT_FIELDS_STR, AugmentationDefinition.class);

        assertEquals(this.validDefinitionWithInputSchemaListAndOutputFieldList, actual);
    }

    @Test
    public void testValidAugmentationDefinition_withAllSupportedFields() throws Exception {

        final AugmentationDefinition actual = this.codec.withValidation()
                .readValue(VALID_DEF_WITH_ALL_SUPPORTED_FIELDS, AugmentationDefinition.class);

        assertEquals(this.validDefinitionWithAllSupportedFields, actual);
    }

    @Test
    public void testInvalidAugmentationDefinition_invalidName() {

        assertThrows(ConstraintViolationException.class,
                () -> this.codec.withValidation().readValue(INVALID_DEF_INVALID_NAME_STR, AugmentationDefinition.class));
    }

    @Test
    public void testInvalidAugmentationDefinition_invalidRuleNoField() {

        assertThrows(ConstraintViolationException.class,
                () -> this.codec.withValidation().readValue(INVALID_DEF_INVALID_RULE_NO_FIELD_STR, AugmentationDefinition.class));
    }

    @Test
    public void testInvalidAugmentationDefinition_invalidRuleNoInputFields() {

        assertThrows(ConstraintViolationException.class,
                () -> this.codec.withValidation().readValue(INVALID_DEF_INVALID_RULE_NO_INPUT_STR, AugmentationDefinition.class));
    }

    @Test
    public void testInvalidAugmentationDefinition_invalidRuleMissingInputFields() {

        assertThrows(ConstraintViolationException.class,
                () -> this.codec.withValidation().readValue(INVALID_DEF_INVALID_RULE_MISSING_INPUT_STR, AugmentationDefinition.class));
    }

    @Test
    public void testInvalidAugmentationDefinition_invalidFieldsBadFormatOutput() {

        assertThrows(ConstraintViolationException.class,
                () -> this.codec.withValidation().readValue(INVALID_DEF_INVALID_FIELDS_BAD_FORMAT_OUTPUT, AugmentationDefinition.class));
    }

}