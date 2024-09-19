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

    /**
     * Returns DocumentDto for the client image
     *
     * @param clientId Client Id
     * @return DocumentDto of the Image
     * @throws IOException IOException
     */
    @GetMapping(value = "/clients/{clientId}/image")
    @ResponseStatus(HttpStatus.OK)
    public DocumentDto findByClientId(@PathVariable("clientId") final UUID clientId) throws IOException {
        return service.findByClientId(clientId, DocumentType.image.getLabel());
    }

    /**
     * Returns ResponseEntity with byte[] for the client agreement
     *
     * @param clientId Client Id
     * @return ResponseEntity<byte []>
     */
    @GetMapping(value = "/clients/{clientId}/image/download")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<byte[]> findClientAgreement(@PathVariable("clientId") final UUID clientId, @RequestParam("fileName") String fileName) {
        return service.findClientAgreement(clientId, DocumentType.image, fileName);
    }

    /**
     * Returns ResponseEntity with byte[] of the Client Agreement
     *
     * @param clientId Client Id
     * @param imageId  Image Id
     * @return ResponseEntity<byte [ ]>
     */
    @GetMapping(value = "/clients/{clientId}/image/{fileName}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<byte[]> findById(@PathVariable("clientId") final UUID clientId, @RequestParam(value = "imageId",required = false) final UUID imageId,
                                           @PathVariable("fileName") String fileName) {

        return service.findById(clientId, imageId ,DocumentType.image, fileName);
    }

    /**
     * Creates the image for the client
     *
     * @param clientId Client Id
     * @param image    Client image in MultipartFile format
     */
    @PostMapping(value = "/clients/{clientId}/image")
    @ResponseStatus(HttpStatus.CREATED)
    public void create(@PathVariable("clientId") final UUID clientId, @RequestParam MultipartFile image) {
        service.create(clientId, DocumentType.image, image);
    }

    /**
     * Updates the agreement with the client
     *
     * @param clientId Client id
     * @param imageId  Image Id
     * @param image    image in MutipartFile format
     * @throws IOException IOException
     */
    @PutMapping(value = "/clients/{clientId}/image")
    @ResponseStatus(HttpStatus.OK)
    public void update(@PathVariable("clientId") final UUID clientId, @RequestParam(value = "imageId", required = false) final UUID imageId,
                       @RequestParam MultipartFile image) throws IOException {
        service.update(clientId, imageId, DocumentType.image, image);
    }

    /**
     * Delete the client image
     *
     * @param clientId Client Id
     */
    @DeleteMapping(value = "/clients/{clientId}/image")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("clientId") final UUID clientId, @RequestParam("fileName") String fileName) {
        service.deleteByClientId(clientId, DocumentType.image, fileName);
    }

    /**
     * Delete the client image by image id
     *
     * @param clientId Client Id
     * @param imageId  Image Id
     */
    @DeleteMapping(value = "/clients/{clientId}/image/{imageId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("clientId") final UUID clientId, @PathVariable("imageId") final UUID imageId) {
        service.deleteById(clientId, imageId);
    }
}
