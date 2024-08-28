/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.service.kpi.pmsc;

import static com.ericsson.oss.air.csac.model.TestResourcesUtils.DEPLOYED_SIMPLE_KPI_OBJ;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.ericsson.oss.air.csac.model.pmsc.KpiCalculationDTO;
import com.ericsson.oss.air.csac.model.pmsc.LegacyKpiSubmissionDto;
import com.ericsson.oss.air.csac.model.pmsc.ParameterDTO;
import com.ericsson.oss.air.csac.repository.DeployedKpiDefDAO;
import com.ericsson.oss.air.csac.repository.impl.inmemorydb.DeployedKpiDefDAOImp;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

class DryRunPmscRestClientTest {

    static final ParameterDTO PARAMETER_DTO = new ParameterDTO("id", "filter");
    static final KpiCalculationDTO KPI_CALCULATION_DTO_OBJECT = new KpiCalculationDTO("source", List.of("kpi names"), PARAMETER_DTO);
    static final LegacyKpiSubmissionDto KPI_DEFINITIONS_SUBMISSION = new LegacyKpiSubmissionDto("source",
            List.of(DEPLOYED_SIMPLE_KPI_OBJ));

    private final DeployedKpiDefDAO deployedKpiDefDAO = new DeployedKpiDefDAOImp();
    private final DryRunPmscRestClient testClient = new DryRunPmscRestClient(this.deployedKpiDefDAO);

    private ListAppender<ILoggingEvent> listAppender;

    @BeforeEach
    void setup() {
        final Logger logger = (Logger) LoggerFactory.getLogger(DryRunPmscRestClient.class);
        logger.setLevel(Level.INFO);
        this.listAppender = new ListAppender<>();
        this.listAppender.start();
        logger.addAppender(listAppender);
    }

    @Test
    void create() {

        assertEquals(HttpStatus.NO_CONTENT, this.testClient.create(KPI_DEFINITIONS_SUBMISSION).getStatusCode());

        assertTrue(this.listAppender.list.get(0).getMessage().startsWith("Updating KPI definitions"));
    }

    @Test
    void delete() {
        this.testClient.delete(List.of(DEPLOYED_SIMPLE_KPI_OBJ));
        assertTrue(this.listAppender.list.get(0).getMessage().startsWith("Deleting KPI Definitions"));
    }

    @Test
    void deleteById() {
        this.testClient.deleteById(List.of("csac_3d3b5e94_51c6_401d_b1fd_440361beb32c_simple_kpi"));
        assertTrue(this.listAppender.list.get(0).getMessage().startsWith("Deleting KPI Definitions"));
    }

    @Test
    void deleteAll() {
        this.testClient.deleteAll();
        assertTrue(this.listAppender.list.get(0).getMessage().startsWith("Deleting KPI Definitions"));
    }

    @Test
    void getAll() {
        this.testClient.getAll();
        assertTrue(this.listAppender.list.get(0).getMessage().startsWith("Retrieving all KPIs"));
    }
}