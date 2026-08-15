package com.hybridapi.tests;

import com.hybridapi.ApiClient;
import io.restassured.response.Response;
import org.testng.annotations.Test;

import java.util.Map;

import static org.testng.Assert.assertEquals;

/**
 * Third real, independent verified target - an HTTP echo service, chosen to
 * directly prove each verb (get/post/put/delete) sends what ApiClient claims
 * it sends: the response reflects the exact query params / JSON body back.
 * Originally scoped against httpbin.org (also from the public-apis list),
 * substituted for postman-echo.com after httpbin.org's public instance was
 * found genuinely returning 503 Service Unavailable when checked live before
 * writing this test - see README "Why postman-echo.com, not httpbin.org".
 */
public class PostmanEchoTest {

    private final ApiClient client = new ApiClient("https://postman-echo.com");

    @Test
    public void getEchoesQueryParams() {
        Response response = client.get("/get", Map.of("foo", "bar"));

        assertEquals(response.statusCode(), 200);
        assertEquals(response.jsonPath().getString("args.foo"), "bar");
    }

    @Test
    public void postEchoesJsonBody() {
        Response response = client.post("/post", Map.of("name", "hybrid-api"));

        assertEquals(response.statusCode(), 200);
        assertEquals(response.jsonPath().getString("json.name"), "hybrid-api");
    }

    @Test
    public void putEchoesJsonBody() {
        Response response = client.put("/put", Map.of("name", "updated"));

        assertEquals(response.statusCode(), 200);
        assertEquals(response.jsonPath().getString("json.name"), "updated");
    }

    @Test
    public void deleteSucceeds() {
        Response response = client.delete("/delete");

        assertEquals(response.statusCode(), 200);
    }
}
