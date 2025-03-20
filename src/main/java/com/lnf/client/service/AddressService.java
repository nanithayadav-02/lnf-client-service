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

import com.lnf.client.converter.AddressConverter;
import com.lnf.client.model.Client;
import com.lnf.client.model.ClientAddress;
import com.lnf.client.model.enums.AddressType;
import com.lnf.client.repository.ClientAddressRepository;
import com.lnf.client.repository.ClientRepository;
import com.lnf.dto.client.AddressDto;
import com.lnf.exception.LnFBadRequestException;
import com.lnf.exception.LnFEntityNotFoundException;
import com.lnf.exception.LnFException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class AddressService {

    private final ClientAddressRepository repository;
    private final ClientRepository clientRepository;

    /**
     * Returns List of AddressDtos for the client address by clientId
     *
     * @param clientId Client Id
     * @return List of AddressDtos of the client address
     */
    public List<AddressDto> findByClientId(UUID clientId) {
        searchForClient(clientId);
        List<ClientAddress> entities = repository.findAddressByClientId(clientId);
        return entities.stream().map(AddressConverter::toTransportModel).filter(Objects::nonNull).toList();
    }

    /**
     * Returns AddressDto of the Client by clientId and addressId
     *
     * @param clientId  Client Id
     * @param addressId Address ID
     * @return AddressDto of the client address
     */
    public AddressDto findById(UUID clientId, UUID addressId) {
        searchForClient(clientId);
        return AddressConverter.toTransportModel(searchForAddress(addressId));
    }

    /**
     * Creates the list of addresses for the client
     *
     * @param clientId Client Id
     * @param resource AddressDto
     */
    public void create(UUID clientId, List<AddressDto> resource) {
        LnFBadRequestException.throwOnCondition(Objects::isNull, resource, "Failed to create Addresses for client[%s] with null payload".formatted(clientId));
        Client client = searchForClient(clientId);
        List<ClientAddress> entities = new ArrayList<>();
        resource.stream().filter(Objects::nonNull).forEach(addressDto -> {
            ClientAddress entity = AddressConverter.toEntityModel(addressDto, new ClientAddress());
            entity.setClient(client);
            entities.add(entity);
        });
        save(entities);
        log.debug("Address for Client {} successfully created", clientId);
    }

    /**
     * Checks if a primary address already exists for the client
     *
     * @param clientId      Client Id
     * @param clientEntity  The client entity
     * @param clientAddress Address of the client
     */
    private void checkIfPrimaryAddressExists(UUID clientId, Client clientEntity, AddressDto clientAddress) {
        if (clientAddress.getAddressType() != null) {
            boolean isCurrentAddressPrimary = clientAddress.getAddressType().equals(AddressType.Primary.name());
            if (isCurrentAddressPrimary) {
                boolean primaryExists = clientEntity.getClientAddresses().stream()
                        .anyMatch(address -> address.getAddressType() == AddressType.Primary);

                if (primaryExists) {
                    throw new LnFBadRequestException("Client[%s] already has a Primary address".formatted(clientId));
                }
            }
        }
    }

    public void create(UUID clientId, AddressDto resource) {
        LnFBadRequestException.throwOnCondition(Objects::isNull, resource, "Failed to create Address for client[%s] with null payload".formatted(clientId));
        Client clientEntity = searchForClient(clientId);
        checkIfPrimaryAddressExists(clientId, clientEntity, resource);
        ClientAddress entity = AddressConverter.toEntityModel(resource);
        entity.setClient(clientEntity);
        save(entity);
        log.debug("Address for Client {} successfully created", clientId);
    }

    /**
     * Updates the address for the client
     *
     * @param clientId  Client Id
     * @param addressId Address ID
     * @param resource  AddressDto
     */
    public void update(UUID clientId, UUID addressId, AddressDto resource) {
        LnFBadRequestException.throwOnCondition(Objects::isNull, resource, "Failed to update Address for client[%s] with null payload".formatted(clientId));
        Client clientEntity = searchForClient(clientId);
        ClientAddress entity = searchForAddress(addressId);
        boolean primaryExists = clientEntity.getClientAddresses().stream()
                .anyMatch(address -> address.getAddressType() == AddressType.Primary);

        if (primaryExists && entity.getAddressType() != AddressType.Primary
                && resource.getAddressType().equals(AddressType.Primary.name())) {
            throw new LnFException("Primary AddressType already exists for addressId  : " + addressId);
        }
        ClientAddress updatedEntity = AddressConverter.toEntityModel(resource, entity);
        save(updatedEntity);
        log.debug("Address for Client {} successfully updated", clientId);
    }

    /**
     * Deletes the List of client addresses by clientId
     *
     * @param clientId Client Id
     */
    public void deleteByClientId(UUID clientId) {
        searchForClient(clientId);
        List<ClientAddress> entities = repository.findAddressByClientId(clientId);
        try {
            repository.deleteAll(entities);
            log.debug("Addresses for Client {} successfully deleted", clientId);
        } catch (RuntimeException e) {
            String errorMessage = "Failed to delete Addresses for client [%s]".formatted(clientId);
            throw new LnFException(errorMessage);
        }

    }

    /**
     * Deletes the client address by clientId and addressId
     *
     * @param clientId  Client Id
     * @param addressId Address Id
     */
    public void deleteById(UUID clientId, UUID addressId) {
        searchForClient(clientId);
        ClientAddress entity = searchForAddress(addressId);
        try {
            repository.delete(entity);
            log.debug("Address {} for client {} successfully deleted", addressId, clientId);
        } catch (RuntimeException e) {
            String errorMessage = "Failed to delete Address[[%s] for client [%s]".formatted(addressId, clientId);
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

    private void save(List<ClientAddress> entities) {
        try {
            repository.saveAll(entities);
        } catch (RuntimeException e) {
            String errorMessage = "Failed to save Address for client [%s]".formatted(entities.getFirst().getId());
            throw new LnFException(errorMessage);
        }
    }

    private Client searchForClient(UUID clientId) {
        return clientRepository.findById(clientId).
                orElseThrow(() -> new LnFEntityNotFoundException("Client with id [%s] does not exist".formatted(clientId)));
    }

    private ClientAddress searchForAddress(UUID addressId) {
        return repository.findById(addressId).
                orElseThrow(() -> new LnFEntityNotFoundException("Address with id [%s] does not exist".formatted(addressId)));
    }

}

