/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/
package com.ericsson.oss.air.csac.model;

import java.util.List;
import java.util.Set;

import com.ericsson.oss.air.api.model.InputMetricDto;
import com.ericsson.oss.air.api.model.KpiDefinitionDto;
import com.ericsson.oss.air.api.model.KpiDefinitionListDto;
import com.ericsson.oss.air.api.model.KpiRefsDto;
import com.ericsson.oss.air.api.model.PmDefinitionDto;
import com.ericsson.oss.air.api.model.PmDefinitionListDto;
import com.ericsson.oss.air.api.model.ProfileDefinitionDto;
import com.ericsson.oss.air.api.model.ProfileDefinitionListDto;
import com.ericsson.oss.air.api.model.RtIndexContextFieldDto;
import com.ericsson.oss.air.api.model.RtIndexDefDto;
import com.ericsson.oss.air.api.model.RtIndexDefListDto;
import com.ericsson.oss.air.api.model.RtIndexInfoFieldDto;
import com.ericsson.oss.air.api.model.RtIndexSourceDto;
import com.ericsson.oss.air.api.model.RtIndexTargetDto;
import com.ericsson.oss.air.api.model.RtIndexValueFieldDto;
import com.ericsson.oss.air.api.model.RtIndexWriterDto;
import com.ericsson.oss.air.csac.model.augmentation.ArdqRegistrationResponseDto;
import com.ericsson.oss.air.csac.model.augmentation.SchemaMappingResponseDto;
import com.ericsson.oss.air.csac.model.pmsc.KpiDefinitionDTO;
import com.ericsson.oss.air.csac.model.pmsc.KpiTypeEnum;
import com.ericsson.oss.air.csac.model.pmschema.SchemaURI;
import com.ericsson.oss.air.csac.model.runtime.index.ContextFieldDto;
import com.ericsson.oss.air.csac.model.runtime.index.DeployedIndexDefinitionDto;
import com.ericsson.oss.air.csac.model.runtime.index.IndexSourceDto;
import com.ericsson.oss.air.csac.model.runtime.index.IndexTargetDto;
import com.ericsson.oss.air.csac.model.runtime.index.IndexWriterDto;
import com.ericsson.oss.air.csac.model.runtime.index.InfoFieldDto;
import com.ericsson.oss.air.csac.model.runtime.index.ValueFieldDto;

public class TestResourcesUtils {

    public static final String DEFAULT_SCHEMA_NAME = "testSchema";

    public static final String TEST_FACT_TABLE_NAME = "testFactTableName";

    //define AugmentationDefinition
    public static final String ARDQ_TYPE = "core";

    public static final AugmentationRuleField VALID_AUGMENTATION_RULE_FIELD = AugmentationRuleField.builder()
            .inputFields(List.of("inputField1", "inputField2"))
            .output("outputField")
            .build();

    public static final AugmentationRule AUGMENTATION_RULE = AugmentationRule.builder()
            .inputSchemaReference("input|schema|reference_1")
            .fields(List.of(VALID_AUGMENTATION_RULE_FIELD))
            .build();

    public static final List<AugmentationRule> VALID_AUGMENTATION_RULE_LIST = List.of(AUGMENTATION_RULE);

    public static final String VALID_AUGMENTATION_NAME = "testAugmentation";

    public static final AugmentationDefinition VALID_AUGMENTATION_DEF_OBJ = AugmentationDefinition.builder()
            .name(VALID_AUGMENTATION_NAME)
            .type(ARDQ_TYPE)
            .url("http://localhost:8080")
            .augmentationRules(VALID_AUGMENTATION_RULE_LIST)
            .build();

    public static final List<AugmentationDefinition> VALID_LIST_AUGMENTATION_DEF_OBJ = List.of(VALID_AUGMENTATION_DEF_OBJ);

    // define PMDefinition
    public static final String VALID_PM_DEF_NAME = "pmdef_name";

    public static final String VALID_PM_DEF_NAME_NEW = "pmdef_name_new";

    public static final String VALID_PM_DEF_SOURCE = "5G|PM_COUNTERS|AMF_Mobility_NetworkSlice_1";

    public static final String VALID_SCHEMA_NAME = "AMF_Mobility_NetworkSlice_1";

    private static final String PM_DESCRIPTION = "pmdef description";

    public static final PMDefinition VALID_PM_DEF_OBJ = new PMDefinition(VALID_PM_DEF_NAME, VALID_PM_DEF_SOURCE, PM_DESCRIPTION);

    public static final PMDefinition VALID_PM_DEF_NEW_OBJ = new PMDefinition(VALID_PM_DEF_NAME_NEW, VALID_PM_DEF_SOURCE, PM_DESCRIPTION);

    public static final List<PMDefinition> VALID_LIST_PM_DEF_OBJ = List.of(VALID_PM_DEF_OBJ);

    // define KPIDefinition
    public static final String VALID_SIMPLE_KPI_DEF_NAME = "kpi_simple_name";

    public static final String VALID_SIMPLE_KPI_DEF_NAME_NEW = "kpi_simple_name_new";

    public static final String VALID_COMPLEX_KPI_DEF_NAME = "kpi_complex_name";

    private static final String KPI_DEF_DESCRIPTION = "kpidef decription";

    private static final String VALID_KPI_DEF_DISPLAY_NAME = "kpidef_display_name";

    private static final String VALID_KPI_DEF_EXPRESSION = "MAX(input_alias)";

    private static final String VALID_KPI_DEF_AGGREGATION_TYPE = "MAX";

    private static final boolean VALID_KPI_DEF_IS_VISIBLE = true;

    // define InputMetric List
    private static final String VALID_ID = VALID_PM_DEF_NAME;

    private static final String VALID_ID_NEW = VALID_PM_DEF_NAME_NEW;

    private static final String VALID_ALIAS = "input_alias";

    public static final InputMetric SIMPLE_INPUT_METRIC = new InputMetric(VALID_ID, VALID_ALIAS, InputMetric.Type.PM_DATA);

    public static final InputMetric SIMPLE_INPUT_METRIC_NEW = new InputMetric(VALID_ID_NEW, VALID_ALIAS, InputMetric.Type.PM_DATA);

    public static final InputMetric COMPLEX_INPUT_METRIC = new InputMetric(VALID_SIMPLE_KPI_DEF_NAME, VALID_ALIAS, InputMetric.Type.KPI);

    public static final List<InputMetric> SIMPLE_INPUT_METRIC_LIST = List.of(SIMPLE_INPUT_METRIC);

    public static final List<InputMetric> SIMPLE_INPUT_METRIC_LIST_NEW = List.of(SIMPLE_INPUT_METRIC, SIMPLE_INPUT_METRIC_NEW);

    public static final List<InputMetric> COMPLEX_INPUT_METRIC_LIST = List.of(COMPLEX_INPUT_METRIC);

    public static final KPIDefinition VALID_SIMPLE_KPI_DEF_OBJ = KPIDefinition.builder()
            .name(VALID_SIMPLE_KPI_DEF_NAME)
            .description(KPI_DEF_DESCRIPTION)
            .displayName(VALID_KPI_DEF_DISPLAY_NAME)
            .expression(VALID_KPI_DEF_EXPRESSION)
            .aggregationType(VALID_KPI_DEF_AGGREGATION_TYPE)
            .isVisible(VALID_KPI_DEF_IS_VISIBLE)
            .inputMetrics(SIMPLE_INPUT_METRIC_LIST)
            .build();

    public static final KPIDefinition VALID_SIMPLE_KPI_DEF_OBJ_NEW = KPIDefinition.builder()
            .name(VALID_SIMPLE_KPI_DEF_NAME_NEW)
            .description(KPI_DEF_DESCRIPTION)
            .displayName(VALID_KPI_DEF_DISPLAY_NAME)
            .expression(VALID_KPI_DEF_EXPRESSION)
            .aggregationType(VALID_KPI_DEF_AGGREGATION_TYPE)
            .isVisible(VALID_KPI_DEF_IS_VISIBLE)
            .inputMetrics(SIMPLE_INPUT_METRIC_LIST_NEW)
            .build();

    public static final List<KPIDefinition> VALID_LIST_KPI_DEF_OBJ = List.of(VALID_SIMPLE_KPI_DEF_OBJ);

    public static final KPIDefinition VALID_COMPLEX_KPI_DEF_OBJ = KPIDefinition.builder()
            .name(VALID_COMPLEX_KPI_DEF_NAME)
            .description(KPI_DEF_DESCRIPTION)
            .displayName(VALID_KPI_DEF_DISPLAY_NAME)
            .expression(VALID_KPI_DEF_EXPRESSION)
            .aggregationType(VALID_KPI_DEF_AGGREGATION_TYPE)
            .isVisible(VALID_KPI_DEF_IS_VISIBLE)
            .inputMetrics(COMPLEX_INPUT_METRIC_LIST)
            .build();

    // define ProfileDefinition
    public static final String VALID_PROFILE_DEF_NAME = "profiledef_name";

    private static final String PROFILE_DEF_DESCRIPTION = "profiledef decription";

    public static final String VALID_PROFILE_DEF_AGGREGATION_FIELD = "field1";

    public static final String VALID_PROFILE_DEF_AGGREGATION_FIELD2 = "field2";

    public static final List<String> VALID_PROFILE_DEF_AGGREGATION_FIELD_LIST = List.of(VALID_PROFILE_DEF_AGGREGATION_FIELD,
            VALID_PROFILE_DEF_AGGREGATION_FIELD2);

    public static final List<KPIReference> VALID_KPI_REFERENCE_LIST = List.of(KPIReference.builder().ref(VALID_SIMPLE_KPI_DEF_NAME).build(),
            KPIReference.builder().ref(VALID_COMPLEX_KPI_DEF_NAME).build());

    // Define Profile Object
    public static final ProfileDefinition VALID_PROFILE_DEF_OBJ = ProfileDefinition.builder()
            .name(VALID_PROFILE_DEF_NAME)
            .description(PROFILE_DEF_DESCRIPTION)
            .context(VALID_PROFILE_DEF_AGGREGATION_FIELD_LIST)
            .kpis(VALID_KPI_REFERENCE_LIST)
            .build();

    public static final ProfileDefinition AUGMENTED_PROFILE_DEF_OBJ = ProfileDefinition.builder()
            .name(VALID_PROFILE_DEF_NAME)
            .description(PROFILE_DEF_DESCRIPTION)
            .augmentation("cardq")
            .context(VALID_PROFILE_DEF_AGGREGATION_FIELD_LIST)
            .kpis(VALID_KPI_REFERENCE_LIST)
            .build();

    public static final List<ProfileDefinition> VALID_LIST_PROFILE_DEF_OBJ = List.of(VALID_PROFILE_DEF_OBJ);

    public static final ResourceSubmission VALID_RESOURCE_SUBMISSION = ResourceSubmission.builder()
            .pmDefs(VALID_LIST_PM_DEF_OBJ)
            .kpiDefs(VALID_LIST_KPI_DEF_OBJ)
            .profileDefs(VALID_LIST_PROFILE_DEF_OBJ)
            .augmentationDefinitions(VALID_LIST_AUGMENTATION_DEF_OBJ)
            .build();

    // Deployed KPI
    public static final Integer DEFAULT_AGGREGATION_PERIOD = 15;

    public static final String DEPLOYED_SIMPLE_KPI_STR =
            "{\n" + "      \"name\": \"sum_integer_1440_simple\",\n" + "      \"expression\": \"SUM(fact_table_0.integerColumn0)\",\n"
                    + "      \"object_type\": \"INTEGER\",\n" + "      \"aggregation_type\": \"SUM\",\n" + "      \"aggregation_period\": 1440,\n"
                    + "      \"aggregation_elements\": [\n" + "        \"fact_table_0.agg_column_0\"\n" + "      ],\n"
                    + "      \"is_visible\": true,\n" + "      \"inp_data_category\": \"pm_data\",\n"
                    + "      \"inp_data_identifier\": \"eric-data-message-bus-kf:9092|topic0|fact_table_0\"\n" + "    }";

    public static final String DEPLOYED_COMPLEX_KPI_STR = "{\n" + "      \"name\": \"sum_integer_60_complex\",\n"
            + "      \"expression\": \"SUM(kpi_simple_60.integer_simple) FROM kpi_db://kpi_simple_60\",\n" + "      \"object_type\": \"INTEGER\",\n"
            + "      \"aggregation_type\": \"SUM\",\n" + "      \"aggregation_period\": 60,\n" + "      \"aggregation_elements\": [\n"
            + "        \"kpi_simple_60.agg_column_0\",\n" + "        \"kpi_simple_60.agg_column_1\"\n" + "      ],\n"
            + "      \"is_visible\": true,\n" + "      \"execution_group\": \"COMPLEX1\"\n" + "    }";

    public static final List<String> DEPLOYED_SIMPLE_KPI_AGG_ELEMENTS = List.of(VALID_SCHEMA_NAME + "." + VALID_PROFILE_DEF_AGGREGATION_FIELD,
            DEFAULT_SCHEMA_NAME + "." + VALID_PROFILE_DEF_AGGREGATION_FIELD2);

    public static final String DEPLOYED_SIMPLE_KPI_NAME = "csac_3d3b5e94_51c6_401d_b1fd_440361beb32c_simple_kpi";

    public static final KpiDefinitionDTO DEPLOYED_SIMPLE_KPI_OBJ = KpiDefinitionDTO.builder().withName(DEPLOYED_SIMPLE_KPI_NAME)
            .withKpiType(KpiTypeEnum.SIMPLE).withExpression("MAX(AMF_Mobility_NetworkSlice_1.pmdef_name)").withAggregationType("MAX")
            .withAggregationPeriod(DEFAULT_AGGREGATION_PERIOD).withObjectType("FLOAT")
            .withAggregationElements(DEPLOYED_SIMPLE_KPI_AGG_ELEMENTS).withIsVisible(true)
            .withInpDataCategory(InputMetric.Type.PM_DATA.toString()).withInpDataIdentifier("5G|PM_COUNTERS|AMF_Mobility_NetworkSlice_1").build();

    public static final String DEPLOYED_SIMPLE_KPI_NAME_2 = "csac_3d3b5e94_51c6_401d_b1fd_440361beb32c_simple_kpi_2";

    public static final KpiDefinitionDTO DEPLOYED_SIMPLE_KPI_OBJ_2 = KpiDefinitionDTO.builder().withName(DEPLOYED_SIMPLE_KPI_NAME_2)
            .withKpiType(KpiTypeEnum.SIMPLE).withExpression("MAX(testSchema.pmdef_name)").withAggregationType("MAX")
            .withAggregationPeriod(DEFAULT_AGGREGATION_PERIOD).withObjectType("FLOAT")
            .withAggregationElements(List.of("testSchema.field1", "testSchema.field2")).withIsVisible(false)
            .withInpDataCategory(InputMetric.Type.PM_DATA.toString()).withInpDataIdentifier("pmdef_source_2").build();

    public static final String DEPLOYED_COMPLEX_KPI_NAME = "csac_5601f9b5_afe8_4c49_b1e5_ae7f1d11537c_complex_kpi";

    public static final KpiDefinitionDTO DEPLOYED_COMPLEX_KPI_OBJ = KpiDefinitionDTO.builder().withKpiType(KpiTypeEnum.COMPLEX)
            .withName(DEPLOYED_COMPLEX_KPI_NAME).withExpression("MAX(testFactTableName.pmdef_name) FROM kpi_db://testFactTableName")
            .withAggregationType("MAX").withAggregationPeriod(DEFAULT_AGGREGATION_PERIOD).withObjectType("FLOAT")
            .withAggregationElements(List.of("testFactTableName.field1", "testFactTableName.field2")).withExecutionGroup("csac_execution_group")
            .withIsVisible(true).build();

    // define generated open api model DTO
    public static final String KPI_DEF_NAME_EX = "slice_registered_users_mean";

    public static final InputMetricDto INPUT_METRIC_DTO_EX = new InputMetricDto().id("VS_NS_NbrRegisteredSub_5GS").alias("p0")
            .type(InputMetricDto.TypeEnum.PM_DATA);

    public static final KpiDefinitionDto KPI_DEFINITION_DTO_EX = new KpiDefinitionDto().name(KPI_DEF_NAME_EX)
            .description("Mean registered subscribers of network slice through AMF").displayName("Mean Registered Subscribers")
            .expression(VALID_KPI_DEF_EXPRESSION).aggregationType(VALID_KPI_DEF_AGGREGATION_TYPE).isVisible(VALID_KPI_DEF_IS_VISIBLE)
            .inputMetrics(List.of(INPUT_METRIC_DTO_EX));

    public static final KpiDefinitionListDto KPI_DEFINITION_LIST_DTO_EX = new KpiDefinitionListDto().total(1).count(1).start(0).rows(10)
            .kpiDefs(List.of(KPI_DEFINITION_DTO_EX));

    public static final PmDefinitionDto PM_DEFINITION_DTO_EX = new PmDefinitionDto().name("VS_NS_NbrRegisteredSub_5GS")
            .source("something/something/NetworkSlice").description("PCC v1.22:13.1");

    public static final PmDefinitionListDto PM_DEFINITION_LIST_DTO_EX = new PmDefinitionListDto().total(1).count(1).start(0).rows(10)
            .pmDefs(List.of(PM_DEFINITION_DTO_EX));

    public static final KpiRefsDto KPI_REFS_DTO_EX = new KpiRefsDto().ref(KPI_DEF_NAME_EX);

    public static final ProfileDefinitionDto PROFILE_DEFINITION_DTO_EX = new ProfileDefinitionDto().name("5G Slice Assurance Subscriber Profile")
            .description("First simple profile using a single KPI resource").context(List.of("snssai")).kpis(List.of(KPI_REFS_DTO_EX));

    public static final ProfileDefinitionListDto PROFILE_DEFINITION_LIST_DTO_EX = new ProfileDefinitionListDto().total(1).count(1).start(0).rows(10)
            .profileDefs(List.of(PROFILE_DEFINITION_DTO_EX));

    public static final String RT_KPI_DEF_LIST_EX_STR = "{\"total\":1,\"count\":1,\"start\":0,\"rows\":10,\"kpi_defs\":[{\"kpi_name\":\"PDUSesMaxNbr\",\"kpi_type\":\"complex\",\"kpi_context\":[\"snssai\"],\"deployment_details\":{\"rt_name\":\"csac_01b45930_46d2_4991_a5b2_938ccd647bca\",\"rt_table\":\"kpi_csac_complex_snssai_15\",\"rt_definition\":{\"name\":\"csac_01b45930_46d2_4991_a5b2_938ccd647bca\",\"alias\":\"csac_complex_snssai\",\"expression\":\"SUM(kpi_csac_simple_snssai_15.csac_cc42516f_a1fa_4a2c_b3bd_d6bb97a7a1a5) FROM kpi_db://kpi_csac_simple_snssai_15\",\"object_type\":\"FLOAT\",\"aggregation_type\":\"SUM\",\"aggregation_period\":15,\"aggregation_elements\":[\"kpi_csac_simple_snssai_15.snssai\"],\"is_visible\":true,\"execution_group\":\"csac_execution_group\"}}}]}";

    // Define new model simple KPI definitions
    public static final String NEW_SIMPLE_KPI_DEF_STR = "{\n" + "            \"name\": \"sum_integer_1440_simple\",\n"
            + "            \"expression\": \"SUM(new_fact_table_0.pmCounters.integerColumn0)\",\n" + "            \"object_type\": \"INTEGER\",\n"
            + "            \"aggregation_type\": \"SUM\"\n" + "          }";

    // Define new model complex KPI definitions
    public static final String NEW_COMPLEX_KPI_DEF_STR = "{\n" + "            \"name\": \"sum_integer_60_complex\",\n"
            + "            \"expression\": \"SUM(kpi_simple_60.integer_simple) FROM kpi_db://kpi_simple_60\",\n"
            + "            \"object_type\": \"INTEGER\",\n" + "            \"aggregation_type\": \"SUM\"\n" + "          }";

    public static final String NEW_COMPLEX_KPI_DEF_ALL_STR = "{\n" + "            \"name\": \"sum_integer_60_complex\",\n"
            + "            \"expression\": \"SUM(kpi_simple_60.integer_simple) FROM kpi_db://kpi_simple_60\",\n"
            + "            \"object_type\": \"INTEGER\",\n" + "            \"aggregation_type\": \"SUM\",\n" + "            \"exportable\": true,\n"
            + "            \"execution_group\": \"COMPLEX1\"\n" + "          }";

    public static final String AUG_DEF_LIST_EX_STR = "{\"total\":1,\"count\":1,\"start\":0,\"rows\":10,\"augmentations\":[{\"ardq_id\":\"cardq\",\"ardq_url\":\"${cardq}\",\"ardq_type\":\"core\",\"ardq_rules\":[{\"input_schema\":\"5G|PM_COUNTERS|AMF_Mobility_NetworkSlice_1\",\"fields\":[{\"output\":\"nsi\",\"input\":[\"snssai\",\"nodeFDN\"]}]}],\"profiles\":[]}]}";

    // Define AAS response model
    public static final ArdqRegistrationResponseDto REGISTRATION_RESPONSE_DTO = ArdqRegistrationResponseDto.builder().ardqId("cardq")
            .ardqUrl("localhost:8080").rules(List.of("{}"))
            .schemaMappings(List.of(SchemaMappingResponseDto.builder().inputSchema("foo").outputSchema("bar").build())).build();

    public static final DeployedIndexDefinitionDto DEPLOYED_INDEX_DEFINITION_DTO_A = DeployedIndexDefinitionDto.builder()
            .indexDefinitionName("nameOfIndexerA")
            .indexDefinitionDescription("description of indexer A")
            .indexSource(IndexSourceDto.builder()
                    .indexSourceName("DataCatalog DataSource name")
                    .indexSourceType(IndexSourceDto.IndexSourceType.PM_STATS_EXPORTER)
                    .indexSourceDescription("DataCatalog DataSource description")
                    .build())
            .indexTarget(IndexTargetDto.builder()
                    .indexTargetName("search_index_a_name")
                    .indexTargetDisplayName("SearchIndexA_DisplayName")
                    .indexTargetDescription("SearchIndexA_Description")
                    .build())
            .indexWriters(Set.of(IndexWriterDto.builder()
                    .name("writerA_name")
                    .inputSchema("writerA_schemaRegistryName")
                    .contextFieldList(List.of(ContextFieldDto.builder()
                            .name("contextFieldA_name")
                            .contextFieldDisplayName("Context Field A")
                            .nameType(ContextFieldDto.ContextNameType.STRAIGHT)
                            .recordName("contextFieldA_recordName")
                            .description("contextFieldA Description")
                            .build()))
                    .valueFieldList(List.of(ValueFieldDto.builder()
                            .name("valueFieldA_name")
                            .valueFieldDisplayName("Value Field A")
                            .unit("errors/minute")
                            .type(ValueFieldDto.ValueFieldType.FLOAT)
                            .recordName("valueFieldA_recordName")
                            .description("valueFieldA Description")
                            .build()))
                    .infoFieldList(List.of(InfoFieldDto.builder()
                            .name("infoFieldA_name")
                            .infoFieldDisplayName("Info Field A")
                            .type(InfoFieldDto.InfoFieldType.STRING)
                            .recordName("infoFieldA_recordName")
                            .description("infoFieldA Description")
                            .build()))
                    .build()))
            .build();

    public static final DeployedIndexDefinitionDto DEPLOYED_INDEX_DEFINITION_DTO_B = DeployedIndexDefinitionDto.builder()
            .indexDefinitionName("nameOfIndexerB")
            .indexDefinitionDescription("description of indexer B")
            .indexSource(IndexSourceDto.builder()
                    .indexSourceName("DataCatalog DataSource name")
                    .indexSourceType(IndexSourceDto.IndexSourceType.PM_STATS_EXPORTER)
                    .indexSourceDescription("DataCatalog DataSource description")
                    .build())
            .indexTarget(IndexTargetDto.builder()
                    .indexTargetName("search_index_b_name")
                    .indexTargetDisplayName("SearchIndexB_DisplayName")
                    .indexTargetDescription("SearchIndexB_Description")
                    .build())
            .indexWriters(Set.of(IndexWriterDto.builder()
                    .name("writerB_name")
                    .inputSchema("writerB_schemaRegistryName")
                    .contextFieldList(List.of(ContextFieldDto.builder()
                            .name("contextFieldB_name")
                            .contextFieldDisplayName("Context Field B")
                            .nameType(ContextFieldDto.ContextNameType.STRAIGHT)
                            .recordName("contextFieldB_recordName")
                            .description("contextFieldB Description")
                            .build()))
                    .valueFieldList(List.of(ValueFieldDto.builder()
                            .name("valueFieldB_name")
                            .valueFieldDisplayName("Value Field B")
                            .unit("errors/minute")
                            .type(ValueFieldDto.ValueFieldType.FLOAT)
                            .recordName("valueFieldB_recordName")
                            .description("valueFieldB Description")
                            .build()))
                    .infoFieldList(List.of(InfoFieldDto.builder()
                            .name("infoFieldB_name")
                            .infoFieldDisplayName("Info Field B")
                            .type(InfoFieldDto.InfoFieldType.STRING)
                            .recordName("infoFieldB_recordName")
                            .description("infoFieldB Description")
                            .build()))
                    .build()))
            .build();

    public static final RtIndexDefListDto RT_INDEX_DEF_LIST_DTO = new RtIndexDefListDto()
            .total(1)
            .addIndexesItem(new RtIndexDefDto()
                    .name("nameOfIndexerA")
                    .description("description of indexer A")
                    .source(new RtIndexSourceDto()
                            .name("DataCatalog DataSource name")
                            .type(RtIndexSourceDto.TypeEnum.PMSTATSEXPORTER)
                            .description("DataCatalog DataSource description")
                    )
                    .target(new RtIndexTargetDto()
                            .name("search_index_a_name")
                            .displayName("SearchIndexA_DisplayName")
                            .description("SearchIndexA_Description")
                    )
                    .addWritersItem(new RtIndexWriterDto()
                            .name("writerA_name")
                            .inputSchema("writerA_schemaRegistryName")
                            .addContextItem(new RtIndexContextFieldDto()
                                    .name("contextFieldA_name")
                                    .displayName("Context Field A")
                                    .nameType(RtIndexContextFieldDto.NameTypeEnum.STRAIGHT)
                                    .recordName("contextFieldA_recordName")
                                    .description("contextFieldA Description")
                            )
                            .addValueItem(new RtIndexValueFieldDto()
                                    .name("valueFieldA_name")
                                    .displayName("Value Field A")
                                    .unit("errors/minute")
                                    .type("float")
                                    .recordName("valueFieldA_recordName")
                                    .description("valueFieldA Description")
                            )
                            .addInfoItem(new RtIndexInfoFieldDto()
                                    .name("infoFieldA_name")
                                    .displayName("Info Field A")
                                    .type(RtIndexInfoFieldDto.TypeEnum.STRING)
                                    .recordName("infoFieldA_recordName")
                                    .description("infoFieldA Description")
                            )
                    )
            );

    public static final String AIS_INDEX_DEFINITION_CREATE_OR_UPDATE_REQUEST_BODY = "{\"name\":\"nameOfIndexerA\",\"description\":\"description for indexer A\",\"source\":{\"name\":\"KafkaTopicName\",\"type\":\"pmstatsexporter\"},\"target\":{\"displayName\":\"SearchIndexA_DisplayName\",\"name\":\"search_index_a_name\",\"indexDescription\":\"SearchIndexA_Description\"},\"writers\":[{\"name\":\"writerA_name\",\"inputSchema\":\"writerA_schemaRegistryName\",\"context\":[{\"name\":\"contextFieldA_name\",\"displayName\":\"Context Field A\",\"nameType\":\"straight\",\"recordName\":\"contextFieldA_recordName\",\"description\":\"contextFieldA Description\"}],\"value\":[{\"name\":\"valueFieldA_name\",\"displayName\":\"Value Field A\",\"unit\":\"errors/minute\",\"type\":\"float\",\"recordName\":\"valueFieldA_recordName\",\"description\":\"valueFieldA Description\"}],\"info\":[{\"name\":\"infoFieldA_name\",\"displayName\":\"Info Field A\",\"type\":\"string\",\"recordName\":\"infoFieldA_recordName\",\"description\":\"infoFieldA Description\"}]}]}";

    public static final String PROVISIONING_STATE_INITIAL_STATE = "{\n" +
            "  \"id\" : 1,\n" +
            "  \"provisioningState\" : \"INITIAL\",\n" +
            "  \"provisioningStartTime\" : 1705599004000,\n" +
            "  \"provisioningEndTime\" : 1705599004000\n" +
            "}";

    public static final String PROVISIONING_STATE_COMPLETED_STATE = "{\n" +
            "  \"id\" : 2,\n" +
            "  \"provisioningState\" : \"COMPLETED\",\n" +
            "  \"provisioningStartTime\" : 1705599005000,\n" +
            "  \"provisioningEndTime\" : 1705599005100\n" +
            "}";

    public static final String PMSCHEMAS_EX = "{\n"
            + "  \"pmschemas\": [\n"
            + "    {\n"
            + "      \"schema_ref\": \"5G|PM_COUNTERS|AMF_Mobility_NetworkSlice_1\",\n"
            + "      \"schema_topic\": \"eric-oss-3gpp-pm-xml-core-parser-\",\n"
            + "      \"augmented\": false,\n"
            + "      \"pmdefs\": [\n"
            + "        \"pmCounters.VS_NS_NbrRegisteredSub_5GS\"\n"
            + "      ],\n"
            + "      \"contexts\": [\n"
            + "        \"snssai\",\n"
            + "        \"nodeFDN\"\n"
            + "      ]\n"
            + "    },\n"
            + "    {\n"
            + "      \"schema_ref\": \"5G|PM_COUNTERS|cardq_AMF_Mobility_NetworkSlice_1\",\n"
            + "      \"schema_topic\": \"eric-oss-assurance-augmentation-processing\",\n"
            + "      \"augmented\": true,\n"
            + "      \"pmdefs\": [\n"
            + "        \"pmCounters.VS_NS_NbrRegisteredSub_5GS\"\n"
            + "      ],\n"
            + "      \"contexts\": [\n"
            + "        \"site\",\n"
            + "        \"nssi\"\n"
            + "      ]\n"
            + "    }\n"
            + "  ]\n"
            + "}";

    // define PMSchemaDefinition

    public static final String PM_SCHEMA_NAME = "smf_nsmf_pdu_session_snssai_apn_1";

    public static final String PM_SCHEMA_URI_STR = "dc:5G|PM_COUNTERS|smf_nsmf_pdu_session_snssai_apn_1";

    public static final SchemaURI PM_SCHEMA_URI = SchemaURI.fromString(PM_SCHEMA_URI_STR);

    static final List<String> PM_SCHEMA_CONTEXT = List.of("nodeFDN", "snssai", "apn");

    static final String PM_COUNTER_NAME = "pmCounters.create_sm_context_resp_succ";

    static final String PM_COUNTER_DESC = "The number of successful Nsmf_PDUSession_CreateSMContext Response sent to AMF";

    public static final PMSchemaDefinition.PMCounter PM_COUNTER = PMSchemaDefinition.PMCounter.builder().name(PM_COUNTER_NAME)
            .description(PM_COUNTER_DESC).build();

    public static final PMSchemaDefinition VALID_PM_SCHEMA_DEFINITION_PM_COUNTER = PMSchemaDefinition.builder()
            .name(PM_SCHEMA_NAME)
            .uri(PM_SCHEMA_URI)
            .context(PM_SCHEMA_CONTEXT)
            .pmCounters(List.of(PM_COUNTER))
            .build();

    public static final PMSchemaDefinition VALID_PM_SCHEMA_DEFINITION_WO_PM_COUNTERS = PMSchemaDefinition.builder()
            .name(PM_SCHEMA_NAME)
            .uri(PM_SCHEMA_URI)
            .context(PM_SCHEMA_CONTEXT)
            .build();

    static final String VALID_DEF_WITH_ONE_COUNTER_STR = "{\"name\":\"smf_nsmf_pdu_session_snssai_apn_1\",\"uri\":\"dc:5G|PM_COUNTERS|smf_nsmf_pdu_session_snssai_apn_1\",\"pm_counters\":[{\"name\":\"pmCounters.create_sm_context_resp_succ\",\"description\":\"The number of successful Nsmf_PDUSession_CreateSMContext Response sent to AMF\"}],\"context\":[\"nodeFDN\",\"snssai\",\"apn\"]}\n";

    // Define runtime metadata
    public static final String RUNTIME_CONTEXT_METADATA_EX = """
            {
                    "id": "nodefdn_snssai",
                    "contextFields": [
                      {
                        "name": "snssai",
                        "displayName": "S-NSSAI",
                        "description": "The set of Network Slice Selection Assistance Information allowed by the 5G network operator for a particular 5G network slice"
                      },
                      {
                        "name": "nodeFDN",
                        "displayName": "Node FQDN",
                        "description": "Fully qualified node name"
                      }
                    ]
            }""";

    public static final String RUNTIME_CONTEXT_METADATA_EX_2 = """
            {
                    "id": "plmnid_qos_snssai",
                    "contextFields": [
                      {
                        "name": "plmnId"
                      },
                      {
                        "name": "qos"
                      },
                      {
                        "name": "snssai"
                      }
                    ]
            }""";

    public static final String RUNTIME_KPI_METADATA_EX = """
            {
                    "name": "DLDelay_GnbDu",
                    "displayName": "Downlink delay in gNB-DU for NRCellDU",
                    "type": "FLOAT",
                    "description": "Average packet transmission delay through the gNB-DU part to the UE. It is used to evaluate delay performance of gNB-DU in downlink"
            }""";

    public static final String RUNTIME_KPI_METADATA_EX_2 = """
            {
                    "name": "DLLat_gNB_DU",
                    "displayName": "Downlink Latency gNB-DU (Unit: ms)",
                    "type": "FLOAT",
                    "description": "This KPI describes the gNodeB-Distributed unit (gNB-DU), part of the packet transmission latency experienced by an end-user. It is used to evaluate the gNB latency contribution to the total packet latency. The KPI type is MEAN."
            }""";

    public static final String RUNTIME_KPI_METADATA_EX_3 = """
            {
                    "name": "DlUeThroughput",
                    "displayName": "DlUeThroughput (Unit: kbps)",
                    "type": "FLOAT",
                    "description": "This KPI describes the average downlink RAN UE throughput for a sub-network."
            }""";

    public static final String RUNTIME_KPI_METADATA_EX_4 = """
            {
                    "name": "PartialDRBAccessibility",
                    "displayName": "Partial DRB Accessibility (%)",
                    "type": "FLOAT",
                    "description": "This KPI describes the Data Radio Bearer (DRB) setup success rate, including the success rate for setting up Radio Resource Control (RRC) connection and Next Generation (NG) signaling connection"
            }""";
}