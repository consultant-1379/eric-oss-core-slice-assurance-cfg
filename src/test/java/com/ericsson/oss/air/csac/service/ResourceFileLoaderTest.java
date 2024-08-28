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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;

import com.ericsson.oss.air.csac.model.InputMetric;
import com.ericsson.oss.air.csac.model.KPIDefinition;
import com.ericsson.oss.air.csac.model.KPIReference;
import com.ericsson.oss.air.csac.model.PMDefinition;
import com.ericsson.oss.air.csac.model.ProfileDefinition;
import com.ericsson.oss.air.csac.model.ResourceSubmission;
import com.ericsson.oss.air.exception.ResourceFileLoaderException;
import com.ericsson.oss.air.util.codec.Codec;
import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class ResourceFileLoaderTest {

    private static final String TEST_RESOURCE_PATH = "/tmp/config";
    private static final String TEST_RESOURCE_FILE = "test.json";

    private static final String VALID_FILE_CONTENTS = "{\"pm_defs\": [{\"name\": \"pmdefs\", \"source\": \"source\", \"description\":\"desc\"}], "
            + "\"kpi_defs\": [{\"name\": \"kpidefs\", \"description\": \"desc\", \"display_name\": \"display\", "
            + "\"expression\": \"AVG\", \"aggregation_type\": \"AVG\", \"is_visible\": true, "
            + "\"input_metrics\": [{\"id\": \"id\", \"alias\": \"alias\", \"type\": \"pm_data\"}]}], "
            + "\"profile_defs\": [{\"name\": \"profiledefs\", \"description\": \"desc\", \"aggregation_fields\": [\"aggregation\"], "
            + "\"kpis\": [{\"ref\": \"ref\"}]}]}";

    private FileSystem testFileSystem;

    @Autowired
    private ResourceFileLoader resourceFileLoader;

    @Mock
    private Codec mockCodec;

    @Mock
    private Path path;

    @BeforeEach
    public void setUp() {
        this.testFileSystem = Jimfs.newFileSystem(Configuration.unix());
    }

    @AfterEach
    public void tearDown() throws Exception {
        this.testFileSystem.close();
    }

    @Test
    void testResourceFileLoaderValidContents() throws Exception {

        final ResourceSubmission resourceSubmission = ResourceSubmission.builder()
                .pmDefs(List.of(new PMDefinition("pmdefs", "source", "desc")))
                .kpiDefs(List.of(new KPIDefinition("kpidefs", "desc", "display", "AVG", "AVG", null, true,
                        List.of(new InputMetric("id", "alias", InputMetric.Type.fromString("pm_data"))))))
                .profileDefs(List.of(ProfileDefinition.builder()
                        .name("profiledefs").description("desc").context(List.of("aggregation"))
                        .kpis(List.of(KPIReference.builder().ref("ref").build())).build()))
                .build();

        final Path resourceDir = testFileSystem.getPath(TEST_RESOURCE_PATH);
        Files.createDirectories(resourceDir);

        final Path resourceFile = resourceDir.resolve(testFileSystem.getPath(TEST_RESOURCE_FILE));
        Files.createFile(resourceFile);

        Files.write(resourceFile, VALID_FILE_CONTENTS.getBytes(StandardCharsets.UTF_8), StandardOpenOption.WRITE);

        final Object actual = resourceFileLoader.loadResourceFilePath(resourceFile);

        assertNotNull(actual);
        assertEquals(resourceSubmission, actual);
    }

    @Test
    void testResourceFileLoaderThrowException() {
        when(mockCodec.withValidation()).thenThrow(RuntimeException.class);
        when(path.toString()).thenReturn("exception");

        ResourceFileLoader resourceFileLoader = new ResourceFileLoader(mockCodec);
        assertThrows(ResourceFileLoaderException.class, () -> resourceFileLoader.loadResourceFilePath(path));
    }

    @Test
    void testResourceFileLoaderEmptyContents() throws Exception {
        final ResourceSubmission resourceSubmission = new ResourceSubmission();

        final Path resourceDir = testFileSystem.getPath(TEST_RESOURCE_PATH);
        Files.createDirectories(resourceDir);

        final Path resourceFile = resourceDir.resolve(testFileSystem.getPath(TEST_RESOURCE_FILE));
        Files.createFile(resourceFile);

        final String EMPTY_CONTENTS = "";
        Files.write(resourceFile, EMPTY_CONTENTS.getBytes(StandardCharsets.UTF_8), StandardOpenOption.WRITE);

        assertThrows(ResourceFileLoaderException.class, () -> resourceFileLoader.loadResourceFilePath(resourceFile));
    }

}