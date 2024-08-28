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

import com.ericsson.oss.air.csac.model.AugmentationDefinition;
import com.ericsson.oss.air.util.DiffEngine;
import org.springframework.stereotype.Component;

/**
 * This component allows an augmentation-specific {@link DiffEngine} builder to be injected as a bean rather than statically declared where needed.
 * The example below shows how to autowire this class.
 *
 * <pre>
 *     &#64;Autowired
 *     private AugmentationDiffCalculator calculator;
 *
 *     void fun() {
 *
 *         DiffEngine&lt;AugmentationDefinition&gt; diffCalculator = this.calculator.builder()
 *                                                     .identityFunction(AugmentationDefinition::getName)
 *                                                     .source(sourceList)
 *                                                     .build();
 *
 *         final List&lt;AugmentationDefinition&gt; addedDefinitions = diffCalculator.getAdded(candidateList);
 *     }
 * </pre>
 */
@Component
public class AugmentationDiffCalculator {
    
    /**
     * Returns an augmentation-specific {@link DiffEngine.DiffEngineBuilder}.
     *
     * @return an augmentation-specific {@code DiffEngine.DiffEngineBuilder}
     */
    public DiffEngine.DiffEngineBuilder<AugmentationDefinition> builder() {
        return DiffEngine.builder();
    }
}
