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

import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.ericsson.oss.air.csac.configuration.SchemaMigration;
import com.ericsson.oss.air.csac.configuration.metrics.CustomMetricsRegistry;
import com.ericsson.oss.air.csac.handler.DataDictionaryHandler;
import com.ericsson.oss.air.csac.handler.ResourceSubmissionHandler;
import com.ericsson.oss.air.csac.handler.ServiceUpdateHandler;
import com.ericsson.oss.air.csac.handler.event.ConsistencyCheckEvent;
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
import com.ericsson.oss.air.util.LazySupplier;
import com.ericsson.oss.air.util.logging.FaultHandler;
import com.ericsson.oss.air.util.logging.audit.AuditLogFactory;
import com.ericsson.oss.air.util.logging.audit.AuditLogger;
import com.ericsson.oss.air.util.operator.SequentialOperator;
import io.micrometer.core.instrument.Counter;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * The class provides the entry point for all CSAC functionality executed after the application has started.  This includes
 *
 * <ul>
 * <li>Database schema migration</li>
 * <li>Loading and validating Assurance resources</li>
 * <li>Provisioning downstream services</li>
 * </ul>
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class CsacEntryPoint {

    private static final AuditLogger AUDIT_LOGGER = AuditLogFactory.getLogger(CsacEntryPoint.class);

    @Setter(AccessLevel.PACKAGE) // used only for unit tests
    private Counter fileLoadErrorCounter = CustomMetricsRegistry.registerResourceFileLoadErrorCount();

    private final ResourceFileLoader resourceFileLoader;

    private final ResourceSubmissionHandler resourceSubmissionHandler;

    private final ValidationHandler validationHandler;

    private final DataDictionaryHandler dictionaryHandler;

    private final DiffCalculator diffCalculator;

    private final ServiceUpdateHandler serviceUpdateHandler;

    private final FaultHandler faultHandler;

    private final SchemaMigration schemaMigration;

    private final ProvisioningTracker provisioningTracker;

    private final ConsistencyCheckHandler consistencyCheckHandler;

    private final SequentialOperator<Void> forcedProvisioningOperator;

    private final LazySupplier<Map<String, List<PMDefinition>>> validPMDefinitions = new LazySupplier<Map<String, List<PMDefinition>>>() {

        @Override
        protected Map<String, List<PMDefinition>> initialize() {
            return new HashMap<>();
        }
    };

    /**
     * Bootstraps the CSAC microservice.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void bootstrap() {

        try {
            log.info("Validating application config");
            this.validationHandler.validateAppConfig();

            // create schemas for csac.
            this.schemaMigration.migrate();

            // kick off the provisioning operation
            startProvisioning();
        } catch (final Exception e) {
            this.faultHandler.fatal(e);
        }
    }

    /**
     * Wrapper method for the provisioning operation.
     *
     * @throws IOException if the exception occurs during resource file loading.
     */
    public void startProvisioning() throws IOException {

        try {

            this.startCsacDataFlow();
        } catch (final CsacProvisioningStateTransitionException pste) {

            // a state transition failure during provisioning may indicate an inconsistency in the provisioned configuration.
            this.consistencyCheckHandler.notifyCheckFailure(new ConsistencyCheckEvent.Payload(ConsistencyCheckEvent.Payload.Type.SUSPECT, 1));
            this.provisioningTracker.stopProvisioning(pste);
            throw new CsacConsistencyCheckException(pste);
        } catch (final Exception e) {

            AUDIT_LOGGER.error(e);

            this.provisioningTracker.stopProvisioning(e);
            throw e;
        }
    }

    /**
     * Starts the CSAC data flow that:
     *
     * <ol>
     * <li>initializes the database</li>
     * <li>validates and loads the resource file(s) from disk</li>
     * <li>incrementally aggregates the submitted resources</li>
     * <li>incrementally validates the submitted resources</li>
     * <li>persists the validated resources</li>
     * <li>provisions the resources to downstream components</li>
     * </ol>
     * <p>
     * NOTE: if there are any validation violations or if there is no difference between the submitted and the existing
     * resources, then the entire flow ends.
     */
    void startCsacDataFlow() throws IOException {

        // reset the valid PM definition supplier in the event that this is a re-entry.
        this.validPMDefinitions.reset();

        final Instant csacStartTime = Instant.now();

        final ResourceSubmission masterResourceSubmission = new ResourceSubmission();
        final List<Path> validResourceFilePaths;

        try {
            validResourceFilePaths = this.resourceSubmissionHandler.getOrderedResourceList();
        } catch (final Exception e) {
            fileLoadErrorCounter.increment();
            throw e;
        }

        for (final Path resourceFile : validResourceFilePaths) {
            final ResourceSubmission newResourceSubmission;

            try {
                newResourceSubmission = this.resourceFileLoader.loadResourceFilePath(resourceFile);
            } catch (final Exception e) {
                fileLoadErrorCounter.increment();
                throw e;
            }

            validateNewResourceSubmission(masterResourceSubmission, newResourceSubmission, resourceFile);

        }

        final boolean dictionaryUpdated = updateDictionaryResources(masterResourceSubmission);

        // Calculate affected profiles
        final Set<ProfileDefinition> pendingProfiles = this.diffCalculator.getAffectedProfiles(masterResourceSubmission);

        if (!dictionaryUpdated && pendingProfiles.isEmpty()) {
            log.info("No runtime resource changes detected.");

            this.forcedProvisioningOperator.apply(null);

            return;
        }

        this.provisioningTracker.startProvisioning();

        // individual provisioning handlers are responsible for determining exactly what needs to be provisioned.
        this.serviceUpdateHandler.notify(masterResourceSubmission.getProfileDefs(), csacStartTime);
        this.provisioningTracker.stopProvisioning();
    }

    private void validateNewResourceSubmission(final ResourceSubmission masterResourceSubmission, final ResourceSubmission newResourceSubmission,
                                               final Path resourceFile) {

        // log presence of PM Schema definitions if there are any.
        if (newResourceSubmission.hasPmSchemaDefs()) {
            log.info("Discovered {} PM Schema definitions from resource file {}",
                    newResourceSubmission.getPmSchemaDefs().size(), resourceFile.toString());
        }

        // validate AugmentationDefinition(s) in the master resource submission.
        if (newResourceSubmission.hasAugmentationDefs()) {
            log.info("Validating {} augmentation definitions from resource file {}",
                    newResourceSubmission.getAugmentationDefinitions().size(),
                    resourceFile.toString());
            this.validationHandler.validateAugmentations(newResourceSubmission);
        }

        // add PMSchemaDefinition.PMCounters to newResourceSubmission's list of PMDefinitions
        // PMDefinitions take precedence over PMCounters
        newResourceSubmission.addPmCountersToPmDefList();

        // validate PMDefinitions in the new resource submission
        if (newResourceSubmission.hasPmDefs()) {
            log.info("Validating {} PM definitions from resource file {}", newResourceSubmission.getPmDefs().size(), resourceFile.toString());
            updateValidPmDefinitions(newResourceSubmission);
        }

        // merge the subsequent resource submission onto the master resource submission
        masterResourceSubmission.mergeResourceSubmission(newResourceSubmission);

        // validate KPIDefinitions in the master resource submission
        if (newResourceSubmission.hasKpiDefs()) {
            log.info("Validating {} KPI definitions from resource file {}", newResourceSubmission.getKpiDefs().size(), resourceFile.toString());
            this.validationHandler.validateKPIDefinitions(masterResourceSubmission);
        }

        // validate ProfileDefinitions in the master resource submission
        if (newResourceSubmission.hasProfileDefs()) {
            log.info("Validating {} profile definitions from resource file {}", newResourceSubmission.getProfileDefs().size(),
                    resourceFile.toString());
            this.validationHandler.validateProfileDefinitions(masterResourceSubmission);
        }
    }

    private boolean updateDictionaryResources(final ResourceSubmission resourceSubmission) {

        if (!this.diffCalculator.isChanged(resourceSubmission)) {
            log.info("No dictionary resource changes found. Skipping dictionary updates.");
            return false;
        } else {

            // insert valid PM Schema definitions into Data Dictionary
            log.info("Updating PM Schema definitions in data dictionary");
            this.dictionaryHandler.insertPMSchemaDefinitions(resourceSubmission.getPmSchemaDefs());

            // insert validated PMDefinitions into Data Dictionary
            log.info("Updating PM definitions in data dictionary");
            this.dictionaryHandler.insertPMDefinitions(this.validPMDefinitions.get());

            // insert valid augmentation definitions into Data Dictionary
            log.info("Updating augmentation definitions in data dictionary");
            this.dictionaryHandler.insertAugmentationDefinitions(resourceSubmission.getAugmentationDefinitions());

            // insert validated KPIDefinitions into Data Dictionary
            log.info("Updating KPI definitions in data dictionary");
            this.dictionaryHandler.insertKPIDefinitions(resourceSubmission.getKpiDefs());

            //insert validated profile definitions into Data Dictionary
            log.info("Updating profile definitions in data dictionary");
            this.dictionaryHandler.insertProfileDefinitions(resourceSubmission.getProfileDefs());
            return true;
        }
    }

    /*
     * (non-javadoc)
     *
     * Updates the valid PM definitions map with valid definitions from the current partial ResourceSubmission.
     */
    private void updateValidPmDefinitions(final ResourceSubmission newResourceSubmission) {

        final Map<String, List<PMDefinition>> validPms = this.validationHandler.getValidPMDefinitions(newResourceSubmission.getPmDefs());

        // need to merge the valid PMs from this submission with the master list of validated PMs
        for (final Map.Entry<String, List<PMDefinition>> validPmEntry : validPms.entrySet()) {
            if (this.validPMDefinitions.get().containsKey(validPmEntry.getKey())) {

                // ensure that the valid PMs for the current schema contain any previously validated PMs, as well as
                // updates to previously validated PMs and any newly added PMs
                final List<PMDefinition> mergedPmList = mergeLists(validPmEntry.getValue(),
                        this.validPMDefinitions.get().get(validPmEntry.getKey()));
                this.validPMDefinitions.get().put(validPmEntry.getKey(), mergedPmList);
            } else {
                this.validPMDefinitions.get().put(validPmEntry.getKey(), validPmEntry.getValue());
            }
        }
    }

    /*
     * For unit test purposes only.  Returns the current map of valid PM definitions.
     */
    protected Map<String, List<PMDefinition>> getValidPMDefinitions() {
        return this.validPMDefinitions.get();
    }

    /*
     * (non-javadoc)
     *
     * Utility method that merges the source PM list into the target PM list.
     */
    private List<PMDefinition> mergeLists(final List<PMDefinition> source, final List<PMDefinition> target) {

        final Set<PMDefinition> targetSet = new TreeSet<>((a, b) -> a.getName().compareTo(b.getName()));
        targetSet.addAll(target);

        for (final PMDefinition def : source) {
            targetSet.remove(def);
            targetSet.add(def);
        }

        return targetSet.stream().toList();
    }
}
