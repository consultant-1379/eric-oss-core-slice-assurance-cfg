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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.ericsson.oss.air.csac.configuration.ResourceProperties;
import com.ericsson.oss.air.csac.model.InputMetric;
import com.ericsson.oss.air.csac.model.KPIDefinition;
import com.ericsson.oss.air.csac.model.KPIReference;
import com.ericsson.oss.air.csac.model.PMDefinition;
import com.ericsson.oss.air.csac.model.ProfileDefinition;
import com.ericsson.oss.air.exception.CsacValidationException;
import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.ResourceUtils;

@ActiveProfiles("test")
class ResourceSubmissionHandlerTest {

    private static final String TEST_RESOURCE_PATH = "/tmp/config";
    private static final String VALID_FILE_NAME = "test_1.json";
    private static final String INVALID_FILE_NAME = "test.json";
    private static final String OOB_FILE_NAME_1 = "csac-oob-kpi-defs.json";
    private static final String OOB_FILE_NAME_2 = "csac-oob-ran-kpi-defs.json";

    private static final String CUSTOM_FILE_NAME_1 = "custom_file_1.json";

    private static final String CUSTOM_FILE_NAME_2 = "custom_file_2.json";

    private static final String NON_RESOURCE_FILE_NAME = "README.md";

    private ResourceProperties testResourceProperties;

    private static final String VALID_FILE_CONTENTS_1 =
            "{\"pm_defs\": [{\"name\": \"pmdefs\", \"source\": \"source\", \"description\":\"desc\"}], "
                    + "\"kpi_defs\": [{\"name\": \"kpidefs\", \"description\": \"desc\", \"display_name\": \"display\", "
                    + "\"expression\": \"AVG\", \"aggregation_type\": \"AVG\", \"is_visible\": true, "
                    + "\"input_metrics\": [{\"id\": \"id\", \"alias\": \"alias\", \"type\": \"pm_data\"}]}], "
                    + "\"profile_defs\": [{\"name\": \"profiledefs\", \"description\": \"desc\", \"aggregation_fields\": [\"aggregation\"], "
                    + "\"kpis\": [{\"ref\": \"ref\"}]}]}";

    private static final String VALID_FILE_CONTENTS_2 =
            "{\"pm_defs\": [{\"name\": \"pmdefs\", \"source\": \"source\", \"description\":\"desc\"}], "
                    + "\"kpi_defs\": [{\"name\": \"kpidefs\", \"description\": \"desc\", \"display_name\": \"display\", "
                    + "\"expression\": \"AVG\", \"aggregation_type\": \"AVG\", \"is_visible\": true, "
                    + "\"input_metrics\": [{\"id\": \"id\", \"alias\": \"alias\", \"type\": \"pm_data\"}]}], "
                    + "\"profile_defs\": [{\"name\": \"profiledefs\", \"description\": \"desc\", \"aggregation_fields\": [\"aggregation\"], "
                    + "\"kpis\": [{\"ref\": \"ref\"}]}]}";

    private static final String VALID_FILE_CONTENTS_PM_ONLY = "{\"pm_defs\":[{\"name\":\"pmdefs\",\"source\":\"source_new\",\"description\":\"desc_new\"}]}";

    private static final String VALID_FILE_CONTENTS_KPI_ONLY = "{\"kpi_defs\":[{\"name\":\"kpidefs\",\"description\":\"desc\","
            + "\"display_name\":\"display\",\"expression\":\"AVG\",\"aggregation_type\":\"AVG\",\"is_visible\":true,"
            + "\"input_metrics\":[{\"id\":\"id\",\"alias\":\"alias\",\"type\":\"pm_data\"}]}]}";

    // Define PMDefinitions
    public static final PMDefinition VALID_PM_DEF_OBJ_1 = new PMDefinition("pmdef_name_1", "pmdef_source_1", "pmdef_description_1");

    // Define KPIDefinition List
    public static final InputMetric SIMPLE_INPUT_METRIC = new InputMetric("pmdef_name", "input_alias", InputMetric.Type.PM_DATA);
    public static final KPIDefinition VALID_SIMPLE_KPI_DEF_OBJ_1 = new KPIDefinition("kpi_name_1", "kpidef description_1",
            "kpidef_display_name_1", "MAX(input_alias)", "MAX", null, true, List.of(SIMPLE_INPUT_METRIC));

    // Define ProfileDefinition
    private static final List<KPIReference> VALID_KPI_REFERENCE_LIST = List.of(KPIReference.builder().ref("kpi_simple_name").build(),
            KPIReference.builder().ref("kpi_complex_name").build());
    public static final ProfileDefinition VALID_PROFILE_DEF_OBJ_1 = ProfileDefinition.builder()
            .name("profiledef_name_1")
            .description("profiledef description_1")
            .context(List.of("field1", "field2"))
            .kpis(VALID_KPI_REFERENCE_LIST)
            .build();

    ResourceSubmissionHandler resourceSubmissionHandler;

    private FileSystem testFileSystem;

    @BeforeEach
    public void setUp() throws Exception {

        this.testFileSystem = Jimfs.newFileSystem(Configuration.unix());

        this.testResourceProperties = new ResourceProperties();
        this.testResourceProperties.setFileSystem(this.testFileSystem);
        this.testResourceProperties.setPath(TEST_RESOURCE_PATH);

        this.resourceSubmissionHandler = new ResourceSubmissionHandler(this.testResourceProperties);
    }

    @AfterEach
    public void tearDown() throws Exception {
        this.testFileSystem.close();
    }

    private List<Path> createResourceFiles(final Map<String, String> fileContentsMap) throws IOException {

        final Path resourceDir = this.testFileSystem.getPath(TEST_RESOURCE_PATH);

        Files.createDirectories(resourceDir);

        List<Path> resourceFiles = new ArrayList<>();
        for (Map.Entry<String, String> fileContents : fileContentsMap.entrySet()) {
            final Path resourceFile = resourceDir.resolve(this.testFileSystem.getPath(fileContents.getKey()));
            Files.createFile(resourceFile);
            Files.write(resourceFile, fileContents.getValue().getBytes(StandardCharsets.UTF_8), StandardOpenOption.WRITE);

            resourceFiles.add(resourceFile);
        }

        return resourceFiles;
    }

    private String loadFixtureAsString(final String fixtureName) throws IOException {

        final File resource = ResourceUtils.getFile(this.getClass().getResource("/fixtures/" + fixtureName));

        return Files.readString(resource.toPath());
    }

    @Test
    public void testGetOrderedResourceList_noOobFile() throws Exception {

        // creates a dummy file to ensure the directory path exists
        createResourceFiles(Map.of(OOB_FILE_NAME_1, loadFixtureAsString("oob_resource_1.json")));

        final List<Path> resources = this.resourceSubmissionHandler.getOrderedResourceList();

        assertEquals(0, resources.size());
    }

    @Test
    public void testGetOrderedResourceList_missingOobFile() throws Exception {

        // creates a dummy file to ensure the directory path exists
        createResourceFiles(Map.of(OOB_FILE_NAME_1, loadFixtureAsString("oob_resource_1.json")));

        this.testResourceProperties.setOob(List.of(OOB_FILE_NAME_1, "no_such_file.json"));

        assertThrows(CsacValidationException.class, () -> this.resourceSubmissionHandler.getOrderedResourceList());
    }

    @Test
    public void testGetOrderedResourceList_oneOobFile() throws Exception {

        createResourceFiles(Map.of(OOB_FILE_NAME_1, loadFixtureAsString("oob_resource_1.json")));

        this.testResourceProperties.setOob(List.of(OOB_FILE_NAME_1));

        final List<Path> resources = this.resourceSubmissionHandler.getOrderedResourceList();

        assertEquals(1, resources.size());
    }

    @Test
    public void testGetOrderedResourceList_twoOobFiles() throws Exception {

        final Map<String, String> fixtureMap = new HashMap<>();
        fixtureMap.put(OOB_FILE_NAME_1, loadFixtureAsString("oob_resource_1.json"));
        fixtureMap.put(OOB_FILE_NAME_2, loadFixtureAsString("oob_resource_2.json"));
        createResourceFiles(fixtureMap);

        this.testResourceProperties.setOob(List.of(OOB_FILE_NAME_1, OOB_FILE_NAME_2));

        final List<Path> resources = this.resourceSubmissionHandler.getOrderedResourceList();

        assertEquals(2, resources.size());

        // check the order
        assertEquals(OOB_FILE_NAME_1, resources.get(0).getFileName().toString());
        assertEquals(OOB_FILE_NAME_2, resources.get(1).getFileName().toString());
    }

    @Test
    public void testGetOrderedResourceList_oobWithCustomNameFormat() throws Exception {

        // create them out of order
        createResourceFiles(Map.of(CUSTOM_FILE_NAME_1, loadFixtureAsString("custom_resource_1.json")));
        createResourceFiles(Map.of(OOB_FILE_NAME_1, loadFixtureAsString("oob_resource_1.json")));
        createResourceFiles(Map.of(CUSTOM_FILE_NAME_2, loadFixtureAsString("custom_resource_2.json")));
        createResourceFiles(Map.of(OOB_FILE_NAME_2, loadFixtureAsString("oob_resource_2.json")));
        createResourceFiles(Map.of(NON_RESOURCE_FILE_NAME,
                "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed consequat mattis turpis sit amet egestas."));

        this.testResourceProperties.setOob(List.of(OOB_FILE_NAME_1, CUSTOM_FILE_NAME_2));

        final List<Path> resources = this.resourceSubmissionHandler.getOrderedResourceList();
        assertEquals(3, resources.size());

        assertEquals(OOB_FILE_NAME_1, resources.get(0).getFileName().toString());
        assertEquals(CUSTOM_FILE_NAME_2, resources.get(1).getFileName().toString());
        assertEquals(CUSTOM_FILE_NAME_1, resources.get(2).getFileName().toString());
    }

    @Test
    public void testGetOrderedResourceList_oneOobWithCustomResources() throws Exception {

        // create them out of order
        createResourceFiles(Map.of(CUSTOM_FILE_NAME_1, loadFixtureAsString("custom_resource_1.json")));
        createResourceFiles(Map.of(OOB_FILE_NAME_1, loadFixtureAsString("oob_resource_1.json")));
        createResourceFiles(Map.of(CUSTOM_FILE_NAME_2, loadFixtureAsString("custom_resource_2.json")));
        createResourceFiles(Map.of(OOB_FILE_NAME_2, loadFixtureAsString("oob_resource_2.json")));
        createResourceFiles(Map.of(NON_RESOURCE_FILE_NAME,
                "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed consequat mattis turpis sit amet egestas."));

        this.testResourceProperties.setOob(List.of(OOB_FILE_NAME_1));

        final List<Path> resources = this.resourceSubmissionHandler.getOrderedResourceList();

        // did not register the second OOB file in the ResourceProperties, and the non-resource file should be skipped
        assertEquals(3, resources.size());

        // check the order
        assertEquals(OOB_FILE_NAME_1, resources.get(0).getFileName().toString());
        assertEquals(CUSTOM_FILE_NAME_1, resources.get(1).getFileName().toString());
        assertEquals(CUSTOM_FILE_NAME_2, resources.get(2).getFileName().toString());

    }

    @Test
    public void testGetOrderedPathList() throws Exception {

        final List<String> expectedUnorderedNames = List.of(
                "custom_resource_1.json",
                "custom_resource_10.json",
                "custom_resource_2.json",
                "custom_resource_20.json",
                "custom_resource_3.json",
                "custom_resource_30.json",
                "custom_resource_4.json",
                "custom_resource_40.json",
                "custom_resource_5.json",
                "custom_resource_50.json"
        );

        final List<String> expectedOrderedNames = List.of(
                "custom_resource_1.json",
                "custom_resource_2.json",
                "custom_resource_3.json",
                "custom_resource_4.json",
                "custom_resource_5.json",
                "custom_resource_10.json",
                "custom_resource_20.json",
                "custom_resource_30.json",
                "custom_resource_40.json",
                "custom_resource_50.json");

        final List<Path> unorderedList = List.of(
                Path.of("custom_resource_1.json"),
                Path.of("custom_resource_10.json"),
                Path.of("custom_resource_2.json"),
                Path.of("custom_resource_20.json"),
                Path.of("custom_resource_3.json"),
                Path.of("custom_resource_30.json"),
                Path.of("custom_resource_4.json"),
                Path.of("custom_resource_40.json"),
                Path.of("custom_resource_5.json"),
                Path.of("custom_resource_50.json")
        );

        assertEquals(expectedUnorderedNames, unorderedList.stream().map(Path::toString).collect(Collectors.toList()));

        final List<Path> orderedList = this.resourceSubmissionHandler.getOrderedPathList(unorderedList.stream());

        assertEquals(expectedOrderedNames, orderedList.stream().map(Path::toString).collect(Collectors.toList()));
    }
}
