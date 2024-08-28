/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import com.ericsson.oss.air.csac.configuration.schema.InputSchemaProvider;
import com.ericsson.oss.air.csac.handler.validation.KPIContextValidator;
import com.ericsson.oss.air.csac.model.InputMetric;
import com.ericsson.oss.air.csac.model.InputMetricOverride;
import com.ericsson.oss.air.csac.model.KPIDefinition;
import com.ericsson.oss.air.csac.model.KPIReference;
import com.ericsson.oss.air.csac.model.PMDefinition;
import com.ericsson.oss.air.csac.model.ProfileDefinition;
import com.ericsson.oss.air.csac.model.pmsc.AggregationPeriod;
import com.ericsson.oss.air.csac.model.pmsc.KPIDefinitionDTOMapping;
import com.ericsson.oss.air.csac.model.pmsc.KpiDefinitionDTO;
import com.ericsson.oss.air.csac.model.pmsc.KpiDefinitionDTOWithRelationship;
import com.ericsson.oss.air.csac.model.pmschema.SchemaReference;
import com.ericsson.oss.air.csac.model.runtime.RuntimeKpiKey;
import com.ericsson.oss.air.csac.repository.DeployedKpiDefDAO;
import com.ericsson.oss.air.csac.repository.KPIDefinitionDAO;
import com.ericsson.oss.air.csac.repository.PMDefinitionDAO;
import com.ericsson.oss.air.csac.repository.cache.ResolvedKpiCache;
import com.ericsson.oss.air.exception.CsacValidationException;
import com.ericsson.oss.air.util.codec.Codec;
import com.ericsson.oss.air.util.codec.Digest;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

/**
 * The type Kpi calculator.
 */
@Component
@Slf4j
public class KPICalculator {

    /**
     * The aggregation period for the PMSC KPI aggregations
     */
    @Getter(AccessLevel.PACKAGE) // Getter for unit tests only
    private Integer aggregationPeriod;

    /**
     * The Pm definition dao.
     */
    private final PMDefinitionDAO pmDefinitionDAO;

    /**
     * The Kpi definition dao.
     */
    private final KPIDefinitionDAO kpiDefinitionDAO;

    /**
     * The Deployed kpi def dao.
     */
    private final DeployedKpiDefDAO deployedKpiDefDAO;

    private final InputSchemaProvider inputSchemaProvider;

    private final ResolvedKpiCache resolvedKpiCache;

    private final Codec codec = new Codec();

    /**
     * Instantiates a new Kpi calculator.
     *
     * @param pmDefinitionDAO   the pm definition dao
     * @param kpiDefinitionDAO  the kpi definition dao
     * @param deployedKpiDefDAO the deployed kpi def dao
     */
    @Autowired
    public KPICalculator(final PMDefinitionDAO pmDefinitionDAO, final KPIDefinitionDAO kpiDefinitionDAO, final DeployedKpiDefDAO deployedKpiDefDAO,
                         final InputSchemaProvider inputSchemaProvider, final ResolvedKpiCache resolvedKpiCache) {
        this.pmDefinitionDAO = pmDefinitionDAO;
        this.kpiDefinitionDAO = kpiDefinitionDAO;
        this.deployedKpiDefDAO = deployedKpiDefDAO;
        this.inputSchemaProvider = inputSchemaProvider;
        this.resolvedKpiCache = resolvedKpiCache;
    }

    @Autowired
    public void setAggregationPeriod(
            @Value("${provisioning.pmsc.aggregationPeriod.default:15}")
            final Integer aggregationPeriod) {
        if (!AggregationPeriod.contains(aggregationPeriod)) {
            throw new CsacValidationException(
                    String.format("%d is not a permitted value for the PMSC KPI aggregation period", aggregationPeriod));
        }

        this.aggregationPeriod = aggregationPeriod;
        log.debug("The aggregation period value for the PMSC KPI calculations is set to : " + this.aggregationPeriod);
    }

    /**
     * Calculate affected KPI from given pending profile
     *
     * @param pendingProfiles the pending profiles
     * @return the list of {@link KpiDefinitionDTOWithRelationship}
     */
    public List<KpiDefinitionDTOWithRelationship> calculateAffectedKPIs(final List<ProfileDefinition> pendingProfiles) {

        final List<KpiDefinitionDTOWithRelationship> affectedKpiDTOWithRelationshipList = new ArrayList<>();

        pendingProfiles.forEach(profile -> profile.getKpis().forEach(kpiReference -> {
            final KPIDefinition parentKpiDef = this.kpiDefinitionDAO.findByKPIDefName(kpiReference.getRef());
            final List<String> profileContext = profile.getContext();
            final List<InputMetricOverride> inputMetricOverrides = kpiReference.getInputMetricOverrides();

            final RuntimeKpiKey runtimeKpiKey = generateRuntimeKpiKey(kpiReference, profileContext, parentKpiDef);
            final Optional<KpiDefinitionDTO> resolvedRtKpi = this.resolvedKpiCache.get(runtimeKpiKey);
            if (resolvedRtKpi.isEmpty()) {
                // KPI does not exist in the resolved run time kpi cache
                if (KPIContextValidator.isComplexKpi(parentKpiDef)) {
                    final Map<String, KpiDefinitionDTO> affectedChildSimpleKpis = new HashMap<>();
                    parentKpiDef.getInputMetrics().forEach(inputMetric -> {
                        // Complex kpi, check if the child simple kpi has been resolved
                        final String kpiName = inputMetric.getId();
                        final KPIDefinition childKpi = this.kpiDefinitionDAO.findByKPIDefName(kpiName);

                        // Simple kpi can be override with different context fields, retrieve the correct context for it
                        final List<String> aggregationFields = getAggregationContext(kpiName, inputMetricOverrides, profileContext);
                        final RuntimeKpiKey childKpiRtKey = generateRuntimeKpiKey(kpiReference, aggregationFields, childKpi);
                        final Optional<KpiDefinitionDTO> childKpiDefDto = this.resolvedKpiCache.get(childKpiRtKey);
                        if (childKpiDefDto.isEmpty()) {
                            // If not resolved, calculate the child simple kpi with input metric override option and store in the cache
                            final KpiDefinitionDTO simpleKpiDefinitionDto = getAffectedChildKpi(childKpi, aggregationFields, profile);
                            affectedChildSimpleKpis.put(kpiName, simpleKpiDefinitionDto);
                            affectedKpiDTOWithRelationshipList.add(getKpiDefinitionDTOWithRelationship(childKpi, simpleKpiDefinitionDto, profile));
                        } else {
                            affectedChildSimpleKpis.put(kpiName, childKpiDefDto.get());
                        }
                    });

                    KpiDefinitionDTO complexKpiDefinitionDto = createComplexKpiDefinitionDto(parentKpiDef, profile, affectedChildSimpleKpis);
                    complexKpiDefinitionDto = updateRuntimeKpiInstanceId(complexKpiDefinitionDto, parentKpiDef, profile);
                    this.resolvedKpiCache.put(runtimeKpiKey, complexKpiDefinitionDto);
                    affectedKpiDTOWithRelationshipList.add(getKpiDefinitionDTOWithRelationship(parentKpiDef, complexKpiDefinitionDto, profile));
                    log.debug("New instantiated complex KPI: {} with context {}", parentKpiDef.getName(), profileContext);
                } else {
                    KpiDefinitionDTO simpleKpiDefinitionDto = createSimpleKpiDefinitionDto(parentKpiDef, profile);
                    simpleKpiDefinitionDto = updateRuntimeKpiInstanceId(simpleKpiDefinitionDto, parentKpiDef, profile);
                    this.resolvedKpiCache.put(runtimeKpiKey, simpleKpiDefinitionDto);
                    final KpiDefinitionDTOWithRelationship simpleKpiDefDtoWithRelationship = getKpiDefinitionDTOWithRelationship(parentKpiDef,
                            simpleKpiDefinitionDto, profile);
                    affectedKpiDTOWithRelationshipList.add(simpleKpiDefDtoWithRelationship);
                    log.debug("New instantiated simple KPI: {} with profile context: {}", parentKpiDef.getName(), profileContext);
                }
            }

        }));

        return affectedKpiDTOWithRelationshipList;
    }

    /*
     * (non-javadoc)
     * Generates the runtime kpi key for a kpi, only if it is a complex kpi ,and it has input metrics overrides, stringifies the attributes and add as key element
     */
    @SneakyThrows
    private RuntimeKpiKey generateRuntimeKpiKey(final KPIReference kpiReference, final List<String> aggregationFields,
                                                final KPIDefinition kpiDefinition) {
        final RuntimeKpiKey runtimeKpiKey = new RuntimeKpiKey().toBuilder().withAggregationFields(aggregationFields)
                .withAggregationPeriod(aggregationPeriod).withKpDefinitionName(kpiDefinition.getName()).build();

        if (KPIContextValidator.isComplexKpi(kpiDefinition)) {
            final String referenceKey = new Digest().getDigestAsHex(this.codec.writeValueAsString(kpiReference));
            runtimeKpiKey.setReferenceKey(referenceKey);
        }
        return runtimeKpiKey;
    }

    /*
     * (non-javadoc)
     * Calculates the affected child kpi with input metrics override option
     */
    private KpiDefinitionDTO getAffectedChildKpi(final KPIDefinition kpiDefinition, final List<String> aggregationFields,
                                                 final ProfileDefinition profile) {
        // calculate the KPI
        final String kpiName = kpiDefinition.getName();
        KpiDefinitionDTO simpleKpiDefinitionDto = createSimpleKpiDefinitionDto(
                kpiDefinition, profile, aggregationFields);
        // if KPI is in the DB, get the id
        simpleKpiDefinitionDto = updateRuntimeKpiInstanceId(simpleKpiDefinitionDto, kpiDefinition, profile);
        // If not, instantiate the child simple kpis and add to the map for complex kpi calculation
        final RuntimeKpiKey childKpiRtKey = RuntimeKpiKey.builder().withKpDefinitionName(kpiName)
                .withAggregationPeriod(aggregationPeriod).withAggregationFields(aggregationFields).build();
        this.resolvedKpiCache.put(childKpiRtKey, simpleKpiDefinitionDto);

        log.debug("New instantiated child simple KPI: {} with context {}", kpiName, aggregationFields);
        return simpleKpiDefinitionDto;
    }

    /*
     * (non-javadoc)
     * Rebuild the runtime kpi definition dto with correct id if it is in the DB already.
     */
    private KpiDefinitionDTO updateRuntimeKpiInstanceId(KpiDefinitionDTO kpiDefinitionDTO, final KPIDefinition kpiDefinition,
                                                        final ProfileDefinition profile) {
        final KpiDefinitionDTO deployedKpi = this.deployedKpiDefDAO.getDeployedKpiByAggregation(kpiDefinition.getName(), profile.getContext());
        if (Objects.nonNull(deployedKpi)) {
            kpiDefinitionDTO = kpiDefinitionDTO.toBuilder().withName(deployedKpi.getName()).build();
        }
        return kpiDefinitionDTO;
    }

    /*
     * (non-javadoc)
     * Gets the correct aggregation fields from either the profile context or the input metric overrides context
     */
    private List<String> getAggregationContext(final String inputMetricId, final List<InputMetricOverride> inputMetricOverrides,
                                               final List<String> profileContext) {
        final Map<String, List<String>> overrideMap = new HashMap<>();
        List<String> inputMetricsIdList = new ArrayList<>();
        if (!ObjectUtils.isEmpty(inputMetricOverrides)) {

            inputMetricOverrides.forEach(inputMetricOverride -> overrideMap.put(inputMetricOverride.getId(),
                    Objects.isNull(inputMetricOverride.getContext()) ? profileContext : inputMetricOverride.getContext()));
            inputMetricsIdList = inputMetricOverrides.stream().map(InputMetricOverride::getId).collect(Collectors.toList());

        }
        if (inputMetricsIdList.contains(inputMetricId)) {
            return overrideMap.get(inputMetricId);
        } else {
            return profileContext;
        }
    }

    /*
     * (non-javadoc)
     * Creates a simple runtime KPI definition using the provided KPI definition and profile.
     */
    private KpiDefinitionDTO createSimpleKpiDefinitionDto(final KPIDefinition kpiDefinition, final ProfileDefinition profile) {
        return createSimpleKpiDefinitionDto(kpiDefinition, profile, profile.getContext());
    }

    /*
     * (non-javadoc)
     * Creates a simple runtime KPI definition using the provided KPI definition, profile and aggregation context
     */
    private KpiDefinitionDTO createSimpleKpiDefinitionDto(final KPIDefinition kpiDefinition, final ProfileDefinition profile,
                                                          final List<String> aggregationFields) {
        final String pmDefName = kpiDefinition.getInputMetrics().get(0).getId();
        final String schemaName = this.pmDefinitionDAO.findSchemaByPMDefName(pmDefName);
        final PMDefinition pmDef = this.pmDefinitionDAO.findByPMDefName(pmDefName);
        final String source = this.inputSchemaProvider.getSchemaReference(profile, pmDef);

        final String tableName = Objects.isNull(profile.getAugmentation()) ? schemaName : SchemaReference.of(source).getSchemaId();

        return KPIDefinitionDTOMapping.createSimpleKPIDTO(kpiDefinition, aggregationFields, tableName, source, aggregationPeriod);
    }

    /**
     * Creates a complex runtime KPI definition.
     *
     * @param complexKpiDefinition      the KPI definition
     * @param profile                   the profile definition
     * @param simpleKpiDefinitionDtoMap the map of runtime simple KPI definitions for the provided profile definition
     * @return the complex runtime KPI definition
     */
    KpiDefinitionDTO createComplexKpiDefinitionDto(final KPIDefinition complexKpiDefinition, final ProfileDefinition profile,
                                                   final Map<String, KpiDefinitionDTO> simpleKpiDefinitionDtoMap) {

        final Map<String, KpiDefinitionDTO> inputSimpleKpiDefinitionDtoMap = new HashMap<>();

        complexKpiDefinition.getInputMetrics().forEach(inputMetric -> {
            final KpiDefinitionDTO resolvedInputMetric = getResolvedInputMetric(inputMetric, simpleKpiDefinitionDtoMap, profile)
                    .orElseThrow(() -> new CsacValidationException("Complex KPI \"" + complexKpiDefinition.getName()
                            + "\" cannot be instantiated. Missing input metric \"" + inputMetric.getId()
                            + "\" in profile \"" + profile.getName() + "\""));
            inputSimpleKpiDefinitionDtoMap.put(inputMetric.getId(), resolvedInputMetric);
        });

        return KPIDefinitionDTOMapping.createComplexKPIDTO(complexKpiDefinition, profile, aggregationPeriod, inputSimpleKpiDefinitionDtoMap);
    }

    /**
     * Retrieves the correct fact table name based on the simple kpi definition dto map and input metrics override content
     *
     * @param inputMetricOverrides           a list of input metric override configuration
     * @param inputSimpleKpiDefinitionDtoMap a map of runtime simple KPI definition
     * @return a table name
     */
    String retrieveFactTableName(final List<InputMetricOverride> inputMetricOverrides,
                                 final Map<String, KpiDefinitionDTO> inputSimpleKpiDefinitionDtoMap) {
        // Build the table name which will be used in the FROM clause
        String simpleKpiFactTableName = null;
        final List<String> inputMetricContextOverrideIds = new ArrayList<>();
        if (!ObjectUtils.isEmpty(inputMetricOverrides)) {
            inputMetricOverrides.forEach(inputMetricOverride -> {
                if (!ObjectUtils.isEmpty(inputMetricOverride.getContext())) {
                    inputMetricContextOverrideIds.add(inputMetricOverride.getId());
                }
            });
        }
        final List<String> tableNameList = new ArrayList<>();
        // Use the fact table name from a kpi which is either no context override or from the same table
        for (final Map.Entry<String, KpiDefinitionDTO> inputMetricMapEntry : inputSimpleKpiDefinitionDtoMap.entrySet()) {
            final String simpleKpiName = inputMetricMapEntry.getKey();
            final KpiDefinitionDTO simpleKpiDto = inputMetricMapEntry.getValue();
            final String tableName = simpleKpiDto.getFactTableName();
            tableNameList.add(tableName);
            if (!inputMetricContextOverrideIds.contains(simpleKpiName)) {
                simpleKpiFactTableName = tableName;
                break;
            }
        }
        return Objects.isNull(simpleKpiFactTableName) ? tableNameList.get(0) : simpleKpiFactTableName;
    }

    /*
     * (non-javadoc)
     * Gets the runtime KPI definition with its associated KPI definition and profile.
     */
    private KpiDefinitionDTOWithRelationship getKpiDefinitionDTOWithRelationship(final KPIDefinition kpiDefinition,
                                                                                 final KpiDefinitionDTO kpiDefinitionDto,
                                                                                 final ProfileDefinition profile) {
        return new KpiDefinitionDTOWithRelationship(kpiDefinitionDto, kpiDefinition.getName(), profile);
    }

    /*
     * (non-javadoc)
     * Resolves and returns the provided input metric as a runtime KPI definition.
     */
    private Optional<KpiDefinitionDTO> getResolvedInputMetric(final InputMetric inputMetric, final Map<String, KpiDefinitionDTO> kpiDefinitionDtoMap,
                                                              final ProfileDefinition profile) {

        final String inputMetricId = inputMetric.getId();

        final Optional<KpiDefinitionDTO> inputMetricDtoFromProfile = Optional.ofNullable(kpiDefinitionDtoMap.get(inputMetricId));

        /*
         * This input metric's definition may not be in this profile. For IDUN-28459: if it is absent, then it must be
         * in the deployed KPI data store.
         */
        return inputMetricDtoFromProfile.isEmpty() ?
                Optional.ofNullable(this.deployedKpiDefDAO.getDeployedKpiByAggregation(inputMetricId, profile.getContext()))
                :
                inputMetricDtoFromProfile;
    }

}
