/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.repository.impl.jdbc.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import java.util.Set;

import com.ericsson.oss.air.csac.model.runtime.index.ContextFieldDto;
import com.ericsson.oss.air.csac.model.runtime.index.DeployedIndexDefinitionDto;
import com.ericsson.oss.air.csac.model.runtime.index.IndexSourceDto;
import com.ericsson.oss.air.csac.model.runtime.index.IndexTargetDto;
import com.ericsson.oss.air.csac.model.runtime.index.IndexWriterDto;
import com.ericsson.oss.air.csac.model.runtime.index.InfoFieldDto;
import com.ericsson.oss.air.csac.model.runtime.index.ValueFieldDto;
import com.ericsson.oss.air.csac.repository.impl.jdbc.DeployedIndexDefinitionDaoJdbcImpl;
import com.ericsson.oss.air.exception.CsacDAOException;
import org.h2.tools.SimpleResultSet;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DeployedIndexDefinitionMapperTest {

    private final ContextFieldDto context1 = ContextFieldDto.builder()
            .name("context1")
            .build();

    private final ContextFieldDto context2 = ContextFieldDto.builder()
            .name("context2")
            .build();

    private final ValueFieldDto value1 = ValueFieldDto.builder()
            .name("value1")
            .build();

    private final ValueFieldDto value2 = ValueFieldDto.builder()
            .name("value2")
            .build();

    private final InfoFieldDto info1 = InfoFieldDto.builder()
            .name("info1")
            .build();

    private final InfoFieldDto info2 = InfoFieldDto.builder()
            .name("info2")
            .build();

    private final IndexWriterDto writer1 = IndexWriterDto.builder()
            .name("writer")
            .inputSchema("schema")
            .contextFieldList(List.of(context1, context2))
            .valueFieldList(List.of(value1, value2))
            .infoFieldList(List.of(info1, info2))
            .build();

    private final IndexSourceDto source1 = IndexSourceDto.builder()
            .indexSourceName("source")
            .indexSourceType(IndexSourceDto.IndexSourceType.PM_STATS_EXPORTER)
            .indexSourceDescription("Index source description")
            .build();

    private final IndexTargetDto target1 = IndexTargetDto.builder()
            .indexTargetName("target")
            .indexTargetDisplayName("Index Target")
            .indexTargetDescription("Index description")
            .build();

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

    private final String invalidJsonStr = "{\n" +
            "  \"notAField1\" : \"value1\"" +
            "  \"notAField2\" : \"value2\"" +
            "}";
    private final DeployedIndexDefinitionDto expectedIndex = DeployedIndexDefinitionDto.builder()
            .indexDefinitionName("index")
            .indexDefinitionDescription("Index description")
            .indexSource(source1)
            .indexTarget(target1)
            .indexWriters(Set.of(writer1))
            .build();

    private DeployedIndexDefinitionMapper testMapper;

    private SimpleResultSet testResultSet;

    @BeforeEach
    void setUp() {
        this.testMapper = new DeployedIndexDefinitionMapper();

        this.testResultSet = new SimpleResultSet();
        this.testResultSet.addColumn(DeployedIndexDefinitionDaoJdbcImpl.COLUMN_IDX_NAME, Types.VARCHAR, 0, 0);
        this.testResultSet.addColumn(DeployedIndexDefinitionDaoJdbcImpl.COLUMN_IDX_DEF, Types.VARCHAR, 0, 0);
    }

    @Test
    void mapRow() throws Exception {

        testResultSet.addRow("index", validIndexDefinitionStr);
        testResultSet.next();

        final DeployedIndexDefinitionDto actual = this.testMapper.mapRow(this.testResultSet, 0);

        assertNotNull(actual);
        assertEquals(this.expectedIndex, actual);

    }

    @Test
    void mapRow_jdbcException() throws Exception {

        final SimpleResultSet resultSet = new SimpleResultSet();
        resultSet.addColumn("RANDOM_COLUMN_NAME", 0, 0, 0);
        resultSet.next();

        assertThrows(SQLException.class, () -> this.testMapper.mapRow(resultSet, 0));
    }

    @Test
    void mapRow_jsonProcessingException() throws Exception {

        this.testResultSet.addRow("invalid", this.invalidJsonStr);
        this.testResultSet.next();

        Assertions.assertThrows(CsacDAOException.class, () -> this.testMapper.mapRow(this.testResultSet, 0));
    }

}