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

@RestController
@RequiredArgsConstructor
@RequestMapping("/lnf")
public class AccountStatementController {

    private final AccountStatementService service;

    @GetMapping(value = "/clients/account-statement")
    @ResponseStatus(HttpStatus.OK)
    public List<AccountStatementDto> findAccountStatement(@RequestParam LocalDate startDate,
                                                          @RequestParam LocalDate endDate) {
        return service.findAccountStatement(startDate, endDate);
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
                                      String email) {
        service.sendEmailWithPdfAttachment(startDate, endDate, email);
    }
}
