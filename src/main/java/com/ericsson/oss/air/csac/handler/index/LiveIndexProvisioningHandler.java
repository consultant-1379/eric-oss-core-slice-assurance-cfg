/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.handler.index;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.ericsson.oss.air.csac.configuration.index.IndexerTemplateConfiguration;
import com.ericsson.oss.air.csac.handler.ServiceUpdateHandler;
import com.ericsson.oss.air.csac.handler.event.ConsistencyCheckEvent;
import com.ericsson.oss.air.csac.handler.event.ConsistencyCheckHandler;
import com.ericsson.oss.air.csac.model.KPIDefinition;
import com.ericsson.oss.air.csac.model.ProfileDefinition;
import com.ericsson.oss.air.csac.model.pmsc.KpiDefinitionDTO;
import com.ericsson.oss.air.csac.model.runtime.RuntimeKpiInstance;
import com.ericsson.oss.air.csac.model.runtime.index.ContextFieldDto;
import com.ericsson.oss.air.csac.model.runtime.index.DeployedIndexDefinitionDto;
import com.ericsson.oss.air.csac.model.runtime.index.IndexWriterDto;
import com.ericsson.oss.air.csac.model.runtime.index.ValueFieldDto;
import com.ericsson.oss.air.csac.repository.DeployedIndexDefinitionDao;
import com.ericsson.oss.air.csac.repository.DeployedKpiDefDAO;
import com.ericsson.oss.air.csac.repository.KPIDefinitionDAO;
import com.ericsson.oss.air.csac.service.index.IndexerProvisioningService;
import com.ericsson.oss.air.exception.CsacConsistencyCheckException;
import com.ericsson.oss.air.util.operator.StatefulSequentialOperator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

/**
 * Live implementation of the {@link IndexProvisioningHandler}.  This handler will be invoked if index provisioning is enabled in the application
 * properties.
 */
@RequiredArgsConstructor
@Slf4j
@Component
@ConditionalOnProperty(value = "provisioning.index.enabled",
                       havingValue = "true")
@Primary
public class LiveIndexProvisioningHandler extends IndexProvisioningHandler {

    private final DeployedIndexDefinitionDao indexDefinitionDao;

    private final DeployedKpiDefDAO deployedKpiDefDAO;

    private final KPIDefinitionDAO kpiDefinitionDAO;

    private final IndexerProvisioningService indexerService;

    private final IndexerTemplateConfiguration indexerTemplateConfiguration;

    private final ConsistencyCheckHandler consistencyCheckHandler;

    @Override
    protected StatefulSequentialOperator getRollbackOperator() {

        // no rollback currently supported
        return StatefulSequentialOperator.noop();
    }

    @Override
    protected void doApply(final List<ProfileDefinition> profileDefinitions) {

        // the profile definitions are ignored for IDUN-61612.  All visible runtime KPIs will be indexed
        // using the default index definition.

        final Map<String, List<RuntimeKpiInstance>> writerData = getWriterData();

        if (writerData.isEmpty()) {
            log.info("No indexable KPIs.  Skipping indexer provisioning");
            return;
        }

        final DeployedIndexDefinitionDto defaultIndex = getDefaultIndexDefinition(writerData);

        provisionIndex(defaultIndex, getUpdateType(defaultIndex));
    }

    protected void provisionIndex(final DeployedIndexDefinitionDto indexDto, ServiceUpdateHandler.ServiceUpdateType updateType) {

        switch (updateType) {
            case CREATE -> this.indexerService.create(indexDto);
            case UPDATE -> this.indexerService.update(indexDto);
            default -> log.info("No KPI index changes found. Skipping update.");
        }

        if (ServiceUpdateHandler.ServiceUpdateType.NO_OP != updateType) {
            try {
                this.indexDefinitionDao.save(indexDto);
            } catch (final Exception e) {
                this.consistencyCheckHandler.notifyCheckFailure(new ConsistencyCheckEvent.Payload(ConsistencyCheckEvent.Payload.Type.SUSPECT, 1));
                throw new CsacConsistencyCheckException(e);
            }
        }

    }

    /*
     * (non-javadoc)
     *
     * Returns a DeployedIndexDefinitionDto based on the default index definition template and the provided list of runtime
     * KPI instances.
     */
    protected DeployedIndexDefinitionDto getDefaultIndexDefinition(final Map<String, List<RuntimeKpiInstance>> writerData) {

        final DeployedIndexDefinitionDto defaultIndex = this.indexerTemplateConfiguration.getIndexDefinitionTemplate();

        for (final String writerName : writerData.keySet()) {

            final IndexWriterDto writerDto = this.indexerTemplateConfiguration.getIndexWriterTemplate()
                    .orElseThrow(() -> new IllegalStateException("Missing default index writer template"));
            writerDto.name(writerName)
                    .inputSchema(writerName);

            for (final ValueFieldDto valueField : getValueFieldsForWriter(writerName, writerData)) {
                writerDto.valueFieldList().add(valueField);
            }

            for (final ContextFieldDto contextField : getContextFieldsForWriter(writerName, writerData)) {
                writerDto.contextFieldList().add(contextField);
            }

            defaultIndex.indexWriter(writerDto);
        }

        return defaultIndex;
    }

    /*
     * (non-javadoc)
     *
     * Returns a list of context fields derived from the provided list of runtime KPI instances.
     */
    protected List<ContextFieldDto> getContextFieldsForWriter(final String writerName, final Map<String, List<RuntimeKpiInstance>> writerData) {

        final RuntimeKpiInstance representativeKpi = writerData.get(writerName).stream().findFirst().orElseThrow();

        return representativeKpi.getContextFieldList().stream().map(this::getContextField).collect(Collectors.toList());
    }

    /*
     * (non-javadoc)
     *
     * Returns a list of value fields derived from the provided list of runtime KPI instances.
     */
    protected List<ValueFieldDto> getValueFieldsForWriter(final String writerName, final Map<String, List<RuntimeKpiInstance>> writerData) {
        return writerData.get(writerName).stream().map(this::getValueField).collect(Collectors.toList());
    }

    /*
     * (non-javadoc)
     *
     * Returns a ContextFieldDto derived from the provided context field name.
     */
    protected ContextFieldDto getContextField(final String contextFieldName) {
        return ContextFieldDto.builder().name(contextFieldName).build();
    }

    /*
     * (non-javadoc)
     *
     * Returns a ValueFieldDto derived from the provided runtime KPI instance.
     */
    protected ValueFieldDto getValueField(final RuntimeKpiInstance rtInstance) {

        final KPIDefinition definition = this.kpiDefinitionDAO.findByKPIDefName(rtInstance.getKpDefinitionName());

        final ValueFieldDto.ValueFieldType fieldType = ValueFieldDto.ValueFieldType
                .valueOf(((KpiDefinitionDTO) rtInstance.getRuntimeDefinition()).getObjectType().toUpperCase(Locale.ROOT));

        return ValueFieldDto.builder()
                .type(fieldType)
                .recordName(rtInstance.getInstanceId())
                .valueFieldDisplayName(definition.getDisplayName())
                .name(rtInstance.getKpDefinitionName())
                .description(definition.getDescription())
                .build();
    }

    /*
     * (non-javadoc)
     *
     * Returns a map where the keys are the fact table names and the values are the lists of visible KPIs
     * in the corresponding fact table.
     */
    protected Map<String, List<RuntimeKpiInstance>> getWriterData() {

        final Map<String, List<RuntimeKpiInstance>> kpiTables = new HashMap<>();

        final List<RuntimeKpiInstance> visibleKpis = getVisibleKpiDefinitions();

        for (final RuntimeKpiInstance deployedKpi : visibleKpis) {

            final KpiDefinitionDTO kpiDefinitionDTO = (KpiDefinitionDTO) deployedKpi.getRuntimeDefinition();
            final String writerName = kpiDefinitionDTO.getFactTableName();

            kpiTables.computeIfAbsent(writerName, k -> new ArrayList<>()).add(deployedKpi);
        }

        return kpiTables;
    }

    /*
     * (non-javadoc)
     *
     * Returns a list of all visible deployed KPIs from the DeployedKpiDefDAO.
     */
    protected List<RuntimeKpiInstance> getVisibleKpiDefinitions() {

        final List<RuntimeKpiInstance> allRtInstances = this.deployedKpiDefDAO.findAllRuntimeKpis();
        return allRtInstances.stream().filter(LiveIndexProvisioningHandler::isVisible).collect(Collectors.toList());
    }

    /*
     * (non-javadoc)
     *
     * Returns the service update type based on the specified index.  If it does not already exist in the datastore, it is considered
     * a create operation.  If it exists but has changed, it is considered an update.  Otherwise, no operation is performed.
     */
    protected ServiceUpdateHandler.ServiceUpdateType getUpdateType(final DeployedIndexDefinitionDto indexDto) {

        final Optional<DeployedIndexDefinitionDto> existing = this.indexDefinitionDao.findById(indexDto.indexDefinitionName());

        if (existing.isEmpty()) {
            return ServiceUpdateHandler.ServiceUpdateType.CREATE;
        }

        if (indexDto.equals(existing.get())) {
            return ServiceUpdateHandler.ServiceUpdateType.NO_OP;
        }

        return ServiceUpdateHandler.ServiceUpdateType.UPDATE;

    }

    /*
     * (non-javadoc)
     *
     * Utility method to determine whether a given runtime KPI instance is exportable.
     */
    private static boolean isVisible(RuntimeKpiInstance runtimeKpiInstance) {
        return ((KpiDefinitionDTO) runtimeKpiInstance.getRuntimeDefinition()).getIsVisible();
    }
}
