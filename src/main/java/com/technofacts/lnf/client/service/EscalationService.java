package com.technofacts.lnf.client.service;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import com.technofacts.lnf.client.converter.EscalationConverter;
import com.technofacts.lnf.client.dto.EscalationDto;
import com.technofacts.lnf.client.exception.LnFBadRequestException;
import com.technofacts.lnf.client.exception.LnFEntityNotFoundException;
import com.technofacts.lnf.client.exception.LnFException;
import com.technofacts.lnf.client.model.Client;
import com.technofacts.lnf.client.model.Escalation;
import com.technofacts.lnf.client.repository.ClientRepository;
import com.technofacts.lnf.client.repository.EscalationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
@Log
public class EscalationService {

    private final EscalationRepository repository;
    private final ClientRepository clientRepository;

    public List<EscalationDto> findAll() {
        List<Escalation> entities = repository.findAll();
        return entities.stream().map(EscalationConverter::toTransportModel).filter(Objects::nonNull).collect(Collectors.toList());
    }

    public EscalationDto findByClientId(UUID clientId) {
        searchForClient(clientId);
        Escalation entity = repository.findByClientId(clientId)
                .orElseThrow(() -> new LnFEntityNotFoundException(String.format("Escalation for client [%s] does not exist", clientId)));
        return EscalationConverter.toTransportModel(entity);
    }

    public EscalationDto findById(UUID clientId, UUID escalationId) {
        searchForClient(clientId);
        return EscalationConverter.toTransportModel(searchForEscalation(escalationId));
    }

    public void create(UUID clientId, EscalationDto resource) {
        LnFBadRequestException.throwOnCondition(Objects::isNull, resource, String.format("Failed to create Escalation for client[%s] with null payload", clientId));
        Client clientEntity = searchForClient(clientId);
        Escalation entity = EscalationConverter.toEntityModel(resource);
        entity.setClient(clientEntity);
        save(entity);
        log.info(() -> String.format("Escalation for Client[%s] successfully created", clientId));
    }

    public void update(UUID clientId, UUID escalationId, EscalationDto resource) {
        LnFBadRequestException.throwOnCondition(Objects::isNull, resource, String.format("Failed to update Escalation for client[%s] with null payload", clientId));
        searchForClient(clientId);
        Escalation entity = searchForEscalation(escalationId);
        Escalation updatedEntity = EscalationConverter.toEntityModel(resource, entity);
        save(updatedEntity);
        log.info(() -> String.format("Escalation for Client[%s] successfully created", clientId));
    }

    public void deleteById(UUID clientId, UUID escalationId) {
        searchForClient(clientId);
        Escalation entity = searchForEscalation(escalationId);
        try {
            repository.delete(entity);
            log.info(() -> String.format("Escalation[%s] for client [%s] successfully deleted", escalationId, clientId));
        } catch (RuntimeException e) {
            String errorMessage = String.format("Failed to delete Escalation[[%s] for client [%s]", escalationId, clientId);
            throw new LnFException(errorMessage);
        }
    }

    public void deleteByClientId(UUID clientId) {
        searchForClient(clientId);
        Escalation entity = repository.findByClientId(clientId)
                .orElseThrow(() -> new LnFEntityNotFoundException(String.format("Escalation for client [%s] does not exist", clientId)));
        try {
            repository.delete(entity);
            log.info(() -> String.format("Escalation for Client[%s] successfully deleted", clientId));
        } catch (RuntimeException e) {
            String errorMessage = String.format("Failed to delete Escalation for client [%s]", clientId);
            throw new LnFException(errorMessage);
        }

    }

    private void save(Escalation entity) {
        try {
            repository.save(entity);
        } catch (RuntimeException e) {
            String errorMessage = String.format("Failed to save Escalation for client [%s]", entity.getClient().getId());
            throw new LnFException(errorMessage);
        }
    }

    private Client searchForClient(UUID clientId) {
        return clientRepository.findById(clientId).
                orElseThrow(() -> new LnFEntityNotFoundException(String.format("Client with id [%s] does not exist", clientId)));
    }

    private Escalation searchForEscalation(UUID escalationId) {
        return repository.findById(escalationId).
                orElseThrow(() -> new LnFEntityNotFoundException(String.format("Escalation with id [%s] does not exist", escalationId)));
    }

}

