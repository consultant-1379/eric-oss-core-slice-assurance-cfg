/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.util;

import java.util.Map;
import java.util.Objects;

/**
 * Utilities for using Map classes.
 */
public class MapUtils {

    private MapUtils() {
        // no-op.  Hides implicit default ctor.
    }

    /**
     * Returns the map entry in the source map corresponding to the provided key.
     *
     * @param key
     *         map entry key
     * @param source
     *         source map
     * @param <K>
     *         map key type
     * @param <V>
     *         mapy value type
     * @return the map entr in the source map corresponding to the provide key, or null if none is present
     */
    public static <K, V> Map.Entry<K, V> getMapEntry(K key, Map<K, V> source) {

        Objects.requireNonNull(key);
        Objects.requireNonNull(source);

        // iterate over the source entry set and return the entry whose key matches the provided key.
        return source.entrySet().stream().filter(e -> e.getKey().equals(key)).findFirst().orElse(null);
    }
}
