/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.handler.kpi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.ericsson.oss.air.csac.model.ProfileDefinition;
import com.ericsson.oss.air.util.operator.StatefulSequentialOperator;

class KpiProvisioningHandlerTest {

    private static final class TestKpiProvisioningHandler extends KpiProvisioningHandler {

        @Override
        protected void doApply(final List<ProfileDefinition> profileDefinitions) {
            // much ado about nothing
        }
    }

    @Test
    void getRollbackOperator() {

        final StatefulSequentialOperator actual = new TestKpiProvisioningHandler().getRollbackOperator();

        assertNotNull(actual);
        assertEquals(StatefulSequentialOperator.noop(), actual);
    }
}