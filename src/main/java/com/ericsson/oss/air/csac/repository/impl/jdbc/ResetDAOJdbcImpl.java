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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.ericsson.oss.air.csac.configuration.JdbcConfig;
import com.ericsson.oss.air.csac.repository.ResetDAO;
import com.ericsson.oss.air.csac.repository.impl.jdbc.util.SqlEncoder;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.Flyway;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * JDBC implementation of the {@link ResetDAO} API.  This class will clear all or part of the CSAC persistent store.
 */
@Slf4j
@Repository
@Primary
@AllArgsConstructor
@Profile({ "prod", "test" })
public class ResetDAOJdbcImpl implements ResetDAO {

    protected static final String GET_TABLES_FOR_SCHEMA_SQL = "SELECT tablename FROM pg_tables WHERE schemaname = '%1$s'";

    protected static final String TRUNCATE_TABLE_SQL = "TRUNCATE %1$s.%2$s CASCADE";

    private JdbcTemplate jdbcTemplate;

    private JdbcConfig jdbcConfig;

    private Flyway flyway;

    private static final Set<String> EXCLUSION_SET = new HashSet<>(List.of("rt_prov_state"));

    @Override
    public void clear(final SchemaType schemaType) {

        final String schemaName = getSchemaForType(schemaType);

        final String schemaHistoryTable = this.flyway.getConfiguration().getTable();

        EXCLUSION_SET.add(schemaHistoryTable);

        for (final String table : getTablesForSchema(schemaType)) {

            if (!getDoExclude(table)) {
                final String sql = SqlEncoder.encode(TRUNCATE_TABLE_SQL, schemaName, table);
                log.info("Resetting table '{}.{}': {}", schemaName, table, sql);
                this.jdbcTemplate.execute(sql);
            }
        }
    }

    /*
     * (non-javadoc)
     *
     * Returns a list of table names associated with the specified schema.
     */
    protected List<String> getTablesForSchema(final SchemaType schemaType) {

        final String sql = SqlEncoder.encode(GET_TABLES_FOR_SCHEMA_SQL, getSchemaForType(schemaType));

        return this.jdbcTemplate.queryForList(sql, String.class);
    }

    /*
     * (non-javadoc)
     *
     * Returns the schema name for the give schema type.
     */
    protected String getSchemaForType(final SchemaType schemaType) {

        return switch (schemaType) {
            case DICTIONARY -> this.jdbcConfig.getDictionarySchemaName();
            case RUNTIME -> this.jdbcConfig.getRuntimeDatastoreSchemaName();
            default -> null;
        };
    }

    /*
     * (non-javadoc)
     *
     * Returns {@code true} if the provided table is in to be excluded from the purge.
     */
    protected boolean getDoExclude(final String table) {
        return EXCLUSION_SET.stream().anyMatch(table::equalsIgnoreCase);
    }
}
