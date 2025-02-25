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
import com.lnf.client.converter.TaskConverter;
import com.lnf.client.model.Project;
import com.lnf.client.model.Task;
import com.lnf.client.repository.ProjectRepository;
import com.lnf.client.repository.TaskRepository;
import com.lnf.dto.client.TaskDto;
import com.lnf.exception.LnFBadRequestException;
import com.lnf.exception.LnFEntityNotFoundException;
import com.lnf.exception.LnFException;
import com.lnf.util.RestUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class TaskService {

    private final TaskRepository repository;
    private final ProjectRepository projectRepository;
    private final ProjectTaskEmployeeService projectTaskEmployeeService;

    /**
     * Return requested page with list of TaskDto objects with requested size assigned to the project.
     * Raises LnFEntityNotFoundException if the requested page is more than the total number of pages or the
     * project is not found.
     *
     * @param projectId Project Id
     * @param page      Requested Page Number
     * @param size      Requested size in the page
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
        return entities.stream().map(TaskConverter::toTransportModel).filter(Objects::nonNull).toList();
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
        return entities.stream().map(TaskConverter::toTransportModel).filter(Objects::nonNull).toList();
    }

    /**
     * Returns taskDto from the projectId and taskId. Raises LnFEntityNotFoundException
     * if there is no project or task.
     *
     * @param projectId Project Id
     * @param taskId    Task Id
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
     * @param resource  TaskDto object
     */
    public void create(UUID projectId, TaskDto resource) {
        LnFBadRequestException.throwOnCondition(Objects::isNull, resource, "Failed to create Task for project[%s] with null payload".formatted(projectId));
        Project projectEntity = searchForProject(projectId);
        Task entity = TaskConverter.toEntityModel(resource);
        entity.setProject(projectEntity);
        save(entity);
        log.debug("Task for Project {} successfully created", projectId);
    }

    /**
     * Updates the project
     *
     * @param projectId Project Id
     * @param taskId    Task Id
     * @param resource  TaskDto
     */
    public void update(UUID projectId, UUID taskId, TaskDto resource) {
        LnFBadRequestException.throwOnCondition(Objects::isNull, resource, "Failed to update Task for project[%s] with null payload".formatted(projectId));
        searchForProject(projectId);
        Task entity = search(taskId);
        Task updatedEntity = TaskConverter.toEntityModel(resource, entity);
        save(updatedEntity);
        log.debug("Task {} for Project {} successfully created", taskId, projectId);
    }

    /**
     * Deletes the tasks by projectId
     *
     * @param projectId Project Id
     */
    public void deleteByProjectId(UUID projectId) {
        searchForProject(projectId);
        List<Task> entities = repository.findByProjectId(projectId);
        deleteTasks(projectId, entities);
    }

    private void deleteTasks(UUID projectId, List<Task> tasks) {
        tasks.stream()
                .filter(Objects::nonNull)
                .forEach(projectTaskEmployeeService::deleteAllByTask);

        try {
            repository.deleteAll(tasks);
            log.debug("Tasks for project {} successfully deleted", projectId);
        } catch (RuntimeException e) {
            String errorMessage = "Failed to delete tasks for project[%s]".formatted(projectId);
            throw new LnFException(errorMessage);
        }
    }

    /**
     * Deletes the task by projectId and taskId
     *
     * @param projectId Project Id
     * @param taskId    Task Id
     */
    public void deleteByProjectIdAndTaskId(UUID projectId, UUID taskId) {
        searchForProject(projectId);
        Task entity = search(taskId);
        try {
            deleteTasks(projectId, (List.of(entity)));
            repository.delete(entity);
            log.debug("Task {} for project {} successfully deleted", taskId, projectId);
        } catch (RuntimeException e) {
            String errorMessage = "Failed to delete Task [%s] for project [%s]".formatted(taskId, projectId);
            throw new LnFException(errorMessage);
        }
    }

    private Page<TaskDto> validateAndGetPages(int page, Page<Task> resultPage) {
        if (page > resultPage.getTotalPages()) {
            throw new LnFEntityNotFoundException("Total number of pages [%d], requested page [%d] does not exist".formatted(resultPage.getTotalPages(), page));
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
                orElseThrow(() -> new LnFEntityNotFoundException("Project with id [%s] does not exist".formatted(projectId)));
    }

    private Task search(UUID taskId) {
        return repository.findById(taskId).
                orElseThrow(() -> new LnFEntityNotFoundException("Task with id [%s] does not exist".formatted(taskId)));
    }

    public void deleteLastUploadFile() {
        List<Task> tasks = repository.findByUploadedTime();
        repository.deleteAll(tasks);
    }

    public void deleteTaskList(List<UUID> taskIds) {
        List<Task> taskList = taskIds.stream()
                .map(id -> repository.findById(id)
                        .orElseThrow(() -> new LnFException("Task not found for id: " + id)))
                .toList();
        repository.deleteAll(taskList);
    }

    public List<TaskDto> getLastUploadData() {
        List<Task> entities = repository.findByUploadedTime();
        return entities.stream().map(TaskConverter::toTransportModel).filter(Objects::nonNull).toList();
    }

}


