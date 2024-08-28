/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.service.kpi.pmsc;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.ericsson.oss.air.csac.handler.pmsc.util.KpiId;
import com.ericsson.oss.air.csac.model.pmsc.KpiDefinitionDTO;
import com.ericsson.oss.air.csac.model.pmsc.KpiDefinitionSubmission;
import com.ericsson.oss.air.csac.model.pmsc.KpiOutputTableDto;
import com.ericsson.oss.air.csac.model.pmsc.KpiOutputTableListDto;
import com.ericsson.oss.air.csac.model.pmsc.KpiSubmissionDto;
import com.ericsson.oss.air.csac.model.pmsc.LegacyKpiSubmissionDto;
import com.ericsson.oss.air.csac.model.pmsc.PmscKpiDefinitionDto;
import com.ericsson.oss.air.csac.model.pmsc.response.PmscComplexOutputTableDto;
import com.ericsson.oss.air.csac.model.pmsc.response.PmscKpiResponseDto;
import com.ericsson.oss.air.csac.model.pmsc.response.PmscSimpleOutputTableDto;
import com.ericsson.oss.air.csac.service.exception.PmscHttpResponseErrorHandler;
import com.ericsson.oss.air.exception.http.InternalServerErrorException;
import com.ericsson.oss.air.exception.http.ServiceUnavailableException;
import com.ericsson.oss.air.exception.http.TooManyRequestsException;
import com.ericsson.oss.air.util.codec.Codec;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "provisioning.pmsc.restClient",
                       name = "url")
@Profile({ "prod", "test" })
public class LivePmscRestClient implements PmscRestClient {

    private final Codec codec = new Codec();

    private final RestTemplate restTemplate;

    @Value("${provisioning.pmsc.restClient.url}")
    private String pmscUrl;

    static final String PMSC_KPIS_DEFINITIONS_API = "/kpi-handling/model/v1/definitions";

    /**
     * Submits a request to the PMSC to create a set of new KPI definitions in the provided {@link KpiDefinitionSubmission}.
     *
     * @param kpiDefinitionSubmission {@link KpiDefinitionSubmission}
     * @return response entity
     */
    @Override
    @SneakyThrows
    // Use resilience4j configuration as workaround for the ticket ESOA-4203
    @Retryable(maxAttemptsExpression = "#{${resilience4j.retry.instances.pmsc.max-attempts}}",
               backoff = @Backoff(delayExpression = "#{${resilience4j.retry.instances.pmsc.wait-duration}}",
                                  multiplierExpression = "#{${resilience4j.retry.instances.pmsc.exponential-backoff-multiplier}}",
                                  maxDelayExpression = "#{${resilience4j.retry.instances.pmsc.maxDelay}}"),
               retryFor = { InternalServerErrorException.class, TooManyRequestsException.class, ServiceUnavailableException.class,
                       ResourceAccessException.class })
    public ResponseEntity<Void> create(final KpiDefinitionSubmission kpiDefinitionSubmission) {

        // before actually creating the KPIs in PMSC, let's check to see if they already exist
        // it is here to work around possible connection drops after successfully provisioning, preventing CSAC from
        // handling the success case properly.
        if (doSkipCreate(kpiDefinitionSubmission)) {
            log.info("Runtime KPIs already configured. Skipping PMSC provisioning.");
            return ResponseEntity.status(HttpStatus.CREATED).build();
        }

        final HttpHeaders headers = new HttpHeaders();

        headers.setContentType(MediaType.APPLICATION_JSON);
        final HttpEntity<KpiDefinitionSubmission> entity = new HttpEntity<>(kpiDefinitionSubmission, headers);

        log.info("Provisioning KPI submission: {}", this.codec.writeValueAsString(kpiDefinitionSubmission));

        final String url = this.pmscUrl + PMSC_KPIS_DEFINITIONS_API;
        final String endpoint = "POST " + url;

        this.restTemplate.setErrorHandler(new PmscHttpResponseErrorHandler(endpoint));

        log.info("PMSC request: {}", endpoint);

        try {
            final ResponseEntity<Void> response = this.restTemplate.exchange(url, HttpMethod.POST, entity,
                    Void.class);

            log.info("PMSC response: {} - {}", response.getStatusCode().value(), ((HttpStatus) response.getStatusCode()).getReasonPhrase());

            log.info("KPI submission provisioning successful");

            return response;
        } catch (final Exception reqEx) {
            log.error("Request error", reqEx);
            throw reqEx;
        }
    }

    @Override
    @Retryable(maxAttemptsExpression = "#{${resilience4j.retry.instances.pmsc.max-attempts}}",
               backoff = @Backoff(delayExpression = "#{${resilience4j.retry.instances.pmsc.wait-duration}}",
                                  multiplierExpression = "#{${resilience4j.retry.instances.pmsc.exponential-backoff-multiplier}}",
                                  maxDelayExpression = "#{${resilience4j.retry.instances.pmsc.maxDelay}}"),
               retryFor = { InternalServerErrorException.class, TooManyRequestsException.class, ServiceUnavailableException.class,
                       ResourceAccessException.class })
    public void delete(final List<KpiDefinitionDTO> kpiDefinitionDtoList) {
        this.deleteById(kpiDefinitionDtoList.stream().map(KpiDefinitionDTO::getName).collect(Collectors.toList()));
    }

    @Override
    public void deleteById(final List<String> ids) {
        if (ObjectUtils.isEmpty(ids)) {
            return;
        }
        // calls the DELETE /model/v1/definitions endpoint in PMSC
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        final HttpEntity<List<String>> entity = new HttpEntity<>(ids, headers);

        final String url = this.pmscUrl + PMSC_KPIS_DEFINITIONS_API;
        final String endpoint = "DELETE " + url;

        this.restTemplate.setErrorHandler(new PmscHttpResponseErrorHandler(endpoint));

        log.info("PMSC request: {} with list of kpis {}", endpoint, ids);

        final ResponseEntity<Void> response = this.restTemplate.exchange(url, HttpMethod.DELETE, entity,
                Void.class);

        log.info("PMSC response: {} - {}", response.getStatusCode().value(), ((HttpStatus) response.getStatusCode()).getReasonPhrase());
        log.info("Successfully deleted list of KPIs in PMSC");

    }

    @Override
    @Retryable(maxAttemptsExpression = "#{${resilience4j.retry.instances.pmsc.max-attempts}}",
               backoff = @Backoff(delayExpression = "#{${resilience4j.retry.instances.pmsc.wait-duration}}",
                                  multiplierExpression = "#{${resilience4j.retry.instances.pmsc.exponential-backoff-multiplier}}",
                                  maxDelayExpression = "#{${resilience4j.retry.instances.pmsc.maxDelay}}"),
               retryFor = { InternalServerErrorException.class, TooManyRequestsException.class, ServiceUnavailableException.class,
                       ResourceAccessException.class })
    public void deleteAll() {
        // retrieves all KPIs from the PMSC using the ::getAll method
        this.deleteById(this.getAll().stream().map(PmscKpiDefinitionDto::getName).collect(Collectors.toList()));
    }

    @Override
    @SneakyThrows
    public List<PmscKpiDefinitionDto> getAll() {

        final String url = this.pmscUrl + PMSC_KPIS_DEFINITIONS_API;
        final String endpoint = "GET " + url;

        // retrieves all KPIs whose names match the CSAC convention for runtime KPI names
        this.restTemplate.setErrorHandler(new PmscHttpResponseErrorHandler(endpoint));
        log.info("PMSC request: {}", endpoint);
        final ResponseEntity<PmscKpiResponseDto> allKpis = this.restTemplate.getForEntity(url,
                PmscKpiResponseDto.class);

        final PmscKpiResponseDto pmscKpiResponseDto = allKpis.getBody();
        if (Objects.isNull(pmscKpiResponseDto)) {
            log.info("No KPIs retrieved from PMSC");
            return List.of();
        }

        final List<PmscKpiDefinitionDto> csacKpis = new ArrayList<>();

        // Delete complex kpis first to resolve dependency validation failure
        final List<PmscComplexOutputTableDto> complexKpiTables = pmscKpiResponseDto.getScheduledComplex().getKpiOutputTables();
        complexKpiTables.forEach(kpiOutputTableDto -> csacKpis.addAll(
                kpiOutputTableDto.getKpiDefinitions().stream().filter(k -> k.getName().startsWith(KpiId.NAME_PREFIX + KpiId.STRING_JOINER_DELIMITER))
                        .collect(Collectors.toList())));

        final List<PmscSimpleOutputTableDto> simpleKpiTables = pmscKpiResponseDto.getScheduledSimple().getKpiOutputTables();
        simpleKpiTables.forEach(kpiOutputTableDto -> csacKpis.addAll(
                kpiOutputTableDto.getKpiDefinitions().stream().filter(k -> k.getName().startsWith(KpiId.NAME_PREFIX + KpiId.STRING_JOINER_DELIMITER))
                        .collect(Collectors.toList())));
        log.debug("Retrieved CSAC KPIs from PMSC: {} ", this.codec.writeValueAsString(csacKpis));
        return csacKpis;
    }

    @SneakyThrows
    protected boolean doSkipCreate(final KpiDefinitionSubmission kpiDefinitionSubmission) {

        log.info("Checking for existing runtime KPIs");

        final List<PmscKpiDefinitionDto> existingKpis = this.getAll();

        log.info("Found {} runtime KPIs", existingKpis.size());

        final Set<String> existingIds = existingKpis.stream().map(PmscKpiDefinitionDto::getName).collect(Collectors.toSet());

        final Set<String> candidateIds;

        if (kpiDefinitionSubmission instanceof KpiSubmissionDto submissionDto) {
            candidateIds = getKpiIdsFromSubmission(submissionDto);
        } else {
            candidateIds = getKpiIdsFromLegacy((LegacyKpiSubmissionDto) kpiDefinitionSubmission);
        }

        return existingIds.containsAll(candidateIds);
    }

    Set<String> getKpiIdsFromSubmission(final KpiSubmissionDto submissionDto) {

        final Set<String> candidateIds = new HashSet<>();

        candidateIds.addAll(getKpiIdsFromTables(submissionDto.getOnDemand()));
        candidateIds.addAll(getKpiIdsFromTables(submissionDto.getScheduledSimple()));
        candidateIds.addAll(getKpiIdsFromTables(submissionDto.getScheduledComplex()));

        return candidateIds;
    }

    Set<String> getKpiIdsFromLegacy(final LegacyKpiSubmissionDto legacyKpiSubmissionDto) {
        return legacyKpiSubmissionDto.getKpiDefinitionsList().stream().map(KpiDefinitionDTO::getName).collect(Collectors.toSet());
    }

    Set<String> getKpiIdsFromTables(final KpiOutputTableListDto tableListDto) {

        final Set<String> kpiIds = new HashSet<>();

        if (Objects.nonNull(tableListDto)) {
            for (final KpiOutputTableDto table : tableListDto.getKpiOutputTables()) {
                kpiIds.addAll(getKpiIdsFromTable(table));
            }
        }

        return kpiIds;
    }

    Set<String> getKpiIdsFromTable(final KpiOutputTableDto tableDto) {
        return tableDto.getKpiDefinitions().stream().map(PmscKpiDefinitionDto::getName).collect(Collectors.toSet());
    }

}
