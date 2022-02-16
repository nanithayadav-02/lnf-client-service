package com.technofacts.lnf.client.config;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.tcp.TcpClient;

@Configuration
public class WebClientConfiguration {

    @Value("${employee.service.url}")
    private String employeeServiceUrl;

    public static final int TIMEOUT = 1000;

    @Qualifier("EmployeeService")
    @Bean
    public WebClient employeeWebClient() {

        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 1000)
                .responseTimeout(Duration.ofMillis(1000))
                .doOnConnected(conn -> conn.addHandlerLast(new ReadTimeoutHandler(1000, TimeUnit.MILLISECONDS))
                        .addHandlerLast(new WriteTimeoutHandler(1000, TimeUnit.MILLISECONDS)));

        return WebClient.builder()
                .baseUrl(employeeServiceUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }

}
