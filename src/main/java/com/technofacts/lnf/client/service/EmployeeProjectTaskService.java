package com.technofacts.lnf.client.service;

import com.technofacts.lnf.client.converter.TaskConverter;
import com.technofacts.lnf.client.model.Project;
import com.technofacts.lnf.client.model.ProjectTaskEmployee;
import com.technofacts.lnf.client.model.Task;
import com.technofacts.lnf.client.repository.ProjectRepository;
import com.technofacts.lnf.client.repository.ProjectTaskEmployeeRepository;
import com.technofacts.lnf.dto.client.EmployeeProjectTaskDto;
import com.technofacts.lnf.dto.client.TaskDto;
import com.technofacts.lnf.dto.employee.EmployeeDto;
import com.technofacts.lnf.exception.LnFEntityNotFoundException;
import com.technofacts.lnf.service.employee.EmployeeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class EmployeeProjectTaskService {

    private final ProjectTaskEmployeeRepository repository;
    private final ProjectRepository projectRepository;
    private final EmployeeService employeeService;

    /**
     * Get projects associated to the employee
     *
     * @param employeeId Employee Id
     * @return ProjectEmployeeDto
     */
    public EmployeeProjectTaskDto findTasksByEmployeeIdAndProjectId(final String employeeId, final UUID projectId) {

        searchForEmployee(employeeId);
        Project project = searchForProject(projectId);
        List<ProjectTaskEmployee> projectTaskEmployees = search(project, employeeId);
        List<Task> tasks = projectTaskEmployees.stream().map(ProjectTaskEmployee::getTask).toList();
        List<TaskDto> taskDtos = tasks.stream()
                .map(TaskConverter::toTransportModel)
                .filter(Objects::nonNull)
                .toList();

        EmployeeProjectTaskDto employeeProjectTaskDto = new EmployeeProjectTaskDto();
        employeeProjectTaskDto.setEmployeeId(employeeId);
        employeeProjectTaskDto.setProjectId(projectId);
        employeeProjectTaskDto.getTasks().addAll(taskDtos);

        return employeeProjectTaskDto;

    }

    private List<ProjectTaskEmployee> search(Project project, String employeeId) {
        return repository.findByProjectAndEmployeeId(project, employeeId);
    }

    private Project searchForProject(UUID projectId) {
        return projectRepository.findById(projectId).orElseThrow(() -> new LnFEntityNotFoundException(String.format("Project with id [%s] does not exist", projectId)));
    }

    private EmployeeDto searchForEmployee(String employeeId) {
        try {
            return employeeService.findOne(employeeId);
        } catch (RuntimeException ex) {
            throw new LnFEntityNotFoundException(String.format("Failed to find the employee [%s] ", employeeId));
        }
    }
}
