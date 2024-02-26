package com.technofacts.lnf.client.controller;

import com.technofacts.lnf.client.service.AccountStatementService;
import com.technofacts.lnf.dto.client.AccountStatementDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/lnf")
public class AccountStatementController {

    private final AccountStatementService service;

    @GetMapping(value = "/clients/{clientId}/account-statement")
    @ResponseStatus(HttpStatus.OK)
    public List<AccountStatementDto> findByClientId(@PathVariable UUID clientId) {
        return service.findByClientId(clientId);
    }

    @GetMapping(value = "/clients/{clientId}/account-statement/{months}")
    @ResponseStatus(HttpStatus.OK)
    public List<AccountStatementDto> findStatementForDesiredMonths(@PathVariable UUID clientId,
                                                                   @PathVariable int months) {
        return service.findStatementForDesiredMonths(clientId, months);
    }

    @GetMapping(value = "/clients/{clientId}/account-statement/dateRange")
    @ResponseStatus(HttpStatus.OK)
    public List<AccountStatementDto> findByClientIdAndDateRange(@PathVariable UUID clientId,
                                                                @RequestParam LocalDate startDate,
                                                                @RequestParam LocalDate endDate) {
        return service.findByClientIdAndDateRange(clientId, startDate, endDate);
    }

    @GetMapping(value = "/clients/account-statement")
    @ResponseStatus(HttpStatus.OK)
    public List<AccountStatementDto> findByDateRange(@RequestParam LocalDate startDate,
                                                     @RequestParam LocalDate endDate) {
        return service.findByDateRange(startDate, endDate);
    }

    @GetMapping("/clients/account-statement/pdf")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<byte[]> downloadPaymentsAsPdf(@RequestParam LocalDate startDate,
                                                        @RequestParam LocalDate endDate) {
        byte[] pdfBytes = service.generateStatementPdf(startDate, endDate);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=AccountStatement.pdf");
        headers.setContentType(MediaType.APPLICATION_PDF);

        return ResponseEntity
                .ok()
                .headers(headers)
                .body(pdfBytes);
    }

    @PostMapping("/clients/account-statement/email")
    public void emailAccountStatement(@RequestParam LocalDate startDate,
                                      @RequestParam LocalDate endDate,
                                      @RequestParam   String email) {
        service.sendEmailWithPdfAttachment(startDate, endDate, email);
    }
}


