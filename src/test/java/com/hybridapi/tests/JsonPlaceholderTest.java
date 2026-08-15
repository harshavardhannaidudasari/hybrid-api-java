package com.hybridapi.tests;

import com.hybridapi.ApiClient;
import io.restassured.response.Response;
import org.testng.annotations.Test;

import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Second real, independent verified target (alongside dummyjson.com) proving
 * ApiClient genuinely works against any REST API via its baseUrl constructor
 * - no framework changes were needed to add this, only new test files.
 * jsonplaceholder.typicode.com is built for the exact same purpose as
 * dummyjson.com ("fake REST API for testing and prototyping"), sourced from
 * the public-apis list (github.com/public-apis/public-apis).
 */
public class JsonPlaceholderTest {

    private final ApiClient client = new ApiClient("https://jsonplaceholder.typicode.com");

    @Test
    public void getSinglePostReturnsExpectedFields() {
        Response response = client.get("/posts/1");

        assertEquals(response.statusCode(), 200);
        assertEquals(response.jsonPath().getInt("id"), 1);
        assertTrue(response.jsonPath().getString("title").length() > 0);
    }

    @Test
    public void getPostListRespectsLimitParam() {
        Response response = client.get("/posts", Map.of("_limit", 5));

        assertEquals(response.statusCode(), 200);
        assertEquals(response.jsonPath().getList("$").size(), 5);
    }

    @Test
    public void addPostReturnsCreatedIdAndEchoesTitle() {
        Response response = client.post("/posts", Map.of("title", "hybrid-api test", "body", "testing", "userId", 1));

        assertEquals(response.statusCode(), 201);
        assertTrue(response.jsonPath().getInt("id") > 0);
        assertEquals(response.jsonPath().getString("title"), "hybrid-api test");
    }

    @Test
    public void updatePostReturnsUpdatedTitle() {
        Response response = client.put("/posts/1", Map.of("id", 1, "title", "updated title", "body", "updated", "userId", 1));

        assertEquals(response.statusCode(), 200);
        assertEquals(response.jsonPath().getString("title"), "updated title");
    }

    @Test
    public void deletePostSucceeds() {
        Response response = client.delete("/posts/1");

        assertEquals(response.statusCode(), 200);
    }
}
