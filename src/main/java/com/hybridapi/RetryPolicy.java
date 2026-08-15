package com.hybridapi;

import java.util.function.Supplier;
import io.restassured.response.Response;

/**
 * Retries a request a fixed number of times on a 5xx response or a thrown
 * connection exception, with a fixed backoff between attempts. Kept dumb
 * on purpose - no exponential/jitter logic - since the goal is smoothing
 * over occasional flaky-network blips against a real public API, not
 * surviving a real outage.
 */
public final class RetryPolicy {

    private RetryPolicy() {
    }

    public static Response withRetry(Supplier<Response> request) {
        return withRetry(request, ApiConfig.RETRY_MAX_ATTEMPTS, ApiConfig.RETRY_BACKOFF_MS);
    }

    public static Response withRetry(Supplier<Response> request, int maxAttempts, long backoffMs) {
        RuntimeException lastFailure = null;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                Response response = request.get();
                if (response.statusCode() < 500) {
                    return response;
                }
                lastFailure = new IllegalStateException(
                        "Server error " + response.statusCode() + " on attempt " + attempt);
            } catch (RuntimeException e) {
                lastFailure = e;
            }
            if (attempt < maxAttempts) {
                sleep(backoffMs);
            }
        }
        throw lastFailure;
    }

    private static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }
}
