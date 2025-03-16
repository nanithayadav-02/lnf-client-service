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

import com.lnf.client.model.enums.DocumentType;
import com.lnf.client.service.DocumentService;
import com.lnf.dto.client.DocumentDto;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;


@RestController
@RequiredArgsConstructor
@RequestMapping("/lnf")
public class AgreementController {

    private final DocumentService service;
    private static final String CLIENT_SERVICE = "clientService";

    /**
     * Returns DocumentDto for the client agreement
     *
     * @param clientId Client Id
     * @return DocumentDto of the Agreement
     * @throws IOException IOException
     */
    @GetMapping(value = "/clients/{clientId}/agreement")
    @ResponseStatus(HttpStatus.OK)
    public DocumentDto findByClientId(@PathVariable final UUID clientId) throws IOException {
        return service.findByClientId(clientId, DocumentType.agreement.getLabel());
    }

    /**
     * Returns ResponseEntity with byte[] for the client agreement
     *
     * @param clientId Client Id
     * @return ResponseEntity<byte [ ]>
     */
    @CircuitBreaker(name = CLIENT_SERVICE)
    @GetMapping(value = "/clients/{clientId}/agreement/download")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<byte[]> findClientAgreement(@PathVariable final UUID clientId, @RequestParam String fileName) {
        return service.findClientAgreement(clientId, DocumentType.agreement, fileName);
    }

    /**
     * Returns ResponseEntity with byte[] of the Client Agreement
     *
     * @param clientId Client Id
     * @return ResponseEntity<byte [ ]>
     */
    @GetMapping(value = "/clients/{clientId}/agreement/{fileName}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<byte[]> findById(@PathVariable final UUID clientId,
                                           @PathVariable String fileName) {
        return service.findById(clientId, DocumentType.agreement, fileName);
    }

    /**
     * Creates the agreement with the client
     *
     * @param clientId  Client Id
     * @param agreement Agreement in MutipartFile format
     */
    @PostMapping(value = "/clients/{clientId}/agreement")
    @ResponseStatus(HttpStatus.CREATED)
    public void create(@PathVariable final UUID clientId, @RequestParam MultipartFile agreement) {
        service.create(clientId, DocumentType.agreement, agreement);
    }

    /**
     * Updates the agreement with the client
     *
     * @param clientId  Client id
     * @param agreement Agreement in MutipartFile format
     * @throws IOException IOException
     */
    @PutMapping(value = "/clients/{clientId}/agreement")
    @ResponseStatus(HttpStatus.OK)
    public void update(@PathVariable final UUID clientId, @RequestParam MultipartFile agreement) throws IOException {
        service.update(clientId, DocumentType.agreement, agreement);
    }

    /**
     * Delete the client agreement
     *
     * @param clientId Client Id
     */
    @DeleteMapping(value = "/clients/{clientId}/agreement/{fileName}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable final UUID clientId, @PathVariable String fileName) {
        service.deleteByClientId(clientId, DocumentType.agreement, fileName);
    }

}
