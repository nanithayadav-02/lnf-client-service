package com.technofacts.lnf.client.service;

import com.technofacts.lnf.client.converter.ContactConverter;
import com.technofacts.lnf.client.model.Client;
import com.technofacts.lnf.client.model.ClientContact;
import com.technofacts.lnf.client.repository.ClientContactRepository;
import com.technofacts.lnf.client.repository.ClientRepository;
import com.technofacts.lnf.dto.client.ContactDto;
import com.technofacts.lnf.exception.LnFBadRequestException;
import com.technofacts.lnf.exception.LnFEntityNotFoundException;
import com.technofacts.lnf.exception.LnFException;
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
        LnFBadRequestException.throwOnCondition(Objects::isNull, resource, String.format("Failed to create contact for client [%s] with null payload", clientId));
        Client clientEntity = searchForClient(clientId);
        List<ClientContact> entities = new ArrayList<>();
        resource.stream().filter(Objects::nonNull).forEach(contactDto -> {
            ClientContact entity = ContactConverter.toEntityModel(contactDto);
            entity.setClient(clientEntity);
            entities.add(entity);
        });
        save(entities);
        log.error("Contact for client {} successfully created", clientId);
    }

    /**
     * Creates the contact for the client
     *
     * @param clientId Client Id
     * @param resource ContactDto
     */
    public void create(UUID clientId, ContactDto resource) {
        LnFBadRequestException.throwOnCondition(Objects::isNull, resource, String.format("Failed to create contact for client[%s] with null payload", clientId));
        Client clientEntity = searchForClient(clientId);
        ClientContact entity = ContactConverter.toEntityModel(resource);
        entity.setClient(clientEntity);
        save(entity);
        log.error("Contact for client {} successfully created", clientId);
    }

    /**
     * Updates the contact for the client
     *
     * @param clientId  Client Id
     * @param contactId Contact ID
     * @param resource  ContactDto
     */
    public void update(UUID clientId, UUID contactId, ContactDto resource) {
        LnFBadRequestException.throwOnCondition(Objects::isNull, resource, String.format("Failed to contact client[%s] with null payload", clientId));
        searchForClient(clientId);
        ClientContact entity = searchForContact(contactId);
        ClientContact updatedEntity = ContactConverter.toEntityModel(resource, entity);
        save(updatedEntity);
        log.error("Contact for client {} successfully updated", clientId);
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
            log.error("Contacts for client {} successfully deleted", clientId);
        } catch (RuntimeException e) {
            String errorMessage = String.format("Failed to delete contacts for client [%s]", clientId);
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
            log.error("Contact {} for client {} successfully deleted", contactId, clientId);
        } catch (RuntimeException e) {
            String errorMessage = String.format("Failed to delete contact[%s] for client [%s]", contactId, clientId);
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
                orElseThrow(() -> new LnFEntityNotFoundException(String.format("Client with id [%s] does not exist", clientId)));
    }

    private ClientContact searchForContact(UUID contactId) {
        return repository.findById(contactId).
                orElseThrow(() -> new LnFEntityNotFoundException(String.format("Contact with id [%s] does not exist", contactId)));
    }
}
