package com.technofacts.lnf.client.controller;

import java.io.IOException;
import java.util.UUID;

import com.technofacts.lnf.client.model.enums.DocumentType;
import com.technofacts.lnf.client.service.DocumentService;
import com.technofacts.lnf.dto.client.DocumentDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


@RestController
@RequiredArgsConstructor
@RequestMapping("/lnf")
public class AgreementController {

    private final DocumentService service;

    /**
     * Returns DocumentDto for the client agreement
     *
     * @param clientId Client Id
     * @return DocumentDto of the Agreement
     * @throws IOException IOException
     */
    @GetMapping(value = "/clients/{clientId}/agreement")
    @ResponseStatus(HttpStatus.OK)
    public DocumentDto findByClientId(@PathVariable("clientId") final UUID clientId) throws IOException {
        return service.findByClientId(clientId, DocumentType.agreement);
    }

    /**
     * Returns ResponseEntity with byte[] of the Client Agreement
     *
     * @param clientId    Client Id
     * @param agreementId Agreement Id
     * @return ResponseEntity<byte [ ]>
     */
    @GetMapping(value = "/clients/{clientId}/agreement/{agreementId}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<byte[]> findById(@PathVariable("clientId") final UUID clientId, @PathVariable("agreementId") final UUID agreementId) {
        return service.findById(clientId, agreementId);
    }

    /**
     * Creates the agreement with the client
     *
     * @param clientId  Client Id
     * @param agreement Agreement in MutipartFile format
     */
    @PostMapping(value = "/clients/{clientId}/agreement")
    @ResponseStatus(HttpStatus.CREATED)
    public void create(@PathVariable("clientId") final UUID clientId, @RequestParam MultipartFile agreement) {
        service.create(clientId, DocumentType.agreement, agreement);
    }

    /**
     * Updates the agreement with the client
     *
     * @param clientId    Client id
     * @param agreementId Agreement Id
     * @param agreement   Agreement in MutipartFile format
     * @throws IOException IOException
     */
    @PutMapping(value = "/clients/{clientId}/agreement/{agreementId}")
    @ResponseStatus(HttpStatus.OK)
    public void update(@PathVariable("clientId") final UUID clientId, @PathVariable("agreementId") final UUID agreementId,
                       @RequestParam MultipartFile agreement) throws IOException {
        service.update(clientId, agreementId, agreement);
    }

    /**
     * Delete the client agreement
     *
     * @param clientId Client Id
     */
    @DeleteMapping(value = "/clients/{clientId}/agreement")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("clientId") final UUID clientId) {
        service.deleteByClientId(clientId, DocumentType.agreement);
    }

    /**
     * Delete the client agreement by agreement id
     *
     * @param clientId    Client Id
     * @param agreementId Agreement Id
     */
    @DeleteMapping(value = "/clients/{clientId}/agreement/{agreementId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("clientId") final UUID clientId, @PathVariable("agreementId") final UUID agreementId) {
        service.deleteById(clientId, agreementId);
    }
}
