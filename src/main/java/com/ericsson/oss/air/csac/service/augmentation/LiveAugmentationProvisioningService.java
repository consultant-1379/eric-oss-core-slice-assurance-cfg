/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.service.augmentation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ericsson.oss.air.csac.configuration.augmentation.AugmentationConfiguration;
import com.ericsson.oss.air.csac.model.AugmentationDefinition;
import com.ericsson.oss.air.csac.model.augmentation.ArdqRegistrationResponseDto;
import com.ericsson.oss.air.csac.model.augmentation.AugmentationRequestDto;
import com.ericsson.oss.air.csac.model.augmentation.SchemaMappingResponseDto;
import com.ericsson.oss.air.exception.CsacValidationException;
import com.ericsson.oss.air.util.codec.Codec;
import com.ericsson.oss.air.util.logging.FaultHandler;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

/**
 * {@inheritDoc} This implementation submits augmentation resources to the augmentation service.
 */
@Service
@Primary
@Profile({ "prod", "test" })
@Slf4j
@RequiredArgsConstructor
public class LiveAugmentationProvisioningService implements AugmentationProvisioningService {

    private final ArdqRestClient ardqRestClient;

    private final AugmentationRestClient augmentationRestClient;

    private final AugmentationConfiguration augmentationConfiguration;

    private final Codec codec;

    private final FaultHandler faultHandler;

    @Override
    public void checkArdqType(final AugmentationDefinition augDefinition) {

        final String queryType = Strings.isBlank(augDefinition.getType()) ? "default" : augDefinition.getType();
        log.info("Checking dimensioning query type: {}", queryType);

        if (!ObjectUtils.isEmpty(augDefinition.getType())) {

            final String expectedQueryType = augDefinition.getType();
            final String ardqUrl = augDefinition.getUrl();

            //resolve ardqUrl if it is url reference.
            final String resolvedArdqUrl = this.augmentationConfiguration.getResolvedUrl(ardqUrl);

            final List<String> retrievedQueryTypes = this.ardqRestClient.getArdqQueryTypes(resolvedArdqUrl);

            if (!retrievedQueryTypes.contains(expectedQueryType)) {
                final String errorMsg = String.format(
                        "Dimensioning query type %s is not supported by the target ARDQ service %s",
                        expectedQueryType,
                        ardqUrl);
                final CsacValidationException exception = new CsacValidationException(errorMsg);
                this.faultHandler.fatal(exception);
                throw exception;
            }
        }
    }

    @SneakyThrows
    @Override
    public void create(final List<AugmentationRequestDto> augmentationRequestDtoList) {
        log.info("Submitting new augmentation definitions: {}", this.codec.writeValueAsString(augmentationRequestDtoList));

        augmentationRequestDtoList.stream()
                .forEach(this.augmentationRestClient::create);

    }

    @SneakyThrows
    @Override
    public void update(final List<AugmentationRequestDto> augmentationRequestDtoList) {
        log.info("Updating augmentation definitions: {}", this.codec.writeValueAsString(augmentationRequestDtoList));

        augmentationRequestDtoList.stream()
                .forEach(this.augmentationRestClient::update);
    }

    @SneakyThrows
    @Override
    public void delete(final List<AugmentationRequestDto> augmentationRequestDtoList) {
        log.info("Deleting augmentation definitions: {}", this.codec.writeValueAsString(augmentationRequestDtoList));

        augmentationRequestDtoList.stream()
                .forEach(augmentationRequestDto -> this.augmentationRestClient.delete(augmentationRequestDto.getArdqId()));

    }

    @Override
    public void deleteAll() {

        log.info("Deleting all augmentation definitions");

        // get all registered ardqIds in the AAS
        final List<String> ardqIds = this.augmentationRestClient.getAllArdqIds();

        // delete each one
        for (final String ardqId : ardqIds) {
            this.augmentationRestClient.delete(ardqId);
        }
    }

    @Override
    public Map<String, String> getSchemaMappings(final String ardqId) {
        final Map<String, String> schemaMappings = new HashMap<>();

        final ArdqRegistrationResponseDto response = this.augmentationRestClient.getArdqRegistrationById(ardqId);
        final List<SchemaMappingResponseDto> schemaMappingResponseDtos = response.getSchemaMappings();
        schemaMappingResponseDtos.forEach(schemaMappingResponseDto -> schemaMappings.put(schemaMappingResponseDto.getInputSchema(),
                schemaMappingResponseDto.getOutputSchema()));
        return schemaMappings;
    }
}
