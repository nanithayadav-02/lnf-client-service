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

import com.lnf.client.service.ClientDetailsService;
import com.lnf.dto.client.ClientDetailsDto;
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
