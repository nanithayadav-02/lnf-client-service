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

package com.lnf.client.restapi;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Optional;

@Service
public abstract class BaseWebClientService {

    protected WebClient.RequestHeadersSpec<?> addJwtToken(WebClient.RequestHeadersSpec<?> spec) {
        // Retrieve the JWT token value, if available
        Optional<String> jwtTokenValue = getJwtTokenValue();

        // If the token value is present, add it to the request headers
        jwtTokenValue.ifPresent(token -> spec.headers(header -> header.setBearerAuth(token)));
        return spec;
    }

    private Optional<String> getJwtTokenValue() {
        return Optional.ofNullable(getJwtToken())
                .map(Jwt::getTokenValue);
    }

    private Jwt getJwtToken() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
            return jwt;
        }
        return null;
    }
}

