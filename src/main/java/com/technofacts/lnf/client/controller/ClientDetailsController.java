package com.technofacts.lnf.client.controller;

import com.technofacts.lnf.client.service.ClientDetailsService;
import com.technofacts.lnf.dto.client.ClientDetailsDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/lnf")
@Slf4j
public class ClientDetailsController {

    private final ClientDetailsService clientDetailsService;

    @GetMapping(value = "/clients/{clientId}/details")
    @ResponseStatus(HttpStatus.OK)
    public ClientDetailsDto findByClientId(@PathVariable("clientId") UUID clientId) {
        return clientDetailsService.findByClientId(clientId);
    }

}
