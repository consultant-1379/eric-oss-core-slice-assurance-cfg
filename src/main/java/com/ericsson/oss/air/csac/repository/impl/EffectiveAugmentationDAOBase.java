/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.repository.impl;

import java.util.List;
import java.util.Objects;


import com.ericsson.oss.air.csac.model.AugmentationDefinition;
import com.ericsson.oss.air.csac.repository.EffectiveAugmentationDAO;
import com.ericsson.oss.air.exception.CsacValidationException;
import org.apache.logging.log4j.util.Strings;

/**
 * Abstract base class for all {@link EffectiveAugmentationDAO} implementations.  This class provides common entry points for the DAO implementations
 * where additional common processing may be required.
 */
public abstract class EffectiveAugmentationDAOBase implements EffectiveAugmentationDAO {

    @Override
    public void save(final AugmentationDefinition effectiveAugmentation, final List<String> affectedProfiles) {

        // check for properties that are mandatory in effective profiles but optional in the resource model.
        checkEffectiveAugmentationProperties(effectiveAugmentation, affectedProfiles);

        doSave(effectiveAugmentation, affectedProfiles);
    }

    /*
     * (non-javadoc)
     *
     * Perform final pre-persistence checks on the data to be saved before persisting.
     */
    private void checkEffectiveAugmentationProperties(final AugmentationDefinition effectiveDefinition, final List<String> affectedProfiles) {

        // the affected profile list may not be empty
        if (Objects.isNull(affectedProfiles) || affectedProfiles.isEmpty()) {
            throw new CsacValidationException(
                    "Effective augmentation must be associated with at least one profile: " + effectiveDefinition.getName());
        }

        // effect augmentation must have a non-blank URL
        if (Strings.isBlank(effectiveDefinition.getUrl())) {
            throw new CsacValidationException("Effective augmentation URL missing: " + effectiveDefinition.getName());
        }

    }

    /**
     * Implementation method to perform the actual persistence operation. This must be overridden in all non-abstract implementations of this DAO.
     */
    public abstract void doSave(final AugmentationDefinition effectiveAugmentation, final List<String> affectedProfiles);
}
