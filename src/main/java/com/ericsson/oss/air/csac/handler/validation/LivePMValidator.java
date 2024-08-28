/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.handler.validation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.ericsson.oss.air.csac.configuration.schema.InputSchemaProvider;
import com.ericsson.oss.air.csac.model.PMDefinition;
import com.ericsson.oss.air.csac.model.pmschema.PmSchema;
import com.ericsson.oss.air.exception.CsacValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

/**
 * Component that validates the PM definitions using external services
 */
@Component
@Slf4j
@ConditionalOnProperty(value = "validation.external.enabled",
                       havingValue = "true")
@Primary
public class LivePMValidator implements PMValidator {

    @Autowired
    private InputSchemaProvider inputSchemaProvider;

    /**
     * Validates that the PM names from the submitted resources are present in their associated Avro PM schemas.
     * <p>
     * Validation will fail if any of the following conditions are true:
     *  <ol>
     * 	<li>A reference to the schema cannot be located in the Data Catalog</li>
     * 	<li>The schema cannot be retrieved because it does not exist in the Schema Registry</li>
     *  <li>The schema retrieved from the Schema Registry does not contain the PM names</li>
     *  </ol>
     *  This method returns a map keyed on PM schema names each with a list of their corresponding PM definitions.
     * <p>
     * <ul>
     * <li>the PM Definition's source attribute is equal to the subject, the schema's registered name in the Schema Registry</li>
     * <li>subject = dataSpace|dataCategory|schemaName</li>
     * <li>the message schema's specification reference = subject/version where version can be an integer or "latest"</li>
     * <li>CSAC will always retrieve the latest version of a PM schema.</li>
     * </ul>
     *
     * @param submittedPMDefinitions the submitted PM definitions
     * @return a map keyed on PM schema names each with a list of their corresponding PM definitions
     */
    @Override
    public Map<String, List<PMDefinition>> getValidPMDefinitions(final List<PMDefinition> submittedPMDefinitions) {

        if (ObjectUtils.isEmpty(submittedPMDefinitions)) {
            return new HashMap<>();
        }

        final Set<String> inputSchemaReferencesSet = submittedPMDefinitions.stream().map(PMDefinition::getSource).collect(Collectors.toSet());

        // If there is no message schema found in the Data Catalog for the reference, a validation exception will be thrown during the prefetch
        this.inputSchemaProvider.prefetchInputSchemas(inputSchemaReferencesSet);

        final Map<String, List<PMDefinition>> validatedSchemaPmDefMap = new HashMap<>();
        submittedPMDefinitions.stream().distinct().forEach(pmDefinition -> {
            final String pmName = pmDefinition.getName();
            final PmSchema schema = this.inputSchemaProvider.getSchema(pmDefinition.getSource());
            if (Objects.isNull(schema)) {
                final String errorMsg = "Can not retrieve schema with reference '" + pmDefinition.getSource() + "'" + " from Schema Registry";
                log.error(errorMsg);
                throw new CsacValidationException(errorMsg);
            }
            final List<String> schemaFieldsNames = schema.getFieldPaths();
            if (!schemaFieldsNames.contains(pmName)) {
                final String errorMessage = "The PM name(s): " + pmName
                        + " cannot be found in the PM schema named " + schema.getName() + ". The PM schema contains: "
                        + schema.getFieldPaths();
                log.error(errorMessage);
                throw new CsacValidationException(errorMessage);
            }
            validatedSchemaPmDefMap.merge(schema.getName(), new ArrayList<>(List.of(pmDefinition)), (oldValue, newValue) -> {
                oldValue.addAll(newValue);
                return oldValue;
            });
        });

        log.info("Live PM validation is successful.");

        return validatedSchemaPmDefMap;

    }

    @Override
    public void validate(final List<PMDefinition> submittedPMDefinitions) {

        this.getValidPMDefinitions(submittedPMDefinitions);

    }

}
