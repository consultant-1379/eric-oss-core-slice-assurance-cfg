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

import static com.ericsson.oss.air.csac.model.TestResourcesUtils.DEPLOYED_SIMPLE_KPI_OBJ;
import static com.ericsson.oss.air.csac.model.TestResourcesUtils.VALID_PROFILE_DEF_OBJ;
import static com.ericsson.oss.air.csac.model.TestResourcesUtils.VALID_SIMPLE_KPI_DEF_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import com.ericsson.oss.air.csac.model.KPIReference;
import com.ericsson.oss.air.csac.model.ProfileDefinition;
import org.junit.jupiter.api.Test;

class KpiDefinitionDTOWithRelationshipTest {

    @Test
    void constructor_valid() {
        final KpiDefinitionDTO kpiDefinitionDTO = DEPLOYED_SIMPLE_KPI_OBJ.toBuilder().withKpiType(KpiTypeEnum.SIMPLE).build();

        final ProfileDefinition profile = VALID_PROFILE_DEF_OBJ.toBuilder()
                .kpis(List.of(KPIReference.builder().ref(VALID_SIMPLE_KPI_DEF_NAME).build()))
                .build();

        final KpiDefinitionDTOWithRelationship kpiDefRel = new KpiDefinitionDTOWithRelationship(kpiDefinitionDTO, "test", profile);
        assertEquals("test", kpiDefRel.getKpiName());
        assertEquals(profile, kpiDefRel.getProfile());
        assertEquals(kpiDefinitionDTO, new KpiDefinitionDTO(kpiDefRel));
    }

}