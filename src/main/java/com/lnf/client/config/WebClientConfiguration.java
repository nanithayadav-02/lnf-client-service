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

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.exporter.otlp.http.trace.OtlpHttpSpanExporter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Configuration
public class WebClientConfiguration {

    @Value("${employee.service.url}")
    private String employeeServiceUrl;

    @Value("${file.service.url}")
    private String fileServiceUrl;

    @Value("${timesheet.service.url}")
    private String timesheetServiceUrl;

    @Value("${email.service.url}")
    private String emailServiceUrl;

    @Value("${application.maxInMemorySize}")
    private int maxInMemorySize;

    @Value("${connection.timeout}")
    private int timeOut;


    @Bean
    WebClient employeeWebClient() {
        return createWebClient(employeeServiceUrl);
    }

    @Bean
    @Primary
    WebClient fileServiceWebClient() {
        return createWebClient(fileServiceUrl);
    }

    @Bean
    WebClient timesheetServiceWebClient() {
        return createWebClient(timesheetServiceUrl);
    }

    @Bean
    WebClient emailServiceWebClient() {
        return createWebClient(emailServiceUrl);
    }

    private WebClient createWebClient(String baseUrl) {
        var exchangeStrategies = ExchangeStrategies.builder()
                .codecs(codecs -> codecs.defaultCodecs().maxInMemorySize(maxInMemorySize))
                .build();

        var httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, timeOut)
                .responseTimeout(Duration.ofMillis(timeOut))
                .doOnConnected(conn -> conn.addHandlerLast(new ReadTimeoutHandler(timeOut, TimeUnit.MILLISECONDS))
                        .addHandlerLast(new WriteTimeoutHandler(timeOut, TimeUnit.MILLISECONDS)));

        return WebClient.builder()
                .baseUrl(baseUrl)
                .filter(addTracingHeaders())
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .exchangeStrategies(exchangeStrategies)
                .build();
    }

    @Bean
    @ConditionalOnProperty(name = "management.tracing.enabled", havingValue = "true", matchIfMissing = false)
    OtlpHttpSpanExporter otlpHttpSpanExporter(@Value("${tracing.url:}") String url) {
        if (url == null || url.isEmpty()) {
            return null;
        }
        return OtlpHttpSpanExporter.builder()
                .setEndpoint(url)
                .build();
    }

    private ExchangeFilterFunction addTracingHeaders() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            Span currentSpan = Span.current();

            if (currentSpan.getSpanContext().isValid()) {
                ClientRequest.Builder requestBuilder = ClientRequest.from(clientRequest)
                        .header("traceparent", getTraceParentHeader(currentSpan));

                return Mono.just(requestBuilder.build());
            }
            return Mono.just(clientRequest);
        });
    }

    private String getTraceParentHeader(Span span) {
        return String.format("00-%s-%s-01",
                span.getSpanContext().getTraceId(),
                span.getSpanContext().getSpanId());
    }

}
