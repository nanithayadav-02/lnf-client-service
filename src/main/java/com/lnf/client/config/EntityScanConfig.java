package com.lnf.client.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EntityScanConfig {

    @Bean(name = "jpaPackagesToScan")
    public String[] jpaPackagesToScan() {
        return new String[]{"com.lnf.client.model", "com.lnf.tenant.core.model"};
    }
}