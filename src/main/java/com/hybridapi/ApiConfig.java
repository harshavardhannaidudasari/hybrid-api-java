package com.hybridapi;

/**
 * Central place every setting comes from. Every value has a HYBRID_API_* env var
 * override so this library can be pointed at a different backend (or auth flow)
 * without touching code - that's what makes it reusable across projects/environments.
 */
public final class ApiConfig {

    public static final String BASE_URL = env("HYBRID_API_BASE_URL", "https://dummyjson.com");
    public static final int TIMEOUT_MS = Integer.parseInt(env("HYBRID_API_TIMEOUT_MS", "10000"));
    public static final int RETRY_MAX_ATTEMPTS = Integer.parseInt(env("HYBRID_API_RETRY_ATTEMPTS", "3"));
    public static final long RETRY_BACKOFF_MS = Long.parseLong(env("HYBRID_API_RETRY_BACKOFF_MS", "300"));

    public static final String AUTH_USERNAME = env("HYBRID_API_AUTH_USERNAME", "emilys");
    public static final String AUTH_PASSWORD = env("HYBRID_API_AUTH_PASSWORD", "emilyspass");

    private ApiConfig() {
    }

    private static String env(String key, String fallback) {
        String value = System.getenv(key);
        if (value == null || value.isBlank()) {
            String prop = System.getProperty(key);
            return (prop == null || prop.isBlank()) ? fallback : prop;
        }
        return value;
    }
}
