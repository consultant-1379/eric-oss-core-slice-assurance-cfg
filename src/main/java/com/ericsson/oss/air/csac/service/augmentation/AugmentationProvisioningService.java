/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.service.augmentation;

import java.util.List;
import java.util.Map;

import com.ericsson.oss.air.csac.model.AugmentationDefinition;
import com.ericsson.oss.air.csac.model.augmentation.AugmentationRequestDto;
import com.ericsson.oss.air.csac.service.ProvisioningService;
import com.ericsson.oss.air.exception.CsacValidationException;

/**
 * Interface for all augmentation provisioning service implementations.
 */
public interface AugmentationProvisioningService extends ProvisioningService {

    /**
     * Checks the dimensioning query type optionally configured in the ARDQ definition.  This will query the target ARDQ service to ensure that the
     * specified query type is supported.
     *
     * @param augDefinition
     *         Augmentation definition with the target URL and dimensioning query type.
     * @throws CsacValidationException
     *         if the dimensioning query type is not supported by the target ARDQ service.
     */
    void checkArdqType(AugmentationDefinition augDefinition);

    /**
     * Creates the provided effective augmentations in the AAS.
     *
     * @param augmentationRequestDtoList
     *         list of new effective ARDQ definitions
     */
    void create(List<AugmentationRequestDto> augmentationRequestDtoList);

    /**
     * Updates the provided effective augmentations in the AAS.
     *
     * @param augmentationRequestDtoList
     *         list of updated effective augmentation definitions
     */
    void update(List<AugmentationRequestDto> augmentationRequestDtoList);

    /**
     * Deletes the provided effective augmentations in the AAS.
     *
     * @param augmentationRequestDtoList
     *         list of deleted effective augmentation definitions
     */
    void delete(List<AugmentationRequestDto> augmentationRequestDtoList);

    /**
     * Retrieves the schema mappings from the AAS as a map of input schema reference/augmented schema reference values.
     *
     * @param ardqId
     *         unique identifier for the augmentation definition.
     * @return map of input/augmented schema references.
     */
    Map<String, String> getSchemaMappings(final String ardqId);

    /**
     * Deletes all effective augmentation configuration in the AAS.
     */
    void deleteAll();
}
