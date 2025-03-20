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

import com.lnf.client.service.ClientDirectoryService;
import com.lnf.dto.client.ClientDirectoryDto;
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
public class ClientDirectoriesController {

    private final ClientDirectoryService service;
    private final PaginationAndSortingHandler paginationAndSortingHandler;

    @GetMapping(value = "/clients/{clientId}/directory")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> findAll(@RequestParam(required = false) String search,
                                     @PathVariable UUID clientId,
                                     @PageableAsQueryParam PageRequestDto pageRequest) {
        if (search != null && !search.isEmpty()) {
            if (pageRequest != null && pageRequest.getPage() != null) {
                return ResponseEntity.ok(service.findingAllWithPagination(search, clientId, pageRequest));
            } else {
                return ResponseEntity.ok(service.findAllByClientId(clientId, search));
            }
        } else if (clientId != null) {
            return service.findClientsDirectoryByClientId(clientId, pageRequest);
        } else {
            return paginationAndSortingHandler.handleFindAllRequest(pageRequest, service);
        }
    }

    @PostMapping(value = "/clients/{clientId}/directories")
    @ResponseStatus(HttpStatus.CREATED)
    public void create(@PathVariable UUID clientId, @RequestBody final List<ClientDirectoryDto> resource) {
        service.createAll(clientId, resource);
    }

}
