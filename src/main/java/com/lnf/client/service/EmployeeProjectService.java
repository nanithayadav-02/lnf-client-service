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
import com.lnf.client.model.ProjectEmployee;
import com.lnf.client.repository.ProjectEmployeeRepository;
import com.lnf.dto.client.EmployeeProjectDto;
import com.lnf.dto.client.ProjectDto;
import com.lnf.dto.employee.EmployeeDto;
import com.lnf.exception.LnFEntityNotFoundException;
import com.lnf.service.employee.EmployeeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class EmployeeProjectService {

    private final ProjectEmployeeRepository repository;
    private final EmployeeService employeeService;

    /**
     * Get projects associated to the employee
     *
     * @param employeeId Employee Id
     * @return ProjectEmployeeDto
     */
    public EmployeeProjectDto findProjectsByEmployeeId(final String employeeId) {

        searchForEmployee(employeeId);
        List<ProjectEmployee> projectEmployees = searchForProjects(employeeId);
        List<Project> projects = projectEmployees.stream().map(ProjectEmployee::getProject).toList();
        List<ProjectDto> projectDtos = projects.stream()
                .map(ProjectConverter::toTransportModel)
                .filter(Objects::nonNull)
                .toList();

        EmployeeProjectDto employeeProjectDto = new EmployeeProjectDto();
        employeeProjectDto.setEmployeeId(employeeId);
        employeeProjectDto.getProjects().addAll(projectDtos);

        return employeeProjectDto;

    }

    private List<ProjectEmployee> searchForProjects(String employeeId) {
        return repository.findByEmployeeId(employeeId);
    }

    private EmployeeDto searchForEmployee(String employeeId) {
        try {
            return employeeService.findOne(employeeId);
        } catch (RuntimeException ex) {
            throw new LnFEntityNotFoundException("Failed to find the employee [%s] ".formatted(employeeId));
        }
    }

}
