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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.List;

import com.ericsson.oss.air.csac.handler.validation.augmentation.AugmentationValidator;
import com.ericsson.oss.air.csac.handler.validation.pmsc.PmscConfigurationValidator;
import com.ericsson.oss.air.csac.model.AugmentationDefinition;
import com.ericsson.oss.air.csac.model.AugmentationRule;
import com.ericsson.oss.air.csac.model.AugmentationRuleField;
import com.ericsson.oss.air.csac.model.ResourceSubmission;
import com.ericsson.oss.air.csac.service.ResourceFileLoader;
import com.ericsson.oss.air.exception.CsacValidationException;
import jakarta.validation.Validation;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ValidationHandlerTest {

    final AugmentationRuleField augmentationRuleField = AugmentationRuleField.builder()
            .output("outputField")
            .inputFields(List.of("inputField"))
            .build();
    final AugmentationRule augmentationRule = AugmentationRule.builder()
            .inputSchemaReference("inputSchemaReference")
            .fields(List.of(augmentationRuleField))
            .build();
    final AugmentationDefinition augmentationDefinition = AugmentationDefinition.builder()
            .name("test")
            .url("http://localhost:8080")
            .type("core")
            .augmentationRules(List.of(augmentationRule))
            .build();

    final ResourceSubmission resourceSubmission = ResourceSubmission.builder()
            .augmentationDefinitions(List.of(augmentationDefinition))
            .build();

    @Mock
    private ResourceFileLoader resourceFileLoader;

    @Mock
    private PMValidator pmValidator;

    @Mock
    private KPIContextValidator kpiContextValidator;

    @Mock
    private ProfileContextValidator profileContextValidator;

    @Mock
    private AugmentationValidator augmentationValidator;

    @Mock
    private PmscConfigurationValidator pmscConfigurationValidator;

    @InjectMocks
    private ValidationHandler validationHandler;

    @Builder
    static class TestBean {

        @NotNull
        private String field;
    }

    @BeforeEach
    void setUp() {
        this.validationHandler.setValidator(Validation.buildDefaultValidatorFactory().getValidator());
    }

    @Test
    void ValidatePMDefinitions_ValidResourceSubmission() throws IOException {
        validationHandler.getValidPMDefinitions(resourceSubmission.getPmDefs());
        verify(pmValidator, times(1)).getValidPMDefinitions(resourceSubmission.getPmDefs());
    }

    @Test
    void ValidateKPIDefinitions_ValidResourceSubmission() throws IOException {
        validationHandler.validateKPIDefinitions(resourceSubmission);
        verify(kpiContextValidator, times(1)).validateKPIDefinitions(resourceSubmission);
    }

    @Test
    void ValidateProfileDefinitions_ValidResourceSubmission() throws IOException {
        validationHandler.validateProfileDefinitions(resourceSubmission);
        verify(profileContextValidator, times(1)).validateProfileDefinitions(resourceSubmission);
    }

    @Test
    void ValidatePMDefinitions_validationError_exceptionThrown() throws IOException {
        when(pmValidator.getValidPMDefinitions(any())).thenThrow(CsacValidationException.class);

        assertThrows(CsacValidationException.class,
                () -> validationHandler.getValidPMDefinitions(resourceSubmission.getPmDefs()));
    }

    @Test
    void ValidateKPIDefinitions_validationError_exceptionThrown() throws IOException {
        doThrow(CsacValidationException.class).when(kpiContextValidator).validateKPIDefinitions(any());

        assertThrows(CsacValidationException.class,
                () -> validationHandler.validateKPIDefinitions(new ResourceSubmission()));
    }

    @Test
    void validateAugmentations_validResourceSubmission() {

        this.validationHandler.validateAugmentations(this.resourceSubmission);

        verify(this.augmentationValidator, times(1)).validate(this.resourceSubmission.getAugmentationDefinitions().get(0));
    }

    @Test
    void validateAugmentations_throwsCsacValidationException_whenNotValid() {

        doThrow(CsacValidationException.class).when(this.augmentationValidator).validate(any());

        assertThrows(CsacValidationException.class,
                () -> this.validationHandler.validateAugmentations(this.resourceSubmission));
    }

    @Test
    void validateAppConfig_valid() {

        this.validationHandler.validateAppConfig();

        verify(this.augmentationValidator, times(1)).validateAppConfig();
        verify(this.pmscConfigurationValidator, times(1)).validateAppConfig();
    }

    @Test
    void validateAppConfig_throwsCsacValidationException_whenNotValid() {

        doThrow(CsacValidationException.class).when(this.augmentationValidator).validateAppConfig();

        assertThrows(CsacValidationException.class,
                () -> this.validationHandler.validateAppConfig());
    }

    @Test
    void checkEntity_nullEntity() throws Exception {
        assertThrows(CsacValidationException.class, () -> this.validationHandler.checkEntity(null));
    }

    @Test
    void checkEntity_invalidEntity() throws Exception {
        assertThrows(CsacValidationException.class, () -> this.validationHandler.checkEntity(TestBean.builder().build()));
    }

    @Test
    void checkEntity() throws Exception {
        assertDoesNotThrow(() -> this.validationHandler.checkEntity(new TestBean("bean")));
    }
}
