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

import static com.ericsson.oss.air.csac.model.TestResourcesUtils.DEPLOYED_INDEX_DEFINITION_DTO_A;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.ericsson.oss.air.csac.model.runtime.index.DeployedIndexDefinitionDto;
import com.ericsson.oss.air.csac.repository.impl.inmemorydb.DeployedIndexDefinitionDaoImpl;
import com.ericsson.oss.air.util.codec.Codec;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class DryRunIndexerRestClientTest {

    private static final Codec CODEC = new Codec();

    @Mock
    private DeployedIndexDefinitionDaoImpl definitionDao;

    private IndexerRestClient testClient;

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

    private ListAppender<ILoggingEvent> listAppender;

    @BeforeEach
    void setup() {
        this.testClient = new DryRunIndexerRestClient(CODEC, definitionDao);
        final Logger logger = (Logger) LoggerFactory.getLogger(DryRunIndexerRestClient.class);
        logger.setLevel(Level.INFO);
        this.listAppender = new ListAppender<>();
        this.listAppender.start();
        logger.addAppender(listAppender);
    }

    @Test
    void create() throws Exception {

        final DeployedIndexDefinitionDto expected = CODEC.readValue(this.validIndexDefinitionStr, DeployedIndexDefinitionDto.class);

        assertEquals(HttpStatus.OK, this.testClient.create(expected).getStatusCode());

        assertTrue(this.listAppender.list.get(0).getMessage().startsWith("Creating index definition"));

        assertEquals(expected.indexDefinitionName(), this.listAppender.list.get(0).getArgumentArray()[0]);
    }

    @Test
    void create_nullEntity() throws Exception {
        assertThrows(NullPointerException.class, () -> this.testClient.create(null));
    }

    @Test
    void update() throws Exception {

        final DeployedIndexDefinitionDto expected = CODEC.readValue(this.validIndexDefinitionStr, DeployedIndexDefinitionDto.class);

        assertEquals(HttpStatus.OK, this.testClient.update(expected).getStatusCode());

        assertTrue(this.listAppender.list.get(0).getMessage().startsWith("Updating index definition"));

        assertEquals(expected.indexDefinitionName(), this.listAppender.list.get(0).getArgumentArray()[0]);

    }

    @Test
    void update_nullEntity() throws Exception {
        assertThrows(NullPointerException.class, () -> this.testClient.update(null));
    }

    @Test
    void deleteById() {
        this.testClient.deleteById(List.of("idx-name"));
        assertTrue(this.listAppender.list.get(0).getMessage().startsWith("Deleting the list of deployed index definitions"));
    }

    @Test
    void delete() {
        this.testClient.delete(List.of(DEPLOYED_INDEX_DEFINITION_DTO_A));

        assertTrue(this.listAppender.list.get(0).getMessage().startsWith("Deleting the list of deployed index definitions"));
        assertEquals(List.of(DEPLOYED_INDEX_DEFINITION_DTO_A.indexDefinitionName()), this.listAppender.list.get(0).getArgumentArray()[0]);
    }

    @Test
    void deleteAll() {
        final List<DeployedIndexDefinitionDto> list = new ArrayList<>();
        list.add(DEPLOYED_INDEX_DEFINITION_DTO_A);
        when(this.definitionDao.findAll()).thenReturn(list);

        this.testClient.deleteAll();

        verify(this.definitionDao, times(1)).findAll();
        assertTrue(this.listAppender.list.get(0).getMessage().startsWith("Deleting the list of deployed index definitions"));
    }
}