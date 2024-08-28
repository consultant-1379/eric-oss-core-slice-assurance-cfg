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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;


import com.ericsson.oss.air.csac.model.AugmentationDefinition;

/**
 * Generic difference utility that can generate lists of new, updated, and deleted resources when given two sequences of resources to compare. To
 * perform the comparisons, the diff engine requires an identify function that returns a unique string identifier for each element in the sequences
 * being compared. For example, to compare {@link AugmentationDefinition} resources in the candidate and source sequences, the
 * {@link AugmentationDefinition#getName()} method provides the identity comparison required.  The {@code DiffEngine} does not modify the source
 * sequence.
 *
 * An example of the {@code DiffEngine} is shown below.
 *
 * <pre>
 *     final List&lt;AugmentationDefinition&gt; existingDefinitions = effectiveAugmentationDao.findAll();
 *
 *     final Function&lt;AugmentationDefinition, String&gt; identity = AugmentationDefinition::getName;
 *
 *     final DiffEngine&lt;AugmentationDefinition&gt; diffEngine = DiffEngine.bulider()
 *              .source(existingDefinitions)
 *              .identityFunction(identity)
 *              .build();
 *
 *      final List&lt;AugmentationDefinition&gt; addedDefinitions = diffEngine.getAdded(existingDefinitions);
 *      final List&lt;AugmentationDefinition&gt; updatedDefinitions = diffEngine.getUpdated(existingDefintions);
 *      final List&lt;AugmentationDefinition&gt; deletedDefinitions = diffEngine.getDeleted(existingDefinitions);
 * </pre>
 *
 * @param <T>
 *         object type in the sequences being compared.
 */
public class DiffEngine<T> {

    private final Map<String, T> existingResources;

    private final Function<T, String> identityFunction;

    /**
     * Builder class for the {@code DiffEngine<T>} type.
     *
     * @param <T>
     *         object type in the sequences being compared.
     */
    public static class DiffEngineBuilder<T> {

        private List<T> resourceList;
        private Function<T, String> identityFunction;

        /**
         * Sets the sequence of existing elements for the comparison.
         *
         * @param existingResources
         *         the sequence of existing elements for the comparison
         * @return this builder
         */
        public DiffEngineBuilder<T> source(final List<T> existingResources) {
            this.resourceList = new ArrayList<>(existingResources);
            return this;
        }

        /**
         * Sets the identity function used to identify unique resources in the sequences being compared.
         *
         * @param identity
         *         the identity function used to identify unique resources in the sequences being compared
         * @return this builder
         */
        public DiffEngineBuilder<T> identityFunction(final Function<T, String> identity) {
            this.identityFunction = identity;
            return this;
        }

        /**
         * Returns a {@code DiffEngine} instance using the source sequence and identity functions provided in the builder methods.
         *
         * @return a {@code DiffEngine} instance using the source sequence and identity functions provided in the builder methods
         */
        public DiffEngine<T> build() {

            checkForNull("existingResources", this.resourceList);
            checkForNull("identity", this.identityFunction);
            return new DiffEngine<>(this.resourceList, this.identityFunction);
        }

        private void checkForNull(final String elementName, final Object element) {
            if (Objects.isNull(element)) {
                throw new NullPointerException("DiffEngineBuilder error: " + elementName + " must not be null");
            }
        }
    }

    private DiffEngine(final List<T> existingResources, final Function<T, String> identityFunction) {
        this.existingResources = existingResources.stream().collect(Collectors.toMap(identityFunction, Function.identity()));
        this.identityFunction = identityFunction;
    }

    /**
     * Returns a builder for the {@code DiffEngine}.
     *
     * @return a builder for the {@code DiffEngine}
     */
    public static DiffEngineBuilder builder() {
        return new DiffEngineBuilder();
    }

    /**
     * Returns a list of all resources in the provided candidate resource list that do not exist in the existing resource list.
     *
     * @param candidateResources
     *         list of candidate resources
     * @return a list of all resources in the provided candidate resource list that do not exist in the existing resource list
     */
    public List<T> getAdded(final List<T> candidateResources) {

        final List<T> addedResources = new ArrayList<>();

        for (final T candidate : candidateResources) {
            final String candidateIdentity = this.identityFunction.apply(candidate);
            if (!this.existingResources.containsKey(candidateIdentity)) {
                addedResources.add(candidate);
            }
        }

        return addedResources;
    }

    /**
     * Returns a list of all resources in the candidate resource list that exist in the existing resource list but are different from the existing
     * resources.
     *
     * @param candidateResources
     *         list of candidate resources
     * @return a list of all resources in the candidate resource list that exist in the existing resource list but are different from the existing
     *         resources
     */
    public List<T> getUpdated(final List<T> candidateResources) {

        final List<T> updatedResources = new ArrayList<>();

        for (final T candidate : candidateResources) {
            final String candidateIdentity = this.identityFunction.apply(candidate);
            if (this.existingResources.containsKey(candidateIdentity) && !candidate.equals(this.existingResources.get(candidateIdentity))) {
                updatedResources.add(candidate);
            }
        }
        
        return updatedResources;
    }

    /**
     * Returns a list of all resources that exist in the existing resource list but are not present in the candidate resource list.
     *
     * @param candidateResources
     *         list of candidate resources
     * @return a list of all resources that exist in the existing resource list but are not present in the candidate resource list
     */
    public List<T> getDeleted(final List<T> candidateResources) {

        final List<T> deletedResources = new ArrayList<>();

        final Set<String> candidateIdentities = candidateResources.stream().map(this.identityFunction).collect(Collectors.toSet());
        final Set<String> deletedIdentities = new HashSet<>(this.existingResources.keySet());

        deletedIdentities.removeAll(candidateIdentities);

        for (final String deletedIdentity : deletedIdentities) {
            deletedResources.add(this.existingResources.get(deletedIdentity));
        }

        return deletedResources;
    }

}
