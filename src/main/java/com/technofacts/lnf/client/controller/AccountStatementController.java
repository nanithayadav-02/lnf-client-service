package com.technofacts.lnf.client.controller;

import com.technofacts.lnf.client.service.AccountStatementService;
import com.technofacts.lnf.dto.client.AccountStatementDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/lnf")
public class AccountStatementController {

    private final AccountStatementService service;

    @GetMapping(value = "/clients/accountStatement/dateRange")
    @ResponseStatus(HttpStatus.OK)
    public List<AccountStatementDto> findAccountStatement(@RequestParam LocalDate startDate,
                                                          @RequestParam LocalDate endDate) {
        return service.findAccountStatement(startDate, endDate);
    }
}
