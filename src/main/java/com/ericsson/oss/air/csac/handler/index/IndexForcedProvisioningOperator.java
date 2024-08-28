/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.handler.index;

import com.ericsson.oss.air.util.operator.SequentialOperator;

/**
 * Base type for forced index provisioning operations.  If configured, CSAC can force the runtime index provisioning when no changes are detected
 * in dictionary or runtime configuration.
 */
public abstract class IndexForcedProvisioningOperator extends SequentialOperator<Void> {
}
