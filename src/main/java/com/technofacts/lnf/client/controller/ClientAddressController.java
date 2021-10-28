package com.technofacts.lnf.client.controller;

import java.util.UUID;

import com.technofacts.lnf.client.dto.AddressDto;
import com.technofacts.lnf.client.service.ClientAddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/lnf")
public class ClientAddressController {

    private final ClientAddressService service;

    @GetMapping(value = "/clients/{clientId}/address")
    public AddressDto findByEmployeeId(@PathVariable("clientId") final UUID clientId) {
        return service.findByClientId(clientId);
    }

    @GetMapping(value = "/clients/{clientId}/address/{addressId}")
    public AddressDto findById(@PathVariable("clientId") final UUID clientId, @PathVariable("addressId") final UUID addressId) {
        return service.findById(clientId, addressId);
    }

    @PostMapping(value = "/clients/{clientId}/address")
    @ResponseStatus(HttpStatus.CREATED)
    public void create(@PathVariable("clientId") final UUID clientId, @RequestBody final AddressDto resource) {
        service.create(clientId, resource);
    }

    @PutMapping(value = "/clients/{clientId}/address/{addressId}")
    @ResponseStatus(HttpStatus.OK)
    public void update(@PathVariable("clientId") final UUID clientId, @PathVariable("addressId") final UUID addressId, @RequestBody final AddressDto resource) {
        service.update(clientId, addressId, resource);
    }

    @DeleteMapping(value = "/clients/{clientId}/address")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("clientId") final UUID clientId) {
        service.deleteByClientId(clientId);
    }

    @DeleteMapping(value = "/clients/{clientId}/address/{addressId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("clientId") final UUID clientId, @PathVariable("addressId") final UUID addressId) {
        service.deleteById(clientId, addressId);
    }
}
