/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.handler;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.ericsson.oss.air.csac.handler.index.IndexForcedProvisioningOperator;
import com.ericsson.oss.air.util.operator.SequentialOperator;

@ExtendWith(MockitoExtension.class)
class ForcedProvisioningConfigurationTest {

    @Mock
    private IndexForcedProvisioningOperator indexForcedProvisioningOperator;

    @InjectMocks
    @Spy
    private ForcedProvisioningConfiguration testConfiguration;

    @Test
    void forcedProvisioningOperator_forceIndexFalse() {

        final SequentialOperator<Void> actual = this.testConfiguration.forcedProvisioningOperator(false);

        assertNull(ReflectionTestUtils.getField(actual, "child"));
    }

    @Test
    void forcedProvisioningOperator_forceIndexTrue() {

        final SequentialOperator<Void> actual = this.testConfiguration.forcedProvisioningOperator(true);

        final SequentialOperator<Void> op = (SequentialOperator<Void>) ReflectionTestUtils.getField(actual, "child");
        assertNotNull(op);
        assertInstanceOf(IndexForcedProvisioningOperator.class, op);
    }
}