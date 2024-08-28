/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.repository;

import java.util.stream.Stream;

import com.ericsson.oss.air.csac.model.runtime.index.DeployedIndexDefinitionDto;

/**
 * DAO API for successfully deployed {@link DeployedIndexDefinitionDao} instances.
 */
public interface DeployedIndexDefinitionDao extends DaoRepository<DeployedIndexDefinitionDto, String> {

    /**
     * Returns a stream of {@link DeployedIndexDefinitionDao} from the repository.
     *
     * @param <S>
     *         type or subtype of {@code DeployedIndexDefinitionDao}
     * @return a stream of {@code DeployedIndexDefinitionDao} from the repository.
     */
    <S extends DeployedIndexDefinitionDto> Stream<S> stream();
}
