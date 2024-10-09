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

import com.lnf.client.service.StatementOfWorkService;
import com.lnf.dto.client.StatementOfWorkDto;
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
