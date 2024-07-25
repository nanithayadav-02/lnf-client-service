package com.technofacts.lnf.client.service;

import com.google.common.collect.Lists;
import com.technofacts.lnf.client.converter.ClientConverter;
import com.technofacts.lnf.client.model.Client;
import com.technofacts.lnf.client.model.enums.DocumentType;
import com.technofacts.lnf.client.repository.ClientRepository;
import com.technofacts.lnf.dto.client.ClientDto;
import com.technofacts.lnf.dto.client.ClientOverviewDto;
import com.technofacts.lnf.dto.client.ProjectDto;
import com.technofacts.lnf.dto.common.PageRequestDto;
import com.technofacts.lnf.exception.LnFBadRequestException;
import com.technofacts.lnf.exception.LnFEntityNotFoundException;
import com.technofacts.lnf.exception.LnFException;
import com.technofacts.lnf.service.common.page.PaginatedAndSortedService;
import com.technofacts.lnf.service.specification.GenericSpecificationBuilder;
import com.technofacts.lnf.util.RestUtil;
import com.technofacts.lnf.util.specification.SpecificationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;

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
    @Cacheable(value = "clients")
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
    @Cacheable(value = "clients")
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
    @Cacheable(value = "clients")
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
    @Cacheable(value = "clients")
    public List<ClientOverviewDto> findAll() {
        List<Client> entities = repository.findAll();
        return entities.stream().map(ClientConverter::toMiniTransportModel)
                .filter(Objects::nonNull)
                .toList();
    }

    @Cacheable(value = "clients")
    public List<ClientDto> findAll(String search) {
        Specification<Client> specification = buildClientSpecification(search);
        List<Client> entities = repository.findAll(specification);
        return convertToDtos(entities);
    }

    /**
     * Returns clientDto from the clientId. Raises LnFEntityNotFoundException
     * if there is no client with the input clientId
     *
     * @param clientId Client Id
     * @return ClientDto object
     */
    @Cacheable(value = "clients")
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
    @CacheEvict(value = "clients", allEntries = true)
    public void create(ClientDto resource) {
        LnFBadRequestException.throwOnCondition(Objects::isNull, resource,
                "Failed to create Client with null payload");
        Client entity = ClientConverter.toEntityModel(resource);
        saveEntity(entity);
        log.error("Client {} successfully created", entity.getCode());
    }

    /**
     * Updates the client
     *
     * @param clientId Client Id
     * @param resource ClientDto
     */
    @Transactional
    @CacheEvict(value = "clients", allEntries = true)
    public void update(UUID clientId, ClientDto resource) {
        LnFBadRequestException.throwOnCondition(Objects::isNull, resource,
                "Failed to update Client with null payload");
        Client entity = search(clientId);
        Client updatedEntity = ClientConverter.toEntityModel(resource, entity);
        saveEntity(updatedEntity);
        log.error("Client {} successfully updated", clientId);
    }

    /**
     * Deletes the client
     *
     * @param clientId Client Id
     */
    @CacheEvict(value = "clients", allEntries = true)
    public void delete(UUID clientId) {
        Client entity = search(clientId);
        List<ProjectDto> projectList = projectService.findProjectsByClientId(clientId);
        projectList.forEach(project -> projectService.delete(project.getId()));
        try {
            repository.delete(entity);
            log.error("Client {} successfully deleted", entity.getCode());
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
        log.error("Clients cache cleared.");
    }

    private Page<ClientOverviewDto> validateAndGetPages(int page, Page<Client> resultPage) {
        if (page > resultPage.getTotalPages()) {
            throw new LnFEntityNotFoundException(String.format("Total number of pages [%d], " +
                    "requested page [%d] does not exist", resultPage.getTotalPages(), page));
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
    private Client search(UUID clientId) {
        return repository.findById(clientId).
                orElseThrow(() -> new LnFEntityNotFoundException(String.format("Client with id [%s] does not exist", clientId)));
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

}

