/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.model.runtime.metadata;

import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;

/**
 * The KpiContextId provides a single context ID string suitable for indexing the context and KPI metadata responses.
 */
@Data
public class KpiContextId implements Comparable<KpiContextId>, Supplier<String> {

    private String contextId;

    private static final String UNDERSCORE = "_";

    /**
     * Private constructor to prevent direct instantiation
     *
     * @param contextField the context field
     */
    private KpiContextId(final Set<String> contextField) {
        final List<String> contextList = contextField.stream()
                .map(String::toLowerCase)
                .map(s -> s.replaceAll("\\s", ""))
                .distinct()
                .sorted()
                .toList();
        this.contextId = StringUtils.join(contextList, UNDERSCORE);
    }

    /**
     * Static factor method to create {@link KpiContextId} instance
     *
     * @param contextField the context field
     * @return the kpi context id
     */
    public static KpiContextId of(final Set<String> contextField) {
        return new KpiContextId(contextField);
    }

    /**
     * Static factor method to create {@link KpiContextId} instance
     *
     * @param contextId the context id
     * @return the kpi context id
     */
    public static KpiContextId of(final String contextId) {
        return new KpiContextId(Set.of(contextId.split(UNDERSCORE)));
    }

    @Override
    public int compareTo(final KpiContextId o) {
        return this.get().compareTo(o.get());
    }

    @Override
    public String get() {
        return this.contextId;
    }
}
