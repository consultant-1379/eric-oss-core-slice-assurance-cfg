/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.handler.pmsc;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ericsson.oss.air.csac.handler.pmsc.legacy.PMSCHandler;

@ExtendWith(MockitoExtension.class)
class PmscKpiProvisioningHandlerTest {

    @Mock
    private PMSCHandler pmscHandler;

    @InjectMocks
    private PmscKpiProvisioningHandler pmscKpiProvisioningHandler;

    @Test
    void doApply() {

        this.pmscKpiProvisioningHandler.doApply(Collections.emptyList());

        verify(this.pmscHandler, times(1)).submit(anyList());
    }
}