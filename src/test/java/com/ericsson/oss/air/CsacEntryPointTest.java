/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air;

import static com.ericsson.oss.air.csac.model.TestResourcesUtils.PM_COUNTER;
import static com.ericsson.oss.air.csac.model.TestResourcesUtils.PM_SCHEMA_NAME;
import static com.ericsson.oss.air.csac.model.TestResourcesUtils.PM_SCHEMA_URI;
import static com.ericsson.oss.air.csac.model.TestResourcesUtils.VALID_PM_SCHEMA_DEFINITION_PM_COUNTER;
import static com.ericsson.oss.air.util.logging.TestLoggingUtils.FACILITY_KEY;
import static com.ericsson.oss.air.util.logging.TestLoggingUtils.FACILITY_VALUE;
import static com.ericsson.oss.air.util.logging.TestLoggingUtils.SUBJECT_KEY;
import static com.ericsson.oss.air.util.logging.TestLoggingUtils.SUBJECT_VALUE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.ericsson.oss.air.csac.configuration.SchemaMigration;
import com.ericsson.oss.air.csac.handler.DataDictionaryHandler;
import com.ericsson.oss.air.csac.handler.ResourceSubmissionHandler;
import com.ericsson.oss.air.csac.handler.ServiceUpdateHandler;
import com.ericsson.oss.air.csac.handler.event.ConsistencyCheckHandler;
import com.ericsson.oss.air.csac.handler.status.ProvisioningTracker;
import com.ericsson.oss.air.csac.handler.validation.ValidationHandler;
import com.ericsson.oss.air.csac.model.PMDefinition;
import com.ericsson.oss.air.csac.model.ProfileDefinition;
import com.ericsson.oss.air.csac.model.ResourceSubmission;
import com.ericsson.oss.air.csac.service.DiffCalculator;
import com.ericsson.oss.air.csac.service.ResourceFileLoader;
import com.ericsson.oss.air.exception.CsacConsistencyCheckException;
import com.ericsson.oss.air.exception.CsacProvisioningStateTransitionException;
import com.ericsson.oss.air.exception.CsacValidationException;
import com.ericsson.oss.air.exception.ResourceFileLoaderException;
import com.ericsson.oss.air.util.logging.FaultHandler;
import com.ericsson.oss.air.util.operator.SequentialOperator;
import io.micrometer.core.instrument.Counter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.CannotGetJdbcConnectionException;

@ExtendWith(MockitoExtension.class)
class CsacEntryPointTest {

    @Mock
    private SchemaMigration schemaMigration;

    @Mock
    private ResourceFileLoader resourceFileLoader;

    @Mock
    private ValidationHandler validationHandler;

    @Mock
    private DataDictionaryHandler dataDictionaryHandler;

    @Mock
    private ServiceUpdateHandler serviceUpdateHandler;

    @Mock
    private DiffCalculator diffCalculator;

    @Mock
    private FaultHandler faultHandler;

    @Mock
    private ResourceSubmission resourceSubmission;

    @Mock
    private ResourceSubmissionHandler resourceSubmissionHandler;

    @Mock
    private Counter fileLoadErrorCounter;

    @Mock
    private ProvisioningTracker provisioningTracker;

    @Mock
    private ConsistencyCheckHandler consistencyCheckHandler;

    @Mock
    private SequentialOperator<Void> forcedProvisioningOperator;

    @InjectMocks
    @Spy
    private CsacEntryPoint csacEntryPoint;

    private Logger log;

    private ListAppender<ILoggingEvent> listAppender;

    @BeforeEach
    void setUp() {

        this.log = (Logger) LoggerFactory.getLogger(CsacEntryPoint.class);
        this.log.setLevel(Level.INFO);

        this.listAppender = new ListAppender<>();
        this.listAppender.start();

        this.log.addAppender(this.listAppender);
    }

    @AfterEach
    void tearDown() {
        this.listAppender.stop();
    }

    @Test
    void bootstrap_validResources_Valid() throws IOException {

        Path mockFilePath = Path.of("tmp/config/junit");
        when(resourceFileLoader.loadResourceFilePath(any())).thenReturn(resourceSubmission);
        when(resourceSubmission.hasPmSchemaDefs()).thenReturn(true);
        when(resourceSubmission.hasAugmentationDefs()).thenReturn(true);
        when(resourceSubmission.hasPmDefs()).thenReturn(true);
        when(resourceSubmission.hasKpiDefs()).thenReturn(true);
        when(resourceSubmission.hasProfileDefs()).thenReturn(true);
        when(resourceSubmissionHandler.getOrderedResourceList()).thenReturn(List.of(mockFilePath));

        csacEntryPoint.bootstrap();

        verify(validationHandler, times(1)).validateAppConfig();
        verify(schemaMigration, times(1)).migrate();
        verify(resourceFileLoader, times(1)).loadResourceFilePath(any());
        verify(validationHandler, times(1)).validateAugmentations(any());
        verify(validationHandler, times(1)).getValidPMDefinitions(any());
        verify(validationHandler, times(1)).validateKPIDefinitions(any());
        verify(validationHandler, times(1)).validateProfileDefinitions(any());
        verify(diffCalculator, times(1)).isChanged(any());
        verify(diffCalculator, times(1)).getAffectedProfiles(any());
        verify(dataDictionaryHandler, times(0)).insertPMDefinitions(any());
        verify(dataDictionaryHandler, times(0)).insertKPIDefinitions(any());
        verify(dataDictionaryHandler, times(0)).insertAugmentationDefinitions(any());
        verify(dataDictionaryHandler, times(0)).insertProfileDefinitions(any());
        verify(dataDictionaryHandler, times(0)).insertPMSchemaDefinitions(any());

        final List<ILoggingEvent> loggingEventList = this.listAppender.list;
        final boolean isPmSchemaLogMsgPresent = loggingEventList.stream()
                .anyMatch(event -> event.getFormattedMessage().equals("Discovered 0 PM Schema definitions from resource file tmp/config/junit"));
        assertTrue(isPmSchemaLogMsgPresent);

    }

    @Test
    void bootstrap_noPmSchemaDefs() throws IOException {

        Path mockFilePath = Path.of("tmp/config/junit");
        when(resourceFileLoader.loadResourceFilePath(any())).thenReturn(resourceSubmission);
        when(resourceSubmission.hasPmSchemaDefs()).thenReturn(false);
        when(resourceSubmission.hasAugmentationDefs()).thenReturn(true);
        when(resourceSubmission.hasPmDefs()).thenReturn(true);
        when(resourceSubmission.hasKpiDefs()).thenReturn(true);
        when(resourceSubmission.hasProfileDefs()).thenReturn(true);
        when(resourceSubmissionHandler.getOrderedResourceList()).thenReturn(List.of(mockFilePath));

        csacEntryPoint.bootstrap();

        verify(validationHandler, times(1)).validateAppConfig();
        verify(schemaMigration, times(1)).migrate();
        verify(resourceFileLoader, times(1)).loadResourceFilePath(any());
        verify(validationHandler, times(1)).validateAugmentations(any());
        verify(validationHandler, times(1)).getValidPMDefinitions(any());
        verify(validationHandler, times(1)).validateKPIDefinitions(any());
        verify(validationHandler, times(1)).validateProfileDefinitions(any());
        verify(diffCalculator, times(1)).isChanged(any());
        verify(diffCalculator, times(1)).getAffectedProfiles(any());
        verify(dataDictionaryHandler, times(0)).insertPMDefinitions(any());
        verify(dataDictionaryHandler, times(0)).insertKPIDefinitions(any());
        verify(dataDictionaryHandler, times(0)).insertAugmentationDefinitions(any());
        verify(dataDictionaryHandler, times(0)).insertProfileDefinitions(any());
        verify(dataDictionaryHandler, times(0)).insertPMSchemaDefinitions(any());

        final List<ILoggingEvent> loggingEventList = this.listAppender.list;
        final boolean isPmSchemaLogMsgPresent = loggingEventList.stream()
                .anyMatch(event -> event.getFormattedMessage().equals("Discovered 0 PM Schema definitions from resource file tmp/config/junit"));
        assertFalse(isPmSchemaLogMsgPresent);

    }

    @Test
    void testBootstrap_noAugmentationDefs() throws IOException {

        Path mockFilePath = Path.of("tmp/config/junit");
        when(resourceFileLoader.loadResourceFilePath(any())).thenReturn(resourceSubmission);
        when(resourceSubmission.hasPmSchemaDefs()).thenReturn(true);
        when(resourceSubmission.hasAugmentationDefs()).thenReturn(false);
        when(resourceSubmission.hasPmDefs()).thenReturn(true);
        when(resourceSubmission.hasKpiDefs()).thenReturn(true);
        when(resourceSubmission.hasProfileDefs()).thenReturn(true);
        when(resourceSubmissionHandler.getOrderedResourceList()).thenReturn(List.of(mockFilePath));

        csacEntryPoint.bootstrap();
        verify(validationHandler, times(1)).validateAppConfig();
        verify(schemaMigration, times(1)).migrate();
        verify(resourceFileLoader, times(1)).loadResourceFilePath(any());
        verify(validationHandler, times(0)).validateAugmentations(any());
        verify(validationHandler, times(1)).getValidPMDefinitions(any());
        verify(validationHandler, times(1)).validateKPIDefinitions(any());
        verify(validationHandler, times(1)).validateProfileDefinitions(any());
        verify(diffCalculator, times(1)).isChanged(any());
        verify(diffCalculator, times(1)).getAffectedProfiles(any());
        verify(dataDictionaryHandler, times(0)).insertPMDefinitions(any());
        verify(dataDictionaryHandler, times(0)).insertKPIDefinitions(any());
        verify(dataDictionaryHandler, times(0)).insertAugmentationDefinitions(any());
        verify(dataDictionaryHandler, times(0)).insertProfileDefinitions(any());
        verify(dataDictionaryHandler, times(0)).insertPMSchemaDefinitions(any());

    }

    @Test
    public void testBootstrap_NoPmDefs() throws Exception {

        Path mockFilePath = Path.of("tmp/config/junit");
        when(resourceFileLoader.loadResourceFilePath(any())).thenReturn(resourceSubmission);
        when(resourceSubmission.hasPmSchemaDefs()).thenReturn(true);
        when(resourceSubmission.hasAugmentationDefs()).thenReturn(true);
        when(resourceSubmission.hasPmDefs()).thenReturn(false);
        when(resourceSubmission.hasKpiDefs()).thenReturn(true);
        when(resourceSubmission.hasProfileDefs()).thenReturn(true);
        when(resourceSubmissionHandler.getOrderedResourceList()).thenReturn(List.of(mockFilePath));

        csacEntryPoint.bootstrap();
        verify(validationHandler, times(1)).validateAppConfig();
        verify(schemaMigration, times(1)).migrate();
        verify(resourceFileLoader, times(1)).loadResourceFilePath(any());
        verify(validationHandler, times(1)).validateAugmentations(any());
        verify(validationHandler, times(0)).getValidPMDefinitions(any());
        verify(validationHandler, times(1)).validateKPIDefinitions(any());
        verify(validationHandler, times(1)).validateProfileDefinitions(any());
        verify(diffCalculator, times(1)).isChanged(any());
        verify(diffCalculator, times(1)).getAffectedProfiles(any());
        verify(dataDictionaryHandler, times(0)).insertPMDefinitions(any());
        verify(dataDictionaryHandler, times(0)).insertKPIDefinitions(any());
        verify(dataDictionaryHandler, times(0)).insertAugmentationDefinitions(any());
        verify(dataDictionaryHandler, times(0)).insertProfileDefinitions(any());
        verify(dataDictionaryHandler, times(0)).insertPMSchemaDefinitions(any());

    }

    @Test
    public void testBootstrap_NoKpiDefs() throws IOException {

        Path mockFilePath = Path.of("tmp/config/junit");
        when(resourceFileLoader.loadResourceFilePath(any())).thenReturn(resourceSubmission);
        when(resourceSubmission.hasPmSchemaDefs()).thenReturn(true);
        when(resourceSubmission.hasAugmentationDefs()).thenReturn(true);
        when(resourceSubmission.hasPmDefs()).thenReturn(true);
        when(resourceSubmission.hasKpiDefs()).thenReturn(false);
        when(resourceSubmission.hasProfileDefs()).thenReturn(true);
        when(resourceSubmissionHandler.getOrderedResourceList()).thenReturn(List.of(mockFilePath));

        csacEntryPoint.bootstrap();
        verify(validationHandler, times(1)).validateAppConfig();
        verify(schemaMigration, times(1)).migrate();
        verify(resourceFileLoader, times(1)).loadResourceFilePath(any());
        verify(validationHandler, times(1)).validateAugmentations(any());
        verify(validationHandler, times(1)).getValidPMDefinitions(any());
        verify(validationHandler, times(0)).validateKPIDefinitions(any());
        verify(validationHandler, times(1)).validateProfileDefinitions(any());
        verify(diffCalculator, times(1)).isChanged(any());
        verify(diffCalculator, times(1)).getAffectedProfiles(any());
        verify(dataDictionaryHandler, times(0)).insertPMDefinitions(any());
        verify(dataDictionaryHandler, times(0)).insertKPIDefinitions(any());
        verify(dataDictionaryHandler, times(0)).insertAugmentationDefinitions(any());
        verify(dataDictionaryHandler, times(0)).insertProfileDefinitions(any());
        verify(dataDictionaryHandler, times(0)).insertPMSchemaDefinitions(any());

    }

    @Test
    public void testBootstrap_NoProfileDefs() throws Exception {

        Path mockFilePath = Path.of("tmp/config/junit");
        when(resourceFileLoader.loadResourceFilePath(any())).thenReturn(resourceSubmission);
        when(resourceSubmission.hasPmSchemaDefs()).thenReturn(true);
        when(resourceSubmission.hasAugmentationDefs()).thenReturn(true);
        when(resourceSubmission.hasPmDefs()).thenReturn(true);
        when(resourceSubmission.hasKpiDefs()).thenReturn(true);
        when(resourceSubmission.hasProfileDefs()).thenReturn(false);
        when(resourceSubmissionHandler.getOrderedResourceList()).thenReturn(List.of(mockFilePath));

        csacEntryPoint.bootstrap();
        verify(validationHandler, times(1)).validateAppConfig();
        verify(schemaMigration, times(1)).migrate();
        verify(resourceFileLoader, times(1)).loadResourceFilePath(any());
        verify(validationHandler, times(1)).validateAugmentations(any());
        verify(validationHandler, times(1)).getValidPMDefinitions(any());
        verify(validationHandler, times(1)).validateKPIDefinitions(any());
        verify(validationHandler, times(0)).validateProfileDefinitions(any());
        verify(diffCalculator, times(1)).isChanged(any());
        verify(diffCalculator, times(1)).getAffectedProfiles(any());
        verify(dataDictionaryHandler, times(0)).insertPMDefinitions(any());
        verify(dataDictionaryHandler, times(0)).insertKPIDefinitions(any());
        verify(dataDictionaryHandler, times(0)).insertAugmentationDefinitions(any());
        verify(dataDictionaryHandler, times(0)).insertProfileDefinitions(any());
        verify(dataDictionaryHandler, times(0)).insertPMSchemaDefinitions(any());

    }

    @Test
    void Bootstrap_With_Profile_Change() throws IOException {

        Path mockFilePath = Path.of("tmp/config/junit");
        when(resourceSubmission.hasPmSchemaDefs()).thenReturn(true);
        when(resourceSubmission.hasAugmentationDefs()).thenReturn(true);
        when(resourceSubmission.hasPmDefs()).thenReturn(true);
        when(resourceSubmission.hasKpiDefs()).thenReturn(true);
        when(resourceSubmission.hasProfileDefs()).thenReturn(true);
        when(diffCalculator.isChanged(any())).thenReturn(true);
        when(resourceFileLoader.loadResourceFilePath(any())).thenReturn(resourceSubmission);
        when(resourceSubmissionHandler.getOrderedResourceList()).thenReturn(List.of(mockFilePath));

        csacEntryPoint.bootstrap();
        verify(validationHandler, times(1)).validateAppConfig();
        verify(schemaMigration, times(1)).migrate();
        verify(validationHandler, times(1)).validateAugmentations(any());
        verify(validationHandler, times(1)).getValidPMDefinitions(any());
        verify(validationHandler, times(1)).validateKPIDefinitions(any());
        verify(validationHandler, times(1)).validateProfileDefinitions(any());
        verify(diffCalculator, times(1)).isChanged(any());
        verify(diffCalculator, times(1)).getAffectedProfiles(any());
        verify(dataDictionaryHandler, times(1)).insertPMDefinitions(any());
        verify(dataDictionaryHandler, times(1)).insertKPIDefinitions(any());
        verify(dataDictionaryHandler, times(1)).insertAugmentationDefinitions(any());
        verify(dataDictionaryHandler, times(1)).insertProfileDefinitions(any());
        verify(dataDictionaryHandler, times(1)).insertPMSchemaDefinitions(any());
        verify(serviceUpdateHandler, times(1)).notify(any(), any());
    }

    @Test
    void bootstrap_InvalidConfigFile_Valid() throws IOException {

        final Path mockFilePath = Path.of("tmp/config/junit");
        when(this.resourceFileLoader.loadResourceFilePath(any())).thenThrow(ResourceFileLoaderException.class);
        when(this.resourceSubmissionHandler.getOrderedResourceList()).thenReturn(List.of(mockFilePath));

        this.csacEntryPoint.setFileLoadErrorCounter(this.fileLoadErrorCounter);
        this.csacEntryPoint.bootstrap();

        verify(this.fileLoadErrorCounter, times(1)).increment();
        verify(this.faultHandler, times(1)).fatal(any());
    }

    @Test
    void bootstrap_invalidAppConfig_ThrowsRuntimeException() throws IOException {

        doThrow(CsacValidationException.class).when(this.validationHandler).validateAppConfig();

        csacEntryPoint.bootstrap();

        verify(faultHandler, times(1)).fatal(any());
    }

    @Test
    void bootstrap_multipleValidResources_Valid() throws IOException {

        Path mockFilePath_1 = Path.of("tmp/config/oob");
        Path mockFilePath_2 = Path.of("tmp/config/junit");
        when(resourceFileLoader.loadResourceFilePath(any())).thenReturn(resourceSubmission);
        when(resourceSubmission.hasAugmentationDefs()).thenReturn(true);
        when(resourceSubmission.hasPmDefs()).thenReturn(true);
        when(resourceSubmission.hasKpiDefs()).thenReturn(true);
        when(resourceSubmission.hasProfileDefs()).thenReturn(true);
        when(resourceSubmissionHandler.getOrderedResourceList()).thenReturn(List.of(mockFilePath_1, mockFilePath_2));
        when(diffCalculator.isChanged(any())).thenReturn(true);

        csacEntryPoint.bootstrap();

        verify(validationHandler, times(1)).validateAppConfig();
        verify(schemaMigration, times(1)).migrate();
        verify(resourceFileLoader, times(2)).loadResourceFilePath(any());
        verify(validationHandler, times(2)).validateAugmentations(any());
        verify(validationHandler, times(2)).getValidPMDefinitions(any());
        verify(validationHandler, times(2)).validateKPIDefinitions(any());
        verify(validationHandler, times(2)).validateProfileDefinitions(any());
        verify(diffCalculator, times(1)).isChanged(any());
        verify(diffCalculator, times(1)).getAffectedProfiles(any());
        verify(dataDictionaryHandler, times(1)).insertPMDefinitions(any());
        verify(dataDictionaryHandler, times(1)).insertKPIDefinitions(any());
        verify(dataDictionaryHandler, times(1)).insertAugmentationDefinitions(any());
        verify(dataDictionaryHandler, times(1)).insertProfileDefinitions(any());
        verify(provisioningTracker, times(1)).startProvisioning();
        verify(provisioningTracker, times(1)).stopProvisioning();
        verify(forcedProvisioningOperator, times(0)).apply(any());
    }

    @Test
    void csacEntryPoint_mergePmDefs() throws Exception {

        final PMDefinition def1 = PMDefinition.builder()
                .name("def1")
                .description("Def 1")
                .source("EBSN|PM_COUNTERS|PM_SCHEMA_1")
                .build();

        final PMDefinition def2 = PMDefinition.builder()
                .name("def2")
                .description("Def 2")
                .source("EBSN|PM_COUNTERS|PM_SCHEMA_1")
                .build();

        final PMDefinition def3 = PMDefinition.builder()
                .name("def3")
                .description("Def 3")
                .source("EBSN|PM_COUNTERS|PM_SCHEMA_1")
                .build();

        final PMDefinition def1_1 = PMDefinition.builder()
                .name("def1")
                .description("Def 1-1")
                .source("EBSN|PM_COUNTERS|PM_SCHEMA_1")
                .build();

        final PMDefinition def2_1 = PMDefinition.builder()
                .name("def2")
                .description("Def 2-1")
                .source("EBSN|PM_COUNTERS|PM_SCHEMA_1")
                .build();

        final PMDefinition def4 = PMDefinition.builder()
                .name("def4")
                .description("Def 4")
                .source("EBSN|PM_COUNTERS|PM_SCHEMA_1")
                .build();

        final List<PMDefinition> pmDefs1 = new ArrayList<>(List.of(def1, def2, def3));
        final List<PMDefinition> pmDefs2 = new ArrayList<>(List.of(def1_1, def2_1, def4));

        final ResourceSubmission sub1 = ResourceSubmission.builder()
                .pmDefs(pmDefs1)
                .build();

        final ResourceSubmission sub2 = ResourceSubmission.builder()
                .pmDefs(pmDefs2)
                .build();

        final Map<String, List<PMDefinition>> validPms1 = new HashMap<>();
        validPms1.put("PM_SCHEMA_1", pmDefs1);

        final Map<String, List<PMDefinition>> validPms2 = new HashMap<>();
        validPms2.put("PM_SCHEMA_1", pmDefs2);

        when(resourceFileLoader.loadResourceFilePath(any())).thenReturn(sub1, sub2);
        when(resourceSubmissionHandler.getOrderedResourceList()).thenReturn(List.of(Path.of("path1"), Path.of("path2")));
        when(validationHandler.getValidPMDefinitions(any())).thenReturn(validPms1, validPms2);

        this.csacEntryPoint.startCsacDataFlow();

        assertFalse(this.csacEntryPoint.getValidPMDefinitions().isEmpty());

        final Set<PMDefinition> actual = new TreeSet<>((a, b) -> a.getName().compareTo(b.getName()));
        actual.addAll(this.csacEntryPoint.getValidPMDefinitions().get("PM_SCHEMA_1"));
        assertTrue(actual.contains(def1_1));
        assertEquals(def1_1.getDescription(), actual.stream().filter(d -> d.getName().equals(def1.getName())).findFirst().get().getDescription());
        assertTrue(actual.contains(def4));
    }

    @Test
    void startProvisioning_trackingErrorOnStarting() throws Exception {

        when(this.diffCalculator.getAffectedProfiles(any())).thenReturn(Set.of(new ProfileDefinition()));

        doThrow(new CsacProvisioningStateTransitionException()).when(this.provisioningTracker).startProvisioning();

        assertThrows(CsacConsistencyCheckException.class, () -> this.csacEntryPoint.startProvisioning());

        verify(this.csacEntryPoint, times(1)).startCsacDataFlow();
        verify(this.provisioningTracker, times(1)).startProvisioning();
        verify(this.provisioningTracker, times(0)).stopProvisioning();
        verify(this.provisioningTracker, times(1)).stopProvisioning(any(CsacProvisioningStateTransitionException.class));
        verify(this.consistencyCheckHandler, times(1)).notifyCheckFailure(any());
    }

    @Test
    void startProvisioning_AuditableDatabaseException() throws Exception {

        final Path mockFilePath = Path.of("tmp/config/junit");
        when(this.resourceFileLoader.loadResourceFilePath(any())).thenReturn(resourceSubmission);
        when(this.resourceSubmission.hasAugmentationDefs()).thenReturn(true);
        when(this.resourceSubmission.hasPmDefs()).thenReturn(true);
        when(this.resourceSubmission.hasKpiDefs()).thenReturn(true);
        when(this.resourceSubmission.hasProfileDefs()).thenReturn(true);
        when(this.resourceSubmissionHandler.getOrderedResourceList()).thenReturn(List.of(mockFilePath));
        doThrow(CannotGetJdbcConnectionException.class).when(this.validationHandler).validateKPIDefinitions(any());

        assertThrows(CannotGetJdbcConnectionException.class, () -> this.csacEntryPoint.startProvisioning());

        verify(this.resourceFileLoader, times(1)).loadResourceFilePath(any());
        verify(this.validationHandler, times(1)).validateAugmentations(any());
        verify(this.validationHandler, times(1)).getValidPMDefinitions(any());
        verify(this.validationHandler, times(1)).validateKPIDefinitions(any());
        verify(this.validationHandler, never()).validateProfileDefinitions(any());
        verify(this.provisioningTracker, times(1)).stopProvisioning(any(CannotGetJdbcConnectionException.class));

        final List<ILoggingEvent> loggingEventList = this.listAppender.list;

        assertFalse(loggingEventList.isEmpty());

        for (final ILoggingEvent loggingEvent : loggingEventList.subList(0, 2)) {
            assertTrue(loggingEvent.getMDCPropertyMap().isEmpty());
        }

        final ILoggingEvent auditEvent = this.listAppender.list.get(3);

        assertEquals(Level.ERROR, auditEvent.getLevel());
        assertEquals("Cannot connect to database: ", auditEvent.getFormattedMessage());
        assertNotNull(auditEvent.getThrowableProxy());

        final Map<String, String> mdcPropMap = auditEvent.getMDCPropertyMap();

        assertFalse(mdcPropMap.isEmpty());
        assertEquals(2, mdcPropMap.size());
        assertEquals(FACILITY_VALUE, mdcPropMap.get(FACILITY_KEY));
        assertEquals(SUBJECT_VALUE, mdcPropMap.get(SUBJECT_KEY));
    }

    @Test
    void startProvisioning_NotAuditableException() throws Exception {

        final Path mockFilePath = Path.of("tmp/config/junit");
        when(this.resourceFileLoader.loadResourceFilePath(any())).thenReturn(resourceSubmission);
        when(this.resourceSubmission.hasAugmentationDefs()).thenReturn(true);
        when(this.resourceSubmission.hasPmDefs()).thenReturn(true);
        when(this.resourceSubmission.hasKpiDefs()).thenReturn(true);
        when(this.resourceSubmission.hasProfileDefs()).thenReturn(true);
        when(this.resourceSubmissionHandler.getOrderedResourceList()).thenReturn(List.of(mockFilePath));
        doThrow(CsacValidationException.class).when(this.validationHandler).validateKPIDefinitions(any());

        assertThrows(CsacValidationException.class, () -> this.csacEntryPoint.startProvisioning());

        verify(this.resourceFileLoader, times(1)).loadResourceFilePath(any());
        verify(this.validationHandler, times(1)).validateAugmentations(any());
        verify(this.validationHandler, times(1)).getValidPMDefinitions(any());
        verify(this.validationHandler, times(1)).validateKPIDefinitions(any());
        verify(this.validationHandler, never()).validateProfileDefinitions(any());
        verify(this.provisioningTracker, times(1)).stopProvisioning(any(CsacValidationException.class));

        final List<ILoggingEvent> loggingEventList = this.listAppender.list;

        assertFalse(loggingEventList.isEmpty());

        for (final ILoggingEvent loggingEvent : loggingEventList) {
            assertTrue(loggingEvent.getMDCPropertyMap().isEmpty());
        }
    }

    @Test
    void startCsacDataFlow_noNewResources() throws IOException {

        Path mockFilePath = Path.of("tmp/config/junit");
        when(resourceFileLoader.loadResourceFilePath(any())).thenReturn(resourceSubmission);
        when(resourceSubmission.hasAugmentationDefs()).thenReturn(true);
        when(resourceSubmission.hasPmDefs()).thenReturn(true);
        when(resourceSubmission.hasKpiDefs()).thenReturn(true);
        when(resourceSubmission.hasProfileDefs()).thenReturn(true);
        when(diffCalculator.isChanged(any())).thenReturn(false);
        when(diffCalculator.getAffectedProfiles(any())).thenReturn(Set.of());
        when(resourceSubmissionHandler.getOrderedResourceList()).thenReturn(List.of(mockFilePath));

        csacEntryPoint.startCsacDataFlow();

        verify(resourceFileLoader, times(1)).loadResourceFilePath(any());
        verify(validationHandler, times(1)).validateAugmentations(any());
        verify(validationHandler, times(1)).getValidPMDefinitions(any());
        verify(validationHandler, times(1)).validateKPIDefinitions(any());
        verify(validationHandler, times(1)).validateProfileDefinitions(any());
        verify(diffCalculator, times(1)).isChanged(any());
        verify(diffCalculator, times(1)).getAffectedProfiles(any());
        verify(dataDictionaryHandler, times(0)).insertPMDefinitions(any());
        verify(dataDictionaryHandler, times(0)).insertKPIDefinitions(any());
        verify(dataDictionaryHandler, times(0)).insertAugmentationDefinitions(any());
        verify(dataDictionaryHandler, times(0)).insertProfileDefinitions(any());
        verify(provisioningTracker, times(0)).startProvisioning();
        verify(provisioningTracker, times(0)).stopProvisioning();
        verify(forcedProvisioningOperator, times(1)).apply(any());
    }

    @Test
    void startCsacDataFlow_addPmCountersToPmDefList() throws Exception {

        final PMDefinition def1 = PM_COUNTER.toPmDefinition(PM_SCHEMA_URI);

        final PMDefinition def2 = PMDefinition.builder()
                .name("def2")
                .description("Def 2")
                .source(PM_SCHEMA_URI.getSchemeSpecificPart())
                .build();

        final ResourceSubmission sub1 = ResourceSubmission.builder()
                .pmSchemaDefs(List.of(VALID_PM_SCHEMA_DEFINITION_PM_COUNTER))
                .build();

        final ResourceSubmission sub2 = ResourceSubmission.builder()
                .pmDefs(List.of(def2))
                .build();

        final Map<String, List<PMDefinition>> validPmDefs1 = new HashMap<>();
        validPmDefs1.put(PM_SCHEMA_NAME, List.of(def1));

        final Map<String, List<PMDefinition>> validPmDefs2 = new HashMap<>();
        validPmDefs2.put(PM_SCHEMA_NAME, List.of(def2));

        when(resourceFileLoader.loadResourceFilePath(any())).thenReturn(sub1, sub2);
        when(resourceSubmissionHandler.getOrderedResourceList()).thenReturn(List.of(Path.of("path1"), Path.of("path2")));
        when(validationHandler.getValidPMDefinitions(any())).thenReturn(validPmDefs1, validPmDefs2);

        this.csacEntryPoint.startCsacDataFlow();

        assertFalse(this.csacEntryPoint.getValidPMDefinitions().isEmpty());
        assertEquals(1, this.csacEntryPoint.getValidPMDefinitions().size());

        final List<PMDefinition> actualPmDefs = this.csacEntryPoint.getValidPMDefinitions().get(PM_SCHEMA_NAME);

        assertEquals(2, actualPmDefs.size());
        assertTrue(actualPmDefs.contains(def1));
        assertTrue(actualPmDefs.contains(def2));
    }
}
