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

import com.lnf.client.service.ClientContactService;
import com.lnf.dto.client.ContactDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

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
    public List<ContactDto> findByClientId(@PathVariable final UUID clientId) {
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
    public ContactDto findById(@PathVariable final UUID clientId, @PathVariable final UUID contactId) {
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
    public void create(@PathVariable final UUID clientId, @RequestBody final ContactDto resource) {
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
    public void update(@PathVariable final UUID clientId, @PathVariable final UUID contactId, @RequestBody final ContactDto resource) {
        service.update(clientId, contactId, resource);
    }

    /**
     * Deletes the client contact by clientId
     *
     * @param clientId Client Id
     */
    @DeleteMapping(value = "/clients/{clientId}/contact")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable final UUID clientId) {
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
    public void delete(@PathVariable final UUID clientId, @PathVariable final UUID contactId) {
        service.deleteById(clientId, contactId);
    }
}
