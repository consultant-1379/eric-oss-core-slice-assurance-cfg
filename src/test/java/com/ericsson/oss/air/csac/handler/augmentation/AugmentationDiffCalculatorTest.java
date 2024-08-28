/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.handler.augmentation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import com.ericsson.oss.air.csac.model.AugmentationDefinition;
import com.ericsson.oss.air.csac.model.AugmentationRule;
import com.ericsson.oss.air.csac.model.AugmentationRuleField;
import com.ericsson.oss.air.util.DiffEngine;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class AugmentationDiffCalculatorTest {

    @Autowired
    private AugmentationDiffCalculator calculator;

    private final AugmentationRuleField field1 = AugmentationRuleField.builder()
            .output("field1")
            .inputFields(List.of("input1", "input2"))
            .build();

    private final AugmentationRuleField field2 = AugmentationRuleField.builder()
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
    void builder() {

        final DiffEngine<AugmentationDefinition> differ = this.calculator.builder()
                .identityFunction(AugmentationDefinition::getName)
                .source(List.of())
                .build();

        final List<AugmentationDefinition> added = differ.getAdded(List.of(aug1, aug2));
        final List<AugmentationDefinition> updated = differ.getUpdated(List.of(aug1, aug2));
        final List<AugmentationDefinition> deleted = differ.getDeleted(List.of(aug1, aug2));

        assertEquals(2, added.size());
        assertEquals(0, updated.size());
        assertEquals(0, deleted.size());
    }
}