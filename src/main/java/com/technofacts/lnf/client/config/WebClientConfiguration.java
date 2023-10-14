package com.technofacts.lnf.client.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Configuration
public class WebClientConfiguration {

    @Value("${employee.service.url}")
    private String employeeServiceUrl;

    @Value("${file.upload.service.url}")
    private String fileUploadServiceUrl;

    @Value("${application.maxInMemorySize}")
    private int maxInMemorySize;

    @Qualifier("EmployeeService")
    @Bean
    public WebClient employeeWebClient() {

        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 2000)
                .responseTimeout(Duration.ofMillis(2000))
                .doOnConnected(conn -> conn.addHandlerLast(new ReadTimeoutHandler(2000, TimeUnit.MILLISECONDS))
                        .addHandlerLast(new WriteTimeoutHandler(2000, TimeUnit.MILLISECONDS)));

        return WebClient.builder()
                .baseUrl(employeeServiceUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }

    @Bean
    @Primary
    @Qualifier("fileUploadService")
    public WebClient fileUploadServiceWebClient() {

        ExchangeStrategies exchangeStrategies = ExchangeStrategies.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(maxInMemorySize))
                .build();

        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 2000)
                .responseTimeout(Duration.ofMillis(2000))
                .doOnConnected(conn -> conn.addHandlerLast(new ReadTimeoutHandler(2000, TimeUnit.MILLISECONDS))
                        .addHandlerLast(new WriteTimeoutHandler(2000, TimeUnit.MILLISECONDS)));

        return WebClient.builder()
                .baseUrl(fileUploadServiceUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .exchangeStrategies(exchangeStrategies)
                .build();
    }
}

