/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.handler.pmsc.legacy;

import java.util.ArrayList;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = LoopbackProvisioner.class)
class LoopbackProvisionerTest {

    @Autowired
    private Provisioner provisioner;

    @Test
    void provision_AnyKpisAnyProfiles_Void() {
        this.provisioner.provision(new ArrayList<>(), new ArrayList<>());
    }

}