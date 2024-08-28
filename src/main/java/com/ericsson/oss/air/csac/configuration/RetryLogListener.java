/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.csac.configuration;

import java.util.Arrays;

import com.ericsson.oss.air.util.logging.audit.AuditLogFactory;
import com.ericsson.oss.air.util.logging.audit.AuditLogger;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.RetryContext;
import org.springframework.retry.annotation.Retryable;
import org.springframework.retry.interceptor.MethodInvocationRetryCallback;
import org.springframework.retry.listener.MethodInvocationRetryListenerSupport;
import org.springframework.stereotype.Component;

/**
 * Responsible for issuing audit logs for retry attempts and outcomes. This listener is active for all methods annotated with {@code Retryable},
 * regardless if the executed REST request in an annotated method throws an {@code Exception} or is successful.
 */
@Component
@Slf4j
public class RetryLogListener extends MethodInvocationRetryListenerSupport {

    private static final AuditLogger AUDIT_LOGGER = AuditLogFactory.getLogger(RetryLogListener.class);

    @Override
    protected <T, E extends Throwable> void doClose(final RetryContext context, final MethodInvocationRetryCallback<T, E> callback,
                                                    final Throwable throwable) {

        if (isExhaustedAfterFailure(context)) {
            AUDIT_LOGGER.error("Retries exhausted after " + context.getRetryCount() + " attempts. Cause: ", throwable);
        }

        super.doClose(context, callback, throwable);
    }

    @Override
    protected <T, E extends Throwable> void doOnSuccess(final RetryContext context, final MethodInvocationRetryCallback<T, E> callback,
                                                        final T result) {

        // Only log if the previous attempts failed
        if (context.getRetryCount() > 0) {
            AUDIT_LOGGER.info("Attempt {} successful for previous exception: {}", context.getRetryCount() + 1,
                    context.getLastThrowable().getMessage());
        }

        super.doOnSuccess(context, callback, result);
    }

    @Override
    protected <T, E extends Throwable> void doOnError(final RetryContext context, final MethodInvocationRetryCallback<T, E> callback,
                                                      final Throwable throwable) {

        final Retryable retryable = callback.getInvocation().getMethod().getAnnotation(Retryable.class);

        final Class<? extends Throwable>[] retriedExceptions = retryable.retryFor();

        /*
         * Checks if the thrown exception is a recoverable exception.
         * If no retryable exceptions are specified for the annotated method, then all thrown exceptions will trigger a retry.
         */
        final boolean isRetryable =
                retriedExceptions.length == 0 || Arrays.stream(retriedExceptions).anyMatch(retryException -> retryException.isInstance(throwable));

        if (isRetryable) {
            AUDIT_LOGGER.error("Attempt {} failed: {}. Retrying.", context.getRetryCount(), throwable.getMessage());
        }

        super.doOnError(context, callback, throwable);
    }

    /*
     * Returns true if the retry attempts are exhausted after the attempts have all failed. False, if otherwise.
     *
     * If a retry attempt is successful, then the context will not be exhausted. Unrecoverable exceptions that did not trigger any retry attempts will be
     * exhausted and have a retry count (a.k.a. attempt number) of 1.
     */
    private boolean isExhaustedAfterFailure(final RetryContext context) {
        return Boolean.parseBoolean(String.valueOf(context.getAttribute(RetryContext.EXHAUSTED))) && context.getRetryCount() > 1;
    }
}
