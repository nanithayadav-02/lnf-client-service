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

import com.lnf.client.service.ClientNotesService;
import com.lnf.dto.client.ClientNotesDto;
import com.lnf.dto.common.PageRequestDto;
import com.lnf.service.common.page.PageableAsQueryParam;
import com.lnf.service.common.page.PaginationAndSortingHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/lnf")
public class ClientNotesController {

    private final ClientNotesService service;
    private final PaginationAndSortingHandler paginationAndSortingHandler;

    @GetMapping(value = "/clients/notes")
    public ResponseEntity<?> findAll(@RequestParam(value = "search", required = false) String search,
                                     @PageableAsQueryParam PageRequestDto pageRequest) {
        if (search != null && !search.isEmpty()) {
            if (pageRequest != null && pageRequest.getPage() != null) {
                return ResponseEntity.ok(service.findingAllWithPagination(search, pageRequest));
            } else {
                return ResponseEntity.ok(service.findAll(search));
            }
        } else {
            return paginationAndSortingHandler.handleFindAllRequest(pageRequest, service);
        }
    }

    @GetMapping(value = "/clients/{clientId}/notes")
    public List<ClientNotesDto> findByClientId(@PathVariable("clientId") final UUID clientId) {
        return service.findByClientId(clientId);
    }

    @GetMapping(value = "/clients/{clientId}/notes/{notesId}")
    public ClientNotesDto findById(@PathVariable("clientId") final UUID clientId, @PathVariable("notesId") final UUID notesId) {
        return service.findById(clientId, notesId);
    }

    @PostMapping(value = "/clients/{clientId}/notes")
    @ResponseStatus(HttpStatus.CREATED)
    public void create(@PathVariable("clientId") final UUID clientId, @RequestBody final List<ClientNotesDto> resource) {
        service.create(clientId, resource);
    }

    @PutMapping(value = "/clients/{clientId}/notes/{notesId}")
    @ResponseStatus(HttpStatus.OK)
    public void update(@PathVariable("clientId") final UUID clientId, @PathVariable("notesId") final UUID notesId,
                       @RequestBody final ClientNotesDto resource) {
        service.update(clientId, notesId, resource);
    }

    @DeleteMapping(value = "/clients/{clientId}/notes")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("clientId") final UUID clientId) {
        service.deleteByClientId(clientId);
    }

    @DeleteMapping(value = "/clients/{clientId}/notes/{notesId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("clientId") final UUID clientId, @PathVariable("notesId") final UUID notesId) {
        service.deleteById(clientId, notesId);
    }

}
