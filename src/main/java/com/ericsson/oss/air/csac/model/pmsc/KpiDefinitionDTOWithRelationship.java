/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.model.pmsc;

import com.ericsson.oss.air.csac.model.KPIDefinition;
import com.ericsson.oss.air.csac.model.ProfileDefinition;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NonNull;

/**
 * A child class of {@link KpiDefinitionDTO} adding new fields:
 *
 * <ul>
 * <li>kpi name of {@link KPIDefinition} </li>
 * <li>profile {@link ProfileDefinition} </li>
 * </ul>
 * <p>
 * This class will be used in for the provisioning.
 */
@Data
public class KpiDefinitionDTOWithRelationship extends KpiDefinitionDTO {

    @NonNull
    @JsonIgnore
    private final String kpiName;

    @NonNull
    @JsonIgnore
    private final ProfileDefinition profile;

    public KpiDefinitionDTOWithRelationship(final KpiDefinitionDTO kpiDefinitionDTO, final String name, final ProfileDefinition profile) {
        super(kpiDefinitionDTO);
        this.kpiName = name;
        this.profile = profile;
    }

}
