package com.technofacts.lnf.client.service;

import com.google.common.collect.Lists;
import com.technofacts.lnf.client.converter.ClientConverter;
import com.technofacts.lnf.client.model.Client;
import com.technofacts.lnf.client.repository.ClientRepository;
import com.technofacts.lnf.client.repository.specification.client.ClientSpecificationBuilder;
import com.technofacts.lnf.dto.client.ClientDto;
import com.technofacts.lnf.exception.LnFBadRequestException;
import com.technofacts.lnf.exception.LnFEntityNotFoundException;
import com.technofacts.lnf.exception.LnFException;
import com.technofacts.lnf.util.RestUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Transactional
@RequiredArgsConstructor
@Log
public class ClientService {

    private static final String SEARCH_REGEX_PATTERN = "([\\w]+)\\s*:\\s*([\\w.@\\- ]+?)(?=(,|$))";

    private final ClientRepository repository;

    /**
     * Return requested page with list of ClientDto objects with requested size. Raises LnFEntityNotFoundException
     * if the requested page is more than the total number of pages.
     *
     * @param page Requested Page Number
     * @param size Requested size in the page
     * @return A Page object with clientDtos
     */
    public Page<ClientDto> findPaginated(final int page, final int size) {
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
    public Page<ClientDto> findPaginatedAndSorted(int page, int size, String sortBy, String sortOrder) {
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
    public List<ClientDto> findAllSorted(String sortBy, String sortOrder) {
        final Sort sortInfo = RestUtil.constructSort(sortBy, sortOrder);
        List<Client> entities = Lists.newArrayList(repository.findAll(sortInfo));
        return entities.stream().map(ClientConverter::toTransportModel)
                .filter(Objects::nonNull)
                .toList();
    }

    /**
     * Return list of all ClientDto objects
     *
     * @return List of all ClientDto objects.
     */
    public List<ClientDto> findAll() {
        List<Client> entities = repository.findAll();
        return entities.stream().map(ClientConverter::toTransportModel)
                .filter(Objects::nonNull)
                .toList();
    }

    /**
     * Returns clientDto from the clientId. Raises LnFEntityNotFoundException
     * if there is no client with the input clientId
     *
     * @param clientId Client Id
     * @return ClientDto object
     */
    public ClientDto findByClientId(UUID clientId) {
        Client entity = search(clientId);
        return ClientConverter.toTransportModel(entity);
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
        log.info(() -> String.format("Client[%s] successfully created", entity.getCode()));
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
        log.info(() -> String.format("Client[%s] successfully updated", clientId));
    }

    /**
     * Deletes the client
     *
     * @param clientId Client Id
     */
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

    private Page<ClientDto> validateAndGetPages(int page, Page<Client> resultPage) {
        if (page > resultPage.getTotalPages()) {
            throw new LnFEntityNotFoundException(String.format("Total number of pages [%d], " +
                    "requested page [%d] does not exist", resultPage.getTotalPages(), page));
        }
        return resultPage.map(ClientConverter::toTransportModel);
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
    private Client search(UUID clientId) {
        return repository.findById(clientId).
                orElseThrow(() -> new LnFEntityNotFoundException(String.format("Client with id [%s] does not exist", clientId)));
    }


    /**
     * Return list of all ClientDto objects matching the search query.
     *
     * @return List of all ClientDto objects.
     */
    public List<ClientDto> findAll(String search) {
        Specification<Client> specification = buildClientSpecification(search);
        List<Client> entities = repository.findAll(specification);
        return convertToDtos(entities);
    }
    private List<ClientDto> convertToDtos(List<Client> entities) {
        return entities.stream()
                .map(ClientConverter::toTransportModel)
                .filter(Objects::nonNull)
                .toList();
    }
    private Specification<Client> buildClientSpecification(String search) {
        final ClientSpecificationBuilder builder = new ClientSpecificationBuilder();
        createSearchConditions(search, builder);
        return builder.build();
    }

    private void createSearchConditions(String search, ClientSpecificationBuilder builder) {
        log.info(() -> String.format("search [%s]", search));
        log.info(() -> String.format("builder [%s]", builder.toString()));
        final Matcher matcher = Pattern.compile(SEARCH_REGEX_PATTERN, Pattern.CASE_INSENSITIVE).matcher(search);
        while (matcher.find()) {
            processSearchGroup(matcher, builder);
        }
    }

    private void processSearchGroup(final Matcher matcher, final ClientSpecificationBuilder builder) {
        String key = matcher.group(1).trim();
        String value = matcher.group(2).trim();
        log.info("Key: " + key + ", Value: " + value);
        Class<?> fieldType = getFieldClass(key);
        addCondition(builder, key, fieldType, value);
    }

    private void addCondition(final ClientSpecificationBuilder builder, final String key, Class<?> fieldType, final String value) {
        if (fieldType != null) {
            Object convertedValue = convertToFieldType(fieldType, value);
            builder.with(key, ":", convertedValue);
        }
    }
    private Class<?> getFieldClass(String fieldName) {
        try {
            Class<?> clazz = Class.forName("com.technofacts.lnf.client.model.Client");
            Field field = clazz.getDeclaredField(fieldName);
            return field.getType();
        } catch (ClassNotFoundException | NoSuchFieldException e) {
            return null;
        }
    }

    private Object convertToFieldType(Class<?> fieldType, String value) {
        if (fieldType.isEnum()) {
            return getEnumConstant(fieldType, value);
        } else if (fieldType == Integer.class || fieldType == int.class) {
            return Integer.valueOf(value);
        } else {
            return value;
        }
    }

    private Enum<?> getEnumConstant(Class<?> fieldType, String value) {
        String uppercaseValue = value.toUpperCase();
        for (Enum<?> enumConstant : ((Class<? extends Enum>) fieldType).getEnumConstants()) {
            if (enumConstant.name().toUpperCase().equals(uppercaseValue)) {
                return enumConstant;
            }
        }
        return null;
    }
}

