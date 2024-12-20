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

import com.lnf.client.service.AddressService;
import com.lnf.dto.client.AddressDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/lnf")
public class AddressController {

    private final AddressService service;

    /**
     * Returns List of AddressDtos for the client address by clientId
     *
     * @param clientId Client Id
     * @return List of AddressDtos of the client address
     */
    @GetMapping(value = "/clients/{clientId}/address")
    @ResponseStatus(HttpStatus.OK)
    public List<AddressDto> findByClientId(@PathVariable final UUID clientId) {
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
    public AddressDto findById(@PathVariable final UUID clientId, @PathVariable final UUID addressId) {
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
    public void create(@PathVariable final UUID clientId, @RequestBody final AddressDto resource) {
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
    public void update(@PathVariable final UUID clientId, @PathVariable final UUID addressId, @RequestBody final AddressDto resource) {
        service.update(clientId, addressId, resource);
    }

    /**
     * Deletes the client address by clientId
     *
     * @param clientId Client Id
     */
    @DeleteMapping(value = "/clients/{clientId}/address")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable final UUID clientId) {
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
    public void delete(@PathVariable final UUID clientId, @PathVariable final UUID addressId) {
        service.deleteById(clientId, addressId);
    }

}
