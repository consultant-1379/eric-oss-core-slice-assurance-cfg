/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.service.augmentation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.ericsson.oss.air.csac.model.AugmentationDefinition;
import com.ericsson.oss.air.csac.model.AugmentationRule;
import com.ericsson.oss.air.csac.model.augmentation.AugmentationRequestDto;
import com.ericsson.oss.air.csac.repository.EffectiveAugmentationDAO;
import com.ericsson.oss.air.util.codec.Codec;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

class DryrunAugmentationProvisioningServiceTest {

    private final Codec codec = new Codec();

    private final Codec mockCodec = spy(this.codec);

    private final EffectiveAugmentationDAO dao = mock(EffectiveAugmentationDAO.class);

    private final DryrunAugmentationProvisioningService service = new DryrunAugmentationProvisioningService(this.codec, this.dao);

    private final DryrunAugmentationProvisioningService mockService = new DryrunAugmentationProvisioningService(this.mockCodec, this.dao);

    private ListAppender<ILoggingEvent> listAppender;

    @BeforeEach
    public void setUpLogging() {

        final Logger logger = (Logger) LoggerFactory.getLogger(DryrunAugmentationProvisioningService.class);
        this.listAppender = new ListAppender<>();
        this.listAppender.start();
        logger.addAppender(listAppender);
    }

    @Test
    void checkArdqType_default() {

        final AugmentationDefinition def = AugmentationDefinition.builder()
                .name("aug1")
                .build();

        this.service.checkArdqType(def);

        assertEquals("default", this.listAppender.list.get(0).getArgumentArray()[0]);

    }

    @Test
    void checkArdqType_nonDefault() {

        final AugmentationDefinition def = AugmentationDefinition.builder()
                .name("aug1")
                .type("core")
                .build();

        this.service.checkArdqType(def);

        assertEquals("core", this.listAppender.list.get(0).getArgumentArray()[0]);

    }

    @Test
    void create() throws Exception {

        final AugmentationRequestDto dto = AugmentationRequestDto.builder()
                .ardqId("aug1")
                .build();

        this.service.create(List.of(dto));

        final List<Map<String, Object>> actual = this.codec.readValue(this.listAppender.list.get(0).getArgumentArray()[0].toString(), List.class);

        assertEquals(1, this.listAppender.list.size());
        assertEquals("aug1", actual.get(0).get("ardqId"));
    }

    @Test
    void create_jsonException() throws Exception {

        when(this.mockCodec.writeValueAsString(any())).thenThrow(JsonProcessingException.class);

        final AugmentationRequestDto dto = AugmentationRequestDto.builder()
                .ardqId("aug1")
                .build();

        assertThrows(JsonProcessingException.class, () -> this.mockService.create(List.of(dto)));

    }

    @Test
    void update() throws Exception {

        final AugmentationRequestDto dto = AugmentationRequestDto.builder()
                .ardqId("aug1")
                .build();

        this.service.update(List.of(dto));

        final List<Map<String, Object>> actual = this.codec.readValue(this.listAppender.list.get(0).getArgumentArray()[0].toString(), List.class);

        assertEquals(1, this.listAppender.list.size());
        assertEquals("aug1", actual.get(0).get("ardqId"));
    }

    @Test
    void update_jsonException() throws Exception {

        when(this.mockCodec.writeValueAsString(any())).thenThrow(JsonProcessingException.class);

        final AugmentationRequestDto dto = AugmentationRequestDto.builder()
                .ardqId("aug1")
                .build();

        assertThrows(JsonProcessingException.class, () -> this.mockService.update(List.of(dto)));

    }

    @Test
    void delete() throws Exception {

        final AugmentationRequestDto dto = AugmentationRequestDto.builder()
                .ardqId("aug1")
                .build();

        this.service.delete(List.of(dto));

        final List<Map<String, Object>> actual = this.codec.readValue(this.listAppender.list.get(0).getArgumentArray()[0].toString(), List.class);

        assertEquals(1, this.listAppender.list.size());
        assertEquals("aug1", actual.get(0).get("ardqId"));
    }

    @Test
    void delete_jsonException() throws Exception {

        when(this.mockCodec.writeValueAsString(any())).thenThrow(JsonProcessingException.class);

        final AugmentationRequestDto dto = AugmentationRequestDto.builder()
                .ardqId("aug1")
                .build();

        assertThrows(JsonProcessingException.class, () -> this.mockService.delete(List.of(dto)));

    }

    @Test
    void getSchemaMappings() {

        final String ardqId = "cardq";

        final String inputSchema1 = "5G|PM_COUNTERS|AMF_Mobility_NetworkSlice_1";
        final String inputSchema2 = "5G|PM_COUNTERS|smf_nsmf_pdu_session_snssai_apn_1";
        final String inputSchema3 = "5G|PM_COUNTERS|up_payload_dnn_slice_1";

        final List<String> inputSchemaListA = List.of(inputSchema1, inputSchema2);
        final List<String> inputSchemaListB = List.of(inputSchema3);

        final AugmentationDefinition definition = AugmentationDefinition.builder()
                .name(ardqId)
                .augmentationRules(List.of(new AugmentationRule(null, inputSchemaListA, null),
                        new AugmentationRule(null, inputSchemaListB, null)))
                .build();

        when(this.dao.findById(ardqId)).thenReturn(Optional.of(definition));

        final String outputSchema1 = "5G|PM_COUNTERS|cardq_AMF_Mobility_NetworkSlice_1";
        final String outputSchema2 = "5G|PM_COUNTERS|cardq_smf_nsmf_pdu_session_snssai_apn_1";
        final String outputSchema3 = "5G|PM_COUNTERS|cardq_up_payload_dnn_slice_1";

        final Map<String, String> expectedMap = new HashMap<>();
        expectedMap.put(inputSchema1, outputSchema1);
        expectedMap.put(inputSchema2, outputSchema2);
        expectedMap.put(inputSchema3, outputSchema3);

        final Map<String, String> actualMap = this.mockService.getSchemaMappings("cardq");

        assertEquals(expectedMap.size(), actualMap.size());
        assertEquals(expectedMap.get(inputSchema1), actualMap.get(inputSchema1));
        assertEquals(expectedMap.get(inputSchema2), actualMap.get(inputSchema2));
        assertEquals(expectedMap.get(inputSchema3), actualMap.get(inputSchema3));

    }

    @Test
    void getSchemaMappings_NoAugmentations() {

        final String ardqId = "cardq";

        when(this.dao.findById(ardqId)).thenReturn(Optional.empty());

        assertEquals(0, this.mockService.getSchemaMappings(ardqId).size());
    }

    @Test
    void deleteAll() throws Exception {

        final AugmentationDefinition def = AugmentationDefinition.builder().name("aug1").build();

        when(this.dao.findAll()).thenReturn(List.of(def));

        this.service.deleteAll();

        final List<Map<String, Object>> actual = this.codec.readValue(this.listAppender.list.get(0).getArgumentArray()[0].toString(), List.class);

        assertEquals(1, this.listAppender.list.size());
        assertEquals("aug1", actual.get(0).get("ardq_id"));
    }

    @Test
    void deleteAll_jsonException() throws Exception {

        when(this.mockCodec.writeValueAsString(any())).thenThrow(JsonProcessingException.class);

        assertThrows(JsonProcessingException.class, () -> this.mockService.deleteAll());

    }
}