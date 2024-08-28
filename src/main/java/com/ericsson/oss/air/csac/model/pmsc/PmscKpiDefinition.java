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

import java.util.regex.Pattern;

import com.ericsson.oss.air.csac.handler.pmsc.util.KpiId;

/**
 * Represents all PMSC KPI Dtos, regardless of PMSC version
 */
public interface PmscKpiDefinition {

    /**
     * Valid pattern for KPI aliases in the generated PMSC KPI submission.
     */
    String VALID_KPI_ALIAS_PATTERN = "^[a-z][a-z0-9_]{0," + (KpiId.getMaxAliasLength() - 1) + "}$";

    Pattern ALIAS_PATTERN_CHECKER = Pattern.compile(VALID_KPI_ALIAS_PATTERN);

    /**
     * Checks the validity of the generated alias for this KPI instance.  If the alias is invalid, an IllegalArgumentException is thrown.
     *
     * @param alias
     * @throws IllegalArgumentException
     *         if the specified alias is invalid.
     */
    static void checkKpiAlias(final String alias) {

        if (!isValidKpiAlias(alias)) {
            throw new IllegalArgumentException("Invalid alias: \"" + alias + "\"");
        }
    }

    /**
     * Returns true if the specified alias is valid.
     *
     * @param alias
     *         alias to validate
     * @return true if the specified alias is valid
     */
    static boolean isValidKpiAlias(final String alias) {
        return ALIAS_PATTERN_CHECKER.matcher(alias).matches();
    }

}
