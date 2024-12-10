package com.lnf.client.service;

import com.lnf.client.model.Project;
import com.lnf.client.model.ProjectEmployee;
import com.lnf.client.model.ProjectTaskEmployee;
import com.lnf.client.model.Task;
import com.lnf.client.repository.ProjectEmployeeRepository;
import com.lnf.client.repository.ProjectRepository;
import com.lnf.client.repository.ProjectTaskEmployeeRepository;
import com.lnf.client.repository.TaskRepository;
import com.lnf.dto.employee.EmployeeDto;
import com.lnf.exception.LnFEntityNotFoundException;
import com.lnf.exception.LnFException;
import com.lnf.service.employee.EmployeeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class EmployeeProjectAndTaskService {

    public static final String ACTIVE = "Active";
    private final ProjectTaskEmployeeRepository repository;

    private final TaskRepository taskRepository;

    private final ProjectRepository projectRepository;

    private final ProjectEmployeeRepository projectEmployeeRepository;

    private final EmployeeService employeeService;

    @CacheEvict(value = "projectTaskEmployees", allEntries = true)
    public void addAllEmployeesToProjectAndTask(UUID projectId, UUID taskId) {

        Project project = searchForProject(projectId);
        Task task = searchForTask(taskId);

        List<String> requiredIds = projectEmployeeRepository.findAllByProjectId(projectId).stream()
                .map(ProjectEmployee::getEmployeeId)
                .filter(employeeId -> !repository.findByTask(task)
                        .stream().map(ProjectTaskEmployee::getEmployeeId).toList().contains(employeeId))
                .toList();

        requiredIds.forEach(employeeId -> {
            EmployeeDto employeeDto = employeeService.findOne(employeeId);
            if (employeeDto != null) {
                ProjectTaskEmployee taskEmployee = new ProjectTaskEmployee();
                taskEmployee.setProject(project);
                taskEmployee.setTask(task);
                taskEmployee.setEmployeeId(employeeId);
                save(taskEmployee);
                log.debug("Successfully added the employee {} to the task {} of the project {}",
                        employeeId, task.getName(), project.getCode());

            } else {
                log.error("Failed to add the employee {} to the task {} of the project {}",
                        employeeId, task.getName(), project.getCode());
            }

        });
    }

    public void addAllActiveEmployeeToProject(UUID projectId) {
        Project project = searchForProject(projectId);
        List<String> statuses = List.of(ACTIVE);
        List<String> requiredIds = new ArrayList<>(employeeService.findByStatuses(statuses));
        requiredIds.removeAll(projectEmployeeRepository.findAllByProjectId(projectId)
                .stream().map(ProjectEmployee::getEmployeeId).toList());

        requiredIds.forEach(employeeId -> {

            ProjectEmployee projectEmployee = new ProjectEmployee();
            projectEmployee.setProject(project);
            projectEmployee.setEmployeeId(employeeId);
            save(projectEmployee);
            log.debug("Successfully added the employee {} to the project {}", employeeId, project.getCode());
        });
    }

    public void removeEmployeeFromProject(UUID projectId) {
        searchForProject(projectId);
        removeAllTasksToEmployee(projectId);

        List<String> statuses = List.of(ACTIVE);
        List<String> employeeIds = new ArrayList<>(employeeService.findByStatuses(statuses));
        employeeIds.forEach(employeeId -> {
            try {
                ProjectEmployee projectEmployee = search(projectId, employeeId);
                projectEmployeeRepository.delete(projectEmployee);
            } catch (LnFEntityNotFoundException ex) {
                log.warn(ex.getMessage());
            }
        });
    }

    public void removeEmployeesFromProjectAndTask(UUID projectId, UUID taskId) {

        List<String> statuses = List.of(ACTIVE);
        List<String> employeeIds = new ArrayList<>(employeeService.findByStatuses(statuses));

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

    public void addAllTasksToEmployee(UUID projectId) {

        Project project = searchForProject(projectId);
        searchForProjectEmployee(projectId);

        List<String> statuses = List.of(ACTIVE);
        List<String> employeeIds = new ArrayList<>(employeeService.findByStatuses(statuses));

        Set<Task> tasks = project.getTasks();

        List<String> requiredIds = projectEmployeeRepository.findAllByProjectId(projectId).stream()
                .map(ProjectEmployee::getEmployeeId)
                .filter(employeeIds::contains)
                .toList();

        requiredIds.forEach(employeeId -> tasks.forEach(task -> {
            ProjectTaskEmployee projectTaskEmployee = repository.findByProjectAndTaskAndEmployee(project, task, employeeId);
            if (projectTaskEmployee == null) {

                ProjectTaskEmployee taskEmployee = new ProjectTaskEmployee();
                taskEmployee.setEmployeeId(employeeId);
                taskEmployee.setProject(project);
                taskEmployee.setTask(task);
                save(taskEmployee);
            }
        }));

    }

    public void removeAllTasksToEmployee(UUID projectId) {
        searchForProject(projectId);

        try {
            List<ProjectTaskEmployee> byProjectId = repository.findByProjectId(projectId);
            repository.deleteAll(byProjectId);
        } catch (LnFEntityNotFoundException ex) {
            log.warn(ex.getMessage());
        }
    }

    private void save(ProjectTaskEmployee entity) {
        try {
            repository.save(entity);
        } catch (RuntimeException e) {
            String errorMessage = String.format("Failed to save Project[%s], Task [%s] and employeeId [%s]",
                    entity.getTask().getId(), entity.getEmployeeId());
            throw new LnFException(errorMessage);
        }
    }

    private void save(ProjectEmployee entity) {
        try {
            projectEmployeeRepository.save(entity);
        } catch (RuntimeException e) {
            String errorMessage = String.format("Failed to save Project [%s] and employeeId [%s]",
                    entity.getProject().getCode(), entity.getEmployeeId());
            throw new LnFException(errorMessage);
        }
    }

    private ProjectTaskEmployee search(Project project, Task task, String employeeId) {
        return repository.findByProjectAndTaskAndEmployeeId(project, task, employeeId)
                .orElseThrow(() -> new LnFEntityNotFoundException(("TaskEmployee entity with project [%s], " +
                        "taskId [%s] and employeeId [%s] does not exist").formatted(project.getId(), task.getId(), employeeId)));
    }

    private ProjectEmployee search(UUID projectId, String employeeId) {
        return projectEmployeeRepository.findByProjectIdAndEmployeeId(projectId, employeeId)
                .orElseThrow(() -> new LnFEntityNotFoundException(("ProjectEmployee entity with projectId" +
                        " [%s] and employeeId [%s] does not exist").formatted(projectId, employeeId)));
    }

    private Task searchForTask(UUID taskId) {
        return taskRepository.findById(taskId)
                .orElseThrow(() -> new LnFEntityNotFoundException("Task with id [%s] does not exist".formatted(taskId)));
    }

    private Project searchForProject(UUID projectId) {
        return projectRepository.findById(projectId).orElseThrow(() ->
                new LnFEntityNotFoundException("Project with id [%s] does not exist".formatted(projectId)));
    }

    private boolean searchForProjectEmployee(UUID projectId) {
        if (projectEmployeeRepository.existsByProjectId(projectId))
            return true;
        else
            throw new LnFEntityNotFoundException("Project with id [%s] does not Allocated to Employees".formatted(projectId));
    }

}
