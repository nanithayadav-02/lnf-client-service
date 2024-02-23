package com.technofacts.lnf.client.controller;

import com.technofacts.lnf.client.service.ClientExpenseService;
import com.technofacts.lnf.dto.client.ClientExpenseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/lnf")
public class ClientExpenseController {

    private final ClientExpenseService service;

    @GetMapping(value = "/clients/{clientId}/expense")
    @ResponseStatus(HttpStatus.OK)
    public List<ClientExpenseDto> findByClientId(@PathVariable UUID clientId) {
        return service.findByClientId(clientId);
    }

    @GetMapping(value = "/clients/{clientId}/expense/{months}")
    @ResponseStatus(HttpStatus.OK)
    public List<ClientExpenseDto> findForDesiredMonths(@PathVariable UUID clientId,
                                                                @PathVariable int months) {
        return service.findForDesiredMonths(clientId, months);
    }

    @GetMapping(value = "/clients/{clientId}/expense/dateRange")
    @ResponseStatus(HttpStatus.OK)
    public List<ClientExpenseDto> findByClientIdAndDateRange(@PathVariable UUID clientId,
                                                                @RequestParam LocalDate startDate,
                                                                @RequestParam LocalDate endDate) {
        return service.findByClientIdAndDateRange(clientId, startDate, endDate);
    }

    @GetMapping(value = "/clients/expense")
    @ResponseStatus(HttpStatus.OK)
    public List<ClientExpenseDto> findByDateRange(@RequestParam LocalDate startDate,
                                                  @RequestParam LocalDate endDate) {
        return service.findByDateRange(startDate, endDate);
    }
}
