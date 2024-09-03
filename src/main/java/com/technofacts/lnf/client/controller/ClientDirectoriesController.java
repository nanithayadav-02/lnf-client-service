/*
 * Copyright (c)  Lever And Fulcrum (LNF)
 */
package com.technofacts.lnf.client.controller;

import com.technofacts.lnf.client.service.ClientDirectoryService;
import com.technofacts.lnf.dto.client.ClientDirectoryDto;
import com.technofacts.lnf.dto.common.PageRequestDto;
import com.technofacts.lnf.service.common.page.PageableAsQueryParam;
import com.technofacts.lnf.service.common.page.PaginationAndSortingHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/lnf")
public class ClientDirectoriesController {

    private final ClientDirectoryService service;

    private final PaginationAndSortingHandler paginationAndSortingHandler;

    @GetMapping(value = "/clients/directories")
    @ResponseStatus(HttpStatus.OK)
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

    @PostMapping(value = "/clients/{clientId}/directories")
    @ResponseStatus(HttpStatus.CREATED)
    public void create(@PathVariable("clientId") UUID clientId, @RequestBody final List<ClientDirectoryDto> resource) {
        service.createAll(clientId, resource);
    }

}
