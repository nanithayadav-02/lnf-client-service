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
public class ImageController {

    private final DocumentService service;
    private static final String CLIENT_SERVICE = "clientService";

    /**
     * Returns DocumentDto for the client image
     *
     * @param clientId Client Id
     * @return DocumentDto of the Image
     * @throws IOException IOException
     */
    @CircuitBreaker(name = CLIENT_SERVICE)
    @GetMapping(value = "/clients/{clientId}/image")
    @ResponseStatus(HttpStatus.OK)
    public DocumentDto findByClientId(@PathVariable final UUID clientId) throws IOException {
        return service.findByClientId(clientId, DocumentType.image.getLabel());
    }

    /**
     * Returns ResponseEntity with byte[] for the client agreement
     *
     * @param clientId Client Id
     * @return ResponseEntity<byte [ ]>
     */
    @CircuitBreaker(name = CLIENT_SERVICE)
    @GetMapping(value = "/clients/{clientId}/image/download")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<byte[]> findClientAgreement(@PathVariable final UUID clientId, @RequestParam String fileName) {
        return service.findClientAgreement(clientId, DocumentType.image, fileName);
    }

    /**
     * Returns ResponseEntity with byte[] of the Client Agreement
     *
     * @param clientId Client Id
     * @return ResponseEntity<byte [ ]>
     */
    @CircuitBreaker(name = CLIENT_SERVICE)
    @GetMapping(value = "/clients/{clientId}/image/{fileName}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<byte[]> findById(@PathVariable final UUID clientId,
                                           @PathVariable String fileName) {

        return service.findById(clientId, DocumentType.image, fileName);
    }

    /**
     * Creates the image for the client
     *
     * @param clientId Client Id
     * @param image    Client image in MultipartFile format
     */
    @CircuitBreaker(name = CLIENT_SERVICE)
    @PostMapping(value = "/clients/{clientId}/image")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<String> create(@PathVariable final UUID clientId, @RequestParam MultipartFile image) {
        service.create(clientId, DocumentType.image, image);
        return ResponseEntity.ok("file uploaded successfully");
    }

    /**
     * Updates the agreement with the client
     *
     * @param clientId Client id
     * @param image    image in MutipartFile format
     * @throws IOException IOException
     */
    @PutMapping(value = "/clients/{clientId}/image")
    @ResponseStatus(HttpStatus.OK)
    public void update(@PathVariable final UUID clientId,
                       @RequestParam MultipartFile image) throws IOException {
        service.update(clientId, DocumentType.image, image);
    }

    /**
     * Delete the client image
     *
     * @param clientId Client Id
     */
    @CircuitBreaker(name = CLIENT_SERVICE)
    @DeleteMapping(value = "/clients/{clientId}/image")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<String> delete(@PathVariable final UUID clientId, @RequestParam String fileName) {
        service.deleteByClientId(clientId, DocumentType.image, fileName);
        return ResponseEntity.ok("file uploaded successfully");
    }

}
