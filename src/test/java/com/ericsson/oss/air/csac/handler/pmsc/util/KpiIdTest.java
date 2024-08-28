/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.handler.pmsc.util;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.UUID;

import com.ericsson.oss.air.csac.model.pmsc.PmscKpiDefinition;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

class KpiIdTest {

    @Test
    void testGenerateId_defaultLength() {

        final List<String> aliasElements = List.of("csac", "simple", "plmnid", "snssai", "gnbName");
        final String expected = "csac_simple_plmnid_snssai_gnbname";

        final KpiId idGenerator = new KpiId();

        final String actual = idGenerator.generateId(aliasElements);

        assertTrue(PmscKpiDefinition.isValidKpiAlias(actual));
        assertDoesNotThrow(() -> PmscKpiDefinition.checkKpiAlias(actual));
        assertEquals(expected, actual);
    }

    @Test
    void testGenerateValidId_longSourceLength() {

        final List<String> aliasElements = List.of("csac", "simple", "plmnid", "snssai", "managedElement", "moValue");
        final String expected = "cc4839333801783c868e82a664117b1a4e59e5";  // as a hash, it is predictable

        final KpiId idGenerator = new KpiId();

        final String actual = idGenerator.generateId(aliasElements);

        assertTrue(PmscKpiDefinition.isValidKpiAlias(actual));
        assertDoesNotThrow(() -> PmscKpiDefinition.checkKpiAlias(actual));
        assertEquals(expected, actual);
    }

    @Test
    void testGenerateValidId_setLength() {

        final List<String> aliasElements = List.of("csac", "simple", "plmnid", "snssai", "managedElement", "moValue");
        final String expected = "cc4839333801783c868e";  // as a hash, it is predictable

        final KpiId idGenerator = new KpiId().maxLength(20);

        final String actual = idGenerator.generateId(aliasElements);

        assertEquals(20, actual.length());
        assertEquals(20, idGenerator.getIdLength());
        assertTrue(PmscKpiDefinition.isValidKpiAlias(actual));
        assertDoesNotThrow(() -> PmscKpiDefinition.checkKpiAlias(actual));
        assertEquals(expected, actual);
    }

    @Test
    void testGenerateKpiName() {

        final UUID emptyUUID = UUID.fromString("00000000-0000-0000-0000-000000000000");
        try (final MockedStatic<UUID> utilities = Mockito.mockStatic(UUID.class)) {
            utilities.when(UUID::randomUUID).thenReturn(emptyUUID);
            final String sampleKpiName = new KpiId().generateKpiName();
            Assertions.assertEquals("csac_00000000_0000_0000_0000_000000000000", sampleKpiName);
        }
    }

    @Test
    void testGenerateAlias() {

        final List<String> aliasElements = List.of("csac", "simple", "plmnid", "snssai", "gnbName");
        final String expected = "csac_simple_plmnid_snssai_gnbname";

        final KpiId idGenerator = new KpiId();

        final String actual = idGenerator.generateAlias(aliasElements);

        assertTrue(PmscKpiDefinition.isValidKpiAlias(actual));
        assertDoesNotThrow(() -> PmscKpiDefinition.checkKpiAlias(actual));
        assertEquals(expected, actual);

    }
}