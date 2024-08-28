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

import static com.ericsson.oss.air.csac.repository.impl.jdbc.ProfileDefinitionDAOJdbcImpl.COUNT_STATEMENT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import com.ericsson.oss.air.csac.configuration.JdbcConfig;
import com.ericsson.oss.air.csac.model.ProfileDefinition;
import com.ericsson.oss.air.csac.model.TestResourcesUtils;
import com.ericsson.oss.air.csac.repository.impl.jdbc.mapper.ProfileDefinitionMapper;
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
public class ProfileDefinitionDAOJdbcImplTest {

    private final ProfileDefinition profileDefinition1 = ProfileDefinition.builder()
            .name("profileDef1")
            .description("Test Profile Definition1")
            .context(List.of("context1"))
            .kpis(TestResourcesUtils.VALID_KPI_REFERENCE_LIST)
            .augmentation("aug1")
            .build();

    private final ProfileDefinition profileDefinition2 = ProfileDefinition.builder()
            .name("profileDef2")
            .description("Test Profile Definition2")
            .context(List.of("context2"))
            .kpis(TestResourcesUtils.VALID_KPI_REFERENCE_LIST)
            .build();

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
    private ProfileDefinitionDAOJdbcImpl testDao;

    @BeforeEach
    public void setUp() throws Exception {
        lenient().when(this.jdbcConfig.getDictionarySchemaName()).thenReturn("dict");
        lenient().when(this.jdbcConfig.getRuntimeDatastoreSchemaName()).thenReturn("rtds");
    }

    @Test
    void save() {
        this.testDao.save(this.profileDefinition1);

        Mockito.verify(this.namedParameterJdbcTemplate,
                Mockito.times(1)).batchUpdate(anyString(), any(SqlParameterSource[].class));
    }

    @Test
    void save_nullInput_throwException() {
        assertThrows(NullPointerException.class, () -> this.testDao.save(null));
    }

    @Test
    void saveAll() {
        this.testDao.saveAll(List.of(this.profileDefinition1, this.profileDefinition2));

        Mockito.verify(this.namedParameterJdbcTemplate,
                Mockito.times(1)).batchUpdate(anyString(), any(SqlParameterSource[].class));
    }

    @Test
    void saveAll_nullInput_throwException() {
        assertThrows(NullPointerException.class, () -> this.testDao.saveAll(null));
    }

    @Test
    void saveAll_invalidProfileDefinitionJson_throwException() throws JsonProcessingException {
        when(this.mapper.writeValueAsString(this.profileDefinition1)).thenThrow(JsonProcessingException.class);
        assertThrows(CsacDAOException.class, () -> this.testDao.saveAll(List.of(this.profileDefinition1)));
    }

    @Test
    void findAll() {
        final List<ProfileDefinition> expectedResult = List.of(this.profileDefinition1, this.profileDefinition2);

        when(this.jdbcTemplate.query(anyString(), any(ProfileDefinitionMapper.class)))
                .thenReturn(expectedResult);

        final List<ProfileDefinition> actualResult = this.testDao.findAll();
        assertEquals(2, actualResult.size());
        assertEquals(expectedResult, actualResult);
    }

    @Test
    void findById() throws Exception {

        when(this.namedParameterJdbcTemplate.query(
                anyString(),
                any(SqlParameterSource.class),
                any(ProfileDefinitionMapper.class))).thenReturn(List.of(this.profileDefinition1));

        assertTrue(this.testDao.findById(this.profileDefinition1.getName()).isPresent());
    }

    @Test
    void findById_noProfile() throws Exception {

        when(this.namedParameterJdbcTemplate.query(
                anyString(),
                any(SqlParameterSource.class),
                any(ProfileDefinitionMapper.class))).thenReturn(Collections.emptyList());

        assertFalse(this.testDao.findById(this.profileDefinition1.getName()).isPresent());
    }

    @Test
    void findById_Exception() {

        when(this.namedParameterJdbcTemplate.query(
                anyString(),
                any(SqlParameterSource.class),
                any(ProfileDefinitionMapper.class)))
                .thenThrow(EmptyResultDataAccessException.class);

        assertThrows(CsacDAOException.class, () -> this.testDao.findById(this.profileDefinition1.getName()));
    }

    @Test
    void totalAugmentationDefinitions() {
        when(this.jdbcTemplate.queryForObject(String.format(COUNT_STATEMENT, this.jdbcConfig.getDictionarySchemaName()), Integer.class)).thenReturn(
                3);

        assertEquals(3, this.testDao.totalProfileDefinitions());
    }

}
