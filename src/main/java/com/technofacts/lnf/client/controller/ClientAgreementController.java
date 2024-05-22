package com.technofacts.lnf.client.controller;

import com.technofacts.lnf.client.service.ClientAgreementService;
import com.technofacts.lnf.dto.client.AgreementDto;
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

    @GetMapping("/clients/{clientId}/agreements")
    @ResponseStatus(HttpStatus.OK)
    public List<AgreementDto> findByProjectIdAndClientId(@PathVariable("clientId") UUID clientId) {
        return service.findByClientId(clientId);
    }

    @GetMapping("/clients/{clientId}/agreements/{fileName}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<byte[]> findById(@PathVariable("clientId") UUID clientId, @PathVariable("fileName") String fileName) {
        return service.findById(clientId, fileName);
    }

    @PostMapping(value = "/clients/{clientId}/agreements", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public void create(@PathVariable UUID clientId, @RequestPart("files") MultipartFile[] files,
                       @RequestPart("resource") List<AgreementDto> resource) {
        service.create(clientId, files, resource);
    }

    @PutMapping(value = "/clients/{clientId}/agreements", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public void update(@PathVariable("clientId") UUID clientId, @RequestParam("fileName") String fileName,
                       @RequestPart("file") MultipartFile file, @RequestPart("resource") AgreementDto resource) {
        service.update(clientId, fileName, file, resource);
    }

    @DeleteMapping(value = "/clients/{clientId}/agreements")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteByIdAndFileName(@PathVariable("clientId") UUID clientId, @RequestParam("fileName") String fileName) {
        service.deleteByIdAndFileName(clientId, fileName);
    }

}
