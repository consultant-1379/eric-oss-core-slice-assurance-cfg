/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.repository.impl;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


import java.util.Collections;
import java.util.List;


import com.ericsson.oss.air.csac.model.AugmentationDefinition;
import com.ericsson.oss.air.exception.CsacValidationException;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.Mockito;

class EffectiveAugmentationDAOBaseTest {

    private final EffectiveAugmentationDAOBase testDao = Mockito.mock(EffectiveAugmentationDAOBase.class, Answers.CALLS_REAL_METHODS);

    @Test
    void save() {

        final AugmentationDefinition actual = AugmentationDefinition.builder()
                .name("testAug")
                .url("http://test.org:8080")
                .build();

        final List<String> actualProfiles = List.of("profile1", "profile2");

        this.testDao.save(actual, actualProfiles);
        verify(this.testDao, times(1)).doSave(any(), any());
    }

    @Test
    void save_missingUrl() {

        final AugmentationDefinition actual = AugmentationDefinition.builder()
                .name("testAug")
                .build();

        final List<String> actualProfiles = List.of("profile1", "profile2");

        assertThrows(CsacValidationException.class, () -> this.testDao.save(actual, actualProfiles));
        verify(this.testDao, times(0)).doSave(any(), any());
    }

    @Test
    void save_emptyAffectedProfiles() {

        final AugmentationDefinition actual = AugmentationDefinition.builder()
                .name("testAug")
                .url("http://test.org:8080")
                .build();

        final List<String> actualProfiles = Collections.emptyList();

        assertThrows(CsacValidationException.class, () -> this.testDao.save(actual, actualProfiles));
        verify(this.testDao, times(0)).doSave(any(), any());
    }

    @Test
    void save_missingAffectedProfiles() {

        final AugmentationDefinition actual = AugmentationDefinition.builder()
                .name("testAug")
                .url("http://test.org:8080")
                .build();

        assertThrows(CsacValidationException.class, () -> this.testDao.save(actual, null));
        verify(this.testDao, times(0)).doSave(any(), any());
    }
}