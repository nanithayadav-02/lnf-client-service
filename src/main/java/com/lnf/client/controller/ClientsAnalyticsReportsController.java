package com.lnf.client.controller;

import com.lnf.client.service.ClientsAnalyticsReportsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/lnf")
public class ClientsAnalyticsReportsController {

    private final ClientsAnalyticsReportsService service;

    @GetMapping("client/analytics")
    public Map<String, Object> clientAnalytics(@RequestParam(value = "year") int year) {
        return service.clientAnalyticsReports(year);
    }
}
