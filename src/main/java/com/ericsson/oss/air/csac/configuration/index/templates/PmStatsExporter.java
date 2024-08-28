/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.configuration.index.templates;

/**
 * Data bean containing PM Stats Exporter specific indexer templates.
 */
public class PmStatsExporter {

    public static final String SOURCE = "pmstatsexporter";

    public static final String INDEX_WRITER_TEMPLATE = "{\n" +
            "  \"name\": null,\n" +
            "  \"inputSchema\": null,\n" +
            "  \"context\": [],\n" +
            "  \"value\": [],\n" +
            "  \"info\": [\n" +
            "    {\n" +
            "      \"name\": \"begin_timestamp\",\n" +
            "      \"type\": \"time\",\n" +
            "      \"recordName\": \"aggregation_begin_time\",\n" +
            "      \"description\": \"Aggregation period start timestamp\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"name\": \"end_timestamp\",\n" +
            "      \"type\": \"time\",\n" +
            "      \"recordName\": \"aggregation_end_time\",\n" +
            "      \"description\": \"Aggregation period end timestamp\"\n" +
            "    }\n" +
            "  ]\n" +
            "}";

    public static final String DEPLOYED_INDEX_DEFINITION_TEMPLATE = "{\n" +
            "  \"name\": \"ESOA-2-Indexer\",\n" +
            "  \"description\": \"ESOA 2.0 Indexer specification. This indexer will be used by default to index all exportable runtime KPIs.\",\n" +
            "  \"source\": {\n" +
            "    \"name\": \"pm-stats-calc-handling-avro-scheduled\",\n" +
            "    \"type\": \"pmstatsexporter\",\n" +
            "    \"description\": \"ESOA 2.0 Indexer source specificiation\"\n" +
            "  },\n" +
            "  \"target\": {\n" +
            "    \"name\": \"index-oob\",\n" +
            "    \"displayName\": \"ESOA 2.0 KPI Index\",\n" +
            "    \"description\": \"Default index for all out-of-box (OOB) KPIs\"\n" +
            "  },\n" +
            "  \"writers\": []\n" +
            "}";

    // Private constructor prevents instantiation
    private PmStatsExporter() {
        // no-op
    }
}
