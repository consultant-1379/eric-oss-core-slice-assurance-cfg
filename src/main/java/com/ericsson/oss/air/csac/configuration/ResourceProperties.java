/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.configuration;

import com.ericsson.oss.air.exception.CsacValidationException;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * This class represents all properties associated with CSAC resources.
 */
@Configuration
@ConfigurationProperties(prefix = "csac.resource")
@Validated
public class ResourceProperties {

    @Setter
    private FileSystem fileSystem = FileSystems.getDefault();

    @NotBlank
    private String resourcePath;

    /*
     * List of out-of-box resource files to load. OOB resources will be
     * loaded in the order they appear in this list
     */
    private final List<String> oobFileList = new ArrayList<>();

    /**
     * Returns the path to the CSAC resource files as defined in the application configuration properties.
     *
     * @return the path to the CSAC resource files as defined in the application configuration properties
     */
    public Path getResourcePath() {

        final Path path = this.fileSystem.getPath(this.resourcePath);

        checkResourcePath(path);

        return path;
    }

    /**
     * Returns a list of out-of-box resource file names.
     *
     * @return list of out-of-box resource file names.
     */
    public List<String> getOobResourceFilenames() {
        this.oobFileList.removeIf(Objects::isNull);
        this.oobFileList.removeIf(String::isEmpty);
        return Collections.unmodifiableList(this.oobFileList);
    }

    private void checkResourcePath(final Path resourcePath) {

        if (!PATH_EXISTS.test(resourcePath)) {
            throw new CsacValidationException("Resource path does not exist: " + this.resourcePath);
        }
        if (!PATH_IS_DIRECTORY.test(resourcePath)) {
            throw new CsacValidationException("Invalid resource path: " + this.resourcePath);
        }
    }

    /**
     * Sets the out-of-box candidate file name list from the application properties.
     *
     * @param oobCandidates
     *         candidate out-of-box file names.
     */
    public void setOob(final List<String> oobCandidates) {

        if (Objects.nonNull(oobCandidates)) {
            this.oobFileList.addAll(oobCandidates);
        }
    }

    /**
     * Sets the directory path for all CSAC resource files.
     *
     * @param path
     *         the directory path for all CSAC resource files
     */
    public void setPath(final String path) {
        this.resourcePath = path;
    }

    /*
     * Predicate to verify that the specified path exists in the current file system.
     */
    protected static final Predicate<Path> PATH_EXISTS = Files::exists;

    /*
     * Predicate to verify that the specified file is a directory.
     */
    protected static final Predicate<Path> PATH_IS_DIRECTORY = Files::isDirectory;

}
