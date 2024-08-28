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

import static com.ericsson.oss.air.csac.model.TestResourcesUtils.DEPLOYED_INDEX_DEFINITION_DTO_A;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import com.ericsson.oss.air.csac.model.runtime.index.DeployedIndexDefinitionDto;
import org.junit.jupiter.api.Test;

class IndexerRestClientTest {

    private final IndexerRestClient testClient = new IndexerRestClient() {
    };

    @Test
    void create() {
        assertThrows(UnsupportedOperationException.class, () -> this.testClient.create(new DeployedIndexDefinitionDto()));
    }

    @Test
    void update() {
        assertThrows(UnsupportedOperationException.class, () -> this.testClient.update(new DeployedIndexDefinitionDto()));
    }

    @Test
    void delete() {
        assertThrows(UnsupportedOperationException.class, () -> this.testClient.delete(List.of(DEPLOYED_INDEX_DEFINITION_DTO_A)));
    }

    @Test
    void get() {
        assertThrows(UnsupportedOperationException.class, () -> this.testClient.get("id"));
    }

    @Test
    void getAll() {
        assertThrows(UnsupportedOperationException.class, () -> this.testClient.getAll());
    }
}