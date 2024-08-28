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

import javax.net.ssl.SSLContext;

/**
 * Handles initializing, configuring, and refreshing the SSL context for the embedded server and the clients.
 */
public interface SslContextConfigurator<T>{

    /**
     * Initializes the {@code SslContextConfigurator} with the initially loaded keystore and truststore.
     */
    void init();

    /**
     * Refreshes the SSL Context when the keystore and the truststore have changed.
     *
     * @param updatedSslMaterial  the updated material to be utilized to create a new SSL Context for CSAC
     */
    void refresh(final T updatedSslMaterial);

    /**
     * Gets the SSL context required for all secured CSAC communication.
     *
     * @return the SSL context
     */
    SSLContext getSslContext();
}