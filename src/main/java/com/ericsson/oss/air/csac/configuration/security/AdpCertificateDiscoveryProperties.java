/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.configuration.security;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Represents the configuration for the 2PP Certificate Reloader library.
 */
@Data
@ConfigurationProperties(prefix = "adp-certificate.discovery")
public class AdpCertificateDiscoveryProperties {

    private String rootReadPath = "certs";
    private String keystoreRelativeDir = "keystore";
    private String truststoreRelativeDir = "truststore";
    private String rootWritePath;
}
