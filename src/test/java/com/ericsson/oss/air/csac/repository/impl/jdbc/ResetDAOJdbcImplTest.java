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

import static com.ericsson.oss.air.csac.repository.impl.jdbc.ResetDAOJdbcImpl.GET_TABLES_FOR_SCHEMA_SQL;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.List;

import com.ericsson.oss.air.csac.configuration.JdbcConfig;
import com.ericsson.oss.air.csac.repository.ResetDAO;
import com.ericsson.oss.air.csac.repository.impl.jdbc.util.SqlEncoder;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

@ExtendWith(MockitoExtension.class)
class ResetDAOJdbcImplTest {

    public static final String DICTIONARY_SCHEMA = "dict";
    public static final String RUNTIME_SCHEMA = "rtds";

    private static final String SCHEMA_HISTORY_TABLE = "flyway_schema_history";

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private JdbcConfig jdbcConfig;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private Flyway flyway;

    @InjectMocks
    private ResetDAOJdbcImpl resetDAOJdbc;

    @BeforeEach
    public void setUp() throws Exception {
        lenient().when(this.flyway.getConfiguration().getTable()).thenReturn(SCHEMA_HISTORY_TABLE);
        lenient().when(this.jdbcConfig.getDictionarySchemaName()).thenReturn(DICTIONARY_SCHEMA);
        lenient().when(this.jdbcConfig.getRuntimeDatastoreSchemaName()).thenReturn(RUNTIME_SCHEMA);
        lenient().when(this.jdbcTemplate.queryForList(eq(SqlEncoder.encode(GET_TABLES_FOR_SCHEMA_SQL, DICTIONARY_SCHEMA)), eq(String.class)))
                .thenReturn(List.of("dictTable1", "dictTable2", SCHEMA_HISTORY_TABLE));
        lenient().when(this.jdbcTemplate.queryForList(eq(SqlEncoder.encode(GET_TABLES_FOR_SCHEMA_SQL, RUNTIME_SCHEMA)), eq(String.class)))
                .thenReturn(List.of("rtdsTable1", "rtdsTable2", "rtdsTable3", "rt_prov_state"));
    }

    @Test
    void clear() {

        this.resetDAOJdbc.clear();

        verify(this.jdbcTemplate, times(5)).execute(anyString());
    }

    @Test
    void clear_schemaType_RUNTIME() {

        this.resetDAOJdbc.clear(ResetDAO.SchemaType.RUNTIME);

        verify(this.jdbcTemplate, times(3)).execute(anyString());
    }

    @Test
    void clear_schemaType_DICTIONARY() {

        this.resetDAOJdbc.clear(ResetDAO.SchemaType.DICTIONARY);

        verify(this.jdbcTemplate, times(2)).execute(anyString());

    }

    @Test
    void getTablesForSchema() {

        assertEquals(List.of("dictTable1", "dictTable2", SCHEMA_HISTORY_TABLE), this.resetDAOJdbc.getTablesForSchema(ResetDAO.SchemaType.DICTIONARY));
        assertEquals(List.of("rtdsTable1", "rtdsTable2", "rtdsTable3", "rt_prov_state"),
                this.resetDAOJdbc.getTablesForSchema(ResetDAO.SchemaType.RUNTIME));

    }

    @Test
    void getSchemaForType() {

        assertEquals(DICTIONARY_SCHEMA, this.resetDAOJdbc.getSchemaForType(ResetDAO.SchemaType.DICTIONARY));
        assertEquals(RUNTIME_SCHEMA, this.resetDAOJdbc.getSchemaForType(ResetDAO.SchemaType.RUNTIME));
    }
}