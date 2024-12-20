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

import com.lnf.client.converter.ContactConverter;
import com.lnf.client.model.Client;
import com.lnf.client.model.ClientContact;
import com.lnf.client.repository.ClientContactRepository;
import com.lnf.client.repository.ClientRepository;
import com.lnf.dto.client.ContactDto;
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
public class ClientContactService {

    private final ClientContactRepository repository;
    private final ClientRepository clientRepository;

    /**
     * Returns ContactDto for the client by clientId
     *
     * @param clientId Client Id
     * @return ContactDto of the client
     */
    public List<ContactDto> findByClientId(UUID clientId) {
        searchForClient(clientId);
        List<ClientContact> entities = repository.findByClientId(clientId);
        return entities.stream().map(ContactConverter::toTransportModel).filter(Objects::nonNull).toList();
    }

    /**
     * Returns ContactDto of the Client by clientId and contactId
     *
     * @param clientId  Client Id
     * @param contactId Contact ID
     * @return ContactDto of the client
     */
    public ContactDto findById(UUID clientId, UUID contactId) {
        searchForClient(clientId);
        return ContactConverter.toTransportModel(searchForContact(contactId));
    }

    public void create(UUID clientId, List<ContactDto> resource) {
        LnFBadRequestException.throwOnCondition(Objects::isNull, resource, "Failed to create contact for client [%s] with null payload".formatted(clientId));
        Client clientEntity = searchForClient(clientId);
        List<ClientContact> entities = new ArrayList<>();
        resource.stream().filter(Objects::nonNull).forEach(contactDto -> {
            ClientContact entity = ContactConverter.toEntityModel(contactDto);
            entity.setClient(clientEntity);
            entities.add(entity);
        });
        save(entities);
        log.debug("Contact for client {} successfully created", clientId);
    }

    /**
     * Creates the contact for the client
     *
     * @param clientId Client Id
     * @param resource ContactDto
     */
    public void create(UUID clientId, ContactDto resource) {
        LnFBadRequestException.throwOnCondition(Objects::isNull, resource, "Failed to create contact for client[%s] with null payload".formatted(clientId));
        Client clientEntity = searchForClient(clientId);
        ClientContact entity = ContactConverter.toEntityModel(resource);
        entity.setClient(clientEntity);
        save(entity);
        log.debug("Contact for client {} successfully created", clientId);
    }

    /**
     * Updates the contact for the client
     *
     * @param clientId  Client Id
     * @param contactId Contact ID
     * @param resource  ContactDto
     */
    public void update(UUID clientId, UUID contactId, ContactDto resource) {
        LnFBadRequestException.throwOnCondition(Objects::isNull, resource, "Failed to contact client[%s] with null payload".formatted(clientId));
        searchForClient(clientId);
        ClientContact entity = searchForContact(contactId);
        ClientContact updatedEntity = ContactConverter.toEntityModel(resource, entity);
        save(updatedEntity);
        log.debug("Contact for client {} successfully updated", clientId);
    }

    /**
     * Deletes the client contact by clientId
     *
     * @param clientId Client Id
     */
    public void deleteByClientId(UUID clientId) {
        searchForClient(clientId);
        List<ClientContact> entities = repository.findByClientId(clientId);
        try {
            repository.deleteAll(entities);
            log.debug("Contacts for client {} successfully deleted", clientId);
        } catch (RuntimeException e) {
            String errorMessage = "Failed to delete contacts for client [%s]".formatted(clientId);
            throw new LnFException(errorMessage);
        }
    }

    /**
     * Deletes the client contact by clientId and contactId
     *
     * @param clientId  Client Id
     * @param contactId Contact Id
     */
    public void deleteById(UUID clientId, UUID contactId) {
        searchForClient(clientId);
        ClientContact entity = searchForContact(contactId);
        try {
            repository.delete(entity);
            log.debug("Contact {} for client {} successfully deleted", contactId, clientId);
        } catch (RuntimeException e) {
            String errorMessage = "Failed to delete contact[%s] for client [%s]".formatted(contactId, clientId);
            throw new LnFException(errorMessage);
        }
    }

    private void save(ClientContact entity) {
        try {
            repository.save(entity);
        } catch (RuntimeException e) {
            String errorMessage = String.format("Failed to save contact for client [%s]", entity.getClient().getId());
            throw new LnFException(errorMessage);
        }
    }

    private void save(List<ClientContact> entities) {
        try {
            repository.saveAll(entities);
        } catch (RuntimeException e) {
            String errorMessage = String.format("Failed to save contact for employee [%s]", entities.get(0).getClient().getId());
            throw new LnFException(errorMessage);
        }
    }

    private Client searchForClient(UUID clientId) {
        return clientRepository.findById(clientId).
                orElseThrow(() -> new LnFEntityNotFoundException("Client with id [%s] does not exist".formatted(clientId)));
    }

    private ClientContact searchForContact(UUID contactId) {
        return repository.findById(contactId).
                orElseThrow(() -> new LnFEntityNotFoundException("Contact with id [%s] does not exist".formatted(contactId)));
    }
}
