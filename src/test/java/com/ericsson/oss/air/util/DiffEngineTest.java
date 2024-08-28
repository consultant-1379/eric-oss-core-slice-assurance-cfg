/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import java.util.List;
import java.util.function.Function;

import com.ericsson.oss.air.csac.model.AugmentationDefinition;
import com.ericsson.oss.air.csac.model.AugmentationRule;
import com.ericsson.oss.air.csac.model.AugmentationRuleField;
import org.junit.jupiter.api.Test;

class DiffEngineTest {

    AugmentationRuleField field1 = AugmentationRuleField.builder()
            .output("field1")
            .inputFields(List.of("input1", "input2"))
            .build();

    AugmentationRuleField field2 = AugmentationRuleField.builder()
            .output("field2")
            .inputFields(List.of("input1", "input2"))
            .build();

    private final AugmentationRule rule1 = AugmentationRule.builder()
            .inputSchemaReference("schema1")
            .fields(List.of(field1))
            .build();

    private final AugmentationRule rule2 = AugmentationRule.builder()
            .inputSchemaReference("schema1")
            .fields(List.of(field2))
            .build();

    private final AugmentationDefinition aug1 = AugmentationDefinition.builder()
            .name("aug1")
            .url("url1")
            .augmentationRules(List.of(rule1))
            .build();

    private final AugmentationDefinition aug1_update = AugmentationDefinition.builder()
            .name("aug1")
            .url("url1")
            .augmentationRules(List.of(rule1, rule2))
            .build();

    private final AugmentationDefinition aug2 = AugmentationDefinition.builder()
            .name("aug2")
            .url("url1")
            .augmentationRules(List.of(rule1))
            .build();

    @Test
    void testGetAdded_emptyExisting() {

        Function<AugmentationDefinition, String> identity = AugmentationDefinition::getName;

        final List<AugmentationDefinition> existing = List.of();

        final DiffEngine<AugmentationDefinition> diffEngine = DiffEngine.builder()
                .source(existing)
                .identityFunction(identity)
                .build();

        final List<AugmentationDefinition> actual = diffEngine.getAdded(List.of(aug1));

        assertEquals(1, actual.size());
        assertEquals(aug1, actual.get(0));
    }

    @Test
    void testGetAdded_existingResources() {

        final Function<AugmentationDefinition, String> identity = AugmentationDefinition::getName;

        final List<AugmentationDefinition> existing = List.of(aug1);

        final DiffEngine<AugmentationDefinition> diffEngine = DiffEngine.builder()
                .source(existing)
                .identityFunction(identity)
                .build();

        final List<AugmentationDefinition> actual = diffEngine.getAdded(List.of(aug1, aug2));

        assertEquals(1, actual.size());
        assertEquals(aug2, actual.get(0));
    }

    @Test
    void testGetUpdated() {

        final Function<AugmentationDefinition, String> identity = AugmentationDefinition::getName;

        final List<AugmentationDefinition> existing = List.of(aug1);

        final DiffEngine<AugmentationDefinition> diffEngine = DiffEngine.builder()
                .source(existing)
                .identityFunction(identity)
                .build();

        final List<AugmentationDefinition> actual = diffEngine.getUpdated(List.of(aug1_update));

        assertEquals(1, actual.size());
        assertEquals(aug1_update, actual.get(0));

    }

    @Test
    void testGetDeleted() {

        final Function<AugmentationDefinition, String> identity = AugmentationDefinition::getName;

        final List<AugmentationDefinition> existing = List.of(aug1, aug2);

        final DiffEngine<AugmentationDefinition> diffEngine = DiffEngine.builder()
                .source(existing)
                .identityFunction(identity)
                .build();

        final List<AugmentationDefinition> actual = diffEngine.getDeleted(List.of(aug1));

        assertEquals(1, actual.size());
        assertEquals(aug2, actual.get(0));

    }

    @Test
    void testBuilder_nullResourceList() throws Exception {

        final Function<AugmentationDefinition, String> identity = AugmentationDefinition::getName;

        assertThrows(NullPointerException.class, () -> DiffEngine.builder().identityFunction(identity).build());
    }

    @Test
    void testBuilder_nullIdentityFunction() throws Exception {

        assertThrows(NullPointerException.class, () -> DiffEngine.builder().source(List.of()).build());
    }

    @Test
    void testGetAll_noChanges() {

        final Function<AugmentationDefinition, String> identity = AugmentationDefinition::getName;

        final List<AugmentationDefinition> existing = List.of(aug1, aug2);

        final DiffEngine<AugmentationDefinition> diffEngine = DiffEngine.builder()
                .source(existing)
                .identityFunction(identity)
                .build();

        final List<AugmentationDefinition> actualAdded = diffEngine.getAdded(List.of(aug1, aug2));
        final List<AugmentationDefinition> actualUpdated = diffEngine.getUpdated(List.of(aug1, aug2));
        final List<AugmentationDefinition> actualDeleted = diffEngine.getDeleted(List.of(aug1, aug2));

        assertEquals(0, actualAdded.size());
        assertEquals(0, actualUpdated.size());
        assertEquals(0, actualDeleted.size());

    }

}