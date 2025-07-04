package com.lnf.client.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

@Configuration
public class DynamicDataSourceConfig {

    @Value("${spring.datasource.url}")
    private String clientUrl;

    @Value("${spring.datasource.username}")
    private String clientUsername;

    @Value("${spring.datasource.password}")
    private String clientPassword;

    @Value("${spring.datasource.driverClassName}")
    private String clientDriverClassName;

    @Value("${spring.tenant.datasource.url}")
    private String tenantUrl;

    @Value("${spring.tenant.datasource.username}")
    private String tenantUsername;

    @Value("${spring.tenant.datasource.password}")
    private String tenantPassword;

    @Value("${spring.tenant.datasource.driverClassName}")
    private String tenantDriverClassName;

    @Value("${lnf.tenant.enabled:true}")
    private boolean tenantEnabled;

    @Bean
    @Primary
    public DataSource dataSource() {
        HikariConfig config = new HikariConfig();

        config.setJdbcUrl(tenantEnabled ? tenantUrl : clientUrl);
        config.setUsername(tenantEnabled ? tenantUsername : clientUsername);
        config.setPassword(tenantEnabled ? tenantPassword : clientPassword);
        config.setDriverClassName(tenantEnabled ? tenantDriverClassName : clientDriverClassName);

        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setIdleTimeout(60000);

        return new HikariDataSource(config);
    }

}
