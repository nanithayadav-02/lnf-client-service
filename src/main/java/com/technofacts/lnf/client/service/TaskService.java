package com.technofacts.lnf.client.service;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;
import com.technofacts.lnf.client.converter.ProjectConverter;
import com.technofacts.lnf.client.converter.TaskConverter;
import com.technofacts.lnf.client.model.Project;
import com.technofacts.lnf.client.model.Task;
import com.technofacts.lnf.client.repository.ProjectRepository;
import com.technofacts.lnf.client.repository.TaskRepository;
import com.technofacts.lnf.dto.client.ProjectDto;
import com.technofacts.lnf.dto.client.TaskDto;
import com.technofacts.lnf.exception.LnFBadRequestException;
import com.technofacts.lnf.exception.LnFEntityNotFoundException;
import com.technofacts.lnf.exception.LnFException;
import com.technofacts.lnf.util.RestUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
@Log
public class TaskService {

    private final TaskRepository repository;
    private final ProjectRepository projectRepository;

    /**
     * Return requested page with list of TaskDto objects with requested size assigned to the project.
     * Raises LnFEntityNotFoundException if the requested page is more than the total number of pages or the
     * project is not found.
     *
     * @param projectId Project Id
     * @param page Requested Page Number
     * @param size Requested size in the page
     * @return A Page object with taskDtos
     */
    public Page<TaskDto> findPaginatedByProjectId(final UUID projectId, final int page, final int size) {
        searchForProject(projectId);
        Page<Task> resultPage = repository.findByProjectId(projectId, PageRequest.of(page, size));
        return validateAndGetPages(page, resultPage);
    }

    /**
     * Return requested page with sorted list of TaskDto objects with requested size assigned to the project.
     * Raises LnFEntityNotFoundException if the requested page is more than the total number of pages or the
     * project is not found.
     *
     * @param projectId Project Id
     * @param page      Requested Page Number
     * @param size      Requested size in the page
     * @param sortBy    sorting parameter
     * @param sortOrder sort order ASC or DESC
     * @return A Page object with sorted taskDtos
     */
    public Page<TaskDto> findPaginatedAndSortedByProjectId(final UUID projectId, final int page, final int size, String sortBy, String sortOrder) {
        searchForProject(projectId);
        final Sort sortInfo = RestUtil.constructSort(sortBy, sortOrder);
        Page<Task> resultPage = repository.findByProjectId(projectId, PageRequest.of(page, size, sortInfo));
        return validateAndGetPages(page, resultPage);
    }

    /**
     * Return sorted list of all TaskDto objects assigned to a project
     *
     * @param projectId Project Id
     * @param sortBy    sorting parameter
     * @param sortOrder sort order ASC or DESC
     * @return Sorted list of all ProjectDto objects.
     */
    public List<TaskDto> findAllSortedByProjectId(final UUID projectId, String sortBy, String sortOrder) {
        searchForProject(projectId);
        final Sort sortInfo = RestUtil.constructSort(sortBy, sortOrder);
        List<Task> entities = Lists.newArrayList(repository.findByProjectId(projectId, sortInfo));
        return entities.stream().map(TaskConverter::toTransportModel).filter(Objects::nonNull).collect(Collectors.toList());
    }

    /**
     * Return list of all TaskDto objects assigned to a project
     *
     * @param projectId Project Id
     * @return List of all ProjectDto objects.
     */
    public List<TaskDto> findAllByProjectId(final UUID projectId) {
        searchForProject(projectId);
        List<Task> entities = repository.findByProjectId(projectId);
        return entities.stream().map(TaskConverter::toTransportModel).filter(Objects::nonNull).collect(Collectors.toList());
    }

    /**
     * Returns taskDto from the projectId and taskId. Raises LnFEntityNotFoundException
     * if there is no project or task.
     *
     * @param projectId Project Id
     * @param taskId Task Id
     * @return TaskDto object
     */
    public TaskDto findByProjectIdAndTaskId(UUID projectId, UUID taskId) {
        searchForProject(projectId);
        return findByTaskId(taskId);
    }

    /**
     * Returns taskDto from the taskId. Raises LnFEntityNotFoundException
     * if there is no task.
     *
     * @param taskId Task Id
     * @return TaskDto object
     */
    public TaskDto findByTaskId(UUID taskId) {
        Task entity = search(taskId);
        return TaskConverter.toTransportModel(entity);
    }

    /**
     * Creates the project
     *
     * @param projectId Project Id
     * @param resource TaskDto object
     */
    public void create(UUID projectId, TaskDto resource) {
        LnFBadRequestException.throwOnCondition(Objects::isNull, resource, String.format("Failed to create Task for project[%s] with null payload", projectId));
        Project projectEntity = searchForProject(projectId);
        Task entity = TaskConverter.toEntityModel(resource);
        entity.setProject(projectEntity);
        save(entity);
        log.info(() -> String.format("Task for Project[%s] successfully created", projectId));
    }

    /**
     * Updates the project
     *
     * @param projectId Project Id
     * @param taskId Task Id
     * @param resource TaskDto
     */
    public void update(UUID projectId, UUID taskId, TaskDto resource) {
        LnFBadRequestException.throwOnCondition(Objects::isNull, resource, String.format("Failed to update Task for project[%s] with null payload", projectId));
        searchForProject(projectId);
        Task entity = search(taskId);
        Task updatedEntity = TaskConverter.toEntityModel(resource, entity);
        save(updatedEntity);
        log.info(() -> String.format("Task[%s] for Project[%s] successfully created", taskId, projectId));
    }

    /**
     * Deletes the tasks by projectId
     *
     * @param projectId Project Id
     */
    public void deleteByProjectId(UUID projectId) {
        searchForProject(projectId);
        List<Task> entities = repository.findByProjectId(projectId);
        try {
            repository.deleteAll(entities);
            log.info(() -> String.format("Tasks for project[%s] successfully deleted", projectId));
        } catch (RuntimeException e) {
            String errorMessage = String.format("Failed to delete tasks(s) for project [%s]", projectId);
            throw new LnFException(errorMessage);
        }
    }

    /**
     * Deletes the task by projectId and taskId
     *
     * @param projectId Project Id
     * @param taskId Task Id
     */
    public void deleteByProjectIdAndTaskId(UUID projectId, UUID taskId) {
        searchForProject(projectId);
        Task entity = search(taskId);
        try {
            repository.delete(entity);
            log.info(() -> String.format("Task[%s] for project [%s] successfully deleted", taskId, projectId));
        } catch (RuntimeException e) {
            String errorMessage = String.format("Failed to delete Task[[%s] for project [%s]", taskId, projectId);
            throw new LnFException(errorMessage);
        }
    }

    private Page<TaskDto> validateAndGetPages(int page, Page<Task> resultPage) {
        if (page > resultPage.getTotalPages()) {
            throw new LnFEntityNotFoundException(String.format("Total number of pages [%d], requested page [%d] does not exist", resultPage.getTotalPages(), page));
        }
        return resultPage.map(TaskConverter::toTransportModel);
    }

    private void save(Task entity) {
        try {
            repository.save(entity);
        } catch (RuntimeException e) {
            String errorMessage = String.format("Failed to save Task for project [%s]", entity.getProject().getId());
            throw new LnFException(errorMessage);
        }
    }

    private Project searchForProject(UUID projectId) {
        return projectRepository.findById(projectId).
                orElseThrow(() -> new LnFEntityNotFoundException(String.format("Project with id [%s] does not exist", projectId)));
    }

    private Task search(UUID taskId) {
        return repository.findById(taskId).
                orElseThrow(() -> new LnFEntityNotFoundException(String.format("Task with id [%s] does not exist", taskId)));
    }



}

