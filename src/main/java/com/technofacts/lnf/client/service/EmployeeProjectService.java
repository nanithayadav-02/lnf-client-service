package com.technofacts.lnf.client.service;

import com.technofacts.lnf.client.converter.ProjectConverter;
import com.technofacts.lnf.client.model.Project;
import com.technofacts.lnf.client.model.ProjectEmployee;
import com.technofacts.lnf.client.repository.ProjectEmployeeRepository;
import com.technofacts.lnf.dto.client.EmployeeProjectDto;
import com.technofacts.lnf.dto.client.ProjectDto;
import com.technofacts.lnf.dto.employee.EmployeeDto;
import com.technofacts.lnf.exception.LnFEntityNotFoundException;
import com.technofacts.lnf.service.employee.EmployeeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@Transactional
@RequiredArgsConstructor
@Log
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

        EmployeeDto employeeDto = searchForEmployee(employeeId);
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
            throw new LnFEntityNotFoundException(String.format("Failed to find the employee [%s] ", employeeId));
        }
    }

}
