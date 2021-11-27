package com.technofacts.lnf.client.service;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import com.technofacts.lnf.client.converter.TaskConverter;
import com.technofacts.lnf.client.dto.TaskDto;
import com.technofacts.lnf.client.exception.LnFBadRequestException;
import com.technofacts.lnf.client.exception.LnFEntityNotFoundException;
import com.technofacts.lnf.client.exception.LnFException;
import com.technofacts.lnf.client.model.Project;
import com.technofacts.lnf.client.model.Task;
import com.technofacts.lnf.client.repository.ProjectRepository;
import com.technofacts.lnf.client.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
@Log
public class TaskService {

    private final TaskRepository repository;
    private final ProjectRepository projectRepository;

    public List<TaskDto> findAll() {
        List<Task> entities = repository.findAll();
        return entities.stream().map(TaskConverter::toTransportModel).filter(Objects::nonNull).collect(Collectors.toList());
    }

    public List<TaskDto> findByProjectId(UUID projectId) {
        searchForProject(projectId);
        List<Task> entities = repository.findByProjectId(projectId);
        return entities.stream().map(TaskConverter::toTransportModel).filter(Objects::nonNull).collect(Collectors.toList());
    }

    public TaskDto findById(UUID projectId, UUID taskId) {
        searchForProject(projectId);
        return TaskConverter.toTransportModel(searchForTask(taskId));
    }

    public void create(UUID projectId, TaskDto resource) {
        LnFBadRequestException.throwOnCondition(Objects::isNull, resource, String.format("Failed to create Task for project[%s] with null payload", projectId));
        Project projectEntity = searchForProject(projectId);
        Task entity = TaskConverter.toEntityModel(resource);
        entity.setProject(projectEntity);
        save(entity);
        log.info(() -> String.format("Task for Project[%s] successfully created", projectId));
    }

    public void update(UUID projectId, UUID taskId, TaskDto resource) {
        LnFBadRequestException.throwOnCondition(Objects::isNull, resource, String.format("Failed to update Task for project[%s] with null payload", projectId));
        searchForProject(projectId);
        Task entity = searchForTask(taskId);
        Task updatedEntity = TaskConverter.toEntityModel(resource, entity);
        save(updatedEntity);
        log.info(() -> String.format("Task for Project[%s] successfully created", taskId));
    }

    public void deleteById(UUID projectId, UUID taskId) {
        searchForProject(projectId);
        Task entity = searchForTask(taskId);
        try {
            repository.delete(entity);
            log.info(() -> String.format("Task[%s] for project [%s] successfully deleted", taskId, projectId));
        } catch (RuntimeException e) {
            String errorMessage = String.format("Failed to delete Task[[%s] for project [%s]", taskId, projectId);
            throw new LnFException(errorMessage);
        }
    }

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

    private Task searchForTask(UUID projectId) {
        return repository.findById(projectId).
                orElseThrow(() -> new LnFEntityNotFoundException(String.format("Task with id [%s] does not exist", projectId)));
    }

}

