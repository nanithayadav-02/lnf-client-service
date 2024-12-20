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

import com.lnf.client.converter.ProjectConverter;
import com.lnf.client.model.Project;
import com.lnf.client.model.ProjectTaskEmployee;
import com.lnf.client.model.Task;
import com.lnf.client.repository.ProjectEmployeeRepository;
import com.lnf.client.repository.ProjectRepository;
import com.lnf.client.repository.ProjectTaskEmployeeRepository;
import com.lnf.client.repository.TaskRepository;
import com.lnf.dto.client.ClientEmployeeDto;
import com.lnf.dto.client.ProjectTaskEmployeeDto;
import com.lnf.dto.employee.EmployeeDto;
import com.lnf.exception.LnFEntityNotFoundException;
import com.lnf.exception.LnFException;
import com.lnf.service.employee.EmployeeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ProjectTaskEmployeeService {

    private final ProjectTaskEmployeeRepository repository;
    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final ProjectEmployeeRepository projectEmployeeRepository;
    private final EmployeeService employeeService;
    private final CacheManager cacheManager;

    /**
     * Get employees associated to the task
     *
     * @param projectId Project Id
     * @param taskId    Task Id
     * @return TaskEmployeeDto
     */
    @Cacheable(value = "projectTaskEmployees")
    public ProjectTaskEmployeeDto findEmployeesByProjectIdAndTaskId(final UUID projectId, final UUID taskId) {
        Project project = searchForProject(projectId);
        Task task = searchForTask(taskId);
        List<ProjectTaskEmployee> projectTaskEmployees = repository.findByProjectAndTask(project, task);

        // Get the list of employee details from the Employee microservice
        List<ClientEmployeeDto> employeeDtos = fetchEmployeeDetails(projectTaskEmployees);

        return createProjectTaskEmployeeDto(taskId, project, task, employeeDtos);
    }

    @Cacheable(value = "projectTaskEmployees")
    public Map<String, Object> findAllAssignedEmployees(final UUID projectId, final UUID taskId, int page, Integer size) {
        Project project = searchForProject(projectId);
        Task task = searchForTask(taskId);
        List<ProjectTaskEmployee> projectTaskEmployees = repository.findByProjectAndTask(project, task);
        size = (size == null || size <= 0) ? projectTaskEmployees.size() : size;
        List<ClientEmployeeDto> employeeDtos = fetchEmployeeDetails(paginateEmployees(projectTaskEmployees, page, size));
        ProjectTaskEmployeeDto dto = createProjectTaskEmployeeDto(taskId, project, task, employeeDtos);
        return createPaginationContent(projectTaskEmployees.size(), size, dto);
    }

    private ProjectTaskEmployeeDto createProjectTaskEmployeeDto(UUID taskId, Project project, Task task, List<ClientEmployeeDto> employeeDtos) {
        // Return the ProjectTaskEmployeeDtos
        ProjectTaskEmployeeDto projectTaskEmployeeDto = new ProjectTaskEmployeeDto();
        projectTaskEmployeeDto.setProjectId(project.getId());
        projectTaskEmployeeDto.setProjectCode(project.getCode());
        projectTaskEmployeeDto.setTaskId(taskId);
        projectTaskEmployeeDto.setTaskName(task.getName());
        projectTaskEmployeeDto.setEmployees(employeeDtos);

        return projectTaskEmployeeDto;
    }

    private List<ProjectTaskEmployee> paginateEmployees(List<ProjectTaskEmployee> employees, int page, int size) {
        int fromIndex = page * size;
        int toIndex = Math.min(fromIndex + size, employees.size());
        return employees.subList(fromIndex, toIndex);
    }

    private List<ClientEmployeeDto> fetchEmployeeDetails(List<ProjectTaskEmployee> pagedEmployees) {
        List<String> employeeIds = pagedEmployees.stream()
                .map(ProjectTaskEmployee::getEmployeeId)
                .toList();
        return employeeService.findByEmployeeIds(employeeIds)
                .stream()
                .map(ProjectConverter::mapToClientEmployee)
                .filter(Objects::nonNull)
                .toList();
    }

    private Map<String, Object> createPaginationContent(int totalEmployees, Integer size,
                                                        ProjectTaskEmployeeDto projectTaskEmployeeDto) {
        int totalPages = (totalEmployees + size - 1) / size;

        Map<String, Object> result = new HashMap<>();
        result.put("ProjectTaskEmployees", projectTaskEmployeeDto);
        result.put("totalElements", totalEmployees);
        result.put("totalPages", totalPages);
        return result;
    }

    /**
     * Add employees to the task
     *
     * @param projectId   Project Id
     * @param taskId      Task Id
     * @param employeeIds List of Strings
     */
    @CacheEvict(value = "projectTaskEmployees", allEntries = true)
    public void addEmployeesToProjectAndTask(UUID projectId, UUID taskId, List<String> employeeIds) {
        Project project = searchForProject(projectId);
        Task task = searchForTask(taskId);
        employeeIds.forEach(employeeId -> {
            try {
                search(project, task, employeeId);
                log.debug("Employee {} is already associated to the Task {}", employeeId, task.getId());

            } catch (LnFEntityNotFoundException ex) {
                EmployeeDto employeeDto = employeeService.findOne(employeeId);
                if (employeeDto != null) {
                    ProjectTaskEmployee taskEmployee = new ProjectTaskEmployee();
                    taskEmployee.setProject(project);
                    taskEmployee.setTask(task);
                    taskEmployee.setEmployeeId(employeeId);
                    save(taskEmployee);
                    log.debug("Successfully added the employee {} to the task {} of the project {}", employeeId, task.getName(), project.getCode());

                } else {
                    log.error("Failed to add the employee {} to the task {} of the project {}", employeeId, task.getName(), project.getCode());
                }
            }
        });
    }

    /**
     * Remove employees from
     * the task
     *
     * @param projectId   Project Id
     * @param taskId      Task Id
     * @param employeeIds List of Strings
     */
    @CacheEvict(value = "projectTaskEmployees", allEntries = true)
    public void removeEmployeesFromProjectAndTask(UUID projectId, UUID taskId, List<String> employeeIds) {
        Project project = searchForProject(projectId);
        Task task = searchForTask(taskId);
        employeeIds.forEach(employeeId -> {
            try {
                ProjectTaskEmployee taskEmployee = search(project, task, employeeId);
                repository.delete(taskEmployee);
            } catch (LnFEntityNotFoundException ex) {
                log.warn(ex.getMessage());
            }
        });
    }

    private void save(ProjectTaskEmployee entity) {
        try {
            repository.save(entity);
        } catch (RuntimeException e) {
            String errorMessage = String.format("Failed to save Project[%s], Task [%s] and employeeId [%s]", entity.getProject().getId(), entity.getTask().getId(), entity.getEmployeeId());
            throw new LnFException(errorMessage);
        }
    }

    public void deleteAllByTask(Task task) {
        List<ProjectTaskEmployee> projectTaskEmployees = repository.findByTask(task);
        try {
            repository.deleteAll(projectTaskEmployees);
            log.debug("projectTaskEmployees is successfully removed from the Task {}", task.getId());
        } catch (RuntimeException e) {
            String errorMessage = "Failed to remove projectTaskEmployees from the Task[%s]".formatted(task.getId());
            throw new LnFException(errorMessage, e);
        }
    }

    public void delete(ProjectTaskEmployee entity) {
        try {
            repository.delete(entity);
            log.debug("Employee {} is successfully removed from the Task {}", entity.getEmployeeId(), entity.getTask().getId());
        } catch (RuntimeException e) {
            String errorMessage = String.format("Failed to remove Employee [%s] from the Task[%s]", entity.getEmployeeId(), entity.getTask().getId());
            throw new LnFException(errorMessage, e);
        }
    }

    /**
     * Clears the cache for projectTaskEmployees.
     */
    public void clearProjectTaskEmployeesCache() {
        Objects.requireNonNull(cacheManager.getCache("projectTaskEmployees")).clear();
        log.debug("ProjectTaskEmployees cache cleared.");
    }

    private ProjectTaskEmployee search(Project project, Task task, String employeeId) {
        return repository.findByProjectAndTaskAndEmployeeId(project, task, employeeId).orElseThrow(() -> new LnFEntityNotFoundException("TaskEmployee entity with project [%s], taskId [%s] and employeeId [%s] does not exist".formatted(project.getId(), task.getId(), employeeId)));
    }

    private Task searchForTask(UUID taskId) {
        return taskRepository.findById(taskId).orElseThrow(() -> new LnFEntityNotFoundException("Task with id [%s] does not exist".formatted(taskId)));
    }

    private Project searchForProject(UUID projectId) {
        return projectRepository.findById(projectId).orElseThrow(() -> new LnFEntityNotFoundException("Project with id [%s] does not exist".formatted(projectId)));
    }

    public void addAllTasksToEmployee(UUID projectId, List<String> employeeIds) {
        Project project = searchForProject(projectId);
        Set<Task> tasks = project.getTasks();
        employeeIds.forEach(employeeId ->
                tasks.forEach(
                        task -> {
                            ProjectTaskEmployee taskEmployee = new ProjectTaskEmployee();
                            taskEmployee.setEmployeeId(employeeId);
                            taskEmployee.setProject(project);
                            taskEmployee.setTask(task);
                            save(taskEmployee);

                        }
                ));
    }

    public void addAllTasksToEmployee(UUID projectId, String employeeId) {
        Project project = searchForProject(projectId);
        Set<Task> tasks = project.getTasks();
        tasks.forEach(
                task -> {
                    ProjectTaskEmployee taskEmployee = new ProjectTaskEmployee();
                    taskEmployee.setEmployeeId(employeeId);
                    taskEmployee.setProject(project);
                    taskEmployee.setTask(task);
                    save(taskEmployee);

                }
        );
    }
}
