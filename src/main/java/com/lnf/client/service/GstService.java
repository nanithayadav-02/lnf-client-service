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

import com.lnf.client.converter.GstConverter;
import com.lnf.client.repository.ClientRepository;
import com.lnf.client.repository.GstRepository;
import com.lnf.client.model.Client;
import com.lnf.client.model.Gst;
import com.lnf.dto.client.GstDto;
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
        log.debug("Gst for client {} successfully created", clientId);
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
        log.debug("Gst for client {} successfully created", clientId);
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
        log.debug("Gst for client {} successfully updated", clientId);
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
            log.debug("Gsts for client {} successfully deleted", clientId);
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
            log.debug("Gst {} for client {} successfully deleted", gstId, clientId);
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
