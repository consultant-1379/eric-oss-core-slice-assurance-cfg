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

import static org.junit.Assert.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class PmscKpiDefinitionTest {

    @Test
    void checkValidKpiAlias() {

        final String testAlias = "this_is_a_valid_kpi_alias";

        PmscKpiDefinition.checkKpiAlias(testAlias);
    }

    @Test
    void checkInvalidKpiAlias_upperCaseChars() {

        final String testAlias = "csac_complex_snssai_moFdn";

        final Exception ex = assertThrows(IllegalArgumentException.class, () -> PmscKpiDefinition.checkKpiAlias(testAlias),
                "Expected IllegalArgumentException");

    }

    @Test
    void checkInvalidKapiAlias_tooManyChars() {

        final String testAlias = "a".repeat(60);

        final Exception ex = assertThrows(IllegalArgumentException.class, () -> PmscKpiDefinition.checkKpiAlias(testAlias),
                "Expected IllegalArgumentException");
    }

    @Test
    void checkInvalidKpiAlias_nonAlphaLeadingChars() {

        final String testAlias = "1_abc";

        final Exception ex = assertThrows(IllegalArgumentException.class, () -> PmscKpiDefinition.checkKpiAlias(testAlias),
                "Expected IllegalArgumentException");
    }
    
    @Test
    void testIsValidKpiAlias() {

        final String actual = "csac_simple_plmnid_snssai_managedelement_movalue";

        assertFalse(PmscKpiDefinition.isValidKpiAlias(actual));
    }
}
