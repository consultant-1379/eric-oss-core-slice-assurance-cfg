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

import static com.ericsson.oss.air.csac.repository.impl.jdbc.EffectiveAugmentationDAOJdbcImpl.COUNT_STATEMENT;
import static com.ericsson.oss.air.csac.repository.impl.jdbc.EffectiveAugmentationDAOJdbcImpl.DELETE_EFFECTIVE_AUG_DEF_STATEMENT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
class EffectiveAugmentationDAOJdbcImplTest {

    private final AugmentationDefinition testDef = AugmentationDefinition.builder()
            .name("test")
            .url("http://test.org:8080")
            .augmentationRules(List.of(AugmentationRule.builder()
                    .inputSchemaReference("schema")
                    .fields(List.of(AugmentationRuleField.builder()
                            .output("field")
                            .inputFields(List.of("input")).build())
                    ).build())).build();

    private final List<String> testProfiles = List.of("profile");

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Spy
    private final ObjectMapper mapper = new ObjectMapper();

    @Mock
    private FaultHandler faultHandler;

    @Mock
    private JdbcConfig jdbcConfig;

    @InjectMocks
    private EffectiveAugmentationDAOJdbcImpl testDao;

    @BeforeEach
    public void setUp() throws Exception {
        lenient().when(this.jdbcConfig.getDictionarySchemaName()).thenReturn("dict");
        lenient().when(this.jdbcConfig.getRuntimeDatastoreSchemaName()).thenReturn("rtds");
    }

    @Test
    void doSave_update2Tables() {
        this.testDao.save(this.testDef, this.testProfiles);

        Mockito.verify(this.namedParameterJdbcTemplate,
                Mockito.times(2)).update(anyString(), any(SqlParameterSource.class));
    }

    @Test
    void doSave_exception() throws JsonProcessingException {
        when(this.mapper.writeValueAsString(this.testDef)).thenThrow(JsonProcessingException.class);

        assertThrows(CsacDAOException.class, () -> this.testDao.save(this.testDef, this.testProfiles));
    }

    @Test
    void findAll() {

        final List<AugmentationDefinition> expectedResult = List.of(this.testDef);
        when(this.jdbcTemplate.query(anyString(), any(AugmentationDefinitionMapper.class)))
                .thenReturn(List.of(this.testDef));

        final List<AugmentationDefinition> actualResult = this.testDao.findAll();

        assertEquals(1, actualResult.size());
        assertEquals(expectedResult, actualResult);
    }

    @Test
    void findById() throws JsonProcessingException {

        final String AugmentationDefString = this.mapper.writeValueAsString(this.testDef);

        when(this.namedParameterJdbcTemplate.query(
                anyString(),
                any(SqlParameterSource.class),
                any(AugmentationDefinitionMapper.class))).thenReturn(List.of(this.testDef));

        assertTrue(this.testDao.findById("test").isPresent());
    }

    @Test
    void findById_missingAugmentation() throws JsonProcessingException {

        final String AugmentationDefString = this.mapper.writeValueAsString(this.testDef);

        when(this.namedParameterJdbcTemplate.query(
                anyString(),
                any(SqlParameterSource.class),
                any(AugmentationDefinitionMapper.class))).thenReturn(Collections.emptyList());

        assertFalse(this.testDao.findById("test").isPresent());
    }

    @Test
    void findById_exception() throws JsonProcessingException {

        final String AugmentationDefString = this.mapper.writeValueAsString(this.testDef);

        when(this.namedParameterJdbcTemplate.query(
                anyString(),
                any(SqlParameterSource.class),
                any(AugmentationDefinitionMapper.class)))
                .thenThrow(EmptyResultDataAccessException.class);

        assertThrows(CsacDAOException.class, () -> this.testDao.findById("test"));
    }

    @Test
    void delete() {

        this.testDao.delete("test");

        Mockito.verify(this.jdbcTemplate, times(1)).update(
                String.format(DELETE_EFFECTIVE_AUG_DEF_STATEMENT, this.jdbcConfig.getRuntimeDatastoreSchemaName()), "test");
    }

    @Test
    void findAllProfileNames() {

        final List<String> expected = List.of("profile");
        when(this.namedParameterJdbcTemplate.queryForList(anyString(), any(SqlParameterSource.class), any(Class.class)))
                .thenReturn(expected);

        final List<String> actual = this.testDao.findAllProfileNames("test");

        assertEquals(1, actual.size());
        assertEquals(expected, actual);

    }

    @Test
    void totalEffectiveAugmentations() {

        when(this.jdbcTemplate.queryForObject(String.format(COUNT_STATEMENT, this.jdbcConfig.getRuntimeDatastoreSchemaName()),
                Integer.class)).thenReturn(1);

        assertEquals(1, this.testDao.totalEffectiveAugmentations());
    }
}