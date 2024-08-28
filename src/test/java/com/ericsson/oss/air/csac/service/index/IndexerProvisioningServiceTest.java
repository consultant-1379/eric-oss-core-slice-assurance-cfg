/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.service.index;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import jakarta.validation.Validation;

import com.ericsson.oss.air.csac.handler.validation.ValidationHandler;
import com.ericsson.oss.air.csac.model.runtime.index.DeployedIndexDefinitionDto;
import com.ericsson.oss.air.exception.CsacValidationException;
import com.ericsson.oss.air.util.codec.Codec;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class IndexerProvisioningServiceTest {

    private IndexerProvisioningService testService;

    @Mock
    private IndexerRestClient indexerRestClient;

    private static final Codec CODEC = new Codec();

    private ValidationHandler validationHandler;

    private final String validIndexDefinitionStr = "{\n" +
            "  \"name\" : \"index\",\n" +
            "  \"description\" : \"Index description\",\n" +
            "  \"source\" : {\n" +
            "    \"name\" : \"source\",\n" +
            "    \"type\" : \"pmstatsexporter\",\n" +
            "    \"description\" : \"Index source description\"\n" +
            "  },\n" +
            "  \"target\" : {\n" +
            "    \"name\" : \"target\",\n" +
            "    \"displayName\" : \"Index Target\",\n" +
            "    \"description\" : \"Index description\"\n" +
            "  },\n" +
            "  \"writers\" : [ {\n" +
            "    \"name\" : \"writer\",\n" +
            "    \"inputSchema\" : \"schema\",\n" +
            "    \"context\" : [ {\n" +
            "      \"name\" : \"context1\",\n" +
            "      \"nameType\" : \"straight\"\n" +
            "    }, {\n" +
            "      \"name\" : \"context2\",\n" +
            "      \"nameType\" : \"straight\"\n" +
            "    } ],\n" +
            "    \"value\" : [ {\n" +
            "      \"name\" : \"value1\",\n" +
            "      \"type\" : \"float\"\n" +
            "    }, {\n" +
            "      \"name\" : \"value2\",\n" +
            "      \"type\" : \"float\"\n" +
            "    } ],\n" +
            "    \"info\" : [ {\n" +
            "      \"name\" : \"info1\",\n" +
            "      \"type\" : \"string\"\n" +
            "    }, {\n" +
            "      \"name\" : \"info2\",\n" +
            "      \"type\" : \"string\"\n" +
            "    } ]\n" +
            "  } ]\n" +
            "}";

    @BeforeEach
    void setUp() {

        this.validationHandler = new ValidationHandler();
        this.validationHandler.setValidator(Validation.buildDefaultValidatorFactory().getValidator());
        this.testService = new IndexerProvisioningService(this.indexerRestClient, this.validationHandler);
    }

    @Test
    void create() throws Exception {

        final DeployedIndexDefinitionDto actual = CODEC.readValue(this.validIndexDefinitionStr, DeployedIndexDefinitionDto.class);

        assertDoesNotThrow(() -> this.testService.create(actual));
    }

    @Test
    void create_nullEntity() throws Exception {
        assertThrows(CsacValidationException.class, () -> this.testService.create(null));
    }

    @Test
    void create_invalidEntity() throws Exception {
        assertThrows(CsacValidationException.class, () -> this.testService.create(new DeployedIndexDefinitionDto()));
    }

    @Test
    void update() throws Exception {

        final DeployedIndexDefinitionDto actual = CODEC.readValue(this.validIndexDefinitionStr, DeployedIndexDefinitionDto.class);

        assertDoesNotThrow(() -> this.testService.update(actual));
    }

    @Test
    void udpate_nullEntity() throws Exception {
        assertThrows(CsacValidationException.class, () -> this.testService.update(null));
    }

    @Test
    void update_invalidEntity() throws Exception {
        assertThrows(CsacValidationException.class, () -> this.testService.update(new DeployedIndexDefinitionDto()));
    }
}