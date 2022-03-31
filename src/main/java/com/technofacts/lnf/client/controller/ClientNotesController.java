package com.technofacts.lnf.client.controller;

import com.technofacts.lnf.client.service.ClientNotesService;
import com.technofacts.lnf.dto.client.ClientNotesDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/lnf")
public class ClientNotesController {

    private final ClientNotesService service;

    @GetMapping(value = "/clients/{clientId}/notes")
    public List<ClientNotesDto> findByClientId(@PathVariable("clientId") final UUID clientId) {
        return service.findByClientId(clientId);
    }

    @GetMapping(value = "/clients/{clientId}/notes/{notesId}")
    public ClientNotesDto findById(@PathVariable("clientId") final UUID clientId, @PathVariable("notesId") final UUID notesId) {
        return service.findById(clientId, notesId);
    }

    @GetMapping(value = "/clients/notes")
    @ResponseStatus(HttpStatus.OK)
    public List<ClientNotesDto> findAll() {
        return service.findAll();
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
