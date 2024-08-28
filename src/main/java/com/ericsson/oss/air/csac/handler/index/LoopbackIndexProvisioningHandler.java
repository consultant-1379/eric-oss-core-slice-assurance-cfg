/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.handler.index;

import java.util.List;

import com.ericsson.oss.air.csac.model.ProfileDefinition;
import com.ericsson.oss.air.util.operator.StatefulSequentialOperator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Loopback implementation of the {@link IndexProvisioningHandler}.  The loopback handler is called when remote indexer configuration is disabled.
 */
@Slf4j
@Component
public class LoopbackIndexProvisioningHandler extends IndexProvisioningHandler {

    protected static final String INDEX_PROVISIONING_IS_DISABLED = "Index provisioning is disabled";

    /**
     * {@inheritDoc}
     *
     * The loopback handler returns a no-op rollback operator as no rollback is required.
     *
     * @return no-op loopback operator
     */
    @Override
    protected StatefulSequentialOperator getRollbackOperator() {
        return StatefulSequentialOperator.noop();
    }

    @Override
    protected void doApply(List<ProfileDefinition> profileDefinitions) {
        log.info(INDEX_PROVISIONING_IS_DISABLED);
    }
}
