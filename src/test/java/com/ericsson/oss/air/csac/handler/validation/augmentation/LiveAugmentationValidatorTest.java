/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.handler.validation.augmentation;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ericsson.oss.air.csac.configuration.augmentation.AugmentationConfiguration;
import com.ericsson.oss.air.csac.configuration.augmentation.AugmentationProperties;
import com.ericsson.oss.air.csac.configuration.schema.InputSchemaProvider;
import com.ericsson.oss.air.csac.model.AugmentationDefinition;
import com.ericsson.oss.air.csac.model.AugmentationRule;
import com.ericsson.oss.air.csac.model.AugmentationRuleField;
import com.ericsson.oss.air.csac.model.pmschema.PMSchemaDTO;
import com.ericsson.oss.air.csac.model.pmschema.PmSchema;
import com.ericsson.oss.air.csac.service.augmentation.LiveAugmentationProvisioningService;
import com.ericsson.oss.air.exception.CsacValidationException;
import com.ericsson.oss.air.util.logging.FaultHandler;
import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class LiveAugmentationValidatorTest {

    private static final String KAFKA_TOPIC = "test-topic";

    public static final String INPUT_SCHEMA_REFERENCE = "inputSchemaReference";

    final AugmentationRuleField augmentationRuleField = AugmentationRuleField.builder()
            .output("outputField")
            .inputFields(List.of("inputField1", "inputField2"))
            .build();

    final AugmentationRule augmentationRule = AugmentationRule.builder()
            .inputSchemaReference(INPUT_SCHEMA_REFERENCE)
            .fields(List.of(augmentationRuleField))
            .build();

    final AugmentationDefinition augmentationDefinition = AugmentationDefinition.builder()
            .name("test")
            .url("http://localhost:8080")
            .type("core")
            .augmentationRules(List.of(augmentationRule))
            .build();

    final Field inputField1 = new Field("inputField1", Schema.create(Schema.Type.STRING), null, null);

    final Field inputField2 = new Field("inputField2", Schema.create(Schema.Type.STRING), null, null);

    final Field outputField = new Field("outputField", Schema.create(Schema.Type.STRING), null, null);

    final PmSchema inputSchema = new PmSchema(KAFKA_TOPIC, PMSchemaDTO.builder()
            .name("AMF_Mobility_NetworkSlice")
            .namespace("PM_COUNTERS")
            .fields(List.of(inputField1, inputField2))
            .build());

    final PmSchema inputSchemaWithoutInputField2 = new PmSchema(KAFKA_TOPIC, PMSchemaDTO.builder()
            .name("AMF_Mobility_NetworkSlice")
            .namespace("PM_COUNTERS")
            .fields(List.of(inputField1))
            .build());

    final PmSchema inputSchemaWithOutputField = new PmSchema(KAFKA_TOPIC, PMSchemaDTO.builder()
            .name("AMF_Mobility_NetworkSlice")
            .namespace("PM_COUNTERS")
            .fields(List.of(inputField1, inputField2, outputField))
            .build());

    @Mock
    private LiveAugmentationProvisioningService augmentationProvisioningService;

    @Mock
    private AugmentationProperties augmentationProperties;

    @Mock
    private AugmentationConfiguration augmentationConfiguration;

    @Mock
    private InputSchemaProvider inputSchemaProvider;

    @Mock
    private FaultHandler faultHandler;

    @InjectMocks
    private LiveAugmentationValidator augmentationValidator;

    @Test
    void validate_prodMode_shouldCallgetSchemaAndPassValidation() {
        when(this.augmentationConfiguration.isDryRunModeEnabled()).thenReturn(false);
        when(this.inputSchemaProvider.getSchema(INPUT_SCHEMA_REFERENCE)).thenReturn(inputSchema);

        this.augmentationValidator.validate(augmentationDefinition);

        verify(this.augmentationProvisioningService, times(1)).checkArdqType(augmentationDefinition);
        verify(this.inputSchemaProvider, times(1)).getSchema(INPUT_SCHEMA_REFERENCE);
    }

    @Test
    void validate_dryrunMode_shouldNotCallgetSchemaAndPassValidation() {
        when(this.augmentationConfiguration.isDryRunModeEnabled()).thenReturn(true);

        this.augmentationValidator.validate(augmentationDefinition);

        verify(this.augmentationProvisioningService, times(1)).checkArdqType(augmentationDefinition);
        verify(this.inputSchemaProvider, times(0)).getSchema(INPUT_SCHEMA_REFERENCE);

    }

    @Test
    void validate_throwsCsacValidationException_whenNotValid() {

        doThrow(CsacValidationException.class).when(this.augmentationProvisioningService).checkArdqType(augmentationDefinition);

        assertThrows(CsacValidationException.class,
                () -> this.augmentationValidator.validate(augmentationDefinition));

    }

    @Test
    void validateAppConfig_test() {

        final Map<String, String> ardqConfigMap = new HashMap<>();
        ardqConfigMap.put("ardq", "http://ardq:8080");
        ardqConfigMap.put("core", "http://core:8080");

        when(this.augmentationProperties.getArdqConfig()).thenReturn(ardqConfigMap);

        this.augmentationValidator.validateAppConfig();

        verify(this.augmentationProvisioningService, times(1)).checkUrl("http://ardq:8080");
        verify(this.augmentationProvisioningService, times(1)).checkUrl("http://core:8080");
    }

    @Test
    void validateAppConfig_throwsCsacValidationException_whenNotValid() {

        final Map<String, String> ardqConfigMap = new HashMap<>();
        ardqConfigMap.put("ardq", "http://ardq:8080");
        ardqConfigMap.put("core", "http://core:8080");

        when(this.augmentationProperties.getArdqConfig()).thenReturn(ardqConfigMap);

        doThrow(CsacValidationException.class).when(this.augmentationProvisioningService).checkUrl((String) any());

        assertThrows(CsacValidationException.class,
                () -> this.augmentationValidator.validateAppConfig());

    }

    @Test
    void checkFields_successful() {

        when(this.inputSchemaProvider.getSchema(INPUT_SCHEMA_REFERENCE)).thenReturn(inputSchema);

        this.augmentationValidator.checkFields(augmentationDefinition);

        verify(this.inputSchemaProvider, times(1)).getSchema(INPUT_SCHEMA_REFERENCE);
    }

    @Test
    void checkFields_inputFieldDoNotExistInSchema_throwException() {

        when(this.inputSchemaProvider.getSchema(INPUT_SCHEMA_REFERENCE)).thenReturn(inputSchemaWithoutInputField2);

        assertThrows(CsacValidationException.class,
                () -> this.augmentationValidator.checkFields(augmentationDefinition));

    }

    @Test
    void checkFields_outputFieldExistInSchema_throwException() {

        when(this.inputSchemaProvider.getSchema(INPUT_SCHEMA_REFERENCE)).thenReturn(inputSchemaWithOutputField);

        assertThrows(CsacValidationException.class,
                () -> this.augmentationValidator.checkFields(augmentationDefinition));

    }

}
