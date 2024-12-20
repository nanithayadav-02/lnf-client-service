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

package com.lnf.client.service;

import com.lnf.client.converter.ClientDetailsConverter;
import com.lnf.client.model.Client;
import com.lnf.client.repository.ClientRepository;
import com.lnf.dto.client.ClientDetailsDto;
import com.lnf.exception.LnFEntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ClientDetailsService {

    private final ClientRepository clientRepository;

    public ClientDetailsDto findByClientId(UUID clientId) {
        Client entity = search(clientId);
        return ClientDetailsConverter.toTransportModel(entity);
    }

    private Client search(UUID clientId) {
        return clientRepository.findById(clientId).
                orElseThrow(() -> new LnFEntityNotFoundException("Client with id [%s] does not exist".formatted(clientId)));
    }

}
