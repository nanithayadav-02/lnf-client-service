package com.technofacts.lnf.client.controller;

import java.util.UUID;

import com.technofacts.lnf.dto.client.EscalationDto;
import com.technofacts.lnf.client.service.EscalationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/lnf")
public class EscalationController {

    private final EscalationService service;

    @GetMapping(value = "/clients/{clientId}/escalation")
    public EscalationDto findByClientId(@PathVariable("clientId") final UUID clientId) {
        return service.findByClientId(clientId);
    }

    @GetMapping(value = "/clients/{clientId}/escalation/{escalationId}")
    public EscalationDto findById(@PathVariable("clientId") final UUID clientId, @PathVariable("escalationId") final UUID escalationId) {
        return service.findById(clientId, escalationId);
    }

    @PostMapping(value = "/clients/{clientId}/escalation")
    @ResponseStatus(HttpStatus.CREATED)
    public void create(@PathVariable("clientId") final UUID clientId, @RequestBody final EscalationDto resource) {
        service.create(clientId, resource);
    }

    @PutMapping(value = "/clients/{clientId}/escalation/{escalationId}")
    @ResponseStatus(HttpStatus.OK)
    public void update(@PathVariable("clientId") final UUID clientId, @PathVariable("escalationId") final UUID escalationId, @RequestBody final EscalationDto resource) {
        service.update(clientId, escalationId, resource);
    }

    @DeleteMapping(value = "/clients/{clientId}/escalation")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("clientId") final UUID clientId) {
        service.deleteByClientId(clientId);
    }

    @DeleteMapping(value = "/clients/{clientId}/escalation/{escalationId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("clientId") final UUID clientId, @PathVariable("escalationId") final UUID escalationId) {
        service.deleteById(clientId, escalationId);
    }
}

