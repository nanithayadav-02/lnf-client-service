package com.technofacts.lnf.client.service;

import java.util.List;
import java.util.UUID;

import com.technofacts.lnf.client.model.Project;
import com.technofacts.lnf.client.model.Task;
import com.technofacts.lnf.client.model.ProjectTaskEmployee;
import com.technofacts.lnf.client.repository.ProjectRepository;
import com.technofacts.lnf.client.repository.ProjectTaskEmployeeRepository;
import com.technofacts.lnf.client.repository.TaskRepository;
import com.technofacts.lnf.dto.client.TaskEmployeeDto;
import com.technofacts.lnf.exception.LnFEntityNotFoundException;
import com.technofacts.lnf.exception.LnFException;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
@Log
public class ProjectTaskEmployeeService {

    private final ProjectTaskEmployeeRepository repository;
    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;

    /**
     * Get employees associated to the task
     *
     * @param  projectId Project Id
     * @param taskId Task Id
     * @return TaskEmployeeDto
     */
    public TaskEmployeeDto findEmployeesByProjectIdAndTaskId(final UUID projectId, final UUID taskId) {
        searchForProject(projectId);
        searchForTask(taskId);
        return null;
    }

    /**
     * Add employees to the task
     *
     * @param  projectId Project Id
     * @param taskId   Task Id
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
                ProjectTaskEmployee taskEmployee = new ProjectTaskEmployee();
                taskEmployee.setProject(project);
                taskEmployee.setTask(task);
                taskEmployee.setEmployeeId(employeeId);
                save(taskEmployee);
            }
        });

    }

    /**
     * Remove employees from
     * the task
     *
     * @param  projectId Project Id
     * @param taskId   Task Id
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

    public void delete(ProjectTaskEmployee entity) {
        try {
            repository.delete(entity);
            log.info(() -> String.format("Employee [%s] is successfully removed from the Task[%s]", entity.getEmployeeId(), entity.getTask().getId()));
        } catch (RuntimeException e) {
            String errorMessage = String.format("Failed to remove Employee [%s] from the Task[%s]", entity.getEmployeeId(),entity.getTask().getId());
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


}
