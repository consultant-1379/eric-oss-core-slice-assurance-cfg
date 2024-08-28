/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.util.codec;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
class CodecTest {

    @Autowired
    private Codec codec;

    private static final String TEST_RESOURCE_PATH = "/tmp/config";

    private static final String TEST_RESOURCE_FILE = "test-bean.json";

    private static final TestBean TEST_BEAN = new TestBean("field1", 1, true);

    private static final TestBean TEST_BEAN_INVALID_INT = new TestBean("field1", -1, true);

    private static final String TEST_BEAN_DEF = "{\"sField\":\"field1\",\"nField\":1,\"bField\":true}";

    private static final String INVALID_TEST_BEAN_DEF = "{\"sField\":\"field1\",\"nField\":-1,\"bField\":true}";

    private static FileSystem testFileSystem;

    @BeforeAll
    public static void setUp() throws Exception {

        testFileSystem = Jimfs.newFileSystem(Configuration.unix());
    }

    @AfterAll
    public static void tearDown() throws Exception {
        testFileSystem.close();
    }

    @Test
    void withValidation() {

        final Codec vc = this.codec.withValidation();

        assertNotNull(vc);
        assertTrue(vc instanceof ValidatingCodec);

        // ensure that multiple invocations from the same instance of Codec will return the same instance of ValidatingCodec
        final Codec vc2 = this.codec.withValidation();
        assertEquals(vc, vc2);
    }

    @Test
    void writeValueAsString() throws Exception {
        assertEquals(TEST_BEAN_DEF, this.codec.writeValueAsString(TEST_BEAN));
    }

    @Test
    void writeValueAsStringPretty() throws Exception {

        final String expected = "{" + System.lineSeparator() +
                "  \"sField\" : \"field1\"," + System.lineSeparator() +
                "  \"nField\" : 1," + System.lineSeparator() +
                "  \"bField\" : true" + System.lineSeparator() + "}";

        assertEquals(expected, this.codec.writeValueAsStringPretty(TEST_BEAN));
    }

    @Test
    void readValue_fromString() throws Exception {

        final TestBean tb = this.codec.readValue(TEST_BEAN_DEF, TestBean.class);

        assertEquals(TEST_BEAN, tb);
    }

    @Test
    void readValue_fromString_invalidBean() throws Exception {

        final TestBean tb = this.codec.readValue(INVALID_TEST_BEAN_DEF, TestBean.class);

        assertEquals(TEST_BEAN_INVALID_INT, tb);
    }

    @Test
    void testReadValue_fromPath() throws Exception {

        final Path resourceDir = testFileSystem.getPath(TEST_RESOURCE_PATH);
        Files.createDirectories(resourceDir);

        final Path resourceFile = resourceDir.resolve(testFileSystem.getPath(TEST_RESOURCE_FILE));
        Files.createFile(resourceFile);

        Files.write(resourceFile, TEST_BEAN_DEF.getBytes(StandardCharsets.UTF_8), StandardOpenOption.WRITE);

        final TestBean tb = this.codec.readValue(resourceFile, TestBean.class);

        assertEquals(TEST_BEAN, tb);
    }

    @Test
    void testReadValue_fromInputStream() throws Exception {

        final ByteArrayInputStream bais = new ByteArrayInputStream(TEST_BEAN_DEF.getBytes(StandardCharsets.UTF_8));
        final TestBean tb = this.codec.readValue(bais, TestBean.class);
        assertEquals(TEST_BEAN, tb);
    }

    @Test
    void testReadValue_fromReader() throws Exception {

        final StringReader reader = new StringReader(TEST_BEAN_DEF);
        final TestBean tb = this.codec.readValue(reader, TestBean.class);
        assertEquals(TEST_BEAN, tb);
    }

    @Test
    void testReadValue_fromByteArray() throws Exception {

        final byte[] bytes = TEST_BEAN_DEF.getBytes(StandardCharsets.UTF_8);
        final TestBean tb = this.codec.readValue(bytes, TestBean.class);
        assertEquals(TEST_BEAN, tb);
    }
}