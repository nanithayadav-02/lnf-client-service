package com.technofacts.lnf.client.controller;

import java.io.IOException;
import java.util.UUID;

import com.technofacts.lnf.dto.client.DocumentDto;
import com.technofacts.lnf.client.model.enums.DocumentType;
import com.technofacts.lnf.client.service.DocumentService;
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

    @GetMapping(value = "/clients/{clientId}/agreement")
    @ResponseStatus(HttpStatus.CREATED)
    public DocumentDto findByClientId(@PathVariable("clientId") final UUID clientId) throws IOException {
        return service.findByClientId(clientId, DocumentType.agreement);
    }

    @GetMapping(value = "/clients/{clientId}/agreement/{agreementId}")
    public ResponseEntity<byte[]> findById(@PathVariable("clientId") final UUID clientId, @PathVariable("agreementId") final UUID agreementId) {
        return service.findById(clientId, agreementId);
    }

    @PostMapping(value = "/clients/{clientId}/agreement")
    @ResponseStatus(HttpStatus.CREATED)
    public void create(@PathVariable("clientId") final UUID clientId, @RequestParam MultipartFile agreement) {
        service.create(clientId, DocumentType.agreement, agreement);
    }

    @PutMapping(value = "/clients/{clientId}/agreement/{agreementId}")
    @ResponseStatus(HttpStatus.OK)
    public void update(@PathVariable("clientId") final UUID clientId, @PathVariable("agreementId") final UUID agreementId,
                       @RequestParam MultipartFile agreement) throws IOException {
        service.update(clientId, agreementId, agreement);
    }

    @DeleteMapping(value = "/clients/{clientId}/agreement")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("clientId") final UUID clientId) {
        service.deleteByClientId(clientId, DocumentType.agreement);
    }

    @DeleteMapping(value = "/clients/{clientId}/agreement/{agreementId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("clientId") final UUID clientId, @PathVariable("agreementId") final UUID agreementId) {
        service.deleteById(clientId, agreementId);
    }
}
