/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.configuration.augmentation;

import java.util.Map;
import java.util.Optional;


import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * This class represents all the properties required for Assurance Augmentation Service (AAS) provisioning.
 */
@Configuration
@ConfigurationProperties(prefix = "provisioning")
@AllArgsConstructor
@RequiredArgsConstructor
public class AugmentationProperties {

    @Setter
    private Map<String, Object> aas;

    /**
     * Returns true if AAS provisioning is enabled, otherwise false.
     *
     * @return true if AAS provisioning is enabled, otherwise false.
     */
    public boolean isEnabled() {
        return (boolean) this.aas.get("enabled");
    }

    /**
     * Returns the URL for the Assurance Augmentation Service (AAS).  This may be null if AAS provisioning is disabled.
     *
     * @return URL of the AAS.
     */
    public String getAasUrl() {
        return (String) this.aas.get("url");
    }

    /**
     * Returns the URL matching the specified ARDQ Id from the application configuration.  If the specified ARDQ Id does not exist, an empty Optional
     * is returned.
     *
     * @param ardqId
     *         case-sensitive ARDQ Id
     * @return Optional containing the URL string matching the provided ARDQ Id, or an empty Optional if it does not exist.
     */
    public Optional<String> getArdqUrl(final String ardqId) {
        
        final Map<String, String> ardqConfig = getArdqConfig();

        return Optional.ofNullable(ardqConfig.get(ardqId));
    }

    /**
     * Returns the ardq configuration from the application configuration as a Map with ardq id as key and ardq URL as value.
     *
     * @return Map with ardq id as key and ardq URL as value
     */
    public Map<String, String> getArdqConfig() {
        return (Map<String, String>) this.aas.get("ardq");
    }

}
