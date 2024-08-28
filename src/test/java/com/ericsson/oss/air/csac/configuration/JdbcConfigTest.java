/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.configuration;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest(classes = JdbcConfig.class,
                properties = "spring.datasource.hikari.runtimeDatastoreSchemaName=rtds_test")
class JdbcConfigTest {

    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    JdbcConfig jdbcConfig;

    @Test
    public void ConditionalOn_BeanCreation() {
        // Make sure jdbc beans are created when config is provided.
        assertDoesNotThrow(() -> this.applicationContext.getBean(DataSource.class));
        assertDoesNotThrow(() -> this.applicationContext.getBean(JdbcTemplate.class));
        assertDoesNotThrow(() -> this.applicationContext.getBean(NamedParameterJdbcTemplate.class));
    }

    @Test
    public void schemaNameCheck() {
        assertEquals("dict", this.jdbcConfig.getDictionarySchemaName());
        assertEquals("rtds_test", this.jdbcConfig.getRuntimeDatastoreSchemaName());
    }

}