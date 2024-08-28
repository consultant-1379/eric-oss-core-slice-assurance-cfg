/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.model.runtime;

import com.ericsson.oss.air.csac.model.pmsc.KpiDefinitionDTO;
import com.ericsson.oss.air.csac.model.pmsc.KpiTypeEnum;

/**
 * Base type for all runtime KPI definitions, where the runtime KPI is the object submitted to a downstream analytics engine for calculation.
 * Currently, only the PM Stats Calculator ("pmsc") is supported.
 */
@SubTypes({ KpiDefinitionDTO.class })
public interface RuntimeKpiDefinition {

    /**
     * Returns the fact table name for this KPI definition.  The 'fact table' is the name of the source record or database table name containing this
     * KPI definition.
     *
     * @return the fact table name for this KPI definition
     */
    default String getFactTableName() {
        return "";
    }

    /**
     * Returns the PM Stats Calculator KPI type.
     *
     * @return the PM Stats Calculator KPI type
     */
    KpiTypeEnum getKpiType();
}
