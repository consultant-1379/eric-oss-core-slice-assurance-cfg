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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import jakarta.validation.ConstraintViolationException;
import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
class ValidatingCodecTest {

    private static final String TEST_RESOURCE_PATH = "/tmp/config";

    private static final String TEST_RESOURCE_FILE = "test-bean.json";

    private static final TestBean TEST_BEAN = new TestBean("field1", 1, true);

    private static final String TEST_BEAN_DEF = "{\"sField\":\"field1\",\"nField\":1,\"bField\":true}";

    private static final String INVALID_TEST_BEAN_DEF_EMPTY_FIELD = "{\"sField\":\"\",\"nField\":1,\"bField\":true}";

    private static final String INVALID_TEST_BEAN_DEF_BAD_INT = "{\"sField\":\"\",\"nField\":-1,\"bField\":true}";

    private FileSystem testFileSystem;

    @Autowired
    private Codec codec;

    private Codec validatingCodec;

    @BeforeEach
    public void setUp() {
        this.validatingCodec = this.codec.withValidation();
        this.testFileSystem = Jimfs.newFileSystem(Configuration.unix());
    }

    @AfterEach
    public void tearDown() throws Exception {
        this.testFileSystem.close();
    }

    @Test
    void readValue_fromString() throws Exception {

        final TestBean tb = this.validatingCodec.readValue(TEST_BEAN_DEF, TestBean.class);
        assertNotNull(tb);
        assertEquals(TEST_BEAN, tb);

        assertThrows(ConstraintViolationException.class, () -> this.validatingCodec.readValue(INVALID_TEST_BEAN_DEF_EMPTY_FIELD, TestBean.class));
        assertThrows(ConstraintViolationException.class, () -> this.validatingCodec.readValue(INVALID_TEST_BEAN_DEF_BAD_INT, TestBean.class));
    }

    @Test
    void testReadValue_fromPath_Valid() throws Exception {

        final Path resourceDir = testFileSystem.getPath(TEST_RESOURCE_PATH);
        Files.createDirectories(resourceDir);

        final Path resourceFile = resourceDir.resolve(testFileSystem.getPath(TEST_RESOURCE_FILE));
        Files.createFile(resourceFile);

        Files.write(resourceFile, TEST_BEAN_DEF.getBytes(StandardCharsets.UTF_8), StandardOpenOption.WRITE);

        assertEquals(TEST_BEAN, this.validatingCodec.readValue(resourceFile, TestBean.class));

    }

    @Test
    void testReadValue_fromPath_InValid() throws Exception {

        final Path resourceDir = testFileSystem.getPath(TEST_RESOURCE_PATH);
        Files.createDirectories(resourceDir);

        final Path resourceFile = resourceDir.resolve(testFileSystem.getPath(TEST_RESOURCE_FILE));
        Files.createFile(resourceFile);

        Files.write(resourceFile, INVALID_TEST_BEAN_DEF_EMPTY_FIELD.getBytes(StandardCharsets.UTF_8), StandardOpenOption.WRITE);

        assertThrows(ConstraintViolationException.class, () -> this.validatingCodec.readValue(resourceFile, TestBean.class));

    }

    @Test
    void testReadValue_fromInputStream() throws Exception {

        final ByteArrayInputStream bais = new ByteArrayInputStream(TEST_BEAN_DEF.getBytes(StandardCharsets.UTF_8));
        final TestBean tb = this.validatingCodec.readValue(bais, TestBean.class);
        assertEquals(TEST_BEAN, tb);

        final ByteArrayInputStream invalidInt = new ByteArrayInputStream(INVALID_TEST_BEAN_DEF_BAD_INT.getBytes(StandardCharsets.UTF_8));
        assertThrows(ConstraintViolationException.class, () -> this.validatingCodec.readValue(invalidInt, TestBean.class));

        final ByteArrayInputStream invalidString = new ByteArrayInputStream(INVALID_TEST_BEAN_DEF_EMPTY_FIELD.getBytes(StandardCharsets.UTF_8));
        assertThrows(ConstraintViolationException.class, () -> this.validatingCodec.readValue(invalidString, TestBean.class));
    }

    @Test
    void testReadValue_fromReader() throws Exception {

        final StringReader validReader = new StringReader(TEST_BEAN_DEF);
        final StringReader invalidIntReader = new StringReader(INVALID_TEST_BEAN_DEF_BAD_INT);
        final StringReader invalidStringReader = new StringReader(INVALID_TEST_BEAN_DEF_EMPTY_FIELD);

        assertEquals(TEST_BEAN, this.validatingCodec.readValue(validReader, TestBean.class));
        assertThrows(ConstraintViolationException.class, () -> this.validatingCodec.readValue(invalidIntReader, TestBean.class));
        assertThrows(ConstraintViolationException.class, () -> this.validatingCodec.readValue(invalidStringReader, TestBean.class));
    }

    @Test
    void testReadValue_fromByteArray() throws Exception {

        assertEquals(TEST_BEAN, this.validatingCodec.readValue(TEST_BEAN_DEF.getBytes(StandardCharsets.UTF_8), TestBean.class));
        assertThrows(ConstraintViolationException.class,
                () -> this.validatingCodec.readValue(INVALID_TEST_BEAN_DEF_BAD_INT.getBytes(StandardCharsets.UTF_8), TestBean.class));
        assertThrows(ConstraintViolationException.class,
                () -> this.validatingCodec.readValue(INVALID_TEST_BEAN_DEF_EMPTY_FIELD.getBytes(StandardCharsets.UTF_8), TestBean.class));
    }
}