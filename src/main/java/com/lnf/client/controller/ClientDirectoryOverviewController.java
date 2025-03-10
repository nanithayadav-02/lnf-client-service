/*
 *
 *  * Copyright © 2024 Lever And Fulcrum Solutions (hereinafter referred to as "LNF").
 *  * All rights reserved.
 *  *
 *  * This source code is the proprietary property of LNF
 *  *
 *  * Unauthorized copying, redistribution, or modification of this code,
 *  * via any medium, is strictly prohibited unless expressly authorized
 *  * in writing by LNF.
 *  *
 *  * This code is confidential and intended solely for the use of LNF
 *  * and its authorized personnel.
 *
 */
package com.lnf.client.controller;

import com.lnf.client.service.ClientDirectoryService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/lnf")
public class ClientDirectoryOverviewController {

    private final ClientDirectoryService service;
    private static final String CLIENT_SERVICE = "clientService";

    @CircuitBreaker(name = CLIENT_SERVICE, fallbackMethod = "clientIdDirectoryExcelFallback")
    @Retry(name = CLIENT_SERVICE)
    @GetMapping("/clients/{clientId}/directory/excel")
    public ResponseEntity<byte[]> clientDirectoryExcel(@PathVariable UUID clientId) {
        byte[] excelBytes = service.clientDirectoryExcel(clientId);
        HttpHeaders headers = generateHeadersForFile("client-directory" + "-" + ".xlsx");
        return new ResponseEntity<>(excelBytes, headers, HttpStatus.OK);
    }

    @CircuitBreaker(name = CLIENT_SERVICE, fallbackMethod = "clientDirectoryExcelFallback")
    @Retry(name = CLIENT_SERVICE)
    @GetMapping("/client/directory/excel")
    public ResponseEntity<byte[]> clientDirectoryExcel() {
        byte[] excelBytes = service.clientDirectoryExcel();
        HttpHeaders headers = generateHeadersForFile("client-directory" + "-" + ".xlsx");
        return new ResponseEntity<>(excelBytes, headers, HttpStatus.OK);
    }

    private HttpHeaders generateHeadersForFile(String fileName) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", fileName);
        return headers;
    }

    @CircuitBreaker(name = CLIENT_SERVICE, fallbackMethod = "downloadClientDirectoryAsPdfFallback")
    @Retry(name = CLIENT_SERVICE)
    @GetMapping("/client/directory/pdf")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<byte[]> downloadClientDirectoryAsPdf() {

        byte[] pdfBytes = service.downloadClientDirectoryAsPdf();

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=clientDirectory.pdf");
        headers.setContentType(MediaType.APPLICATION_PDF);

        return ResponseEntity
                .ok()
                .headers(headers)
                .body(pdfBytes);
    }

    @CircuitBreaker(name = CLIENT_SERVICE, fallbackMethod = "downloadClientIdDirectoryAsPdfFallback")
    @Retry(name = CLIENT_SERVICE)
    @GetMapping("/clients/{clientId}/directory/pdf")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<byte[]> downloadClientDirectoryAsPdf(@PathVariable UUID clientId) {

        byte[] pdfBytes = service.downloadClientDirectoryAsPdf(clientId);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=clientDirectory.pdf");
        headers.setContentType(MediaType.APPLICATION_PDF);

        return ResponseEntity
                .ok()
                .headers(headers)
                .body(pdfBytes);
    }

    public ResponseEntity<byte[]> clientIdDirectoryExcelFallback(UUID clientId, Exception ex) {
        return fallbackResponse("client-directory-fallback.xlsx", MediaType.APPLICATION_OCTET_STREAM);
    }

    public ResponseEntity<byte[]> clientDirectoryExcelFallback(Exception ex) {
        return fallbackResponse("client-directory-fallback.xlsx", MediaType.APPLICATION_OCTET_STREAM);
    }

    public ResponseEntity<byte[]> downloadClientIdDirectoryAsPdfFallback(UUID clientId, Exception ex) {
        return fallbackResponse("clientDirectory-fallback.pdf", MediaType.APPLICATION_PDF);
    }

    public ResponseEntity<byte[]> downloadClientDirectoryAsPdfFallback(Exception ex) {
        return fallbackResponse("clientDirectory-fallback.pdf", MediaType.APPLICATION_PDF);
    }


    private ResponseEntity<byte[]> fallbackResponse(String fileName, MediaType mediaType) {
        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName)
                .contentType(mediaType)
                .body("Service is temporarily unavailable. Please try again later.".getBytes());
    }

}
