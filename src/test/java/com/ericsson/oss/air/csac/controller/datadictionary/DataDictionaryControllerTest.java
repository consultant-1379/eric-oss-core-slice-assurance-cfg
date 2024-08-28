/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.controller.datadictionary;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.ericsson.oss.air.api.model.AugmentationDto;
import com.ericsson.oss.air.api.model.AugmentationFieldDto;
import com.ericsson.oss.air.api.model.AugmentationListDto;
import com.ericsson.oss.air.api.model.AugmentationRuleDto;
import com.ericsson.oss.air.api.model.InputMetricDto;
import com.ericsson.oss.air.api.model.KpiDefinitionDto;
import com.ericsson.oss.air.api.model.KpiDefinitionListDto;
import com.ericsson.oss.air.api.model.PmDefinitionDto;
import com.ericsson.oss.air.api.model.PmDefinitionListDto;
import com.ericsson.oss.air.csac.handler.request.AugmentationRequestHandler;
import com.ericsson.oss.air.csac.handler.request.KPIDefRequestHandler;
import com.ericsson.oss.air.csac.handler.request.PMDefRequestHandler;
import com.ericsson.oss.air.csac.model.PMDefinition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class DataDictionaryControllerTest {

    private static final List<PMDefinition> PM_DEFINITIONS = List.of(new PMDefinition("nameOne", "sourceOne", "descOne"),
            new PMDefinition("nameTwo", "sourceTwo", "descTwo"));

    private static final List<PmDefinitionDto> PM_DEFINITION_DTOS = PM_DEFINITIONS.stream()
            .map(obj -> new PmDefinitionDto().name(obj.getName()).source(obj.getSource()).description(obj.getDescription()))
            .collect(Collectors.toList());

    private static final List<InputMetricDto> INPUT_METRIC_DTOS = Collections.singletonList(
            new InputMetricDto().id("id1").alias("p0").type(InputMetricDto.TypeEnum.PM_DATA));

    private static final List<KpiDefinitionDto> KPI_DEFINITION_DTOS = Arrays.asList(
            new KpiDefinitionDto().name("name1").description("des1").displayName("displayName1").expression("AVG(p0)").aggregationType("AVG")
                    .isVisible(true).inputMetrics(INPUT_METRIC_DTOS),
            new KpiDefinitionDto().name("name2").description("des2").displayName("displayName2").expression("AVG(p1)").aggregationType("AVG")
                    .isVisible(true).inputMetrics(INPUT_METRIC_DTOS)
    );

    private static final List<AugmentationDto> AUGMENTATION_DTOS = Collections.singletonList(
            new AugmentationDto().ardqId("ardq_1").ardqUrl("url1").profiles(Collections.emptyList())
                    .addArdqRulesItem(
                            new AugmentationRuleDto().fields(List.of(new AugmentationFieldDto().output("field").input(List.of("input1", "input2"))))
                                    .inputSchema("schema"))
    );

    private final PmDefinitionListDto pmDefListDto = new PmDefinitionListDto();
    private final KpiDefinitionListDto kpiDefinitionListDto = new KpiDefinitionListDto();
    private final AugmentationListDto augmentationListDto = new AugmentationListDto();

    @MockBean
    private PMDefRequestHandler pmDefRequestHandler;

    @MockBean
    private KPIDefRequestHandler kpiDefRequestHandler;

    @MockBean
    private AugmentationRequestHandler augmentationRequestHandler;

    @Autowired
    private DataDictionaryController dictionaryController;

    @Autowired
    private TestRestTemplate restTemplate;

    @BeforeEach
    void setup() {
        pmDefListDto.total(2).count(2).start(0).rows(2).pmDefs(PM_DEFINITION_DTOS);
        kpiDefinitionListDto.total(2).count(2).start(0).rows(2).kpiDefs(KPI_DEFINITION_DTOS);
        this.augmentationListDto.total(1).count(1).start(0).rows(2).augmentations(AUGMENTATION_DTOS);
    }

    @Test
    void getPmdefs() {
        when(pmDefRequestHandler.getPMDefinitions(anyInt(), anyInt())).thenReturn(pmDefListDto);

        ResponseEntity<PmDefinitionListDto> response = restTemplate.getForEntity("/v1/dictionary/pmdefs", PmDefinitionListDto.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(pmDefListDto, response.getBody());
    }

    @Test
    void getPmdefsWithTrailingSlash() {
        when(pmDefRequestHandler.getPMDefinitions(anyInt(), anyInt())).thenReturn(pmDefListDto);

        ResponseEntity<PmDefinitionListDto> response = restTemplate.getForEntity("/v1/dictionary/pmdefs/", PmDefinitionListDto.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(pmDefListDto, response.getBody());
    }

    @Test
    void getPmdefs_bad_query() {
        ResponseEntity<PmDefinitionListDto> response = restTemplate.getForEntity("/v1/dictionary/pmdefs?start=0,rows=1", PmDefinitionListDto.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void getPmdefs_negative_start_BadRequest() {
        ResponseEntity<PmDefinitionListDto> response = restTemplate.getForEntity("/v1/dictionary/pmdefs?start=-1&rows=1", PmDefinitionListDto.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void getPmdefs_NegativeRows_GoodRequest() {

        final PmDefinitionListDto expected = new PmDefinitionListDto().start(0).rows(-1).count(2).total(2).pmDefs(PM_DEFINITION_DTOS);

        when(pmDefRequestHandler.getPMDefinitions(anyInt(), anyInt())).thenReturn(expected);

        ResponseEntity<PmDefinitionListDto> response = restTemplate.getForEntity("/v1/dictionary/pmdefs?start=0&rows=-1", PmDefinitionListDto.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        final PmDefinitionListDto actual = response.getBody();
        assertNotNull(actual);
        assertEquals(-1, actual.getRows());
        assertEquals(2, actual.getCount());
        assertEquals(actual.getCount(), actual.getPmDefs().size());
    }

    @Test
    void getPmdefs_InvalidStart_BadRequest() {
        ResponseEntity<PmDefinitionListDto> response = restTemplate.getForEntity("/v1/dictionary/kpis?start=test", PmDefinitionListDto.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void getPmdefs_InvalidRows_BadRequest() {
        ResponseEntity<PmDefinitionListDto> response = restTemplate.getForEntity("/v1/dictionary/kpis?rows=test", PmDefinitionListDto.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void getKPIs() {
        when(kpiDefRequestHandler.getKPIDefinitions(anyInt(), anyInt())).thenReturn(kpiDefinitionListDto);
        ResponseEntity<KpiDefinitionListDto> response = restTemplate.getForEntity("/v1/dictionary/kpis", KpiDefinitionListDto.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(kpiDefinitionListDto, response.getBody());
    }

    @Test
    void getKPIsWithTrailingSlash() {
        when(kpiDefRequestHandler.getKPIDefinitions(anyInt(), anyInt())).thenReturn(kpiDefinitionListDto);
        ResponseEntity<KpiDefinitionListDto> response = restTemplate.getForEntity("/v1/dictionary/kpis/", KpiDefinitionListDto.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(kpiDefinitionListDto, response.getBody());
    }

    @Test
    void getKPIs_badQuery() {
        ResponseEntity<KpiDefinitionListDto> response = restTemplate.getForEntity("/v1/dictionary/kpis?start=0rows=5", KpiDefinitionListDto.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void getKPIs_negativeStart_BadRequest() {
        ResponseEntity<KpiDefinitionListDto> response = restTemplate.getForEntity("/v1/dictionary/kpis?start=-1&rows=5", KpiDefinitionListDto.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void getKPIs_NegativeRows_GoodRequest() {

        final KpiDefinitionListDto expected = new KpiDefinitionListDto().start(0).rows(-1).count(2).total(2).kpiDefs(KPI_DEFINITION_DTOS);

        when(kpiDefRequestHandler.getKPIDefinitions(anyInt(), anyInt())).thenReturn(expected);

        ResponseEntity<KpiDefinitionListDto> response = restTemplate.getForEntity("/v1/dictionary/kpis?start=0&rows=-1", KpiDefinitionListDto.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        final KpiDefinitionListDto actual = response.getBody();
        assertNotNull(actual);
        assertEquals(-1, actual.getRows());
        assertEquals(2, actual.getCount());
        assertEquals(actual.getCount(), actual.getKpiDefs().size());
    }

    @Test
    void getKPIs_InvalidStart_BadRequest() {
        ResponseEntity<KpiDefinitionListDto> response = restTemplate.getForEntity("/v1/dictionary/kpis?start=test", KpiDefinitionListDto.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void getKPIs_InvalidRows_BadRequest() {
        ResponseEntity<KpiDefinitionListDto> response = restTemplate.getForEntity("/v1/dictionary/kpis?rows=test", KpiDefinitionListDto.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void getAugmentations() {

        when(this.augmentationRequestHandler.getAugmentationList(anyInt(), anyInt())).thenReturn(this.augmentationListDto);

        final ResponseEntity<AugmentationListDto> actual = restTemplate.getForEntity("/v1/dictionary/augmentations", AugmentationListDto.class);
        assertEquals(HttpStatus.OK, actual.getStatusCode());
        assertEquals(this.augmentationListDto, actual.getBody());
    }

    @Test
    void getAugmentationsWithTrailingSlash() {

        when(this.augmentationRequestHandler.getAugmentationList(anyInt(), anyInt())).thenReturn(this.augmentationListDto);

        final ResponseEntity<AugmentationListDto> actual = restTemplate.getForEntity("/v1/dictionary/augmentations/", AugmentationListDto.class);
        assertEquals(HttpStatus.OK, actual.getStatusCode());
        assertEquals(this.augmentationListDto, actual.getBody());
    }

    @Test
    void getAugmentations_OK_NegativeRows() {

        when(this.augmentationRequestHandler.getAugmentationList(anyInt(), anyInt())).thenReturn(this.augmentationListDto);

        final ResponseEntity<AugmentationListDto> actual = restTemplate.getForEntity("/v1/dictionary/augmentations?rows=-1",
                AugmentationListDto.class);
        assertEquals(HttpStatus.OK, actual.getStatusCode());
    }

    @Test
    void getAugmentations_InvalidRows_BadRequest() {

        final ResponseEntity<AugmentationListDto> actual = restTemplate.getForEntity("/v1/dictionary/augmentations?rows=test",
                AugmentationListDto.class);
        assertEquals(HttpStatus.BAD_REQUEST, actual.getStatusCode());
    }

    @Test
    void getAugmentations_InvalidStart_BadRequest() {

        final ResponseEntity<AugmentationListDto> actual = restTemplate.getForEntity("/v1/dictionary/augmentations?start=test",
                AugmentationListDto.class);
        assertEquals(HttpStatus.BAD_REQUEST, actual.getStatusCode());
    }

    @Test
    void getAugmentations_NegativeStart_BadRequest() {

        final ResponseEntity<AugmentationListDto> actual = restTemplate.getForEntity("/v1/dictionary/augmentations?start=-1",
                AugmentationListDto.class);
        assertEquals(HttpStatus.BAD_REQUEST, actual.getStatusCode());
    }
}