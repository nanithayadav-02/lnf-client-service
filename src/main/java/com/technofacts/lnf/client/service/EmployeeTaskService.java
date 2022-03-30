package com.technofacts.lnf.client.service;

import com.technofacts.lnf.dto.client.EmployeeProjectDto;
import com.technofacts.lnf.dto.client.EmployeeProjectTaskDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@Transactional
@RequiredArgsConstructor
@Log
public class EmployeeTaskService {

    private final EmployeeProjectService employeeProjectService;
    private final EmployeeProjectTaskService employeeProjectTaskService;
    private static final String ACTIVE = "Active";

    public List<EmployeeProjectTaskDto> findEmployeeTasks(final String employeeId) {
        EmployeeProjectDto employeeProjectDto = employeeProjectService.findProjectsByEmployeeId(employeeId);
        List<EmployeeProjectTaskDto> employeeProjectTaskDtoList = new ArrayList<>();
        employeeProjectDto.getProjects().stream()
                .filter(projectDto -> ACTIVE.equalsIgnoreCase(projectDto.getStatus()))
                .filter(Objects::nonNull)
                .forEach(projectDto -> {
                    employeeProjectTaskDtoList
                            .add(employeeProjectTaskService.findTasksByEmployeeIdAndProjectId(employeeId, projectDto.getId()));
                });
        return employeeProjectTaskDtoList;
    }

}
