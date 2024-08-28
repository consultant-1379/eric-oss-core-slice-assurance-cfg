/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.repository.impl.jdbc;

import static com.ericsson.oss.air.csac.repository.impl.jdbc.AugmentationDefinitionDAOJdbcImpl.AUG_COUNT_STATEMENT;
import static com.ericsson.oss.air.csac.repository.impl.jdbc.AugmentationDefinitionDAOJdbcImpl.DELETE_AUG_DEF_STATEMENT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import com.ericsson.oss.air.csac.configuration.JdbcConfig;
import com.ericsson.oss.air.csac.model.AugmentationDefinition;
import com.ericsson.oss.air.csac.model.AugmentationRule;
import com.ericsson.oss.air.csac.model.AugmentationRuleField;
import com.ericsson.oss.air.csac.repository.impl.jdbc.mapper.AugmentationDefinitionMapper;
import com.ericsson.oss.air.exception.CsacDAOException;
import com.ericsson.oss.air.util.logging.FaultHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

@ExtendWith(MockitoExtension.class)
class AugmentationDefinitionDAOJdbcImplTest {

    private final AugmentationRuleField field1 = AugmentationRuleField.builder()
            .inputFields(List.of("input"))
            .output("field")
            .build();

    private final AugmentationRule testRule = AugmentationRule.builder()
            .fields(List.of(this.field1))
            .inputSchemaReference("input|schema|reference")
            .build();

    private final AugmentationDefinition testDef = AugmentationDefinition.builder()
            .name("ardq")
            .url("http://localhost:8080")
            .augmentationRules(List.of(this.testRule))
            .build();

    private final AugmentationDefinition testDef2 = AugmentationDefinition.builder()
            .name("ardq2")
            .url("http://localhost:8080")
            .augmentationRules(List.of(this.testRule))
            .build();

    private final AugmentationDefinition testDef3 = AugmentationDefinition.builder()
            .name("ardq3")
            .url("http://localhost:8080")
            .augmentationRules(List.of(this.testRule))
            .build();

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Spy
    private final ObjectMapper mapper = new ObjectMapper();

    @Mock
    private JdbcConfig jdbcConfig;

    @Mock
    private FaultHandler faultHandler;

    @InjectMocks
    private AugmentationDefinitionDAOJdbcImpl testDao;

    @BeforeEach
    public void setUp() throws Exception {
        lenient().when(this.jdbcConfig.getDictionarySchemaName()).thenReturn("dict");
        lenient().when(this.jdbcConfig.getRuntimeDatastoreSchemaName()).thenReturn("rtds");
    }

    @Test
    void save() {
        this.testDao.save(this.testDef);

        Mockito.verify(this.namedParameterJdbcTemplate,
                times(1)).batchUpdate(anyString(), any(SqlParameterSource[].class));
    }

    @Test
    void save_nullInput_throwException() {
        assertThrows(NullPointerException.class, () -> this.testDao.save(null));
    }

    @Test
    void saveAll() {
        this.testDao.saveAll(List.of(this.testDef2, this.testDef3));

        Mockito.verify(this.namedParameterJdbcTemplate,
                times(1)).batchUpdate(anyString(), any(SqlParameterSource[].class));
    }

    @Test
    void saveAll_nullInput_throwException() {
        assertThrows(NullPointerException.class, () -> this.testDao.saveAll(null));
    }

    @Test
    void saveAll_invalidAugmentationDefinitionJson_throwException() throws JsonProcessingException {
        when(this.mapper.writeValueAsString(this.testDef)).thenThrow(JsonProcessingException.class);

        assertThrows(CsacDAOException.class, () -> this.testDao.saveAll(List.of(this.testDef)));
    }

    @Test
    void findAll() {
        final List<AugmentationDefinition> expectedResult = List.of(this.testDef, this.testDef2, this.testDef3);

        when(this.jdbcTemplate.query(anyString(), any(AugmentationDefinitionMapper.class)))
                .thenReturn(List.of(this.testDef, this.testDef2, this.testDef3));

        final List<AugmentationDefinition> actualResult = this.testDao.findAll();
        assertEquals(3, actualResult.size());
        assertEquals(expectedResult, actualResult);
    }

    @Test
    void testFindAll_start0_rows3() {
        final List<AugmentationDefinition> expectedResult = List.of(this.testDef, this.testDef2, this.testDef3);

        when(this.jdbcTemplate.queryForObject(String.format(AUG_COUNT_STATEMENT, this.jdbcConfig.getDictionarySchemaName()),
                Integer.class)).thenReturn(3);
        when(this.jdbcTemplate.query(anyString(), any(AugmentationDefinitionMapper.class)))
                .thenReturn(List.of(this.testDef, this.testDef2, this.testDef3));

        final List<AugmentationDefinition> actualResult = this.testDao.findAll(0, 3);

        assertEquals(expectedResult, actualResult);

    }

    @Test
    void findById() throws Exception {

        when(this.namedParameterJdbcTemplate.query(
                anyString(),
                any(SqlParameterSource.class),
                any(AugmentationDefinitionMapper.class))).thenReturn(List.of(this.testDef));

        assertTrue(this.testDao.findById("ardq").isPresent());
    }

    @Test
    void findById_missingDef() throws Exception {

        when(this.namedParameterJdbcTemplate.query(
                anyString(),
                any(SqlParameterSource.class),
                any(AugmentationDefinitionMapper.class))).thenReturn(Collections.emptyList());

        assertTrue(this.testDao.findById("ardq").isEmpty());
    }

    @Test
    void findById_Exception() throws JsonProcessingException {

        when(this.namedParameterJdbcTemplate.query(
                anyString(),
                any(SqlParameterSource.class),
                any(AugmentationDefinitionMapper.class)))
                .thenThrow(EmptyResultDataAccessException.class);

        assertThrows(CsacDAOException.class, () -> this.testDao.findById("test"));
    }

    @Test
    void delete() {
        this.testDao.delete("ardq");
        Mockito.verify(this.jdbcTemplate, times(1))
                .update(String.format(DELETE_AUG_DEF_STATEMENT, this.jdbcConfig.getDictionarySchemaName()), "ardq");
    }

    @Test
    void totalAugmentationDefinitions() {

        when(this.jdbcTemplate.queryForObject(String.format(AUG_COUNT_STATEMENT, this.jdbcConfig.getDictionarySchemaName()),
                Integer.class)).thenReturn(3);

        assertEquals(3, this.testDao.totalAugmentationDefinitions());
    }
}