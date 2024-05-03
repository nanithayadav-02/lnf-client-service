package com.technofacts.lnf.client.service;

import com.google.common.collect.Lists;
import com.technofacts.lnf.client.converter.ProjectConverter;
import com.technofacts.lnf.client.model.Client;
import com.technofacts.lnf.client.model.Project;
import com.technofacts.lnf.client.repository.ClientRepository;
import com.technofacts.lnf.client.repository.ProjectRepository;
import com.technofacts.lnf.dto.client.ClientEmployeeDto;
import com.technofacts.lnf.dto.client.ProjectDto;
import com.technofacts.lnf.dto.client.ProjectOverviewDto;
import com.technofacts.lnf.exception.LnFBadRequestException;
import com.technofacts.lnf.exception.LnFEntityNotFoundException;
import com.technofacts.lnf.exception.LnFException;
import com.technofacts.lnf.service.common.page.PaginatedAndSortedService;
import com.technofacts.lnf.service.specification.GenericSpecificationBuilder;
import com.technofacts.lnf.util.RestUtil;
import com.technofacts.lnf.util.specification.SpecificationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
@Log
public class ProjectService implements PaginatedAndSortedService<ProjectOverviewDto> {

    private final ProjectRepository repository;
    private final ClientRepository clientRepository;
    private final ProjectEmployeeService projectEmployeeService;
    private final TaskService taskService;
    private final CacheManager cacheManager;

    /**
     * Return requested page with list of ProjectDto objects with requested size. Raises LnFEntityNotFoundException
     * if the requested page is more than the total number of pages.
     *
     * @param page Requested Page Number
     * @param size Requested size in the page
     * @return A Page object with projectDto
     */
    @Override
    @Cacheable(value = "projects")
    public Page<ProjectOverviewDto> findPaginated(final int page, final int size) {
        Page<Project> resultPage = repository.findAll(PageRequest.of(page, size));
        return validateAndGetPages(page, resultPage);
    }

    /**
     * Return requested page with sorted list of ProjectDto objects with requested size. Raises LnFEntityNotFoundException
     * if the requested page is more than the total number of pages.
     *
     * @param page      Requested Page Number
     * @param size      Requested size in the page
     * @param sortBy    sorting parameter
     * @param sortOrder sort order ASC or DESC
     * @return A Page object with sorted projectDtos
     */
    @Override
    @Cacheable(value = "projects")
    public Page<ProjectOverviewDto> findPaginatedAndSorted(int page, int size, String sortBy, String sortOrder) {
        final Sort sortInfo = RestUtil.constructSort(sortBy, sortOrder);
        Page<Project> resultPage = repository.findAll(PageRequest.of(page, size, sortInfo));
        return validateAndGetPages(page, resultPage);
    }

    /**
     * Return sorted list of all ProjectDto objects
     *
     * @param sortBy    sorting parameter
     * @param sortOrder sort order ASC or DESC
     * @return Sorted list of all ProjectDto objects.
     */
    @Override
    @Cacheable(value = "projects")
    public List<ProjectOverviewDto> findAllSorted(String sortBy, String sortOrder) {
        final Sort sortInfo = RestUtil.constructSort(sortBy, sortOrder);
        List<Project> entities = Lists.newArrayList(repository.findAll(sortInfo));
        return entities.stream().map(ProjectConverter::toMiniTransportModel).filter(Objects::nonNull).toList();
    }

    /**
     * Return list of all ProjectDto objects
     *
     * @return List of all ProjectDto objects.
     */
    @Override
    @Cacheable(value = "projects")
    public List<ProjectOverviewDto> findAll() {
        List<Project> entities = repository.findAll();
        return entities.stream().map(ProjectConverter::toMiniTransportModel)
                .filter(Objects::nonNull)
                .toList();
    }

    /**
     * Return list of all ProjectDto objects matching the search query.
     *
     * @return List of all ProjectDto objects.
     */
    @Cacheable(value = "projects")
    public List<ProjectDto> findAll(String search) {
        Specification<Project> specification = buildProjectSpecification(search);
        List<Project> entities = repository.findAll(specification);
        return convertToDtos(entities);
    }

    private List<ProjectDto> convertToDtos(List<Project> entities) {
        return entities.stream()
                .map(ProjectConverter::toTransportModel)
                .filter(Objects::nonNull)
                .toList();
    }

    private Specification<Project> buildProjectSpecification(String search) {
        GenericSpecificationBuilder<Project> clientBuilder = new GenericSpecificationBuilder<>();
        Function<String, Class<?>> fieldClassForProject = this::getFieldClassFromProject;
        return SpecificationUtil.buildSpecification(search, clientBuilder, fieldClassForProject);
    }

    private Class<?> getFieldClassFromProject(String fieldName) {
        return SpecificationUtil.getFieldClass(Project.class, fieldName);
    }

        /**
     * Returns projectDto from the projectId. Raises LnFEntityNotFoundException
     * if there is no project with the input projectId
     *
     * @param projectId Project Id
     * @return ProjectDto object
     */
    public ProjectDto findByProjectId(UUID projectId) {
        Project entity = search(projectId);
        return ProjectConverter.toTransportModel(entity);
    }

    /**
     * Returns List of projectDto associated to the client. Raises LnFEntityNotFoundException
     * if there is no client with the input clientId
     *
     * @param clientId Project Id
     * @return List of ProjectDto objects associated to the client.
     */
    public List<ProjectDto> findProjectsByClientId(UUID clientId) {
        searchForClient(clientId);
        List<Project> projects = repository.findByClientId(clientId);
        return projects.stream().map(ProjectConverter::toTransportModel)
                .filter(Objects::nonNull)
                .toList();
    }

    /**
     * Returns a list of EmployeeDto objects associated with the given clientId.
     * Raises a LnFEntityNotFoundException if there is no client with the input clientId.
     *
     * @param clientId The UUID of the client.
     * @return A list of EmployeeDto objects associated with the client.
     */
    @Cacheable(value = "clients")
    public List<ClientEmployeeDto> findEmployeesByClientId(UUID clientId) {
        searchForClient(clientId);
        List<Project> projects = repository.findByClientId(clientId);

        return projects.stream()
                .filter(Objects::nonNull)
                .flatMap(project ->
                        projectEmployeeService.findEmployeesByProjectId(project.getId())
                                .getEmployees().stream())
                .toList();
    }

    /**
     * Creates the project
     *
     * @param resource projectDto object
     */
    @CacheEvict(value = "projects", allEntries = true)
    public void create(ProjectDto resource) {
        LnFBadRequestException.throwOnCondition(Objects::isNull, resource, "Failed to create Project with null payload");
        Project entity = ProjectConverter.toEntityModel(resource);
        if (resource.getClientId() != null) {
           Client client = searchForClient(resource.getClientId());
           entity.setClient(client);
        }
        saveEntity(entity);
        log.info(() -> String.format("Project[%s] successfully created", entity.getCode()));
    }

    /**
     * Updates the project
     *
     * @param projectId Project Id
     * @param resource ProjectDto
     */
    @Transactional
    @CacheEvict(value = "projects", allEntries = true)
    public void update(UUID projectId, ProjectDto resource) {
        LnFBadRequestException.throwOnCondition(Objects::isNull, resource, "Failed to update Project with null payload");
        Project entity = search(projectId);
        Project updatedEntity = ProjectConverter.toEntityModel(resource, entity);
        // Set the client
        if (resource.getClientId() != null) {
            Client client = searchForClient(resource.getClientId());
            updatedEntity.setClient(client);
        } else {
            updatedEntity.setClient(null);
        }
        saveEntity(updatedEntity);
        log.info(() -> String.format("Project[%s] successfully updated", projectId));
    }

    /**
     * Deletes the project
     *
     * @param projectId Project Id
     */
    @CacheEvict(value = "projects", allEntries = true)
    public void delete(UUID projectId) {
        Project entity = search(projectId);
        deleteRelatedEntities(entity);
        deleteProject(entity);
    }

    private void deleteRelatedEntities(Project project) {
        projectEmployeeService.deleteProjectEmployeesByProject(project);
        taskService.deleteByProjectId(project.getId());
    }

    private void deleteProject(Project entity) {
        try {
            repository.delete(entity);
            log.info(() -> String.format("Project[%s] successfully deleted", entity.getCode()));
        } catch (RuntimeException e) {
            String errorMessage = String.format("Failed to delete Project [%s]", entity.getCode());
            throw new LnFException(errorMessage, e);
        }
    }

    /**
     * Clears the cache for projects.
     */
    public void clearProjectsCache() {
        Objects.requireNonNull(cacheManager.getCache("projects")).clear();
        log.info("Projects cache cleared.");
    }

    private Page<ProjectOverviewDto> validateAndGetPages(int page, Page<Project> resultPage) {
        if (page > resultPage.getTotalPages()) {
            throw new LnFEntityNotFoundException(String.format("Total number of pages [%d], requested page [%d] does not exist", resultPage.getTotalPages(), page));
        }
        return resultPage.map(ProjectConverter::toMiniTransportModel);
    }

    private void saveEntity(Project entity) {
        try {
            repository.save(entity);
        } catch (RuntimeException e) {
            String errorMessage = String.format("Failed to save Project [%s]", entity.getClient().getId());
            throw new LnFException(errorMessage);
        }
    }

    private Client searchForClient(UUID clientId) {
        return clientRepository.findById(clientId).
                orElseThrow(() -> new LnFEntityNotFoundException(String.format("Client with id [%s] does not exist", clientId)));
    }

    private Project search(UUID projectId) {
        return repository.findById(projectId).
                orElseThrow(() -> new LnFEntityNotFoundException(String.format("Project with id [%s] does not exist", projectId)));
    }

}

