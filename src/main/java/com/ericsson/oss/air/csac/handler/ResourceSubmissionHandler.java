/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.handler;

import com.ericsson.oss.air.csac.configuration.ResourceProperties;
import com.ericsson.oss.air.exception.CsacValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@Slf4j
public class ResourceSubmissionHandler {

    static final String RESOURCE_FILE_NAMING_CONVENTION = "^\\D*_(\\d+)\\.json$";
    static final Pattern PATTERN = Pattern.compile(RESOURCE_FILE_NAMING_CONVENTION);

    private final ResourceProperties resourceProperties;

    @Autowired
    public ResourceSubmissionHandler(final ResourceProperties resourceProperties) {
        this.resourceProperties = resourceProperties;
    }

    /**
     * Returns a list of resource file paths ordered as follows:
     *
     * <ol>
     * <li>Out-of box (OOB) resource files in the order specified in the application configuration</li>
     * <li>Custom resource files in ascending order based on the numbered suffixes, i.e. &lt;filename&gt;_&lt;filenumber&gt;.json</li>
     * </ol>
     *
     * @return a sorted list of resource file {@link Path} objects
     * @throws CsacValidationException
     *         if the OOB resource file is not present
     */
    public List<Path> getOrderedResourceList() throws IOException {

        final List<Path> resourceFileList = new ArrayList<>();

        // get the root directory for all resourse files
        final Path resourceDirectory = this.resourceProperties.getResourcePath();

        // get OOB file paths
        final List<Path> outOfBoxPaths = this.getOutOfBoxResourcePaths(resourceDirectory);
        resourceFileList.addAll(outOfBoxPaths);

        // get the numbered custom files in order
        final List<Path> customPaths = this.getCustomResourcePaths(resourceDirectory);

        // remove any duplicates that may have been specified as OOB in the application properties.
        customPaths.stream()
                .filter(customPath -> {
                    if (outOfBoxPaths.contains(customPath)) {
                        log.warn("Out of box resource file found using custom resource naming convention: {}", customPath.getFileName().toString());
                        return false;
                    }

                    return true;
                })
                .collect(Collectors.toCollection(() -> resourceFileList));

        return Collections.unmodifiableList(resourceFileList);
    }

    private List<Path> getOutOfBoxResourcePaths(final Path resourceDirectory) {

        final List<Path> resourceFileList = new ArrayList<>();

        for (final String oobFileName : this.resourceProperties.getOobResourceFilenames()) {
            final Path oobPath = resourceDirectory.resolve(oobFileName);

            if (!Files.exists(oobPath)) {
                throw new CsacValidationException("Out-of-box resource file not found: " + oobPath);
            }

            resourceFileList.add(oobPath);
        }

        if (this.resourceProperties.getOobResourceFilenames().isEmpty()) {
            log.info("No out-of-box resource files specified");
        } else {
            if (!resourceFileList.isEmpty()) {
                log.info("Out of box resource files: {}", resourceFileList.stream().map(Path::toString).collect(Collectors.joining(",")));
            } else {
                log.info("No out-of-box resource files found in {}", resourceDirectory.toString());
            }
        }

        return resourceFileList;
    }

    private List<Path> getCustomResourcePaths(final Path resourceDirectory) throws IOException {

        final List<Path> resourceFileList = new ArrayList<>();

        try (final Stream<Path> stream = Files.list(resourceDirectory)
                .filter(pathname -> pathname.toString().matches(RESOURCE_FILE_NAMING_CONVENTION))) {

            resourceFileList.addAll((getOrderedPathList(stream)));
        }

        if (!resourceFileList.isEmpty()) {
            log.info("Custom resource files: {}", resourceFileList.stream().map(Path::toString).collect(Collectors.joining(",")));
        } else {
            log.info("No custom resource files found in {}", resourceDirectory.toString());
        }
        return resourceFileList;
    }

    /*
     * Orders the provided stream of resource file paths based on the value of the integer at the end
     * of the filename.
     */
    protected List<Path> getOrderedPathList(final Stream<Path> unorderedStream) {

        final List<Path> orderedList = new ArrayList<>();

        unorderedStream.sorted(Comparator.comparing(path -> {
            final Matcher matcher = PATTERN.matcher(path.toString());
            if (matcher.matches()) {
                return Integer.parseInt(matcher.group(1));
            }
            return null;
        })).collect(Collectors.toCollection(() -> orderedList));

        return orderedList;
    }

}
