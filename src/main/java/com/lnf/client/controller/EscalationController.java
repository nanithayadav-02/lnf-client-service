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

import com.lnf.client.service.EscalationService;
import com.lnf.dto.client.EscalationDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/lnf")
public class EscalationController {

    private final EscalationService service;

    /**
     * Returns EscalationDto for the client address by clientId
     *
     * @param clientId Client Id
     * @return EscalationDto of the client escalation
     */
    @GetMapping(value = "/clients/{clientId}/escalation")
    @ResponseStatus(HttpStatus.OK)
    public List<EscalationDto> findByClientId(@PathVariable final UUID clientId) {
        return service.findByClientId(clientId);
    }

    /**
     * Returns EscalationDto of the Client by clientId and addressId
     *
     * @param clientId     Client Id
     * @param escalationId Escalation ID
     * @return EscalationDto of the client escalation
     */
    @GetMapping(value = "/clients/{clientId}/escalation/{escalationId}")
    @ResponseStatus(HttpStatus.OK)
    public EscalationDto findById(@PathVariable final UUID clientId, @PathVariable final UUID escalationId) {
        return service.findById(clientId, escalationId);
    }

    /**
     * Creates the escalation for the client
     *
     * @param clientId Client Id
     * @param resource EscalationDto
     */
    @PostMapping(value = "/clients/{clientId}/escalation")
    @ResponseStatus(HttpStatus.CREATED)
    public void create(@PathVariable final UUID clientId, @RequestBody final List<EscalationDto> resource) {
        service.create(clientId, resource);
    }

    /**
     * Updates the escalation for the client
     *
     * @param clientId     Client Id
     * @param escalationId Escalation ID
     * @param resource     EscalationDto
     */
    @PutMapping(value = "/clients/{clientId}/escalation/{escalationId}")
    @ResponseStatus(HttpStatus.OK)
    public void update(@PathVariable final UUID clientId, @PathVariable final UUID escalationId, @RequestBody final EscalationDto resource) {
        service.update(clientId, escalationId, resource);
    }

    /**
     * Deletes the client escalation by clientId
     *
     * @param clientId Client Id
     */
    @DeleteMapping(value = "/clients/{clientId}/escalation")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable final UUID clientId) {
        service.deleteByClientId(clientId);
    }

    /**
     * Deletes the client escalation by clientId and escalationId
     *
     * @param clientId     Client Id
     * @param escalationId Escalation Id
     */
    @DeleteMapping(value = "/clients/{clientId}/escalation/{escalationId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable final UUID clientId, @PathVariable final UUID escalationId) {
        service.deleteById(clientId, escalationId);
    }
}

