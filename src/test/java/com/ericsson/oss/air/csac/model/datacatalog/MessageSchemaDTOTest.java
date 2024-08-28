/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.model.datacatalog;

import static org.junit.jupiter.api.Assertions.*;

import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;

import com.ericsson.oss.air.util.codec.Codec;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;


class MessageSchemaDTOTest {

    private Codec codec = new Codec(Validation.buildDefaultValidatorFactory().getValidator());

    @Test
    void readValue_MessageSchemaDTO_ReturnsMessageSchemaDTO() throws JsonProcessingException {

        final MessageSchemaDTO messageSchemaDto = this.codec.withValidation()
            .readValue("{\"id\":\"1\"," +
                    "\"specificationReference\":\"subject/2\"," +
                    "\"messageDataTopic\":{\"name\":\"test-topic\"}," +
                    "\"dataService\":{\"foo1\":\"bar1\"}," +
                    "\"dataType\":{\"foo2\":\"bar2\"}}",
                MessageSchemaDTO.class);

        assertNotNull(messageSchemaDto);
        assertEquals("subject/2", messageSchemaDto.getSpecificationReference());
        assertNotNull(messageSchemaDto.getMessageDataTopic());
        assertEquals("test-topic", messageSchemaDto.getMessageDataTopic().getName());
        assertNotNull(messageSchemaDto.getId());
        assertNotNull(messageSchemaDto.getMessageDataTopic());
        assertNotNull(messageSchemaDto.getDataService());
        assertNotNull(messageSchemaDto.getDataType());
    }


    @Test
    void readValue_MessageSchemaWithBlankSpecificationReference_ThrowsException() {

        assertThrows(ConstraintViolationException.class,
            () -> this.codec.withValidation().readValue(
                "{\"id\":\"1\"," +
                    "\"specificationReference\":\"       \"," +
                    "\"messageDataTopic\":{\"foo\":\"bar\"}," +
                    "\"dataService\":{\"foo1\":\"bar1\"}," +
                    "\"dataType\":{\"foo2\":\"bar2\"}}",
                MessageSchemaDTO.class));
    }

    @Test
    void readValue_MessageSchemaWithMissingSpecificationReference_ThrowsException() {

        assertThrows(ConstraintViolationException.class,
            () -> this.codec.withValidation().readValue(
                "{\"id\":\"1\"," +
                    "\"messageDataTopic\":{\"foo\":\"bar\"}," +
                    "\"dataService\":{\"foo1\":\"bar1\"}," +
                    "\"dataType\":{\"foo2\":\"bar2\"}}",
                MessageSchemaDTO.class));
    }

}