/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.service;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;


import com.ericsson.oss.air.exception.CsacValidationException;

/**
 * Base type for all provisioning services.
 */
public interface ProvisioningService {

    /**
     * Throws a CsacValidationException if the provided URL string is invalid.
     *
     * @param urlSpec
     *         URL string to check.
     * @throws CsacValidationException
     *         if the provided URL string is invalid.
     */
    default void checkUrl(final String urlSpec) {

        try {
            this.checkUrl(new URL(urlSpec));
        } catch (final MalformedURLException mue) {
            throw new CsacValidationException(mue);
        }
    }

    /**
     * Throws a CsacValidationException if the provided URL is invalid.
     *
     * @param url
     *         URL to check.
     * @throws CsacValidationException
     *         if the provided URL is invalid.
     */
    default void checkUrl(final URL url) {
        try {
            // catches URL patterns that may be valid URLs but not compliant with RFC 2396.
            url.toURI();
        } catch (final URISyntaxException ex) {
            throw new CsacValidationException(ex);
        }
    }
}
