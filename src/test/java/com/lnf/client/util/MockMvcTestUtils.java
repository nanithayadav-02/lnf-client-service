package com.lnf.client.util;

import org.springframework.test.web.servlet.request.RequestPostProcessor;

public class MockMvcTestUtils {

    private static final String DEFAULT_TENANT_ID = "client";

    public static RequestPostProcessor withTenantHeader() {
        return request -> {
            request.addHeader("X-TenantID", DEFAULT_TENANT_ID);
            return request;
        };
    }
}
