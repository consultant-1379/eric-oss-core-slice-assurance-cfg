/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.service.augmentation;

import static com.ericsson.oss.air.csac.model.TestResourcesUtils.REGISTRATION_RESPONSE_DTO;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.ericsson.oss.air.csac.configuration.augmentation.AugmentationConfiguration;
import com.ericsson.oss.air.csac.configuration.augmentation.AugmentationProperties;
import com.ericsson.oss.air.csac.model.AugmentationDefinition;
import com.ericsson.oss.air.csac.model.AugmentationRule;
import com.ericsson.oss.air.csac.model.AugmentationRuleField;
import com.ericsson.oss.air.csac.model.augmentation.AugmentationFieldRequestDto;
import com.ericsson.oss.air.csac.model.augmentation.AugmentationRequestDto;
import com.ericsson.oss.air.csac.model.augmentation.AugmentationRuleRequestDto;
import com.ericsson.oss.air.exception.CsacValidationException;
import com.ericsson.oss.air.util.codec.Codec;
import com.ericsson.oss.air.util.logging.FaultHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;

@ExtendWith(MockitoExtension.class)
class LiveAugmentationProvisioningServiceTest {

    public static final String TEST_AUGMENTATION1 = "testAugmentation1";

    public static final String TEST_AUGMENTATION2 = "testAugmentation2";

    public static final String ARDQ_URL = "http://test.com:8080";

    public static final String URL_REFERENCE = "${cardq}";

    private final AugmentationRuleField validField1 = AugmentationRuleField.builder()
            .output("outputField")
            .inputFields(List.of("inputField1", "inputField2"))
            .build();

    private final AugmentationRule validRule1 = AugmentationRule.builder()
            .inputSchemaReference("input|schema|reference")
            .fields(List.of(validField1))
            .build();

    private final AugmentationDefinition augDefinitionWithType = AugmentationDefinition.builder()
            .name(TEST_AUGMENTATION1)
            .type("core")
            .augmentationRules(List.of(this.validRule1))
            .url(ARDQ_URL)
            .build();

    private final AugmentationDefinition augDefinitionWithoutType = AugmentationDefinition.builder()
            .name(TEST_AUGMENTATION1)
            .augmentationRules(List.of(this.validRule1))
            .url(ARDQ_URL)
            .build();

    private final AugmentationDefinition augDefinitionWithUrlReference = AugmentationDefinition.builder()
            .name(TEST_AUGMENTATION1)
            .type("core")
            .augmentationRules(List.of(this.validRule1))
            .url(URL_REFERENCE)
            .build();

    private final AugmentationFieldRequestDto augmentationFieldRequestDto1 = AugmentationFieldRequestDto.builder()
            .input(List.of("inputField1", "inputField2"))
            .output("outputField1")
            .build();

    private final AugmentationRuleRequestDto augmentationRuleRequestDto1 = AugmentationRuleRequestDto.builder()
            .inputSchema("input|schema|reference")
            .fields(List.of(augmentationFieldRequestDto1))
            .build();

    private final AugmentationRequestDto augmentationRequestDto1 = AugmentationRequestDto.builder()
            .ardqId(TEST_AUGMENTATION1)
            .ardqUrl(ARDQ_URL)
            .rules(List.of(augmentationRuleRequestDto1))
            .build();

    private final AugmentationFieldRequestDto augmentationFieldRequestDto2 = AugmentationFieldRequestDto.builder()
            .input(List.of("inputField3", "inputField4"))
            .output("outputField2")
            .build();

    private final AugmentationRuleRequestDto augmentationRuleRequestDto2 = AugmentationRuleRequestDto.builder()
            .inputSchema("dummy|schema|reference")
            .fields(List.of(augmentationFieldRequestDto2))
            .build();

    private final AugmentationRequestDto augmentationRequestDto2 = AugmentationRequestDto.builder()
            .ardqId(TEST_AUGMENTATION2)
            .ardqUrl(ARDQ_URL)
            .rules(List.of(augmentationRuleRequestDto2))
            .build();

    private final Codec codec = new Codec();

    private final Codec mockCodec = spy(this.codec);

    private final FaultHandler faultHandler = new FaultHandler();

    @Mock
    private ArdqRestClient ardqRestClient;

    @Mock
    private AugmentationRestClient augmentationRestClient;

    @Mock
    private AugmentationProperties augmentationProperties;

    @Mock
    private AugmentationConfiguration augmentationConfiguration;

    private LiveAugmentationProvisioningService service;

    private LiveAugmentationProvisioningService mockService;

    private ListAppender<ILoggingEvent> listAppender;

    @BeforeEach
    public void setUp() {

        this.service = new LiveAugmentationProvisioningService(this.ardqRestClient,
                this.augmentationRestClient,
                this.augmentationConfiguration,
                this.codec,
                this.faultHandler);

        this.mockService = new LiveAugmentationProvisioningService(this.ardqRestClient,
                this.augmentationRestClient,
                this.augmentationConfiguration,
                this.mockCodec,
                this.faultHandler);

        final Logger logger = (Logger) LoggerFactory.getLogger(LiveAugmentationProvisioningService.class);
        this.listAppender = new ListAppender<>();
        this.listAppender.start();
        logger.addAppender(listAppender);
    }

    @Test
    void checkArdqType_augmentationDefinitionWithoutType() {

        this.service.checkArdqType(augDefinitionWithoutType);

        verify(this.ardqRestClient, times(0)).getArdqQueryTypes(any());
    }

    @Test
    void checkArdqType_augmentationDefinitionWithTypeAndUrl() {

        when(this.augmentationConfiguration.getResolvedUrl(ARDQ_URL)).thenReturn(ARDQ_URL);
        when(this.ardqRestClient.getArdqQueryTypes(ARDQ_URL)).thenReturn(List.of("core", "ran"));

        this.service.checkArdqType(augDefinitionWithType);

        verify(this.ardqRestClient, times(1)).getArdqQueryTypes(augDefinitionWithType.getUrl());
    }

    @Test
    void checkArdqType_augmentationDefinitionWithTypeAndUrlReference() {

        when(this.augmentationConfiguration.getResolvedUrl(URL_REFERENCE)).thenReturn(ARDQ_URL);
        when(this.ardqRestClient.getArdqQueryTypes(ARDQ_URL))
                .thenReturn(List.of(augDefinitionWithUrlReference.getType()));

        this.service.checkArdqType(augDefinitionWithUrlReference);

        verify(this.ardqRestClient, times(1)).getArdqQueryTypes(ARDQ_URL);
    }

    @Test
    void checkArdqType_augmentationDefinitionWithType_throwExceptionWhenTypeNotFoundInArdqService() {

        when(this.augmentationConfiguration.getResolvedUrl(ARDQ_URL)).thenReturn(ARDQ_URL);
        when(this.ardqRestClient.getArdqQueryTypes(ARDQ_URL)).thenReturn(List.of("ran"));

        assertThrows(CsacValidationException.class, () -> this.service.checkArdqType(augDefinitionWithType));
    }

    @Test
    void create() throws Exception {

        this.service.create(List.of(augmentationRequestDto1, augmentationRequestDto2));

        verify(this.augmentationRestClient, times(1)).create(augmentationRequestDto1);
        verify(this.augmentationRestClient, times(1)).create(augmentationRequestDto2);
    }

    @Test
    void create_jsonException() throws Exception {

        when(this.mockCodec.writeValueAsString(any())).thenThrow(JsonProcessingException.class);

        assertThrows(JsonProcessingException.class, () -> this.mockService.create(List.of(augmentationRequestDto1)));
    }

    @Test
    void update() throws Exception {

        this.service.update(List.of(augmentationRequestDto1, augmentationRequestDto2));

        verify(this.augmentationRestClient, times(1)).update(augmentationRequestDto1);
        verify(this.augmentationRestClient, times(1)).update(augmentationRequestDto2);
    }

    @Test
    void update_jsonException() throws Exception {

        when(this.mockCodec.writeValueAsString(any())).thenThrow(JsonProcessingException.class);

        assertThrows(JsonProcessingException.class, () -> this.mockService.update(List.of(augmentationRequestDto1)));
    }

    @Test
    void delete() throws Exception {

        this.service.delete(List.of(augmentationRequestDto1, augmentationRequestDto2));

        verify(this.augmentationRestClient, times(1)).delete(augmentationRequestDto1.getArdqId());
        verify(this.augmentationRestClient, times(1)).delete(augmentationRequestDto2.getArdqId());
    }

    @Test
    void delete_jsonException() throws Exception {

        when(this.mockCodec.writeValueAsString(any())).thenThrow(JsonProcessingException.class);

        assertThrows(JsonProcessingException.class, () -> this.mockService.delete(List.of(augmentationRequestDto1)));
    }

    @Test
    void getSchemaMappings() {
        when(this.augmentationRestClient.getArdqRegistrationById("cardq")).thenReturn(REGISTRATION_RESPONSE_DTO);
        final Map<String, String> schemaMappings = this.service.getSchemaMappings("cardq");
        assertEquals(1, schemaMappings.size());
        assertEquals("bar", schemaMappings.get("foo"));
    }

    @Test
    void deleteAll() throws Exception {

        when(this.augmentationRestClient.getAllArdqIds()).thenReturn(List.of("aug1", "aug2"));

        this.service.deleteAll();

        verify(this.augmentationRestClient, times(1)).delete("aug1");
        verify(this.augmentationRestClient, times(1)).delete("aug2");
    }

}