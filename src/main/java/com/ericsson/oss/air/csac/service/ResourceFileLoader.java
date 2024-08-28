/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.service;

import java.nio.file.Path;

import com.ericsson.oss.air.csac.model.ResourceSubmission;
import com.ericsson.oss.air.exception.ResourceFileLoaderException;
import com.ericsson.oss.air.util.codec.Codec;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * This class is for loading the csac resource file as specified in the path.
 */
@Service
@Slf4j
@NoArgsConstructor
public class ResourceFileLoader {

    @Autowired
    private Codec codec;

    /**
     * Constructs a {@code ResourceFileLoader} instance with the provided {@link Codec}. This constructor is intended for testing purposes only.
     *
     * @param codec codec injected
     */
    protected ResourceFileLoader(Codec codec) {
        this.codec = codec;
    }

    /**
     * Load json file from disk.
     *
     * @param resourceFile file to be loaded
     * @return an object deserialized from resource file
     * @throws ResourceFileLoaderException
     */
    public ResourceSubmission loadResourceFilePath(final Path resourceFile) {

        try {
            log.info("Loading resource from {}", resourceFile.toString());

            final ResourceSubmission deserialized = this.codec.withValidation().readValue(resourceFile, ResourceSubmission.class);

            log.info("Resource loaded: {}", this.codec.writeValueAsStringPretty(deserialized));

            return deserialized;
        } catch (final Exception e) {
            throw new ResourceFileLoaderException("Error reading resource file: " + resourceFile.toString() + ": " + e.getMessage(), e);
        }

    }

}
