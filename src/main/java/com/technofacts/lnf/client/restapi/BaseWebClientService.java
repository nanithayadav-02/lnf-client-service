package com.technofacts.lnf.client.restapi;

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

