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

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import lombok.Getter;

/**
 * Static definitions of the default index templates.
 */
@Getter
public class IndexTemplateData {

    /**
     * Holder for templates associated with a single index source.
     */
    public static class IndexTemplateHolder {

        public enum IndexTemplateType {
            INDEX_WRITER,
            INDEX_DEFINITION
        }

        @Getter
        private final String source;

        @Getter
        private final Map<IndexTemplateType, String> templates = new EnumMap<>(IndexTemplateType.class);

        public IndexTemplateHolder(final String source) {
            this.source = source;
        }
    }

    private static final Map<String, IndexTemplateHolder> HOLDER_MAP = new HashMap<>();

    static {
        final IndexTemplateHolder pmStatsExporterHolder = new IndexTemplateHolder(PmStatsExporter.SOURCE);
        pmStatsExporterHolder.getTemplates().put(IndexTemplateHolder.IndexTemplateType.INDEX_WRITER, PmStatsExporter.INDEX_WRITER_TEMPLATE);
        pmStatsExporterHolder.getTemplates()
                .put(IndexTemplateHolder.IndexTemplateType.INDEX_DEFINITION, PmStatsExporter.DEPLOYED_INDEX_DEFINITION_TEMPLATE);

        HOLDER_MAP.put(pmStatsExporterHolder.getSource(), pmStatsExporterHolder);
    }

    // Private constructor prevents instantiation
    private IndexTemplateData() {
        // no-op
    }

    /**
     * Returns the set of known index sources.
     *
     * @return set of known index sources
     */
    public static Set<String> getIndexTemplateSources() {
        return HOLDER_MAP.keySet();
    }

    /**
     * Returns the specified template, if it exists.
     *
     * @param source
     *         index source
     * @param type
     *         template type
     * @return the specified template, if it exists
     */
    public static Optional<String> getIndexTemplate(final String source, final IndexTemplateHolder.IndexTemplateType type) {

        final Optional<IndexTemplateHolder> holder = Optional.ofNullable(HOLDER_MAP.get(source));

        if (holder.isPresent()) {
            return Optional.ofNullable(holder.get().templates.get(type));
        }

        return Optional.empty();
    }

}
