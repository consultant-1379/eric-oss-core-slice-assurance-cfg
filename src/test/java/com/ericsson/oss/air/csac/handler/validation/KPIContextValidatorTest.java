/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.handler.validation;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import com.ericsson.oss.air.csac.model.InputMetric;
import com.ericsson.oss.air.csac.model.KPIDefinition;
import com.ericsson.oss.air.csac.model.PMDefinition;
import com.ericsson.oss.air.csac.model.ResourceSubmission;
import com.ericsson.oss.air.csac.repository.KPIDefinitionDAO;
import com.ericsson.oss.air.csac.repository.PMDefinitionDAO;
import com.ericsson.oss.air.csac.service.ResourceFileLoader;
import com.ericsson.oss.air.exception.CsacValidationException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
public class KPIContextValidatorTest {
    @MockBean
    private ResourceFileLoader resourceFileLoader;

    @Autowired
    private KPIContextValidator kpiValidator;

    @MockBean
    private PMDefinitionDAO pmDefinitionDAO;

    @MockBean
    private KPIDefinitionDAO kpiDefinitionDAO;

    private static final String PM_NAME_A = "myPM_A";
    private static final String PM_NAME_B = "myPM_B";
    private static final String PM_NAME_X = "myPM_x";
    private static final String KPI_NAME_A = "myKPI_A";
    private static final String KPI_NAME_B = "myKPI_B";
    private static final String KPI_NAME_D = "myKPI_D";

    private static final InputMetric inputMetric_PM_A = new InputMetric(PM_NAME_A, "p0", InputMetric.Type.PM_DATA);
    private static final InputMetric inputMetric_KPI_A = new InputMetric(KPI_NAME_A, "k0", InputMetric.Type.KPI);
    private static final InputMetric inputMetric_PM_B = new InputMetric(PM_NAME_B, "p1", InputMetric.Type.PM_DATA);
    private static final InputMetric inputMetric_KPI_B = new InputMetric(KPI_NAME_B, "k1", InputMetric.Type.KPI);
    private static final InputMetric inputMetric_PM_X = new InputMetric(PM_NAME_X, "px", InputMetric.Type.PM_DATA);
    private static final InputMetric inputMetric_KPI_X = new InputMetric("myKPI_X", "kx", InputMetric.Type.KPI);
    private static final InputMetric inputMetric_C = new InputMetric(PM_NAME_X, "p1", InputMetric.Type.PM_DATA);
    private static final List<InputMetric> inputMetrics_PM_A = List.of(inputMetric_PM_A);
    private static final List<InputMetric> inputMetrics_PM_B = List.of(inputMetric_PM_B);
    private static final List<InputMetric> inputMetrics_KPI_A = List.of(inputMetric_KPI_A);
    private static final List<InputMetric> inputMetrics_KPI_B = List.of(inputMetric_KPI_B);
    private static final List<InputMetric> inputMetrics_D = Arrays.asList(inputMetric_PM_A, inputMetric_KPI_A);
    private static final List<InputMetric> inputMetrics_E = Arrays.asList(inputMetric_PM_A, inputMetric_C);
    private static final List<InputMetric> inputMetrics_PM_X = Arrays.asList(inputMetric_PM_X, inputMetric_KPI_X);
    private static final List<InputMetric> inputMetrics_KPI_X = List.of(inputMetric_KPI_X);

    private static final String SCHEMA_REF = "myUniqueReference";

    private static final PMDefinition PM_DEFN_A = new PMDefinition(PM_NAME_A, SCHEMA_REF, null);
    private static final PMDefinition PM_DEFN_B = new PMDefinition(PM_NAME_B, SCHEMA_REF, null);
    private static final KPIDefinition KPI_DEFN_PM_A = new KPIDefinition(
            KPI_NAME_A, null, null, "AVG(a1)", "AVG", null, true, inputMetrics_PM_A);
    private static final KPIDefinition KPI_DEFN_PM_B = new KPIDefinition(
            KPI_NAME_B, null, null, "AVG(a1)", "AVG", null, true, inputMetrics_PM_B);
    private static final KPIDefinition KPI_DEFN_KPI_A = new KPIDefinition(
            KPI_NAME_A, null, null, "AVG(a1)", "AVG", null, true, inputMetrics_KPI_A);
    private static final KPIDefinition KPI_DEFN_KPI_B = new KPIDefinition(
            KPI_NAME_B, null, null, "AVG(a1)", "AVG", null, true, inputMetrics_KPI_B);
    private static final KPIDefinition KPI_DEFN_PM_Invalid = new KPIDefinition(
            KPI_NAME_A, null, null, "AVG(a1)", "AVG", null, true, inputMetrics_PM_X);
    private static final KPIDefinition KPI_DEFN_KPI_Invalid = new KPIDefinition(
            KPI_NAME_A, null, null, "AVG(a1)", "AVG", null, true, inputMetrics_KPI_X);
    private static final KPIDefinition KPI_DEFN_D_Invalid = new KPIDefinition(
            KPI_NAME_D, null, null, "AVG(a1)", "AVG", null, true, inputMetrics_D);
    private static final KPIDefinition KPI_DEFN_E = new KPIDefinition(
            KPI_NAME_D, null, null, "AVG(a1)", "AVG", null, true, inputMetrics_E);

    private static final ResourceSubmission RS_1 = new ResourceSubmission();
    private static final ResourceSubmission RS_2 = new ResourceSubmission();
    private static final ResourceSubmission RS_3 = new ResourceSubmission();
    private static final ResourceSubmission RS_4 = new ResourceSubmission();
    private static final ResourceSubmission RS_5 = new ResourceSubmission();

    @BeforeAll
    static void setUpClass() {

        RS_1.setPmDefs(List.of(PM_DEFN_A));
        RS_1.setKpiDefs(List.of(KPI_DEFN_PM_A));
        RS_2.setPmDefs(List.of(PM_DEFN_B));
        RS_2.setKpiDefs(List.of(KPI_DEFN_KPI_A));
        RS_3.setPmDefs(Arrays.asList(PM_DEFN_A, PM_DEFN_B));
        RS_3.setKpiDefs(Arrays.asList(KPI_DEFN_KPI_A, KPI_DEFN_KPI_B, KPI_DEFN_PM_A, KPI_DEFN_PM_B));
        RS_4.setPmDefs(List.of(PM_DEFN_A));
        RS_4.setKpiDefs(Arrays.asList(KPI_DEFN_D_Invalid, KPI_DEFN_KPI_Invalid, KPI_DEFN_PM_Invalid));
    }

    @Test
    void checkSimpleKPIContext_InResourceSubmission_Valid() {
        this.kpiValidator.checkSimpleInputMetricContext(KPI_DEFN_PM_A, RS_1);
    }

    @Test
    void checkSimpleKPIContext_InDictionary_Valid() {
        Mockito.when(pmDefinitionDAO.getAllPmDefNames()).thenReturn(new HashSet<>(List.of(PM_NAME_B)));
        this.kpiValidator.checkSimpleInputMetricContext(KPI_DEFN_PM_B, RS_1);
    }

    @Test
    void checkSimpleKPIContext_Invalid() {
        assertThrows(CsacValidationException.class, () -> this.kpiValidator.checkSimpleInputMetricContext(KPI_DEFN_PM_Invalid, RS_1));
    }

    @Test
    void checkComplexInputMetricContext_InResourceSubmission_Valid() {
        this.kpiValidator.checkComplexInputMetricContext(KPI_DEFN_KPI_A, RS_2);
    }

    @Test
    void checkComplexInputMetricContext_InDictionary_Valid() {
        Mockito.when(kpiDefinitionDAO.getAllKpiDefNames()).thenReturn(new HashSet<>(List.of(KPI_NAME_B)));
        this.kpiValidator.checkComplexInputMetricContext(KPI_DEFN_KPI_B, RS_2);
    }

    @Test
    void checkComplexInputMetricContext_Invalid() {
        assertThrows(CsacValidationException.class, () -> this.kpiValidator.checkComplexInputMetricContext(KPI_DEFN_KPI_Invalid, RS_2));
    }

    @Test
    void isComplexKpi_True() {
        assertTrue(KPIContextValidator.isComplexKpi(KPI_DEFN_KPI_A));
    }

    @Test
    void isComplexKpi_False() {
        assertFalse(KPIContextValidator.isComplexKpi(KPI_DEFN_PM_A));
    }

    @Test
    void validateKPIDefinitions_Succeed() {
        this.kpiValidator.validateKPIDefinitions(RS_3);
    }

    @Test
    void validate() {

        assertDoesNotThrow(() -> this.kpiValidator.validate(RS_3));
    }

    @Test
    void validateKPIDefinitions_Failed() {
        assertThrows(CsacValidationException.class, () -> this.kpiValidator.validateKPIDefinitions(RS_4));
    }
}
