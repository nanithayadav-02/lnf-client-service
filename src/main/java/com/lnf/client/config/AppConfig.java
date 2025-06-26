package com.lnf.client.config;

import com.lnf.tenant.core.context.TenantContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class AppConfig implements WebMvcConfigurer {

    public static final String DEFAULT_TENANT_ID = "client";

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new HandlerInterceptor() {
            @Override
            public boolean preHandle(jakarta.servlet.http.HttpServletRequest request,
                                     jakarta.servlet.http.HttpServletResponse response,
                                     Object handler) {
                String tenantID = request.getHeader("X-TenantID");
                if (tenantID == null || tenantID.isBlank()) {
                    tenantID = DEFAULT_TENANT_ID;
                }
                TenantContext.setCurrentTenant(tenantID);
                return true;
            }

            public void afterCompletion(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Object handler,
                                        Exception ex) {
                TenantContext.clear();
            }
        });
    }

}
