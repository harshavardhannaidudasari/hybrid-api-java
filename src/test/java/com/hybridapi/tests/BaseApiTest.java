package com.hybridapi.tests;

import com.hybridapi.ApiClient;
import org.testng.annotations.BeforeClass;

public abstract class BaseApiTest {

    protected ApiClient client;

    @BeforeClass
    public void setUpClient() {
        client = new ApiClient();
    }
}
