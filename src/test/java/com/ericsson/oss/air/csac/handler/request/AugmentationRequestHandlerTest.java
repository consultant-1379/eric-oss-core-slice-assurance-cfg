/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.handler.request;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import com.ericsson.oss.air.api.model.AugmentationDto;
import com.ericsson.oss.air.api.model.AugmentationListDto;
import com.ericsson.oss.air.csac.model.AugmentationDefinition;
import com.ericsson.oss.air.csac.model.AugmentationRule;
import com.ericsson.oss.air.csac.model.AugmentationRuleField;
import com.ericsson.oss.air.csac.repository.AugmentationDefinitionDAO;
import com.ericsson.oss.air.csac.repository.EffectiveAugmentationDAO;
import com.ericsson.oss.air.util.codec.Codec;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class AugmentationRequestHandlerTest {

    @Autowired
    private AugmentationRequestHandler requestHandler;

    @MockBean
    private AugmentationDefinitionDAO augmentationDao;

    @MockBean
    private EffectiveAugmentationDAO effectiveAugmentationDAO;

    private final AugmentationRuleField field1 = AugmentationRuleField.builder()
            .output("field")
            .inputFields(List.of("inp1", "inp2"))
            .build();

    private final AugmentationRuleField field2 = AugmentationRuleField.builder()
            .outputFields(List.of("field1", "field2"))
            .inputFields(List.of("inp1", "inp2"))
            .build();

    private final AugmentationRuleField field3 = AugmentationRuleField.builder()
            .output("field1")
            .outputFields(List.of("field2"))
            .inputFields(List.of("inp1", "inp2"))
            .build();

    private final AugmentationRule testRule1 = AugmentationRule.builder()
            .inputSchemaReference("schema")
            .fields(List.of(field1))
            .build();

    private final AugmentationRule testRule2 = AugmentationRule.builder()
            .inputSchemas(List.of("schema1", "schema2"))
            .fields(List.of(field2))
            .build();

    private final AugmentationRule testRule3 = AugmentationRule.builder()
            .inputSchemaReference("schema1")
            .inputSchemas(List.of("schema2"))
            .fields(List.of(field3))
            .build();

    private final AugmentationDefinition testAug1 = AugmentationDefinition.builder()
            .name("aug1")
            .augmentationRules(List.of(testRule1))
            .build();

    private final AugmentationDefinition testAug2 = AugmentationDefinition.builder()
            .name("aug2")
            .url("url2")
            .type("type2")
            .augmentationRules(List.of(testRule1))
            .build();

    private final AugmentationDefinition testAug3 = AugmentationDefinition.builder()
            .name("aug3")
            .url("url3")
            .type("type3")
            .augmentationRules(List.of(testRule2))
            .build();

    private final AugmentationDefinition testAug4 = AugmentationDefinition.builder()
            .name("aug4")
            .url("url4")
            .type("type4")
            .augmentationRules(List.of(testRule3))
            .build();

    private static final String EXPECTED_DTO_LIST_WITH_EFFECTIVE_AUG_STR = "{\"total\":1,\"count\":1,\"start\":0,\"rows\":1,\"augmentations\":[{\"ardq_id\":\"aug2\",\"ardq_url\":\"url2\",\"ardq_type\":\"type2\",\"ardq_rules\":[{\"input_schema\":\"schema\",\"fields\":[{\"output\":\"field\",\"input\":[\"inp1\",\"inp2\"]}]}],\"profiles\":[\"profile\"]}]}";

    private static final String EXPECTED_DTO_FOR_DEF_WITH_SCHEMA_LIST_AND_OUTPUT_FIELDS_LIST_STR =
            "{\"ardq_id\":\"aug3\",\"ardq_url\":\"url3\",\"ardq_type\":\"type3\","
                    + "\"ardq_rules\":["
                    + "{\"input_schema\":\"schema1\",\"fields\":[{\"output\":\"field1\",\"input\":[\"inp1\",\"inp2\"]},{\"output\":\"field2\",\"input\":[\"inp1\",\"inp2\"]}]},"
                    + "{\"input_schema\":\"schema2\",\"fields\":[{\"output\":\"field1\",\"input\":[\"inp1\",\"inp2\"]},{\"output\":\"field2\",\"input\":[\"inp1\",\"inp2\"]}]}],"
                    + "\"profiles\":[\"profile\"]}";

    private static AugmentationListDto expecteAugListWithEffective;

    @BeforeAll
    public static void setUpClass() throws Exception {

        expecteAugListWithEffective = new Codec().readValue(EXPECTED_DTO_LIST_WITH_EFFECTIVE_AUG_STR, AugmentationListDto.class);

    }

    @Test
    void getAugmentationList_noEffectiveAugmentations() {

        when(this.augmentationDao.findAll(0, 1)).thenReturn(List.of(testAug1));
        when(this.augmentationDao.totalAugmentationDefinitions()).thenReturn(1);

        final AugmentationListDto actual = this.requestHandler.getAugmentationList(0, 1);

        assertEquals(1, actual.getTotal());
        assertEquals(1, actual.getCount());
        assertEquals(1, actual.getAugmentations().size());
    }

    @Test
    void getAugmentationList_withOptionalFields() {

        when(this.augmentationDao.findAll(0, 1)).thenReturn(List.of(testAug2));
        when(this.augmentationDao.totalAugmentationDefinitions()).thenReturn(1);

        final AugmentationListDto actual = this.requestHandler.getAugmentationList(0, 1);

        assertEquals(1, actual.getTotal());
        assertEquals(1, actual.getCount());
        assertEquals(1, actual.getAugmentations().size());
    }

    @Test
    void getAugmentationList_withEffectiveAugmentations() {

        when(this.augmentationDao.findAll(0, 1)).thenReturn(List.of(testAug2));
        when(this.augmentationDao.totalAugmentationDefinitions()).thenReturn(1);
        when(this.effectiveAugmentationDAO.findById(any())).thenReturn(Optional.of(testAug2));
        when(this.effectiveAugmentationDAO.findAllProfileNames(any())).thenReturn(List.of("profile"));

        final AugmentationListDto actual = this.requestHandler.getAugmentationList(0, 1);

        assertEquals(1, actual.getTotal());
        assertEquals(1, actual.getCount());
        assertEquals(1, actual.getAugmentations().size());

        assertEquals(expecteAugListWithEffective, actual);
    }

    @Test
    void mapDefinitionToDto_withInputSchemaListAndOutputFieldList() throws Exception {
        final AugmentationDto expected = new Codec().readValue(
                EXPECTED_DTO_FOR_DEF_WITH_SCHEMA_LIST_AND_OUTPUT_FIELDS_LIST_STR,
                AugmentationDto.class);

        when(this.effectiveAugmentationDAO.findById(any())).thenReturn(Optional.of(testAug3));
        when(this.effectiveAugmentationDAO.findAllProfileNames(any())).thenReturn(List.of("profile"));

        final AugmentationDto actual = this.requestHandler.mapDefinitionToDto(testAug3);

        assertEquals(expected, actual);
    }

    @Test
    void mapDefinitionToDto_withAllFields() throws Exception {
        final AugmentationDto expected = new Codec().readValue(
                EXPECTED_DTO_FOR_DEF_WITH_SCHEMA_LIST_AND_OUTPUT_FIELDS_LIST_STR,
                AugmentationDto.class);
        expected.setArdqId("aug4");
        expected.setArdqUrl("url4");
        expected.setArdqType("type4");

        when(this.effectiveAugmentationDAO.findById(any())).thenReturn(Optional.of(testAug4));
        when(this.effectiveAugmentationDAO.findAllProfileNames(any())).thenReturn(List.of("profile"));

        final AugmentationDto actual = this.requestHandler.mapDefinitionToDto(testAug4);

        assertEquals(expected, actual);

    }

}