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

import com.lnf.client.converter.TaskConverter;
import com.lnf.client.repository.ProjectRepository;
import com.lnf.client.repository.ProjectTaskEmployeeRepository;
import com.lnf.client.model.Project;
import com.lnf.client.model.ProjectTaskEmployee;
import com.lnf.client.model.Task;
import com.lnf.dto.client.EmployeeProjectTaskDto;
import com.lnf.dto.client.TaskDto;
import com.lnf.dto.employee.EmployeeDto;
import com.lnf.exception.LnFEntityNotFoundException;
import com.lnf.service.employee.EmployeeService;
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
