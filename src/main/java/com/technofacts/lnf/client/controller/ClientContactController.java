package com.technofacts.lnf.client.controller;

import java.util.List;
import java.util.UUID;

import com.technofacts.lnf.client.service.ClientContactService;
import com.technofacts.lnf.dto.client.ContactDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/lnf")
public class ClientContactController {

    private final ClientContactService service;

    /**
     * Returns ContactDto for the client by clientId
     *
     * @param clientId Client Id
     * @return ContactDto of the client
     */
    @GetMapping(value = "/clients/{clientId}/contact")
    @ResponseStatus(HttpStatus.OK)
    public List<ContactDto> findByClientId(@PathVariable("clientId") final UUID clientId) {
        return service.findByClientId(clientId);
    }

    /**
     * Returns ContactDto of the Client by clientId and contactId
     *
     * @param clientId  Client Id
     * @param contactId Contact ID
     * @return ContactDto of the client
     */
    @GetMapping(value = "/clients/{clientId}/contact/{contactId}")
    @ResponseStatus(HttpStatus.OK)
    public ContactDto findById(@PathVariable("clientId") final UUID clientId, @PathVariable("contactId") final UUID contactId) {
        return service.findById(clientId, contactId);
    }

    /**
     * Creates the contact for the client
     *
     * @param clientId Client Id
     * @param resource ContactDto
     */
    @PostMapping(value = "/clients/{clientId}/contact")
    @ResponseStatus(HttpStatus.CREATED)
    public void create(@PathVariable("clientId") final UUID clientId, @RequestBody final ContactDto resource) {
        service.create(clientId, resource);
    }

    /**
     * Updates the contact for the client
     *
     * @param clientId  Client Id
     * @param contactId Contact ID
     * @param resource  ContactDto
     */
    @PutMapping(value = "/clients/{clientId}/contact/{contactId}")
    @ResponseStatus(HttpStatus.OK)
    public void update(@PathVariable("clientId") final UUID clientId, @PathVariable("contactId") final UUID contactId, @RequestBody final ContactDto resource) {
        service.update(clientId, contactId, resource);
    }

    /**
     * Deletes the client contact by clientId
     *
     * @param clientId Client Id
     */
    @DeleteMapping(value = "/clients/{clientId}/contact")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("clientId") final UUID clientId) {
        service.deleteByClientId(clientId);
    }

    /**
     * Deletes the client contact by clientId and contactId
     *
     * @param clientId  Client Id
     * @param contactId Contact Id
     */
    @DeleteMapping(value = "/clients/{clientId}/contact/{contactId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("clientId") final UUID clientId, @PathVariable("contactId") final UUID contactId) {
        service.deleteById(clientId, contactId);
    }
}
