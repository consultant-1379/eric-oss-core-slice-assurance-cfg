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

import java.util.Collection;
import java.util.List;

import com.ericsson.oss.air.csac.model.runtime.index.DeployedIndexDefinitionDto;
import org.springframework.http.ResponseEntity;

public interface IndexerRestClient {

    /**
     * Creates a new index {@link DeployedIndexDefinitionDto definition} in the AIS.
     *
     * @param indexDto index definition to create
     */
    default ResponseEntity<Void> create(final DeployedIndexDefinitionDto indexDto) {
        throw new UnsupportedOperationException();
    }

    /**
     * Updates an existing index {@link DeployedIndexDefinitionDto definition} in the AIS.
     *
     * @param indexDto index definition to update
     */
    default ResponseEntity<Void> update(final DeployedIndexDefinitionDto indexDto) {
        throw new UnsupportedOperationException();
    }

    /**
     * Deletes the specified index {@link DeployedIndexDefinitionDto definitions} in the AIS.
     *
     * @param indexIds Ids of the index definitions to delete.
     */
    default void deleteById(final List<String> indexIds) {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns the specified index {@link DeployedIndexDefinitionDto definition} from the AIS, if it exists.
     *
     * @return the specified index definition, if it exists.
     */
    default ResponseEntity<DeployedIndexDefinitionDto> get(final String indexId) {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns a collection of all deployed index {@link DeployedIndexDefinitionDto definitions} in the AIS.
     *
     * @return a collection of all deployed index definitions in the AIS.
     */
    default ResponseEntity<Collection<DeployedIndexDefinitionDto>> getAll() {
        throw new UnsupportedOperationException();
    }

    /**
     * Deletes all deployed {@link DeployedIndexDefinitionDto} definitions in the AIS
     */
    default void deleteAll() {
        throw new UnsupportedOperationException();
    }

    /**
     * Deletes a list of {@link DeployedIndexDefinitionDto} definitions in the AIS
     *
     * @param definitions a list of deployed definitions dto
     */
    default void delete(final List<DeployedIndexDefinitionDto> definitions) {
        throw new UnsupportedOperationException();
    }
}
