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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Test;

class TemplateInstanceBuilderTest {

    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    static class TestBean {

        @JsonProperty
        private String field1;

        @JsonProperty
        private String field2;
    }

    private final TestBean testBean = TestBean.builder()
            .field1("field1 value")
            .field2("field2 value")
            .build();

    @Test
    void getTemplateInstance() throws Exception {

        final String template = "{\"field1\":\"field1 value\",\"field2\":\"field2 value\"}";

        final TestBean actual = TemplateInstanceBuilder.getTemplateInstance(Optional.of(template), TestBean.class).get();

        assertEquals(testBean, actual);

    }

    @Test
    void getTemplateInstance_mismatchedTemplate() throws Exception {

        final String template = "{\"field3\":\"field1 value\",\"field4\":\"field2 value\"}";

        assertThrows(JsonProcessingException.class, () -> TemplateInstanceBuilder.getTemplateInstance(Optional.of(template), TestBean.class).get());
    }

    @Test
    void getTemplateInstance_nullTemplate() throws Exception {

        assertTrue(TemplateInstanceBuilder.getTemplateInstance(Optional.empty(), TestBean.class).isEmpty());
    }
}