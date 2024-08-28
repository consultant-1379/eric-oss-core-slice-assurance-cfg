/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.repository.cache;

import static com.ericsson.oss.air.csac.model.TestResourcesUtils.DEPLOYED_SIMPLE_KPI_OBJ;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Optional;

import com.ericsson.oss.air.csac.model.pmsc.AggregationPeriod;
import com.ericsson.oss.air.csac.model.runtime.RuntimeKpiKey;
import com.ericsson.oss.air.csac.repository.DeployedKpiDefDAO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ResolvedKpiCacheTest {

    @Mock
    private DeployedKpiDefDAO deployedKpiDefDAO;

    @InjectMocks
    private ResolvedKpiCache resolvedKpiCache;

    private final RuntimeKpiKey runtimeKpiKey = RuntimeKpiKey.builder().withKpDefinitionName("testKpi").withAggregationFields(List.of("field1"))
            .withAggregationPeriod(AggregationPeriod.FIFTEEN.getValue()).build();

    @Test
    void get_noCacheExist() {
        assertEquals(Optional.empty(), this.resolvedKpiCache.get(runtimeKpiKey));
    }

    @Test
    void getFromCache() {
        this.resolvedKpiCache.put(runtimeKpiKey, DEPLOYED_SIMPLE_KPI_OBJ);
        assertEquals(DEPLOYED_SIMPLE_KPI_OBJ, this.resolvedKpiCache.get(runtimeKpiKey).get());
    }

    @Test
    void flushToRuntimeDS() {
        this.resolvedKpiCache.put(runtimeKpiKey, DEPLOYED_SIMPLE_KPI_OBJ);
        this.resolvedKpiCache.flush();
        verify(deployedKpiDefDAO, times(1)).createDeployedKpi(DEPLOYED_SIMPLE_KPI_OBJ, runtimeKpiKey.getKpDefinitionName(), List.of("field1"));
    }

    @Test
    void flushToRuntimeDSWithNoData() {
        this.resolvedKpiCache.flush();
        verify(deployedKpiDefDAO, times(0)).createDeployedKpi(any(), anyString(), List.of(anyString()));
    }

    @Test
    void deleteAll() {
        this.resolvedKpiCache.put(runtimeKpiKey, DEPLOYED_SIMPLE_KPI_OBJ);

        this.resolvedKpiCache.deleteAll();

        assertTrue(this.resolvedKpiCache.isEmpty());
    }
}
