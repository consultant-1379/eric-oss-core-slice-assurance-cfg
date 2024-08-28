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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.ericsson.oss.air.csac.model.runtime.index.DeployedIndexDefinitionDto;
import com.ericsson.oss.air.csac.repository.DeployedIndexDefinitionDao;
import com.ericsson.oss.air.util.codec.Codec;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

/**
 * Dry run implementation of the {@link IndexerRestClient}.  This client simply logs activities for development and testing purposes rather than
 * submit requests to a live AIS service.
 */
@Service
@Profile({ "dry-run" })
@RequiredArgsConstructor
@Slf4j
public class DryRunIndexerRestClient implements IndexerRestClient {

    private final Codec codec;

    private final DeployedIndexDefinitionDao indexDefinitionDao;

    @Override
    @SneakyThrows
    public ResponseEntity<Void> create(final DeployedIndexDefinitionDto indexDto) {

        log.info("Creating index definition {}: {}", indexDto.indexDefinitionName(), this.codec.writeValueAsString(indexDto));
        return ResponseEntity.ok().build();
    }

    @Override
    @SneakyThrows
    public ResponseEntity<Void> update(final DeployedIndexDefinitionDto indexDto) {

        log.info("Updating index definition {}: {}", indexDto.indexDefinitionName(), this.codec.writeValueAsString(indexDto));
        return ResponseEntity.ok().build();
    }

    @Override
    public void deleteById(final List<String> ids) {
        log.info("Deleting the list of deployed index definitions: {}", ids);
    }

    @Override
    public void delete(final List<DeployedIndexDefinitionDto> definitionDtos) {
        this.deleteById(definitionDtos.stream().map(DeployedIndexDefinitionDto::indexDefinitionName).collect(Collectors.toList()));
    }

    @Override
    public void deleteAll() {
        final List<String> ids = new ArrayList<>();
        this.indexDefinitionDao.findAll().forEach(deployedIndexDefinitionDto -> ids.add(deployedIndexDefinitionDto.indexDefinitionName()));
        this.deleteById(ids);
    }
}
