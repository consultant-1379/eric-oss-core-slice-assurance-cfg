/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.handler.index;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LoopbackIndexForcedProvisioningOperatorTest {

    @Mock
    private LoopbackIndexProvisioningHandler loopbackIndexProvisioningHandler;

    @InjectMocks
    private LoopbackIndexForcedProvisioningOperator testOperator;

    @Test
    void doApply() {

        this.testOperator.apply(null);

        verify(this.loopbackIndexProvisioningHandler, times(1)).apply(any());
    }
}