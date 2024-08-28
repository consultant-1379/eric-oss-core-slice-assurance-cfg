/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.util;

import java.util.function.Function;
import java.util.function.Predicate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NonNull;

/**
 * Reusable conditional function that allows execution of simple conditional statements
 * using a readable declarative syntax rather than an if-else statement.  Conditionals may
 * not be chained, so only a simple if-else statement can be modeled using this class.
 */

@AllArgsConstructor
@Builder
public class Conditional<R, V> {

    @NonNull
    private Predicate<V> condition;

    @NonNull
    private Function<V, R> value;

    @Builder.Default
    private Function<V, R> elseValue = a -> null;

    /**
     * Returns a value after applying the condition to the provided input.  If the condition
     * is {@code true}, the {@code value} is returned, otherwise the {@code elseValue} is returned.
     *
     * @param t input value to apply the condition to
     * @return a value after applying the condition to the provided input
     */
    public R apply(final V t) {

        return this.condition.test(t)
                ? this.value.apply(t)
                : this.elseValue.apply(t);
    }
}
