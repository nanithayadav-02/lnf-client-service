package com.technofacts.lnf.client.controller;

import java.util.List;
import java.util.UUID;

import com.technofacts.lnf.client.service.EscalationService;
import com.technofacts.lnf.dto.client.EscalationDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

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
    public List<EscalationDto> findByClientId(@PathVariable("clientId") final UUID clientId) {
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
    public EscalationDto findById(@PathVariable("clientId") final UUID clientId, @PathVariable("escalationId") final UUID escalationId) {
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
    public void create(@PathVariable("clientId") final UUID clientId, @RequestBody final List<EscalationDto> resource) {
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
    public void update(@PathVariable("clientId") final UUID clientId, @PathVariable("escalationId") final UUID escalationId, @RequestBody final EscalationDto resource) {
        service.update(clientId, escalationId, resource);
    }

    /**
     * Deletes the client escalation by clientId
     *
     * @param clientId Client Id
     */
    @DeleteMapping(value = "/clients/{clientId}/escalation")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("clientId") final UUID clientId) {
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
    public void delete(@PathVariable("clientId") final UUID clientId, @PathVariable("escalationId") final UUID escalationId) {
        service.deleteById(clientId, escalationId);
    }
}

