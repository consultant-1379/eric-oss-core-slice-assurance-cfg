/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.configuration.augmentation;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ericsson.oss.air.exception.CsacValidationException;
import com.ericsson.oss.air.util.logging.FaultHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * AugmentationConfiguration class is responsible for resolving ARDQ URL references.
 */
@Configuration
@RequiredArgsConstructor
public class AugmentationConfiguration {

    private static final Pattern URL_REFERENCE_PATTERN = Pattern.compile("^\\$\\{(\\w+)\\}$");

    final Environment env;

    final AugmentationProperties augmentationProperties;

    final FaultHandler faultHandler;

    /**
     * Resolves the given ARDQ url if it is a URL reference. Example of URL reference: ${cardq}
     *
     * @param ardqUrl string representation of a URL or a URL reference.
     * @return resolved URL string
     */
    public String getResolvedUrl(final String ardqUrl) {

        return this.isUrlReference(ardqUrl) ? this.resolveUrl(ardqUrl) : ardqUrl;
    }

    /*
     * Resolves URL from the provided URL reference.
     */
    private String resolveUrl(final String urlReference) {
        final Matcher matcher = URL_REFERENCE_PATTERN.matcher(urlReference);
        matcher.find();

        return getArdqUrlFromAppConfig(matcher.group(1));
    }

    private boolean isUrlReference(final String urlString) {
        final Matcher matcher = URL_REFERENCE_PATTERN.matcher(urlString);

        return matcher.matches();
    }

    private String getArdqUrlFromAppConfig(final String ardqId) {
        final Optional<String> ardqUrlOptional = this.augmentationProperties.getArdqUrl(ardqId);

        if (ardqUrlOptional.isEmpty()) {
            final String errorMsg = String.format(
                    "ARDQ url for augmentation '%s' not found in augmentation or application configuration.", ardqId);
            final CsacValidationException cve = new CsacValidationException(errorMsg);
            this.faultHandler.error(cve);
            throw cve;
        }

        return ardqUrlOptional.get();
    }

    /**
     * Returns true if dry-run mode is enabled, otherwise false.
     *
     * @return true if dry-run mode is enabled, otherwise false.
     */
    public boolean isDryRunModeEnabled() {

        final List<String> activeProfiles = Arrays.stream(this.env.getActiveProfiles()).toList();

        return activeProfiles.contains("dry-run");
    }
}
