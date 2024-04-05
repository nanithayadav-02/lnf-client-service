package com.technofacts.lnf.client.service;

import com.technofacts.lnf.client.model.Project;
import com.technofacts.lnf.client.model.ProjectEmployee;
import com.technofacts.lnf.client.model.ProjectTaskEmployee;
import com.technofacts.lnf.client.model.Task;
import com.technofacts.lnf.client.repository.ProjectEmployeeRepository;
import com.technofacts.lnf.client.repository.ProjectRepository;
import com.technofacts.lnf.client.repository.ProjectTaskEmployeeRepository;
import com.technofacts.lnf.client.repository.TaskRepository;
import com.technofacts.lnf.dto.client.ProjectTaskEmployeeDto;
import com.technofacts.lnf.dto.employee.EmployeeDto;
import com.technofacts.lnf.exception.LnFEntityNotFoundException;
import com.technofacts.lnf.exception.LnFException;
import com.technofacts.lnf.service.employee.EmployeeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Log
public class ProjectTaskEmployeeService {

    private final ProjectTaskEmployeeRepository repository;
    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final ProjectEmployeeRepository projectEmployeeRepository;
    private final EmployeeService employeeService;

    /**
     * Get employees associated to the task
     *
     * @param projectId Project Id
     * @param taskId    Task Id
     * @return TaskEmployeeDto
     */
    public ProjectTaskEmployeeDto findEmployeesByProjectIdAndTaskId(final UUID projectId, final UUID taskId) {
        Project project = searchForProject(projectId);
        Task task = searchForTask(taskId);
        List<ProjectTaskEmployee> projectTaskEmployees = repository.findByProjectAndTask(project, task);
        List<String> employeeIds = projectTaskEmployees.stream().map(ProjectTaskEmployee::getEmployeeId).toList();

        // Get the list of employee details from the Employee microservice
        List<EmployeeDto> employeeDtos = employeeService.findByEmployeeIds(employeeIds);

        // Return the ProjectEmployeeDtos
        ProjectTaskEmployeeDto projectTaskEmployeeDto = new ProjectTaskEmployeeDto();
        projectTaskEmployeeDto.setProjectId(project.getId());
        projectTaskEmployeeDto.setProjectCode(project.getCode());
        projectTaskEmployeeDto.setTaskId(taskId);
        projectTaskEmployeeDto.setTaskName(task.getName());
        projectTaskEmployeeDto.setEmployees(employeeDtos);

        return projectTaskEmployeeDto;
    }

    /**
     * Add employees to the task
     *
     * @param projectId   Project Id
     * @param taskId      Task Id
     * @param employeeIds List of Strings
     */
    public void addEmployeesToProjectAndTask(UUID projectId, UUID taskId, List<String> employeeIds) {
        Project project = searchForProject(projectId);
        Task task = searchForTask(taskId);
        employeeIds.forEach(employeeId -> {
            try {
                search(project, task, employeeId);
                log.info(String.format("Employee[%s] is already associated to the Task [%s]", employeeId, task.getId()));

            } catch (LnFEntityNotFoundException ex) {
                EmployeeDto employeeDto = employeeService.findOne(employeeId);
                if (employeeDto != null) {
                    ProjectTaskEmployee taskEmployee = new ProjectTaskEmployee();
                    taskEmployee.setProject(project);
                    taskEmployee.setTask(task);
                    taskEmployee.setEmployeeId(employeeId);
                    save(taskEmployee);
                    log.log(Level.INFO, String.format("Successfully added the employee [%s] to the task [%s] of the project [%s]", employeeId, task.getName(), project.getCode()));

                } else {
                    log.log(Level.SEVERE, String.format("Failed to add the employee [%s] to the task [%s] of the project [%s]", employeeId, task.getName(), project.getCode()));
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
    public void removeEmployeesFromProjectAndTask(UUID projectId, UUID taskId, List<String> employeeIds) {
        Project project = searchForProject(projectId);
        Task task = searchForTask(taskId);
        employeeIds.forEach(employeeId -> {
            try {
                ProjectTaskEmployee taskEmployee = search(project, task, employeeId);
                repository.delete(taskEmployee);
            } catch (LnFEntityNotFoundException ex) {
                log.warning(ex.getMessage());
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
            log.info(() -> String.format("projectTaskEmployees is successfully removed from the Task[%s]", task.getId()));
        } catch (RuntimeException e) {
            String errorMessage = String.format("Failed to remove projectTaskEmployees from the Task[%s]", task.getId());
            throw new LnFException(errorMessage, e);
        }
    }

    public void delete(ProjectTaskEmployee entity) {
        try {
            repository.delete(entity);
            log.info(() -> String.format("Employee [%s] is successfully removed from the Task[%s]", entity.getEmployeeId(), entity.getTask().getId()));
        } catch (RuntimeException e) {
            String errorMessage = String.format("Failed to remove Employee [%s] from the Task[%s]", entity.getEmployeeId(), entity.getTask().getId());
            throw new LnFException(errorMessage, e);
        }
    }

    private ProjectTaskEmployee search(Project project, Task task, String employeeId) {
        return repository.findByProjectAndTaskAndEmployeeId(project, task, employeeId).orElseThrow(() -> new LnFEntityNotFoundException(String.format("TaskEmployee entity with project [%s], taskId [%s] and employeeId [%s] does not exist", project.getId(), task.getId(), employeeId)));
    }

    private Task searchForTask(UUID taskId) {
        return taskRepository.findById(taskId).orElseThrow(() -> new LnFEntityNotFoundException(String.format("Task with id [%s] does not exist", taskId)));
    }

    private Project searchForProject(UUID projectId) {
        return projectRepository.findById(projectId).orElseThrow(() -> new LnFEntityNotFoundException(String.format("Project with id [%s] does not exist", projectId)));
    }


    public void addAllEmployeesToProjectAndTask(UUID projectId, UUID taskId) {

        Project project = searchForProject(projectId);
        Task task = searchForTask(taskId);

        List<String> requiredIds = projectEmployeeRepository.findAllByProjectId(projectId).stream()
                .map(ProjectEmployee::getEmployeeId)
                .filter(employeeId -> !repository.findByTask(task).stream().map(ProjectTaskEmployee::getEmployeeId).collect(Collectors.toList()).contains(employeeId))
                .toList();

        requiredIds.forEach(employeeId -> {
                EmployeeDto employeeDto = employeeService.findOne(employeeId);
                if (employeeDto != null) {
                    ProjectTaskEmployee taskEmployee = new ProjectTaskEmployee();
                    taskEmployee.setProject(project);
                    taskEmployee.setTask(task);
                    taskEmployee.setEmployeeId(employeeId);
                    save(taskEmployee);
                    log.log(Level.INFO, String.format("Successfully added the employee [%s] to the task [%s] of the project [%s]", employeeId, task.getName(), project.getCode()));

                } else {
                    log.log(Level.SEVERE, String.format("Failed to add the employee [%s] to the task [%s] of the project [%s]", employeeId, task.getName(), project.getCode()));
                }

        });
    }
}
