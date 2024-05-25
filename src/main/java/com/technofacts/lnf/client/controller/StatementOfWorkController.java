package com.technofacts.lnf.client.controller;

import com.technofacts.lnf.client.service.StatementOfWorkService;
import com.technofacts.lnf.dto.client.StatementOfWorkDto;
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
public class StatementOfWorkController {

    private final StatementOfWorkService service;

    @GetMapping("/clients/{clientId}/project/{projectId}/sow")
    @ResponseStatus(HttpStatus.OK)
    public List<StatementOfWorkDto> findByProjectIdAndClientId(@PathVariable("clientId") UUID clientId,
                                                               @PathVariable("projectId") UUID projectId) {

        return service.findByProjectIdAndClientId(clientId, projectId);
    }

    @GetMapping("/clients/{clientId}/project/{projectId}/sow/{fileName}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<byte[]> findById(@PathVariable("clientId") UUID clientId, @PathVariable("projectId") UUID projectId,
                                           @PathVariable("fileName") String fileName) {
        return service.findById(clientId, projectId, fileName);
    }

    @PostMapping(value = "/clients/{clientId}/project/{projectId}/sow", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public void create(@PathVariable UUID clientId, @PathVariable UUID projectId, @RequestPart("files") MultipartFile[] files,
                       @RequestPart("resource") List<StatementOfWorkDto> resource) {
        service.create(clientId, projectId, files, resource);
    }

    @PutMapping(value = "/clients/{clientId}/project/{projectId}/sow", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public void update(@PathVariable("clientId") UUID clientId, @PathVariable("projectId") UUID projectId, @RequestParam("fileName") String fileName,
                       @RequestPart("file") MultipartFile file, @RequestPart("resource") StatementOfWorkDto resource) {
        service.update(clientId, projectId, fileName, file, resource);
    }

    @DeleteMapping(value = "/clients/{clientId}/project/{projectId}/sows")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteByIdAndFileName(@PathVariable("clientId") UUID clientId, @PathVariable("projectId") UUID projectId,
                                      @RequestParam("fileName") String fileName) {
        service.deleteByIdAndFileName(clientId, projectId, fileName);
    }

}
