package com.technofacts.lnf.client.config;

import com.technofacts.lnf.service.common.page.PaginationAndSortingHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PaginationAndSortingConfig {

    @Bean
    public PaginationAndSortingHandler paginationAndSortingHandler() {
        return new PaginationAndSortingHandler();
    }

}
