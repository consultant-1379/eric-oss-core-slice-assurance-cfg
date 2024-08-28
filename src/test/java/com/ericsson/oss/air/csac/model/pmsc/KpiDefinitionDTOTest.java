/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.model.pmsc;

import static com.ericsson.oss.air.csac.model.TestResourcesUtils.DEPLOYED_COMPLEX_KPI_STR;
import static com.ericsson.oss.air.csac.model.TestResourcesUtils.DEPLOYED_SIMPLE_KPI_OBJ;
import static com.ericsson.oss.air.csac.model.TestResourcesUtils.DEPLOYED_SIMPLE_KPI_STR;
import static com.ericsson.oss.air.csac.model.TestResourcesUtils.VALID_PROFILE_DEF_AGGREGATION_FIELD;
import static com.ericsson.oss.air.csac.model.TestResourcesUtils.VALID_PROFILE_DEF_AGGREGATION_FIELD2;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import com.ericsson.oss.air.util.codec.Codec;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.validation.Validation;
import org.apache.commons.collections.CollectionUtils;
import org.junit.jupiter.api.Test;
import org.springframework.util.ObjectUtils;

class KpiDefinitionDTOTest {

    private static final Codec codec = new Codec(Validation.buildDefaultValidatorFactory().getValidator());

    @Test
    void testSimpleKPI() throws JsonProcessingException {

        final KpiDefinitionDTO kpi = codec.withValidation().readValue(DEPLOYED_SIMPLE_KPI_STR, KpiDefinitionDTO.class);

        assertFalse(ObjectUtils.isEmpty(kpi.getName()));
        assertFalse(ObjectUtils.isEmpty(kpi.getAlias()));
        assertFalse(ObjectUtils.isEmpty(kpi.getExpression()));
        assertFalse(ObjectUtils.isEmpty(kpi.getObjectType()));
        assertFalse(ObjectUtils.isEmpty(kpi.getAggregationType()));
        assertFalse(ObjectUtils.isEmpty(kpi.getAggregationElements()));
        assertFalse(ObjectUtils.isEmpty(kpi.getAggregationPeriod()));
        assertFalse(ObjectUtils.isEmpty(kpi.getIsVisible()));
        assertFalse(ObjectUtils.isEmpty(kpi.getInpDataCategory()));
        assertFalse(ObjectUtils.isEmpty(kpi.getInpDataIdentifier()));
        assertTrue(ObjectUtils.isEmpty(kpi.getExecutionGroup()));

        assertEquals("kpi_sum_integer_1440_simple_1440", kpi.getFactTableName());

    }

    @Test
    void testComplexKPI() throws JsonProcessingException {
        final KpiDefinitionDTO kpi = codec.withValidation().readValue(DEPLOYED_COMPLEX_KPI_STR, KpiDefinitionDTO.class);
        assertFalse(ObjectUtils.isEmpty(kpi.getName()));
        assertFalse(ObjectUtils.isEmpty(kpi.getAlias()));
        assertFalse(ObjectUtils.isEmpty(kpi.getExpression()));
        assertFalse(ObjectUtils.isEmpty(kpi.getObjectType()));
        assertFalse(ObjectUtils.isEmpty(kpi.getAggregationType()));
        assertFalse(ObjectUtils.isEmpty(kpi.getAggregationElements()));
        assertFalse(ObjectUtils.isEmpty(kpi.getAggregationPeriod()));
        assertFalse(ObjectUtils.isEmpty(kpi.getIsVisible()));
        assertTrue(ObjectUtils.isEmpty(kpi.getInpDataCategory()));
        assertTrue(ObjectUtils.isEmpty(kpi.getInpDataIdentifier()));
        assertFalse(ObjectUtils.isEmpty(kpi.getExecutionGroup()));
        assertEquals("kpi_sum_integer_60_complex_60", kpi.getFactTableName());
    }

    @Test
    void testContentEquals() throws JsonProcessingException {
        final KpiDefinitionDTO deployedKpi = DEPLOYED_SIMPLE_KPI_OBJ;

        final KpiDefinitionDTO deployedKpi2 = deployedKpi.toBuilder().withName("A random name").build();
        assertTrue(deployedKpi2.contentEquals(deployedKpi));

        final KpiDefinitionDTO deployedKpi3 = deployedKpi.toBuilder().withName("A random name").withIsVisible(false).build();
        assertFalse(deployedKpi3.contentEquals(deployedKpi));

        final KpiDefinitionDTO deployedKpi4 =
                deployedKpi.toBuilder().withName("A random name").withKpiType(KpiTypeEnum.COMPLEX).build();
        assertNotEquals(deployedKpi4.getKpiType(), deployedKpi.getKpiType());
        assertTrue(deployedKpi4.contentEquals(deployedKpi));
    }

    @Test
    void constructor_valid() {
        final KpiDefinitionDTO deployedKpi = DEPLOYED_SIMPLE_KPI_OBJ;

        final KpiDefinitionDTO kpi = new KpiDefinitionDTO(deployedKpi);
        assertTrue(deployedKpi.contentEquals(kpi));
    }

    @Test
    void getUnqualifiedAggregationElements() {

        final List<String> expectedUnqualifiedList = List.of(VALID_PROFILE_DEF_AGGREGATION_FIELD, VALID_PROFILE_DEF_AGGREGATION_FIELD2);

        assertTrue(CollectionUtils.isEqualCollection(expectedUnqualifiedList, DEPLOYED_SIMPLE_KPI_OBJ.getUnqualifiedAggregationElements()));
    }

    @Test
    void getUnqualifiedAggregationElements_NoAggElements() {

        assertTrue(CollectionUtils.isEqualCollection(new KpiDefinitionDTO().getAggregationElements(), CollectionUtils.EMPTY_COLLECTION));
    }
}
