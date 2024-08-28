/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.model.runtime.metadata;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

class KpiContextIdTest {

    @Test
    void test_ConstructNewKpiContextId() {
        assertEquals("a_b_c", KpiContextId.of(Set.of("C", "B", "A")).get());
        assertEquals("a_b_c", KpiContextId.of(Set.of("c", "B", "a")).get());
        assertEquals("a_b_c", KpiContextId.of(Set.of("C", "b", "A")).get());
        assertEquals("a_b_c", KpiContextId.of(Set.of(" c", " B ", "A ")).get());
        assertEquals("aa_b_c", KpiContextId.of(Set.of(" c", " B ", " A A ")).get());

        assertEquals(KpiContextId.of(Set.of("c", "B", "a")), KpiContextId.of(Set.of("C", "B", "A")));
        assertEquals(KpiContextId.of(Set.of("c c", "BB", "AA")), KpiContextId.of(Set.of("C c", "B b", "A a")));

    }

    @Test
    void test_sortKpiContextId() {

        final List<KpiContextId> kpiContextIds = List.of(
                KpiContextId.of(Set.of("C")),
                KpiContextId.of(Set.of("A")),
                KpiContextId.of(Set.of("B"))
        );

        final List<KpiContextId> sortedKpiContextIds = kpiContextIds.stream().sorted().toList();

        assertEquals("a", sortedKpiContextIds.get(0).get());
        assertEquals("b", sortedKpiContextIds.get(1).get());
        assertEquals("c", sortedKpiContextIds.get(2).get());

    }

    @Test
    void test_CreateNewKpiContextId() {
        assertEquals("a_b_c", KpiContextId.of("a_b_c").get());
        assertEquals("a_b_c", KpiContextId.of("A_B_C").get());
        assertEquals("", KpiContextId.of("").get());
    }

}