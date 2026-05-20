package com.technofacts.lnf.client.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtIssuerAuthenticationManagerResolver;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.session.RegisterSessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;

@Configuration
@EnableWebSecurity
@Profile({"prod", "dev"})
public class SecurityConfig {

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri:}")
    private String issuerUri;

    @Autowired(required = false)
    private JwtIssuerAuthenticationManagerResolver multiTenantJwtResolver;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    private String ACTUATOR_HEALTH_ENDPOINT_PATTERN;
    private String ACTUATOR_INFO_ENDPOINT_PATTERN;

    @PostConstruct
    public void initialize() {
        this.ACTUATOR_HEALTH_ENDPOINT_PATTERN = contextPath + "/actuator/health/**";
        this.ACTUATOR_INFO_ENDPOINT_PATTERN = contextPath+ "/actuator/info";
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(ACTUATOR_HEALTH_ENDPOINT_PATTERN, ACTUATOR_INFO_ENDPOINT_PATTERN).permitAll() // Allow health checks w/o authN
                        .anyRequest().authenticated())
                .sessionManagement(sessionManagement -> sessionManagement
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .oauth2ResourceServer(oauth2 -> {
                    if (multiTenantJwtResolver != null) {
                        oauth2.authenticationManagerResolver(multiTenantJwtResolver);
                    } else {
                        oauth2.jwt(Customizer.withDefaults());
                    }
                })
                .build();
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        if (issuerUri == null || issuerUri.isBlank()) {
            return token -> { throw new IllegalStateException("Single-tenant mode: spring.security.oauth2.resourceserver.jwt.issuer-uri is required"); };
        }
        return JwtDecoders.fromIssuerLocation(issuerUri);
    }

    @Bean
    public SessionAuthenticationStrategy sessionAuthenticationStrategy() {
        return new RegisterSessionAuthenticationStrategy(sessionRegistry());
    }

    @Bean
    public SessionRegistry sessionRegistry() {
        return new SessionRegistryImpl();
    }

}