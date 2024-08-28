/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/
package com.ericsson.oss.air.csac.handler.request;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.ericsson.oss.air.api.model.RtIndexDefDto;
import com.ericsson.oss.air.api.model.RtIndexDefListDto;
import com.ericsson.oss.air.csac.model.TestResourcesUtils;
import com.ericsson.oss.air.csac.model.runtime.index.DeployedIndexDefinitionDto;
import com.ericsson.oss.air.csac.repository.DeployedIndexDefinitionDao;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
public class RuntimeIndexDefRequestHandlerTest {

    private final Stream<DeployedIndexDefinitionDto> deployedIndexDefsList = Stream.of(
            TestResourcesUtils.DEPLOYED_INDEX_DEFINITION_DTO_A,
            TestResourcesUtils.DEPLOYED_INDEX_DEFINITION_DTO_B
    );

    @Mock
    private DeployedIndexDefinitionDao deployedIndexDefinitionDao;

    @InjectMocks
    private RuntimeIndexDefRequestHandler runtimeIndexDefRequestHandler;

    @Test
    void test_retrievingRtIndexDefinitions() {
        when(this.deployedIndexDefinitionDao.stream()).thenReturn(this.deployedIndexDefsList);

        final RtIndexDefListDto actual = this.runtimeIndexDefRequestHandler.getRtIndexDefinitions();

        assertEquals(2, actual.getTotal());
        assertEquals(2, actual.getIndexes().size());
        assertEquals(List.of("nameOfIndexerA", "nameOfIndexerB"),
                actual.getIndexes().stream().map(RtIndexDefDto::getName).collect(Collectors.toList()));
        assertEquals(1, actual.getIndexes().get(0).getWriters().size());
        assertEquals(1, actual.getIndexes().get(1).getWriters().size());

        verify(this.deployedIndexDefinitionDao, times(1)).stream();
    }

}
