package com.lnf.client.config;

import com.lnf.client.repository.DataSourceConfigRepository;
import com.lnf.tenant.core.model.DataSourceConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class TenantDataSource {

    private final DataSourceConfigRepository configRepository;
    private final Map<String, DataSource> dataSourceMap = new ConcurrentHashMap<>();

    public DataSource getDataSource(String tenantId) {
        return dataSourceMap.computeIfAbsent(tenantId, this::createDataSource);
    }

    private DataSource createDataSource(String tenantId) {
        DataSourceConfig config = configRepository.findByTenantId(tenantId);
        if (config == null || !config.isInitialize()) {
            throw new RuntimeException("Invalid or inactive tenant: " + tenantId);
        }
        return DataSourceBuilder.create()
                .url(config.getUrl())
                .username(config.getUsername())
                .password(config.getPassword())
                .driverClassName(config.getDriverClassName())
                .build();
    }

    public Map<String, DataSource> preloadAll() {
        return configRepository.findAll().stream()
                .filter(DataSourceConfig::isInitialize)
                .collect(Collectors.toMap(DataSourceConfig::getName, config -> createDataSource(config.getName())));
    }
}