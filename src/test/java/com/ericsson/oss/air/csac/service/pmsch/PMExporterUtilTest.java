/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.service.pmsch;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class PMExporterUtilTest {

    @Test
    @Disabled("Used only to generate sample PM Exporter schemas")
    void generateSchemaTest() {

        final SchemaBuilder.FieldAssembler<Schema> assembler = SchemaBuilder.record("kpi_12345_15").fields();

        assembler.name("plmnid").type().nullable().stringType().noDefault();
        assembler.name("snssai").type().nullable().stringType().noDefault();
        assembler.name("managedelement").type().nullable().stringType().noDefault();
        assembler.name("movalue").type().nullable().stringType().noDefault();
        assembler.name("csac_01b45930_46d2_4991_a5b2_938ccd647bca").type().unionOf().nullType().and().floatType().endUnion().nullDefault();
        assembler.name("csac_cc42516f_a1fa_4a2c_b3bd_d6bb97a7a1a5").type().unionOf().nullType().and().floatType().endUnion().nullDefault();

        final Schema schema = assembler.endRecord();

        System.out.println(schema);
    }
}
