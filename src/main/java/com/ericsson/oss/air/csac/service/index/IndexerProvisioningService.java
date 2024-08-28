/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.service.index;

import com.ericsson.oss.air.csac.handler.validation.ValidationHandler;
import com.ericsson.oss.air.csac.model.runtime.index.DeployedIndexDefinitionDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * The {@code IndexerProvisioningService} provides an abstraction of the Assurance Indexer Service (AIS) for the
 * {@link com.ericsson.oss.air.csac.handler.index.IndexProvisioningHandler}.  This abstraction invokes the REST client for the indexer service and
 * transforms the HTTP responses into entities that can be consumed by the provisioning handler.
 */
@RequiredArgsConstructor
@Service
public class IndexerProvisioningService {

    private final IndexerRestClient indexerClient;

    private final ValidationHandler validationHandler;

    /**
     * Creates the provided index {@link DeployedIndexDefinitionDto definition} in the indexer service.
     *
     * @param indexDto
     *         index definition to create
     * @throws com.ericsson.oss.air.exception.CsacValidationException
     *         if the provided entity is invalid
     */
    public void create(final DeployedIndexDefinitionDto indexDto) {

        this.validationHandler.checkEntity(indexDto);

        this.indexerClient.create(indexDto);
    }

    /**
     * Updates the provided index {@link DeployedIndexDefinitionDto definition} in the indexer service.
     *
     * @param indexDto
     *         index definition to update
     * @throws com.ericsson.oss.air.exception.CsacValidationException
     *         if the provided entity is invalid
     */
    public void update(final DeployedIndexDefinitionDto indexDto) {

        this.validationHandler.checkEntity(indexDto);

        this.indexerClient.update(indexDto);
    }
}
