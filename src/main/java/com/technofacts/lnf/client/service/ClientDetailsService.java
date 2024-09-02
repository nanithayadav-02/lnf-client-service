package com.technofacts.lnf.client.service;

import com.technofacts.lnf.client.converter.ClientDetailsConverter;
import com.technofacts.lnf.client.model.Client;
import com.technofacts.lnf.client.repository.ClientRepository;
import com.technofacts.lnf.dto.client.ClientDetailsDto;
import com.technofacts.lnf.exception.LnFEntityNotFoundException;
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

    private final ClientRepository  clientRepository;

    public ClientDetailsDto findByClientId(UUID clientId) {
        Client entity = search(clientId);
        return ClientDetailsConverter.toTransportModel(entity);
    }

    private Client search(UUID clientId) {
        return clientRepository.findById(clientId).
                orElseThrow(() -> new LnFEntityNotFoundException(String.format("Client with id [%s] does not exist", clientId)));
    }

}
