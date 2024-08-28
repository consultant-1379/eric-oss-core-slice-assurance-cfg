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

import com.ericsson.oss.air.csac.handler.util.ProvisioningHandler;
import com.ericsson.oss.air.csac.model.ProfileDefinition;

/**
 * Base type for all indexer provisioning handlers.
 */
public abstract class IndexProvisioningHandler extends ProvisioningHandler<List<ProfileDefinition>> {
}
