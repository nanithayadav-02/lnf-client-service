package com.technofacts.lnf.client.controller;

import java.io.IOException;
import java.util.UUID;

import com.technofacts.lnf.client.dto.DocumentDto;
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
public class ImageController {

    private final DocumentService service;

    @GetMapping(value = "/clients/{clientId}/image")
    @ResponseStatus(HttpStatus.CREATED)
    public DocumentDto findByEmployeeId(@PathVariable("clientId") final UUID clientId) throws IOException {
        return service.findByClientId(clientId, DocumentType.image);
    }

    @GetMapping(value = "/clients/{clientId}/image/{imageId}")
    public ResponseEntity<byte[]> findById(@PathVariable("clientId") final UUID clientId, @PathVariable("imageId") final UUID imageId) {
        return service.findById(clientId, imageId);
    }

    @PostMapping(value = "/clients/{clientId}/image")
    @ResponseStatus(HttpStatus.CREATED)
    public void create(@PathVariable("clientId") final UUID clientId, @RequestParam MultipartFile image) {
        service.create(clientId, DocumentType.image, image);
    }

    @PutMapping(value = "/clients/{clientId}/image/{imageId}")
    @ResponseStatus(HttpStatus.OK)
    public void update(@PathVariable("clientId") final UUID clientId, @PathVariable("imageId") final UUID imageId,
                       @RequestParam MultipartFile image) throws IOException {
        service.update(clientId, imageId, image);
    }

    @DeleteMapping(value = "/clients/{clientId}/image")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("clientId") final UUID clientId) {
        service.deleteByClientId(clientId, DocumentType.image);
    }

    @DeleteMapping(value = "/clients/{clientId}/image/{imageId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("clientId") final UUID clientId, @PathVariable("imageId") final UUID imageId) {
        service.deleteById(clientId, imageId);
    }
}
