/*
 * Copyright (c)  Lever And Fulcrum (LNF)
 */
package com.technofacts.lnf.client.controller;

import com.technofacts.lnf.client.service.ClientDirectoryService;
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

    @GetMapping("/clients/{clientId}/directory/excel")
    public ResponseEntity<byte[]> clientDirectoryExcel(@PathVariable("clientId") UUID clientId)  {
        byte[] excelBytes = service.clientDirectoryExcel(clientId);
        HttpHeaders headers = generateHeadersForFile("client-directory" + "-" + ".xlsx");
        return new ResponseEntity<>(excelBytes, headers, HttpStatus.OK);
    }

    @GetMapping("/client/directory/excel")
    public ResponseEntity<byte[]> clientDirectoryExcel()  {
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

    @GetMapping("/clients/{clientId}/directory/pdf")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<byte[]> downloadClientDirectoryAsPdf(@PathVariable("clientId") UUID clientId) {

        byte[] pdfBytes = service.downloadClientDirectoryAsPdf(clientId);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=clientDirectory.pdf");
        headers.setContentType(MediaType.APPLICATION_PDF);

        return ResponseEntity
                .ok()
                .headers(headers)
                .body(pdfBytes);
    }

}
