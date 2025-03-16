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
import com.lnf.client.converter.ProjectConverter;
import com.lnf.client.model.Client;
import com.lnf.client.model.Project;
import com.lnf.client.repository.ClientRepository;
import com.lnf.client.repository.ProjectRepository;
import com.lnf.dto.client.ClientEmployeeDto;
import com.lnf.dto.client.ProjectDto;
import com.lnf.dto.client.ProjectOverviewDto;
import com.lnf.dto.common.PageRequestDto;
import com.lnf.exception.LnFBadRequestException;
import com.lnf.exception.LnFEntityNotFoundException;
import com.lnf.exception.LnFException;
import com.lnf.service.common.page.PaginatedAndSortedService;
import com.lnf.service.specification.GenericSpecificationBuilder;
import com.lnf.service.timesheet.TimesheetService;
import com.lnf.util.RestUtil;
import com.lnf.util.specification.SpecificationUtil;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
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
public class ProjectService implements PaginatedAndSortedService<ProjectOverviewDto> {

    private final ProjectRepository repository;
    private final ClientRepository clientRepository;
    private final ProjectEmployeeService projectEmployeeService;
    private final TaskService taskService;
    private final CacheManager cacheManager;
    private final TimesheetService timesheetService;
    private static final String CLIENT_SERVICE = "clientService";

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

    public Page<ProjectDto> findingAllWithPagination(String search, PageRequestDto pageRequestDto) {
        Pageable pageable = PageRequest.of(pageRequestDto.getPage(), pageRequestDto.getSize(),
                RestUtil.constructSort(pageRequestDto.getSortBy(), pageRequestDto.getSortOrder()));
        Specification<Project> specification = buildProjectSpecification(search);
        Page<Project> resultPage = repository.findAll(specification, pageable);
        return resultPage.map(ProjectConverter::toTransportModel);
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
    @Cacheable(value = "projects")
    public List<ProjectOverviewDto> findProjectsByClientId(UUID clientId) {
        searchForClient(clientId);
        List<Project> projects = repository.findByClientId(clientId);
        return projects.stream().map(ProjectConverter::toMiniTransportModel)
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
    @Retry(name = CLIENT_SERVICE)
    public List<ClientEmployeeDto> findEmployeesByClientId(UUID clientId) {
        searchForClient(clientId);
        List<Project> projects = repository.findByClientId(clientId);

        return projects.stream()
                .filter(Objects::nonNull)
                .flatMap(project ->
                        projectEmployeeService.findEmployeesByProjectId(project.getId())
                                .getEmployees().stream())
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(ClientEmployeeDto::getEmployeeId, Function.identity(), (e1, e2) -> e1))
                .values()
                .stream()
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
        log.debug("Project {} successfully created", entity.getCode());
    }

    /**
     * Updates the project
     *
     * @param projectId Project Id
     * @param resource  ProjectDto
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
        log.debug("Project {} successfully updated", projectId);
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
            log.debug("Project {} successfully deleted", entity.getCode());
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
        log.debug("Projects cache cleared.");
    }

    private Page<ProjectOverviewDto> validateAndGetPages(int page, Page<Project> resultPage) {
        if (page > resultPage.getTotalPages()) {
            throw new LnFEntityNotFoundException("Total number of pages [%d], requested page [%d] does not exist".formatted(resultPage.getTotalPages(), page));
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
                orElseThrow(() -> new LnFEntityNotFoundException("Client with id [%s] does not exist".formatted(clientId)));
    }

    public Project search(UUID projectId) {
        return repository.findById(projectId).
                orElseThrow(() -> new LnFEntityNotFoundException("Project with id [%s] does not exist".formatted(projectId)));
    }

    @Retry(name = CLIENT_SERVICE)
    public List<Map<String, Object>> getTimeSheetsByClientId(UUID clientId, UUID projectId,
                                                             Optional<Integer> month, Optional<Integer> year,
                                                             String status) {

        //listing the employeeIds based on clientId
        List<String> employeeIds = repository.findEmployeeIdsByClientId(clientId);

        List<UUID> projectIds;

        if (projectId != null) {
            projectIds = Collections.singletonList(projectId);
        } else {
            //listing the projectIds based on clientId
            projectIds = repository.findProjectIdsByClientId(clientId);
        }
        //Based on clientId we are passing employeeIds and projectIds for listing the timeSheets by year and month and status
        return timesheetService.findTimeSheetsByEmployeeIds(employeeIds, projectIds, month, year, status);
    }

    public void deleteLastUploadFile() {
        List<Project> projects = repository.findByUploadedTime();
        repository.deleteAll(projects);
    }

    public void deleteProjectList(List<UUID> taskIds) {
        List<Project> projectList = taskIds.stream()
                .map(id -> repository.findById(id)
                        .orElseThrow(() -> new LnFException("Project not found for id: " + id)))
                .toList();
        repository.deleteAll(projectList);
    }

    public Page<ProjectDto> getLastUploadData(PageRequestDto pageRequest) {
        Pageable pageable = createPageable(pageRequest);
        List<Project> entities = repository.findByUploadedTime();
        return paginateProjectDetails(convertToDtos(entities), pageable);
    }

    private Pageable createPageable(PageRequestDto pageRequest) {
        return PageRequest.of(
                pageRequest.getPage(),
                pageRequest.getSize(),
                RestUtil.constructSort(pageRequest.getSortBy(), pageRequest.getSortOrder())
        );
    }

    private Page<ProjectDto> paginateProjectDetails(List<ProjectDto> projectDtos, Pageable pageable) {
        int totalRecords = projectDtos.size();
        int start = pageable.getPageSize() * pageable.getPageNumber();
        int end = Math.min(start + pageable.getPageSize(), totalRecords);
        List<ProjectDto> paginatedProjectDetails = projectDtos.subList(start, end);

        return new PageImpl<>(paginatedProjectDetails, pageable, totalRecords);
    }

    public List<ProjectDto> create(List<ProjectDto> resources, UUID clientId) {
        LnFBadRequestException.throwOnCondition(Objects::isNull, resources, "Failed to create Project with null payload");
        List<Project> entities = resources.stream()
                .map(resource -> {
                    Project project = ProjectConverter.toEntityModel(resource);
                    if (clientId != null) {
                        Client client = searchForClient(clientId);
                        project.setClient(client);
                    }
                    return project;
                })
                .toList();

        return save(entities);
    }

    private List<ProjectDto> save(List<Project> entities) {
        Set<String> existingCodes = repository.findAll().stream()
                .map(Project::getCode).collect(Collectors.toSet());

        List<ProjectDto> invalidData = new ArrayList<>();
        LocalDateTime uploadTime = LocalDateTime.now();

        for (Project project : entities) {
            if (existingCodes.contains(project.getCode())) {
                invalidData.add(ProjectConverter.toTransportModel(project));
            } else {
                try {
                    project.setUploadTime(uploadTime);
                    repository.save(project);
                } catch (RuntimeException e) {
                    String errorMessage = "Failed to save Projects";
                    throw new LnFException(errorMessage, e);
                }
            }
        }
        return invalidData;
    }

}
