package com.technofacts.lnf.client.controller;

import java.util.List;
import java.util.UUID;

import com.technofacts.lnf.client.dto.ContactDto;
import com.technofacts.lnf.client.service.ClientContactService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/lnf")
public class ClientContactController {

    private final ClientContactService service;

    @GetMapping(value = "/clients/{clientId}/contact")
    public List<ContactDto> findByClientId(@PathVariable("clientId") final UUID clientId) {
        return service.findByClientId(clientId);
    }

    @GetMapping(value = "/clients/{clientId}/contact/{contactId}")
    public ContactDto findById(@PathVariable("clientId") final UUID clientId, @PathVariable("contactId") final UUID contactId) {
        return service.findById(clientId, contactId);
    }

    @PostMapping(value = "/clients/{clientId}/contact")
    @ResponseStatus(HttpStatus.CREATED)
    public void create(@PathVariable("clientId") final UUID clientId, @RequestBody final ContactDto resource) {
        service.create(clientId, resource);
    }

    @PutMapping(value = "/clients/{clientId}/contact/{contactId}")
    @ResponseStatus(HttpStatus.OK)
    public void update(@PathVariable("clientId") final UUID clientId, @PathVariable("contactId") final UUID contactId, @RequestBody final ContactDto resource) {
        service.update(clientId, contactId, resource);
    }

    @DeleteMapping(value = "/clients/{clientId}/contact")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("clientId") final UUID clientId) {
        service.deleteByClientId(clientId);
    }

    @DeleteMapping(value = "/clients/{clientId}/contact/{contactId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("clientId") final UUID clientId, @PathVariable("contactId") final UUID contactId) {
        service.deleteById(clientId, contactId);
    }
}
