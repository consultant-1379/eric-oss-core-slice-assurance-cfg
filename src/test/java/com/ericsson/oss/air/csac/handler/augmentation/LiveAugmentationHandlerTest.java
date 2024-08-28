/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.handler.augmentation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.ericsson.oss.air.csac.configuration.augmentation.AugmentationConfiguration;
import com.ericsson.oss.air.csac.handler.event.ConsistencyCheckEvent;
import com.ericsson.oss.air.csac.handler.event.ConsistencyCheckHandler;
import com.ericsson.oss.air.csac.model.AugmentationDefinition;
import com.ericsson.oss.air.csac.model.AugmentationRule;
import com.ericsson.oss.air.csac.model.AugmentationRuleField;
import com.ericsson.oss.air.csac.model.KPIReference;
import com.ericsson.oss.air.csac.model.ProfileDefinition;
import com.ericsson.oss.air.csac.model.augmentation.AugmentationFieldRequestDto;
import com.ericsson.oss.air.csac.model.augmentation.AugmentationRequestDto;
import com.ericsson.oss.air.csac.model.augmentation.AugmentationRuleRequestDto;
import com.ericsson.oss.air.csac.repository.AugmentationDefinitionDAO;
import com.ericsson.oss.air.csac.repository.EffectiveAugmentationDAO;
import com.ericsson.oss.air.csac.service.augmentation.AugmentationProvisioningService;
import com.ericsson.oss.air.exception.CsacConsistencyCheckException;
import com.ericsson.oss.air.exception.CsacValidationException;
import com.ericsson.oss.air.util.codec.Codec;
import com.ericsson.oss.air.util.logging.FaultHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;

@ExtendWith(MockitoExtension.class)
public class LiveAugmentationHandlerTest {

    private static final String VALID_NAME = "profiledef_name";

    private static final String DESCRIPTION = "profiledef decription";

    private static final String VALID_AGGREGATION_FIELD = "field";

    private static final List<String> VALID_AGGREGATION_FIELDS = List.of(VALID_AGGREGATION_FIELD);

    private static final String VALID_REF = "kpi_reference";

    private static final KPIReference VALID_KPI_REF = KPIReference.builder().ref(VALID_REF).build();

    private static final List<KPIReference> VALID_LIST_KPI_REFS = List.of(VALID_KPI_REF);

    public static final String TEST_AUGMENTATION = "testAugmentation";

    public static final String ARDQ_URL = "http://test.com:8080";

    public static final String URL_REFERENCE = "${cardq}";

    public static final String ARDQ_TYPE = "core";

    private final AugmentationRuleField validField1 = AugmentationRuleField.builder()
            .output("outputField")
            .inputFields(List.of("inputField1", "inputField2"))
            .build();

    private final AugmentationRuleField validField2 = AugmentationRuleField.builder()
            .output("outputField2")
            .inputFields(List.of("inputField3", "inputField4"))
            .build();

    private final AugmentationRuleField validField3 = AugmentationRuleField.builder()
            .outputFields(List.of("outputField1", "outputField2"))
            .inputFields(List.of("inputField1", "inputField2"))
            .build();

    private final AugmentationRuleField validField4 = AugmentationRuleField.builder()
            .output("outputField1")
            .outputFields(List.of("outputField2", "outputField3"))
            .inputFields(List.of("inputField1", "inputField2"))
            .build();

    private final AugmentationRule validRule1 = AugmentationRule.builder()
            .inputSchemaReference("input|schema|reference")
            .fields(List.of(validField1))
            .build();

    private final AugmentationRule validRule2 = AugmentationRule.builder()
            .inputSchemaReference("dummy|schema|reference")
            .fields(List.of(validField2))
            .build();

    private final AugmentationRule validRule3 = AugmentationRule.builder()
            .inputSchemas(List.of("dummy|schema|reference1", "dummy|schema|reference2"))
            .fields(List.of(validField3))
            .build();

    private final AugmentationRule validRule4 = AugmentationRule.builder()
            .inputSchemaReference("dummy|schema|reference1")
            .inputSchemas(List.of("dummy|schema|reference2", "dummy|schema|reference3"))
            .fields(List.of(validField4))
            .build();

    private final AugmentationDefinition augDefinitionWithUrlReference = AugmentationDefinition.builder()
            .name(TEST_AUGMENTATION)
            .url(URL_REFERENCE)
            .augmentationRules(List.of(this.validRule1))
            .build();

    private final AugmentationDefinition augDefinitionWithUrl = AugmentationDefinition.builder()
            .name(TEST_AUGMENTATION)
            .augmentationRules(List.of(this.validRule1))
            .url(ARDQ_URL)
            .build();

    private final AugmentationDefinition augDefinitionWithUrlUpdate = AugmentationDefinition.builder()
            .name(TEST_AUGMENTATION)
            .augmentationRules(List.of(this.validRule1, this.validRule2))
            .url(ARDQ_URL)
            .build();

    private final AugmentationDefinition augDefinitionWithType = AugmentationDefinition.builder()
            .name(TEST_AUGMENTATION)
            .augmentationRules(List.of(this.validRule1))
            .type(ARDQ_TYPE)
            .url(ARDQ_URL)
            .build();

    private final AugmentationDefinition augDefinitionWithEmptyType = AugmentationDefinition.builder()
            .name(TEST_AUGMENTATION)
            .augmentationRules(List.of(this.validRule1))
            .type("  ")
            .url(ARDQ_URL)
            .build();

    private final AugmentationDefinition augDefWithSchemaListAndOutputFieldList = AugmentationDefinition.builder()
            .name(TEST_AUGMENTATION)
            .augmentationRules(List.of(validRule3))
            .type(ARDQ_TYPE)
            .url(ARDQ_URL)
            .build();

    private final AugmentationDefinition augDefWithAllSupportedFields = AugmentationDefinition.builder()
            .name(TEST_AUGMENTATION)
            .augmentationRules(List.of(validRule4))
            .type(ARDQ_TYPE)
            .url(ARDQ_URL)
            .build();

    private final AugmentationDefinition augDefinition1 = augDefinitionWithUrl;

    private final AugmentationDefinition augDefinition2 = AugmentationDefinition.builder()
            .name("DummyAug")
            .augmentationRules(List.of(validRule2))
            .url(ARDQ_URL)
            .build();

    private final ProfileDefinition profileDefinitionWithoutAugmentation = ProfileDefinition.builder()
            .name(VALID_NAME)
            .description(DESCRIPTION)
            .context(VALID_AGGREGATION_FIELDS)
            .kpis(VALID_LIST_KPI_REFS)
            .build();

    private final ProfileDefinition profileDefinitionWithAugmentation = ProfileDefinition.builder()
            .name(VALID_NAME)
            .description(DESCRIPTION)
            .context(VALID_AGGREGATION_FIELDS)
            .augmentation(TEST_AUGMENTATION)
            .kpis(VALID_LIST_KPI_REFS)
            .build();

    private final ProfileDefinition profileDefinition1 = profileDefinitionWithAugmentation;

    private final ProfileDefinition profileDefinition2 = ProfileDefinition.builder()
            .name("DummyProfile")
            .description(DESCRIPTION)
            .context(List.of("DummyAggField"))
            .augmentation("DummyAug")
            .kpis(List.of(KPIReference.builder().ref("DummyKpiRef").build()))
            .build();

    private final ProfileDefinition profileDefinition3 = ProfileDefinition.builder()
            .name("Test profile 3")
            .description("Profile to test multiple profiles referencing same augmentation")
            .context(VALID_AGGREGATION_FIELDS)
            .augmentation(TEST_AUGMENTATION)
            .kpis(VALID_LIST_KPI_REFS)
            .build();

    @Mock
    private AugmentationDefinitionDAO augmentationDefinitionDAO;

    @Mock
    private EffectiveAugmentationDAO effectiveAugmentationDAO;

    @Mock
    private AugmentationConfiguration augmentationConfiguration;

    @Mock
    private ConsistencyCheckHandler consistencyCheckHandler;

    @Mock
    private AugmentationProvisioningService provisioningService;

    private final FaultHandler faultHandler = new FaultHandler();

    private final AugmentationDiffCalculator calculator = new AugmentationDiffCalculator();

    private final Codec codec = new Codec();

    private ListAppender<ILoggingEvent> listAppender;

    @InjectMocks
    private LiveAugmentationHandler augmentationHandler;

    @BeforeEach
    void setup() {
        this.augmentationHandler = new LiveAugmentationHandler(augmentationDefinitionDAO,
                effectiveAugmentationDAO,
                augmentationConfiguration,
                faultHandler,
                calculator,
                provisioningService,
                codec,
                consistencyCheckHandler);

        final Logger logger = (Logger) LoggerFactory.getLogger(LiveAugmentationHandler.class);
        logger.setLevel(Level.INFO);
        this.listAppender = new ListAppender<>();
        this.listAppender.start();
        logger.addAppender(listAppender);
    }

    @Test
    void submit_successful_newDeployment() {
        final List<ProfileDefinition> pendingProfiles = new ArrayList<>();

        pendingProfiles.add(profileDefinitionWithAugmentation);

        when(this.augmentationDefinitionDAO.findAll())
                .thenReturn(List.of(augDefinitionWithUrl));

        this.augmentationHandler.submit(pendingProfiles);

        verify(this.provisioningService, times(1)).create(any());
        verify(this.provisioningService, times(0)).update(any());
        verify(this.provisioningService, times(0)).delete(any());

        final int listAppenderSize = this.listAppender.list.size();
        assertEquals("AAS provisioning completed successfully.", this.listAppender.list.get(listAppenderSize - 1).getMessage());
    }

    @Test
    void submit_successful_updateOneAugmentation() {
        final List<ProfileDefinition> pendingProfiles = new ArrayList<>();

        pendingProfiles.add(profileDefinitionWithAugmentation);

        when(this.augmentationDefinitionDAO.findAll())
                .thenReturn(List.of(augDefinitionWithUrlUpdate));
        when(this.effectiveAugmentationDAO.findAll())
                .thenReturn(List.of(augDefinitionWithUrl));

        this.augmentationHandler.submit(pendingProfiles);

        verify(this.provisioningService, times(0)).create(any());
        verify(this.provisioningService, times(1)).update(any());
        verify(this.provisioningService, times(0)).delete(any());

        final int listAppenderSize = this.listAppender.list.size();
        assertEquals("AAS provisioning completed successfully.", this.listAppender.list.get(listAppenderSize - 1).getMessage());
    }

    @Test
    void submit_successful_deleteOneAugmentation() {
        final List<ProfileDefinition> pendingProfiles = new ArrayList<>();

        pendingProfiles.add(profileDefinitionWithoutAugmentation);

        when(this.effectiveAugmentationDAO.findAll())
                .thenReturn(List.of(augDefinitionWithUrl));

        this.augmentationHandler.submit(pendingProfiles);

        verify(this.provisioningService, times(0)).create(any());
        verify(this.provisioningService, times(0)).update(any());
        verify(this.provisioningService, times(1)).delete(any());

        final int listAppenderSize = this.listAppender.list.size();
        assertEquals("AAS provisioning completed successfully.", this.listAppender.list.get(listAppenderSize - 1).getMessage());
    }

    @Test
    void submit_successful_upgrade() {

        // upgrade scenario:
        // - start with four profiles:  aug1, aug2, aug3, no aug
        // - add a profile:  aug3 create
        // - remove a profile: aug1 delete
        // - update aug2: update

        final ProfileDefinition profile1 = ProfileDefinition.builder()
                .name("profile1")
                .augmentation("aug1")
                .build();

        final ProfileDefinition profile1delete = ProfileDefinition.builder()
                .name("profile1")
                .build();

        final ProfileDefinition profile2 = ProfileDefinition.builder()
                .name("profile2")
                .augmentation("aug2")
                .build();

        final ProfileDefinition profile3 = ProfileDefinition.builder()
                .name("profile3")
                .augmentation("aug3")
                .build();

        final ProfileDefinition profile4 = ProfileDefinition.builder()
                .name("profile4")
                .build();

        final AugmentationDefinition aug1 = AugmentationDefinition.builder()
                .name("aug1")
                .url(ARDQ_URL)
                .augmentationRules(List.of(validRule1))
                .build();

        final AugmentationDefinition aug2 = AugmentationDefinition.builder()
                .name("aug2")
                .url(ARDQ_URL)
                .augmentationRules(List.of(validRule2))
                .build();

        final AugmentationDefinition aug2update = AugmentationDefinition.builder()
                .name("aug2")
                .url(ARDQ_URL)
                .augmentationRules(List.of(validRule2, validRule4))
                .build();

        final AugmentationDefinition aug3 = AugmentationDefinition.builder()
                .name("aug3")
                .url(ARDQ_URL)
                .augmentationRules(List.of(validRule3))
                .build();

        final List<ProfileDefinition> pendingProfiles = new ArrayList<>(List.of(profile1, profile2, profile3, profile4));

        when(this.augmentationDefinitionDAO.findAll())
                .thenReturn(List.of(aug1, aug2update));
        when(this.effectiveAugmentationDAO.findAll())
                .thenReturn(List.of(aug2, aug3));

        this.augmentationHandler.submit(pendingProfiles);

        verify(this.provisioningService, times(1)).create(any());
        verify(this.provisioningService, times(1)).update(any());
        verify(this.provisioningService, times(1)).delete(any());

        final int listAppenderSize = this.listAppender.list.size();
        assertEquals("AAS provisioning completed successfully.", this.listAppender.list.get(listAppenderSize - 1).getMessage());
    }

    @Test
    void submit_noEffectiveAugmentations_noAASProvisioning() {
        final List<ProfileDefinition> pendingProfiles = new ArrayList<>();

        pendingProfiles.add(profileDefinitionWithoutAugmentation);

        this.augmentationHandler.submit(pendingProfiles);

        verify(this.provisioningService, times(0)).create(any());
        verify(this.provisioningService, times(0)).update(any());
        verify(this.provisioningService, times(0)).delete(any());
    }

    @Test
    void submit_noEffectiveAugmentations_noPendingProfiles() {
        final List<ProfileDefinition> pendingProfiles = new ArrayList<>();

        this.augmentationHandler.submit(pendingProfiles);

        verify(this.provisioningService, times(0)).create(any());
        verify(this.provisioningService, times(0)).update(any());
        verify(this.provisioningService, times(0)).delete(any());
    }

    @Test
    void submit_noEffectiveAugmentationsInUpdatedProfile_deleteAugmentation() {
        final List<ProfileDefinition> pendingProfiles = new ArrayList<>();

        pendingProfiles.add(profileDefinitionWithoutAugmentation);

        when(this.augmentationDefinitionDAO.findAll())
                .thenReturn(List.of(augDefinitionWithUrl));
        when(this.effectiveAugmentationDAO.findAll()).thenReturn(List.of(augDefinition1));

        this.augmentationHandler.submit(pendingProfiles);

        verify(this.provisioningService, times(1)).delete(any());
        verify(this.effectiveAugmentationDAO, times(1)).delete(augDefinition1.getName());
    }

    @Test
    void getEffectiveAugmentations_profileWithAugmentation_returnEffectiveAugmentations() {
        final List<ProfileDefinition> pendingProfiles = new ArrayList<>();

        pendingProfiles.add(profileDefinitionWithAugmentation);
        when(this.augmentationDefinitionDAO.findAll())
                .thenReturn(List.of(augDefinitionWithUrl));

        final List<AugmentationDefinition> effectiveAugmentationList = this.augmentationHandler.getEffectiveAugmentations(pendingProfiles);

        assertEquals(1, effectiveAugmentationList.size());
        assertEquals(augDefinitionWithUrl, effectiveAugmentationList.get(0));
    }

    @Test
    void getEffectiveAugmentations_ProfilesWithSameAugmentation_returnOneEffectiveAugmentation() {

        final List<ProfileDefinition> pendingProfiles = List.of(profileDefinition1, profileDefinition3);
        when(this.augmentationDefinitionDAO.findAll())
                .thenReturn(List.of(augDefinitionWithUrl));

        final List<AugmentationDefinition> effectiveAugmentationList = this.augmentationHandler.getEffectiveAugmentations(pendingProfiles);

        assertEquals(1, effectiveAugmentationList.size());
    }

    @Test
    void getEffectiveAugmentations_ProfilesWithDifferentAugmentation_returnMultipleEffectiveAugmentations() {

        final List<ProfileDefinition> pendingProfiles = List.of(profileDefinition1, profileDefinition2);
        when(this.augmentationDefinitionDAO.findAll())
                .thenReturn(List.of(augDefinition1, augDefinition2));

        final List<AugmentationDefinition> effectiveAugmentationList = this.augmentationHandler.getEffectiveAugmentations(pendingProfiles);

        assertEquals(2, effectiveAugmentationList.size());

    }

    @Test
    void getEffectiveAugmentations_profileWithoutAugmentation_ReturnEmptyList() {
        final List<ProfileDefinition> pendingProfiles = new ArrayList<>();

        pendingProfiles.add(profileDefinitionWithoutAugmentation);
        when(this.augmentationDefinitionDAO.findAll())
                .thenReturn(List.of(augDefinitionWithUrl));

        final List<AugmentationDefinition> effectiveAugmentationList = this.augmentationHandler.getEffectiveAugmentations(pendingProfiles);

        assertEquals(0, effectiveAugmentationList.size());
    }

    @Test
    void getEffectiveAugmentations_noDictionaryAugmentationDefs_ReturnEmptyList() {
        final List<ProfileDefinition> pendingProfiles = List.of(profileDefinitionWithAugmentation, profileDefinitionWithoutAugmentation);

        when(this.augmentationDefinitionDAO.findAll())
                .thenReturn(new ArrayList<>());

        final List<AugmentationDefinition> effectiveAugmentationList = this.augmentationHandler.getEffectiveAugmentations(pendingProfiles);

        assertEquals(0, effectiveAugmentationList.size());
    }

    @Test
    void getEffectiveAugmentations_effectiveAugmentationWithUrlReference_shouldResolveURL() {
        final List<ProfileDefinition> pendingProfiles = new ArrayList<>();

        pendingProfiles.add(profileDefinitionWithAugmentation);
        when(this.augmentationDefinitionDAO.findAll())
                .thenReturn(List.of(augDefinitionWithUrlReference));
        when(this.augmentationConfiguration.getResolvedUrl(augDefinitionWithUrlReference.getUrl()))
                .thenReturn(ARDQ_URL);

        assertEquals(URL_REFERENCE, augDefinitionWithUrlReference.getUrl());

        final List<AugmentationDefinition> effectiveAugmentationList = this.augmentationHandler.getEffectiveAugmentations(pendingProfiles);

        assertEquals(1, effectiveAugmentationList.size());
        assertEquals(ARDQ_URL, effectiveAugmentationList.get(0).getUrl());
    }

    @Test
    void getEffectiveAugmentations_effectiveAugmentationWithUrlRefAndNoArdqUrlConfig_ThrowException() {
        final List<ProfileDefinition> pendingProfiles = new ArrayList<>();

        pendingProfiles.add(profileDefinitionWithAugmentation);
        when(this.augmentationDefinitionDAO.findAll())
                .thenReturn(List.of(augDefinitionWithUrlReference));
        when(this.augmentationConfiguration.getResolvedUrl(augDefinitionWithUrlReference.getUrl()))
                .thenThrow(CsacValidationException.class);

        assertThrows(CsacValidationException.class, () -> this.augmentationHandler.getEffectiveAugmentations(pendingProfiles));
    }

    @Test
    void createAugmentations_success() {
        final List<AugmentationDefinition> augmentationDefinitionList = new ArrayList<>();
        augmentationDefinitionList.add(augDefinition1);
        augmentationDefinitionList.add(augDefinition2);
        final List<ProfileDefinition> profileList = new ArrayList<>();
        profileList.add(profileDefinition1);
        profileList.add(profileDefinition2);

        this.augmentationHandler.createAugmentations(augmentationDefinitionList, profileList);

        verify(effectiveAugmentationDAO, times(1)).save(augDefinition1, List.of(profileDefinition1.getName()));
        verify(effectiveAugmentationDAO, times(1)).save(augDefinition2, List.of(profileDefinition2.getName()));
        //assert number of augmentations created from log messages.
        assertEquals(2, this.listAppender.list.get(0).getArgumentArray()[0]);
    }

    @Test
    void createAugmentations_noAugmentationDefinitions() {
        final List<AugmentationDefinition> augmentationDefinitionList = new ArrayList<>();
        final List<ProfileDefinition> profileList = new ArrayList<>();
        profileList.add(profileDefinition1);
        profileList.add(profileDefinition2);

        this.augmentationHandler.createAugmentations(augmentationDefinitionList, profileList);

        verify(effectiveAugmentationDAO, times(0)).save(any(), any());
        //assert number of augmentations created from log messages.
        assertEquals(0, this.listAppender.list.get(0).getArgumentArray()[0]);
    }

    @Test
    void createAugmentations_exception() {
        final List<AugmentationDefinition> augmentationDefinitionList = new ArrayList<>();
        augmentationDefinitionList.add(augDefinition1);
        augmentationDefinitionList.add(augDefinition2);
        final List<ProfileDefinition> profileList = new ArrayList<>();
        profileList.add(profileDefinition1);
        profileList.add(profileDefinition2);

        doThrow(new RuntimeException("test")).when(this.effectiveAugmentationDAO).save(any(), any());

        assertThrows(CsacConsistencyCheckException.class,
                () -> this.augmentationHandler.createAugmentations(augmentationDefinitionList, profileList));

        final ArgumentCaptor<ConsistencyCheckEvent.Payload> payloadArgumentCaptor = ArgumentCaptor.forClass(ConsistencyCheckEvent.Payload.class);
        verify(this.consistencyCheckHandler, times(1)).notifyCheckFailure(payloadArgumentCaptor.capture());
        assertEquals(ConsistencyCheckEvent.Payload.Type.SUSPECT, payloadArgumentCaptor.getValue().getType());
        assertEquals(1, payloadArgumentCaptor.getValue().getCount());
    }

    @Test
    void updateAugmentations_success() {
        final List<AugmentationDefinition> augmentationDefinitionList = new ArrayList<>();
        augmentationDefinitionList.add(augDefinition1);
        augmentationDefinitionList.add(augDefinition2);
        final List<ProfileDefinition> profileList = new ArrayList<>();
        profileList.add(profileDefinition1);
        profileList.add(profileDefinition2);

        this.augmentationHandler.updateAugmentations(augmentationDefinitionList, profileList);

        verify(effectiveAugmentationDAO, times(1)).save(augDefinition1, List.of(profileDefinition1.getName()));
        verify(effectiveAugmentationDAO, times(1)).save(augDefinition2, List.of(profileDefinition2.getName()));
        //assert number of augmentations created from log messages.
        assertEquals(2, this.listAppender.list.get(0).getArgumentArray()[0]);
    }

    @Test
    void updateAugmentations_noAugmentationDefinitions() {
        final List<AugmentationDefinition> augmentationDefinitionList = new ArrayList<>();
        final List<ProfileDefinition> profileList = new ArrayList<>();
        profileList.add(profileDefinition1);
        profileList.add(profileDefinition2);

        this.augmentationHandler.updateAugmentations(augmentationDefinitionList, profileList);

        verify(effectiveAugmentationDAO, times(0)).save(any(), any());
        //assert number of augmentations created from log messages.
        assertEquals(0, this.listAppender.list.get(0).getArgumentArray()[0]);
    }

    @Test
    void updateAugmentations_exception() {
        final List<AugmentationDefinition> augmentationDefinitionList = new ArrayList<>();
        augmentationDefinitionList.add(augDefinition1);
        augmentationDefinitionList.add(augDefinition2);
        final List<ProfileDefinition> profileList = new ArrayList<>();
        profileList.add(profileDefinition1);
        profileList.add(profileDefinition2);

        doThrow(new RuntimeException("test")).when(this.effectiveAugmentationDAO).save(any(), any());

        assertThrows(CsacConsistencyCheckException.class,
                () -> this.augmentationHandler.updateAugmentations(augmentationDefinitionList, profileList));

        final ArgumentCaptor<ConsistencyCheckEvent.Payload> payloadArgumentCaptor = ArgumentCaptor.forClass(ConsistencyCheckEvent.Payload.class);
        verify(this.consistencyCheckHandler, times(1)).notifyCheckFailure(payloadArgumentCaptor.capture());
        assertEquals(ConsistencyCheckEvent.Payload.Type.SUSPECT, payloadArgumentCaptor.getValue().getType());
        assertEquals(1, payloadArgumentCaptor.getValue().getCount());
    }

    @Test
    void deleteAugmentations_success() {
        final List<AugmentationDefinition> augmentationDefinitionList = new ArrayList<>();
        augmentationDefinitionList.add(augDefinition1);
        augmentationDefinitionList.add(augDefinition2);

        this.augmentationHandler.deleteAugmentations(augmentationDefinitionList);

        verify(effectiveAugmentationDAO, times(1)).delete(augDefinition1.getName());
        verify(effectiveAugmentationDAO, times(1)).delete(augDefinition2.getName());
        //assert number of augmentations created from log messages.
        assertEquals(2, this.listAppender.list.get(0).getArgumentArray()[0]);
    }

    @Test
    void deleteAugmentations_noAugmentationDefinitions() {
        final List<AugmentationDefinition> augmentationDefinitionList = new ArrayList<>();

        this.augmentationHandler.deleteAugmentations(augmentationDefinitionList);

        verify(effectiveAugmentationDAO, times(0)).delete(any());
        //assert number of augmentations created from log messages.
        assertEquals(0, this.listAppender.list.get(0).getArgumentArray()[0]);
    }

    @Test
    void deleteAugmentations_exception() {
        final List<AugmentationDefinition> augmentationDefinitionList = new ArrayList<>();
        augmentationDefinitionList.add(augDefinition1);
        augmentationDefinitionList.add(augDefinition2);

        doThrow(new RuntimeException("test")).when(this.effectiveAugmentationDAO).delete(any());

        assertThrows(CsacConsistencyCheckException.class,
                () -> this.augmentationHandler.deleteAugmentations(augmentationDefinitionList));

        final ArgumentCaptor<ConsistencyCheckEvent.Payload> payloadArgumentCaptor = ArgumentCaptor.forClass(ConsistencyCheckEvent.Payload.class);
        verify(this.consistencyCheckHandler, times(1)).notifyCheckFailure(payloadArgumentCaptor.capture());
        assertEquals(ConsistencyCheckEvent.Payload.Type.SUSPECT, payloadArgumentCaptor.getValue().getType());
        assertEquals(1, payloadArgumentCaptor.getValue().getCount());
    }

    @Test
    void transformDefinitionToDto_Test() {
        final AugmentationFieldRequestDto augmentationFieldRequestDto = AugmentationFieldRequestDto.builder()
                .input(this.validField1.getInputFields())
                .output(this.validField1.getOutput())
                .build();
        final AugmentationRuleRequestDto augmentationRuleRequestDto = AugmentationRuleRequestDto.builder()
                .inputSchema(this.validRule1.getInputSchemaReference())
                .fields(List.of(augmentationFieldRequestDto))
                .build();

        final AugmentationRequestDto augmentationRequestDto = AugmentationRequestDto.builder()
                .ardqId(augDefinition1.getName())
                .ardqUrl(augDefinition1.getUrl())
                .rules(List.of(augmentationRuleRequestDto))
                .build();

        assertEquals(augmentationRequestDto, this.augmentationHandler.mapDefinitionToDto(augDefinition1));

    }

    @Test
    void transformDefinitionToDto_withArdqTypeInDefinition() {
        final AugmentationFieldRequestDto augmentationFieldRequestDto = AugmentationFieldRequestDto.builder()
                .input(this.validField1.getInputFields())
                .output(this.validField1.getOutput())
                .build();
        final AugmentationRuleRequestDto augmentationRuleRequestDto = AugmentationRuleRequestDto.builder()
                .inputSchema(this.validRule1.getInputSchemaReference())
                .fields(List.of(augmentationFieldRequestDto))
                .build();

        final AugmentationRequestDto augmentationRequestDto = AugmentationRequestDto.builder()
                .ardqId(augDefinition1.getName())
                .ardqUrl(augDefinition1.getUrl())
                .ardqType(ARDQ_TYPE)
                .rules(List.of(augmentationRuleRequestDto))
                .build();

        assertEquals(augmentationRequestDto, this.augmentationHandler.mapDefinitionToDto(augDefinitionWithType));

    }

    @Test
    void transformDefinitionToDto_withEmptyArdqTypeInDefinition() {
        final AugmentationFieldRequestDto augmentationFieldRequestDto = AugmentationFieldRequestDto.builder()
                .input(this.validField1.getInputFields())
                .output(this.validField1.getOutput())
                .build();
        final AugmentationRuleRequestDto augmentationRuleRequestDto = AugmentationRuleRequestDto.builder()
                .inputSchema(this.validRule1.getInputSchemaReference())
                .fields(List.of(augmentationFieldRequestDto))
                .build();

        final AugmentationRequestDto augmentationRequestDto = AugmentationRequestDto.builder()
                .ardqId(augDefinition1.getName())
                .ardqUrl(augDefinition1.getUrl())
                .rules(List.of(augmentationRuleRequestDto))
                .build();

        assertEquals(augmentationRequestDto, this.augmentationHandler.mapDefinitionToDto(augDefinitionWithEmptyType));

    }

    @Test
    void transformDefinitionToDto_withInputSchemaListAndOutputFieldList() {

        final AugmentationFieldRequestDto augmentationFieldRequestDto1 = AugmentationFieldRequestDto.builder()
                .input(this.validField3.getInputFields())
                .output(this.validField3.getOutputFields().get(0))
                .build();

        final AugmentationFieldRequestDto augmentationFieldRequestDto2 = AugmentationFieldRequestDto.builder()
                .input(this.validField3.getInputFields())
                .output(this.validField3.getOutputFields().get(1))
                .build();

        final AugmentationRuleRequestDto augmentationRuleRequestDto1 = AugmentationRuleRequestDto.builder()
                .inputSchema(this.validRule3.getInputSchemas().get(0))
                .fields(List.of(augmentationFieldRequestDto1, augmentationFieldRequestDto2))
                .build();

        final AugmentationRuleRequestDto augmentationRuleRequestDto2 = AugmentationRuleRequestDto.builder()
                .inputSchema(this.validRule3.getInputSchemas().get(1))
                .fields(List.of(augmentationFieldRequestDto1, augmentationFieldRequestDto2))
                .build();

        final AugmentationRequestDto augmentationRequestDto = AugmentationRequestDto.builder()
                .ardqId(augDefWithSchemaListAndOutputFieldList.getName())
                .ardqUrl(augDefWithSchemaListAndOutputFieldList.getUrl())
                .ardqType(ARDQ_TYPE)
                .rules(List.of(augmentationRuleRequestDto1, augmentationRuleRequestDto2))
                .build();

        assertEquals(augmentationRequestDto, this.augmentationHandler.mapDefinitionToDto(augDefWithSchemaListAndOutputFieldList));
    }

    @Test
    void transformDefinitionToDto_withAllSupportedFields() {

        final AugmentationFieldRequestDto augmentationFieldRequestDto1 = AugmentationFieldRequestDto.builder()
                .input(this.validField4.getInputFields())
                .output(this.validField4.getOutput())
                .build();

        final AugmentationFieldRequestDto augmentationFieldRequestDto2 = AugmentationFieldRequestDto.builder()
                .input(this.validField4.getInputFields())
                .output(this.validField4.getOutputFields().get(0))
                .build();

        final AugmentationFieldRequestDto augmentationFieldRequestDto3 = AugmentationFieldRequestDto.builder()
                .input(this.validField4.getInputFields())
                .output(this.validField4.getOutputFields().get(1))
                .build();

        final AugmentationRuleRequestDto augmentationRuleRequestDto1 = AugmentationRuleRequestDto.builder()
                .inputSchema(this.validRule4.getInputSchemaReference())
                .fields(List.of(augmentationFieldRequestDto1, augmentationFieldRequestDto2, augmentationFieldRequestDto3))
                .build();

        final AugmentationRuleRequestDto augmentationRuleRequestDto2 = AugmentationRuleRequestDto.builder()
                .inputSchema(this.validRule4.getInputSchemas().get(0))
                .fields(List.of(augmentationFieldRequestDto1, augmentationFieldRequestDto2, augmentationFieldRequestDto3))
                .build();

        final AugmentationRuleRequestDto augmentationRuleRequestDto3 = AugmentationRuleRequestDto.builder()
                .inputSchema(this.validRule4.getInputSchemas().get(1))
                .fields(List.of(augmentationFieldRequestDto1, augmentationFieldRequestDto2, augmentationFieldRequestDto3))
                .build();

        final AugmentationRequestDto augmentationRequestDto = AugmentationRequestDto.builder()
                .ardqId(augDefWithAllSupportedFields.getName())
                .ardqUrl(augDefWithAllSupportedFields.getUrl())
                .ardqType(ARDQ_TYPE)
                .rules(List.of(augmentationRuleRequestDto1, augmentationRuleRequestDto2, augmentationRuleRequestDto3))
                .build();

        assertEquals(augmentationRequestDto, this.augmentationHandler.mapDefinitionToDto(augDefWithAllSupportedFields));
    }
}
