package com.technofacts.lnf.client.service;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import com.technofacts.lnf.client.converter.AddressConverter;
import com.technofacts.lnf.dto.client.AddressDto;
import com.technofacts.lnf.exception.LnFBadRequestException;
import com.technofacts.lnf.exception.LnFEntityNotFoundException;
import com.technofacts.lnf.exception.LnFException;
import com.technofacts.lnf.client.model.Client;
import com.technofacts.lnf.client.model.ClientAddress;
import com.technofacts.lnf.client.repository.ClientAddressRepository;
import com.technofacts.lnf.client.repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
@Log
public class ClientAddressService {

    private final ClientAddressRepository repository;
    private final ClientRepository clientRepository;

    public List<AddressDto> findAll() {
        List<ClientAddress> entities = repository.findAll();
        return entities.stream().map(AddressConverter::toTransportModel).filter(Objects::nonNull).collect(Collectors.toList());
    }

    public AddressDto findByClientId(UUID clientId) {
        searchForClient(clientId);
        ClientAddress entity = repository.findByClientId(clientId)
                .orElseThrow(() -> new LnFEntityNotFoundException(String.format("Address for client [%s] does not exist", clientId)));
        return AddressConverter.toTransportModel(entity);
    }

    public AddressDto findById(UUID clientId, UUID addressId) {
        searchForClient(clientId);
        return AddressConverter.toTransportModel(searchForAddress(addressId));
    }

    public void create(UUID clientId, AddressDto resource) {
        LnFBadRequestException.throwOnCondition(Objects::isNull, resource, String.format("Failed to create Address for client[%s] with null payload", clientId));
        Client clientEntity = searchForClient(clientId);
        ClientAddress entity = AddressConverter.toEntityModel(resource);
        entity.setClient(clientEntity);
        save(entity);
        log.info(() -> String.format("Address for Client[%s] successfully created", clientId));
    }

    public void update(UUID clientId, UUID addressId, AddressDto resource) {
        LnFBadRequestException.throwOnCondition(Objects::isNull, resource, String.format("Failed to update Address for client[%s] with null payload", clientId));
        searchForClient(clientId);
        ClientAddress entity = searchForAddress(addressId);
        ClientAddress updatedEntity = AddressConverter.toEntityModel(resource, entity);
        save(updatedEntity);
        log.info(() -> String.format("Address for Client[%s] successfully created", clientId));
    }

    public void deleteById(UUID clientId, UUID addressId) {
        searchForClient(clientId);
        ClientAddress entity = searchForAddress(addressId);
        try {
            repository.delete(entity);
            log.info(() -> String.format("Address[%s] for client [%s] successfully deleted", addressId, clientId));
        } catch (RuntimeException e) {
            String errorMessage = String.format("Failed to delete Address[[%s] for client [%s]", addressId, clientId);
            throw new LnFException(errorMessage);
        }
    }

    public void deleteByClientId(UUID clientId) {
        searchForClient(clientId);
        ClientAddress entity = repository.findByClientId(clientId)
                .orElseThrow(() -> new LnFEntityNotFoundException(String.format("Address for client [%s] does not exist", clientId)));
        try {
            repository.delete(entity);
            log.info(() -> String.format("Address for Client[%s] successfully deleted", clientId));
        } catch (RuntimeException e) {
            String errorMessage = String.format("Failed to delete Address for client [%s]", clientId);
            throw new LnFException(errorMessage);
        }
        
    }

    private void save(ClientAddress entity) {
        try {
            repository.save(entity);
        } catch (RuntimeException e) {
            String errorMessage = String.format("Failed to save Address for client [%s]", entity.getClient().getId());
            throw new LnFException(errorMessage);
        }
    }

    private Client searchForClient(UUID clientId) {
        return clientRepository.findById(clientId).
                orElseThrow(() -> new LnFEntityNotFoundException(String.format("Client with id [%s] does not exist", clientId)));
    }

    private ClientAddress searchForAddress(UUID addressId) {
        return repository.findById(addressId).
                orElseThrow(() -> new LnFEntityNotFoundException(String.format("Address with id [%s] does not exist", addressId)));
    }

}

