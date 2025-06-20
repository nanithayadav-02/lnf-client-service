/*
 *
 *  * Copyright © 2024 Lever And Fulcrum Solutions (hereinafter referred to as "LNF").
 *  * All rights reserved.
 *  *
 *  * This source code is the proprietary property of LNF
 *  *
 *  * Unauthorized copying, redistribution, or modification of this code,
 *  * via any medium, is strictly prohibited unless expressly authorized
 *  * in writing by LNF.
 *  *
 *  * This code is confidential and intended solely for the use of LNF
 *  * and its authorized personnel.
 *
 */

package com.lnf.client;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {"com.lnf.client", "com.lnf.config.jpa"})
@EnableCaching
@EnableDiscoveryClient
@EntityScan(basePackages = {"com.lnf.client.model", "com.lnf.tenant.core.model"})
@EnableJpaRepositories(basePackages = "com.lnf.client.repository")
@ComponentScan(basePackages = "com.lnf.client")
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
