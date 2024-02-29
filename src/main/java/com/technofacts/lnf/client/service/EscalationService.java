package com.technofacts.lnf.client.service;

import com.technofacts.lnf.client.converter.EscalationConverter;
import com.technofacts.lnf.client.model.Client;
import com.technofacts.lnf.client.model.Escalation;
import com.technofacts.lnf.client.repository.ClientRepository;
import com.technofacts.lnf.client.repository.EscalationRepository;
import com.technofacts.lnf.dto.client.EscalationDto;
import com.technofacts.lnf.exception.LnFBadRequestException;
import com.technofacts.lnf.exception.LnFEntityNotFoundException;
import com.technofacts.lnf.exception.LnFException;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
@Log
public class EscalationService {

    private final EscalationRepository repository;
    private final ClientRepository clientRepository;

    /**
     * Returns EscalationDto for the client address by clientId
     *
     * @param clientId Client Id
     * @return EscalationDto of the client escalation
     */
    public List<EscalationDto> findByClientId(UUID clientId) {
        searchForClient(clientId);
        List<Escalation> entities = repository.findByClientId(clientId);
        return entities.stream().map(EscalationConverter::toTransportModel).filter(Objects::nonNull).toList();
    }

    /**
     * Returns EscalationDto of the Client by clientId and addressId
     *
     * @param clientId     Client Id
     * @param escalationId Escalation ID
     * @return EscalationDto of the client escalation
     */
    public EscalationDto findById(UUID clientId, UUID escalationId) {
        searchForClient(clientId);
        return EscalationConverter.toTransportModel(searchForEscalation(escalationId));
    }

    /**
     * Creates the escalation for the client
     *
     * @param clientId Client Id
     * @param resource EscalationDto
     */
    public void create(UUID clientId, List<EscalationDto> resource) {
        LnFBadRequestException.throwOnCondition(Objects::isNull, resource, String.format("Failed to create escalation for client [%s] with null payload", clientId));
        Client clientEntity = searchForClient(clientId);
        List<Escalation> entities = new ArrayList<> ();
        resource.stream().filter(Objects::nonNull).forEach(escalationDto -> {
            Escalation entity = EscalationConverter.toEntityModel(escalationDto);
            entity.setClient(clientEntity);
            entities.add(entity);
        });
        save(entities);
        log.info(() -> String.format("Escalation for Client[%s] successfully created", clientId));
    }

    /**
     * Updates the escalation for the client
     *
     * @param clientId     Client Id
     * @param escalationId Escalation ID
     * @param resource     EscalationDto
     */
    public void update(UUID clientId, UUID escalationId, EscalationDto resource) {
        LnFBadRequestException.throwOnCondition(Objects::isNull, resource, String.format("Failed to update Escalation for client[%s] with null payload", clientId));
        searchForClient(clientId);
        Escalation entity = searchForEscalation(escalationId);
        Escalation updatedEntity = EscalationConverter.toEntityModel(resource, entity);
        save(updatedEntity);
        log.info(() -> String.format("Escalation for Client[%s] successfully created", clientId));
    }

    /**
     * Deletes the client escalation by clientId
     *
     * @param clientId Client Id
     */
    public void deleteByClientId(UUID clientId) {
        searchForClient(clientId);
        List<Escalation> entities = repository.findByClientId(clientId);
        try {
            repository.deleteAll(entities);
            log.info(() -> String.format("Escalation for Client[%s] successfully deleted", clientId));
        } catch (RuntimeException e) {
            String errorMessage = String.format("Failed to delete Escalation for client [%s]", clientId);
            throw new LnFException(errorMessage);
        }
    }

    /**
     * Deletes the client escalation by clientId and escalationId
     *
     * @param clientId     Client Id
     * @param escalationId Escalation Id
     */
    public void deleteById (UUID clientId, UUID escalationId) {
        searchForClient (clientId);
        Escalation entity = searchForEscalation (escalationId);
        try {
            repository.delete (entity);
            log.info (() -> String.format ("Escalation[%s] for client [%s] successfully deleted", escalationId, clientId));
        } catch (RuntimeException e) {
            String errorMessage = String.format ("Failed to delete Escalation[[%s] for client [%s]", escalationId, clientId);
            throw new LnFException (errorMessage);
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

    private void save(List<Escalation> entities) {
        try {
            repository.saveAll(entities);
        } catch (RuntimeException e) {
            String errorMessage = String.format("Failed to save escalation for employee [%s]", entities.get(0).getClient().getId());
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

