/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.configuration.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.function.Predicate;

import com.ericsson.oss.air.util.codec.Codec;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;

/**
 * Utility builder for JSON templates.  This class generates bean instances by deserializing JSON templates bundled as resources.
 *
 * @param <T>
 *         bean type to generate.
 */
@Builder
@Accessors(fluent = true)
@Getter
@Slf4j
public class TemplateInstanceBuilder<T> {

    private static final Codec CODEC = new Codec();

    private String templateType;

    private Path templateDirPath;

    private Predicate<String> templateFileFilter;

    private Class<T> typeToGenerate;

    /**
     * Returns the bean instance deserialized from the JSON file specified in the builder.
     *
     * @return the bean instance deserialized from the JSON file specified in the builder
     * @throws IOException
     *         if the specified JSON file does not exist or cannot be deserialized
     */
    public T generate() throws IOException {

        final Path templateFilePath = getTemplateFilePath(this.templateDirPath, this.templateFileFilter);

        log.debug("Loading index writer template from '{}'", templateFilePath);

        final T templateInstance = pathToTemplateInstance(templateFilePath, this.typeToGenerate);

        log.debug("{} template loaded: {}", this.templateType, CODEC.writeValueAsString(templateInstance));

        return templateInstance;
    }

    /**
     * Returns a bean instance based on the give template.
     *
     * @param template
     *         JSON template for the bean
     * @param templateType
     *         bean class type
     * @param <T>
     *         bean type
     * @return a bean instance based on the give template
     * @throws JsonProcessingException
     *         if the bean cannot be instantiated from the provided template
     */
    public static <T> Optional<T> getTemplateInstance(final Optional<String> template, final Class<T> templateType)
            throws JsonProcessingException {

        if (template.isPresent()) {
            final T dto = CODEC.readValue(template.get(), templateType);
            return Optional.of(dto);
        }

        return Optional.empty();
    }

    private Path getTemplateFilePath(final Path sourceDirectory, final Predicate<String> fileFilter) throws IOException {

        final String sourceDirName = sourceDirectory.toString();
        try (final InputStream srcDirStream = new ClassPathResource(
                Paths.get(sourceDirName).toString()).getInputStream()) {

            final BufferedReader srcDirReader = new BufferedReader(new InputStreamReader(srcDirStream, StandardCharsets.UTF_8));

            final String indexWriterTemplate = srcDirReader.lines().filter(fileFilter).findFirst().orElseThrow();

            return Paths.get(sourceDirName, indexWriterTemplate);

        }
    }

    private <T> T pathToTemplateInstance(final Path path, final Class<T> clazz) throws IOException {

        try (final InputStream templateInputStream = new ClassPathResource(path.toString()).getInputStream()) {
            return CODEC.readValue(new InputStreamReader(templateInputStream, StandardCharsets.UTF_8), clazz);
        }
    }
}
