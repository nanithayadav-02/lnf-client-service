/*
 * Copyright (c)  Lever And Fulcrum (LNF)
 */
package com.technofacts.lnf.client.controller;

import com.technofacts.lnf.client.service.ClientDirectoryService;
import com.technofacts.lnf.dto.client.ClientDirectoryDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/lnf")
public class ClientDirectoryController {

    private final ClientDirectoryService service;

    @PostMapping(value = "/clients/{clientId}/directory")
    @ResponseStatus(HttpStatus.CREATED)
    public void create(@PathVariable("clientId") UUID clientId, @RequestBody final ClientDirectoryDto resource) {
        service.create(clientId, resource);
    }

    @PutMapping(value = "/clients/{clientId}/directory/{directoryId}")
    @ResponseStatus(HttpStatus.OK)
    public void update(@PathVariable("clientId") UUID clientId, @PathVariable("directoryId") UUID directoryId,
                       @RequestBody final ClientDirectoryDto resource) {
        service.update(clientId, directoryId, resource);
    }

    @GetMapping(value = "/clients/{clientId}/directory")
    public List<ClientDirectoryDto> findByClientId(@PathVariable("clientId") final UUID clientId) {
        return service.findByClientId(clientId);
    }

    @GetMapping(value = "/clients/{clientId}/directory/{directoryId}")
    public ClientDirectoryDto findByClientIdAndId(@PathVariable("clientId") final UUID clientId,
                                                  @PathVariable("directoryId") final UUID directoryId) {
        return service.findByClientIdAndDirectoryId(clientId, directoryId);
    }

    @DeleteMapping(value = "/clients/{clientId}/directory")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteByClientId(@PathVariable("clientId") UUID clientId) {
        service.deleteByClientId(clientId);
    }

    @DeleteMapping(value = "/clients/{clientId}/directory/{directoryId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteByClientIdAndId(@PathVariable("clientId") UUID clientId,
                                      @PathVariable("directoryId") UUID directoryId){
        service.deleteByClientIdAndDirectoryId(clientId, directoryId);
    }

    @GetMapping("/clients/directory/search")
    @ResponseStatus(HttpStatus.OK)
    public List<ClientDirectoryDto> searchByEmail(@RequestParam("email") String email) {
        return service.findByEmail(email);
    }


}
