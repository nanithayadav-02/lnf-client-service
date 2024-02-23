package com.technofacts.lnf.client.controller;

import com.technofacts.lnf.client.service.AccountHistoryService;
import com.technofacts.lnf.dto.client.AccountHistoryDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/lnf")
public class AccountHistoryController {
    private final AccountHistoryService service;

    @GetMapping(value = "/clients/{clientId}/accountHistory")
    @ResponseStatus(HttpStatus.OK)
    public List<AccountHistoryDto> getAccountHistoryByClientId(@PathVariable("clientId") final UUID clientId,
                                                               @RequestParam(required = false) LocalDate startDate ,
                                                               @RequestParam(required = false) LocalDate endDate) {
        return service.getAccountHistoryByClientId(clientId,startDate,endDate);
    }
}
