/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.handler.pmsc.transform;

import static com.ericsson.oss.air.csac.model.pmsc.PMSCKpiUtil.CSAC_KPI_DEF_SOURCE;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashMap;
import java.util.List;

import com.ericsson.oss.air.csac.model.pmsc.KpiDefinitionDTO;
import com.ericsson.oss.air.csac.model.pmsc.KpiDefinitionSubmission;
import com.ericsson.oss.air.csac.model.pmsc.KpiTypeEnum;
import com.ericsson.oss.air.csac.model.pmsc.LegacyKpiSubmissionDto;
import com.ericsson.oss.air.util.codec.Codec;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.validation.Validation;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = { "provisioning.pmsc.enabled=true", "provisioning.pmsc.model.legacy=true" },
                classes = { KpiSubmissionTransformer.class,
                        KpiOutputTablesSubmissionTransformer.class, LegacyKpiSubmissionTransformer.class })
class LegacyKpiSubmissionTransformerTest {

    @Autowired
    private KpiSubmissionTransformer kpiSubmissionTransformer;

    public static final String SERIALIZED_KPIS = "{\"source\":\"CSAC_KPI_DEF_SOURCE\",\"kpi_definitions\":[{\"name\":\"simpleOne\",\"alias\":\"csac_simple_kafka_aggelement\",\"expression\":\"exp\",\"object_type\":\"objType\",\"aggregation_type\":\"aggType\",\"aggregation_period\":1,\"aggregation_elements\":[\"table.aggElement\"],\"is_visible\":true,\"inp_data_category\":\"pm_data\",\"inp_data_identifier\":\"kafka\",\"execution_group\":\"\"},{\"name\":\"simpleTwo\",\"alias\":\"csac_simple_kafka_aggelement\",\"expression\":\"exp\",\"object_type\":\"objType\",\"aggregation_type\":\"aggType\",\"aggregation_period\":1,\"aggregation_elements\":[\"table.aggElement\"],\"is_visible\":true,\"inp_data_category\":\"pm_data\",\"inp_data_identifier\":\"kafka\",\"execution_group\":\"\"},{\"name\":\"complexOne\",\"alias\":\"complexone\",\"expression\":\"exp\",\"object_type\":\"objType\",\"aggregation_type\":\"aggType\",\"aggregation_period\":1,\"aggregation_elements\":[\"table.aggElement\"],\"is_visible\":true,\"execution_group\":\"COMPLEX\"},{\"name\":\"complexTwo\",\"alias\":\"complextwo\",\"expression\":\"exp\",\"object_type\":\"objType\",\"aggregation_type\":\"aggType\",\"aggregation_period\":1,\"aggregation_elements\":[\"table.aggElement\"],\"is_visible\":true,\"execution_group\":\"COMPLEX\"}]}";
    private static final List<String> aggElement = List.of("table.aggElement");
    private static final KpiDefinitionDTO KPI_SAMPLE = KpiDefinitionDTO.builder().withName("simpleOne").withExpression("exp")
            .withObjectType("objType").withAggregationType("aggType").withAggregationPeriod(1).withAggregationElements(aggElement).withIsVisible(true)
            .withExecutionGroup("").withKpiType(KpiTypeEnum.SIMPLE).build();
    private static final KpiDefinitionDTO KPI_SIMPLE_ONE = KPI_SAMPLE.toBuilder().withName("simpleOne").withInpDataCategory("pm_data")
            .withInpDataIdentifier("kafka").build();
    private static final KpiDefinitionDTO KPI_SIMPLE_TWO = KPI_SAMPLE.toBuilder().withName("simpleTwo").withInpDataCategory("pm_data")
            .withInpDataIdentifier("kafka").build();
    private static final KpiDefinitionDTO KPI_COMPLEX_ONE = KPI_SAMPLE.toBuilder().withName("complexOne").withExecutionGroup("COMPLEX")
            .withKpiType(KpiTypeEnum.COMPLEX).build();
    private static final KpiDefinitionDTO KPI_COMPLEX_TWO = KPI_SAMPLE.toBuilder().withName("complexTwo")
            .withKpiType(KpiTypeEnum.COMPLEX).withExecutionGroup("COMPLEX").build();
    public static final List<KpiDefinitionDTO> KPI_DEF_LIST = List.of(KPI_SIMPLE_ONE, KPI_SIMPLE_TWO, KPI_COMPLEX_ONE, KPI_COMPLEX_TWO);
    private final Codec codec = new Codec(Validation.buildDefaultValidatorFactory().getValidator());

    @Test
    void testLegacyKpiSubmissionTransformer() throws JsonProcessingException {
        final LegacyKpiSubmissionDto submission = (LegacyKpiSubmissionDto) new LegacyKpiSubmissionTransformer().apply(KPI_DEF_LIST);

        assertEquals(CSAC_KPI_DEF_SOURCE, submission.getSource());
        assertEquals(KPI_SIMPLE_ONE, submission.getKpiDefinitionsList().get(0));
        assertEquals(KPI_SIMPLE_TWO, submission.getKpiDefinitionsList().get(1));
        assertEquals(KPI_COMPLEX_ONE, submission.getKpiDefinitionsList().get(2));
        assertEquals(KPI_COMPLEX_TWO, submission.getKpiDefinitionsList().get(3));

        // string compare for JSON strings is risky because there is no inherent guarantee of order in a JSON object
        // comparing as HashMaps is more reliable since the two maps are deep-compared
        assertEquals(this.codec.readValue(SERIALIZED_KPIS, HashMap.class),
                this.codec.readValue(this.codec.writeValueAsString(submission), HashMap.class));
    }

    @Test
    void testLegacyKpiSubmissionTransformer_autowiredBean() throws JsonProcessingException {
        final KpiDefinitionSubmission submission = kpiSubmissionTransformer.apply(KPI_DEF_LIST);

        LegacyKpiSubmissionDto l = (LegacyKpiSubmissionDto) submission;
        l.getKpiDefinitionsList().get(0);

        Object expected = this.codec.readValue(SERIALIZED_KPIS, HashMap.class);
        Object actual = this.codec.readValue(this.codec.writeValueAsString(submission), HashMap.class);

        assertEquals(this.codec.readValue(SERIALIZED_KPIS, HashMap.class),
                this.codec.readValue(this.codec.writeValueAsString(submission), HashMap.class));
    }
}