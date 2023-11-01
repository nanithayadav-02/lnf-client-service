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

    @Value("${file.service.url}")
    private String fileServiceUrl;

    @Value("${application.maxInMemorySize}")
    private int maxInMemorySize;

    @Value("${connection.timeout}")
    private int timeOut;

    @Qualifier("EmployeeService")
    @Bean
    public WebClient employeeWebClient() {

        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, timeOut)
                .responseTimeout(Duration.ofMillis(timeOut))
                .doOnConnected(conn -> conn.addHandlerLast(new ReadTimeoutHandler(timeOut, TimeUnit.MILLISECONDS))
                        .addHandlerLast(new WriteTimeoutHandler(timeOut, TimeUnit.MILLISECONDS)));

        return WebClient.builder()
                .baseUrl(employeeServiceUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }

    @Bean
    @Primary
    @Qualifier("FileService")
    public WebClient fileServiceWebClient() {

        ExchangeStrategies exchangeStrategies = ExchangeStrategies.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(maxInMemorySize))
                .build();

        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, timeOut)
                .responseTimeout(Duration.ofMillis(timeOut))
                .doOnConnected(conn -> conn.addHandlerLast(new ReadTimeoutHandler(timeOut, TimeUnit.MILLISECONDS))
                        .addHandlerLast(new WriteTimeoutHandler(timeOut, TimeUnit.MILLISECONDS)));

        return WebClient.builder()
                .baseUrl(fileServiceUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .exchangeStrategies(exchangeStrategies)
                .build();
    }
}

