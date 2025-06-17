package com.lnf.client.restapi;

import com.lnf.client.config.TenantDataSource;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.hibernate.engine.jdbc.connections.spi.AbstractDataSourceBasedMultiTenantConnectionProviderImpl;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@Component
public class DataSourceBasedMultiTenantConnectionProviderImpl
        extends AbstractDataSourceBasedMultiTenantConnectionProviderImpl {

    private static final String DEFAULT_TENANT_ID = "multitenant";

    private final DataSource defaultDS;

    private final ApplicationContext context;

    private final Map<String, DataSource> dataSourceMap = new HashMap<>();
    private boolean initialized = false;

    @PostConstruct
    public void loadDefaultDataSource() {
        dataSourceMap.put(DEFAULT_TENANT_ID, defaultDS);
    }

    @Override
    protected DataSource selectAnyDataSource() {
        return dataSourceMap.get(DEFAULT_TENANT_ID);
    }

    @Override
    protected DataSource selectDataSource(Object object) {
        if (!initialized) {
            initialized = true;
            TenantDataSource tenantDataSource = context.getBean(TenantDataSource.class);
            dataSourceMap.putAll(tenantDataSource.getAll());
        }

        return dataSourceMap.getOrDefault(object, defaultDS);
    }

}