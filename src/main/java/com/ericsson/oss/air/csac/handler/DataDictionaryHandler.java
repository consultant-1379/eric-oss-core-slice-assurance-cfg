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

import java.util.List;
import java.util.Map;

import com.ericsson.oss.air.csac.model.AugmentationDefinition;
import com.ericsson.oss.air.csac.model.KPIDefinition;
import com.ericsson.oss.air.csac.model.PMDefinition;
import com.ericsson.oss.air.csac.model.PMSchemaDefinition;
import com.ericsson.oss.air.csac.model.ProfileDefinition;
import com.ericsson.oss.air.csac.repository.AugmentationDefinitionDAO;
import com.ericsson.oss.air.csac.repository.KPIDefinitionDAO;
import com.ericsson.oss.air.csac.repository.PMDefinitionDAO;
import com.ericsson.oss.air.csac.repository.PMSchemaDefinitionDao;
import com.ericsson.oss.air.csac.repository.ProfileDefinitionDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Handler class for persisting data dictionary resources.
 */
@Component
public class DataDictionaryHandler {

    @Autowired
    private PMDefinitionDAO pmDefinitionDAO;

    @Autowired
    private KPIDefinitionDAO kpiDefinitionDAO;

    @Autowired
    private AugmentationDefinitionDAO augmentationDefinitionDAO;

    @Autowired
    private ProfileDefinitionDAO profileDefinitionDAO;

    @Autowired
    private PMSchemaDefinitionDao pmSchemaDefinitionDao;

    /**
     * Insert validated PMDefinitions into Data Dictionary PMDefinitionDAO
     *
     * @param mapOfPMDefs map of PMDefinitions
     */
    public void insertPMDefinitions(final Map<String, List<PMDefinition>> mapOfPMDefs) {
        pmDefinitionDAO.insertPMDefinitions(mapOfPMDefs);
    }

    /**
     * Insert validated KPIDefinitions into Data Dictionary KPIDefinitionDAO
     *
     * @param kpiDefs list of KPIDefinitions
     */
    public void insertKPIDefinitions(final List<KPIDefinition> kpiDefs) {
        kpiDefinitionDAO.insertKPIDefinitions(kpiDefs);
    }

    /**
     * Saves all provided augmentation definitions in the data dictionary.  If any definitions already exist, they are updated in the dictionary/
     *
     * @param augmentationDefinitions list of augmentation definitions to save in the dictionary.
     */
    public void insertAugmentationDefinitions(final List<AugmentationDefinition> augmentationDefinitions) {

        this.augmentationDefinitionDAO.saveAll(augmentationDefinitions);
    }

    /**
     * Saves all provided profile definitions in the data dictionary. If any definitions already exist, they are updated in the dictionary.
     *
     * @param profileDefinitions list of profile definitions to save in the dictionary
     */
    public void insertProfileDefinitions(final List<ProfileDefinition> profileDefinitions) {
        profileDefinitionDAO.saveAll(profileDefinitions);
    }

    /**
     * Saves all provided PM Schema definitions in the data dictionary. If any definitions already exist, they are updated in the dictionary.
     *
     * @param pmSchemaDefinitions list of PM Schema definitions to save in the dictionary
     */
    public void insertPMSchemaDefinitions(final List<PMSchemaDefinition> pmSchemaDefinitions) {
        pmSchemaDefinitionDao.saveAll(pmSchemaDefinitions);
    }

}
