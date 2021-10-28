package com.technofacts.lnf.client.controller;

import java.util.List;
import java.util.UUID;

import com.technofacts.lnf.client.dto.GstDto;
import com.technofacts.lnf.client.service.GstService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/lnf")
public class GstController {

    private final GstService service;

    @GetMapping(value = "/clients/{clientId}/gst")
    public List<GstDto> findByEmployeeId(@PathVariable("clientId") final UUID clientId) {
        return service.findByClientId(clientId);
    }

    @GetMapping(value = "/clients/{clientId}/gst/{gstId}")
    public GstDto findById(@PathVariable("clientId") final UUID clientId, @PathVariable("gstId") final UUID gstId) {
        return service.findById(clientId, gstId);
    }

    @PostMapping(value = "/clients/{clientId}/gst")
    @ResponseStatus(HttpStatus.CREATED)
    public void create(@PathVariable("clientId") final UUID clientId, @RequestBody final GstDto resource) {
        service.create(clientId, resource);
    }

    @PutMapping(value = "/clients/{clientId}/gst/{gstId}")
    @ResponseStatus(HttpStatus.OK)
    public void update(@PathVariable("clientId") final UUID clientId, @PathVariable("gstId") final UUID gstId, @RequestBody final GstDto resource) {
        service.update(clientId, gstId, resource);
    }

    @DeleteMapping(value = "/clients/{clientId}/gst")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("clientId") final UUID clientId) {
        service.deleteByClientId(clientId);
    }

    @DeleteMapping(value = "/clients/{clientId}/gst/{gstId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("clientId") final UUID clientId, @PathVariable("gstId") final UUID gstId) {
        service.deleteById(clientId, gstId);
    }
}

