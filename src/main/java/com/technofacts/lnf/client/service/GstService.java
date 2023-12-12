package com.technofacts.lnf.client.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import com.technofacts.lnf.client.converter.GstConverter;
import com.technofacts.lnf.client.model.Client;
import com.technofacts.lnf.client.model.Gst;
import com.technofacts.lnf.client.repository.ClientRepository;
import com.technofacts.lnf.client.repository.GstRepository;
import com.technofacts.lnf.dto.client.GstDto;
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
public class GstService {

    private final GstRepository repository;
    private final ClientRepository clientRepository;

    /**
     * Returns GstDto for the client gst by clientId
     *
     * @param clientId Client Id
     * @return GstDto of the client gst
     */
    public List<GstDto> findByClientId(UUID clientId) {
        searchForClient(clientId);
        List<Gst> entities = repository.findByClientId(clientId);
        return entities.stream().map(GstConverter::toTransportModel).filter(Objects::nonNull).toList();
    }

    /**
     * Returns GstDto of the Client by clientId and gstId
     *
     * @param clientId Client Id
     * @param gstId    GST ID
     * @return GstDto of the client gst
     */
    public GstDto findById(UUID clientId, UUID gstId) {
        searchForClient(clientId);
        return GstConverter.toTransportModel(searchForGst(gstId));
    }

    /**
     * Creates the gst for the client
     *
     * @param clientId Client Id
     * @param resource List<GstDto>
     */
    public void create(UUID clientId, List<GstDto> resource) {
        LnFBadRequestException.throwOnCondition(Objects::isNull, resource, String.format("Failed to create gst for client [%s] with null payload", clientId));
        Client clientEntity = searchForClient(clientId);
        List<Gst> entities = new ArrayList<>();
        resource.stream().filter(Objects::nonNull).forEach(gstDto -> {
            Gst entity = GstConverter.toEntityModel(gstDto);
            entity.setClient(clientEntity);
            entities.add(entity);
        });
        save(entities);
        log.info(() -> String.format("Gst for client[%s] successfully created", clientId));
    }

    /**
     * Creates the gst for the client
     *
     * @param clientId Client Id
     * @param resource GstDto
     */
    public void create(UUID clientId, GstDto resource) {
        LnFBadRequestException.throwOnCondition(Objects::isNull, resource, String.format("Failed to create gst for client[%s] with null payload", clientId));
        Client clientEntity = searchForClient(clientId);
        Gst entity = GstConverter.toEntityModel(resource);
        entity.setClient(clientEntity);
        save(entity);
        log.info(() -> String.format("Gst for client[%s] successfully created", clientId));
    }

    /**
     * Updates the gst for the client
     *
     * @param clientId Client Id
     * @param gstId    GST ID
     * @param resource GstDto
     */
    public void update(UUID clientId, UUID gstId, GstDto resource) {
        LnFBadRequestException.throwOnCondition(Objects::isNull, resource, String.format("Failed to gst client[%s] with null payload", clientId));
        searchForClient(clientId);
        Gst entity = searchForGst(gstId);
        Gst updatedEntity = GstConverter.toEntityModel(resource, entity);
        save(updatedEntity);
        log.info(() -> String.format("Gst for client[%s] successfully updated", clientId));
    }

    /**
     * Deletes the client gst by clientId
     *
     * @param clientId Client Id
     */
    public void deleteByClientId(UUID clientId) {
        searchForClient(clientId);
        List<Gst> entities = repository.findByClientId(clientId);
        try {
            repository.deleteAll(entities);
            log.info(() -> String.format("Gsts for client[%s] successfully deleted", clientId));
        } catch (RuntimeException e) {
            String errorMessage = String.format("Failed to delete gsts for client [%s]", clientId);
            throw new LnFException(errorMessage);
        }
    }

    /**
     * Deletes the client gst by clientId and gstId
     *
     * @param clientId Client Id
     * @param gstId    GST Id
     */
    public void deleteById(UUID clientId, UUID gstId) {
        searchForClient(clientId);
        Gst entity = searchForGst(gstId);
        try {
            repository.delete(entity);
            log.info(() -> String.format("Gst[%s] for client [%s] successfully deleted", gstId, clientId));
        } catch (RuntimeException e) {
            String errorMessage = String.format("Failed to delete gst[%s] for client [%s]", gstId, clientId);
            throw new LnFException(errorMessage);
        }
    }

    private void save(Gst entity) {
        try {
            repository.save(entity);
        } catch (RuntimeException e) {
            String errorMessage = String.format("Failed to save gst for client [%s]", entity.getClient().getId());
            throw new LnFException(errorMessage);
        }
    }

    private void save(List<Gst> entities) {
        try {
            repository.saveAll(entities);
        } catch (RuntimeException e) {
            String errorMessage = String.format("Failed to save gst for employee [%s]", entities.get(0).getClient().getId());
            throw new LnFException(errorMessage);
        }
    }

    private Client searchForClient(UUID clientId) {
        return clientRepository.findById(clientId).
                orElseThrow(() -> new LnFEntityNotFoundException(String.format("Client with id [%s] does not exist", clientId)));
    }

    private Gst searchForGst(UUID gstId) {
        return repository.findById(gstId).
                orElseThrow(() -> new LnFEntityNotFoundException(String.format("Gst with id [%s] does not exist", gstId)));
    }
}
