package com.technofacts.lnf.client.controller;

import com.technofacts.lnf.client.service.AddressService;
import com.technofacts.lnf.dto.client.AddressDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/lnf")
public class AddressesController {

    private final AddressService service;

    /**
     * Creates the List of addresses for the client
     *
     * @param clientId  Client Id
     * @param resources List of AddressDto
     */
    @PostMapping(value = "/clients/{clientId}/addresses")
    @ResponseStatus(HttpStatus.CREATED)
    public void create(@PathVariable("clientId") final UUID clientId, @RequestBody final List<AddressDto> resources) {
        service.create(clientId, resources);
    }

}
