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

package com.lnf.client.config;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.Jwt;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class JpaAuditingConfig {

    private static final String CLAIM = "email";
    private static final String DEFAULT_AUDITOR = "SYSTEM";

    @Bean
    public AuditorAware<String> auditorProvider() {
        return this::getAuditor;
    }

    private Optional<String> getAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            Object principal = authentication.getPrincipal();
            return getPrincipalValue(principal);
        }
        return Optional.of(DEFAULT_AUDITOR);
    }

    private Optional<String> getPrincipalValue(Object principal) {
        Map<Class<?>, Function<Object, Optional<String>>> principalExtractor = new HashMap<>();
        principalExtractor.put(Jwt.class, claim -> Optional.ofNullable(((Jwt) claim).getClaimAsString(CLAIM)));
        principalExtractor.put(UserDetails.class, userDetails -> Optional.of(((UserDetails) userDetails).getUsername()));
        principalExtractor.put(String.class, principalStr -> Optional.of((String) principalStr));

        return principalExtractor.entrySet().stream()
                .filter(entry -> entry.getKey().isInstance(principal))
                .map(entry -> entry.getValue().apply(principal))
                .findFirst().orElse(Optional.empty());
    }

}
