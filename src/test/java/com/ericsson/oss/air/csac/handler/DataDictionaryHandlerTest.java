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

import static com.ericsson.oss.air.csac.model.TestResourcesUtils.VALID_PM_SCHEMA_DEFINITION_PM_COUNTER;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ericsson.oss.air.csac.handler.validation.ValidationHandler;
import com.ericsson.oss.air.csac.model.AugmentationDefinition;
import com.ericsson.oss.air.csac.model.KPIDefinition;
import com.ericsson.oss.air.csac.model.PMDefinition;
import com.ericsson.oss.air.csac.model.ProfileDefinition;
import com.ericsson.oss.air.csac.model.ResourceSubmission;
import com.ericsson.oss.air.csac.repository.AugmentationDefinitionDAO;
import com.ericsson.oss.air.csac.repository.KPIDefinitionDAO;
import com.ericsson.oss.air.csac.repository.PMDefinitionDAO;
import com.ericsson.oss.air.csac.repository.PMSchemaDefinitionDao;
import com.ericsson.oss.air.csac.repository.ProfileDefinitionDAO;
import com.ericsson.oss.air.csac.service.ResourceFileLoader;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class DataDictionaryHandlerTest {

    @MockBean
    private ResourceFileLoader resourceFileLoader;

    @MockBean
    private ValidationHandler validationHandler;

    @MockBean
    private PMDefinitionDAO pmDefinitionDAO;

    @MockBean
    private KPIDefinitionDAO kpiDefinitionDAO;

    @MockBean
    private AugmentationDefinitionDAO augmentationDefinitionDAO;

    @MockBean
    private ProfileDefinitionDAO profileDefinitionDAO;

    @MockBean
    private PMSchemaDefinitionDao pmSchemaDefinitionDao;

    @Autowired
    DataDictionaryHandler dataDictionaryHandler;

    @Test
    void insertPMDefinitions() {
        final ResourceSubmission resourceSubmission = new ResourceSubmission();
        when(resourceFileLoader.loadResourceFilePath(any())).thenReturn(resourceSubmission);

        final Map<String, List<PMDefinition>> mapOfPMDefs = new HashMap<>();
        when(validationHandler.getValidPMDefinitions(resourceSubmission.getPmDefs())).thenReturn(mapOfPMDefs);

        this.dataDictionaryHandler.insertPMDefinitions(mapOfPMDefs);
        verify(pmDefinitionDAO, times(1)).insertPMDefinitions(any());
    }

    @Test
    void insertKPIDefinitions() {
        final List<KPIDefinition> listOfKPIDefs = new ArrayList<>();
        this.dataDictionaryHandler.insertKPIDefinitions(listOfKPIDefs);
        verify(kpiDefinitionDAO, times(1)).insertKPIDefinitions(any());
    }

    @Test
    void insertAugmentationDefinitions() {

        final List<AugmentationDefinition> definitionList = new ArrayList<>();
        this.dataDictionaryHandler.insertAugmentationDefinitions(definitionList);
        verify(this.augmentationDefinitionDAO, times(1)).saveAll(any());
    }

    @Test
    void insertProfileDefinitions() {

        final List<ProfileDefinition> definitionList = new ArrayList<>();
        this.dataDictionaryHandler.insertProfileDefinitions(definitionList);
        verify(this.profileDefinitionDAO, times(1)).saveAll(any());

    }

    @Test
    void insertPMSchemaDefinitions() {
        this.dataDictionaryHandler.insertPMSchemaDefinitions(List.of(VALID_PM_SCHEMA_DEFINITION_PM_COUNTER));

        verify(this.pmSchemaDefinitionDao, times(1)).saveAll(any());
    }

}