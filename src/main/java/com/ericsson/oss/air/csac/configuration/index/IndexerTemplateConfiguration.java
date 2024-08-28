/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.configuration.index;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.ericsson.oss.air.csac.configuration.index.templates.IndexTemplateData;
import com.ericsson.oss.air.csac.configuration.util.TemplateInstanceBuilder;
import com.ericsson.oss.air.csac.model.runtime.index.DeployedIndexDefinitionDto;
import com.ericsson.oss.air.csac.model.runtime.index.IndexSourceDto;
import com.ericsson.oss.air.csac.model.runtime.index.IndexWriterDto;
import com.ericsson.oss.air.exception.CsacValidationException;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * This class provides the templates required for generating {@code DeployedIndexDefinitionDto} objects during indexer provisioning.
 */
@Configuration
@ConfigurationProperties(prefix = "provisioning.index")
@Slf4j
@Getter
public class IndexerTemplateConfiguration {

    private final Map<String, String> indexWriterTemplateMap = new HashMap<>();

    private final Map<String, String> indexDefinitionTemplateMap = new HashMap<>();

    @Setter
    private Map<String, SourceName> source;

    @Setter
    public static class SourceName {

        private String name;
    }

    /**
     * Loads the templates as resources after the bean has been instantiated.
     */
    @PostConstruct
    public void loadTemplates() {

        log.debug("Loading index definition templates");

        // this is a workaround as reading from the classpath is not currently working
        final Set<String> sourceDirectories = IndexTemplateData.getIndexTemplateSources();

        log.debug("Index definition template source directories: {}", sourceDirectories);

        for (final String sourceDirectory : sourceDirectories) {

            this.indexWriterTemplateMap.put(sourceDirectory,
                    IndexTemplateData.getIndexTemplate(sourceDirectory, IndexTemplateData.IndexTemplateHolder.IndexTemplateType.INDEX_WRITER)
                            .orElseThrow(() -> new IllegalStateException("Missing index writer template for " + sourceDirectory)));

            this.indexDefinitionTemplateMap.put(sourceDirectory,
                    IndexTemplateData.getIndexTemplate(sourceDirectory, IndexTemplateData.IndexTemplateHolder.IndexTemplateType.INDEX_DEFINITION)
                            .orElseThrow(() -> new IllegalStateException("Missing index definition template for " + sourceDirectory)));

        }
    }

    /**
     * Returns a {@link IndexWriterDto} based on the default template.
     *
     * @return a {@code IndexWriterDto} based on the default template
     */
    public Optional<IndexWriterDto> getIndexWriterTemplate() {
        return getIndexWriterTemplate(IndexSourceDto.IndexSourceType.PM_STATS_EXPORTER);
    }

    /**
     * Returns a {@link IndexWriterDto} based on the template for the specified index source.
     *
     * @param sourceType index source type
     * @return a {@code IndexWriterDto} based on the template for the specified index source
     */
    @SneakyThrows
    public Optional<IndexWriterDto> getIndexWriterTemplate(final IndexSourceDto.IndexSourceType sourceType) {
        final Optional<String> template = Optional.ofNullable(this.indexWriterTemplateMap.get(sourceType.getSourceType()));
        return TemplateInstanceBuilder.getTemplateInstance(template, IndexWriterDto.class);
    }

    /**
     * Returns a {@link DeployedIndexDefinitionDto} based on the default template.
     * <p>
     * When loading the default template, get the source type, then lookup the required properties.
     * If the required properties are missing, throw CsacValidationException
     *
     * @return the deployed index definition template after the validation
     */
    public DeployedIndexDefinitionDto getIndexDefinitionTemplate() {

        final DeployedIndexDefinitionDto deployedIndexDefinitionDto = this.getIndexDefinitionTemplate(
                        IndexSourceDto.IndexSourceType.PM_STATS_EXPORTER)
                .orElseThrow(() -> new IllegalStateException("Missing default index template"));

        validateAndSetIndexSourceName(deployedIndexDefinitionDto);

        return deployedIndexDefinitionDto;
    }

    /**
     * Returns a {@link DeployedIndexDefinitionDto} based on the template for the specified index source.
     *
     * @param sourceType index source type
     * @return a {@link DeployedIndexDefinitionDto} based on the template for the specified index source
     */
    @SneakyThrows
    public Optional<DeployedIndexDefinitionDto> getIndexDefinitionTemplate(final IndexSourceDto.IndexSourceType sourceType) {
        final Optional<String> template = Optional.ofNullable(this.indexDefinitionTemplateMap.get(sourceType.getSourceType()));
        return TemplateInstanceBuilder.getTemplateInstance(template, DeployedIndexDefinitionDto.class);
    }

    private void validateAndSetIndexSourceName(final DeployedIndexDefinitionDto deployedIndexDefinitionDto) {

        final String sourceType = deployedIndexDefinitionDto.indexSource().getIndexSourceType().getSourceType();

        // check if the properties contains the pre-defined template's source type
        if (!this.source.containsKey(sourceType)) {
            log.error("The index source name provided: {} not match with the default template source type: [{}]", this.source.keySet(), sourceType);
            throw new CsacValidationException("The index source name not match with the source type from default template!");
        }

        this.source.keySet()
                .stream()
                .filter(key -> key.equals(sourceType))
                .collect(Collectors.toSet())
                .forEach(key -> deployedIndexDefinitionDto.indexSource()
                        .setIndexSourceName(this.source.get(key).name));
    }

}
