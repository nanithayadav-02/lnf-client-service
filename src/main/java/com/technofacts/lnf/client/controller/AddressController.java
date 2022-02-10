package com.technofacts.lnf.client.controller;

import java.util.UUID;

import com.technofacts.lnf.client.service.AddressService;
import com.technofacts.lnf.dto.client.AddressDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/lnf")
public class AddressController {

    private final AddressService service;

    /**
     * Returns AddressDto for the client address by clientId
     *
     * @param clientId Client Id
     * @return AddressDto of the client address
     */
    @GetMapping(value = "/clients/{clientId}/address")
    @ResponseStatus(HttpStatus.OK)
    public AddressDto findByClientId(@PathVariable("clientId") final UUID clientId) {
        return service.findByClientId(clientId);
    }

    /**
     * Returns AddressDto of the Client by clientId and addressId
     *
     * @param clientId  Client Id
     * @param addressId Address ID
     * @return AddressDto of the client address
     */
    @GetMapping(value = "/clients/{clientId}/address/{addressId}")
    @ResponseStatus(HttpStatus.OK)
    public AddressDto findById(@PathVariable("clientId") final UUID clientId, @PathVariable("addressId") final UUID addressId) {
        return service.findById(clientId, addressId);
    }

    /**
     * Creates the address for the client
     *
     * @param clientId Client Id
     * @param resource AddressDto
     */
    @PostMapping(value = "/clients/{clientId}/address")
    @ResponseStatus(HttpStatus.CREATED)
    public void create(@PathVariable("clientId") final UUID clientId, @RequestBody final AddressDto resource) {
        service.create(clientId, resource);
    }

    /**
     * Updates the address for the client
     *
     * @param clientId  Client Id
     * @param addressId Address ID
     * @param resource  AddressDto
     */
    @PutMapping(value = "/clients/{clientId}/address/{addressId}")
    @ResponseStatus(HttpStatus.OK)
    public void update(@PathVariable("clientId") final UUID clientId, @PathVariable("addressId") final UUID addressId, @RequestBody final AddressDto resource) {
        service.update(clientId, addressId, resource);
    }

    /**
     * Deletes the client address by clientId
     *
     * @param clientId Client Id
     */
    @DeleteMapping(value = "/clients/{clientId}/address")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("clientId") final UUID clientId) {
        service.deleteByClientId(clientId);
    }

    /**
     * Deletes the client address by clientId and addressId
     *
     * @param clientId  Client Id
     * @param addressId Address Id
     */
    @DeleteMapping(value = "/clients/{clientId}/address/{addressId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("clientId") final UUID clientId, @PathVariable("addressId") final UUID addressId) {
        service.deleteById(clientId, addressId);
    }
}
