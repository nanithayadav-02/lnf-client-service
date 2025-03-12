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

import com.google.common.collect.Lists;
import com.lnf.client.converter.ClientConverter;
import com.lnf.client.model.Client;
import com.lnf.client.model.enums.DocumentType;
import com.lnf.client.repository.ClientRepository;
import com.lnf.dto.client.ClientDto;
import com.lnf.dto.client.ClientOverviewDto;
import com.lnf.dto.client.ProjectOverviewDto;
import com.lnf.dto.common.PageRequestDto;
import com.lnf.exception.LnFBadRequestException;
import com.lnf.exception.LnFEntityNotFoundException;
import com.lnf.exception.LnFException;
import com.lnf.service.common.page.PaginatedAndSortedService;
import com.lnf.service.specification.GenericSpecificationBuilder;
import com.lnf.util.RestUtil;
import com.lnf.util.specification.SpecificationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ClientService implements PaginatedAndSortedService<ClientOverviewDto> {

    private final ClientRepository repository;
    private final ProjectService projectService;
    private final CacheManager cacheManager;
    private final DocumentService documentService;

    /**
     * Return requested page with list of ClientDto objects with requested size. Raises LnFEntityNotFoundException
     * if the requested page is more than the total number of pages.
     *
     * @param page Requested Page Number
     * @param size Requested size in the page
     * @return A Page object with clientDtos
     */
    @Override
    public Page<ClientOverviewDto> findPaginated(final int page, final int size) {
        Page<Client> resultPage = repository.findAll(PageRequest.of(page, size));
        return validateAndGetPages(page, resultPage);
    }

    /**
     * Return requested page with sorted list of ClientDto objects with requested size. Raises LnFEntityNotFoundException
     * if the requested page is more than the total number of pages.
     *
     * @param page      Requested Page Number
     * @param size      Requested size in the page
     * @param sortBy    sorting parameter
     * @param sortOrder sort order ASC or DESC
     * @return A Page object with sorted clientDtos
     */
    @Override
    public Page<ClientOverviewDto> findPaginatedAndSorted(int page, int size, String sortBy, String sortOrder) {
        final Sort sortInfo = RestUtil.constructSort(sortBy, sortOrder);
        Page<Client> resultPage = repository.findAll(PageRequest.of(page, size, sortInfo));
        return validateAndGetPages(page, resultPage);
    }

    /**
     * Return sorted list of all ClientDto objects
     *
     * @param sortBy    sorting parameter
     * @param sortOrder sort order ASC or DESC
     * @return Sorted list of all ClientDto objects.
     */
    @Override
    public List<ClientOverviewDto> findAllSorted(String sortBy, String sortOrder) {
        final Sort sortInfo = RestUtil.constructSort(sortBy, sortOrder);
        List<Client> entities = Lists.newArrayList(repository.findAll(sortInfo));
        return entities.stream().map(ClientConverter::toMiniTransportModel)
                .filter(Objects::nonNull)
                .toList();
    }

    /**
     * Return list of all ClientDto objects
     *
     * @return List of all ClientDto objects.
     */
    @Override
    public List<ClientOverviewDto> findAll() {
        List<Client> entities = repository.findAll();
        return entities.stream().map(ClientConverter::toMiniTransportModel)
                .filter(Objects::nonNull)
                .toList();
    }

    public List<ClientDto> findAll(String search) {
        Specification<Client> specification = buildClientSpecification(search);
        List<Client> entities = repository.findAll(specification);
        return convertToDtos(entities);
    }

    /**
     * Returns clientDto from the clientId. Raises LnFEntityNotFoundException
     * if there is no client with the input clientId

     * @param clientId Client Id
     * @return ClientDto object
     */
    public ClientDto findByClientId(UUID clientId) {
        Client entity = search(clientId);
        return findClientWithDocument(entity);
    }

    public Page<ClientDto> findingAllWithPagination(String search, PageRequestDto pageRequestDto) {
        Pageable pageable = PageRequest.of(pageRequestDto.getPage(), pageRequestDto.getSize(),
                RestUtil.constructSort(pageRequestDto.getSortBy(), pageRequestDto.getSortOrder()));
        Specification<Client> specification = buildClientSpecification(search);
        Page<Client> resultPage = repository.findAll(specification, pageable);
        return resultPage.map(ClientConverter::toTransportModel);
    }

    /**
     * Creates the client
     *
     * @param resource clientDto object
     */
    public void create(ClientDto resource) {
        LnFBadRequestException.throwOnCondition(Objects::isNull, resource,
                "Failed to create Client with null payload");
        Client entity = ClientConverter.toEntityModel(resource);
        saveEntity(entity);
        log.debug("Client {} successfully created", entity.getCode());
    }

    /**
     * Updates the client
     *
     * @param clientId Client Id
     * @param resource ClientDto
     */
    @Transactional
    public void update(UUID clientId, ClientDto resource) {
        LnFBadRequestException.throwOnCondition(Objects::isNull, resource,
                "Failed to update Client with null payload");
        Client entity = search(clientId);
        Client updatedEntity = ClientConverter.toEntityModel(resource, entity);
        saveEntity(updatedEntity);
        log.debug("Client {} successfully updated", clientId);
    }

    /**
     * Deletes the client
     *
     * @param clientId Client Id
     */
    public void delete(UUID clientId) {
        Client entity = search(clientId);
        List<ProjectOverviewDto> projectList = projectService.findProjectsByClientId(clientId);
        projectList.forEach(project -> projectService.delete(project.getId()));
        try {
            repository.delete(entity);
            log.debug("Client {} successfully deleted", entity.getCode());
        } catch (RuntimeException e) {
            String errorMessage = String.format("Failed to delete Client [%s]", entity.getCode());
            throw new LnFException(errorMessage, e);
        }
    }

    /**
     * Clears the cache for clients.
     */
    public void clearClientsCache() {
        Objects.requireNonNull(cacheManager.getCache("clients")).clear();
        log.debug("Clients cache cleared.");
    }

    private Page<ClientOverviewDto> validateAndGetPages(int page, Page<Client> resultPage) {
        if (page > resultPage.getTotalPages()) {
            throw new LnFEntityNotFoundException(("Total number of pages [%d], " +
                    "requested page [%d] does not exist").formatted(resultPage.getTotalPages(), page));
        }
        return resultPage.map(ClientConverter::toMiniTransportModel);
    }

    /**
     * Saves the client to the database
     *
     * @param entity Client
     */
    private void saveEntity(Client entity) {
        try {
            repository.save(entity);
        } catch (RuntimeException e) {
            String errorMessage = String.format("Failed to save client [%s]", entity.getCode());
            throw new LnFException(errorMessage, e);
        }
    }

    /**
     * Search and returns the client with id = clientId
     *
     * @param clientId Client Id
     * @return Client object
     */
    public Client search(UUID clientId) {
        return repository.findById(clientId).
                orElseThrow(() -> new LnFEntityNotFoundException("Client with id [%s] does not exist".formatted(clientId)));
    }


    /**
     * Return list of all ClientDto objects matching the search query.
     *
     * @return List of all ClientDto objects.
     */

    private List<ClientDto> convertToDtos(List<Client> entities) {
        return entities.stream()
                .map(ClientConverter::toTransportModel)
                .filter(Objects::nonNull)
                .toList();
    }

    private ClientDto findClientWithDocument(Client entity) {
        ClientDto dto = ClientConverter.toTransportModel(entity);
        if (dto != null) {
            dto.setClientLogo(documentService.findByClientId(dto.getId(), DocumentType.image.name()));
        }
        return dto;
    }

    private Specification<Client> buildClientSpecification(String search) {
        GenericSpecificationBuilder<Client> clientBuilder = new GenericSpecificationBuilder<>();
        Function<String, Class<?>> fieldClassForClient = this::getFieldClassFromClient;
        return SpecificationUtil.buildSpecification(search, clientBuilder, fieldClassForClient);
    }

    private Class<?> getFieldClassFromClient(String fieldName) {
        return SpecificationUtil.getFieldClass(Client.class, fieldName);
    }

    public void deleteLastUploadFile() {
        List<Client> client = repository.findByUploadedTime();
        repository.deleteAll(client);
    }

    public void deleteClientList(List<UUID> clientIds) {
        List<Client> clientList = clientIds.stream()
                .map(id -> repository.findById(id)
                        .orElseThrow(() -> new LnFException("Client not found for id: " + id)))
                .toList();
        repository.deleteAll(clientList);
    }

    public Page<ClientDto> getLastUploadData(PageRequestDto pageRequest) {
        Pageable pageable = createPageable(pageRequest);
        List<Client> entities = repository.findByUploadedTime();
        return paginateClientDetails(convertToDtos(entities), pageable);
    }

    private Pageable createPageable(PageRequestDto pageRequest) {
        return PageRequest.of(
                pageRequest.getPage(),
                pageRequest.getSize(),
                RestUtil.constructSort(pageRequest.getSortBy(), pageRequest.getSortOrder())
        );
    }

    private Page<ClientDto> paginateClientDetails(List<ClientDto> clientDtos, Pageable pageable) {
        int totalRecords = clientDtos.size();
        int start = pageable.getPageSize() * pageable.getPageNumber();
        int end = Math.min(start + pageable.getPageSize(), totalRecords);
        List<ClientDto> paginatedClientDetails = clientDtos.subList(start, end);

        return new PageImpl<>(paginatedClientDetails, pageable, totalRecords);
    }

    public List<ClientDto> create(List<ClientDto> resources) {
        LnFBadRequestException.throwOnCondition(Objects::isNull, resources,
                "Failed to create Client with null payload");
        List<Client> entities = resources.stream().map(ClientConverter::toEntityModel).toList();
        return save(entities);
    }

    private List<ClientDto> save(List<Client> entities) {
        Set<String> existingCodes = repository.findAll().stream()
                .map(Client::getCode).collect(Collectors.toSet());

        List<ClientDto> invalidData = new ArrayList<>();
        LocalDateTime uploadTime = LocalDateTime.now();

        for (Client client : entities) {
            if (existingCodes.contains(client.getCode())) {
                invalidData.add(ClientConverter.toTransportModel(client));
            } else {
                try {
                    client.setUploadTime(uploadTime);
                    repository.save(client);
                } catch (RuntimeException e) {
                    String errorMessage = "Failed to save Projects";
                    throw new LnFException(errorMessage, e);
                }
            }
        }
        return invalidData;
    }

}

