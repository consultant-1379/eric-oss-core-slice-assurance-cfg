/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.handler.validation;

import java.util.List;
import java.util.Map;

import com.ericsson.oss.air.csac.model.PMDefinition;

/**
 * Interface for all of the PM definition validators
 */
public interface PMValidator extends CsacValidator<List<PMDefinition>>{

    /**
     * Validates the PM definitions in the submitted resources after bean validation. Returns a map keyed on PM schema 
     * names each with a list of their corresponding PM definitions.
     *
     * @param submittedPMDefinitions
     *      the submitted PM definitions
     * @return a map keyed on PM schema names each with a list of their corresponding PM definitions
     */
    public Map<String, List<PMDefinition>> getValidPMDefinitions(final List<PMDefinition> submittedPMDefinitions);

}
