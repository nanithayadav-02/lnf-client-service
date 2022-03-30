package com.technofacts.lnf.client.service;

import com.technofacts.lnf.dto.client.EmployeeProjectDto;
import com.technofacts.lnf.dto.client.EmployeeProjectTaskDto;
import com.technofacts.lnf.dto.client.EmployeeTaskDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

@Service
@Transactional
@RequiredArgsConstructor
@Log
public class EmployeeTaskService {

    private final EmployeeProjectService employeeProjectService;
    private final EmployeeProjectTaskService employeeProjectTaskService;
    private static final String ACTIVE = "Active";

    public EmployeeTaskDto findEmployeeTasks(final String employeeId) {
        EmployeeProjectDto employeeProjectDto = employeeProjectService.findProjectsByEmployeeId(employeeId);

        EmployeeTaskDto employeeTaskDto = new EmployeeTaskDto();
        employeeTaskDto.setEmployeeId(employeeId);

        employeeProjectDto.getProjects().stream()
                .filter(projectDto -> ACTIVE.equalsIgnoreCase(projectDto.getStatus()))
                .forEach(projectDto -> {
                    EmployeeProjectTaskDto employeeProjectTaskDto =
                            employeeProjectTaskService.findTasksByEmployeeIdAndProjectId(employeeId, projectDto.getId());
                    if (!CollectionUtils.isEmpty(employeeProjectTaskDto.getTasks())) {
                        employeeTaskDto.getTasks().addAll(employeeProjectTaskDto.getTasks());
                    }
                });

        return employeeTaskDto;
    }

}
