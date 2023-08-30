package com.technofacts.lnf.client.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import com.technofacts.lnf.client.converter.ClientNotesConverter;
import com.technofacts.lnf.client.model.Client;
import com.technofacts.lnf.client.model.ClientNotes;
import com.technofacts.lnf.client.repository.ClientNotesRepository;
import com.technofacts.lnf.client.repository.ClientRepository;
import com.technofacts.lnf.dto.client.ClientNotesDto;
import com.technofacts.lnf.exception.LnFBadRequestException;
import com.technofacts.lnf.exception.LnFEntityNotFoundException;
import com.technofacts.lnf.exception.LnFException;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
@Log
public class ClientNotesService {

    private final ClientNotesRepository repository;
    private final ClientRepository clientRepository;

    public List<ClientNotesDto> findAll() {
        List<ClientNotes> entities = repository.findAll();
        return entities.stream().map(ClientNotesConverter::toTransportModel).filter(Objects::nonNull).collect(Collectors.toList());
    }

    public List<ClientNotesDto> findByClientId(UUID clientId) {
        searchForClient(clientId);
        List<ClientNotes> entities = repository.findByClientId(clientId);
        return entities.stream().map(ClientNotesConverter::toTransportModel).filter(Objects::nonNull).collect(Collectors.toList());
    }

    public ClientNotesDto findById(UUID clientId, UUID notesId) {
        searchForClient(clientId);
        return ClientNotesConverter.toTransportModel(searchForNotes(notesId));
    }

    public void create(UUID clientId, List<ClientNotesDto> resource) {
        LnFBadRequestException.throwOnCondition(Objects::isNull, resource,
                String.format("Failed to create Notes for client [%s] with null payload", clientId));
        Client client = searchForClient(clientId);
        List<ClientNotes> entities = new ArrayList<>();
        resource.stream().filter(Objects::nonNull).forEach(notesDto -> {
            ClientNotes entity = (ClientNotes) ClientNotesConverter.toEntityModel(notesDto, new ClientNotes());
            entity.setClient(client);
            entities.add(entity);
        });
        save(entities);
        log.info(() -> "Notes for Client[" + clientId + "] successfully created");
    }

    public void create(UUID clientId, ClientNotesDto resource) {
        LnFBadRequestException.throwOnCondition(Objects::isNull, resource, "Failed to create Notes with null payload");
        Client employeeEntity = searchForClient(clientId);
        ClientNotes entity = (ClientNotes) ClientNotesConverter.toEntityModel(resource, new ClientNotes());
        entity.setClient(employeeEntity);
        save(entity);
        log.info(() -> String.format("Notes for Client[%s] successfully created", clientId));
    }

    public void update(UUID clientId, UUID notesId, ClientNotesDto resource) {
        LnFBadRequestException.throwOnCondition(Objects::isNull, resource, "Failed to update Notes with null payload");
        searchForClient(clientId);
        ClientNotes entity = searchForNotes(notesId);
        save((ClientNotes) ClientNotesConverter.toEntityModel(resource, entity));
        log.info(() -> String.format("Notes for Client[%s] successfully created", clientId));
    }

    public void deleteById(UUID clientId, UUID notesId) {
        searchForClient(clientId);
        ClientNotes entity = searchForNotes(notesId);
        try {
            repository.delete(entity);
            log.info(() -> String.format("Notes[%s] for client [%s] successfully deleted", notesId, clientId));
        } catch (RuntimeException e) {
            String errorMessage = String.format("Failed to delete Notes[[%s] for client [%s]", notesId, clientId);
            throw new LnFException(errorMessage);
        }
    }

    public void deleteByClientId(UUID clientId) {
        searchForClient(clientId);
        List<ClientNotes> entities = repository.findByClientId(clientId);
        try {
            repository.deleteAll(entities);
            log.info(() -> String.format("Notes for Client[%s] successfully deleted", clientId));
        } catch (RuntimeException e) {
            String errorMessage = String.format("Failed to delete Notes for client [%s]", clientId);
            throw new LnFException(errorMessage);
        }
    }

    private void save(ClientNotes entity) {
        try {
            repository.save(entity);
        } catch (RuntimeException e) {
            String errorMessage = String.format("Failed to save Notes for client [%s]", entity.getClient().getId());
            throw new LnFException(errorMessage);
        }
    }

    private void save(List<ClientNotes> entities) {
        try {
            repository.saveAll(entities);
        } catch (RuntimeException e) {
            String errorMessage = String.format("Failed to save Notes for client [%s]", entities.get(0).getClient().getId());
            throw new LnFException(errorMessage);
        }
    }

    private Client searchForClient(UUID clientId) {
        return clientRepository.findByClientId(clientId).
                orElseThrow(() -> new LnFEntityNotFoundException(String.format("Client with id [%s] does not exist", clientId)));
    }

    private ClientNotes searchForNotes(UUID notesId) {
        return repository.findById(notesId).
                orElseThrow(() -> new LnFEntityNotFoundException(String.format("Notes with id [%s] does not exist", notesId)));
    }

}
