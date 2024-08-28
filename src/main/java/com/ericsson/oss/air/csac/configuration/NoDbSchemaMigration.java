/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Dry-run implementation of the Flyway schema migration.
 */
@Configuration
@Profile({ "dry-run" })
@Slf4j
public class NoDbSchemaMigration implements SchemaMigration {

    @Override
    public void migrate() {
        log.info("Schema migration disabled for dry-run mode");
    }
}
