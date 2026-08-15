package com.hybridapi.tests;

import io.restassured.response.Response;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class ProductsApiTest extends BaseApiTest {

    @Test
    public void getSingleProductReturnsExpectedFields() {
        Response response = client.get("/products/1");

        assertEquals(response.statusCode(), 200);
        assertEquals(response.jsonPath().getInt("id"), 1);
        assertTrue(response.jsonPath().getString("title").length() > 0);
    }

    @Test
    public void getProductListRespectsLimitParam() {
        Response response = client.get("/products", java.util.Map.of("limit", 5));

        assertEquals(response.statusCode(), 200);
        assertEquals(response.jsonPath().getList("products").size(), 5);
    }

    @Test
    public void addProductReturnsCreatedIdAndEchoesTitle() {
        Response response = client.post("/products/add", java.util.Map.of("title", "hybrid-api test product"));

        assertEquals(response.statusCode(), 201);
        assertTrue(response.jsonPath().getInt("id") > 0);
        assertEquals(response.jsonPath().getString("title"), "hybrid-api test product");
    }

    @Test
    public void updateProductReturnsUpdatedTitle() {
        Response response = client.put("/products/1", java.util.Map.of("title", "hybrid-api updated title"));

        assertEquals(response.statusCode(), 200);
        assertEquals(response.jsonPath().getString("title"), "hybrid-api updated title");
    }

    @Test
    public void deleteProductMarksIsDeletedTrue() {
        Response response = client.delete("/products/1");

        assertEquals(response.statusCode(), 200);
        assertTrue(response.jsonPath().getBoolean("isDeleted"));
    }
}
