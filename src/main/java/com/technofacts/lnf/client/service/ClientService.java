package com.technofacts.lnf.client.service;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;
import com.technofacts.lnf.client.converter.ClientConverter;
import com.technofacts.lnf.client.dto.ClientDto;
import com.technofacts.lnf.client.exception.LnFBadRequestException;
import com.technofacts.lnf.client.exception.LnFEntityNotFoundException;
import com.technofacts.lnf.client.exception.LnFException;
import com.technofacts.lnf.client.model.Client;
import com.technofacts.lnf.client.repository.ClientRepository;
import com.technofacts.lnf.client.repository.specification.client.ClientSpecificationBuilder;
import com.technofacts.lnf.client.util.RestUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
@Log
public class ClientService {

    private final ClientRepository repository;

    public List<ClientDto> findPaginatedAndSorted(int page, int size, String sortBy, String sortOrder) {
        final Sort sortInfo = RestUtil.constructSort(sortBy, sortOrder);
        Page<Client> resultPage = repository.findAll(PageRequest.of(page, size, sortInfo));
        return validateAndGetPages(page, resultPage);
    }

    public List<ClientDto> findPaginated(int page, int size) {
        Page<Client> resultPage = repository.findAll(PageRequest.of(page, size));
        return validateAndGetPages(page, resultPage);
    }

    public List<ClientDto> findAllSorted(String sortBy, String sortOrder) {
        final Sort sortInfo = RestUtil.constructSort(sortBy, sortOrder);
        List<Client> entities = Lists.newArrayList(repository.findAll(sortInfo));
        return entities.stream().map(ClientConverter::toTransportModel).filter(Objects::nonNull).collect(Collectors.toList());
    }

    public List<ClientDto> findAll() {
        List<Client> entities = repository.findAll();
        return entities.stream().map(ClientConverter::toTransportModel).filter(Objects::nonNull).collect(Collectors.toList());
    }

    public List<ClientDto> findAll(String search) {
        ClientSpecificationBuilder builder = new ClientSpecificationBuilder();
        Pattern pattern = Pattern.compile("(\\w+?)(:|<|>)(\\w+?),");
        Matcher matcher = pattern.matcher(search + ",");
        while (matcher.find()) {
            builder.with(matcher.group(1), matcher.group(2), matcher.group(3));
        }
        Specification<Client> specification = builder.build();
        List<Client> entities = repository.findAll(specification);
        return entities.stream().map(ClientConverter::toTransportModel).filter(Objects::nonNull).collect(Collectors.toList());
    }

    public ClientDto findByClientId(UUID clientId) {
        Client entity = search(clientId);
        return ClientConverter.toTransportModel(entity);
    }

    public void create(ClientDto resource) {
        LnFBadRequestException.throwOnCondition(Objects::isNull, resource, String.format("Failed to create Client with null payload"));
        Client entity = ClientConverter.toEntityModel(resource);
        saveEntity(entity);
        log.info(() -> String.format("Client[%s] successfully created", entity.getCode()));
    }

    @Transactional
    public void update(UUID clientId, ClientDto resource) {
        LnFBadRequestException.throwOnCondition(Objects::isNull, resource, String.format("Failed to update Client with null payload"));
        Client entity = search(clientId);
        Client updatedEntity = ClientConverter.toEntityModel(resource);
        updatedEntity.setId(entity.getId());
        saveEntity(updatedEntity);
        log.info(() -> String.format("Client[%s] successfully updated", clientId));
    }

    public void delete(UUID clientId) {
        Client entity = search(clientId);
        try {
            repository.delete(entity);
            log.info(() -> String.format("Client[%s] successfully deleted", entity.getCode()));
        } catch (RuntimeException e) {
            String errorMessage = String.format("Failed to delete Client [%s]", entity.getCode());
            throw new LnFException(errorMessage, e);
        }
    }

    private List<ClientDto> validateAndGetPages(int page, Page<Client> resultPage) {
        if (page > resultPage.getTotalPages()) {
            throw new LnFEntityNotFoundException(String.format("Total number of pages [%d], requested page [%d] does not exist", resultPage.getTotalPages(), page));
        }
        List<Client> entities = Lists.newArrayList(resultPage.getContent());
        return entities.stream().map(ClientConverter::toTransportModel).filter(Objects::nonNull).collect(Collectors.toList());
    }

    private Client saveEntity(Client entity) {
        try {
            return repository.save(entity);
        } catch (RuntimeException e) {
            String errorMessage = String.format("Failed to save client [%s]", entity.getCode());
            throw new LnFException(errorMessage, e);
        }
    }

    private Client search(UUID clientId) {
        return repository.findById(clientId).
                orElseThrow(() -> new LnFEntityNotFoundException(String.format("Client with id [%s] does not exist", clientId)));
    }
}

