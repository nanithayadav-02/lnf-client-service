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

import com.lnf.client.service.ClientAgreementService;
import com.lnf.dto.client.AgreementDto;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/lnf")
public class ClientAgreementController {

    private final ClientAgreementService service;
    private static final String CLIENT_SERVICE = "clientService";

    @CircuitBreaker(name = CLIENT_SERVICE)
    @GetMapping("/clients/{clientId}/agreements")
    @ResponseStatus(HttpStatus.OK)
    public List<AgreementDto> findByProjectIdAndClientId(@PathVariable UUID clientId) {
        return service.findByClientId(clientId);
    }

    @CircuitBreaker(name = CLIENT_SERVICE)
    @GetMapping("/clients/{clientId}/agreements/{fileName}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<byte[]> findById(@PathVariable UUID clientId, @PathVariable String fileName) {
        return service.findById(clientId, fileName);
    }

    @CircuitBreaker(name = CLIENT_SERVICE)
    @PostMapping(value = "/clients/{clientId}/agreements", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<String> create(@PathVariable UUID clientId, @RequestPart("files") MultipartFile[] files,
                                         @RequestPart("resource") List<AgreementDto> resource) {
        service.create(clientId, files, resource);
        return ResponseEntity.ok("file uploaded successfully");
    }

    @CircuitBreaker(name = CLIENT_SERVICE)
    @PutMapping(value = "/clients/{clientId}/agreements", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> update(@PathVariable UUID clientId, @RequestParam String fileName,
                                         @RequestPart("file") MultipartFile file, @RequestPart("resource") AgreementDto resource) {
        service.update(clientId, fileName, file, resource);
        return ResponseEntity.ok("file uploaded successfully");
    }

    @DeleteMapping(value = "/clients/{clientId}/agreements")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteByIdAndFileName(@PathVariable UUID clientId, @RequestParam String fileName) {
        service.deleteByIdAndFileName(clientId, fileName);
    }

}
