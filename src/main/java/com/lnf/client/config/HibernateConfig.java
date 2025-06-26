package com.lnf.client.config;

import com.lnf.tenant.core.context.TenantContext;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class HibernateConfig {

    private final JpaProperties jpaProperties;

    private static final String DEFAULT_TENANT_ID = "client";

    public HibernateConfig(JpaProperties jpaProperties) {
        this.jpaProperties = jpaProperties;
    }

    @Bean
    public JpaVendorAdapter jpaVendorAdapter() {
        return new HibernateJpaVendorAdapter();
    }

    @Bean
    public CurrentTenantIdentifierResolver tenantIdentifierResolver() {
        return new CurrentTenantIdentifierResolver() {
            @Override
            public String resolveCurrentTenantIdentifier() {
                String tenantId = TenantContext.getCurrentTenant();
                return (tenantId != null && !tenantId.isBlank()) ? tenantId : DEFAULT_TENANT_ID;
            }

            @Override
            public boolean validateExistingCurrentSessions() {
                return true;
            }
        };
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(
            DataSource dataSource,
            MultiTenantConnectionProvider multiTenantConnectionProvider
    ) {

        Map<String, Object> properties = new HashMap<>(jpaProperties.getProperties());
        properties.put(AvailableSettings.MULTI_TENANT_CONNECTION_PROVIDER, multiTenantConnectionProvider);
        properties.put(AvailableSettings.MULTI_TENANT_IDENTIFIER_RESOLVER, tenantIdentifierResolver());
        properties.put(AvailableSettings.DIALECT, "org.hibernate.dialect.PostgreSQLDialect");
        properties.put(AvailableSettings.SHOW_SQL, true);

        LocalContainerEntityManagerFactoryBean factoryBean = new LocalContainerEntityManagerFactoryBean();
        factoryBean.setDataSource(dataSource);
        factoryBean.setPackagesToScan("com.lnf.client.model", "com.lnf.tenant.core.model");
        factoryBean.setJpaVendorAdapter(jpaVendorAdapter());
        factoryBean.setJpaPropertyMap(properties);

        return factoryBean;
    }
}
