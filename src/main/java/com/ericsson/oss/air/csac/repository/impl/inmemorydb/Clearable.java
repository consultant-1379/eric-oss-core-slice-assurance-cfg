/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.repository.impl.inmemorydb;

/**
 * Identifies an in-memory DAO as clearable.  The implementing class will have the {@link #clear()} method in its API.
 */
public interface Clearable {

    /**
     * Clears all data in the in-memory DAO.
     */
    void clear();
}
