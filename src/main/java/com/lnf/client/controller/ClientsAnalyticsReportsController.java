package com.lnf.client.controller;

import com.lnf.client.service.ClientsAnalyticsReportsService;
import com.lnf.dto.common.DateRangeDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/lnf")
public class ClientsAnalyticsReportsController {

    private final ClientsAnalyticsReportsService service;

    @GetMapping("/client/analytics")
    @ResponseStatus(HttpStatus.OK)
    public Map<String, Object> clientAnalytics(@RequestParam Integer year) {
        return service.clientAnalyticsReports(year);
    }

    @GetMapping(value = "/client/date-ranges")
    @ResponseStatus(HttpStatus.OK)
    public DateRangeDto findApplicantClientDates() {
        return service.getClientDateRanges();
    }

}
