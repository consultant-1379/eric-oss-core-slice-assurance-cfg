/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.repository.impl.jdbc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

import jakarta.validation.Validation;

import com.ericsson.oss.air.csac.configuration.JdbcConfig;
import com.ericsson.oss.air.csac.handler.validation.ValidationHandler;
import com.ericsson.oss.air.csac.model.runtime.index.DeployedIndexDefinitionDto;
import com.ericsson.oss.air.csac.repository.impl.jdbc.mapper.DeployedIndexDefinitionMapper;
import com.ericsson.oss.air.exception.CsacDAOException;
import com.ericsson.oss.air.exception.CsacValidationException;
import com.ericsson.oss.air.util.codec.Codec;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;

@ExtendWith(MockitoExtension.class)
class DeployedIndexDefinitionDaoJdbcImplTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private JdbcConfig jdbcConfig;

    @Spy
    private ValidationHandler validationHandler = new ValidationHandler();

    @InjectMocks
    private DeployedIndexDefinitionDaoJdbcImpl testDao;

    @Spy
    private static Codec CODEC = new Codec();

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
        this.validationHandler.setValidator(Validation.buildDefaultValidatorFactory().getValidator());

        lenient().when(this.jdbcConfig.getDictionarySchemaName()).thenReturn("dict");
        lenient().when(this.jdbcConfig.getRuntimeDatastoreSchemaName()).thenReturn("rtds");

    }

    @Test
    void getSaveStatementSetter() throws Exception {

        final PreparedStatement ps = mock(PreparedStatement.class);

        final PreparedStatementSetter actual = this.testDao.getSaveStatementSetter("index",
                this.validIndexDefinitionStr);

        actual.setValues(ps);

        Mockito.verify(ps, Mockito.times(2))
                .setString(anyInt(), anyString());

    }

    @Test
    void getSaveAllStatementSetter() throws Exception {

        final PreparedStatement ps = mock(PreparedStatement.class);

        final BatchPreparedStatementSetter actual = this.testDao.getSaveAllStatementSetter(List.of("index"),
                List.of(this.validIndexDefinitionStr));

        actual.setValues(ps, 0);

        Mockito.verify(ps, Mockito.times(2))
                .setString(anyInt(), anyString());

        assertEquals(1, actual.getBatchSize());

    }

    @Test
    void save() throws Exception {

        final DeployedIndexDefinitionDto actual = CODEC.readValue(this.validIndexDefinitionStr, DeployedIndexDefinitionDto.class);

        this.testDao.save(actual);

        final String saveSqlStatement = "INSERT INTO rtds.rt_idx_def (idx_name,idx_def) VALUES(?, to_json(?::json)) ON CONFLICT (idx_name) DO UPDATE SET idx_def = EXCLUDED.idx_def";

        Mockito.verify(this.jdbcTemplate, Mockito.times(1))
                .update(eq(saveSqlStatement), any(PreparedStatementSetter.class));
    }

    @Test
    void save_jsonProcessingException() throws Exception {

        when(CODEC.writeValueAsString(any())).thenThrow(JsonProcessingException.class);

        doNothing().when(this.validationHandler).checkEntity(any());

        assertThrows(CsacDAOException.class, () -> this.testDao.save(new DeployedIndexDefinitionDto()));
    }

    @Test
    void save_dataAccessException() throws Exception {

        final DataAccessException mockException = mock(DataAccessException.class);

        doNothing().when(this.validationHandler).checkEntity(any());

        when(this.jdbcTemplate.update(anyString(), any(PreparedStatementSetter.class))).thenThrow(mockException);

        assertThrows(CsacDAOException.class, () -> this.testDao.save(new DeployedIndexDefinitionDto()));
    }

    @Test
    void save_invalidDto() throws Exception {

        final DeployedIndexDefinitionDto invalid = DeployedIndexDefinitionDto.builder().build();

        assertThrows(CsacValidationException.class, () -> this.testDao.save(invalid));
    }

    @Test
    void saveAll_jsonProcessingException() throws Exception {

        when(CODEC.writeValueAsString(any())).thenThrow(JsonProcessingException.class);

        final DeployedIndexDefinitionDto actual = CODEC.readValue(this.validIndexDefinitionStr, DeployedIndexDefinitionDto.class);

        assertThrows(CsacDAOException.class, () -> this.testDao.saveAll(List.of(actual)));

    }

    @Test
    void saveAll_dataAccessException() {

        final DataAccessException mockException = mock(DataAccessException.class);

        doNothing().when(this.validationHandler).checkEntity(any());
        when(this.jdbcTemplate.batchUpdate(anyString(), any(BatchPreparedStatementSetter.class))).thenThrow(mockException);

        assertThrows(CsacDAOException.class, () -> this.testDao.saveAll(List.of(new DeployedIndexDefinitionDto())));
    }

    @Test
    void saveAll() throws Exception {

        final DeployedIndexDefinitionDto actual = CODEC.readValue(this.validIndexDefinitionStr, DeployedIndexDefinitionDto.class);

        this.testDao.saveAll(List.of(actual));

        final String saveAllSqlStatement = "INSERT INTO rtds.rt_idx_def (idx_name,idx_def) VALUES(?, to_json(?::json)) ON CONFLICT (idx_name) DO UPDATE SET idx_def = EXCLUDED.idx_def";

        Mockito.verify(this.jdbcTemplate, Mockito.times(1))
                .batchUpdate(eq(saveAllSqlStatement), any(BatchPreparedStatementSetter.class));
    }

    @Test
    void saveAll_invalidDto() throws Exception {

        final DeployedIndexDefinitionDto actual = CODEC.readValue(this.validIndexDefinitionStr, DeployedIndexDefinitionDto.class);
        final DeployedIndexDefinitionDto invalid = DeployedIndexDefinitionDto.builder().build();

        assertThrows(CsacValidationException.class, () -> this.testDao.saveAll(List.of(actual, invalid)));
    }

    @Test
    void findById_dataAccessException() throws Exception {

        final DataAccessException mockException = mock(DataAccessException.class);

        when(this.jdbcTemplate.query(anyString(), any(DeployedIndexDefinitionMapper.class)))
                .thenThrow(mockException);

        assertThrows(CsacDAOException.class, () -> this.testDao.findById("id"));
    }

    @Test
    void findById() throws Exception {

        final DeployedIndexDefinitionDto expected = CODEC.readValue(this.validIndexDefinitionStr, DeployedIndexDefinitionDto.class);

        final String findSqlStatement = "SELECT idx_def FROM rtds.rt_idx_def WHERE idx_name = 'id'";

        when(this.jdbcTemplate.query(eq(findSqlStatement),
                ArgumentMatchers.any(DeployedIndexDefinitionMapper.class)))
                .thenReturn(List.of(expected));

        final Optional<DeployedIndexDefinitionDto> actual = this.testDao.findById("id");

        assertTrue(actual.isPresent());
        assertEquals(expected, actual.get());

    }

    @Test
    void findById_noMatch() throws Exception {

        final DeployedIndexDefinitionDto expected = CODEC.readValue(this.validIndexDefinitionStr, DeployedIndexDefinitionDto.class);

        when(this.jdbcTemplate.query(anyString(), any(DeployedIndexDefinitionMapper.class)))
                .thenReturn(List.of());

        final Optional<DeployedIndexDefinitionDto> actual = this.testDao.findById("id");

        assertTrue(actual.isEmpty());

    }

    @Test
    void existsById_dataAccessException() throws Exception {

        final DataAccessException mockException = mock(DataAccessException.class);

        when(this.jdbcTemplate.queryForObject(anyString(), any(Class.class)))
                .thenThrow(mockException);

        assertThrows(CsacDAOException.class, () -> this.testDao.existsById("index"));
    }

    @Test
    void existsById() throws Exception {

        final String existsByIdSqlStatement = "SELECT COUNT(*) FROM rtds.rt_idx_def WHERE idx_name = 'index'";

        when(this.jdbcTemplate.queryForObject(eq(existsByIdSqlStatement), any(Class.class)))
                .thenReturn(1);

        assertTrue(this.testDao.existsById("index"));
    }

    @Test
    void existsById_notFound() throws Exception {

        when(this.jdbcTemplate.queryForObject(anyString(), any(Class.class)))
                .thenReturn(0);

        assertFalse(this.testDao.existsById("index"));
    }

    @Test
    void findAll_dataAccessException() throws Exception {

        final DataAccessException mockException = mock(DataAccessException.class);

        when(this.jdbcTemplate.query(anyString(), any(DeployedIndexDefinitionMapper.class)))
                .thenThrow(mockException);

        assertThrows(CsacDAOException.class, () -> this.testDao.findAll());
    }

    @Test
    void findAll() throws Exception {

        final DeployedIndexDefinitionDto expected = CODEC.readValue(this.validIndexDefinitionStr, DeployedIndexDefinitionDto.class);

        final String findAllSqlStatement = "SELECT idx_def FROM rtds.rt_idx_def";

        when(this.jdbcTemplate.query(eq(findAllSqlStatement), any(DeployedIndexDefinitionMapper.class)))
                .thenReturn(List.of(expected));

        assertEquals(1, StreamSupport.stream(this.testDao.findAll().spliterator(), false).count());
    }

    @Test
    void findAllById_dataAccessException() throws Exception {

        final DataAccessException mockException = mock(DataAccessException.class);

        when(this.jdbcTemplate.query(anyString(), any(DeployedIndexDefinitionMapper.class), any()))
                .thenThrow(mockException);

        assertThrows(CsacDAOException.class, () -> this.testDao.findAllById(List.of("index")));
    }

    @Test
    void findAllById() throws Exception {

        final DeployedIndexDefinitionDto expected = CODEC.readValue(this.validIndexDefinitionStr, DeployedIndexDefinitionDto.class);

        final String findAllByIdSqlStatement = "SELECT idx_def FROM rtds.rt_idx_def WHERE idx_name IN (?)";

        when(this.jdbcTemplate.query(eq(findAllByIdSqlStatement), any(DeployedIndexDefinitionMapper.class), any()))
                .thenReturn(List.of(expected));

        this.testDao.findAllById(List.of("index"));
    }

    @Test
    void count_dataAccessException() throws Exception {

        final DataAccessException mockException = mock(DataAccessException.class);

        when(this.jdbcTemplate.queryForObject(anyString(), any(Class.class)))
                .thenThrow(mockException);

        assertThrows(CsacDAOException.class, () -> this.testDao.count());
    }

    @Test
    void count() throws Exception {

        final String countSqlStatement = "SELECT COUNT(*) FROM rtds.rt_idx_def";

        when(this.jdbcTemplate.queryForObject(eq(countSqlStatement), any(Class.class)))
                .thenReturn(1L);

        assertEquals(1, this.testDao.count());
    }

    @Test
    void count_notFound() throws Exception {

        when(this.jdbcTemplate.queryForObject(anyString(), any(Class.class)))
                .thenReturn(0L);

        assertEquals(0, this.testDao.count());
    }

    @Test
    void count_nullResponse() throws Exception {

        when(this.jdbcTemplate.queryForObject(anyString(), any(Class.class)))
                .thenReturn(null);

        assertThrows(NullPointerException.class, () -> this.testDao.count());
    }

    @Test
    void deleteById_dataAccessException() throws Exception {

        final DataAccessException mockException = mock(DataAccessException.class);

        when(this.jdbcTemplate.update(anyString(), anyString()))
                .thenThrow(mockException);

        assertThrows(CsacDAOException.class, () -> this.testDao.deleteById("id"));
    }

    @Test
    void deleteById() throws Exception {

        this.testDao.deleteById("index");

        final String deleteStatement = "DELETE FROM rtds.rt_idx_def WHERE idx_name = ?";

        Mockito.verify(this.jdbcTemplate, Mockito.times(1))
                .update(eq(deleteStatement), anyString());

    }

    @Test
    void delete_dataAccessException() throws Exception {

        final DeployedIndexDefinitionDto expected = CODEC.readValue(this.validIndexDefinitionStr, DeployedIndexDefinitionDto.class);

        final DataAccessException mockException = mock(DataAccessException.class);

        when(this.jdbcTemplate.update(anyString(), anyString()))
                .thenThrow(mockException);

        assertThrows(CsacDAOException.class, () -> this.testDao.delete(expected));
    }

    @Test
    void delete() throws Exception {

        final DeployedIndexDefinitionDto expected = CODEC.readValue(this.validIndexDefinitionStr, DeployedIndexDefinitionDto.class);

        this.testDao.delete(expected);

        Mockito.verify(this.jdbcTemplate, Mockito.times(1))
                .update(anyString(), anyString());
    }

    @Test
    void delete_nullEntity() throws Exception {

        this.testDao.delete(null);

        Mockito.verify(this.jdbcTemplate, Mockito.times(0))
                .update(anyString(), anyString());
    }

    @Test
    void deleteAllById_dataAccessException() throws Exception {

        final DataAccessException mockException = mock(DataAccessException.class);

        when(this.jdbcTemplate.update(anyString(), anyString()))
                .thenThrow(mockException);

        assertThrows(CsacDAOException.class, () -> this.testDao.deleteAllById(List.of("index")));
    }

    @Test
    void deleteAllById() throws Exception {

        this.testDao.deleteAllById(List.of("index"));

        final String deleteStatement = "DELETE FROM rtds.rt_idx_def WHERE idx_name IN (?)";

        Mockito.verify(this.jdbcTemplate, Mockito.times(1))
                .update(eq(deleteStatement), anyString());
    }

    @Test
    void deleteAll_dataAccessException() throws Exception {

        final DataAccessException mockException = mock(DataAccessException.class);

        when(this.jdbcTemplate.update(anyString()))
                .thenThrow(mockException);

        assertThrows(CsacDAOException.class, () -> this.testDao.deleteAll());
    }

    @Test
    void deleteAll() throws Exception {

        this.testDao.deleteAll();

        Mockito.verify(this.jdbcTemplate, Mockito.times(1))
                .update(anyString());

    }

    @Test
    void deleteAllFromList_dataAccessException() throws Exception {

        final DeployedIndexDefinitionDto expected = CODEC.readValue(this.validIndexDefinitionStr, DeployedIndexDefinitionDto.class);

        final DataAccessException mockException = mock(DataAccessException.class);

        when(this.jdbcTemplate.update(anyString(), anyString()))
                .thenThrow(mockException);

        assertThrows(CsacDAOException.class, () -> this.testDao.deleteAll(List.of(expected)));
    }

    @Test
    void testDeleteAll_fromList() throws Exception {

        final DeployedIndexDefinitionDto expected = CODEC.readValue(this.validIndexDefinitionStr, DeployedIndexDefinitionDto.class);

        this.testDao.deleteAll(List.of(expected));

        Mockito.verify(this.jdbcTemplate, Mockito.times(1))
                .update(anyString(), anyString());
    }

    @Test
    void stream_dataAccessException() throws Exception {

        final DeployedIndexDefinitionDto expected = CODEC.readValue(this.validIndexDefinitionStr, DeployedIndexDefinitionDto.class);

        final DataAccessException mockException = mock(DataAccessException.class);

        when(this.jdbcTemplate.query(anyString(), any(DeployedIndexDefinitionMapper.class)))
                .thenThrow(mockException);

        assertThrows(CsacDAOException.class, () -> this.testDao.stream());
    }

    @Test
    void stream() throws Exception {

        final DeployedIndexDefinitionDto expected = CODEC.readValue(this.validIndexDefinitionStr, DeployedIndexDefinitionDto.class);

        when(this.jdbcTemplate.query(anyString(), any(DeployedIndexDefinitionMapper.class)))
                .thenReturn(List.of(expected));

        assertEquals(1, this.testDao.stream().count());
    }
}
