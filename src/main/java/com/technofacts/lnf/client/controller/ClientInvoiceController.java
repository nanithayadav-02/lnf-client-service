package com.technofacts.lnf.client.controller;

import com.technofacts.lnf.client.service.ClientInvoiceService;
import com.technofacts.lnf.dto.client.ClientInvoiceDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/lnf")
public class ClientInvoiceController {

    private final ClientInvoiceService service;

    @GetMapping(value = "/clients/{clientId}/invoices")
    @ResponseStatus(HttpStatus.OK)
    public List<ClientInvoiceDto> getInvoicesByClientId(@PathVariable("clientId") final UUID clientId,
                                                        @RequestParam(required = false) LocalDate startDate ,
                                                        @RequestParam(required = false) LocalDate endDate) {
        return service.getInvoicesByClientId(clientId,startDate,endDate);
    }
}
