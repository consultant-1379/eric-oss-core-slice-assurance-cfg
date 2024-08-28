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
import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ResourcePropertiesTest {

    // need an in-memory filesystem
    private static FileSystem TEST_FILESYSTEM;

    private static final String TEST_RESOURCE = "/config/resource";

    private static final String TEST_MISSING_RESOURCE = "/dummy/path";

    private static final String TEST_INVALID_RESOURCE = "/config/resource/resource_file.json";

    @BeforeAll
    public static void setUpClass() throws Exception {
        TEST_FILESYSTEM = Jimfs.newFileSystem(Configuration.unix());

        final Path resourceDirectory = TEST_FILESYSTEM.getPath(TEST_RESOURCE);
        Files.createDirectories(resourceDirectory);

        final Path path = TEST_FILESYSTEM.getPath(TEST_RESOURCE).resolve(TEST_INVALID_RESOURCE);
        Files.createFile(path);

    }

    @AfterAll
    public static void tearDownClass() throws Exception {
        TEST_FILESYSTEM.close();
    }

    @Test
    public void testResourceDirectoryExists() throws Exception {

        final Path path = TEST_FILESYSTEM.getPath(TEST_RESOURCE);

        assertTrue(ResourceProperties.PATH_EXISTS.test(path));
    }

    @Test
    public void testResourceDirectoryNotExists() throws Exception {
        final Path path = TEST_FILESYSTEM.getPath(TEST_MISSING_RESOURCE);

        assertFalse(ResourceProperties.PATH_EXISTS.test(path));
    }

    @Test
    public void testResourceDirectoryIsDirectory() throws Exception {

        final Path path = TEST_FILESYSTEM.getPath(TEST_RESOURCE);

        assertTrue(ResourceProperties.PATH_IS_DIRECTORY.test(path));
    }

    @Test
    public void testResourceFileIsNotDirectory() throws Exception {

        final Path path = TEST_FILESYSTEM.getPath(TEST_INVALID_RESOURCE);

        assertFalse(ResourceProperties.PATH_IS_DIRECTORY.test(path));
    }

    @Test
    public void testGetResourcePath() throws Exception {

        final ResourceProperties properties = new ResourceProperties();
        properties.setFileSystem(TEST_FILESYSTEM);
        properties.setPath(TEST_RESOURCE);

        final Path path = properties.getResourcePath();

        assertNotNull(path);
    }

    @Test
    public void testGetMissingResourcePath() throws Exception {

        final ResourceProperties properties = new ResourceProperties();
        properties.setFileSystem(TEST_FILESYSTEM);
        properties.setPath(TEST_MISSING_RESOURCE);

        assertThrows(CsacValidationException.class, () -> properties.getResourcePath());
    }

    @Test
    public void testGetInvalidResourcePath() throws Exception {

        final ResourceProperties properties = new ResourceProperties();
        properties.setFileSystem(TEST_FILESYSTEM);
        properties.setPath(TEST_INVALID_RESOURCE);

        assertThrows(CsacValidationException.class, () -> properties.getResourcePath());
    }

    @Test
    public void testGetOobFiles() throws Exception {

        final ResourceProperties properties = new ResourceProperties();
        final List<String> oobFiles = List.of("csac-oob-kpi-defs.json");
        properties.setOob(oobFiles);

        assertEquals(oobFiles, properties.getOobResourceFilenames());
    }

    @Test
    public void testGetOobFiles_nullList() throws Exception {

        final ResourceProperties properties = new ResourceProperties();
        properties.setOob(null);

        assertTrue(properties.getOobResourceFilenames().isEmpty());
    }

}
