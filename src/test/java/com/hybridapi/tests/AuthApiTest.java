package com.hybridapi.tests;

import com.hybridapi.ApiClient;
import com.hybridapi.ApiConfig;
import io.restassured.response.Response;
import org.testng.annotations.Test;

import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Proves the ApiClient's bearer-token support end to end: log in for a real
 * JWT, then use it on a follow-up request to a protected endpoint. This is
 * the flow a UI project would reuse to, e.g., seed/verify account state via
 * the API instead of driving the browser through a login form just to set up
 * test data.
 */
public class AuthApiTest extends BaseApiTest {

    @Test
    public void meEndpointRejectsRequestWithNoToken() {
        Response response = client.get("/auth/me");

        assertEquals(response.statusCode(), 401);
    }

    @Test
    public void loginThenMeEndpointReturnsAuthenticatedUser() {
        Response login = client.post("/auth/login", Map.of(
                "username", ApiConfig.AUTH_USERNAME,
                "password", ApiConfig.AUTH_PASSWORD));

        assertEquals(login.statusCode(), 200);
        String token = login.jsonPath().getString("accessToken");
        assertTrue(token != null && !token.isBlank());

        ApiClient authedClient = new ApiClient().withBearerToken(token);
        Response me = authedClient.get("/auth/me");

        assertEquals(me.statusCode(), 200);
        assertEquals(me.jsonPath().getString("username"), ApiConfig.AUTH_USERNAME);
    }
}
