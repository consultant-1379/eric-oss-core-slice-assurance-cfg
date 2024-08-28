/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.model;

import static com.ericsson.oss.air.csac.model.TestResourcesUtils.PM_COUNTER_DESC;
import static com.ericsson.oss.air.csac.model.TestResourcesUtils.PM_COUNTER_NAME;
import static com.ericsson.oss.air.csac.model.TestResourcesUtils.PM_SCHEMA_CONTEXT;
import static com.ericsson.oss.air.csac.model.TestResourcesUtils.PM_SCHEMA_NAME;
import static com.ericsson.oss.air.csac.model.TestResourcesUtils.PM_SCHEMA_URI_STR;
import static com.ericsson.oss.air.csac.model.TestResourcesUtils.VALID_DEF_WITH_ONE_COUNTER_STR;
import static com.ericsson.oss.air.csac.model.TestResourcesUtils.VALID_PM_SCHEMA_DEFINITION_PM_COUNTER;
import static com.ericsson.oss.air.csac.model.TestResourcesUtils.VALID_PM_SCHEMA_DEFINITION_WO_PM_COUNTERS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import com.ericsson.oss.air.csac.model.pmschema.SchemaURI;
import com.ericsson.oss.air.util.codec.Codec;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.fasterxml.jackson.databind.exc.ValueInstantiationException;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;
import org.apache.commons.collections.ListUtils;
import org.junit.jupiter.api.Test;

class PMSchemaDefinitionTest {

    private static final PMSchemaDefinition VALID_PM_SCHEMA_DEFINITION_EMPTY_PM_COUNTERS = PMSchemaDefinition.builder()
            .name(PM_SCHEMA_NAME)
            .uri(SchemaURI.fromString(PM_SCHEMA_URI_STR))
            .context(PM_SCHEMA_CONTEXT)
            .pmCounters(ListUtils.EMPTY_LIST)
            .build();

    private static final PMSchemaDefinition VALID_PM_SCHEMA_DEFINITION_PM_COUNTER_WO_DESC = PMSchemaDefinition.builder()
            .name(PM_SCHEMA_NAME)
            .uri(SchemaURI.fromString(PM_SCHEMA_URI_STR))
            .context(PM_SCHEMA_CONTEXT)
            .pmCounters(List.of(PMSchemaDefinition.PMCounter.builder().name(PM_COUNTER_NAME).build()))
            .build();

    private static final String VALID_DEF_WITH_NO_COUNTERS_STR = "{\"name\":\"smf_nsmf_pdu_session_snssai_apn_1\",\"uri\":\"dc:5G|PM_COUNTERS|smf_nsmf_pdu_session_snssai_apn_1\",\"context\":[\"nodeFDN\",\"snssai\",\"apn\"]}";

    private static final String VALID_DEF_WITH_EMPTY_COUNTERS_STR = "{\"name\":\"smf_nsmf_pdu_session_snssai_apn_1\",\"uri\":\"dc:5G|PM_COUNTERS|smf_nsmf_pdu_session_snssai_apn_1\",\"pm_counters\":[],\"context\":[\"nodeFDN\",\"snssai\",\"apn\"]}";

    private static final String VALID_DEF_WITH_ONE_COUNTER_NO_DESC_STR = "{\"name\":\"smf_nsmf_pdu_session_snssai_apn_1\",\"uri\":\"dc:5G|PM_COUNTERS|smf_nsmf_pdu_session_snssai_apn_1\",\"pm_counters\":[{\"name\":\"pmCounters.create_sm_context_resp_succ\"}],\"context\":[\"nodeFDN\",\"snssai\",\"apn\"]}\n";

    private static final String INVALID_DEF_WITH_INVALID_NAME_STR = "{\"name\":\"\",\"uri\":\"dc:5G|PM_COUNTERS|smf_nsmf_pdu_session_snssai_apn_1\",\"context\":[\"nodeFDN\",\"snssai\",\"apn\"]}";

    private static final String INVALID_DEF_WITH_NO_URI_STR = "{\"name\":\"smf_nsmf_pdu_session_snssai_apn_1\",\"context\":[\"nodeFDN\",\"snssai\",\"apn\"]}";

    private static final String INVALID_DEF_WITH_INVALID_URI_PATTERN_STR = "{\"name\":\"\",\"uri\":\"5G|PM_COUNTERS|smf_nsmf_pdu_session_snssai_apn_1\",\"context\":[\"nodeFDN\",\"snssai\",\"apn\"]}";

    private static final String INVALID_DEF_WITH_INVALID_PM_COUNTER_TYPE_STR = "{\"name\":\"\",\"uri\":\"dc:5G|PM_COUNTERS|smf_nsmf_pdu_session_snssai_apn_1\",\"pm_counters\":\"foobar\",\"context\":[\"nodeFDN\",\"snssai\",\"apn\"]}";

    private static final String INVALID_DEF_WITH_INVALID_PM_COUNTER_NAME_STR = "{\"name\":\"smf_nsmf_pdu_session_snssai_apn_1\",\"uri\":\"dc:5G|PM_COUNTERS|smf_nsmf_pdu_session_snssai_apn_1\",\"pm_counters\":[{\"name\":\"pmCounters    create_sm_context_resp_succ\",\"description\":\"The number of successful Nsmf_PDUSession_CreateSMContext Response sent to AMF\"}],\"context\":[\"apn\"]}\n";

    private static final String INVALID_DEF_WITH_EMPTY_CONTEXT_STR = "{\"name\":\"smf_nsmf_pdu_session_snssai_apn_1\",\"uri\":\"dc:5G|PM_COUNTERS|smf_nsmf_pdu_session_snssai_apn_1\",\"pm_counters\":[{\"name\":\"pmCounters.create_sm_context_resp_succ\",\"description\":\"The number of successful Nsmf_PDUSession_CreateSMContext Response sent to AMF\"}],\"context\":[]}\n";

    private static final String INVALID_DEF_WITH_BLANK_CONTEXT_ELEM_STR = "{\"name\":\"smf_nsmf_pdu_session_snssai_apn_1\",\"uri\":\"dc:5G|PM_COUNTERS|smf_nsmf_pdu_session_snssai_apn_1\",\"pm_counters\":[{\"name\":\"create_sm_context_resp_succ\",\"description\":\"The number of successful Nsmf_PDUSession_CreateSMContext Response sent to AMF\"}],\"context\":[\"\"]}\n";

    private static final Codec CODEC = new Codec(Validation.buildDefaultValidatorFactory().getValidator());

    @Test
    public void validPMSchemaDefinition_noPMCounters() throws Exception {

        final PMSchemaDefinition actual = CODEC.withValidation().readValue(VALID_DEF_WITH_NO_COUNTERS_STR, PMSchemaDefinition.class);

        assertEquals(VALID_PM_SCHEMA_DEFINITION_WO_PM_COUNTERS, actual);
    }

    @Test
    public void validPMSchemaDefinition_emptyPMCounters() throws Exception {

        final PMSchemaDefinition actual = CODEC.withValidation().readValue(VALID_DEF_WITH_EMPTY_COUNTERS_STR, PMSchemaDefinition.class);

        assertEquals(VALID_PM_SCHEMA_DEFINITION_EMPTY_PM_COUNTERS, actual);
    }

    @Test
    public void validPMSchemaDefinition_onePMCountersNoDesc() throws Exception {

        final PMSchemaDefinition actual = CODEC.withValidation()
                .readValue(VALID_DEF_WITH_ONE_COUNTER_NO_DESC_STR, PMSchemaDefinition.class);

        assertEquals(VALID_PM_SCHEMA_DEFINITION_PM_COUNTER_WO_DESC, actual);
    }

    @Test
    public void validPMSchemaDefinition_onePMCounter() throws Exception {

        final PMSchemaDefinition actual = CODEC.withValidation().readValue(VALID_DEF_WITH_ONE_COUNTER_STR, PMSchemaDefinition.class);

        assertEquals(VALID_PM_SCHEMA_DEFINITION_PM_COUNTER, actual);
    }

    @Test
    public void invalidPMSchemaDefinition_invalidName() {

        assertThrows(ConstraintViolationException.class,
                () -> CODEC.withValidation().readValue(INVALID_DEF_WITH_INVALID_NAME_STR, PMSchemaDefinition.class));
    }

    @Test
    public void invalidPMSchemaDefinition_noUri() {

        assertThrows(ConstraintViolationException.class,
                () -> CODEC.withValidation().readValue(INVALID_DEF_WITH_NO_URI_STR, PMSchemaDefinition.class));
    }

    @Test
    public void invalidPMSchemaDefinition_invalidUriPattern() {

        assertThrows(ValueInstantiationException.class,
                () -> CODEC.withValidation().readValue(INVALID_DEF_WITH_INVALID_URI_PATTERN_STR, PMSchemaDefinition.class));
    }

    @Test
    public void invalidPMSchemaDefinition_invalidPMCounterType() {

        assertThrows(MismatchedInputException.class,
                () -> CODEC.withValidation().readValue(INVALID_DEF_WITH_INVALID_PM_COUNTER_TYPE_STR, PMSchemaDefinition.class));
    }

    @Test
    public void invalidPMSchemaDefinition_invalidPMCounterName() {

        assertThrows(ConstraintViolationException.class,
                () -> CODEC.withValidation().readValue(INVALID_DEF_WITH_INVALID_PM_COUNTER_NAME_STR, PMSchemaDefinition.class));
    }

    @Test
    public void invalidPMSchemaDefinition_emptyContext() {

        assertThrows(ConstraintViolationException.class,
                () -> CODEC.withValidation().readValue(INVALID_DEF_WITH_EMPTY_CONTEXT_STR, PMSchemaDefinition.class));
    }

    @Test
    public void invalidPMSchemaDefinition_invalidContextElement() {

        assertThrows(ConstraintViolationException.class,
                () -> CODEC.withValidation().readValue(INVALID_DEF_WITH_BLANK_CONTEXT_ELEM_STR, PMSchemaDefinition.class));
    }

    @Test
    public void hasPmCounters_returnsTrue() {

        assertTrue(VALID_PM_SCHEMA_DEFINITION_PM_COUNTER_WO_DESC.hasPmCounters());
        assertTrue(VALID_PM_SCHEMA_DEFINITION_PM_COUNTER.hasPmCounters());
    }

    @Test
    public void hasPmCounters_returnsFalse() {

        final PMSchemaDefinition pmSchemaNullPmCounters = PMSchemaDefinition.builder()
                .name(PM_SCHEMA_NAME)
                .uri(SchemaURI.fromString(PM_SCHEMA_URI_STR))
                .context(PM_SCHEMA_CONTEXT)
                .pmCounters(null)
                .build();

        assertFalse(pmSchemaNullPmCounters.hasPmCounters());
        assertFalse(VALID_PM_SCHEMA_DEFINITION_WO_PM_COUNTERS.hasPmCounters());
        assertFalse(VALID_PM_SCHEMA_DEFINITION_EMPTY_PM_COUNTERS.hasPmCounters());
    }

    @Test
    public void pmCounterToPmDefinition() {

        final SchemaURI schemaURI = SchemaURI.fromString(PM_SCHEMA_URI_STR);
        final PMSchemaDefinition.PMCounter pmCounter = PMSchemaDefinition.PMCounter.builder()
                .name(PM_COUNTER_NAME)
                .description(PM_COUNTER_DESC)
                .build();

        final PMDefinition expectedPmDef = PMDefinition.builder()
                .name(PM_COUNTER_NAME)
                .description(PM_COUNTER_DESC)
                .source("5G|PM_COUNTERS|smf_nsmf_pdu_session_snssai_apn_1")
                .build();
        final PMDefinition actualPmDef = pmCounter.toPmDefinition(schemaURI);

        assertEquals(expectedPmDef, actualPmDef);

    }

    @Test
    public void pmCounterToPmDefinition_nullSchemaUri() {

        final PMSchemaDefinition.PMCounter pmCounter = PMSchemaDefinition.PMCounter.builder()
                .name(PM_COUNTER_NAME)
                .description(PM_COUNTER_DESC)
                .build();

        assertThrows(NullPointerException.class, () -> pmCounter.toPmDefinition(null));

    }
}