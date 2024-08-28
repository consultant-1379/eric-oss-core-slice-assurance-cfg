/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.handler.validation;

import com.ericsson.oss.air.csac.model.PMDefinition;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Component that bypasses external validation of the PM definitions
 */
@Component
@Slf4j
@ConditionalOnProperty(value = "validation.external.enabled",
                       havingValue = "false")
public class LoopbackPMValidator implements PMValidator {

    @Override
    public void validate(final List<PMDefinition> submittedPMDefinitions) {
        this.getValidPMDefinitions(submittedPMDefinitions);
    }

    @Override
    public Map<String, List<PMDefinition>> getValidPMDefinitions(final List<PMDefinition> submittedPMDefinitions) {
        log.warn("Live PM validation has been skipped.");
        final Map<String, List<PMDefinition>> schemaNamePMDefMap = new HashMap<>();

        for (final PMDefinition pmDefinition : submittedPMDefinitions) {
            final String source = pmDefinition.getSource();
            final String schemaName = source.substring(source.lastIndexOf('|') + 1);

            if (schemaNamePMDefMap.containsKey(schemaName)) {
                schemaNamePMDefMap.get(schemaName).add(pmDefinition);
            } else {
                final ArrayList<PMDefinition> pmDefinitionList = new ArrayList<>();

                pmDefinitionList.add(pmDefinition);
                schemaNamePMDefMap.put(schemaName, pmDefinitionList);
            }
        }
        return schemaNamePMDefMap;
    }

}
