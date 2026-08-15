package com.hybridapi;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.util.Map;

import static io.restassured.RestAssured.given;

/**
 * Thin, reusable wrapper around RestAssured. This is the piece meant to be
 * imported by *other* projects (a UI suite that wants to assert on backend
 * state before/after driving the browser or an app, for example) rather than
 * used only by the tests in this repo - see README "Using this as a library".
 *
 * Every request goes through {@link RetryPolicy#withRetry}, so a transient
 * 5xx or dropped connection against a real public API doesn't fail a test
 * outright.
 */
public class ApiClient {

    private final String baseUrl;
    private String bearerToken;

    public ApiClient() {
        this(ApiConfig.BASE_URL);
    }

    public ApiClient(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    /** Returns this same client configured to send the given bearer token on every request. */
    public ApiClient withBearerToken(String token) {
        this.bearerToken = token;
        return this;
    }

    public Response get(String path) {
        return RetryPolicy.withRetry(() -> spec().when().get(path));
    }

    public Response get(String path, Map<String, ?> queryParams) {
        return RetryPolicy.withRetry(() -> spec().queryParams(queryParams).when().get(path));
    }

    public Response post(String path, Object body) {
        return RetryPolicy.withRetry(() -> spec().body(body).when().post(path));
    }

    public Response put(String path, Object body) {
        return RetryPolicy.withRetry(() -> spec().body(body).when().put(path));
    }

    public Response patch(String path, Object body) {
        return RetryPolicy.withRetry(() -> spec().body(body).when().patch(path));
    }

    public Response delete(String path) {
        return RetryPolicy.withRetry(() -> spec().when().delete(path));
    }

    private RequestSpecification spec() {
        RequestSpecBuilder builder = new RequestSpecBuilder()
                .setBaseUri(baseUrl)
                .setContentType(ContentType.JSON)
                .addFilter(new RequestLoggingFilter())
                .addFilter(new ResponseLoggingFilter());
        if (bearerToken != null) {
            builder.addHeader("Authorization", "Bearer " + bearerToken);
        }
        return given().spec(builder.build()).config(
                io.restassured.RestAssured.config().httpClient(
                        io.restassured.config.HttpClientConfig.httpClientConfig()
                                .setParam("http.connection.timeout", ApiConfig.TIMEOUT_MS)
                                .setParam("http.socket.timeout", ApiConfig.TIMEOUT_MS)));
    }
}
