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

import static com.ericsson.oss.air.csac.model.TestResourcesUtils.DEPLOYED_SIMPLE_KPI_AGG_ELEMENTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.ericsson.oss.air.csac.configuration.index.IndexerTemplateConfiguration;
import com.ericsson.oss.air.csac.handler.ServiceUpdateHandler;
import com.ericsson.oss.air.csac.handler.event.ConsistencyCheckEvent;
import com.ericsson.oss.air.csac.handler.event.ConsistencyCheckHandler;
import com.ericsson.oss.air.csac.model.InputMetric;
import com.ericsson.oss.air.csac.model.KPIDefinition;
import com.ericsson.oss.air.csac.model.pmsc.KpiDefinitionDTO;
import com.ericsson.oss.air.csac.model.pmsc.KpiTypeEnum;
import com.ericsson.oss.air.csac.model.runtime.RuntimeKpiInstance;
import com.ericsson.oss.air.csac.model.runtime.index.ContextFieldDto;
import com.ericsson.oss.air.csac.model.runtime.index.DeployedIndexDefinitionDto;
import com.ericsson.oss.air.csac.model.runtime.index.ValueFieldDto;
import com.ericsson.oss.air.csac.repository.DeployedIndexDefinitionDao;
import com.ericsson.oss.air.csac.repository.DeployedKpiDefDAO;
import com.ericsson.oss.air.csac.repository.KPIDefinitionDAO;
import com.ericsson.oss.air.csac.service.index.IndexerProvisioningService;
import com.ericsson.oss.air.exception.CsacConsistencyCheckException;
import com.ericsson.oss.air.util.codec.Codec;
import com.ericsson.oss.air.util.operator.StatefulSequentialOperator;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LiveIndexProvisioningHandlerTest {

    private static final Codec CODEC = new Codec();

    private static final String VALUE_1 = "{\"name\":\"Simple Kpi 1\",\"displayName\":\"KPI Definition 1\",\"type\":\"float\",\"recordName\":\"csac_106ed55e_c148_4af9_9a84_e9998bc4ab60\",\"description\":\"KPI definition description\"}";

    private static final String VALUE_2 = "{\"name\":\"Simple Kpi 2\",\"displayName\":\"KPI Definition 2\",\"type\":\"float\",\"recordName\":\"csac_106ed55e_c148_4af9_9a84_e9998bc4ab61\",\"description\":\"KPI definition description\"}";

    private ValueFieldDto value1;

    private ValueFieldDto value2;

    @Mock
    private DeployedIndexDefinitionDao indexDefinitionDao;

    @Mock
    private DeployedKpiDefDAO deployedKpiDefDAO;

    @Mock
    private KPIDefinitionDAO kpiDefinitionDAO;

    @Mock
    private IndexerProvisioningService indexerService;

    @Mock
    private ConsistencyCheckHandler consistencyCheckHandler;

    private IndexProvisioningHandler testHandler;

    private static final String SIMPLE_KPI_NAME_1 = "csac_106ed55e_c148_4af9_9a84_e9998bc4ab60";

    private static final String SIMPLE_KPI_NAME_2 = "csac_b67a7861_b63b_4d9f_ac85_9ce208aafb84";

    private static final String COMPLEX_KPI_NAME_1 = "csac_71e27d9d-1ba7-4d8c-b53f-d58887c7989c";

    private final KpiDefinitionDTO simpleKpi1 = KpiDefinitionDTO.builder().withName(SIMPLE_KPI_NAME_1)
            .withKpiType(KpiTypeEnum.SIMPLE).withExpression("SUM(testSchema.pmdef_name)").withAggregationType("MAX")
            .withAggregationPeriod(15).withObjectType("FLOAT")
            .withAggregationElements(DEPLOYED_SIMPLE_KPI_AGG_ELEMENTS).withIsVisible(true)
            .withInpDataCategory(InputMetric.Type.PM_DATA.toString()).withInpDataIdentifier("pmdef_source").build();

    private final KpiDefinitionDTO simpleKpi2 = KpiDefinitionDTO.builder().withName(SIMPLE_KPI_NAME_2)
            .withKpiType(KpiTypeEnum.SIMPLE).withExpression("MAX(testSchema.pmdef_name)").withAggregationType("MAX")
            .withAggregationPeriod(15).withObjectType("FLOAT")
            .withAggregationElements(DEPLOYED_SIMPLE_KPI_AGG_ELEMENTS).withIsVisible(false)
            .withInpDataCategory(InputMetric.Type.PM_DATA.toString()).withInpDataIdentifier("pmdef_source").build();

    private final KpiDefinitionDTO complex1 = KpiDefinitionDTO.builder().withKpiType(KpiTypeEnum.COMPLEX)
            .withName(COMPLEX_KPI_NAME_1).withExpression("MAX(testFactTableName.pmdef_name) FROM kpi_db://testFactTableName")
            .withAggregationType("MAX").withAggregationPeriod(15).withObjectType("FLOAT")
            .withAggregationElements(List.of("testFactTableName.field1", "testFactTableName.field2")).withExecutionGroup("csac_execution_group")
            .withIsVisible(true).build();

    private final RuntimeKpiInstance instance1 = RuntimeKpiInstance.builder()
            .withInstanceId(SIMPLE_KPI_NAME_1)
            .withContextFieldList(DEPLOYED_SIMPLE_KPI_AGG_ELEMENTS)
            .withKpDefinitionName("Simple Kpi 1")
            .withRuntimeDefinition(simpleKpi1)
            .build();

    private final RuntimeKpiInstance instance2 = RuntimeKpiInstance.builder()
            .withInstanceId(SIMPLE_KPI_NAME_2)
            .withContextFieldList(DEPLOYED_SIMPLE_KPI_AGG_ELEMENTS)
            .withKpDefinitionName("Simple Kpi 2")
            .withRuntimeDefinition(simpleKpi2)
            .build();

    private final RuntimeKpiInstance instance3 = RuntimeKpiInstance.builder()
            .withInstanceId(COMPLEX_KPI_NAME_1)
            .withContextFieldList(DEPLOYED_SIMPLE_KPI_AGG_ELEMENTS)
            .withKpDefinitionName("Complex Kpi 1")
            .withRuntimeDefinition(complex1)
            .build();

    private final String validIndexDefinitionStr = "{\n" +
            "  \"name\" : \"index\",\n" +
            "  \"description\" : \"Index description\",\n" +
            "  \"source\" : {\n" +
            "    \"name\" : \"source\",\n" +
            "    \"type\" : \"pmstatsexporter\",\n" +
            "    \"description\" : \"Index source description\"\n" +
            "  },\n" +
            "  \"target\" : {\n" +
            "    \"name\" : \"target\",\n" +
            "    \"displayName\" : \"Index Target\",\n" +
            "    \"description\" : \"Index description\"\n" +
            "  },\n" +
            "  \"writers\" : [ {\n" +
            "    \"name\" : \"writer\",\n" +
            "    \"inputSchema\" : \"schema\",\n" +
            "    \"context\" : [ {\n" +
            "      \"name\" : \"context1\",\n" +
            "      \"nameType\" : \"straight\"\n" +
            "    }, {\n" +
            "      \"name\" : \"context2\",\n" +
            "      \"nameType\" : \"straight\"\n" +
            "    } ],\n" +
            "    \"value\" : [ {\n" +
            "      \"name\" : \"value1\",\n" +
            "      \"type\" : \"float\"\n" +
            "    }, {\n" +
            "      \"name\" : \"value2\",\n" +
            "      \"type\" : \"float\"\n" +
            "    } ],\n" +
            "    \"info\" : [ {\n" +
            "      \"name\" : \"info1\",\n" +
            "      \"type\" : \"string\"\n" +
            "    }, {\n" +
            "      \"name\" : \"info2\",\n" +
            "      \"type\" : \"string\"\n" +
            "    } ]\n" +
            "  } ]\n" +
            "}";

    @BeforeEach
    void setUp() throws Exception {

        this.value1 = CODEC.readValue(VALUE_1, ValueFieldDto.class);
        this.value2 = CODEC.readValue(VALUE_2, ValueFieldDto.class);

        final IndexerTemplateConfiguration templateConfig = new IndexerTemplateConfiguration();
        templateConfig.loadTemplates();

        final IndexerTemplateConfiguration.SourceName sourceName = new IndexerTemplateConfiguration.SourceName();
        sourceName.setName("name");

        final Map<String, IndexerTemplateConfiguration.SourceName> source = new HashMap<>();
        source.put("pmstatsexporter", sourceName);

        templateConfig.setSource(source);

        this.testHandler = new LiveIndexProvisioningHandler(this.indexDefinitionDao, this.deployedKpiDefDAO, this.kpiDefinitionDAO,
                this.indexerService, templateConfig, this.consistencyCheckHandler);
    }

    @Test
    void getRollbackOperator() {
        assertEquals(StatefulSequentialOperator.noop(), this.testHandler.getRollback());
    }

    @Test
    void apply() {

        final InputMetric inputMetric = InputMetric.builder()
                .id("inputMetric1")
                .alias("p0")
                .type(InputMetric.Type.PM_DATA)
                .build();

        final KPIDefinition kpiDefinition = KPIDefinition.builder()
                .name("KPI_definition_1")
                .description("KPI definition description")
                .inputMetrics(List.of(inputMetric))
                .expression("SUM(p0)")
                .displayName("KPI Definition 1")
                .aggregationType("SUM")
                .isVisible(true)
                .aggregationPeriod(15)
                .build();

        when(this.kpiDefinitionDAO.findByKPIDefName(anyString())).thenReturn(kpiDefinition);

        when(this.deployedKpiDefDAO.findAllRuntimeKpis()).thenReturn(List.of(instance1, instance2, instance3));

        this.testHandler.apply(List.of());

        Mockito.verify(this.indexerService, Mockito.times(1))
                .create(any(DeployedIndexDefinitionDto.class));

        Mockito.verify(this.indexDefinitionDao, Mockito.times(1))
                .save(any(DeployedIndexDefinitionDto.class));
    }

    @Test
    void apply_noRuntimeKpis() {
        when(this.deployedKpiDefDAO.findAllRuntimeKpis()).thenReturn(List.of());

        this.testHandler.apply(List.of());

        Mockito.verify(this.indexerService, Mockito.times(0))
                .create(any(DeployedIndexDefinitionDto.class));

        Mockito.verify(this.indexDefinitionDao, Mockito.times(0))
                .save(any(DeployedIndexDefinitionDto.class));
    }

    @Test
    void provisionIndex_create() {

        ((LiveIndexProvisioningHandler) this.testHandler).provisionIndex(new DeployedIndexDefinitionDto(),
                ServiceUpdateHandler.ServiceUpdateType.CREATE);

        Mockito.verify(this.indexerService, Mockito.times(1))
                .create(any(DeployedIndexDefinitionDto.class));

        Mockito.verify(this.indexDefinitionDao, Mockito.times(1))
                .save(any(DeployedIndexDefinitionDto.class));
    }

    @Test
    void provisionIndex_update() {

        ((LiveIndexProvisioningHandler) this.testHandler).provisionIndex(new DeployedIndexDefinitionDto(),
                ServiceUpdateHandler.ServiceUpdateType.UPDATE);

        Mockito.verify(this.indexerService, Mockito.times(1))
                .update(any(DeployedIndexDefinitionDto.class));

        Mockito.verify(this.indexDefinitionDao, Mockito.times(1))
                .save(any(DeployedIndexDefinitionDto.class));
    }

    @Test
    void provisionIndex_noop() {

        ((LiveIndexProvisioningHandler) this.testHandler).provisionIndex(new DeployedIndexDefinitionDto(),
                ServiceUpdateHandler.ServiceUpdateType.NO_OP);

        Mockito.verify(this.indexerService, Mockito.times(0))
                .create(any(DeployedIndexDefinitionDto.class));

        Mockito.verify(this.indexerService, Mockito.times(0))
                .update(any(DeployedIndexDefinitionDto.class));

        Mockito.verify(this.indexDefinitionDao, Mockito.times(0))
                .save(any(DeployedIndexDefinitionDto.class));
    }

    @Test
    void provisionIndex_create_exception() {

        doThrow(new RuntimeException("test")).when(this.indexDefinitionDao).save(any());

        assertThrows(CsacConsistencyCheckException.class,
                () -> ((LiveIndexProvisioningHandler) this.testHandler).provisionIndex(new DeployedIndexDefinitionDto(),
                        ServiceUpdateHandler.ServiceUpdateType.CREATE));

        final ArgumentCaptor<ConsistencyCheckEvent.Payload> payloadArgumentCaptor = ArgumentCaptor.forClass(ConsistencyCheckEvent.Payload.class);
        verify(this.consistencyCheckHandler, times(1)).notifyCheckFailure(payloadArgumentCaptor.capture());
        assertEquals(ConsistencyCheckEvent.Payload.Type.SUSPECT, payloadArgumentCaptor.getValue().getType());
        assertEquals(1, payloadArgumentCaptor.getValue().getCount());
    }

    @Test
    void getWriterData() {

        when(this.deployedKpiDefDAO.findAllRuntimeKpis()).thenReturn(List.of(instance1, instance2, instance3));

        final Map<String, List<RuntimeKpiInstance>> actual = ((LiveIndexProvisioningHandler) this.testHandler).getWriterData();

        assertEquals(2, actual.size());

        assertTrue(actual.containsKey(complex1.getFactTableName()));
        assertEquals(1, actual.get(complex1.getFactTableName()).size());

        assertTrue(actual.containsKey(simpleKpi1.getFactTableName()));
        assertEquals(1, actual.get(simpleKpi1.getFactTableName()).size());

    }

    @Test
    void getVisibleKpiDefinitions() {

        when(this.deployedKpiDefDAO.findAllRuntimeKpis()).thenReturn(List.of(instance1, instance2, instance3));

        assertEquals(3, this.deployedKpiDefDAO.findAllRuntimeKpis().size());

        assertEquals(2, ((LiveIndexProvisioningHandler) this.testHandler).getVisibleKpiDefinitions().size());

    }

    @Test
    void getValueField() throws Exception {

        final String expectedStr = "{\"name\":\"Simple Kpi 1\",\"displayName\":\"KPI Definition 1\",\"type\":\"float\",\"recordName\":\"csac_106ed55e_c148_4af9_9a84_e9998bc4ab60\",\"description\":\"KPI definition description\"}";
        final ValueFieldDto expected = CODEC.readValue(expectedStr, ValueFieldDto.class);

        final InputMetric inputMetric = InputMetric.builder()
                .id("inputMetric1")
                .alias("p0")
                .type(InputMetric.Type.PM_DATA)
                .build();

        final KPIDefinition kpiDefinition = KPIDefinition.builder()
                .name("KPI_definition_1")
                .description("KPI definition description")
                .inputMetrics(List.of(inputMetric))
                .expression("SUM(p0)")
                .displayName("KPI Definition 1")
                .aggregationType("SUM")
                .isVisible(true)
                .aggregationPeriod(15)
                .build();

        when(this.kpiDefinitionDAO.findByKPIDefName(anyString())).thenReturn(kpiDefinition);

        final ValueFieldDto actual = ((LiveIndexProvisioningHandler) this.testHandler).getValueField(instance1);

        assertEquals(expected, actual);
    }

    @Test
    void getValueFieldsForWriter() {

        final InputMetric inputMetric = InputMetric.builder()
                .id("inputMetric1")
                .alias("p0")
                .type(InputMetric.Type.PM_DATA)
                .build();

        final KPIDefinition kpiDefinition = KPIDefinition.builder()
                .name("KPI_definition_1")
                .description("KPI definition description")
                .inputMetrics(List.of(inputMetric))
                .expression("SUM(p0)")
                .displayName("KPI Definition 1")
                .aggregationType("SUM")
                .isVisible(true)
                .aggregationPeriod(15)
                .build();

        when(this.kpiDefinitionDAO.findByKPIDefName(anyString())).thenReturn(kpiDefinition);

        final Map<String, List<RuntimeKpiInstance>> writerData = new HashMap<>();
        writerData.put("writer1", List.of(instance1, instance3));
        final List<ValueFieldDto> actual = ((LiveIndexProvisioningHandler) this.testHandler).getValueFieldsForWriter("writer1",
                writerData);

        assertEquals(2, actual.size());
    }

    @Test
    void getContextField() {

        final ContextFieldDto expected = ContextFieldDto.builder()
                .name("context1")
                .build();

        final ContextFieldDto actual = ((LiveIndexProvisioningHandler) this.testHandler).getContextField("context1");

        assertEquals(expected, actual);
    }

    @Test
    void getContextFieldsForWriter() {

        final Map<String, List<RuntimeKpiInstance>> writerData = new HashMap<>();
        writerData.put("writer1", List.of(instance1, instance3));

        final List<ContextFieldDto> actual = ((LiveIndexProvisioningHandler) this.testHandler).getContextFieldsForWriter("writer1",
                writerData);

        assertEquals(2, actual.size());

        final Set<String> expectedContext = new HashSet<>(instance1.getContextFieldList());
        final Set<String> actualContext = actual.stream().map(ContextFieldDto::getName).collect(Collectors.toSet());

        assertEquals(expectedContext, actualContext);
    }

    @Test
    void getDefaultIndexDefinition() {

        final InputMetric inputMetric = InputMetric.builder()
                .id("inputMetric1")
                .alias("p0")
                .type(InputMetric.Type.PM_DATA)
                .build();

        final KPIDefinition kpiDefinition = KPIDefinition.builder()
                .name("KPI_definition_1")
                .description("KPI definition description")
                .inputMetrics(List.of(inputMetric))
                .expression("SUM(p0)")
                .displayName("KPI Definition 1")
                .aggregationType("SUM")
                .isVisible(true)
                .aggregationPeriod(15)
                .build();

        when(this.kpiDefinitionDAO.findByKPIDefName(anyString())).thenReturn(kpiDefinition);

        final Map<String, List<RuntimeKpiInstance>> writerData = new HashMap<>();
        writerData.put("table1", List.of(instance1));

        when(this.kpiDefinitionDAO.findByKPIDefName(anyString())).thenReturn(kpiDefinition);

        final DeployedIndexDefinitionDto actual = ((LiveIndexProvisioningHandler) this.testHandler).getDefaultIndexDefinition(writerData);

        assertNotNull(actual);

        final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        assertTrue(validator.validate(actual).isEmpty());
    }

    @Test
    void getUpdateType_noop() throws Exception {

        final DeployedIndexDefinitionDto expected = CODEC.readValue(validIndexDefinitionStr, DeployedIndexDefinitionDto.class);

        when(this.indexDefinitionDao.findById(anyString())).thenReturn(Optional.of(expected));

        assertEquals(ServiceUpdateHandler.ServiceUpdateType.NO_OP, ((LiveIndexProvisioningHandler) this.testHandler).getUpdateType(expected));
    }

    @Test
    void getUpdateType_create() throws Exception {

        final DeployedIndexDefinitionDto expected = CODEC.readValue(validIndexDefinitionStr, DeployedIndexDefinitionDto.class);

        when(this.indexDefinitionDao.findById(anyString())).thenReturn(Optional.empty());

        assertEquals(ServiceUpdateHandler.ServiceUpdateType.CREATE, ((LiveIndexProvisioningHandler) this.testHandler).getUpdateType(expected));
    }

    @Test
    void getUpdateType_update() throws Exception {

        final DeployedIndexDefinitionDto expected = CODEC.readValue(validIndexDefinitionStr, DeployedIndexDefinitionDto.class);

        final DeployedIndexDefinitionDto existing = CODEC.readValue(validIndexDefinitionStr, DeployedIndexDefinitionDto.class);
        existing.indexDefinitionName("otherIndex");

        when(this.indexDefinitionDao.findById(anyString())).thenReturn(Optional.of(existing));

        assertEquals(ServiceUpdateHandler.ServiceUpdateType.UPDATE, ((LiveIndexProvisioningHandler) this.testHandler).getUpdateType(expected));
    }

}