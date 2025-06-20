package com.lnf.client.controller;

import com.lnf.client.config.TenantDataSource;
import com.lnf.tenant.core.context.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Connection;
import java.sql.SQLException;

@RestController
@RequestMapping("/api/debug")
@RequiredArgsConstructor
public class DebugController {

    private final TenantDataSource tenantDataSource;

    @GetMapping("/test-db")
    public String testConnection() {
        String tenantId = TenantContext.getCurrentTenant();
        try (Connection conn = tenantDataSource.getDataSource(tenantId).getConnection()) {
            return "✅ Connection successful for tenant: " + tenantId;
        } catch (SQLException e) {
            return "❌ Failed to connect for tenant: " + tenantId + " - " + e.getMessage();
        }
    }
}